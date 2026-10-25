package moe.shizuku.manager.freeze

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.shizuku.manager.utils.runShellCommand
import org.json.JSONArray
import org.json.JSONObject

data class FreezableApp(
    val packageName: String,
    val label: String,
    val isFrozen: Boolean,
    val isSystem: Boolean,
    val tags: Set<String>
)

object FreezerManager {

    private const val PREFS_NAME = "omnishizuku_freezer"
    private const val KEY_TAGS = "app_tags"
    private const val KEY_AUTO_FREEZE = "auto_freeze_on_screen_off"

    val DEFAULT_TAGS = listOf("Social", "Games", "Work", "Media", "Tools")

    fun isAutoFreezeEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AUTO_FREEZE, false)
    }

    fun setAutoFreezeEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AUTO_FREEZE, enabled).apply()
    }

    fun getAllCustomTags(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val custom = prefs.getStringSet("custom_tags_list", emptySet()) ?: emptySet()
        return (DEFAULT_TAGS + custom).distinct()
    }

    fun addCustomTag(context: Context, tag: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val custom = (prefs.getStringSet("custom_tags_list", emptySet()) ?: emptySet()).toMutableSet()
        custom.add(tag)
        prefs.edit().putStringSet("custom_tags_list", custom).apply()
    }

    fun getAppTags(context: Context, packageName: String): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_TAGS, "{}") ?: "{}"
        return runCatching {
            val json = JSONObject(jsonStr)
            val arr = json.optJSONArray(packageName) ?: return emptySet()
            val set = mutableSetOf<String>()
            for (i in 0 until arr.length()) {
                set.add(arr.getString(i))
            }
            set
        }.getOrDefault(emptySet())
    }

    fun setAppTags(context: Context, packageName: String, tags: Set<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_TAGS, "{}") ?: "{}"
        runCatching {
            val json = JSONObject(jsonStr)
            val arr = JSONArray()
            tags.forEach { arr.put(it) }
            json.put(packageName, arr)
            prefs.edit().putString(KEY_TAGS, json.toString()).apply()
        }
    }

    suspend fun loadApps(context: Context): List<FreezableApp> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val installed = pm.getInstalledApplications(PackageManager.MATCH_UNINSTALLED_PACKAGES)
        installed.mapNotNull { app ->
            if (app.packageName == context.packageName) return@mapNotNull null
            val label = app.loadLabel(pm).toString()
            val isFrozen = !app.enabled
            val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val tags = getAppTags(context, app.packageName)
            FreezableApp(
                packageName = app.packageName,
                label = label,
                isFrozen = isFrozen,
                isSystem = isSystem,
                tags = tags
            )
        }.sortedBy { it.label.lowercase() }
    }

    suspend fun freezeApp(packageName: String): Boolean = withContext(Dispatchers.IO) {
        runShellCommand("pm disable-user --user 0 $packageName") != null
    }

    suspend fun unfreezeApp(packageName: String): Boolean = withContext(Dispatchers.IO) {
        runShellCommand("pm enable --user 0 $packageName") != null
    }

    suspend fun freezeBatch(packageNames: List<String>): Int = withContext(Dispatchers.IO) {
        var count = 0
        for (pkg in packageNames) {
            if (freezeApp(pkg)) count++
        }
        count
    }

    suspend fun unfreezeBatch(packageNames: List<String>): Int = withContext(Dispatchers.IO) {
        var count = 0
        for (pkg in packageNames) {
            if (unfreezeApp(pkg)) count++
        }
        count
    }

    suspend fun freezeTag(context: Context, tag: String): Int {
        val apps = loadApps(context).filter { tag in it.tags && !it.isFrozen }
        return freezeBatch(apps.map { it.packageName })
    }

    suspend fun unfreezeTag(context: Context, tag: String): Int {
        val apps = loadApps(context).filter { tag in it.tags && it.isFrozen }
        return unfreezeBatch(apps.map { it.packageName })
    }
}
