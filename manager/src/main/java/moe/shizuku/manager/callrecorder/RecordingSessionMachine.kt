package moe.shizuku.manager.callrecorder

/**
 * Pure recording session state machine (no Android dependencies) for deterministic testing.
 * Generation tokens prevent stale async callbacks from mutating a newer session.
 */
data class RecordingSessionSnapshot(
    val state: RecordingSessionState = RecordingSessionState.Idle,
    val generation: Long = 0L,
    val durationMs: Long = 0L,
    val fileSizeBytes: Long = 0L,
    val fileName: String? = null,
    val lastError: String? = null
) {
    val isRecording: Boolean
        get() = state is RecordingSessionState.Recording || state is RecordingSessionState.Paused

    val isPaused: Boolean
        get() = state is RecordingSessionState.Paused
}

sealed interface RecordingSessionEvent {
    data object StartRequested : RecordingSessionEvent
    data class Started(val generation: Long? = null) : RecordingSessionEvent
    data object PauseRequested : RecordingSessionEvent
    data class Paused(val generation: Long? = null) : RecordingSessionEvent
    data object ResumeRequested : RecordingSessionEvent
    data class Resumed(val generation: Long? = null) : RecordingSessionEvent
    data object StopRequested : RecordingSessionEvent
    data class Stopped(val generation: Long? = null) : RecordingSessionEvent
    data class Progress(val durationMs: Long, val fileSizeBytes: Long, val generation: Long? = null) : RecordingSessionEvent
    data class Failed(val message: String?, val generation: Long? = null) : RecordingSessionEvent
    data class Reset(val clearFile: Boolean = false) : RecordingSessionEvent
}

object RecordingSessionMachine {

    fun reduce(snapshot: RecordingSessionSnapshot, event: RecordingSessionEvent): RecordingSessionSnapshot {
        if (isStale(snapshot, event)) return snapshot
        return when (event) {
            RecordingSessionEvent.StartRequested -> when (snapshot.state) {
                RecordingSessionState.Idle,
                is RecordingSessionState.Error -> snapshot.copy(
                    state = RecordingSessionState.Starting,
                    generation = snapshot.generation + 1,
                    durationMs = 0L,
                    fileSizeBytes = 0L,
                    lastError = null
                )
                RecordingSessionState.Stopped -> snapshot.copy(
                    state = RecordingSessionState.Starting,
                    generation = snapshot.generation + 1,
                    durationMs = 0L,
                    fileSizeBytes = 0L,
                    lastError = null
                )
                else -> snapshot // reject duplicate/concurrent starts while active/transitioning
            }

            is RecordingSessionEvent.Started -> when (snapshot.state) {
                RecordingSessionState.Starting -> snapshot.copy(
                    state = RecordingSessionState.Recording,
                    lastError = null
                )
                else -> snapshot
            }

            RecordingSessionEvent.PauseRequested -> when (snapshot.state) {
                RecordingSessionState.Recording -> snapshot.copy(state = RecordingSessionState.Paused)
                else -> snapshot
            }
            is RecordingSessionEvent.Paused -> when (snapshot.state) {
                RecordingSessionState.Recording -> snapshot.copy(state = RecordingSessionState.Paused)
                else -> snapshot
            }

            RecordingSessionEvent.ResumeRequested -> when (snapshot.state) {
                RecordingSessionState.Paused -> snapshot.copy(state = RecordingSessionState.Recording)
                else -> snapshot
            }
            is RecordingSessionEvent.Resumed -> when (snapshot.state) {
                RecordingSessionState.Paused -> snapshot.copy(state = RecordingSessionState.Recording)
                else -> snapshot
            }

            RecordingSessionEvent.StopRequested -> when (snapshot.state) {
                RecordingSessionState.Starting,
                RecordingSessionState.Recording,
                RecordingSessionState.Paused -> snapshot.copy(state = RecordingSessionState.Stopping)
                else -> snapshot
            }

            is RecordingSessionEvent.Stopped -> when (snapshot.state) {
                RecordingSessionState.Starting,
                RecordingSessionState.Recording,
                RecordingSessionState.Paused,
                RecordingSessionState.Stopping -> snapshot.copy(
                    state = RecordingSessionState.Idle,
                    durationMs = 0L,
                    fileSizeBytes = 0L
                )
                RecordingSessionState.Idle -> snapshot.copy(durationMs = 0L, fileSizeBytes = 0L)
                else -> snapshot
            }

            is RecordingSessionEvent.Progress -> when (snapshot.state) {
                RecordingSessionState.Recording,
                RecordingSessionState.Paused -> snapshot.copy(
                    durationMs = event.durationMs.coerceAtLeast(0L),
                    fileSizeBytes = event.fileSizeBytes.coerceAtLeast(0L)
                )
                else -> snapshot
            }

            is RecordingSessionEvent.Failed -> {
                val msg = event.message ?: "Unknown recording error"
                snapshot.copy(
                    state = RecordingSessionState.Error(msg),
                    lastError = msg,
                    durationMs = 0L,
                    fileSizeBytes = 0L
                )
            }

            is RecordingSessionEvent.Reset -> snapshot.copy(
                state = RecordingSessionState.Idle,
                durationMs = 0L,
                fileSizeBytes = 0L,
                lastError = null,
                fileName = if (event.clearFile) null else snapshot.fileName
            )
        }
    }

    private fun isStale(snapshot: RecordingSessionSnapshot, event: RecordingSessionEvent): Boolean {
        val eventGen = when (event) {
            is RecordingSessionEvent.Started -> event.generation
            is RecordingSessionEvent.Paused -> event.generation
            is RecordingSessionEvent.Resumed -> event.generation
            is RecordingSessionEvent.Stopped -> event.generation
            is RecordingSessionEvent.Progress -> event.generation
            is RecordingSessionEvent.Failed -> event.generation
            else -> null
        } ?: return false
        return eventGen != snapshot.generation
    }
}
