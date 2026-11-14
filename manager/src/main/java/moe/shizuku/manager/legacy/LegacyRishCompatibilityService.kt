package moe.shizuku.manager.legacy

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import moe.shizuku.manager.MainActivity
import moe.shizuku.manager.R

class LegacyRishCompatibilityService : Service() {
    companion object {
        const val CHANNEL_ID = "legacy_rish_compatibility"
        const val CHANNEL_NAME = "OmniShizuku - Legacy Rish Compatibility"
        private const val NOTIFICATION_ID = 1458
        const val ACTION_DISABLE = "moe.shizuku.manager.action.LEGACY_RISH_DISABLE"
        fun intent(context: Context): Intent = Intent(context, LegacyRishCompatibilityService::class.java)
    }

    @Volatile private var startedGeneration: Long = -1L
    private var listener: ((LegacyRishSnapshot) -> Unit)? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        LegacyRishCompatibilityManager.init(applicationContext)
        ensureChannel()
        startedGeneration = LegacyRishCompatibilityManager.getSnapshot().generation
        startForegroundCompat(buildNotification(LegacyRishCompatibilityManager.getSnapshot()))
        LegacyRishCompatibilityManager.onServiceStarted(applicationContext, startedGeneration)
        val l: (LegacyRishSnapshot) -> Unit = { snapshot ->
            if (startedGeneration == -1L || snapshot.generation == startedGeneration) updateNotification(snapshot)
            if (!snapshot.enabled) stopSelfSafely()
        }
        listener = l
        LegacyRishCompatibilityManager.addListener(l)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_DISABLE) {
            LegacyRishCompatibilityManager.setEnabled(applicationContext, false)
            stopSelfSafely()
            return START_NOT_STICKY
        }
        LegacyRishCompatibilityManager.init(applicationContext)
        ensureChannel()
        val snapshot = LegacyRishCompatibilityManager.getSnapshot()
        if (!snapshot.enabled && snapshot.state == LegacyRishCompatibilityState.DISABLED) {
            stopSelfSafely(); return START_NOT_STICKY
        }
        if (startedGeneration == -1L) startedGeneration = snapshot.generation
        startForegroundCompat(buildNotification(snapshot))
        LegacyRishCompatibilityManager.onServiceStarted(applicationContext, startedGeneration)
        return START_STICKY
    }

    override fun onDestroy() {
        listener?.let { LegacyRishCompatibilityManager.removeListener(it) }
        listener = null
        val gen = startedGeneration
        if (gen != -1L) LegacyRishCompatibilityManager.onServiceStopped(applicationContext, gen)
        super.onDestroy()
    }

    private fun updateNotification(snapshot: LegacyRishSnapshot) {
        if (startedGeneration != -1L && snapshot.generation != startedGeneration) return
        val nm = getSystemService(NotificationManager::class.java) ?: return
        runCatching { nm.notify(NOTIFICATION_ID, buildNotification(snapshot)) }
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW).apply {
            description = getString(R.string.legacy_rish_channel_description)
            setSound(null, null); enableVibration(false); setShowBadge(false)
        }
        nm.createNotificationChannel(channel)
    }

    private fun buildNotification(snapshot: LegacyRishSnapshot): Notification {
        val (contentTitle, contentText) = notificationText(snapshot)
        val openIntent = PendingIntent.getActivity(this, 1,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val disableIntent = PendingIntent.getService(this, 2, intent(this).setAction(ACTION_DISABLE),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_system_icon)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setContentIntent(openIntent)
            .setOngoing(true).setOnlyAlertOnce(true).setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(0, getString(R.string.legacy_rish_action_disable), disableIntent)
            .build()
    }

    private fun notificationText(snapshot: LegacyRishSnapshot): Pair<String, String> {
        val title = getString(R.string.legacy_rish_notification_title)
        val text = when (snapshot.state) {
            LegacyRishCompatibilityState.ACTIVE -> getString(R.string.legacy_rish_state_active)
            LegacyRishCompatibilityState.STARTING -> getString(R.string.legacy_rish_state_starting)
            LegacyRishCompatibilityState.RECONNECTING -> getString(R.string.legacy_rish_state_reconnecting)
            LegacyRishCompatibilityState.WAITING_FOR_SERVER -> getString(R.string.legacy_rish_state_waiting)
            LegacyRishCompatibilityState.STOPPING -> getString(R.string.legacy_rish_state_stopping)
            LegacyRishCompatibilityState.ERROR -> getString(R.string.legacy_rish_state_error, snapshot.lastError ?: getString(R.string.legacy_rish_unknown_error))
            LegacyRishCompatibilityState.DISABLED -> getString(R.string.legacy_rish_state_disabled)
        }
        return title to text
    }

    private fun startForegroundCompat(notification: Notification) {
        try {
            if (Build.VERSION.SDK_INT >= 34) startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            else startForeground(NOTIFICATION_ID, notification)
        } catch (t: Throwable) {
            LegacyRishCompatibilityManager.onServiceError(applicationContext, t.message ?: "Unable to enter foreground", startedGeneration)
            stopSelfSafely()
        }
    }

    private fun stopSelfSafely() {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) stopForeground(STOP_FOREGROUND_REMOVE)
            else @Suppress("DEPRECATION") stopForeground(true)
        }
        stopSelf()
    }
}
