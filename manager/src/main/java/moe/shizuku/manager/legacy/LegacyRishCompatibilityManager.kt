package moe.shizuku.manager.legacy

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.utils.ShizukuStateMachine
import rikka.shizuku.Shizuku

object LegacyRishCompatibilityManager {
    interface ServiceController {
        fun ensureStarted(context: Context)
        fun requestStop(context: Context)
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val listeners = CopyOnWriteArrayList<(LegacyRishSnapshot) -> Unit>()
    private val started = AtomicBoolean(false)

    @Volatile private var snapshot: LegacyRishSnapshot = LegacyRishSnapshot()
    @Volatile private var serviceController: ServiceController = DefaultServiceController

    private val shizukuListener: (ShizukuStateMachine.State) -> Unit = {
        dispatch(LegacyRishEvent.ServerStateChanged(currentServerRunning(), currentBinderAvailable()))
    }
    private val binderReceived = Shizuku.OnBinderReceivedListener {
        dispatch(LegacyRishEvent.ServerStateChanged(currentServerRunning(), true))
    }
    private val binderDead = Shizuku.OnBinderDeadListener {
        dispatch(LegacyRishEvent.BinderDied(snapshot.generation))
        dispatch(LegacyRishEvent.ServerStateChanged(currentServerRunning(), false))
    }

    @Synchronized
    fun init(context: Context) {
        if (started.getAndSet(true)) {
            dispatch(LegacyRishEvent.ServerStateChanged(currentServerRunning(), currentBinderAvailable()))
            reconcile(context.applicationContext)
            return
        }
        snapshot = LegacyRishCompatibilityStateMachine.initial(ShizukuSettings.getLegacyRishCompatibility())
        ShizukuStateMachine.addListener(shizukuListener)
        Shizuku.addBinderReceivedListenerSticky(binderReceived)
        Shizuku.addBinderDeadListener(binderDead)
        dispatch(LegacyRishEvent.ServerStateChanged(currentServerRunning(), currentBinderAvailable()))
        reconcile(context.applicationContext)
        notifyListeners()
    }

    fun setServiceController(controller: ServiceController) { serviceController = controller }
    fun resetForTests() {
        serviceController = DefaultServiceController
        started.set(false)
        listeners.clear()
        snapshot = LegacyRishSnapshot()
    }
    fun getSnapshot(): LegacyRishSnapshot = snapshot

    fun addListener(listener: (LegacyRishSnapshot) -> Unit) {
        listeners.add(listener)
        listener(snapshot)
    }

    fun removeListener(listener: (LegacyRishSnapshot) -> Unit) { listeners.remove(listener) }

    fun setEnabled(context: Context, enabled: Boolean) {
        val app = context.applicationContext
        ShizukuSettings.setLegacyRishCompatibility(enabled)
        dispatch(LegacyRishEvent.PreferenceChanged(enabled))
        reconcile(app)
    }

    fun refresh(context: Context) {
        val app = context.applicationContext
        dispatch(LegacyRishEvent.Refresh)
        dispatch(LegacyRishEvent.ServerStateChanged(currentServerRunning(), currentBinderAvailable()))
        reconcile(app)
    }

    fun onServiceStarted(context: Context, generation: Long) {
        dispatch(LegacyRishEvent.ServiceStarted(generation))
        reconcile(context.applicationContext)
    }

    fun onServiceStopped(context: Context, generation: Long) {
        dispatch(LegacyRishEvent.ServiceStopped(generation))
        reconcile(context.applicationContext)
    }

    fun onServiceError(context: Context, message: String, generation: Long) {
        dispatch(LegacyRishEvent.Error(message, generation))
        reconcile(context.applicationContext)
    }

    private fun dispatch(event: LegacyRishEvent) {
        val next = synchronized(this) {
            val reduced = LegacyRishCompatibilityStateMachine.reduce(snapshot, event)
            if (reduced == snapshot) null else {
                snapshot = reduced
                reduced
            }
        } ?: return
        notifyListeners(next)
    }

    private fun reconcile(context: Context) {
        val s = snapshot
        val ready = s.serverRunning && s.binderAvailable
        val shouldRunService = s.enabled && ready && s.state != LegacyRishCompatibilityState.STOPPING
        val shouldStopService = !s.enabled || s.state == LegacyRishCompatibilityState.STOPPING || !ready
        when {
            shouldRunService -> serviceController.ensureStarted(context)
            shouldStopService -> serviceController.requestStop(context)
            else -> Unit
        }
    }

    private fun notifyListeners(current: LegacyRishSnapshot = snapshot) {
        val list = listeners.toList()
        if (Looper.myLooper() == Looper.getMainLooper()) {
            list.forEach { runCatching { it(current) } }
        } else {
            mainHandler.post { list.forEach { runCatching { it(current) } } }
        }
    }

    private fun currentServerRunning(): Boolean =
        runCatching { ShizukuStateMachine.get() == ShizukuStateMachine.State.RUNNING || Shizuku.pingBinder() }.getOrDefault(false)

    private fun currentBinderAvailable(): Boolean =
        runCatching { Shizuku.pingBinder() }.getOrDefault(false)

    private object DefaultServiceController : ServiceController {
        override fun ensureStarted(context: Context) {
            runCatching {
                ContextCompat.startForegroundService(context, LegacyRishCompatibilityService.intent(context))
            }.onFailure {
                onServiceError(context, it.message ?: "Failed to start compatibility service", snapshot.generation)
            }
        }
        override fun requestStop(context: Context) {
            runCatching { context.stopService(LegacyRishCompatibilityService.intent(context)) }
        }
    }
}
