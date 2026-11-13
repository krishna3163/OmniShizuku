package moe.shizuku.manager.adb

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.android.org.conscrypt.Conscrypt
import java.io.Closeable
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.net.ssl.SSLSocket

private const val TAG = "AdbPairClient"

private const val kCurrentKeyHeaderVersion = 1.toByte()
private const val kMinSupportedKeyHeaderVersion = 1.toByte()
private const val kMaxSupportedKeyHeaderVersion = 1.toByte()
private const val kMaxPeerInfoSize = 8192
private const val kMaxPayloadSize = kMaxPeerInfoSize * 2

private const val kExportedKeyLabel = "adb-label\u0000"
private const val kExportedKeySize = 64

private const val kPairingPacketHeaderSize = 6

private class PeerInfo(
        val type: Byte,
        data: ByteArray) {

    val data = ByteArray(kMaxPeerInfoSize - 1)

    init {
        data.copyInto(this.data, 0, 0, data.size.coerceAtMost(kMaxPeerInfoSize - 1))
    }

    enum class Type(val value: Byte) {
        ADB_RSA_PUB_KEY(0.toByte()),
        ADB_DEVICE_GUID(0.toByte()),
    }

    fun writeTo(buffer: ByteBuffer) {
        buffer.run {
            put(type)
            put(data)
        }

        Log.d(TAG, "write PeerInfo ${toStringShort()}")
    }

    override fun toString(): String {
        return "PeerInfo(${toStringShort()})"
    }

    fun toStringShort(): String {
        return "type=$type, data=${data.contentToString()}"
    }

    companion object {

        fun readFrom(buffer: ByteBuffer): PeerInfo {
            val type = buffer.get()
            val data = ByteArray(kMaxPeerInfoSize - 1)
            buffer.get(data)
            return PeerInfo(type, data)
        }
    }
}

private class PairingPacketHeader(
        val version: Byte,
        val type: Byte,
        val payload: Int) {

    enum class Type(val value: Byte) {
        SPAKE2_MSG(0.toByte()),
        PEER_INFO(1.toByte())
    }

    fun writeTo(buffer: ByteBuffer) {
        buffer.run {
            put(version)
            put(type)
            putInt(payload)
        }

        Log.d(TAG, "write PairingPacketHeader ${toStringShort()}")
    }

    override fun toString(): String {
        return "PairingPacketHeader(${toStringShort()})"
    }

    fun toStringShort(): String {
        return "version=${version.toInt()}, type=${type.toInt()}, payload=$payload"
    }

    companion object {

        fun readFrom(buffer: ByteBuffer): PairingPacketHeader? {
            val version = buffer.get()
            val type = buffer.get()
            val payload = buffer.int

            if (version < kMinSupportedKeyHeaderVersion || version > kMaxSupportedKeyHeaderVersion) {
                Log.e(TAG, "PairingPacketHeader version mismatch (us=$kCurrentKeyHeaderVersion them=${version})")
                return null
            }
            if (type != Type.SPAKE2_MSG.value && type != Type.PEER_INFO.value) {
                Log.e(TAG, "Unknown PairingPacket type=${type}")
                return null
            }
            if (payload <= 0 || payload > kMaxPayloadSize) {
                Log.e(TAG, "header payload not within a safe payload size (size=${payload})")
                return null
            }

            val header = PairingPacketHeader(version, type, payload)
            Log.d(TAG, "read PairingPacketHeader ${header.toStringShort()}")
            return header
        }
    }
}

private class PairingContext private constructor(private val nativePtr: Long) {

    val msg: ByteArray

    init {
        msg = nativeMsg(nativePtr)
    }

    internal fun initCipher(theirMsg: ByteArray) = nativeInitCipher(nativePtr, theirMsg)

    fun encrypt(`in`: ByteArray) = nativeEncrypt(nativePtr, `in`)

    fun decrypt(`in`: ByteArray) = nativeDecrypt(nativePtr, `in`)

    fun destroy() = nativeDestroy(nativePtr)

    private external fun nativeMsg(nativePtr: Long): ByteArray

    private external fun nativeInitCipher(nativePtr: Long, theirMsg: ByteArray): Boolean

    private external fun nativeEncrypt(nativePtr: Long, inbuf: ByteArray): ByteArray?

    private external fun nativeDecrypt(nativePtr: Long, inbuf: ByteArray): ByteArray?

    private external fun nativeDestroy(nativePtr: Long)

    companion object {

        fun create(password: ByteArray): PairingContext? {
            val nativePtr = nativeConstructor(true, password)
            return if (nativePtr != 0L) PairingContext(nativePtr) else null
        }

        @JvmStatic
        private external fun nativeConstructor(isClient: Boolean, password: ByteArray): Long
    }
}

@RequiresApi(Build.VERSION_CODES.R)
class AdbPairingClient(private val host: String, private val port: Int, private val pairCode: String, private val key: AdbKey) : Closeable {

    private enum class State {
        Ready,
        ExchangingMsgs,
        ExchangingPeerInfo,
        Stopped
    }

    private lateinit var socket: Socket
    private lateinit var inputStream: DataInputStream
    private lateinit var outputStream: DataOutputStream

    private val peerInfo: PeerInfo = PeerInfo(PeerInfo.Type.ADB_RSA_PUB_KEY.value, key.adbPublicKey)
    private lateinit var pairingContext: PairingContext
    private var state: State = State.Ready
    private var protoState: AdbPairingState = AdbPairingState.READY

    private fun syncProto(newState: State) {
        state = newState
        protoState = when (newState) {
            State.Ready -> AdbPairingState.READY
            State.ExchangingMsgs -> AdbPairingState.EXCHANGING_MSGS
            State.ExchangingPeerInfo -> AdbPairingState.EXCHANGING_PEER_INFO
            State.Stopped -> AdbPairingState.STOPPED
        }
    }

