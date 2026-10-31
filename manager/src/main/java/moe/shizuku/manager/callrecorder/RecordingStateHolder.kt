package moe.shizuku.manager.callrecorder

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf

/**
 * Singleton state holder for the Call Recorder UI and background service.
 */
object RecordingStateHolder {
    val isRecording = mutableStateOf(false)
    val isPaused = mutableStateOf(false)
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
}
