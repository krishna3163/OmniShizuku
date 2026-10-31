package moe.shizuku.manager.callrecorder

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import java.io.FileDescriptor
import java.nio.ByteBuffer

/**
 * Encodes PCM audio into Opus or AAC using MediaCodec and writes to output using MediaMuxer.
 * Directly ported from the hardened ShizuCallRecorder engine.
 */
class AudioEncoder(
    outputFileDescriptor: FileDescriptor,
    private val codec: RecordingCodec,
    private val channelCount: Int,
    private val bitrate: Int
) {
    companion object {
        private const val TAG = "SCR:AudioEncoder"
        private const val CODEC_TIMEOUT_US = 1_000L
    }

    private val mediaCodec: MediaCodec = MediaCodec.createEncoderByType(codec.mimeType)
    private val mediaMuxer: MediaMuxer = MediaMuxer(outputFileDescriptor, codec.outputFormat)
    private val bufferInfo = MediaCodec.BufferInfo()

    private var codecStarted = false
    private var muxerStarted = false
    private var audioTrack = -1
    private var inputEnded = false
    private var released = false

    init {
        val format = MediaFormat.createAudioFormat(
            codec.mimeType,
            AudioConfig.SAMPLE_RATE,
            channelCount
        )

        format.setInteger(MediaFormat.KEY_BIT_RATE, bitrate)

        val maxInputBlockSize = (if (channelCount == 2) AudioConfig.STEREO_BLOCK_SIZE_BYTES else AudioConfig.BLOCK_SIZE_BYTES) * 2
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, maxInputBlockSize)

        // AAC Profile must only be applied to AAC, never to Opus or AMR
        if (codec.mimeType == MediaFormat.MIMETYPE_AUDIO_AAC) {
            format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
        }

        // Try VBR if supported
        runCatching {
            val codecCaps = mediaCodec.codecInfo.getCapabilitiesForType(codec.mimeType)
            val encoderCaps = codecCaps.encoderCapabilities
            if (encoderCaps?.isBitrateModeSupported(MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR) == true) {
                format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR)
            }
        }.onFailure {
            Log.d(TAG, "VBR not supported for ${codec.key}: ${it.message}")
        }

        mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        mediaCodec.start()
        codecStarted = true
    }

    fun encode(pcm: ByteBuffer, presentationTimeUs: Long) {
        if (released) return

        val frameSize = channelCount * AudioConfig.PCM_BYTES_PER_SAMPLE
        var sourceOffset = 0

        while (pcm.hasRemaining()) {
            drainOutput()

            val inputIndex = waitForInputBuffer()
            if (inputIndex < 0) break

            val inputBuffer = mediaCodec.getInputBuffer(inputIndex) ?: break
            inputBuffer.clear()

            val bytesToCopy = minOf(pcm.remaining(), inputBuffer.remaining())
            if (bytesToCopy == pcm.remaining()) {
                inputBuffer.put(pcm)
            } else {
                val oldLimit = pcm.limit()
                pcm.limit(pcm.position() + bytesToCopy)
                inputBuffer.put(pcm)
                pcm.limit(oldLimit)
            }

            val chunkFrameOffset = sourceOffset / frameSize
            val chunkPresentationTimeUs = presentationTimeUs + (chunkFrameOffset * 1_000_000L) / AudioConfig.SAMPLE_RATE

            mediaCodec.queueInputBuffer(
                inputIndex,
                0,
                bytesToCopy,
                chunkPresentationTimeUs,
                0
            )

            sourceOffset += bytesToCopy
        }
        drainOutput()
    }

    fun finish() {
        if (released || !codecStarted || inputEnded) return

        val inputIndex = waitForInputBuffer()
        if (inputIndex >= 0) {
            mediaCodec.queueInputBuffer(
                inputIndex,
                0,
                0,
                0,
                MediaCodec.BUFFER_FLAG_END_OF_STREAM
            )
            inputEnded = true
            drainOutput(waitForEndOfStream = true)
        }
    }

    fun release() {
        if (released) return
        released = true

        runCatching {
            if (codecStarted) mediaCodec.stop()
        }
        runCatching {
            mediaCodec.release()
        }
        codecStarted = false

        runCatching {
            if (muxerStarted) mediaMuxer.stop()
        }
        runCatching {
            mediaMuxer.release()
        }
        muxerStarted = false
        audioTrack = -1
    }

    private fun waitForInputBuffer(): Int {
        while (!released) {
            val inputIndex = mediaCodec.dequeueInputBuffer(CODEC_TIMEOUT_US)
            if (inputIndex >= 0) return inputIndex
            drainOutput()
        }
        return -1
    }

    private fun drainOutput(waitForEndOfStream: Boolean = false) {
        while (!released) {
            when (val outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, CODEC_TIMEOUT_US)) {
                MediaCodec.INFO_TRY_AGAIN_LATER -> {
                    if (waitForEndOfStream) continue
                    return
                }
                MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    if (muxerStarted) throw IllegalStateException("Format changed after muxer started")
                    audioTrack = mediaMuxer.addTrack(mediaCodec.outputFormat)
                    mediaMuxer.start()
                    muxerStarted = true
                    Log.i(TAG, "MediaMuxer started with track $audioTrack for ${codec.key}")
                }
                else -> {
                    if (outputIndex < 0) continue

                    try {
                        val outputBuffer = mediaCodec.getOutputBuffer(outputIndex)
                        if (outputBuffer != null && bufferInfo.size > 0 &&
                            (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0
                        ) {
                            writeSample(outputBuffer, bufferInfo)
                        }
                    } finally {
                        mediaCodec.releaseOutputBuffer(outputIndex, false)
                    }

                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        return
                    }
                }
            }
        }
    }

    private fun writeSample(buffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        if (!muxerStarted || audioTrack < 0) return
        mediaMuxer.writeSampleData(audioTrack, buffer, bufferInfo)
    }
}
