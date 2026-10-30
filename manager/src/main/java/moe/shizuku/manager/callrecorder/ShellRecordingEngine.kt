package moe.shizuku.manager.callrecorder

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.ParcelFileDescriptor
import android.os.Process
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

internal object ShellWorkarounds {
    fun apply() {
        // Shell workarounds - no-op for basic integration
    }
}

internal class ShellRecordingEngine {
    private val recordingActive = AtomicBoolean(false)
    private val recordingPaused = AtomicBoolean(false)

    private var outputFile: ParcelFileDescriptor? = null
    private var currentPlan: RecordingPlan? = null
    private var recordingCallback: IRecordingCallback? = null

    private var primaryRecorder: AudioRecord? = null
    private var secondaryRecorder: AudioRecord? = null

    private var captureThread: Thread? = null
    private var encoderThread: Thread? = null
    private var muxer: MediaMuxer? = null
    private var encoder: MediaCodec? = null
    private var muxerStarted = false

    private val emptyQueue = ArrayBlockingQueue<AudioFrame>(16)
    private val readyQueue = ArrayBlockingQueue<AudioFrame>(16)

    private class AudioFrame(val hasSecondary: Boolean) {
        val primaryBuffer = ByteBuffer.allocateDirect(4096).order(ByteOrder.nativeOrder())
        val secondaryBuffer: ByteBuffer? = if (hasSecondary) ByteBuffer.allocateDirect(4096).order(ByteOrder.nativeOrder()) else null
        var primaryRead = 0
        var secondaryRead = 0
    }

    fun start(outputFile: ParcelFileDescriptor, plan: RecordingPlan, callback: IRecordingCallback) {
        synchronized(this) {
            if (!recordingActive.compareAndSet(false, true)) return
            this.outputFile = outputFile
            this.currentPlan = plan
            this.recordingCallback = callback
            recordingPaused.set(false)

            repeat(16) { emptyQueue.add(AudioFrame(hasSecondary = plan.secondary != null)) }

            primaryRecorder = createAudioRecord(plan.primary)
            secondaryRecorder = plan.secondary?.let { createAudioRecord(it) }

            primaryRecorder?.startRecording()
            secondaryRecorder?.startRecording()

            setupEncoder(plan)

            captureThread = thread(name = "CallRecorder-Capture", isDaemon = true) {
                Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)
                captureLoop()
            }

            encoderThread = thread(name = "CallRecorder-Encoder", isDaemon = true) {
                Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND)
                encoderLoop()
            }

