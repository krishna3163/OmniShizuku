package moe.shizuku.manager.callrecorder

/**
 * Audio source for capture. Mirrors MediaRecorder.AudioSource values.
 */
enum class AudioSource(val value: Int) {
    VOICE_COMMUNICATION(0),
    VOICE_CALL(1),
    MIC(1),
    CAMCORDER(5),
    VOICE_RECOGNITION(6),
    VOICE_UPLINK(7),
    VOICE_DOWNLINK(8),
    REMOTE_SUBMIX(9),
    OUTPUT(10)
}

/**
 * Recording configuration for a single capture stream.
 */
data class CapturePlan(
    val source: AudioSource = AudioSource.VOICE_COMMUNICATION,
    val sampleRate: Int = 44100,
    val channelCount: Int = 1
)

/**
 * Codec used for encoding the recording output.
 */
enum class RecordingCodec(val key: String, val defaultBitrate: Int, val mimeType: String) {
    OPUS("opus", 16_000, "audio/opus"),
    AAC("aac", 32_000, "audio/mp4a-latm"),
    AMR_WB("amr_wb", 12_000, "audio/amr-wb");

    companion object {
        fun fromOrdinal(ordinal: Int): RecordingCodec = values().firstOrNull { it.ordinal == ordinal } ?: OPUS
    }
}

/**
 * Complete recording plan for a session.
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
        fun fromCode(code: Int): RecordingFailure = values().firstOrNull { it.code == code } ?: INVALID_STATE
    }
}

/**
 * Recording session state.
 */
enum class RecordingState {
    IDLE,
    RECORDING,
    PAUSED,
    STOPPED
}

/**
 * Call direction.
 */
enum class CallDirection {
    INCOMING,
    OUTGOING,
    UNKNOWN
}

/**
 * Call data captured during a phone call.
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
