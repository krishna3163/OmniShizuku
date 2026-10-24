package eu.darken.porter.porsh

import android.annotation.SuppressLint
import android.os.IBinder
import android.util.Log

object PorshConfig {

    private const val TAG = "PORSHConfig"

    internal const val TRANSACTION_createHost = 0
    internal const val TRANSACTION_setWindowSize = 1
    internal const val TRANSACTION_getExitCode = 2

    private var binder: IBinder? = null
    private var interfaceToken: String? = null
    private var transactionCodeStart = 0
    private var libraryPath: String? = null

    /** Set by [init]; a client that transacts before that has no wire to speak on. */
    internal fun getBinder(): IBinder = checkNotNull(binder) { "PorshConfig.init was not called" }

    internal fun getInterfaceToken(): String = checkNotNull(interfaceToken) { "PorshConfig.init was not called" }

    internal fun getTransactionCode(code: Int): Int = transactionCodeStart + code

    fun setLibraryPath(path: String?) {
        libraryPath = path
    }

    @SuppressLint("UnsafeDynamicallyLoadedCode")
    private fun loadLibrary() {
        val path = libraryPath
        if (path == null) {
            System.loadLibrary("porsh")
        } else {
            System.load("$path/libporsh.so")
        }
    }

    fun init(interfaceToken: String, transactionCodeStart: Int) {
        Log.d(TAG, "init (server) $interfaceToken $transactionCodeStart")
        this.interfaceToken = interfaceToken
        this.transactionCodeStart = transactionCodeStart
        loadLibrary()
    }

    fun init(binder: IBinder, interfaceToken: String, transactionCodeStart: Int) {
        Log.d(TAG, "init (client) $binder $interfaceToken $transactionCodeStart")
        this.binder = binder
        this.interfaceToken = interfaceToken
        this.transactionCodeStart = transactionCodeStart
        loadLibrary()
    }
}
