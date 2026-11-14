package rikka.shizuku.nightdog

/**
 * Domain-specific state model for NightDog System Service Supervisor.
 * Pure Kotlin, zero Android framework dependencies for deterministic JVM testing.
 */

/**
 * Supervisor lifecycle states.
 */
enum class NightDogSupervisorState {
    STOPPED,
    STARTING,
    SUPERVISING,
    DEGRADED,
    RESTART_REQUESTED,
    STOPPING,
    FAILED
}

/**
 * Lifecycle states for each monitored Android system service.
 */
enum class NightDogServiceState {
    UNBOUND,
    LINKED,
    DEATH_DETECTED,
    REBIND_PENDING,
    REBINDING,
    RECOVERED,
    UNRECOVERABLE
}

/**
 * Immutable snapshot of a single monitored system service.
 */
data class MonitoredServiceSnapshot(
    val name: String,
    val isCritical: Boolean = true,
    val state: NightDogServiceState = NightDogServiceState.UNBOUND,
    val rebindAttempts: Int = 0,
    val maxRebindAttempts: Int = 10,
    val lastStateChangeMonotonicMs: Long = 0L,
    val nextRetryDelayMs: Long = 0L,
    val rebindPendingSinceMs: Long = 0L
) {
    val isBound: Boolean
        get() = state == NightDogServiceState.LINKED || state == NightDogServiceState.RECOVERED

    val isRebinding: Boolean
        get() = state == NightDogServiceState.REBIND_PENDING || state == NightDogServiceState.REBINDING
}

/**
 * Immutable snapshot of the entire NightDog supervisor and all monitored system services.
 */
data class NightDogSnapshot(
    val supervisorState: NightDogSupervisorState = NightDogSupervisorState.STOPPED,
    val generation: Long = 0L,
    val timeoutMs: Long = 60_000L,
    val pollIntervalMs: Long = 60_000L,
    val lastBeatMonotonicMs: Long = 0L,
    val beatCount: Long = 0L,
    val totalRecoveries: Int = 0,
    val services: Map<String, MonitoredServiceSnapshot> = emptyMap(),
    val fatalReason: String? = null,
    val startedAtMonotonicMs: Long = 0L
) {
    val isSupervising: Boolean
        get() = supervisorState == NightDogSupervisorState.SUPERVISING ||
                supervisorState == NightDogSupervisorState.DEGRADED

    val isDegraded: Boolean
        get() = supervisorState == NightDogSupervisorState.DEGRADED

    val isRunning: Boolean
        get() = supervisorState != NightDogSupervisorState.STOPPED &&
                supervisorState != NightDogSupervisorState.FAILED

    val boundCount: Int
        get() = services.values.count { it.isBound }

    val hasPendingRebinds: Boolean
        get() = services.values.any { it.isRebinding }
}

/**
 * Typed events driving transitions within [NightDogStateMachine].
 */
sealed interface NightDogEvent {
    data class StartRequested(
        val generation: Long,
        val timeoutMs: Long = 60_000L,
        val pollIntervalMs: Long = 60_000L,
        val criticalServices: List<String> = listOf("package", "activity", "user", "appops"),
        val optionalServices: List<String> = listOf("permissionmgr"),
        val currentTimeMs: Long = 0L
    ) : NightDogEvent

    data class Started(val generation: Long) : NightDogEvent

    data class HeartbeatReceived(val currentTimeMs: Long) : NightDogEvent

    data class HealthCheckTick(val currentTimeMs: Long) : NightDogEvent

    data class ServiceLinked(
        val generation: Long,
        val name: String,
        val currentTimeMs: Long
    ) : NightDogEvent

    data class ServiceLinkFailed(
        val generation: Long,
        val name: String,
        val currentTimeMs: Long
    ) : NightDogEvent

    data class ServiceDeathDetected(
        val generation: Long,
        val name: String,
        val currentTimeMs: Long
    ) : NightDogEvent

    data class RebindScheduled(
        val generation: Long,
        val name: String,
        val delayMs: Long,
        val currentTimeMs: Long
    ) : NightDogEvent

