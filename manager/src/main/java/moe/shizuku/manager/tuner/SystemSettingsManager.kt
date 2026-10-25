package moe.shizuku.manager.tuner

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.shizuku.manager.utils.runShellCommand

object SystemSettingsManager {

    enum class Table(val arg: String) {
        GLOBAL("global"),
        SECURE("secure"),
        SYSTEM("system")
    }

    data class SettingItem(
        val table: Table,
        val key: String,
        val value: String
    )

    data class StatusBarIcon(
        val slot: String,
        val label: String
    )

    val STATUS_BAR_ICONS = listOf(
        StatusBarIcon("battery", "Battery"),
        StatusBarIcon("clock", "Clock"),
        StatusBarIcon("alarm_clock", "Alarm Clock"),
        StatusBarIcon("wifi", "Wi-Fi"),
        StatusBarIcon("mobile", "Mobile Data / Cellular"),
        StatusBarIcon("bluetooth", "Bluetooth"),
        StatusBarIcon("headset", "Headphones / Headset"),
        StatusBarIcon("location", "Location (GPS)"),
        StatusBarIcon("nfc", "NFC"),
        StatusBarIcon("hotspot", "Hotspot"),
        StatusBarIcon("volume", "Volume / Vibrate"),
        StatusBarIcon("cast", "Cast / Screen Mirror"),
        StatusBarIcon("vpn", "VPN"),
        StatusBarIcon("rotate", "Auto-Rotate"),
        StatusBarIcon("zen", "Do Not Disturb (Zen)")
    )

    enum class ImmersiveMode(val value: String?, val label: String) {
        NORMAL(null, "Standard (No override)"),
        FULL("immersive.full=*", "Full Screen (Hide Status & Nav Bar)"),
        STATUS("immersive.status=*", "Hide Status Bar Only"),
        NAVIGATION("immersive.navigation=*", "Hide Navigation Bar Only")
    }

    suspend fun getSetting(table: Table, key: String): String? = withContext(Dispatchers.IO) {
        val out = runShellCommand("settings get ${table.arg} $key")?.trim()
        if (out == "null" || out.isNullOrBlank()) null else out
    }

    suspend fun putSetting(table: Table, key: String, value: String): Boolean = withContext(Dispatchers.IO) {
        val cmd = "settings put ${table.arg} $key $value"
        runShellCommand(cmd) != null
    }

    suspend fun deleteSetting(table: Table, key: String): Boolean = withContext(Dispatchers.IO) {
        val cmd = "settings delete ${table.arg} $key"
        runShellCommand(cmd) != null
    }

    suspend fun getBlacklistedIcons(): Set<String> = withContext(Dispatchers.IO) {
        val blacklist = getSetting(Table.SECURE, "icon_blacklist") ?: return@withContext emptySet()
        blacklist.split(',').map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }

    suspend fun setIconVisibility(slot: String, visible: Boolean): Boolean = withContext(Dispatchers.IO) {
        val currentBlacklist = getBlacklistedIcons().toMutableSet()
        if (visible) {
            currentBlacklist.remove(slot)
        } else {
            currentBlacklist.add(slot)
        }
        val newValue = currentBlacklist.joinToString(",")
        putSetting(Table.SECURE, "icon_blacklist", newValue)
    }

    suspend fun getImmersiveMode(): ImmersiveMode = withContext(Dispatchers.IO) {
        val value = getSetting(Table.GLOBAL, "policy_control")
        when (value) {
            "immersive.full=*" -> ImmersiveMode.FULL
            "immersive.status=*" -> ImmersiveMode.STATUS
            "immersive.navigation=*" -> ImmersiveMode.NAVIGATION
            else -> ImmersiveMode.NORMAL
        }
    }

    suspend fun setImmersiveMode(mode: ImmersiveMode): Boolean = withContext(Dispatchers.IO) {
        if (mode.value == null) {
            deleteSetting(Table.GLOBAL, "policy_control")
        } else {
            putSetting(Table.GLOBAL, "policy_control", mode.value)
        }
    }

    suspend fun listSettings(table: Table): List<SettingItem> = withContext(Dispatchers.IO) {
        val output = runShellCommand("settings list ${table.arg}") ?: return@withContext emptyList()
        output.lines()
            .mapNotNull { line ->
                val eqIdx = line.indexOf('=')
                if (eqIdx > 0) {
                    val key = line.substring(0, eqIdx).trim()
                    val value = line.substring(eqIdx + 1).trim()
                    SettingItem(table, key, value)
                } else null
            }
            .sortedBy { it.key }
    }
}
