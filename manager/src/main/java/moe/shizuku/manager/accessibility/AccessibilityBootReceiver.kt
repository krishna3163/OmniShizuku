package moe.shizuku.manager.accessibility

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import moe.shizuku.manager.ktx.logd

/**
 * Starts the accessibility keep-alive daemon after device boot when the
 * auto-boot preference is enabled.
 */
class AccessibilityBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) {
            return
        }
        if (!AccessibilityKeepAliveStore.isAutoBootEnabled()) return
        if (!AccessibilityKeepAliveStore.isKeepAliveEnabled()) return

        logd("Boot completed — starting accessibility keep-alive daemon")
        AccessibilityDaemonService.reconcile(context.applicationContext)
    }
}
