package com.sameerasw.essentials.utils

import android.content.Context
import android.content.pm.PackageManager
import moe.shizuku.manager.files.PrivilegedFiles
import rikka.shizuku.Shizuku

/**
 * Thin adapter for the upstream Essentials battery and thermal utilities.
 * It preserves their API while routing reads through OmniShizuku's existing Shizuku file service.
 */
object ShellUtils {
    fun isAvailable(context: Context): Boolean = Shizuku.pingBinder()

    fun hasPermission(context: Context): Boolean =
        Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED

    @Suppress("UNUSED_PARAMETER")
    fun runCommand(
        context: Context,
        command: String,
        featureName: String? = null,
        notifyOnError: Boolean = true,
    ) {
        runCatching { PrivilegedFiles.exec(command) }
    }

    @Suppress("UNUSED_PARAMETER")
    fun runCommandWithOutput(
        context: Context,
        command: String,
        featureName: String? = null,
        notifyOnError: Boolean = true,
    ): String? =
        runCatching { PrivilegedFiles.exec(command) }.getOrNull()
}
