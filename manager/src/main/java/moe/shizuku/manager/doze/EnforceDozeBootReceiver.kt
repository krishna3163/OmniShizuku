package moe.shizuku.manager.doze

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import moe.shizuku.manager.ShizukuSettings

class EnforceDozeBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val enabled = ShizukuSettings.getPreferences().getBoolean("enforcedoze_serviceEnabled", false)
        if (enabled) {
            val serviceIntent = Intent(context, EnforceDozeService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
        CustomDozePeriodReceiver().scheduleNextCustomDozePeriodBoundary(context)
    }
}
