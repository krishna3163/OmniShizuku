package eu.darken.porter.porsh

import android.os.Parcel
import android.os.RemoteException
import android.system.ErrnoException
import android.system.Os
import android.util.Log
import java.io.File
import java.io.FileDescriptor

class PorshTerminal @Throws(ErrnoException::class, RemoteException::class) constructor(
    private val argv: Array<String>,
) {

    private val tty: Byte = routable(prepare())
    private var stdin: Array<FileDescriptor>? = null
    private var stdout: Array<FileDescriptor>? = null
    private var stderr: Array<FileDescriptor>? = null
    private var ttyFd = -1
    var exitCode: Int = 0
        private set

    init {
        createHost()
    }

    @Throws(ErrnoException::class, RemoteException::class)
    private fun createHost() {
        Log.d(TAG, "createHost")
        val data = Parcel.obtain()
        val reply = Parcel.obtain()

        val env = System.getenv().map { (key, value) -> "$key=$value" }.toTypedArray()

        val dir = File("").absolutePath

        try {
            data.writeInterfaceToken(PorshConfig.getInterfaceToken())
            data.writeByte(tty)
            stdin = Os.pipe().also { data.writeFileDescriptor(it[0]) }
            stdout = Os.pipe().also { data.writeFileDescriptor(it[1]) }
            if ((tty.toInt() and PorshConstants.ATTY_ERR) == 0) {
                stderr = Os.pipe().also { data.writeFileDescriptor(it[1]) }
            }
            data.writeStringArray(argv)
            data.writeStringArray(env)
            data.writeString(dir)
            PorshConfig.getBinder().transact(PorshConfig.getTransactionCode(PorshConfig.TRANSACTION_createHost), data, reply, 0)
            reply.readException()
        } finally {
            data.recycle()
            reply.recycle()

            closeFd(stdin, 0)
            closeFd(stdout, 1)
            closeFd(stderr, 1)
        }
    }

    fun start() {
        Log.d(TAG, "start")

        ttyFd = start(tty, getFd(stdin, 1), getFd(stdout, 0), getFd(stderr, 0))

        if (ttyFd != -1) {
            Thread {
                while (true) {
                    Log.d(TAG, "waitForWindowSizeChange")

                    try {
                        val size = waitForWindowSizeChange(ttyFd)
                        setWindowSize(size)
                    } catch (e: Throwable) {
                        Log.w(TAG, Log.getStackTraceString(e))
                    }
                }
            }.start()
        }
    }

    @Throws(RemoteException::class)
    private fun setWindowSize(size: Long) {
        Log.d(TAG, "setWindowSize")

        val data = Parcel.obtain()
        val reply = Parcel.obtain()

        try {
            data.writeInterfaceToken(PorshConfig.getInterfaceToken())
            data.writeLong(size)
            PorshConfig.getBinder().transact(PorshConfig.getTransactionCode(PorshConfig.TRANSACTION_setWindowSize), data, reply, 0)
            reply.readException()
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    @Throws(RemoteException::class)
    private fun requestExitCode(): Int {
        Log.d(TAG, "requestExitCode")

        val data = Parcel.obtain()
        val reply = Parcel.obtain()

        try {
            data.writeInterfaceToken(PorshConfig.getInterfaceToken())
            PorshConfig.getBinder().transact(PorshConfig.getTransactionCode(PorshConfig.TRANSACTION_getExitCode), data, reply, 0)
            reply.readException()
            return reply.readInt()
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    fun waitFor(): Int {
        Log.d(TAG, "waitFor")

        waitForProcessExit()
        exitCode = try {
            requestExitCode()
        } catch (e: Throwable) {
            Log.w(TAG, Log.getStackTraceString(e))
            -1
        }
        return exitCode
    }

    companion object {

        private const val TAG = "PorshTerminal"

        fun getFd(fileDescriptor: Array<FileDescriptor>?, i: Int): Int {
            if (fileDescriptor == null) return -1
            return FileDescriptors.getFd(fileDescriptor[i])
        }

        fun closeFd(fileDescriptor: Array<FileDescriptor>?, i: Int) {
            if (fileDescriptor == null) return
            FileDescriptors.closeSilently(fileDescriptor[i])
        }

        // `porsh -c cmd | grep x` from a terminal: stderr is a tty but stdout a pipe. Stderr then
        // goes as a pipe too.
        internal fun routable(tty: Byte): Byte {
            if ((tty.toInt() and PorshConstants.ATTY_OUT) != 0) return tty
            return (tty.toInt() and PorshConstants.ATTY_ERR.inv()).toByte()
        }

        // Registered by name and signature from porsh_terminal.cpp, on this class. Public rather
        // than internal: an internal function's JVM name carries a module suffix the table
        // does not know.
        @JvmStatic
        external fun prepare(): Byte

        @JvmStatic
        private external fun start(tty: Byte, stdin: Int, stdout: Int, stderr: Int): Int

        @JvmStatic
        private external fun waitForWindowSizeChange(fd: Int): Long

        @JvmStatic
        private external fun waitForProcessExit()
    }
}
