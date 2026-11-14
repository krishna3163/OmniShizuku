package moe.shizuku.manager.essentials

/**
 * Domain-specific immutable state machine for Caffeinate WakeLock Controller.
 * Pure Kotlin, zero Android framework dependencies for deterministic JVM testing.
 */
enum class CaffeinateState {
    INACTIVE,
    COUNTDOWN_SELECTION,
    ACTIVE,
    EXPIRED,
    ABORTED
}

/**
 * Immutable snapshot of the Caffeinate state machine.
 */
data class CaffeinateSnapshot(
    val state: CaffeinateState = CaffeinateState.INACTIVE,
    val generation: Long = 0L,
    val selectedTimeoutMinutes: Int = 5, // -1 means Infinite
    val selectionSecondsRemaining: Int = 0,
    val startedAtMonotonicMs: Long = 0L,
    val deadlineMonotonicMs: Long = 0L, // 0L if infinite
    val remainingActiveMillis: Long = 0L,
    val totalActiveDurationMillis: Long = 0L,
    val wakeLockHeld: Boolean = false,
    val abortOnScreenOff: Boolean = true
) {
    val isActive: Boolean
        get() = state == CaffeinateState.ACTIVE

    val isStarting: Boolean
        get() = state == CaffeinateState.COUNTDOWN_SELECTION

    val isRunning: Boolean
        get() = isActive || isStarting

    val isTerminalOrIdle: Boolean
        get() = state == CaffeinateState.INACTIVE ||
                state == CaffeinateState.EXPIRED ||
                state == CaffeinateState.ABORTED
}

/**
 * Typed events driving transitions within [CaffeinateStateMachine].
 */
sealed interface CaffeinateEvent {
    data class StartSelection(
        val generation: Long,
        val timeoutMinutes: Int,
        val countdownSeconds: Int = 5,
        val skipCountdown: Boolean = false,
        val abortOnScreenOff: Boolean = true,
        val currentTimeMs: Long = 0L
    ) : CaffeinateEvent

    data class CycleTimeout(
        val availablePresets: List<Int>,
        val resetCountdownSeconds: Int = 5
    ) : CaffeinateEvent

    data class SelectionTick(val secondsRemaining: Int) : CaffeinateEvent

    data class SelectionConfirmed(val currentTimeMs: Long) : CaffeinateEvent

    data class WakeLockStateChanged(val generation: Long, val held: Boolean) : CaffeinateEvent

    data class ActiveTick(val remainingMillis: Long) : CaffeinateEvent

    data class ActiveExpired(val generation: Long) : CaffeinateEvent

    data class ScreenTurnedOff(val currentTimeMs: Long) : CaffeinateEvent

    data class StopRequested(val reason: String = "User requested") : CaffeinateEvent

    data object Reset : CaffeinateEvent
}

/**
 * Pure state reducer for Caffeinate state machine.
 */
object CaffeinateStateMachine {

