package moe.shizuku.manager.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.SystemClock
import androidx.core.content.ContextCompat
import java.util.concurrent.atomic.AtomicLong

object UsbConnectionMonitor {

    const val ACTION_USB_STATE = UsbConnectionReducer.ACTION_USB_STATE

    fun interface Callback { fun onChanged(state: UsbConnectionState) }
    fun interface Clock { fun elapsedRealtime(): Long }

    private val defaultClock = object : Clock {
        override fun elapsedRealtime(): Long = runCatching { SystemClock.elapsedRealtime() }.getOrDefault(System.currentTimeMillis())
    }

    @Volatile var clock: Clock = defaultClock

    private val lastChange = AtomicLong(0L)
    @Volatile private var state = UsbConnectionState()
    @Volatile private var callback: Callback? = null
    @Volatile private var receiver: BroadcastReceiver? = null
    const val DEBOUNCE_MS = 600L

    fun current(): UsbConnectionState = state

    @Synchronized
    fun register(context: Context, cb: Callback) {
        callback = cb
        if (receiver != null) {
            cb.onChanged(state)
            return
        }
        val r = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                handleIntent(intent)
            }
        }
        receiver = r
        val filter = IntentFilter().apply {
            addAction(ACTION_USB_STATE)
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }

        if (Build.VERSION.SDK_INT >= 33) {
            ContextCompat.registerReceiver(context, r, filter, ContextCompat.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(r, filter)
        }

        val sticky = runCatching {
            context.registerReceiver(null, IntentFilter(ACTION_USB_STATE))
        }.getOrNull()
        if (sticky != null) {
            handleIntent(sticky, force = true)
        } else {
            cb.onChanged(state)
        }
    }

    @Synchronized
    fun unregister(context: Context) {
        val r = receiver ?: run {
            callback = null
            return
        }
        receiver = null
        callback = null
        runCatching { context.unregisterReceiver(r) }
    }

    fun handleIntent(intent: Intent, force: Boolean = false) {
        val now = clock.elapsedRealtime()
        if (!force && now - lastChange.get() < DEBOUNCE_MS) return
        val prev = state
        val extras = mutableMapOf<String, Any?>()
        intent.extras?.keySet()?.forEach { key -> extras[key] = intent.extras?.get(key) }
        val next = UsbConnectionReducer.reduce(prev, intent.action, extras)
        if (next != prev || force) {
            lastChange.set(now)
            state = next.copy(lastChangedElapsed = now)
            callback?.onChanged(state)
        }
    }

    fun resetForTests() {
        state = UsbConnectionState()
        lastChange.set(0)
        callback = null
        receiver = null
        clock = defaultClock
    }
}
