package moe.shizuku.manager.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationCompat
import moe.shizuku.manager.MainActivity
import moe.shizuku.manager.R

object UsbConnectionNotificationManager {

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

    @Volatile private var shown = false
    @Volatile var notifier: Notifier = SystemNotifier

    fun resetForTests() {
        shown = false
        notifier = SystemNotifier
    }

    fun isShown(): Boolean = shown

    fun update(context: Context, state: UsbConnectionState) {
        val app = context.applicationContext
        val relevant = state.physicalConnected || state.hostDeviceAttached
        if (!NotificationPreferences.usbConnectionEnabled() || !NotificationPreferences.notificationsAllowed(app) || !relevant) {
            if (shown) {
                notifier.cancel(app, NotificationIds.USB_CONNECTION)
                shown = false
            }
            return
        }
        NotificationChannels.ensureUsb(app)
        val openIntent = PendingIntent.getActivity(
            app, NotificationIds.USB_CONNECTION,
            Intent(app, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val devIntent = PendingIntent.getActivity(
            app, NotificationIds.USB_CONNECTION + 1,
            Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val baseText = app.getString(R.string.notification_usb_connected_text)
        val fullText = UsbConnectionReducer.formatUsbNotificationText(baseText, state)

        val builder = NotificationCompat.Builder(app, NotificationChannels.USB_CONNECTION)
            .setSmallIcon(R.drawable.ic_system_icon)
            .setContentTitle(app.getString(R.string.notification_usb_connected_title))
            .setContentText(baseText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(fullText))
            .setContentIntent(openIntent)
            .setOngoing(relevant)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .addAction(0, app.getString(R.string.notification_usb_action_dashboard), openIntent)
            .addAction(0, "Developer options", devIntent)

        notifier.notify(app, NotificationIds.USB_CONNECTION, builder.build())
        shown = true
    }

    fun cancel(context: Context) {
        notifier.cancel(context.applicationContext, NotificationIds.USB_CONNECTION)
        shown = false
    }
}
