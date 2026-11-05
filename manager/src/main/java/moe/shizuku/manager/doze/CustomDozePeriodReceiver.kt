package moe.shizuku.manager.doze

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import moe.shizuku.manager.ShizukuSettings
import java.util.Calendar

class CustomDozePeriodReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        scheduleNextCustomDozePeriodBoundary(context)
    }

    fun scheduleNextCustomDozePeriodBoundary(context: Context) {
        cancelCustomDozePeriodAlarm(context)
        if (!hasCustomDozePeriods(context)) return
        val delay = getMillisUntilNextCustomDozePeriodBoundary(context)
        if (delay < 0) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = getCustomDozePeriodPendingIntent(context)
        val triggerAt = System.currentTimeMillis() + delay
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
        } catch (_: Exception) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
        }
    }

    fun cancelCustomDozePeriodAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(getCustomDozePeriodPendingIntent(context))
    }

    fun hasCustomDozePeriods(context: Context): Boolean {
        return EnforceDozeManager.getCustomDozePeriods(context).isNotEmpty()
    }

    private fun getCustomDozePeriodPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, CustomDozePeriodReceiver::class.java)
        intent.action = "moe.shizuku.manager.enforcedoze.ACTION_CUSTOM_DOZE_PERIOD_BOUNDARY"
        return PendingIntent.getBroadcast(context, 9012, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun getMillisUntilNextCustomDozePeriodBoundary(context: Context): Long {
        val now = Calendar.getInstance()
        val nowMillis = now.timeInMillis
        var nextBoundary = Long.MAX_VALUE
        for (period in EnforceDozeManager.getCustomDozePeriods(context)) {
            val parsed = EnforceDozeManager.parseCustomDozePeriod(period) ?: continue
            nextBoundary = kotlin.math.min(nextBoundary, getNextBoundaryMillis(now, parsed[0]))
            nextBoundary = kotlin.math.min(nextBoundary, getNextBoundaryMillis(now, parsed[1]))
        }
        if (nextBoundary == Long.MAX_VALUE) return -1
        return kotlin.math.max(1000, nextBoundary - nowMillis)
    }

    private fun getNextBoundaryMillis(now: Calendar, minuteOfDay: Int): Long {
        val boundary = now.clone() as Calendar
        boundary.set(Calendar.HOUR_OF_DAY, minuteOfDay / 60)
        boundary.set(Calendar.MINUTE, minuteOfDay % 60)
        boundary.set(Calendar.SECOND, 0)
        boundary.set(Calendar.MILLISECOND, 0)
        if (!boundary.after(now)) boundary.add(Calendar.DAY_OF_YEAR, 1)
        return boundary.timeInMillis
    }
}
