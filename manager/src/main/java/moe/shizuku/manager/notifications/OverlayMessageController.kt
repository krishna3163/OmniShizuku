package moe.shizuku.manager.notifications

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView

/**
 * Least-privileged overlay: TYPE_APPLICATION_OVERLAY, requires explicit SYSTEM_ALERT_WINDOW grant.
 * Non-interactive informational message (touches pass through), removed on revoke/destroy/disable.
 */
class OverlayMessageController(private val context: Context) : HelperMessageDispatcher.OverlayController {
    private val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var view: TextView? = null

    override fun canShow(): Boolean {
        return NotificationPreferences.messagesOverOtherAppsEnabled() && canDrawOverlays()
    }

    fun canDrawOverlays(): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(context) else true

    override fun show(message: HelperMessage, onDone: () -> Unit) {
        removeView()
        if (!canShow()) { onDone(); return }
        val tv = TextView(context).apply {
            text = message.text
            setPadding(32, 20, 32, 20)
            setBackgroundResource(android.R.drawable.toast_frame)
            contentDescription = message.text
            importantForAccessibility = android.view.View.IMPORTANT_FOR_ACCESSIBILITY_YES
        }
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE
        val flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            flags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 120
        }
        try {
            wm.addView(tv, lp)
            view = tv
            tv.postDelayed({
                if (view === tv) removeView()
                onDone()
            }, message.durationMs)
        } catch (_: Throwable) {
            view = null
            onDone()
        }
    }

    private fun removeView() {
        val v = view ?: return
        view = null
        runCatching { wm.removeViewImmediate(v) }
    }

    override fun removeAll() { removeView() }
}
