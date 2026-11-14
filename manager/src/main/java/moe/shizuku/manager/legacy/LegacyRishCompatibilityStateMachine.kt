package moe.shizuku.manager.legacy

enum class LegacyRishCompatibilityState {
    DISABLED, WAITING_FOR_SERVER, STARTING, ACTIVE, RECONNECTING, STOPPING, ERROR
}

data class LegacyRishSnapshot(
    val state: LegacyRishCompatibilityState = LegacyRishCompatibilityState.DISABLED,
    val enabled: Boolean = false,
    val serverRunning: Boolean = false,
    val binderAvailable: Boolean = false,
    val serviceRunning: Boolean = false,
    val notificationActive: Boolean = false,
    val generation: Long = 0L,
    val lastEvent: String = "Initialized",
    val lastError: String? = null
)

sealed interface LegacyRishEvent {
    data class PreferenceChanged(val enabled: Boolean) : LegacyRishEvent
    data class ServerStateChanged(val serverRunning: Boolean, val binderAvailable: Boolean) : LegacyRishEvent
    data class ServiceStarted(val generation: Long) : LegacyRishEvent
    data class ServiceStopped(val generation: Long) : LegacyRishEvent
    data class BinderDied(val generation: Long? = null) : LegacyRishEvent
    data class Error(val message: String, val generation: Long? = null) : LegacyRishEvent
    data object Refresh : LegacyRishEvent
}

object LegacyRishCompatibilityStateMachine {
    fun initial(enabled: Boolean = false): LegacyRishSnapshot =
        LegacyRishSnapshot(
            state = if (enabled) LegacyRishCompatibilityState.WAITING_FOR_SERVER else LegacyRishCompatibilityState.DISABLED,
            enabled = enabled,
            lastEvent = if (enabled) "Enabled, waiting for server" else "Disabled"
        )

    fun reduce(snapshot: LegacyRishSnapshot, event: LegacyRishEvent): LegacyRishSnapshot {
        if (isStale(snapshot, event)) return snapshot
        return when (event) {
            is LegacyRishEvent.PreferenceChanged -> onPreferenceChanged(snapshot, event.enabled)
            is LegacyRishEvent.ServerStateChanged -> onServerStateChanged(snapshot, event.serverRunning, event.binderAvailable)
            is LegacyRishEvent.ServiceStarted -> onServiceStarted(snapshot)
            is LegacyRishEvent.ServiceStopped -> onServiceStopped(snapshot)
            is LegacyRishEvent.BinderDied -> onBinderDied(snapshot)
            is LegacyRishEvent.Error -> onError(snapshot, event.message)
            LegacyRishEvent.Refresh -> reevaluate(snapshot, snapshot.lastEvent)
        }
    }

    private fun onPreferenceChanged(snapshot: LegacyRishSnapshot, enabled: Boolean): LegacyRishSnapshot {
        if (snapshot.enabled == enabled) return reevaluate(snapshot, snapshot.lastEvent)
        val base = snapshot.copy(enabled = enabled, lastError = if (enabled) null else snapshot.lastError)
        return if (enabled) {
            reevaluate(base.copy(generation = snapshot.generation + 1), "Compatibility enabled")
        } else {
            if (base.serviceRunning || base.notificationActive || base.state == LegacyRishCompatibilityState.ACTIVE ||
                base.state == LegacyRishCompatibilityState.STARTING || base.state == LegacyRishCompatibilityState.RECONNECTING) {
                base.copy(state = LegacyRishCompatibilityState.STOPPING, lastEvent = "Compatibility disabled, stopping")
            } else {
                base.copy(state = LegacyRishCompatibilityState.DISABLED, serviceRunning = false, notificationActive = false,
                    lastEvent = "Compatibility disabled", lastError = null)
            }
        }
    }

    private fun onServerStateChanged(snapshot: LegacyRishSnapshot, serverRunning: Boolean, binderAvailable: Boolean): LegacyRishSnapshot {
        val base = snapshot.copy(serverRunning = serverRunning, binderAvailable = binderAvailable)
        if (!base.enabled) return base.copy(state = LegacyRishCompatibilityState.DISABLED, serviceRunning = false, notificationActive = false,
            lastEvent = "Server state updated while disabled")
        val ready = serverRunning && binderAvailable
        return if (ready) when (base.state) {
            LegacyRishCompatibilityState.ACTIVE -> base.copy(lastEvent = "Server/binder available")
            LegacyRishCompatibilityState.RECONNECTING -> base.copy(
                state = if (base.serviceRunning) LegacyRishCompatibilityState.ACTIVE else LegacyRishCompatibilityState.STARTING,
                lastEvent = if (base.serviceRunning) "Reconnected" else "Server available, starting")
            LegacyRishCompatibilityState.ERROR -> base.copy(state = LegacyRishCompatibilityState.STARTING, lastError = null, lastEvent = "Server available, retrying")
            LegacyRishCompatibilityState.STOPPING -> base.copy(state = LegacyRishCompatibilityState.STARTING, lastEvent = "Server available, starting")
            else -> base.copy(state = if (base.serviceRunning) LegacyRishCompatibilityState.ACTIVE else LegacyRishCompatibilityState.STARTING,
                lastEvent = if (base.serviceRunning) "Compatibility active" else "Server available, starting")
        } else when (base.state) {
            LegacyRishCompatibilityState.ACTIVE, LegacyRishCompatibilityState.STARTING, LegacyRishCompatibilityState.RECONNECTING ->
                base.copy(state = LegacyRishCompatibilityState.WAITING_FOR_SERVER,
                    lastEvent = if (!serverRunning) "Server stopped, waiting" else "Binder unavailable, waiting")
            LegacyRishCompatibilityState.STOPPING -> base.copy(lastEvent = "Server/binder unavailable while stopping")
            LegacyRishCompatibilityState.ERROR -> base.copy(state = LegacyRishCompatibilityState.WAITING_FOR_SERVER, lastEvent = "Waiting for server")
            else -> base.copy(state = LegacyRishCompatibilityState.WAITING_FOR_SERVER, lastEvent = "Waiting for server")
        }
    }

