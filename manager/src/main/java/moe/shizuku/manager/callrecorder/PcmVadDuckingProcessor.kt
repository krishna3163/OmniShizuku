package moe.shizuku.manager.callrecorder

import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Voice activity detection and auto-gain control (AGC) processor for call streams.
 */
class PcmVadDuckingProcessor {
    companion object {
        private const val TARGET_RMS = 2500f
        private const val MIN_AGC_GAIN = 0.4f
        private const val MAX_AGC_GAIN = 8.0f
        private const val AGC_ATTACK_RATE = 0.04f
        private const val AGC_RELEASE_RATE = 0.004f
        private const val TRANSIENT_THRESHOLD = 1.0f
        private const val TRANSIENT_SLEW_RATE = 0.5f

        private const val TARGET_DUCKING_MULTIPLIER = 0.45f
        private const val BASE_NOISE_FLOOR = 55f
        private const val SNR_VOICE_THRESHOLD = 1.5f
        private const val NOISE_ADAPTATION_UP = 0.005f
        private const val NOISE_ADAPTATION_DOWN = 0.1f

        private const val ATTACK_MS = 20
        private const val HOLD_MS = 400
        private const val RELEASE_MS = 200

        private const val ATTACK_SAMPLES = (ATTACK_MS * AudioConfig.SAMPLE_RATE) / 1000
        private const val HOLD_SAMPLES = (HOLD_MS * AudioConfig.SAMPLE_RATE) / 1000
        private const val RELEASE_SAMPLES = (RELEASE_MS * AudioConfig.SAMPLE_RATE) / 1000
        private const val ATTACK_STEP = (1.0f - TARGET_DUCKING_MULTIPLIER) / ATTACK_SAMPLES
        private const val RELEASE_STEP = (1.0f - TARGET_DUCKING_MULTIPLIER) / RELEASE_SAMPLES
    }

    private enum class DuckingState { IDLE, ATTACK, HOLD, RELEASE }

    private class StreamState {
        var backgroundNoiseRms = BASE_NOISE_FLOOR
        var vadActive = false
        var targetAgcGain = 1.0f
        var currentAgcGain = 1.0f
        var state = DuckingState.IDLE
        var currentDuckingMultiplier = 1.0f
        var holdCounter = 0

        fun updateVad(rms: Float) {
            backgroundNoiseRms = if (rms > backgroundNoiseRms) {
                backgroundNoiseRms + (rms - backgroundNoiseRms) * NOISE_ADAPTATION_UP
            } else {
                backgroundNoiseRms + (rms - backgroundNoiseRms) * NOISE_ADAPTATION_DOWN
            }
            backgroundNoiseRms = max(backgroundNoiseRms, BASE_NOISE_FLOOR)
            val snr = rms / backgroundNoiseRms
            vadActive = snr > SNR_VOICE_THRESHOLD && rms > BASE_NOISE_FLOOR
        }

        fun updateAgcTarget(rms: Float) {
            targetAgcGain = if (rms > BASE_NOISE_FLOOR) {
                (TARGET_RMS / rms).coerceIn(MIN_AGC_GAIN, MAX_AGC_GAIN)
            } else {
                1.0f
            }
        }
    }

    private val primaryState = StreamState()
    private val secondaryState = StreamState()

