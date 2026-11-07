package moe.shizuku.manager.shizuwall

import android.content.Context
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.manage.PackageTools
import moe.shizuku.manager.utils.runShellCommand

object ShizuWallManager {

    private const val PREF_NAME = "shizuwall_preferences"
    private const val KEY_FIREWALL_MASTER = "shizuwall_master_enabled"
    private const val KEY_FIREWALL_MODE = "shizuwall_mode" // "BLOCKLIST" or "WHITELIST"

    fun isMasterEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_FIREWALL_MASTER, false)
    }

    fun setMasterEnabled(context: Context, enabled: Boolean): Boolean {
        val cmd = "cmd connectivity set-chain3-enabled ${if (enabled) "true" else "false"}"
        val res = runShellCommand(cmd)
        val success = res != null
        if (success) {
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_FIREWALL_MASTER, enabled)
                .apply()
        }
        return success
    }

    fun getFirewallMode(context: Context): String {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_FIREWALL_MODE, "BLOCKLIST") ?: "BLOCKLIST"
    }

    fun setFirewallMode(context: Context, mode: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FIREWALL_MODE, mode)
            .apply()
    }

    fun setPackageBlocked(context: Context, packageName: String, blocked: Boolean): Boolean {
        return PackageTools.setNetworkBlocked(context, packageName, blocked)
    }

    fun isPackageBlocked(context: Context, packageName: String): Boolean {
        return PackageTools.readNetworkBlocked(context, packageName) ?: false
    }
}