    private fun onServiceStarted(snapshot: LegacyRishSnapshot): LegacyRishSnapshot {
        val base = snapshot.copy(serviceRunning = true, notificationActive = true)
        if (!base.enabled) return base.copy(state = LegacyRishCompatibilityState.STOPPING, lastEvent = "Service started while disabled, stopping")
        return if (base.serverRunning && base.binderAvailable) base.copy(state = LegacyRishCompatibilityState.ACTIVE, lastEvent = "Legacy Rish compatibility active")
        else base.copy(state = LegacyRishCompatibilityState.WAITING_FOR_SERVER, lastEvent = "Service started, waiting for server/binder")
    }

    private fun onServiceStopped(snapshot: LegacyRishSnapshot): LegacyRishSnapshot {
        val base = snapshot.copy(serviceRunning = false, notificationActive = false)
        if (!base.enabled) return base.copy(state = LegacyRishCompatibilityState.DISABLED, lastEvent = "Compatibility stopped", lastError = null)
        if (base.state == LegacyRishCompatibilityState.STOPPING) return base.copy(state = LegacyRishCompatibilityState.WAITING_FOR_SERVER, lastEvent = "Compatibility service stopped")
        return if (base.serverRunning && base.binderAvailable) base.copy(state = LegacyRishCompatibilityState.STARTING, lastEvent = "Service stopped, restarting compatibility")
        else base.copy(state = LegacyRishCompatibilityState.WAITING_FOR_SERVER, lastEvent = "Compatibility service stopped, waiting for server")
    }

    private fun onBinderDied(snapshot: LegacyRishSnapshot): LegacyRishSnapshot {
        if (!snapshot.enabled) return snapshot.copy(binderAvailable = false)
        val base = snapshot.copy(binderAvailable = false)
        return when (base.state) {
            LegacyRishCompatibilityState.ACTIVE ->
                if (base.serverRunning) base.copy(state = LegacyRishCompatibilityState.RECONNECTING, lastEvent = "Binder died, reconnecting")
                else base.copy(state = LegacyRishCompatibilityState.WAITING_FOR_SERVER, lastEvent = "Binder died, waiting for server")
            LegacyRishCompatibilityState.STARTING, LegacyRishCompatibilityState.RECONNECTING ->
                base.copy(state = LegacyRishCompatibilityState.WAITING_FOR_SERVER, lastEvent = "Binder died, waiting for server/binder")
            else -> reevaluate(base, "Binder unavailable")
        }
    }

    private fun onError(snapshot: LegacyRishSnapshot, message: String): LegacyRishSnapshot =
        if (!snapshot.enabled) snapshot.copy(lastError = message, lastEvent = "Error while disabled")
        else snapshot.copy(state = LegacyRishCompatibilityState.ERROR, lastError = message, lastEvent = "Compatibility error")

    private fun reevaluate(snapshot: LegacyRishSnapshot, event: String): LegacyRishSnapshot {
        if (!snapshot.enabled) return snapshot.copy(state = LegacyRishCompatibilityState.DISABLED, lastEvent = event)
        val ready = snapshot.serverRunning && snapshot.binderAvailable
        val state = when {
            ready && snapshot.serviceRunning -> LegacyRishCompatibilityState.ACTIVE
            ready -> LegacyRishCompatibilityState.STARTING
            snapshot.state == LegacyRishCompatibilityState.RECONNECTING -> LegacyRishCompatibilityState.WAITING_FOR_SERVER
            snapshot.state == LegacyRishCompatibilityState.ERROR -> LegacyRishCompatibilityState.ERROR
            snapshot.state == LegacyRishCompatibilityState.STOPPING -> LegacyRishCompatibilityState.STOPPING
            else -> LegacyRishCompatibilityState.WAITING_FOR_SERVER
        }
        return snapshot.copy(state = state, lastEvent = event)
    }

    private fun isStale(snapshot: LegacyRishSnapshot, event: LegacyRishEvent): Boolean {
        val eventGen = when (event) {
            is LegacyRishEvent.ServiceStarted -> event.generation
            is LegacyRishEvent.ServiceStopped -> event.generation
            is LegacyRishEvent.BinderDied -> event.generation
            is LegacyRishEvent.Error -> event.generation
            else -> null
        } ?: return false
        return eventGen != snapshot.generation
    }
}
