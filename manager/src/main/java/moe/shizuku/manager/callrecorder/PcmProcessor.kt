package moe.shizuku.manager.callrecorder

import java.nio.ByteBuffer

/**
 * Handles mixing mono streams with soft-clipping and interleaving dual mono streams into stereo.
 */
class PcmProcessor(private val plan: RecordingPlan) {
    private val vadDuckingProcessor = PcmVadDuckingProcessor()

    fun process(
        primary: ByteBuffer,
        primaryBytes: Int,
        secondary: ByteBuffer?,
        secondaryBytes: Int,
        output: ByteBuffer
    ): Int {
        output.clear()

        if (secondary == null) {
            return copyMono(primary, primaryBytes, output)
        }

        val usableBytes = minOf(primaryBytes, secondaryBytes) and -AudioConfig.PCM_BYTES_PER_SAMPLE
        if (usableBytes <= 0) return 0

        vadDuckingProcessor.process(primary, secondary, usableBytes)

        return if (plan.isAudioChannelSplitEnabled) {
            interleaveStereo(primary, secondary, usableBytes, output)
        } else {
            mixMono(primary, secondary, usableBytes, output)
        }
    }

    private fun copyMono(source: ByteBuffer, byteCount: Int, output: ByteBuffer): Int {
        val usableBytes = byteCount and -AudioConfig.PCM_BYTES_PER_SAMPLE
        if (output.remaining() < usableBytes) return 0

        val oldLimit = source.limit()
        val oldPos = source.position()
        source.limit(oldPos + usableBytes)
        output.put(source)
        source.limit(oldLimit)
        source.position(oldPos)
        return usableBytes
    }

    private fun mixMono(primary: ByteBuffer, secondary: ByteBuffer, byteCount: Int, output: ByteBuffer): Int {
        if (output.remaining() < byteCount || primary.remaining() < byteCount || secondary.remaining() < byteCount) return 0
        val shortCount = byteCount / AudioConfig.PCM_BYTES_PER_SAMPLE

        val primaryShorts = primary.asShortBuffer()
        val secondaryShorts = secondary.asShortBuffer()
        val outputShorts = output.asShortBuffer()

        for (i in 0 until shortCount) {
            val p = primaryShorts.get(i).toInt()
            val s = secondaryShorts.get(i).toInt()

            // Soft-clipping algorithm prevents digital harsh clipping
            val mixed = if (p < 0 && s < 0) {
                (p + s) - ((p * s) / Short.MIN_VALUE.toInt())
            } else if (p > 0 && s > 0) {
                (p + s) - ((p * s) / Short.MAX_VALUE.toInt())
            } else {
                p + s
            }
            outputShorts.put(i, mixed.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort())
        }

        output.position(output.position() + byteCount)
        return byteCount
    }

    private fun interleaveStereo(primary: ByteBuffer, secondary: ByteBuffer, byteCount: Int, output: ByteBuffer): Int {
        val outputBytes = byteCount * 2
        if (output.remaining() < outputBytes) return 0
        val shortCount = byteCount / AudioConfig.PCM_BYTES_PER_SAMPLE

        val primaryShorts = primary.asShortBuffer()
        val secondaryShorts = secondary.asShortBuffer()
        val outputShorts = output.asShortBuffer()

        for (i in 0 until shortCount) {
            val outIndex = i * 2
            outputShorts.put(outIndex, primaryShorts.get(i))
            outputShorts.put(outIndex + 1, secondaryShorts.get(i))
        }

        output.position(output.position() + outputBytes)
        return outputBytes
    }
}