            recordingCallback?.onRecordingStarted()
        }
    }

    private fun createAudioRecord(plan: CapturePlan): AudioRecord {
        val channelConfig = if (plan.channelCount > 1) {
            AudioFormat.CHANNEL_IN_STEREO
        } else {
            AudioFormat.CHANNEL_IN_MONO
        }
        val bufferSize = AudioRecord.getMinBufferSize(plan.sampleRate,
            channelConfig, AudioFormat.ENCODING_PCM_16BIT)
        val minBufferSize = if (bufferSize > 0) bufferSize else 4096
        return AudioRecord(
            plan.source.value,
            plan.sampleRate,
            channelConfig,
            AudioFormat.ENCODING_PCM_16BIT,
            minBufferSize
        )
    }

    private fun setupEncoder(plan: RecordingPlan) {
        val mime = plan.codec.mimeType
        val enc = MediaCodec.createEncoderByType(mime)
        val format = MediaFormat.createAudioFormat(mime, plan.primary.sampleRate,
            if (plan.isAudioChannelSplitEnabled) 2 else 1)
        format.setInteger(MediaFormat.KEY_BIT_RATE, plan.bitrate)
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
        enc.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        enc.start()
        this.encoder = enc

        val mux = MediaMuxer(outputFile!!.fileDescriptor, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        this.muxer = mux
        muxerStarted = false
    }

    private fun captureLoop() {
        val primary = checkNotNull(primaryRecorder)
        val secondary = secondaryRecorder

        while (recordingActive.get()) {
            val frame = emptyQueue.poll(5, TimeUnit.MILLISECONDS) ?: continue
            val pBuf = frame.primaryBuffer
            pBuf.clear()
            val primaryRead = primary.read(pBuf, 4096)
            if (primaryRead < 0) continue
            pBuf.limit(primaryRead); pBuf.position(0)
            frame.primaryRead = primaryRead

            var secondaryRead = 0
            if (secondary != null && frame.secondaryBuffer != null) {
                val sBuf = frame.secondaryBuffer!!
                sBuf.clear()
                secondaryRead = secondary.read(sBuf, 4096)
                if (secondaryRead > 0) { sBuf.limit(secondaryRead); sBuf.position(0) }
                frame.secondaryRead = secondaryRead
            }

            if (!recordingPaused.get()) {
                readyQueue.put(frame)
            } else {
                emptyQueue.put(frame)
            }
        }
    }

    private fun encoderLoop() {
        val plan = checkNotNull(currentPlan)
        val enc = checkNotNull(this.encoder)
        val mux = checkNotNull(this.muxer)
        val bufferInfo = android.media.MediaCodec.BufferInfo()
        var trackIndex = -1

        while (recordingActive.get() || !readyQueue.isEmpty()) {
            val frame = readyQueue.poll(100, TimeUnit.MILLISECONDS) ?: continue

            val inputBufIndex = enc.dequeueInputBuffer(10000)
            if (inputBufIndex >= 0) {
                val inBuf = enc.getInputBuffer(inputBufIndex)
                if (inBuf != null) {
                    inBuf.clear()
                    inBuf.put(frame.primaryBuffer)
                    enc.queueInputBuffer(inputBufIndex, 0, frame.primaryRead,
                        System.currentTimeMillis() * 1000, 0)
                }
            }

            val outputBufIndex = enc.dequeueOutputBuffer(bufferInfo, 10000)
            when {
                outputBufIndex >= 0 -> {
                    if (trackIndex < 0) {
                        trackIndex = mux.addTrack(enc.getOutputFormat())
                        mux.start()
                        muxerStarted = true
                    }
                    val outBuf = enc.getOutputBuffer(outputBufIndex)
                    if (outBuf != null) {
                        outBuf.position(bufferInfo.offset)
                        outBuf.limit(bufferInfo.offset + bufferInfo.size)
                        mux.writeSampleData(trackIndex, outBuf, bufferInfo)
                        enc.releaseOutputBuffer(outputBufIndex, false)
                    }
                }
                outputBufIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    // Format changed
                }
            }

            emptyQueue.put(frame)
        }
    }

    fun pause() {
        synchronized(this) {
            if (recordingActive.get()) recordingPaused.compareAndSet(false, true)
            recordingCallback?.onRecordingPaused()
        }
    }

    fun resume() {
        synchronized(this) {
            if (recordingActive.get()) recordingPaused.compareAndSet(true, false)
            recordingCallback?.onRecordingResumed()
        }
    }

    fun stop() {
        synchronized(this) {
            val wasActive = recordingActive.compareAndSet(true, false)
            if (!wasActive) return

            primaryRecorder?.stop(); primaryRecorder?.release(); primaryRecorder = null
            secondaryRecorder?.stop(); secondaryRecorder?.release(); secondaryRecorder = null

            captureThread?.join(2000); captureThread = null
            encoderThread?.join(4000); encoderThread = null

            encoder?.let { runCatching { it.stop() } }
            encoder?.release()
            encoder = null
            if (muxerStarted) muxer?.let { runCatching { it.stop() } }
            muxer?.release()
            muxer = null
            muxerStarted = false

            outputFile?.close(); outputFile = null
            currentPlan = null
            recordingPaused.set(false)

            recordingCallback?.onRecordingStopped()
        }
    }

    fun isRecording(): Boolean = recordingActive.get()
}
