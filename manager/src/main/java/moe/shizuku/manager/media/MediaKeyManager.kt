package moe.shizuku.manager.media

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.shizuku.manager.utils.runShellCommand

object MediaKeyManager {

    private const val PREFS_NAME = "omnishizuku_media_keys"
    private const val KEY_SKIPPER_ENABLED = "vol_skipper_enabled"

    fun isSkipperEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_SKIPPER_ENABLED, false)
    }

    fun setSkipperEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SKIPPER_ENABLED, enabled).apply()
    }

    suspend fun skipNext(): Boolean = withContext(Dispatchers.IO) {
        val res = runShellCommand("cmd media_session dispatch next")
        res != null
    }

    suspend fun skipPrevious(): Boolean = withContext(Dispatchers.IO) {
        val res = runShellCommand("cmd media_session dispatch previous")
        res != null
    }

    suspend fun togglePlayPause(): Boolean = withContext(Dispatchers.IO) {
        val res = runShellCommand("cmd media_session dispatch play-pause")
        res != null
    }
}
