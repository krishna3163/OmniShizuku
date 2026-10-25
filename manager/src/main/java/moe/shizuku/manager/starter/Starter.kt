package moe.shizuku.manager.starter

import java.io.File
import moe.shizuku.manager.ShizukuApplication
import moe.shizuku.manager.application
import moe.shizuku.manager.utils.ShizukuStateMachine

private val app: ShizukuApplication get() = application

object Starter {

    private val starterFile: File get() = File(app.applicationInfo.nativeLibraryDir, "libshizuku.so")

    val userCommand: String get() = "${starterFile.absolutePath} --apk=${app.applicationInfo.sourceDir}"

    val adbCommand: String get() = "adb shell $userCommand"

    val internalCommand: String get() = userCommand

    suspend fun waitForBinder(timeoutMs: Long = 30_000L): Boolean {
        return ShizukuStateMachine.awaitRunning(timeoutMs)
    }
}
