package moe.shizuku.manager.tether

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.shizuku.manager.utils.runShellCommand
import rikka.shizuku.Shizuku

data class TetherSettingsState(
    val tetherDunRequired: String?,
    val entitlementCheckState: String?,
    val tetherSupported: String?
)

data class TetherOpResult(
    val success: Boolean,
    val message: String
)

object TetherManager {

    fun isShizukuAvailable(): Boolean = Shizuku.pingBinder()

    fun isRootAvailable(): Boolean {
        return try {
            Shizuku.pingBinder() && Shizuku.getUid() == 0
        } catch (_: Throwable) {
            false
        }
    }

    suspend fun getTetherState(): TetherSettingsState = withContext(Dispatchers.IO) {
        val dun = runShellCommand("settings get global tether_dun_required")?.trim()
        val ent = runShellCommand("settings get global tether_entitlement_check_state")?.trim()
        val sup = runShellCommand("settings get global tether_supported")?.trim()
        TetherSettingsState(
            tetherDunRequired = if (dun.isNullOrBlank() || dun == "null") "Not set (Default)" else dun,
            entitlementCheckState = if (ent.isNullOrBlank() || ent == "null") "Not set (Default)" else ent,
            tetherSupported = if (sup.isNullOrBlank() || sup == "null") "Not set (Default)" else sup
        )
    }

    suspend fun bypassCarrierRestrictions(): TetherOpResult = withContext(Dispatchers.IO) {
        if (!isShizukuAvailable()) {
            return@withContext TetherOpResult(false, "Shizuku service is not running")
        }
        val c1 = runShellCommand("settings put global tether_dun_required 0") != null
        val c2 = runShellCommand("settings put global tether_entitlement_check_state 0") != null
        val c3 = runShellCommand("settings put global tether_supported 1") != null

        if (c1 && c2 && c3) {
            TetherOpResult(true, "Carrier entitlement checks bypassed successfully")
        } else {
            TetherOpResult(false, "Failed to apply system settings changes")
        }
    }

    suspend fun restoreDefaultRestrictions(): TetherOpResult = withContext(Dispatchers.IO) {
        if (!isShizukuAvailable()) {
            return@withContext TetherOpResult(false, "Shizuku service is not running")
        }
        val c1 = runShellCommand("settings delete global tether_dun_required") != null
        val c2 = runShellCommand("settings delete global tether_entitlement_check_state") != null
        val c3 = runShellCommand("settings delete global tether_supported") != null

        if (c1 || c2 || c3) {
            TetherOpResult(true, "Carrier settings restored to factory defaults")
        } else {
            TetherOpResult(false, "Failed to restore carrier settings")
        }
    }

    suspend fun applyTtlBypass(ttl: Int = 64): TetherOpResult = withContext(Dispatchers.IO) {
        if (!isShizukuAvailable()) {
            return@withContext TetherOpResult(false, "Shizuku service is not running")
        }
        val res = runShellCommand("iptables -t mangle -D POSTROUTING -j TTL --ttl-set $ttl; iptables -t mangle -A POSTROUTING -j TTL --ttl-set $ttl")
        if (res != null && (res.contains("Permission denied") || res.contains("not allowed") || res.contains("can't initialize iptables table"))) {
            TetherOpResult(false, "Kernel iptables netfilter requires Root access on modern Android. Shizuku Shell access is insufficient for iptables table modifications.")
        } else if (res != null && res.isNotEmpty() && !res.contains("error", ignoreCase = true)) {
            TetherOpResult(true, "TTL $ttl rule updated: $res")
        } else if (res != null) {
            TetherOpResult(true, "TTL $ttl netfilter rule dispatched")
        } else {
            TetherOpResult(false, "Netfilter command requires Root privileges or custom kernel")
        }
    }
}
