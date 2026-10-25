package moe.shizuku.manager.audio.shutup

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.shizuku.manager.utils.runShellCommand

object ShutUpManager {

    private const val PREFS_NAME = "omnishizuku_shutup"
    private const val KEY_ENABLED = "shutup_enabled"
    private const val KEY_MUTED_PKGS = "shutup_muted_pkgs"

    private val DEFAULT_PKGS = setOf(
        "com.zhiliaoapp.musically", // TikTok
        "com.instagram.android",    // Instagram
        "com.google.android.youtube", // YouTube
        "com.facebook.katana"        // Facebook
    )

    fun isEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ENABLED, false)
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ENABLED, enabled)
            .apply()
    }

    fun getMutedPackages(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_MUTED_PKGS, DEFAULT_PKGS)?.toSet() ?: DEFAULT_PKGS
    }

    fun addMutedPackage(context: Context, pkg: String) {
        val current = getMutedPackages(context).toMutableSet()
        current.add(pkg)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_MUTED_PKGS, current)
            .apply()
    }

    fun removeMutedPackage(context: Context, pkg: String) {
        val current = getMutedPackages(context).toMutableSet()
        current.remove(pkg)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_MUTED_PKGS, current)
            .apply()
    }

    suspend fun muteMediaVolume(): Boolean = withContext(Dispatchers.IO) {
        // STREAM_MUSIC is stream 3
        val out1 = runShellCommand("cmd media_session volume --set 0")
        val out2 = runShellCommand("media volume --stream 3 --set 0")
        out1 != null || out2 != null
    }

    suspend fun restoreMediaVolume(level: Int = 10): Boolean = withContext(Dispatchers.IO) {
        val out1 = runShellCommand("cmd media_session volume --set $level")
        val out2 = runShellCommand("media volume --stream 3 --set $level")
        out1 != null || out2 != null
    }
}
