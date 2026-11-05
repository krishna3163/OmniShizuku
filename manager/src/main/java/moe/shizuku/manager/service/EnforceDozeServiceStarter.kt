package moe.shizuku.manager.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import moe.shizuku.manager.doze.EnforceDozeService

object EnforceDozeServiceStarter {
    fun startService(context: Context) {
        val intent = Intent(context, EnforceDozeService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pi = PendingIntent.getForegroundService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            if (alarm.canScheduleExactAlarms()) {
                alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 200, pi)
            } else {
                context.startForegroundService(intent)
            }
        } else {
            context.startService(intent)
        }
    }

    fun stopService(context: Context) {
        context.stopService(Intent(context, EnforceDozeService::class.java))
    }
}
