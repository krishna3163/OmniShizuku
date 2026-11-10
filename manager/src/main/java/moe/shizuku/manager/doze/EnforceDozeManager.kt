package moe.shizuku.manager.doze

import android.app.ActivityManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.utils.runShellCommand
import java.util.*

object EnforceDozeManager {

    private const val PREFS_KEY_PREFIX = "enforcedoze_"

    private fun prefs(context: Context): SharedPreferences =
        ShizukuSettings.getPreferences()

    fun isServiceEnabled(context: Context): Boolean =
        prefs(context).getBoolean("${PREFS_KEY_PREFIX}serviceEnabled", false)

    fun setServiceEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean("${PREFS_KEY_PREFIX}serviceEnabled", enabled).apply()
    }

    fun isDisableWhenCharging(context: Context): Boolean =
        prefs(context).getBoolean("${PREFS_KEY_PREFIX}disableWhenCharging", true)

    fun isDisableMotionSensors(context: Context): Boolean =
        prefs(context).getBoolean("${PREFS_KEY_PREFIX}disableMotionSensors", true)

    fun isTurnOffAllSensorsInDoze(context: Context): Boolean =
        prefs(context).getBoolean("${PREFS_KEY_PREFIX}turnOffAllSensorsInDoze", false)

    fun isTurnOffBiometricsInDoze(context: Context): Boolean =
        prefs(context).getBoolean("${PREFS_KEY_PREFIX}turnOffBiometricsInDoze", false)

    fun isTurnOnBatterySaverInDoze(context: Context): Boolean =
        prefs(context).getBoolean("${PREFS_KEY_PREFIX}turnOnBatterySaverInDoze", false)

    fun isTurnOnAirplaneInDoze(context: Context): Boolean =
        prefs(context).getBoolean("${PREFS_KEY_PREFIX}turnOnAirplaneInDoze", false)

    fun isTurnOffBluetoothInDoze(context: Context): Boolean =
        prefs(context).getBoolean("${PREFS_KEY_PREFIX}turnOffBluetoothInDoze", false)

    fun isTurnOffGPSInDoze(context: Context): Boolean =
        prefs(context).getBoolean("${PREFS_KEY_PREFIX}turnOffGPSInDoze", false)

    fun isTurnOffWiFiInDoze(context: Context): Boolean =
        prefs(context).getBoolean("${PREFS_KEY_PREFIX}turnOffWiFiInDoze", false)

    fun isIgnoreIfHotspot(context: Context): Boolean =
        prefs(context).getBoolean("${PREFS_KEY_PREFIX}ignoreIfHotspot", true)

    fun isTurnOffDataInDoze(context: Context): Boolean =
        prefs(context).getBoolean("${PREFS_KEY_PREFIX}turnOffDataInDoze", false)

    fun isWhitelistMusicAppNetwork(context: Context): Boolean =
        prefs(context).getBoolean("${PREFS_KEY_PREFIX}whitelistMusicAppNetwork", false)

    fun isWhitelistCurrentApp(context: Context): Boolean =
        prefs(context).getBoolean("${PREFS_KEY_PREFIX}whitelistCurrentApp", false)

    fun isIgnoreLockscreenTimeout(context: Context): Boolean =
        prefs(context).getBoolean("${PREFS_KEY_PREFIX}ignoreLockscreenTimeout", true)

    fun isWaitForUnlock(context: Context): Boolean =
        prefs(context).getBoolean("${PREFS_KEY_PREFIX}waitForUnlock", false)

    fun isUseAutoRotateAndBrightnessFix(context: Context): Boolean =
        prefs(context).getBoolean("${PREFS_KEY_PREFIX}autoRotateAndBrightnessFix", false)

    fun isShowPersistentNotif(context: Context): Boolean =
        prefs(context).getBoolean("${PREFS_KEY_PREFIX}showPersistentNotif", false)

    fun isDisableStats(context: Context): Boolean =
        prefs(context).getBoolean("${PREFS_KEY_PREFIX}disableStats", false)

    fun isDisableLogcat(context: Context): Boolean =
        prefs(context).getBoolean("${PREFS_KEY_PREFIX}disableLogcat", false)

    fun getDozeEnterDelay(context: Context): Int =
        prefs(context).getInt("${PREFS_KEY_PREFIX}dozeEnterDelay", 0)

    fun getSensorWhitelistPackage(context: Context): String =
        prefs(context).getString("${PREFS_KEY_PREFIX}sensorWhitelistPackage", "") ?: ""

    fun getExecutionMode(context: Context): String =
        prefs(context).getString("${PREFS_KEY_PREFIX}executionMode", "shizuku") ?: "shizuku"

    fun setDisableWhenCharging(context: Context, v: Boolean) = setBool(context, "disableWhenCharging", v)
    fun setDisableMotionSensors(context: Context, v: Boolean) = setBool(context, "disableMotionSensors", v)
    fun setTurnOffAllSensorsInDoze(context: Context, v: Boolean) = setBool(context, "turnOffAllSensorsInDoze", v)
    fun setTurnOffBiometricsInDoze(context: Context, v: Boolean) = setBool(context, "turnOffBiometricsInDoze", v)
    fun setTurnOnBatterySaverInDoze(context: Context, v: Boolean) = setBool(context, "turnOnBatterySaverInDoze", v)
    fun setTurnOnAirplaneInDoze(context: Context, v: Boolean) = setBool(context, "turnOnAirplaneInDoze", v)
    fun setTurnOffBluetoothInDoze(context: Context, v: Boolean) = setBool(context, "turnOffBluetoothInDoze", v)
    fun setTurnOffGPSInDoze(context: Context, v: Boolean) = setBool(context, "turnOffGPSInDoze", v)
    fun setTurnOffWiFiInDoze(context: Context, v: Boolean) = setBool(context, "turnOffWiFiInDoze", v)
    fun setIgnoreIfHotspot(context: Context, v: Boolean) = setBool(context, "ignoreIfHotspot", v)
    fun setTurnOffDataInDoze(context: Context, v: Boolean) = setBool(context, "turnOffDataInDoze", v)
    fun setWhitelistMusicAppNetwork(context: Context, v: Boolean) = setBool(context, "whitelistMusicAppNetwork", v)
    fun setWhitelistCurrentApp(context: Context, v: Boolean) = setBool(context, "whitelistCurrentApp", v)
    fun setIgnoreLockscreenTimeout(context: Context, v: Boolean) = setBool(context, "ignoreLockscreenTimeout", v)
    fun setWaitForUnlock(context: Context, v: Boolean) = setBool(context, "waitForUnlock", v)
    fun setUseAutoRotateAndBrightnessFix(context: Context, v: Boolean) = setBool(context, "autoRotateAndBrightnessFix", v)
    fun setShowPersistentNotif(context: Context, v: Boolean) = setBool(context, "showPersistentNotif", v)
    fun setDisableStats(context: Context, v: Boolean) = setBool(context, "disableStats", v)
    fun setDisableLogcat(context: Context, v: Boolean) = setBool(context, "disableLogcat", v)
    fun setDozeEnterDelay(context: Context, v: Int) = setInt(context, "dozeEnterDelay", v)
    fun setSensorWhitelistPackage(context: Context, v: String) = setString(context, "sensorWhitelistPackage", v)
    fun setExecutionMode(context: Context, v: String) = setString(context, "executionMode", v)

    private fun setBool(context: Context, key: String, v: Boolean) =
        prefs(context).edit().putBoolean("${PREFS_KEY_PREFIX}$key", v).apply()
    private fun setInt(context: Context, key: String, v: Int) =
        prefs(context).edit().putInt("${PREFS_KEY_PREFIX}$key", v).apply()
    private fun setString(context: Context, key: String, v: String) =
        prefs(context).edit().putString("${PREFS_KEY_PREFIX}$key", v).apply()

    fun getDozeAppBlocklist(context: Context): Set<String> =
        prefs(context).getStringSet("${PREFS_KEY_PREFIX}dozeAppBlockList", emptySet())?.toMutableSet() ?: mutableSetOf()

    fun setDozeAppBlocklist(context: Context, packages: Set<String>) =
        prefs(context).edit().putStringSet("${PREFS_KEY_PREFIX}dozeAppBlockList", packages).apply()

    fun getNotificationBlocklist(context: Context): Set<String> =
        prefs(context).getStringSet("${PREFS_KEY_PREFIX}notificationBlockList", emptySet())?.toMutableSet() ?: mutableSetOf()

    fun setNotificationBlocklist(context: Context, packages: Set<String>) =
        prefs(context).edit().putStringSet("${PREFS_KEY_PREFIX}notificationBlockList", packages).apply()

    fun getCustomDozePeriods(context: Context): Set<String> =
        prefs(context).getStringSet("${PREFS_KEY_PREFIX}customDozePeriods", emptySet())?.toMutableSet() ?: mutableSetOf()

    fun setCustomDozePeriods(context: Context, periods: Set<String>) =
        prefs(context).edit().putStringSet("${PREFS_KEY_PREFIX}customDozePeriods", periods).apply()

    fun getDozeUsageData(context: Context): Set<String> =
        prefs(context).getStringSet("${PREFS_KEY_PREFIX}dozeUsageDataAdvanced", emptySet())?.toMutableSet() ?: mutableSetOf()

    fun setDozeUsageData(context: Context, data: Set<String>) =
        prefs(context).edit().putStringSet("${PREFS_KEY_PREFIX}dozeUsageDataAdvanced", data).apply()

    suspend fun getDeepDozeState(): String = withContext(Dispatchers.IO) {
        runShellCommand("dumpsys deviceidle get deep")?.trim() ?: "UNKNOWN"
    }

    suspend fun getLightDozeState(): String = withContext(Dispatchers.IO) {
        runShellCommand("dumpsys deviceidle get light")?.trim() ?: "UNKNOWN"
    }

    suspend fun forceDeepIdle(): String = withContext(Dispatchers.IO) {
        runShellCommand("dumpsys deviceidle force-idle deep") ?: "Executed"
    }

    suspend fun unforceIdle(): String = withContext(Dispatchers.IO) {
        runShellCommand("dumpsys deviceidle unforce") ?: "Reset"
    }

    suspend fun stepIdle(): String = withContext(Dispatchers.IO) {
        runShellCommand("dumpsys deviceidle step") ?: "Stepped"
    }

    suspend fun disableMotionSensorsCmd(): String = withContext(Dispatchers.IO) {
        runShellCommand("dumpsys sensorservice restrict") ?: "Motion disabled"
    }

    suspend fun enableMotionSensorsCmd(): String = withContext(Dispatchers.IO) {
        runShellCommand("dumpsys sensorservice enable") ?: "Motion enabled"
    }

    suspend fun restrictMotionSensorsForPackage(pkg: String): String = withContext(Dispatchers.IO) {
        runShellCommand("dumpsys sensorservice restrict $pkg") ?: "Motion restricted for $pkg"
    }

    suspend fun getDozeSettingsRaw(): String = withContext(Dispatchers.IO) {
        val out = runShellCommand("dumpsys deviceidle") ?: return@withContext "Unavailable"
        val lines = out.lines()
        val startIndex = lines.indexOfFirst { it.trim().startsWith("Settings:") }
        if (startIndex != -1) {
            lines.subList(startIndex, lines.size.coerceAtMost(startIndex + 30)).joinToString("\n")
        } else {
            out.take(500)
        }
    }

    suspend fun addSelfToDozeWhitelist(pkgName: String): String? = withContext(Dispatchers.IO) {
        runShellCommand("dumpsys deviceidle whitelist +$pkgName")
    }

    suspend fun suspendPackage(pkg: String): String? = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            runShellCommand("pm unsuspend $pkg")
            runShellCommand("pm suspend $pkg")
        } else {
            runShellCommand("pm disable $pkg")
        }
    }

    suspend fun unsuspendPackage(pkg: String): String? = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            runShellCommand("pm unsuspend $pkg")
        } else {
            runShellCommand("pm enable $pkg")
        }
    }

    suspend fun disableNotificationsForPackage(pkg: String, uid: Int, enabled: Boolean): String? = withContext(Dispatchers.IO) {
        val cmd = "service call notification 9 s16 $pkg i32 $uid i32 ${if (enabled) 1 else 0}"
        runShellCommand(cmd)
    }

    suspend fun disableWiFiCmd(): String? = withContext(Dispatchers.IO) {
        runShellCommand("svc wifi disable")
    }

    suspend fun enableWiFiCmd(): String? = withContext(Dispatchers.IO) {
        runShellCommand("svc wifi enable")
    }

    suspend fun disableMobileDataCmd(): String? = withContext(Dispatchers.IO) {
        runShellCommand("svc data disable")
    }

    suspend fun enableMobileDataCmd(): String? = withContext(Dispatchers.IO) {
        runShellCommand("svc data enable")
    }

    suspend fun disableBluetoothCmd(): String? = withContext(Dispatchers.IO) {
        runShellCommand("svc bluetooth disable")
    }

    suspend fun enableBluetoothCmd(): String? = withContext(Dispatchers.IO) {
        runShellCommand("svc bluetooth enable")
    }

    suspend fun setGpsLocationMode(enabled: Boolean): String? = withContext(Dispatchers.IO) {
        val mode = if (enabled) Settings.Secure.LOCATION_MODE_HIGH_ACCURACY else Settings.Secure.LOCATION_MODE_OFF
        runShellCommand("settings put secure location_mode $mode")
    }

    suspend fun setBiometricsEnabled(enabled: Boolean): String? = withContext(Dispatchers.IO) {
        runShellCommand("settings put secure biometric_keyguard_enabled ${if (enabled) 1 else 0}")
    }

    suspend fun setBatterySaverEnabled(enabled: Boolean): String? = withContext(Dispatchers.IO) {
        runShellCommand("settings put global low_power ${if (enabled) 1 else 0}")
    }

    suspend fun setAirplaneModeEnabled(enabled: Boolean): String? = withContext(Dispatchers.IO) {
        runShellCommand("settings put global airplane_mode_on ${if (enabled) 1 else 0}")
        runShellCommand("am broadcast -a android.intent.action.AIRPLANE_MODE --ez state ${if (enabled) "true" else "false"}")
    }

    suspend fun setAllSensorsState(enabled: Boolean): String? = withContext(Dispatchers.IO) {
        val tx = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> 9
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> 8
            else -> 4
        }
        runShellCommand("service call sensor_privacy $tx i32 ${if (enabled) 0 else 1}")
    }

    data class DozeTunable(
        val key: String,
        var value: String,
        val default: String
    )

    private val TUNABLE_DEFAULTS = mapOf(
        "light_after_inactive_to" to "300000",
        "light_pre_idle_to" to "600000",
        "light_idle_to" to "300000",
        "light_idle_factor" to "2",
        "light_max_idle_to" to "900000",
        "light_idle_maintenance_min_budget" to "60000",
        "light_idle_maintenance_max_budget" to "300000",
        "min_light_maintenance_time" to "5000",
        "min_deep_maintenance_time" to "30000",
        "inactive_to" to "1800000",
        "sensing_to" to "240000",
        "locating_to" to "30000",
        "location_accuracy" to "20",
        "motion_inactive_to" to "600000",
        "idle_after_inactive_to" to "1800000",
        "idle_pending_to" to "30000",
        "max_idle_pending_to" to "600000",
        "idle_pending_factor" to "2",
        "idle_to" to "3600000",
        "max_idle_to" to "21600000",
        "idle_factor" to "2",
        "min_time_to_alarm" to "3600000",
        "max_temp_app_whitelist_duration" to "300000",
        "mms_temp_app_whitelist_duration" to "60000",
        "sms_temp_app_whitelist_duration" to "20000",
        "notification_whitelist_duration" to "30000"
    )

    fun getDozeTunables(context: Context): List<DozeTunable> =
        TUNABLE_DEFAULTS.entries.map { (key, default) ->
            DozeTunable(
                key = key,
                value = prefs(context).getString("${PREFS_KEY_PREFIX}tunable_$key", default) ?: default,
                default = default
            )
        }

    fun setDozeTunable(context: Context, key: String, value: String) {
        prefs(context).edit().putString("${PREFS_KEY_PREFIX}tunable_$key", value).apply()
    }

    fun getTunableCommandsList(context: Context): List<String> {
        val tunables = getDozeTunables(context)
        return tunables.map { "device_config put ${it.key} ${it.value}" }
    }

    fun getTunableString(context: Context): String {
        val tunables = getDozeTunables(context)
        return tunables.joinToString(",") { "${it.key}=${it.value}" }
    }

    fun applyTunablesViaDeviceConfig(context: Context) {
        val commands = getTunableCommandsList(context)
        commands.forEach { cmd ->
            runShellCommand(cmd)
        }
    }

    fun resetTunablesToDefaults(context: Context) {
        val editor = prefs(context).edit()
        TUNABLE_DEFAULTS.keys.forEach { key ->
            editor.remove("${PREFS_KEY_PREFIX}tunable_$key")
        }
        editor.apply()
    }

    fun getNonRootFocusedPackageName(context: Context): String? {
        try {
            val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val time = System.currentTimeMillis()
            val appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 10000, time)
            if (appList != null && appList.isNotEmpty()) {
                val sorted = TreeMap<Long, android.app.usage.UsageStats>()
                for (us in appList) {
                    sorted[us.lastTimeUsed] = us
                }
                if (sorted.isNotEmpty()) {
                    return sorted[sorted.lastKey()]?.packageName
                }
            }
} catch (e: Exception) {
                Log.w("EnforceDozeManager", "getMostRecentApp reflection failed", e)
            }
            return null
    }

    fun isCharging(context: Context): Boolean {
        val intent = context.registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED)) ?: return false
        val plugged = intent.getIntExtra(android.os.BatteryManager.EXTRA_PLUGGED, -1)
        return plugged == android.os.BatteryManager.BATTERY_PLUGGED_AC ||
            plugged == android.os.BatteryManager.BATTERY_PLUGGED_USB ||
            plugged == android.os.BatteryManager.BATTERY_PLUGGED_WIRELESS
    }

    fun isScreenOn(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        return pm.isInteractive
    }

    fun getBatteryLevel(context: Context): Int {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as android.os.BatteryManager
        return bm.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    fun getCurrentMinuteOfDay(): Int {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    }

    fun parseCustomDozePeriod(period: String): IntArray? {
        val parts = period.split("-")
        if (parts.size != 2) return null
        val start = parseCustomDozeTime(parts[0])
        val end = parseCustomDozeTime(parts[1])
        if (start < 0 || end < 0 || start == end) return null
        return IntArray(2) { if (it == 0) start else end }
    }

    private fun parseCustomDozeTime(time: String): Int {
        val parts = time.split(":")
        if (parts.size != 2) return -1
        return try {
            val h = parts[0].toInt()
            val m = parts[1].toInt()
            if (h in 0..23 && m in 0..59) h * 60 + m else -1
        } catch (_: NumberFormatException) {
            -1
        }
    }

    fun isInsideCustomDozePeriod(context: Context): Boolean {
        val periods = getCustomDozePeriods(context)
        if (periods.isEmpty()) return true
        val now = getCurrentMinuteOfDay()
        for (period in periods) {
            val parsed = parseCustomDozePeriod(period) ?: continue
            val start = parsed[0]
            val end = parsed[1]
            if (start < end && now in start until end) return true
            if (start > end && (now >= start || now < end)) return true
        }
        return false
    }

    fun isDeviceIdleMode(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        return pm.isDeviceIdleMode
    }
}
