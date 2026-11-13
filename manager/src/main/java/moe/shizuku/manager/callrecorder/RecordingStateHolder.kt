package moe.shizuku.manager.callrecorder

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf

/**
 * Explicit session state for call recording UI/service coordination.
 */
sealed interface RecordingSessionState {
    data object Idle : RecordingSessionState
    data object Starting : RecordingSessionState
    data object Recording : RecordingSessionState
    data object Paused : RecordingSessionState
    data object Stopping : RecordingSessionState
    data class Error(val message: String) : RecordingSessionState
}

/**
 * Singleton state holder for the Call Recorder UI and background service.
 * Delegates lifecycle transitions to [RecordingSessionMachine] as the authoritative reducer.
 */
object RecordingStateHolder {
    private val snapshot = mutableStateOf(RecordingSessionSnapshot())

    val sessionState = mutableStateOf<RecordingSessionState>(RecordingSessionState.Idle)
    val durationMs = mutableLongStateOf(0L)
    val fileSizeBytes = mutableLongStateOf(0L)
    val currentFileName = mutableStateOf<String?>(null)
    val lastError = mutableStateOf<String?>(null)

    // Configuration settings
    val selectedCodec = mutableStateOf(RecordingCodec.OPUS)
    val selectedBitrate = mutableIntStateOf(RecordingCodec.OPUS.defaultBitrate)
    val selectedPrimarySource = mutableStateOf(AudioSource.VOICE_COMMUNICATION)
    val selectedSecondarySource = mutableStateOf<AudioSource?>(null)
    val isChannelSplit = mutableStateOf(false)

    // Automation
    val autoRecordEnabled = mutableStateOf(false)
    val recordIncoming = mutableStateOf(true)
    val recordOutgoing = mutableStateOf(true)
    val ignoreAnonymous = mutableStateOf(false)
    val ignoreContacts = mutableStateOf(false)

    val generation: Long get() = snapshot.value.generation

    val isRecording: Boolean
        get() = snapshot.value.isRecording

    val isPaused: Boolean
        get() = snapshot.value.isPaused

    @Synchronized
    private fun apply(event: RecordingSessionEvent) {
        val next = RecordingSessionMachine.reduce(snapshot.value, event)
        snapshot.value = next
        sessionState.value = next.state
        durationMs.longValue = next.durationMs
        fileSizeBytes.longValue = next.fileSizeBytes
        lastError.value = next.lastError
        currentFileName.value = next.fileName
    }

    fun resetProgress() {
        durationMs.longValue = 0L
        fileSizeBytes.longValue = 0L
        snapshot.value = snapshot.value.copy(durationMs = 0L, fileSizeBytes = 0L)
    }

    fun setIdle(clearFile: Boolean = false) {
        apply(if (clearFile) RecordingSessionEvent.Reset(clearFile = true) else RecordingSessionEvent.Stopped(generation))
    }

    fun setStarting(fileName: String? = null) {
        if (fileName != null) currentFileName.value = fileName
        snapshot.value = snapshot.value.copy(fileName = currentFileName.value)
        apply(RecordingSessionEvent.StartRequested)
    }

    fun setRecording() {
        apply(RecordingSessionEvent.Started(generation))
    }

    fun setPaused() {
        apply(RecordingSessionEvent.Paused(generation))
    }

    fun setResumed() {
        apply(RecordingSessionEvent.Resumed(generation))
    }

    fun setStopping() {
        apply(RecordingSessionEvent.StopRequested)
    }

    fun setStopped() {
        apply(RecordingSessionEvent.Stopped(generation))
    }

    fun setError(message: String?) {
        apply(RecordingSessionEvent.Failed(message, generation))
    }

    fun onProgress(duration: Long, size: Long) {
        apply(RecordingSessionEvent.Progress(duration, size, generation))
    }

    fun snapshot(): RecordingSessionSnapshot = snapshot.value
}