    fun reduce(snapshot: CaffeinateSnapshot, event: CaffeinateEvent): CaffeinateSnapshot {
        return when (event) {
            is CaffeinateEvent.Reset -> CaffeinateSnapshot()

            is CaffeinateEvent.StopRequested -> {
                snapshot.copy(
                    state = CaffeinateState.INACTIVE,
                    wakeLockHeld = false,
                    selectionSecondsRemaining = 0,
                    remainingActiveMillis = 0L,
                    deadlineMonotonicMs = 0L
                )
            }

            is CaffeinateEvent.StartSelection -> {
                if (event.skipCountdown) {
                    val durationMs = if (event.timeoutMinutes > 0) event.timeoutMinutes * 60 * 1000L else 0L
                    val deadline = if (durationMs > 0L) event.currentTimeMs + durationMs else 0L
                    snapshot.copy(
                        state = CaffeinateState.ACTIVE,
                        generation = event.generation,
                        selectedTimeoutMinutes = event.timeoutMinutes,
                        selectionSecondsRemaining = 0,
                        startedAtMonotonicMs = event.currentTimeMs,
                        deadlineMonotonicMs = deadline,
                        remainingActiveMillis = durationMs,
                        totalActiveDurationMillis = durationMs,
                        wakeLockHeld = true,
                        abortOnScreenOff = event.abortOnScreenOff
                    )
                } else {
                    snapshot.copy(
                        state = CaffeinateState.COUNTDOWN_SELECTION,
                        generation = event.generation,
                        selectedTimeoutMinutes = event.timeoutMinutes,
                        selectionSecondsRemaining = event.countdownSeconds,
                        startedAtMonotonicMs = 0L,
                        deadlineMonotonicMs = 0L,
                        remainingActiveMillis = 0L,
                        wakeLockHeld = false,
                        abortOnScreenOff = event.abortOnScreenOff
                    )
                }
            }

            is CaffeinateEvent.CycleTimeout -> {
                if (snapshot.state != CaffeinateState.COUNTDOWN_SELECTION && snapshot.state != CaffeinateState.ACTIVE) {
                    snapshot
                } else {
                    val presets = if (event.availablePresets.isNotEmpty()) event.availablePresets else listOf(5, 10, 30, 60, -1)
                    val idx = presets.indexOf(snapshot.selectedTimeoutMinutes)
                    val nextTimeout = if (idx == -1 || idx == presets.size - 1) presets.first() else presets[idx + 1]

                    snapshot.copy(
                        selectedTimeoutMinutes = nextTimeout,
                        selectionSecondsRemaining = event.resetCountdownSeconds
                    )
                }
            }

            is CaffeinateEvent.SelectionTick -> {
                if (snapshot.state != CaffeinateState.COUNTDOWN_SELECTION) {
                    snapshot
                } else {
                    snapshot.copy(selectionSecondsRemaining = event.secondsRemaining)
                }
            }

            is CaffeinateEvent.SelectionConfirmed -> {
                if (snapshot.state != CaffeinateState.COUNTDOWN_SELECTION) {
                    snapshot
                } else {
                    val durationMs = if (snapshot.selectedTimeoutMinutes > 0) {
                        snapshot.selectedTimeoutMinutes * 60 * 1000L
                    } else {
                        0L
                    }
                    val deadline = if (durationMs > 0L) event.currentTimeMs + durationMs else 0L
                    snapshot.copy(
                        state = CaffeinateState.ACTIVE,
                        selectionSecondsRemaining = 0,
                        startedAtMonotonicMs = event.currentTimeMs,
                        deadlineMonotonicMs = deadline,
                        remainingActiveMillis = durationMs,
                        totalActiveDurationMillis = durationMs,
                        wakeLockHeld = true
                    )
                }
            }

            is CaffeinateEvent.WakeLockStateChanged -> {
                if (event.generation != snapshot.generation) {
                    snapshot
                } else {
                    snapshot.copy(wakeLockHeld = event.held)
                }
            }

            is CaffeinateEvent.ActiveTick -> {
                if (snapshot.state != CaffeinateState.ACTIVE) {
                    snapshot
                } else {
                    snapshot.copy(remainingActiveMillis = event.remainingMillis)
                }
            }

            is CaffeinateEvent.ActiveExpired -> {
                if (event.generation != snapshot.generation || snapshot.state != CaffeinateState.ACTIVE) {
                    snapshot
                } else {
                    snapshot.copy(
                        state = CaffeinateState.EXPIRED,
                        wakeLockHeld = false,
                        remainingActiveMillis = 0L
                    )
                }
            }

            is CaffeinateEvent.ScreenTurnedOff -> {
                if (snapshot.abortOnScreenOff && snapshot.isRunning) {
                    snapshot.copy(
                        state = CaffeinateState.ABORTED,
                        wakeLockHeld = false,
                        selectionSecondsRemaining = 0,
                        remainingActiveMillis = 0L
                    )
                } else {
                    snapshot
                }
            }
        }
    }
}
