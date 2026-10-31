package moe.shizuku.manager.adb

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import moe.shizuku.manager.R
import moe.shizuku.manager.ShizukuSettings

/**
 * Enters the wireless ADB pairing PIN into the system’s “Pair with device” dialog when the user
 * has opted into automatic entry.
 *
 * It only ever looks at the settings windows that host that dialog (declared in
 * `wireless_adb_pin_service_config.xml`), and only while [WirelessAdbPinManager] is set to
 * [WirelessAdbPinMode.Automatic]. The PIN itself is the one the user stored in settings.
 *
 * On phones the user still opens the dialog themselves (Developer options → Wireless debugging
 * → “Pair device with pairing code”); this service finds the PIN field in it, types the stored
 * PIN, and taps the pair button. On anything that does not present a typable PIN field it does
 * nothing and reports so.
 */
class WirelessAdbPinService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var entering = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "Wireless ADB PIN service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (entering || event == null) return
        if (!WirelessAdbPinManager.isAutomatic(this)) return

        val packageName = event.packageName?.toString()
        if (!hostsPairingDialog(packageName)) return

        handler.removeCallbacks(enterAgain)
        entering = true
        enterAgain.run()
    }

    private val enterAgain = object : Runnable {
        private var attempts = 0
        override fun run() {
            if (!WirelessAdbPinManager.isAutomatic(this@WirelessAdbPinService)) {
                entering = false
                attempts = 0
                return
            }
            val entered = tryEnterPin()
            if (entered) {
                entering = false
                attempts = 0
                toast(getString(R.string.wireless_adb_pin_entered))
                return
            }
            attempts++
            if (attempts < ENTER_ATTEMPTS) {
                handler.postDelayed(this, ENTER_INTERVAL_MS)
            } else {
                entering = false
                attempts = 0
                toast(getString(R.string.wireless_adb_pin_not_found))
            }
        }
    }

    /**
     * Walks the visible windows looking for the pairing dialog’s PIN field, types the stored PIN
     * into it, and taps the pair/OK button.
     */
    private fun tryEnterPin(): Boolean {
        val pin = WirelessAdbPinManager.pin(this) ?: return false
        val windows = runCatching { windows }.getOrNull().orEmpty()

        for (window in windows) {
            val root = window.root ?: continue
            if (!hostsPairingDialog(root.packageName?.toString())) continue

            val pinField = findPinField(root) ?: continue

            runCatching { pinField.performAction(AccessibilityNodeInfo.ACTION_FOCUS) }
            runCatching { pinField.setText(pin) }

            val pairButton = findPairButton(root) ?: run {
                return true
            }
            runCatching { pairButton.performAction(AccessibilityNodeInfo.ACTION_CLICK) }
            return true
        }
        return false
    }

    /** An editable text node whose hint or content suggests a PIN / pairing code field. */
    private fun findPinField(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val text = (node.text ?: "").toString()
        val hint = (node.hintText ?: "").toString()
        val combined = (text + " " + hint).lowercase()
        val isEditable = node.isFocusable || node.isClickable
        val looksLikePin = combined.contains("pin") ||
            combined.contains("pairing code") ||
            combined.contains("pair code") ||
            combined.contains("enter code")
        if (looksLikePin && node.isEnabled && node.isVisibleToUser && isTextInput(node)) {
            return node
        }
        for (index in 0 until node.childCount) {
            node.getChild(index)?.let { child ->
                val found = findPinField(child)
                if (found != null) return found
            }
        }
        return null
    }

    private fun isTextInput(node: AccessibilityNodeInfo): Boolean {
        val inputTypes = node.inputType
        return inputTypes != 0 || node.className == "android.widget.EditText"
    }

    /** A button whose text says pair / OK / connect. */
    private fun findPairButton(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val text = (node.text ?: "").toString().lowercase()
        val isPair = text.contains("pair") || text.contains("ok") || text.contains("connect")
        if (isPair && node.isEnabled && node.isClickable && node.isVisibleToUser) {
            return node
        }
        for (index in 0 until node.childCount) {
            node.getChild(index)?.let { child ->
                val found = findPairButton(child)
                if (found != null) return found
            }
        }
        return null
    }

    private fun hostsPairingDialog(packageName: String?): Boolean =
        packageName?.let { it.contains("settings", ignoreCase = true) } == true

    private fun toast(message: String) {
        handler.post {
            Toast.makeText(this@WirelessAdbPinService, message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onInterrupt() {}

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        handler.removeCallbacksAndMessages(null)
        entering = false
        return super.onUnbind(intent)
    }

    companion object {
        private const val TAG = "WirelessAdbPin"
        private const val ENTER_ATTEMPTS = 12
        private const val ENTER_INTERVAL_MS = 500L
    }
}
