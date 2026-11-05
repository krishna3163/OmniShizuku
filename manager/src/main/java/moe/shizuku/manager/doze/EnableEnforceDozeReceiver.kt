package moe.shizuku.manager.doze

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import moe.shizuku.manager.ShizukuSettings

class EnableEnforceDozeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ShizukuSettings.getPreferences().edit().putBoolean("enforcedoze_serviceEnabled", true).apply()
        val serviceIntent = Intent(context, EnforceDozeService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
