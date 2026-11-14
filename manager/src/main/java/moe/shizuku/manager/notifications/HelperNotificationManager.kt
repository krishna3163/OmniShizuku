package moe.shizuku.manager.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import moe.shizuku.manager.MainActivity
import moe.shizuku.manager.R
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.service.WatchdogService

object HelperNotificationManager {
    interface Notifier {
        fun notify(context: Context, id: Int, notification: android.app.Notification)
        fun cancel(context: Context, id: Int)
    }

    object SystemNotifier : Notifier {
        override fun notify(context: Context, id: Int, notification: android.app.Notification) {
            (context.getSystemService(NotificationManager::class.java))?.notify(id, notification)
        }
        override fun cancel(context: Context, id: Int) {
            (context.getSystemService(NotificationManager::class.java))?.cancel(id)
        }
    }

    private const val PREF_LAST_KEY = "helper_notif_last_key"
    private const val PREF_DISMISSED_KEY = "helper_notif_dismissed_key"

    val policy = HelperNotificationPolicy(debounceMs = 5000L)
    @Volatile var notifier: Notifier = SystemNotifier

    private fun prefs(context: Context) = runCatching { ShizukuSettings.getPreferences() }.getOrNull()

    fun loadState(context: Context) {
        val p = prefs(context) ?: return
        val last = p.getString(PREF_LAST_KEY, null)
        val dismissed = p.getString(PREF_DISMISSED_KEY, null)
        policy.restore(last, dismissed)
    }

    private fun persistState(context: Context) {
        val p = prefs(context) ?: return
        p.edit()
            .putString(PREF_LAST_KEY, policy.lastKey)
            .putString(PREF_DISMISSED_KEY, policy.dismissedKey)
            .apply()
    }

    fun resetForTests() {
        policy.reset()
        notifier = SystemNotifier
    }

    fun onDismissed(key: String? = null, context: Context? = null) {
        policy.onDismissed(key)
        context?.let { persistState(it.applicationContext) }
    }

    fun clearHistory(context: Context) {
        policy.reset()
        persistState(context.applicationContext)
        notifier.cancel(context.applicationContext, NotificationIds.HELPER)
    }

    fun onServerHealthy(context: Context) {
        val app = context.applicationContext
        loadState(app)
        if (policy.onHealthy()) {
            notifier.cancel(app, NotificationIds.HELPER)
        }
        if (!NotificationPreferences.canShowHelper(app)) {
            persistState(app)
            return
        }
        postIfNeeded(app, HelperEvent(HelperEventType.RECOVERED, serverGeneration = serverGeneration(app)))
    }

    fun post(event: HelperEvent, context: Context) {
        postIfNeeded(context.applicationContext, event)
    }

    fun postIfNeeded(context: Context, event: HelperEvent) {
        val app = context.applicationContext
        loadState(app)
        val effective = event.copy(serverGeneration = if (event.serverGeneration == 0L) serverGeneration(app) else event.serverGeneration)
        val now = SystemClock.elapsedRealtime()

        if (!policy.shouldPost(
                event = effective,
                nowElapsed = now,
                helperEnabled = NotificationPreferences.helperEnabled(),
                notificationsAllowed = NotificationPreferences.notificationsAllowed(app) && NotificationPreferences.isChannelEnabled(app, NotificationChannels.HELPER)
            )
        ) {
            return
        }

        NotificationChannels.ensureHelper(app)

        val (titleRes, textRes) = when (effective.type) {
            HelperEventType.SERVER_STOPPED -> R.string.notification_helper_stopped_title to R.string.notification_helper_stopped_text
            HelperEventType.NEEDS_ATTENTION -> R.string.notification_helper_attention_title to R.string.notification_helper_attention_text
            HelperEventType.RECOVERED -> R.string.notification_helper_recovered_title to R.string.notification_helper_recovered_text
            HelperEventType.PERMISSION_REQUIRED -> R.string.notification_helper_permission_title to R.string.notification_helper_permission_text
        }
        val text = effective.message ?: app.getString(textRes)
        val openIntent = PendingIntent.getActivity(
            app, NotificationIds.HELPER,
            Intent(app, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val key = helperDedupeKey(effective)
        val dismissIntent = PendingIntent.getBroadcast(
            app, NotificationIds.HELPER + 2,
            Intent(app, HelperDismissReceiver::class.java).apply {
                action = HelperDismissReceiver.ACTION_DISMISS
                putExtra(HelperDismissReceiver.EXTRA_DEDUPE_KEY, key)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(app, NotificationChannels.HELPER)
            .setSmallIcon(R.drawable.ic_system_icon)
            .setContentTitle(app.getString(titleRes))
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(openIntent)
            .setDeleteIntent(dismissIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (effective.showRestart) {
            val restartIntent = PendingIntent.getService(
                app, NotificationIds.HELPER + 10,
                Intent(app, WatchdogService::class.java).setAction(WatchdogService.ACTION_START_NOW),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, app.getString(R.string.notification_helper_action_restart), restartIntent)
        }

        notifier.notify(app, NotificationIds.HELPER, builder.build())
        policy.recordPosted(effective, now)
        persistState(app)
    }

    private fun serverGeneration(context: Context): Long {
        return runCatching {
            android.provider.Settings.Global.getInt(context.contentResolver, android.provider.Settings.Global.BOOT_COUNT, 0).toLong()
        }.getOrDefault(0L)
    }
}
