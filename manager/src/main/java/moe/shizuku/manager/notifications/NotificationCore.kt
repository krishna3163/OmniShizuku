package moe.shizuku.manager.notifications

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import moe.shizuku.manager.R
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.utils.createChannelCompat

object NotificationIds {
    const val HELPER = NotificationRegistry.Ids.HELPER
    const val USB_CONNECTION = NotificationRegistry.Ids.USB_CONNECTION
}

object NotificationChannels {
    const val HELPER = NotificationRegistry.Channels.HELPER
    const val USB_CONNECTION = NotificationRegistry.Channels.USB_CONNECTION
    const val WATCHDOG_ONGOING = NotificationRegistry.Channels.WATCHDOG_ONGOING
    const val WATCHDOG_REPORTS = NotificationRegistry.Channels.WATCHDOG_REPORTS
    const val ADB_PAIRING = NotificationRegistry.Channels.ADB_PAIRING
    const val LEGACY_RISH = NotificationRegistry.Channels.LEGACY_RISH

    fun ensureHelper(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createChannelCompat(HELPER, context.getString(R.string.notification_channel_helper), NotificationManager.IMPORTANCE_DEFAULT, showBadge = true)
    }

    fun ensureUsb(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createChannelCompat(USB_CONNECTION, context.getString(R.string.notification_channel_usb_connection), NotificationManager.IMPORTANCE_LOW, showBadge = false)
    }
}

object NotificationPreferences {
    fun helperEnabled() = ShizukuSettings.getHelperNotificationsEnabled()
    fun messagesOverOtherAppsEnabled() = ShizukuSettings.getMessagesOverOtherAppsEnabled()
    fun liveActivityEnabled() = ShizukuSettings.getLiveActivityNotificationsEnabled()
    fun usbConnectionEnabled() = ShizukuSettings.getUsbConnectionNotificationEnabled()

    fun notificationsAllowed(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < 33) return true
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    fun canShowHelper(context: Context): Boolean =
        helperEnabled() && notificationsAllowed(context) && isChannelEnabled(context, NotificationChannels.HELPER)

    fun canShowUsb(context: Context): Boolean =
        usbConnectionEnabled() && notificationsAllowed(context) && isChannelEnabled(context, NotificationChannels.USB_CONNECTION)

    fun isChannelEnabled(context: Context, channelId: String): Boolean {
        val nm = context.getSystemService(NotificationManager::class.java) ?: return notificationsAllowed(context)
        if (Build.VERSION.SDK_INT >= 24 && !nm.areNotificationsEnabled()) return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return notificationsAllowed(context)
        val ch = nm.getNotificationChannel(channelId) ?: return true
        return ch.importance != NotificationManager.IMPORTANCE_NONE
    }

    fun isChannelBlocked(context: Context, channelId: String): Boolean = !isChannelEnabled(context, channelId)
}
