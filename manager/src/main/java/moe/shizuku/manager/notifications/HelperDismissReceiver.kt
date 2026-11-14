package moe.shizuku.manager.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Handles explicit dismissal of Helper notifications by the user to record the dismissed
 * deduplication key and suppress spam until the server or recovery state changes.
 */
class HelperDismissReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val key = intent?.getStringExtra(EXTRA_DEDUPE_KEY)
        HelperNotificationManager.onDismissed(key, context)
    }

    companion object {
        const val ACTION_DISMISS = "moe.shizuku.manager.action.HELPER_NOTIFICATION_DISMISS"
        const val EXTRA_DEDUPE_KEY = "extra_dedupe_key"
    }
}