    fun process(primary: ByteBuffer, secondary: ByteBuffer, byteCount: Int) {
        if (primary.remaining() < byteCount || secondary.remaining() < byteCount) return
        val sampleCount = byteCount / AudioConfig.PCM_BYTES_PER_SAMPLE

        val primaryShorts = primary.asShortBuffer()
        val secondaryShorts = secondary.asShortBuffer()

        var primarySumSquares = 0L
        var secondarySumSquares = 0L
        for (i in 0 until sampleCount) {
            val pSample = primaryShorts.get(i).toLong()
            val sSample = secondaryShorts.get(i).toLong()
            primarySumSquares += pSample * pSample
            secondarySumSquares += sSample * sSample
        }
        val primaryRms = sqrt((primarySumSquares / sampleCount.toDouble())).toFloat()
        val secondaryRms = sqrt((secondarySumSquares / sampleCount.toDouble())).toFloat()

        primaryState.updateVad(primaryRms)
        secondaryState.updateVad(secondaryRms)
        primaryState.updateAgcTarget(primaryRms)
        secondaryState.updateAgcTarget(secondaryRms)

        val primaryDuckRequested = secondaryState.vadActive && !primaryState.vadActive
        val secondaryDuckRequested = primaryState.vadActive && !secondaryState.vadActive
        val primaryForceRelease = primaryState.vadActive
        val secondaryForceRelease = secondaryState.vadActive

        for (i in 0 until sampleCount) {
            processDuckingState(primaryState, primaryDuckRequested, primaryForceRelease)
            val pAgcSlew = if (primaryState.targetAgcGain < primaryState.currentAgcGain) {
                if (primaryState.currentAgcGain - primaryState.targetAgcGain > TRANSIENT_THRESHOLD) {
                    TRANSIENT_SLEW_RATE
                } else {
                    AGC_ATTACK_RATE
                }
            } else {
                AGC_RELEASE_RATE
            }
            primaryState.currentAgcGain += (primaryState.targetAgcGain - primaryState.currentAgcGain) * pAgcSlew
            val pFinalMultiplier = primaryState.currentAgcGain * primaryState.currentDuckingMultiplier
            val pSample = primaryShorts.get(i).toInt()
            val pAdjusted = (pSample * pFinalMultiplier).toInt()
            primaryShorts.put(i, pAdjusted.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort())

            processDuckingState(secondaryState, secondaryDuckRequested, secondaryForceRelease)
            val sAgcSlew = if (secondaryState.targetAgcGain < secondaryState.currentAgcGain) {
                if (secondaryState.currentAgcGain - secondaryState.targetAgcGain > TRANSIENT_THRESHOLD) {
                    TRANSIENT_SLEW_RATE
                } else {
                    AGC_ATTACK_RATE
                }
            } else {
                AGC_RELEASE_RATE
            }
            secondaryState.currentAgcGain += (secondaryState.targetAgcGain - secondaryState.currentAgcGain) * sAgcSlew
            val sFinalMultiplier = secondaryState.currentAgcGain * secondaryState.currentDuckingMultiplier
            val sSample = secondaryShorts.get(i).toInt()
            val sAdjusted = (sSample * sFinalMultiplier).toInt()
            secondaryShorts.put(i, sAdjusted.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort())
        }
    }

    private fun processDuckingState(stream: StreamState, duckRequest: Boolean, forceRelease: Boolean) {
        if (forceRelease) {
            if (stream.state == DuckingState.ATTACK || stream.state == DuckingState.HOLD) {
                stream.state = DuckingState.RELEASE
            }
        } else if (duckRequest) {
            if (stream.state == DuckingState.IDLE || stream.state == DuckingState.RELEASE) {
                stream.state = DuckingState.ATTACK
            } else if (stream.state == DuckingState.HOLD) {
                stream.holdCounter = HOLD_SAMPLES
            }
        }

        when (stream.state) {
            DuckingState.IDLE -> stream.currentDuckingMultiplier = 1.0f
            DuckingState.ATTACK -> {
                stream.currentDuckingMultiplier -= ATTACK_STEP
                if (stream.currentDuckingMultiplier <= TARGET_DUCKING_MULTIPLIER) {
                    stream.currentDuckingMultiplier = TARGET_DUCKING_MULTIPLIER
                    stream.state = DuckingState.HOLD
                    stream.holdCounter = HOLD_SAMPLES
                }
            }
            DuckingState.HOLD -> {
                stream.currentDuckingMultiplier = TARGET_DUCKING_MULTIPLIER
                if (!duckRequest) {
                    stream.holdCounter--
                    if (stream.holdCounter <= 0) stream.state = DuckingState.RELEASE
                } else {
                    stream.holdCounter = HOLD_SAMPLES
                }
            }
            DuckingState.RELEASE -> {
                stream.currentDuckingMultiplier += RELEASE_STEP
                if (stream.currentDuckingMultiplier >= 1.0f) {
                    stream.currentDuckingMultiplier = 1.0f
                    stream.state = DuckingState.IDLE
                }
            }
        }
    }
}
