package moe.shizuku.manager.notifications

import android.app.Application
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import java.util.concurrent.atomic.AtomicBoolean

/**
 * App-level lifecycle/reconciliation for notification infrastructure.
 * Uses ProcessLifecycleOwner for foreground/background and avoids Activity references.
 */
object NotificationLifecycleOwner : DefaultLifecycleObserver {
    private val initialized = AtomicBoolean(false)
    @Volatile private var overlayController: OverlayMessageController? = null

    fun init(app: Application) {
        val appCtx = app.applicationContext
        migrateIfNeeded(appCtx)
        if (!initialized.compareAndSet(false, true)) {
            reconcile(appCtx)
            return
        }
        val oc = OverlayMessageController(appCtx)
        overlayController = oc
        HelperMessageDispatcher.overlayController = oc
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        HelperMessageDispatcher.clock = object : HelperMessageDispatcher.Clock {
            override fun elapsedRealtime(): Long = android.os.SystemClock.elapsedRealtime()
        }
        HelperMessageDispatcher.scheduler = object : HelperMessageDispatcher.Scheduler {
            override fun postDelayed(delayMs: Long, action: () -> Unit) {
                handler.postDelayed(action, delayMs)
            }
        }
        runCatching { ProcessLifecycleOwner.get().lifecycle.addObserver(this) }
        reconcile(appCtx)
    }

    fun migrateIfNeeded(context: Context) {
        runCatching {
            val app = context.applicationContext
            val nm = app.getSystemService(android.app.NotificationManager::class.java)
            val prefs = app.getSharedPreferences(moe.shizuku.manager.ShizukuSettings.NAME, Context.MODE_PRIVATE)

            val canceller = object : NotificationMigration.NotificationCanceller {
                override fun cancel(id: Int) {
                    nm?.cancel(id)
                }
            }

            val storage = object : NotificationMigration.MigrationStorage {
                override fun getVersion(): Int = prefs.getInt(NotificationMigration.PREF_KEY_MIGRATION_VERSION, 0)
                override fun setVersion(version: Int) {
                    prefs.edit().putInt(NotificationMigration.PREF_KEY_MIGRATION_VERSION, version).apply()
                }
            }

            NotificationMigration.migrate(canceller, storage)
        }
    }

    fun reconcile(context: Context) {
        val app = context.applicationContext
        reconcileUsb(app)
        runCatching { moe.shizuku.manager.service.WatchdogService.requestNotificationRefresh() }
    }

    private fun reconcileUsb(app: Context) {
        if (NotificationPreferences.usbConnectionEnabled()) {
            UsbConnectionMonitor.register(app) { state ->
                UsbConnectionNotificationManager.update(app, state)
            }
            UsbConnectionNotificationManager.update(app, UsbConnectionMonitor.current())
        } else {
            UsbConnectionMonitor.unregister(app)
            UsbConnectionNotificationManager.cancel(app)
        }
    }

    fun onUsbPreferenceChanged(context: Context, enabled: Boolean) {
        val app = context.applicationContext
        if (enabled) reconcileUsb(app) else {
            UsbConnectionMonitor.unregister(app)
            UsbConnectionNotificationManager.cancel(app)
        }
    }

    fun onOverlayPermissionOrPreferenceChanged() {
        val oc = overlayController ?: return
        if (!oc.canShow()) {
            oc.removeAll()
            HelperMessageDispatcher.onOverlayUnavailable()
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        HelperMessageDispatcher.setAppForeground(true)
    }

    override fun onResume(owner: LifecycleOwner) {
        onOverlayPermissionOrPreferenceChanged()
    }

    override fun onStop(owner: LifecycleOwner) {
        HelperMessageDispatcher.setAppForeground(false)
        onOverlayPermissionOrPreferenceChanged()
    }
}
