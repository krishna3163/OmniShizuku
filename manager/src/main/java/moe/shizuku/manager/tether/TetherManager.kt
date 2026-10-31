package moe.shizuku.manager.tether

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.shizuku.manager.utils.runShellCommand

data class TetherSettingsState(
    val tetherDunRequired: String?,
    val entitlementCheckState: String?,
    val tetherSupported: String?
)

object TetherManager {

    suspend fun getTetherState(): TetherSettingsState = withContext(Dispatchers.IO) {
        val dun = runShellCommand("settings get global tether_dun_required")?.trim()
        val ent = runShellCommand("settings get global tether_entitlement_check_state")?.trim()
        val sup = runShellCommand("settings get global tether_supported")?.trim()
        TetherSettingsState(
            tetherDunRequired = dun,
            entitlementCheckState = ent,
            tetherSupported = sup
        )
    }

    suspend fun bypassCarrierRestrictions(): Boolean = withContext(Dispatchers.IO) {
        val c1 = runShellCommand("settings put global tether_dun_required 0") != null
        val c2 = runShellCommand("settings put global tether_entitlement_check_state 0") != null
        val c3 = runShellCommand("settings put global tether_supported 1") != null
        c1 && c2 && c3
    }

    suspend fun restoreDefaultRestrictions(): Boolean = withContext(Dispatchers.IO) {
        val c1 = runShellCommand("settings delete global tether_dun_required") != null
        val c2 = runShellCommand("settings delete global tether_entitlement_check_state") != null
        c1 && c2
    }

    suspend fun applyTtlBypass(ttl: Int = 64): Boolean = withContext(Dispatchers.IO) {
        runShellCommand("iptables -t mangle -D POSTROUTING -j TTL --ttl-set $ttl")
        runShellCommand("iptables -t mangle -A POSTROUTING -j TTL --ttl-set $ttl") != null
    }
}
