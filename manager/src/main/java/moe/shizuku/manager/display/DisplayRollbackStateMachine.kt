package moe.shizuku.manager.display

/**
 * Pure Kotlin domain state machine for Display Density/Resolution Rollback & Safety.
 * Guarantees single-flight changes, monotonic deadlines, race-free confirmation/rollback,
 * and zero Android framework dependencies for deterministic JVM testing.
 */

data class DisplayRollbackSnapshot(
    val state: DisplayRollbackState = DisplayRollbackState.IDLE,
    val generation: Long = 0L,
    val targetChange: DisplayChange? = null,
    val beforeSnapshot: DisplaySnapshot? = null,
    val deadlineMonotonicMs: Long = 0L,
    val remainingMillis: Long = 0L,
    val lastError: String? = null
) {
    val isAwaitingConfirmation: Boolean
        get() = state == DisplayRollbackState.AWAITING_CONFIRMATION

    val isTerminalOrIdle: Boolean
        get() = state == DisplayRollbackState.IDLE ||
                state == DisplayRollbackState.CONFIRMED ||
                state == DisplayRollbackState.RESTORED ||
                state == DisplayRollbackState.FAILED
}

sealed interface DisplayRollbackEvent {
    data class StartChange(
        val generation: Long,
        val target: DisplayChange,
        val before: DisplaySnapshot,
        val timeoutMillis: Long,
        val currentTimeMs: Long
    ) : DisplayRollbackEvent

    data class ApplySucceeded(val generation: Long) : DisplayRollbackEvent

    data class ApplyFailed(val generation: Long, val error: String) : DisplayRollbackEvent

    data class Tick(val currentTimeMs: Long) : DisplayRollbackEvent

    data class ConfirmRequested(val generation: Long, val currentTimeMs: Long) : DisplayRollbackEvent

    data class RejectRequested(val generation: Long) : DisplayRollbackEvent

    data class TimeoutExpired(val generation: Long) : DisplayRollbackEvent

    data class RollbackSucceeded(val generation: Long) : DisplayRollbackEvent

    data class RollbackFailed(val generation: Long, val error: String) : DisplayRollbackEvent

    data object Reset : DisplayRollbackEvent
}

object DisplayRollbackStateMachine {

    fun reduce(snapshot: DisplayRollbackSnapshot, event: DisplayRollbackEvent): DisplayRollbackSnapshot {
        return when (event) {
            is DisplayRollbackEvent.Reset -> DisplayRollbackSnapshot()

            is DisplayRollbackEvent.StartChange -> {
                if (snapshot.state != DisplayRollbackState.IDLE &&
                    snapshot.state != DisplayRollbackState.CONFIRMED &&
                    snapshot.state != DisplayRollbackState.RESTORED &&
                    snapshot.state != DisplayRollbackState.FAILED
                ) {
                    // Cannot start a new change while another is actively in flight
                    snapshot
                } else {
                    val deadline = event.currentTimeMs + event.timeoutMillis
                    snapshot.copy(
                        state = DisplayRollbackState.APPLYING,
                        generation = event.generation,
                        targetChange = event.target,
                        beforeSnapshot = event.before,
                        deadlineMonotonicMs = deadline,
                        remainingMillis = event.timeoutMillis,
                        lastError = null
                    )
                }
            }

            is DisplayRollbackEvent.ApplySucceeded -> {
                if (event.generation != snapshot.generation || snapshot.state != DisplayRollbackState.APPLYING) {
                    snapshot
                } else {
                    snapshot.copy(
                        state = DisplayRollbackState.AWAITING_CONFIRMATION,
                        lastError = null
                    )
                }
            }

            is DisplayRollbackEvent.ApplyFailed -> {
                if (event.generation != snapshot.generation || snapshot.state != DisplayRollbackState.APPLYING) {
                    snapshot
                } else {
                    snapshot.copy(
                        state = DisplayRollbackState.FAILED,
                        lastError = event.error
                    )
                }
            }

            is DisplayRollbackEvent.Tick -> {
                if (snapshot.state != DisplayRollbackState.AWAITING_CONFIRMATION) {
                    snapshot
                } else {
                    val remaining = snapshot.deadlineMonotonicMs - event.currentTimeMs
                    if (remaining <= 0L) {
                        snapshot.copy(
                            state = DisplayRollbackState.ROLLING_BACK,
                            remainingMillis = 0L
                        )
                    } else {
                        snapshot.copy(remainingMillis = remaining)
                    }
                }
            }

            is DisplayRollbackEvent.ConfirmRequested -> {
                if (event.generation != snapshot.generation || snapshot.state != DisplayRollbackState.AWAITING_CONFIRMATION) {
                    snapshot
                } else if (event.currentTimeMs > snapshot.deadlineMonotonicMs) {
                    // Expired confirmation window automatically rolls back
                    snapshot.copy(
                        state = DisplayRollbackState.ROLLING_BACK,
                        remainingMillis = 0L
                    )
                } else {
                    snapshot.copy(
                        state = DisplayRollbackState.CONFIRMED,
                        remainingMillis = 0L,
                        lastError = null
                    )
                }
            }

            is DisplayRollbackEvent.RejectRequested -> {
                if (event.generation != snapshot.generation || snapshot.state != DisplayRollbackState.AWAITING_CONFIRMATION) {
                    snapshot
                } else {
                    snapshot.copy(
                        state = DisplayRollbackState.ROLLING_BACK,
                        remainingMillis = 0L
                    )
                }
            }

            is DisplayRollbackEvent.TimeoutExpired -> {
                if (event.generation != snapshot.generation || snapshot.state != DisplayRollbackState.AWAITING_CONFIRMATION) {
                    snapshot
                } else {
                    snapshot.copy(
                        state = DisplayRollbackState.ROLLING_BACK,
                        remainingMillis = 0L
                    )
                }
            }

            is DisplayRollbackEvent.RollbackSucceeded -> {
                if (event.generation != snapshot.generation || snapshot.state != DisplayRollbackState.ROLLING_BACK) {
                    snapshot
                } else {
                    snapshot.copy(
                        state = DisplayRollbackState.RESTORED,
                        remainingMillis = 0L,
                        lastError = null
                    )
                }
            }

            is DisplayRollbackEvent.RollbackFailed -> {
                if (event.generation != snapshot.generation || snapshot.state != DisplayRollbackState.ROLLING_BACK) {
                    snapshot
                } else {
                    snapshot.copy(
                        state = DisplayRollbackState.FAILED,
                        lastError = event.error
                    )
                }
            }
        }
    }
}
