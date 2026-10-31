package moe.shizuku.manager.callrecorder

/**
 * Logical recording clock based on audio frames rather than wall-clock time.
 * Allows seamless pause/resume without timestamps jumping or creating glitches.
 */
class RecordingClock {
    private var processedFrames = 0L

    fun currentPresentationTimeUs(): Long {
        return (processedFrames * 1_000_000L) / AudioConfig.SAMPLE_RATE
    }

    fun advance(frameCount: Long) {
        if (frameCount > 0) {
            processedFrames += frameCount
        }
    }

    fun reset() {
        processedFrames = 0L
    }
}
