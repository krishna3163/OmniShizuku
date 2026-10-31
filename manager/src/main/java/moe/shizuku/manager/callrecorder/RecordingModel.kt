package moe.shizuku.manager.callrecorder

import android.media.MediaFormat
import android.media.MediaMuxer

/**
 * Audio source for capture. Values match android.media.MediaRecorder.AudioSource.
 */
enum class AudioSource(val value: Int, val displayName: String) {
    VOICE_COMMUNICATION(7, "Voice Communication (VoIP/Call)"),
    VOICE_CALL(4, "Voice Call (Uplink + Downlink)"),
    MIC(1, "Standard Microphone"),
    VOICE_RECOGNITION(6, "Voice Recognition"),
    VOICE_UPLINK(2, "Voice Uplink (Your Voice)"),
    VOICE_DOWNLINK(3, "Voice Downlink (Caller Voice)");

    companion object {
        fun fromValue(value: Int): AudioSource =
            entries.firstOrNull { it.value == value } ?: VOICE_COMMUNICATION
        fun fromOrdinal(ordinal: Int): AudioSource =
            entries.getOrNull(ordinal) ?: VOICE_COMMUNICATION
    }
}

/**
 * Codec used for encoding the call recording.
 */
enum class RecordingCodec(
    val key: String,
    val defaultBitrate: Int,
    val outputFormat: Int,
    val mimeType: String,
    val extension: String,
    val displayName: String
) {
    OPUS(
        key = "opus",
        defaultBitrate = 32_000,
        outputFormat = MediaMuxer.OutputFormat.MUXER_OUTPUT_OGG,
        mimeType = MediaFormat.MIMETYPE_AUDIO_OPUS,
        extension = ".ogg",
        displayName = "Opus (Recommended, OGG)"
    ),
    AAC(
        key = "aac",
        defaultBitrate = 64_000,
        outputFormat = MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4,
        mimeType = MediaFormat.MIMETYPE_AUDIO_AAC,
        extension = ".m4a",
        displayName = "AAC (Universal, M4A)"
    ),
    AMR_WB(
        key = "amr_wb",
        defaultBitrate = 16_000,
        outputFormat = MediaMuxer.OutputFormat.MUXER_OUTPUT_3GPP,
        mimeType = MediaFormat.MIMETYPE_AUDIO_AMR_WB,
        extension = ".3gp",
        displayName = "AMR-WB (Speech 16kHz)"
    );

    companion object {
        fun fromOrdinal(ordinal: Int): RecordingCodec =
            entries.getOrNull(ordinal) ?: OPUS
        fun fromKey(key: String): RecordingCodec =
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: OPUS
    }
}

/**
 * Capture configuration for a stream.
 */
data class CapturePlan(
    val source: AudioSource = AudioSource.VOICE_COMMUNICATION,
    val sampleRate: Int = AudioConfig.SAMPLE_RATE,
    val channelCount: Int = AudioConfig.CAPTURE_CHANNEL_COUNT
)

/**
 * Complete recording configuration for a session.
 */
data class RecordingPlan(
    val primary: CapturePlan = CapturePlan(),
    val secondary: CapturePlan? = null,
    val isAudioChannelSplitEnabled: Boolean = false,
    val codec: RecordingCodec = RecordingCodec.OPUS,
    val bitrate: Int = RecordingCodec.OPUS.defaultBitrate
)

/**
 * Recording failure codes.
 */
enum class RecordingFailure(val code: Int) {
    CAPTURE_START_FAILED(1),
    CAPTURE_READ_FAILED(2),
    ENCODE_FAILED(3),
    PERMISSION_DENIED(4),
    INVALID_STATE(5);

    companion object {
        fun fromCode(code: Int): RecordingFailure =
            entries.firstOrNull { it.code == code } ?: INVALID_STATE
    }
}

/**
 * Recording session states.
 */
enum class RecordingState {
    IDLE,
    RECORDING,
    PAUSED,
    STOPPED
}

/**
 * Call direction for metadata.
 */
enum class CallDirection {
    INCOMING,
    OUTGOING,
    UNKNOWN
}

/**
 * Metadata record for a call.
 */
data class CallData(
    val number: String? = null,
    val direction: CallDirection = CallDirection.UNKNOWN,
    val startTimeMs: Long = System.currentTimeMillis(),
    val endTimeMs: Long = 0,
    val durationMs: Long = 0,
    val isRecorded: Boolean = false,
    val recordingPath: String? = null
)