    fun start(): Boolean {
        if (!AdbPairingStateMachine.canStart(protoState) || state != State.Ready) {
            Log.w(TAG, "start() ignored in state=$state")
            return false
        }
        return try {
            setupTlsConnection()

            protoState = AdbPairingStateMachine.onStart(protoState)
            syncProto(State.ExchangingMsgs)
            if (!doExchangeMsgs()) {
                protoState = AdbPairingStateMachine.onMsgsExchanged(protoState, false)
                syncProto(State.Stopped)
                return false
            }
            protoState = AdbPairingStateMachine.onMsgsExchanged(protoState, true)

            syncProto(State.ExchangingPeerInfo)
            if (!doExchangePeerInfo()) {
                protoState = AdbPairingStateMachine.onPeerInfoExchanged(protoState, false)
                syncProto(State.Stopped)
                return false
            }

            protoState = AdbPairingStateMachine.onPeerInfoExchanged(protoState, true)
            syncProto(State.Stopped)
            true
        } catch (t: Throwable) {
            Log.e(TAG, "Pairing failed", t)
            protoState = AdbPairingStateMachine.onClose(protoState)
            syncProto(State.Stopped)
            false
        } finally {
            runCatching { close() }
        }
    }

    private fun setupTlsConnection() {
        if (state != State.Ready) throw IllegalStateException("Invalid state before TLS setup: $state")
        socket = Socket(host, port)
        socket.tcpNoDelay = true

        val sslContext = key.sslContext
        val sslSocket = sslContext.socketFactory.createSocket(socket, host, port, true) as SSLSocket
        sslSocket.startHandshake()
        Log.d(TAG, "Handshake succeeded.")

        inputStream = DataInputStream(sslSocket.inputStream)
        outputStream = DataOutputStream(sslSocket.outputStream)

        val pairCodeBytes = pairCode.toByteArray()
        val keyMaterial = Conscrypt.exportKeyingMaterial(sslSocket, kExportedKeyLabel, null, kExportedKeySize)
        val passwordBytes = ByteArray(pairCode.length + keyMaterial.size)
        pairCodeBytes.copyInto(passwordBytes)
        keyMaterial.copyInto(passwordBytes, pairCodeBytes.size)

        val ctx = PairingContext.create(passwordBytes)
            ?: throw IllegalStateException("Unable to create PairingContext.")
        pairingContext = ctx
    }

    private fun createHeader(type: PairingPacketHeader.Type, payload: Int): PairingPacketHeader {
        return PairingPacketHeader(kCurrentKeyHeaderVersion, type.value, payload)
    }

    private fun readHeader(): PairingPacketHeader? {
        val bytes = ByteArray(kPairingPacketHeaderSize)
        inputStream.readFully(bytes)
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
        return PairingPacketHeader.readFrom(buffer)
    }

    private fun writeHeader(header: PairingPacketHeader, payload: ByteArray) {
        val buffer = ByteBuffer.allocate(kPairingPacketHeaderSize).order(ByteOrder.BIG_ENDIAN)
        header.writeTo(buffer)
        outputStream.write(buffer.array())
        outputStream.write(payload)
        Log.d(TAG, "write payload, size=${payload.size}")
    }

    private fun doExchangeMsgs(): Boolean {
        if (state != State.ExchangingMsgs) return false
        val msg = pairingContext.msg
        val ourHeader = createHeader(PairingPacketHeader.Type.SPAKE2_MSG, msg.size)
        writeHeader(ourHeader, msg)

        val theirHeader = readHeader() ?: return false
        if (theirHeader.type != PairingPacketHeader.Type.SPAKE2_MSG.value) return false

        val theirMessage = ByteArray(theirHeader.payload)
        inputStream.readFully(theirMessage)

        if (!pairingContext.initCipher(theirMessage)) return false
        return true
    }

    private fun doExchangePeerInfo(): Boolean {
        if (state != State.ExchangingPeerInfo) return false
        val buf = ByteBuffer.allocate(kMaxPeerInfoSize).order(ByteOrder.BIG_ENDIAN)
        peerInfo.writeTo(buf)
        val outbuf = pairingContext.encrypt(buf.array()) ?: return false

        val ourHeader = createHeader(PairingPacketHeader.Type.PEER_INFO, outbuf.size)
        writeHeader(ourHeader, outbuf)

        val theirHeader = readHeader() ?: return false
        if (theirHeader.type != PairingPacketHeader.Type.PEER_INFO.value) return false

        val theirMsg = ByteArray(theirHeader.payload)
        inputStream.readFully(theirMsg)

        val decrypted = pairingContext.decrypt(theirMsg) ?: throw AdbInvalidPairingCodeException()
        if (decrypted.size != kMaxPeerInfoSize) {
            Log.e(TAG, "Got size=${decrypted.size} PeerInfo.size=$kMaxPeerInfoSize")
            return false
        }
        val theirBuf = ByteBuffer.wrap(decrypted).order(ByteOrder.BIG_ENDIAN)
        PeerInfo.readFrom(theirBuf)
        return true
    }

    override fun close() {
        state = State.Stopped
        runCatching { if (this::inputStream.isInitialized) inputStream.close() }
        runCatching { if (this::outputStream.isInitialized) outputStream.close() }
        runCatching { if (this::socket.isInitialized) socket.close() }
        runCatching { if (this::pairingContext.isInitialized) pairingContext.destroy() }
    }

    companion object {

        init {
            System.loadLibrary("adb")
        }

        @JvmStatic
        external fun available(): Boolean
    }
}
