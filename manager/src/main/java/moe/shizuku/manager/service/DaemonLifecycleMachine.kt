package moe.shizuku.manager.service

/**
 * Authoritative lifecycle state machine for the Shizuku Daemon, Native Process Supervisor,
 * Binder IPC availability, and Watchdog recovery.
 * Pure Kotlin, zero Android dependencies for deterministic verification.
 */
enum class DaemonState {
    UNINITIALIZED,
    CHECKING_EXISTING,
    STARTING,
    WAITING_FOR_BINDER,
    RUNNING,
    HEARTBEAT_UNRESPONSIVE,
    STOP_REQUESTED,
    FLUSHING_CONFIGURATION,
    STOPPING,
    STOPPED,
    RESTART_PENDING,
    RESTARTING,
    BACKOFF,
    CIRCUIT_BREAKER_OPEN,
    FAILED
}

enum class SupervisorState {
    NONE,
    SPAWNING,
    SUPERVISING,
    EXITED_CLEANLY,
    CRASH_LIMIT_EXCEEDED,
    TERMINATED
}

enum class DaemonProcessState {
    NOT_RUNNING,
    STARTING,
    RUNNING,
    STOPPING,
    EXITED
}

enum class BinderState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DEAD
}

enum class CoordinatorState {
    IDLE,
    MONITORING,
    SUPERVISOR_GRACE_PERIOD,
    RESTARTING,
    BACKOFF,
    CIRCUIT_BREAKER_OPEN,
    USER_STOPPED
}

data class DaemonLifecycleSnapshot(
    val state: DaemonState = DaemonState.UNINITIALIZED,
    val supervisorState: SupervisorState = SupervisorState.NONE,
    val daemonState: DaemonProcessState = DaemonProcessState.NOT_RUNNING,
    val binderState: BinderState = BinderState.DISCONNECTED,
    val coordinatorState: CoordinatorState = CoordinatorState.IDLE,
    val generation: Long = 0L,
    val supervisorPid: Int? = null,
    val daemonPid: Int? = null,
    val restartCount: Int = 0,
    val consecutiveFailures: Int = 0,
    val isUserStop: Boolean = false,
    val lastExitCode: Int? = null,
    val lastError: String? = null
) {
    val isRunning: Boolean
        get() = state == DaemonState.RUNNING && binderState == BinderState.CONNECTED

    val isAwaitingRecovery: Boolean
        get() = state == DaemonState.RESTART_PENDING ||
                coordinatorState == CoordinatorState.SUPERVISOR_GRACE_PERIOD ||
                coordinatorState == CoordinatorState.RESTARTING
}

sealed interface DaemonLifecycleEvent {
    data object Initialize : DaemonLifecycleEvent
    data class SupervisorSpawned(val pid: Int) : DaemonLifecycleEvent
    data class DaemonStarted(val pid: Int) : DaemonLifecycleEvent
    data object BinderConnected : DaemonLifecycleEvent
    data object HeartbeatReceived : DaemonLifecycleEvent
    data object HeartbeatMissed : DaemonLifecycleEvent
    data class BinderDied(val exitCode: Int? = null) : DaemonLifecycleEvent
    data object SupervisorGraceExpired : DaemonLifecycleEvent
    data object SupervisorRecovered : DaemonLifecycleEvent
    data object RestartInitiated : DaemonLifecycleEvent
    data object RestartSucceeded : DaemonLifecycleEvent
    data class RestartFailed(val reason: String) : DaemonLifecycleEvent
    data object UserStopRequested : DaemonLifecycleEvent
    data object ConfigFlushed : DaemonLifecycleEvent
    data class DaemonTerminated(val exitCode: Int) : DaemonLifecycleEvent
    data object CircuitBreakerTripped : DaemonLifecycleEvent
    data object Reset : DaemonLifecycleEvent
}

object DaemonLifecycleMachine {

    const val MAX_CONSECUTIVE_FAILURES = 5