    data class RebindStarted(
        val generation: Long,
        val name: String,
        val currentTimeMs: Long
    ) : NightDogEvent

    data class ServiceRecovered(
        val generation: Long,
        val name: String,
        val currentTimeMs: Long
    ) : NightDogEvent

    data class RebindFailed(
        val generation: Long,
        val name: String,
        val nextDelayMs: Long,
        val currentTimeMs: Long
    ) : NightDogEvent

    data class ServiceUnrecoverable(
        val generation: Long,
        val name: String,
        val currentTimeMs: Long
    ) : NightDogEvent

    data class FatalFaultDetected(
        val generation: Long,
        val reason: String,
        val currentTimeMs: Long
    ) : NightDogEvent

    data class StopRequested(val generation: Long) : NightDogEvent

    data object Stopped : NightDogEvent

    data object Reset : NightDogEvent
}

/**
 * Pure state reducer for NightDog supervisor state machine.
 */
object NightDogStateMachine {

    const val DEFAULT_TIMEOUT_MS = 60_000L
    const val DEFAULT_POLL_INTERVAL_MS = 60_000L
    const val INITIAL_REBIND_DELAY_MS = 2_000L
    const val BACKOFF_BASE_MS = 5_000L
    const val MAX_BACKOFF_MS = 60_000L
    const val MAX_REBIND_ATTEMPTS = 10

    fun calculateBackoffDelay(attempts: Int): Long {
        val shift = (attempts - 1).coerceIn(0, 4)
        return minOf(BACKOFF_BASE_MS * (1L shl shift), MAX_BACKOFF_MS)
    }

