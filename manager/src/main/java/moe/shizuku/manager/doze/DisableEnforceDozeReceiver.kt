package moe.shizuku.manager.doze

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import moe.shizuku.manager.ShizukuSettings

class DisableEnforceDozeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ShizukuSettings.getPreferences().edit().putBoolean("enforcedoze_serviceEnabled", false).apply()
        context.stopService(Intent(context, EnforceDozeService::class.java))
    }
}