    fun reduce(snapshot: DaemonLifecycleSnapshot, event: DaemonLifecycleEvent): DaemonLifecycleSnapshot {
        return when (event) {
            DaemonLifecycleEvent.Initialize -> {
                snapshot.copy(
                    state = DaemonState.CHECKING_EXISTING,
                    coordinatorState = CoordinatorState.IDLE,
                    isUserStop = false,
                    lastError = null
                )
            }

            is DaemonLifecycleEvent.SupervisorSpawned -> {
                snapshot.copy(
                    state = DaemonState.STARTING,
                    supervisorState = SupervisorState.SUPERVISING,
                    supervisorPid = event.pid,
                    generation = snapshot.generation + 1
                )
            }

            is DaemonLifecycleEvent.DaemonStarted -> {
                snapshot.copy(
                    state = DaemonState.WAITING_FOR_BINDER,
                    daemonState = DaemonProcessState.RUNNING,
                    daemonPid = event.pid,
                    binderState = BinderState.CONNECTING
                )
            }

            DaemonLifecycleEvent.BinderConnected -> {
                snapshot.copy(
                    state = DaemonState.RUNNING,
                    daemonState = DaemonProcessState.RUNNING,
                    binderState = BinderState.CONNECTED,
                    coordinatorState = CoordinatorState.MONITORING,
                    consecutiveFailures = 0,
                    isUserStop = false,
                    lastError = null
                )
            }

            DaemonLifecycleEvent.HeartbeatReceived -> when (snapshot.state) {
                DaemonState.HEARTBEAT_UNRESPONSIVE -> snapshot.copy(state = DaemonState.RUNNING)
                else -> snapshot
            }

            DaemonLifecycleEvent.HeartbeatMissed -> when (snapshot.state) {
                DaemonState.RUNNING -> snapshot.copy(state = DaemonState.HEARTBEAT_UNRESPONSIVE)
                else -> snapshot
            }

            is DaemonLifecycleEvent.BinderDied -> {
                if (snapshot.isUserStop || snapshot.state == DaemonState.STOP_REQUESTED || snapshot.state == DaemonState.STOPPING) {
                    snapshot.copy(
                        state = DaemonState.STOPPED,
                        daemonState = DaemonProcessState.NOT_RUNNING,
                        binderState = BinderState.DEAD,
                        coordinatorState = CoordinatorState.USER_STOPPED,
                        lastExitCode = event.exitCode
                    )
                } else if (snapshot.state == DaemonState.CIRCUIT_BREAKER_OPEN) {
                    snapshot.copy(
                        binderState = BinderState.DEAD,
                        daemonState = DaemonProcessState.NOT_RUNNING
                    )
                } else {
                    val failures = snapshot.consecutiveFailures + 1
                    if (failures > MAX_CONSECUTIVE_FAILURES) {
                        snapshot.copy(
                            state = DaemonState.CIRCUIT_BREAKER_OPEN,
                            daemonState = DaemonProcessState.NOT_RUNNING,
                            binderState = BinderState.DEAD,
                            coordinatorState = CoordinatorState.CIRCUIT_BREAKER_OPEN,
                            consecutiveFailures = failures,
                            lastExitCode = event.exitCode,
                            lastError = "Exceeded max consecutive failures ($failures)"
                        )
                    } else {
                        snapshot.copy(
                            state = DaemonState.RESTART_PENDING,
                            daemonState = DaemonProcessState.NOT_RUNNING,
                            binderState = BinderState.DEAD,
                            coordinatorState = CoordinatorState.SUPERVISOR_GRACE_PERIOD,
                            consecutiveFailures = failures,
                            lastExitCode = event.exitCode
                        )
                    }
                }
            }

            DaemonLifecycleEvent.SupervisorGraceExpired -> when (snapshot.coordinatorState) {
                CoordinatorState.SUPERVISOR_GRACE_PERIOD -> snapshot.copy(
                    state = DaemonState.RESTARTING,
                    coordinatorState = CoordinatorState.RESTARTING,
                    generation = snapshot.generation + 1
                )
                else -> snapshot
            }

            DaemonLifecycleEvent.SupervisorRecovered -> {
                snapshot.copy(
                    state = DaemonState.WAITING_FOR_BINDER,
                    daemonState = DaemonProcessState.RUNNING,
                    binderState = BinderState.CONNECTING,
                    coordinatorState = CoordinatorState.MONITORING
                )
            }

            DaemonLifecycleEvent.RestartInitiated -> {
                snapshot.copy(
                    state = DaemonState.RESTARTING,
                    coordinatorState = CoordinatorState.RESTARTING,
                    restartCount = snapshot.restartCount + 1,
                    generation = snapshot.generation + 1
                )
            }

            DaemonLifecycleEvent.RestartSucceeded -> {
                snapshot.copy(
                    state = DaemonState.WAITING_FOR_BINDER,
                    coordinatorState = CoordinatorState.MONITORING,
                    consecutiveFailures = 0,
                    lastError = null
                )
            }

            is DaemonLifecycleEvent.RestartFailed -> {
                val failures = snapshot.consecutiveFailures + 1
                if (failures > MAX_CONSECUTIVE_FAILURES) {
                    snapshot.copy(
                        state = DaemonState.CIRCUIT_BREAKER_OPEN,
                        coordinatorState = CoordinatorState.CIRCUIT_BREAKER_OPEN,
                        consecutiveFailures = failures,
                        lastError = event.reason
                    )
                } else {
                    snapshot.copy(
                        state = DaemonState.BACKOFF,
                        coordinatorState = CoordinatorState.BACKOFF,
                        consecutiveFailures = failures,
                        lastError = event.reason
                    )
                }
            }

            DaemonLifecycleEvent.UserStopRequested -> {
                snapshot.copy(
                    state = DaemonState.STOP_REQUESTED,
                    coordinatorState = CoordinatorState.USER_STOPPED,
                    isUserStop = true,
                    lastError = null
                )
            }

            DaemonLifecycleEvent.ConfigFlushed -> when (snapshot.state) {
                DaemonState.STOP_REQUESTED -> snapshot.copy(state = DaemonState.FLUSHING_CONFIGURATION)
                else -> snapshot
            }

            is DaemonLifecycleEvent.DaemonTerminated -> {
                val clean = event.exitCode == 0 || event.exitCode == 50
                snapshot.copy(
                    state = if (clean) DaemonState.STOPPED else DaemonState.FAILED,
                    daemonState = DaemonProcessState.EXITED,
                    binderState = BinderState.DEAD,
                    lastExitCode = event.exitCode,
                    supervisorState = if (clean) SupervisorState.EXITED_CLEANLY else SupervisorState.SUPERVISING
                )
            }

            DaemonLifecycleEvent.CircuitBreakerTripped -> {
                snapshot.copy(
                    state = DaemonState.CIRCUIT_BREAKER_OPEN,
                    coordinatorState = CoordinatorState.CIRCUIT_BREAKER_OPEN,
                    lastError = "Circuit breaker opened manually or due to high error rate"
                )
            }

            DaemonLifecycleEvent.Reset -> {
                DaemonLifecycleSnapshot(generation = snapshot.generation + 1)
            }
        }
    }
}