    fun reduce(snapshot: NightDogSnapshot, event: NightDogEvent): NightDogSnapshot {
        return when (event) {
            is NightDogEvent.Reset -> NightDogSnapshot()

            is NightDogEvent.Stopped -> {
                snapshot.copy(
                    supervisorState = NightDogSupervisorState.STOPPED,
                    services = emptyMap(),
                    lastBeatMonotonicMs = 0L,
                    fatalReason = null
                )
            }

            is NightDogEvent.StopRequested -> {
                if (event.generation != snapshot.generation && snapshot.supervisorState != NightDogSupervisorState.STOPPED) {
                    snapshot
                } else {
                    snapshot.copy(
                        supervisorState = NightDogSupervisorState.STOPPING
                    )
                }
            }

            is NightDogEvent.StartRequested -> {
                if (event.generation <= snapshot.generation && snapshot.isRunning) {
                    // Reject older or duplicate generation if already running
                    snapshot
                } else {
                    val serviceMap = mutableMapOf<String, MonitoredServiceSnapshot>()
                    for (name in event.criticalServices) {
                        serviceMap[name] = MonitoredServiceSnapshot(
                            name = name,
                            isCritical = true,
                            state = NightDogServiceState.UNBOUND,
                            rebindAttempts = 0,
                            lastStateChangeMonotonicMs = event.currentTimeMs
                        )
                    }
                    for (name in event.optionalServices) {
                        serviceMap[name] = MonitoredServiceSnapshot(
                            name = name,
                            isCritical = false,
                            state = NightDogServiceState.UNBOUND,
                            rebindAttempts = 0,
                            lastStateChangeMonotonicMs = event.currentTimeMs
                        )
                    }

                    NightDogSnapshot(
                        supervisorState = NightDogSupervisorState.STARTING,
                        generation = event.generation,
                        timeoutMs = event.timeoutMs.coerceAtLeast(5_000L),
                        pollIntervalMs = event.pollIntervalMs.coerceAtLeast(5_000L),
                        lastBeatMonotonicMs = event.currentTimeMs,
                        beatCount = 0L,
                        totalRecoveries = 0,
                        services = serviceMap,
                        fatalReason = null,
                        startedAtMonotonicMs = event.currentTimeMs
                    )
                }
            }

            is NightDogEvent.Started -> {
                if (event.generation != snapshot.generation || snapshot.supervisorState != NightDogSupervisorState.STARTING) {
                    snapshot
                } else {
                    val nextState = computeSupervisorHealthState(snapshot.services)
                    snapshot.copy(supervisorState = nextState)
                }
            }

            is NightDogEvent.HeartbeatReceived -> {
                if (!snapshot.isSupervising && snapshot.supervisorState != NightDogSupervisorState.STARTING) {
                    snapshot
                } else {
                    snapshot.copy(
                        lastBeatMonotonicMs = event.currentTimeMs,
                        beatCount = snapshot.beatCount + 1L
                    )
                }
            }

            is NightDogEvent.HealthCheckTick -> {
                if (!snapshot.isSupervising) {
                    snapshot
                } else if (snapshot.lastBeatMonotonicMs > 0L &&
                    (event.currentTimeMs - snapshot.lastBeatMonotonicMs) >= snapshot.timeoutMs
                ) {
                    val overdueMs = event.currentTimeMs - snapshot.lastBeatMonotonicMs
                    snapshot.copy(
                        supervisorState = NightDogSupervisorState.RESTART_REQUESTED,
                        fatalReason = "heartbeat_timeout:${overdueMs}ms"
                    )
                } else {
                    snapshot
                }
            }

            is NightDogEvent.ServiceLinked -> {
                if (event.generation != snapshot.generation || !snapshot.isRunning) {
                    snapshot
                } else {
                    val svc = snapshot.services[event.name] ?: return snapshot
                    val updatedSvc = svc.copy(
                        state = NightDogServiceState.LINKED,
                        rebindAttempts = 0,
                        lastStateChangeMonotonicMs = event.currentTimeMs,
                        nextRetryDelayMs = 0L,
                        rebindPendingSinceMs = 0L
                    )
                    val updatedMap = snapshot.services + (event.name to updatedSvc)
                    val nextState = if (snapshot.supervisorState == NightDogSupervisorState.RESTART_REQUESTED) {
                        NightDogSupervisorState.RESTART_REQUESTED
                    } else {
                        computeSupervisorHealthState(updatedMap)
                    }
                    snapshot.copy(services = updatedMap, supervisorState = nextState)
                }
            }

            is NightDogEvent.ServiceLinkFailed -> {
                if (event.generation != snapshot.generation || !snapshot.isRunning) {
                    snapshot
                } else {
                    val svc = snapshot.services[event.name] ?: return snapshot
                    val updatedSvc = svc.copy(
                        state = NightDogServiceState.DEATH_DETECTED,
                        lastStateChangeMonotonicMs = event.currentTimeMs
                    )
                    val updatedMap = snapshot.services + (event.name to updatedSvc)
                    val nextState = computeSupervisorHealthState(updatedMap)
                    snapshot.copy(services = updatedMap, supervisorState = nextState)
                }
            }

            is NightDogEvent.ServiceDeathDetected -> {
                if (event.generation != snapshot.generation || !snapshot.isRunning) {
                    snapshot
                } else {
                    val svc = snapshot.services[event.name] ?: return snapshot
                    val updatedSvc = svc.copy(
                        state = NightDogServiceState.DEATH_DETECTED,
                        lastStateChangeMonotonicMs = event.currentTimeMs
                    )
                    val updatedMap = snapshot.services + (event.name to updatedSvc)
                    val nextState = computeSupervisorHealthState(updatedMap)
                    snapshot.copy(services = updatedMap, supervisorState = nextState)
                }
            }

            is NightDogEvent.RebindScheduled -> {
                if (event.generation != snapshot.generation || !snapshot.isRunning) {
                    snapshot
                } else {
                    val svc = snapshot.services[event.name] ?: return snapshot
                    val updatedSvc = svc.copy(
                        state = NightDogServiceState.REBIND_PENDING,
                        nextRetryDelayMs = event.delayMs,
                        rebindPendingSinceMs = event.currentTimeMs,
                        lastStateChangeMonotonicMs = event.currentTimeMs
                    )
                    val updatedMap = snapshot.services + (event.name to updatedSvc)
                    val nextState = computeSupervisorHealthState(updatedMap)
                    snapshot.copy(services = updatedMap, supervisorState = nextState)
                }
            }

            is NightDogEvent.RebindStarted -> {
                if (event.generation != snapshot.generation || !snapshot.isRunning) {
                    snapshot
                } else {
                    val svc = snapshot.services[event.name] ?: return snapshot
                    val updatedSvc = svc.copy(
                        state = NightDogServiceState.REBINDING,
                        lastStateChangeMonotonicMs = event.currentTimeMs
                    )
                    val updatedMap = snapshot.services + (event.name to updatedSvc)
                    snapshot.copy(services = updatedMap)
                }
            }

            is NightDogEvent.ServiceRecovered -> {
                if (event.generation != snapshot.generation || !snapshot.isRunning) {
                    snapshot
                } else {
                    val svc = snapshot.services[event.name] ?: return snapshot
                    val updatedSvc = svc.copy(
                        state = NightDogServiceState.RECOVERED,
                        rebindAttempts = 0,
                        lastStateChangeMonotonicMs = event.currentTimeMs,
                        nextRetryDelayMs = 0L,
                        rebindPendingSinceMs = 0L
                    )
                    val updatedMap = snapshot.services + (event.name to updatedSvc)
                    val nextState = computeSupervisorHealthState(updatedMap)
                    snapshot.copy(
                        services = updatedMap,
                        supervisorState = nextState,
                        totalRecoveries = snapshot.totalRecoveries + 1
                    )
                }
            }

            is NightDogEvent.RebindFailed -> {
                if (event.generation != snapshot.generation || !snapshot.isRunning) {
                    snapshot
                } else {
                    val svc = snapshot.services[event.name] ?: return snapshot
                    val nextAttempts = svc.rebindAttempts + 1
                    val updatedSvc = svc.copy(
                        state = NightDogServiceState.REBIND_PENDING,
                        rebindAttempts = nextAttempts,
                        nextRetryDelayMs = event.nextDelayMs,
                        rebindPendingSinceMs = event.currentTimeMs,
                        lastStateChangeMonotonicMs = event.currentTimeMs
                    )
                    val updatedMap = snapshot.services + (event.name to updatedSvc)
                    val nextState = computeSupervisorHealthState(updatedMap)
                    snapshot.copy(services = updatedMap, supervisorState = nextState)
                }
            }

            is NightDogEvent.ServiceUnrecoverable -> {
                if (event.generation != snapshot.generation || !snapshot.isRunning) {
                    snapshot
                } else {
                    val svc = snapshot.services[event.name] ?: return snapshot
                    val updatedSvc = svc.copy(
                        state = NightDogServiceState.UNRECOVERABLE,
                        lastStateChangeMonotonicMs = event.currentTimeMs
                    )
                    val updatedMap = snapshot.services + (event.name to updatedSvc)
                    if (svc.isCritical) {
                        snapshot.copy(
                            services = updatedMap,
                            supervisorState = NightDogSupervisorState.RESTART_REQUESTED,
                            fatalReason = "critical_service_unrecoverable:${event.name}"
                        )
                    } else {
                        snapshot.copy(
                            services = updatedMap,
                            supervisorState = NightDogSupervisorState.DEGRADED
                        )
                    }
                }
            }

            is NightDogEvent.FatalFaultDetected -> {
                if (event.generation != snapshot.generation && snapshot.supervisorState != NightDogSupervisorState.STOPPED) {
                    snapshot
                } else {
                    snapshot.copy(
                        supervisorState = NightDogSupervisorState.RESTART_REQUESTED,
                        fatalReason = event.reason
                    )
                }
            }
        }
    }

    private fun computeSupervisorHealthState(services: Map<String, MonitoredServiceSnapshot>): NightDogSupervisorState {
        var hasDegraded = false
        for (svc in services.values) {
            if (svc.isCritical) {
                if (svc.state == NightDogServiceState.UNRECOVERABLE) {
                    return NightDogSupervisorState.RESTART_REQUESTED
                }
                if (svc.state != NightDogServiceState.LINKED && svc.state != NightDogServiceState.RECOVERED) {
                    hasDegraded = true
                }
            } else {
                if (svc.state == NightDogServiceState.UNRECOVERABLE) {
                    hasDegraded = true
                }
            }
        }
        return if (hasDegraded) NightDogSupervisorState.DEGRADED else NightDogSupervisorState.SUPERVISING
    }
}
