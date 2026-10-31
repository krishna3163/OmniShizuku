package moe.shizuku.manager.security

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.shizuku.manager.utils.runShellCommand

object ScreenSecurityManager {

    suspend fun isSecureWindowsDisabled(): Boolean = withContext(Dispatchers.IO) {
        val out1 = runShellCommand("settings get global disable_secure_windows")?.trim()
        val out2 = runShellCommand("settings get secure disable_secure_windows")?.trim()
        out1 == "1" || out2 == "1"
    }

    suspend fun setDisableSecureWindows(disable: Boolean): Boolean = withContext(Dispatchers.IO) {
        val v = if (disable) "1" else "0"
        val c1 = runShellCommand("settings put global disable_secure_windows $v") != null
        val c2 = runShellCommand("settings put secure disable_secure_windows $v") != null
        c1 || c2
    }

    suspend fun isUsbDebuggingEnabled(): Boolean = withContext(Dispatchers.IO) {
        val out = runShellCommand("settings get global adb_enabled")?.trim()
        out == "1"
    }

    suspend fun setUsbDebugging(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        val v = if (enabled) "1" else "0"
        runShellCommand("settings put global adb_enabled $v") != null
    }
}
