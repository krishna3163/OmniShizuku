package moe.shizuku.manager.themepatcher

/**
 * Domain-specific immutable state machine for OEM Theme Store Trial Freeze Engine.
 * Pure Kotlin, zero Android framework dependencies for deterministic JVM testing.
 */
enum class ThemePatcherState {
    IDLE,
    WATCHING_FOR_TRIAL,
    TRIAL_DETECTED,
    TERMINATING_STORES,
    APPLYING_PATCH,
    VERIFYING,
    COMPLETED,
    TIMED_OUT,
    CANCELLED,
    FAILED
}

/**
 * Immutable snapshot of the Theme Patcher state machine.
 */
data class ThemePatcherSnapshot(
    val state: ThemePatcherState = ThemePatcherState.IDLE,
    val generation: Long = 0L,
    val storePackages: List<String> = emptyList(),
    val detectedItems: List<String> = emptyList(),
    val startedAtMonotonicMs: Long = 0L,
    val timeoutDeadlineMs: Long = 0L,
    val pollIntervalMs: Long = 3500L,
    val pollCount: Int = 0,
    val maxPollCount: Int = 85, // ~5 minutes at 3.5s interval
    val lastError: String? = null
) {
    val isRunning: Boolean
        get() = state == ThemePatcherState.WATCHING_FOR_TRIAL ||
                state == ThemePatcherState.TRIAL_DETECTED ||
                state == ThemePatcherState.TERMINATING_STORES ||
                state == ThemePatcherState.APPLYING_PATCH ||
                state == ThemePatcherState.VERIFYING

    val isTerminal: Boolean
        get() = state == ThemePatcherState.COMPLETED ||
                state == ThemePatcherState.TIMED_OUT ||
                state == ThemePatcherState.CANCELLED ||
                state == ThemePatcherState.FAILED
}

/**
 * Typed events driving transitions in [ThemePatcherStateMachine].
 */
sealed interface ThemePatcherEvent {
    data class StartRequested(
        val generation: Long,
        val storePackages: List<String>,
        val timeoutDeadlineMs: Long,
        val currentTimeMs: Long
    ) : ThemePatcherEvent

    data class TrialPolled(
        val generation: Long,
        val hasTrial: Boolean,
        val items: List<String>,
        val currentTimeMs: Long
    ) : ThemePatcherEvent

    data class StoresTerminated(val generation: Long) : ThemePatcherEvent

    data class PatchApplied(
        val generation: Long,
        val success: Boolean,
        val error: String? = null
    ) : ThemePatcherEvent

    data class VerificationCompleted(
        val generation: Long,
        val verified: Boolean
    ) : ThemePatcherEvent

    data object CancelRequested : ThemePatcherEvent

    data object Reset : ThemePatcherEvent
}

/**
 * Pure state reducer for Theme Patcher state machine.
 */
object ThemePatcherStateMachine {

    fun reduce(snapshot: ThemePatcherSnapshot, event: ThemePatcherEvent): ThemePatcherSnapshot {
        return when (event) {
            is ThemePatcherEvent.Reset -> ThemePatcherSnapshot()

            is ThemePatcherEvent.CancelRequested -> {
                if (snapshot.isTerminal) {
                    snapshot
                } else {
                    snapshot.copy(
                        state = ThemePatcherState.CANCELLED,
                        lastError = null
                    )
                }
            }

            is ThemePatcherEvent.StartRequested -> {
                if (event.generation <= snapshot.generation && snapshot.isRunning) {
                    // Reject older or equal generation if already running
                    snapshot
                } else {
                    ThemePatcherSnapshot(
                        state = ThemePatcherState.WATCHING_FOR_TRIAL,
                        generation = event.generation,
                        storePackages = event.storePackages,
                        detectedItems = emptyList(),
                        startedAtMonotonicMs = event.currentTimeMs,
                        timeoutDeadlineMs = event.timeoutDeadlineMs,
                        pollCount = 0,
                        lastError = null
                    )
                }
            }

            is ThemePatcherEvent.TrialPolled -> {
                if (event.generation != snapshot.generation || snapshot.state != ThemePatcherState.WATCHING_FOR_TRIAL) {
                    snapshot
                } else {
                    val nextPollCount = snapshot.pollCount + 1
                    val timedOut = (snapshot.timeoutDeadlineMs > 0L && event.currentTimeMs >= snapshot.timeoutDeadlineMs) ||
                            (nextPollCount >= snapshot.maxPollCount)

                    if (event.hasTrial) {
                        snapshot.copy(
                            state = ThemePatcherState.TRIAL_DETECTED,
                            detectedItems = event.items,
                            pollCount = nextPollCount
                        )
                    } else if (timedOut) {
                        snapshot.copy(
                            state = ThemePatcherState.TIMED_OUT,
                            pollCount = nextPollCount
                        )
                    } else {
                        snapshot.copy(pollCount = nextPollCount)
                    }
                }
            }

            is ThemePatcherEvent.StoresTerminated -> {
                if (event.generation != snapshot.generation || snapshot.state != ThemePatcherState.TRIAL_DETECTED) {
                    snapshot
                } else {
                    snapshot.copy(state = ThemePatcherState.APPLYING_PATCH)
                }
            }

            is ThemePatcherEvent.PatchApplied -> {
                if (event.generation != snapshot.generation || snapshot.state != ThemePatcherState.APPLYING_PATCH) {
                    snapshot
                } else if (!event.success) {
                    snapshot.copy(
                        state = ThemePatcherState.FAILED,
                        lastError = event.error ?: "Failed to write theme patch settings"
                    )
                } else {
                    snapshot.copy(state = ThemePatcherState.VERIFYING)
                }
            }

            is ThemePatcherEvent.VerificationCompleted -> {
                if (event.generation != snapshot.generation || snapshot.state != ThemePatcherState.VERIFYING) {
                    snapshot
                } else if (event.verified) {
                    snapshot.copy(state = ThemePatcherState.COMPLETED)
                } else {
                    snapshot.copy(
                        state = ThemePatcherState.FAILED,
                        lastError = "Verification failed: Trial flags were not frozen"
                    )
                }
            }
        }
    }
}
