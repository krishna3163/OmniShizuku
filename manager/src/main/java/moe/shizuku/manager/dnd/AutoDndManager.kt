package moe.shizuku.manager.dnd

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.shizuku.manager.utils.runShellCommand
import org.json.JSONArray

object AutoDndManager {

    private const val PREFS_NAME = "omnishizuku_autodnd"
    private const val KEY_ENABLED = "autodnd_enabled"
    private const val KEY_APPS = "autodnd_target_apps"
    private const val KEY_ZEN_MODE = "autodnd_target_zen_mode" // 1: Priority, 2: Total Silence, 3: Alarms only

    fun isEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ENABLED, false)
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun getTargetZenMode(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_ZEN_MODE, 1)
    }

    fun setTargetZenMode(context: Context, mode: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_ZEN_MODE, mode).apply()
    }

    fun getTargetApps(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_APPS, "[]") ?: "[]"
        return runCatching {
            val arr = JSONArray(jsonStr)
            val set = mutableSetOf<String>()
            for (i in 0 until arr.length()) {
                set.add(arr.getString(i))
            }
            set
        }.getOrDefault(emptySet())
    }

    fun setTargetApps(context: Context, apps: Set<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val arr = JSONArray()
        apps.forEach { arr.put(it) }
        prefs.edit().putString(KEY_APPS, arr.toString()).apply()
    }

    suspend fun getCurrentZenMode(): Int = withContext(Dispatchers.IO) {
        val out = runShellCommand("settings get global zen_mode")?.trim()
        out?.toIntOrNull() ?: 0
    }

    suspend fun setZenMode(mode: Int): Boolean = withContext(Dispatchers.IO) {
        val c1 = runShellCommand("cmd notification set_zen_mode $mode") != null
        val c2 = runShellCommand("settings put global zen_mode $mode") != null
        c1 || c2
    }
}
