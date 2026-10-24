package eu.darken.porter.porsh

import android.util.Log
import kotlin.system.exitProcess

open class Porsh {

    open fun requestPermission(onGrantedRunnable: Runnable) {
    }

    private fun startShell(args: Array<String>, permissionGranted: Boolean) {
        if (!permissionGranted) {
            requestPermission { startShell(args, true) }
            return
        }

        startShell(args)
    }

    private fun startShell(args: Array<String>) {
        try {
            val terminal = PorshTerminal(args)
            terminal.start()
            val exitCode = terminal.waitFor()
            exitProcess(exitCode)
        } catch (e: Throwable) {
            System.err.println(e.message)
            System.err.flush()
            exitProcess(1)
        }
    }

    fun start(args: Array<String>) {
        Log.d(TAG, "args: " + args.contentToString())
        startShell(args, false)
    }

    private companion object {
        const val TAG = "PORSH"
    }
}
