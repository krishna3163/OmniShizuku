package moe.shizuku.manager.callrecorder

import android.media.AudioFormat

/**
 * Audio configuration constants for shell call recording.
 * Standardized on 16 kHz HD Voice PCM-16.
 */
object AudioConfig {
    const val SAMPLE_RATE = 16_000
    const val CAPTURE_CHANNEL_COUNT = 1
    const val CAPTURE_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    const val PCM_ENCODING = AudioFormat.ENCODING_PCM_16BIT
    const val PCM_BYTES_PER_SAMPLE = 2

    const val BLOCK_DURATION_MS = 20
    const val BLOCK_SAMPLE_COUNT = SAMPLE_RATE * BLOCK_DURATION_MS / 1_000 // 320 samples
    const val BLOCK_SIZE_BYTES = BLOCK_SAMPLE_COUNT * PCM_BYTES_PER_SAMPLE // 640 bytes
    const val STEREO_BLOCK_SIZE_BYTES = BLOCK_SIZE_BYTES * 2 // 1280 bytes

    const val LOG_INTERVAL_BLOCKS = 50L
    const val ZERO_READ_WARNING_INTERVAL = 50L
}
