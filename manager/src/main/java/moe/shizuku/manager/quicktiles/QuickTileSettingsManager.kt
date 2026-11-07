package moe.shizuku.manager.quicktiles

import android.content.Context
import android.provider.Settings
import moe.shizuku.manager.utils.runShellCommand

object QuickTileSettingsManager {

    private const val PREF_NAME = "quick_tile_settings"
    private const val KEY_DNS_AUTO_REVERT = "dns_auto_revert"
    private const val KEY_DNS_REVERT_DELAY = "dns_revert_delay" // in seconds
    private const val KEY_USB_AUTO_REVERT = "usb_auto_revert"
    private const val KEY_USB_REVERT_DELAY = "usb_revert_delay" // in seconds

    private const val KEY_CYCLE_DNS_OFF = "cycle_dns_off"
    private const val KEY_CYCLE_DNS_AUTO = "cycle_dns_auto"
    private const val KEY_CYCLE_DNS_CLOUDFLARE = "cycle_dns_cloudflare"
    private const val KEY_CYCLE_DNS_ADGUARD = "cycle_dns_adguard"
    private const val KEY_CYCLE_DNS_QUAD9 = "cycle_dns_quad9"

    const val HOSTNAME_CLOUDFLARE = "one.one.one.one"
    const val HOSTNAME_ADGUARD = "dns.adguard-dns.com"
    const val HOSTNAME_QUAD9 = "dns.quad9.net"

    fun isDnsAutoRevert(context: Context): Boolean =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_DNS_AUTO_REVERT, false)

    fun setDnsAutoRevert(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_DNS_AUTO_REVERT, enabled).apply()
    }

    fun getDnsRevertDelay(context: Context): Int =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(KEY_DNS_REVERT_DELAY, 60)

    fun setDnsRevertDelay(context: Context, seconds: Int) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_DNS_REVERT_DELAY, seconds).apply()
    }

    fun isUsbAutoRevert(context: Context): Boolean =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_USB_AUTO_REVERT, false)

    fun setUsbAutoRevert(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_USB_AUTO_REVERT, enabled).apply()
    }

    fun getUsbRevertDelay(context: Context): Int =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(KEY_USB_REVERT_DELAY, 60)

    fun setUsbRevertDelay(context: Context, seconds: Int) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_USB_REVERT_DELAY, seconds).apply()
    }

    // DNS Cycle config
    fun isCycleDnsOff(context: Context): Boolean =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_CYCLE_DNS_OFF, true)

    fun setCycleDnsOff(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_CYCLE_DNS_OFF, enabled).apply()
    }

    fun isCycleDnsAuto(context: Context): Boolean =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_CYCLE_DNS_AUTO, true)

    fun setCycleDnsAuto(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_CYCLE_DNS_AUTO, enabled).apply()
    }

    fun isCycleDnsCloudflare(context: Context): Boolean =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_CYCLE_DNS_CLOUDFLARE, true)

    fun setCycleDnsCloudflare(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_CYCLE_DNS_CLOUDFLARE, enabled).apply()
    }

    fun isCycleDnsAdguard(context: Context): Boolean =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_CYCLE_DNS_ADGUARD, true)

    fun setCycleDnsAdguard(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_CYCLE_DNS_ADGUARD, enabled).apply()
    }

    fun isCycleDnsQuad9(context: Context): Boolean =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_CYCLE_DNS_QUAD9, true)

    fun setCycleDnsQuad9(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_CYCLE_DNS_QUAD9, enabled).apply()
    }

    // Direct system controls via ADB shell or Settings.Global
    fun isUsbDebuggingEnabled(context: Context): Boolean {
        return try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        } catch (_: Exception) {
            val res = runShellCommand("settings get global adb_enabled")?.trim()
            res == "1"
        }
    }

    fun setUsbDebuggingEnabled(enabled: Boolean): Boolean {
        val value = if (enabled) "1" else "0"
        val cmd = "settings put global adb_enabled $value"
        return runShellCommand(cmd) != null
    }

    fun isWirelessDebuggingEnabled(context: Context): Boolean {
        return try {
            Settings.Global.getInt(context.contentResolver, "adb_wifi_enabled", 0) == 1
        } catch (_: Exception) {
            val res = runShellCommand("settings get global adb_wifi_enabled")?.trim()
            res == "1"
        }
    }

    fun setWirelessDebuggingEnabled(enabled: Boolean): Boolean {
        val value = if (enabled) "1" else "0"
        val cmd = "settings put global adb_wifi_enabled $value"
        return runShellCommand(cmd) != null
    }

    fun isDevOptionsEnabled(context: Context): Boolean {
        return try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1
        } catch (_: Exception) {
            val res = runShellCommand("settings get global development_settings_enabled")?.trim()
            res == "1"
        }
    }

    fun setDevOptionsEnabled(enabled: Boolean): Boolean {
        val value = if (enabled) "1" else "0"
        val cmd = "settings put global development_settings_enabled $value"
        return runShellCommand(cmd) != null
    }

    fun getPrivateDnsMode(context: Context): String {
        return try {
            Settings.Global.getString(context.contentResolver, "private_dns_mode") ?: "off"
        } catch (_: Exception) {
            runShellCommand("settings get global private_dns_mode")?.trim() ?: "off"
        }
    }

    fun getPrivateDnsSpecifier(context: Context): String {
        return try {
            Settings.Global.getString(context.contentResolver, "private_dns_specifier").orEmpty()
        } catch (_: Exception) {
            runShellCommand("settings get global private_dns_specifier")?.trim().orEmpty()
        }
    }

    fun setPrivateDnsMode(mode: String, specifier: String? = null): Boolean {
        val ok1 = runShellCommand("settings put global private_dns_mode $mode") != null
        if (!specifier.isNullOrBlank()) {
            runShellCommand("settings put global private_dns_specifier $specifier")
        }
        return ok1
    }
}

