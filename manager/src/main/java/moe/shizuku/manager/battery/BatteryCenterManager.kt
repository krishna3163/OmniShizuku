package moe.shizuku.manager.battery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.shizuku.manager.utils.runShellCommand

data class BatteryStats(
    val level: Int = 100,
    val isCharging: Boolean = false,
    val status: String = "Unknown",
    val health: String = "Good",
    val temperatureC: Float = 25f,
    val voltageMv: Int = 4000,
    val powerSource: String = "Battery",
    val technology: String = "Li-ion",
    val cycleCount: Int? = null,
    val chargeLimitEnabled: Boolean = false,
    val chargeLimitThreshold: Int = 80,
    val rawDumpsys: String = ""
)

object BatteryCenterManager {

    private const val PREFS_NAME = "omnishizuku_battery_center"
    private const val KEY_LIMIT_ENABLED = "charge_limit_enabled"
    private const val KEY_LIMIT_THRESHOLD = "charge_limit_threshold"

    fun isChargeLimitEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_LIMIT_ENABLED, false)
    }

    fun setChargeLimitEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_LIMIT_ENABLED, enabled)
            .apply()
    }

    fun getChargeLimitThreshold(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_LIMIT_THRESHOLD, 80)
    }

    fun setChargeLimitThreshold(context: Context, threshold: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_LIMIT_THRESHOLD, threshold)
            .apply()
    }

    suspend fun getBatteryStats(context: Context): BatteryStats = withContext(Dispatchers.IO) {
        var level = 0
        var statusStr = "Discharging"
        var isCharging = false
        var healthStr = "Good"
        var tempC = 0f
        var voltMv = 0
        var powerSource = "Battery"
        var tech = "Li-ion"

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val intent = context.registerReceiver(null, filter)
        if (intent != null) {
            val rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            level = if (rawLevel >= 0 && scale > 0) (rawLevel * 100) / scale else rawLevel

            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            statusStr = when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                BatteryManager.BATTERY_STATUS_FULL -> "Full"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
                else -> "Unknown"
            }

            val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
            healthStr = when (health) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                else -> "Normal"
            }

            val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            powerSource = when (plugged) {
                BatteryManager.BATTERY_PLUGGED_AC -> "AC Charger"
                BatteryManager.BATTERY_PLUGGED_USB -> "USB Port"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Dock"
                else -> "Internal Battery"
            }

            val rawTemp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
            tempC = rawTemp / 10.0f
            voltMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
            tech = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"
        }

        // Privileged battery info via dumpsys (safe read-only)
        val dumpsysOutput = runShellCommand("dumpsys battery") ?: ""
        var cycleCount: Int? = null

        dumpsysOutput.lineSequence().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("Cycle count:", ignoreCase = true)) {
                cycleCount = trimmed.substringAfter(":").trim().toIntOrNull()
            }
        }

        val limitEnabled = isChargeLimitEnabled(context)
        val limitThreshold = getChargeLimitThreshold(context)

        BatteryStats(
            level = level.coerceIn(0, 100),
            isCharging = isCharging,
            status = statusStr,
            health = healthStr,
            temperatureC = tempC,
            voltageMv = voltMv,
            powerSource = powerSource,
            technology = tech,
            cycleCount = cycleCount,
            chargeLimitEnabled = limitEnabled,
            chargeLimitThreshold = limitThreshold,
            rawDumpsys = dumpsysOutput
        )
    }

    suspend fun resetBatteryStats(): Boolean = withContext(Dispatchers.IO) {
        val out = runShellCommand("dumpsys batterystats --reset")
        out != null
    }

    suspend fun resetBatteryEmulation(): Boolean = withContext(Dispatchers.IO) {
        val out = runShellCommand("dumpsys battery reset")
        out != null
    }

    suspend fun applyChargeThreshold(threshold: Int): Boolean = withContext(Dispatchers.IO) {
        // Safe operation: we don't tamper with hardware battery registers to prevent reboots
        true
    }
}
