package eu.darken.porter.porsh

import android.os.Binder
import android.os.IBinder
import android.os.Parcel
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.system.Os
import android.util.Log

abstract class PorshService internal constructor(
    private val interfaceToken: String,
    private val transactionCodeStart: Int,
    private val registry: PorshHostRegistry,
    private val envPolicy: EnvPolicy,
) {

    constructor(interfaceToken: String, transactionCodeStart: Int) : this(
        interfaceToken,
        transactionCodeStart,
        PorshHostRegistry(::PorshHost) { SystemClock.elapsedRealtime() },
        EnvPolicy(Os.getuid() == 0),
    )

    private fun createHost(
        args: Array<String>,
        env: Array<String>,
        dir: String?,
        tty: Byte,
        stdin: ParcelFileDescriptor?,
        stdout: ParcelFileDescriptor?,
        stderr: ParcelFileDescriptor?,
    ) {
        val callingPid = Binder.getCallingPid()

        registry.createHost(callingPid, args, envPolicy.resolve(env), dir, tty, stdin, stdout, stderr)
    }

    private fun setWindowSize(size: Long) {
        registry.setWindowSize(Binder.getCallingPid(), size)
    }

    private fun getExitCode(): Int = registry.getExitCode(Binder.getCallingPid())

    abstract fun enforceCallingPermission(func: String)

    open fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
        when (code) {
            transactionCodeStart + PorshConfig.TRANSACTION_createHost -> {
                Log.d(TAG, "TRANSACTION_createHost")

                enforceCallingPermission("createHost")

                if (reply == null || (flags and IBinder.FLAG_ONEWAY) != 0) {
                    return true
                }

                data.enforceInterface(interfaceToken)
                val tty = data.readByte()
                val stdin = data.readFileDescriptor()
                val stdout = data.readFileDescriptor()
                val stderr = if ((tty.toInt() and PorshConstants.ATTY_ERR) == 0) data.readFileDescriptor() else null
                val args = data.createStringArray()!!
                val env = data.createStringArray()!!
                val dir = data.readString()
                createHost(args, env, dir, tty, stdin, stdout, stderr)
                reply.writeNoException()
                return true
            }
            transactionCodeStart + PorshConfig.TRANSACTION_setWindowSize -> {
                Log.d(TAG, "TRANSACTION_setWindowSize")

                enforceCallingPermission("setWindowSize")

                data.enforceInterface(interfaceToken)
                val size = data.readLong()
                setWindowSize(size)
                reply?.writeNoException()
                return true
            }
            transactionCodeStart + PorshConfig.TRANSACTION_getExitCode -> {
                Log.d(TAG, "TRANSACTION_getExitCode")

                enforceCallingPermission("getExitCode")

                data.enforceInterface(interfaceToken)
                val exitCode = getExitCode()
                if (reply != null) {
                    reply.writeNoException()
                    reply.writeInt(exitCode)
                }
                return true
            }
        }
        return false
    }

    private companion object {
        const val TAG = "PorshService"
    }
}
