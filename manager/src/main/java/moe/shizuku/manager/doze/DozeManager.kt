package moe.shizuku.manager.doze

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.shizuku.manager.utils.runShellCommand

object DozeManager {
    private const val PREFS_NAME = "enforce_doze_prefs"
    private const val KEY_AGGRESSIVE_DOZE = "aggressive_doze_enabled"
    private const val KEY_MOTION_KILL = "motion_kill_enabled"

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isAggressiveDozeEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_AGGRESSIVE_DOZE, false)

    fun setAggressiveDozeEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_AGGRESSIVE_DOZE, enabled).apply()
    }

    fun isMotionKillEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_MOTION_KILL, false)

    fun setMotionKillEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_MOTION_KILL, enabled).apply()
    }

    suspend fun getDeepDozeState(): String = withContext(Dispatchers.IO) {
        runShellCommand("dumpsys deviceidle get deep")?.trim() ?: "UNKNOWN"
    }

    suspend fun getLightDozeState(): String = withContext(Dispatchers.IO) {
        runShellCommand("dumpsys deviceidle get light")?.trim() ?: "UNKNOWN"
    }

    suspend fun forceDeepIdle(): String = withContext(Dispatchers.IO) {
        runShellCommand("dumpsys deviceidle force-idle deep") ?: "Executed"
    }

    suspend fun stepIdle(): String = withContext(Dispatchers.IO) {
        runShellCommand("dumpsys deviceidle step") ?: "Stepped"
    }

    suspend fun unforceIdle(): String = withContext(Dispatchers.IO) {
        runShellCommand("dumpsys deviceidle unforce") ?: "Reset"
    }

    suspend fun disableMotion(): String = withContext(Dispatchers.IO) {
        runShellCommand("dumpsys deviceidle disable motion") ?: "Motion disabled"
    }

    suspend fun enableMotion(): String = withContext(Dispatchers.IO) {
        runShellCommand("dumpsys deviceidle enable motion") ?: "Motion enabled"
    }

    suspend fun getDozeSettingsRaw(): String = withContext(Dispatchers.IO) {
        val out = runShellCommand("dumpsys deviceidle") ?: return@withContext "Unavailable"
        val lines = out.lines()
        val startIndex = lines.indexOfFirst { it.trim().startsWith("Settings:") }
        if (startIndex != -1) {
            lines.subList(startIndex, minOf(lines.size, startIndex + 30)).joinToString("\n")
        } else {
            out.take(500)
        }
    }
}
