package moe.shizuku.manager.callrecorder

import android.os.ParcelFileDescriptor
import android.os.Process
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/**
 * Shell recording engine managing the audio capture pipeline, encoder pipeline,
 * and state within the privileged Shizuku shell process.
 */
class ShellRecordingEngine {
    companion object {
        private const val TAG = "SCR:ShellRecordingEngine"
    }

    private val recordingActive = AtomicBoolean(false)
    private val recordingPaused = AtomicBoolean(false)

    private var outputFile: ParcelFileDescriptor? = null
    private var currentPlan: RecordingPlan? = null
    private var recordingCallback: IRecordingCallback? = null

    private var primaryCapture: AudioRecordCapture? = null
    private var secondaryCapture: AudioRecordCapture? = null

    private var captureThread: Thread? = null
    private var encoderThread: Thread? = null
    private var encoder: AudioEncoder? = null

    // 2 seconds buffer pool (100 frames at 20ms each)
    private val poolSize = 100
    private val emptyQueue = ArrayBlockingQueue<AudioFrame>(poolSize)
    private val readyQueue = ArrayBlockingQueue<AudioFrame>(poolSize)

    private val shutdownHook = Thread({
        if (recordingActive.get()) {
            Log.w(TAG, "Shell process dying unexpectedly with active session. Finalizing recording...")
            stop()
        }
    }, "CallRecorder-ShutdownHook")

    private class AudioFrame(hasSecondary: Boolean) {
        val primaryBuffer: ByteBuffer = ByteBuffer.allocateDirect(AudioConfig.BLOCK_SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
        val secondaryBuffer: ByteBuffer? = if (hasSecondary) {
            ByteBuffer.allocateDirect(AudioConfig.BLOCK_SIZE_BYTES).order(ByteOrder.nativeOrder())
        } else null
        var primaryRead: Int = 0
        var secondaryRead: Int = 0
    }

    fun isRecording(): Boolean = recordingActive.get()

    fun start(outputFile: ParcelFileDescriptor, plan: RecordingPlan, callback: IRecordingCallback) {
        synchronized(this) {
            if (!recordingActive.compareAndSet(false, true)) {
                Log.w(TAG, "start() ignored: recording is already active")
                return
            }

            try {
                this.outputFile = outputFile
                this.currentPlan = plan
                this.recordingCallback = callback
                this.recordingPaused.set(false)

                Log.i(TAG, "Starting recording session: codec=${plan.codec.name}, bitrate=${plan.bitrate}, primary=${plan.primary.source.name}, secondary=${plan.secondary?.source?.name}")

                emptyQueue.clear()
                readyQueue.clear()
                repeat(poolSize) {
                    emptyQueue.add(AudioFrame(hasSecondary = plan.secondary != null))
                }

                primaryCapture = AudioRecordCapture(plan.primary.source.value)
                secondaryCapture = plan.secondary?.let { AudioRecordCapture(it.source.value) }

                val outputChannels = if (plan.isAudioChannelSplitEnabled) 2 else 1
                encoder = AudioEncoder(
                    outputFileDescriptor = outputFile.fileDescriptor,
                    codec = plan.codec,
                    channelCount = outputChannels,
                    bitrate = plan.bitrate
                )

                primaryCapture?.start()
                secondaryCapture?.start()

                encoderThread = thread(name = "SCR-Encoder", isDaemon = true) {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND)
                    encoderLoop()
                }

                captureThread = thread(name = "SCR-Capture", isDaemon = true) {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)
                    captureLoop()
                }

                runCatching { Runtime.getRuntime().addShutdownHook(shutdownHook) }
                callback.onRecordingStarted()
                Log.i(TAG, "Native audio capture and encoding pipelines started successfully")
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to start recording session", t)
                runCatching { callback.onRecordingFailure(1, t.message ?: "Failed to start capture") }
                stop()
            }
        }
    }

    private fun captureLoop() {
        val primary = primaryCapture ?: return
        val secondary = secondaryCapture
        val trashPrimary = ByteBuffer.allocateDirect(AudioConfig.BLOCK_SIZE_BYTES).order(ByteOrder.nativeOrder())
        val trashSecondary = secondary?.let { ByteBuffer.allocateDirect(AudioConfig.BLOCK_SIZE_BYTES).order(ByteOrder.nativeOrder()) }

        try {
            while (recordingActive.get()) {
                val frame = emptyQueue.poll(5, TimeUnit.MILLISECONDS)
                val pBuf = frame?.primaryBuffer ?: trashPrimary
                val sBuf = frame?.secondaryBuffer ?: trashSecondary

                pBuf.clear()
                val primaryRead = primary.read(pBuf, AudioConfig.BLOCK_SIZE_BYTES)
                if (primaryRead < 0) {
                    Log.w(TAG, "Primary AudioRecord read error: $primaryRead")
                    continue
                }
                if (primaryRead > 0) {
                    pBuf.limit(primaryRead)
                    pBuf.position(0)
                }

                var secondaryRead = 0
                if (secondary != null && sBuf != null) {
                    sBuf.clear()
                    secondaryRead = secondary.read(sBuf, AudioConfig.BLOCK_SIZE_BYTES)
                    if (secondaryRead > 0) {
                        sBuf.limit(secondaryRead)
                        sBuf.position(0)
                    }
                }

                if (frame == null) {
                    // Encoder lagging, frame dropped to prevent overflow
                    continue
                }

                if (recordingPaused.get()) {
                    emptyQueue.put(frame)
                    continue
                }

                frame.primaryRead = primaryRead
                frame.secondaryRead = secondaryRead
                readyQueue.put(frame)
            }
        } catch (t: Throwable) {
            if (recordingActive.get()) {
                Log.e(TAG, "Capture loop exception", t)
                runCatching { recordingCallback?.onRecordingFailure(2, "Capture read failure") }
                stop()
            }
        }
    }

    private fun encoderLoop() {
        val plan = currentPlan ?: return
        val processor = PcmProcessor(plan)
        val clock = RecordingClock()

        val outputBuffer = ByteBuffer.allocateDirect(
            if (plan.isAudioChannelSplitEnabled) AudioConfig.STEREO_BLOCK_SIZE_BYTES else AudioConfig.BLOCK_SIZE_BYTES
        ).order(ByteOrder.nativeOrder())

        var blocksProcessed = 0L

        try {
            while (recordingActive.get() || !readyQueue.isEmpty()) {
                val frame = readyQueue.poll(100, TimeUnit.MILLISECONDS) ?: continue

                blocksProcessed++
                outputBuffer.clear()

                val processedBytes = processor.process(
                    primary = frame.primaryBuffer,
                    primaryBytes = frame.primaryRead,
                    secondary = frame.secondaryBuffer,
                    secondaryBytes = frame.secondaryRead,
                    output = outputBuffer
                )

                if (processedBytes <= 0) {
                    emptyQueue.put(frame)
                    continue
                }

                val presentationTimeUs = clock.currentPresentationTimeUs()
                val outputChannels = if (plan.isAudioChannelSplitEnabled) 2 else 1
                val outputFrameSize = outputChannels * AudioConfig.PCM_BYTES_PER_SAMPLE
                val frameCount = processedBytes / outputFrameSize

                outputBuffer.flip()
                encoder?.encode(outputBuffer, presentationTimeUs)
                clock.advance(frameCount.toLong())

                // Report progress every 50 blocks (approx 1 second)
                if (blocksProcessed % AudioConfig.LOG_INTERVAL_BLOCKS == 0L) {
                    val durationMs = presentationTimeUs / 1000L
                    val fileSizeBytes = blocksProcessed * processedBytes
                    runCatching {
                        recordingCallback?.onRecordingProgress(durationMs, fileSizeBytes)
                    }
                }

                emptyQueue.put(frame)
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Encoder loop exception", t)
            runCatching { recordingCallback?.onRecordingFailure(3, "Encoder failure") }
            stop()
        }
    }

    fun pause() {
        synchronized(this) {
            if (recordingActive.get() && recordingPaused.compareAndSet(false, true)) {
                Log.i(TAG, "Recording paused")
                runCatching { recordingCallback?.onRecordingPaused() }
            }
        }
    }

    fun resume() {
        synchronized(this) {
            if (recordingActive.get() && recordingPaused.compareAndSet(true, false)) {
                Log.i(TAG, "Recording resumed")
                runCatching { recordingCallback?.onRecordingResumed() }
            }
        }
    }

    fun stop() {
        synchronized(this) {
            val wasActive = recordingActive.compareAndSet(true, false)
            if (!wasActive && primaryCapture == null && encoder == null) return

            Log.i(TAG, "Stopping recording session...")

            runCatching { primaryCapture?.stop() }
            primaryCapture = null

            runCatching { secondaryCapture?.stop() }
            secondaryCapture = null

            val cThread = captureThread
            if (cThread != null && cThread !== Thread.currentThread()) {
                runCatching { cThread.join(1000L) }
            }
            captureThread = null

            val eThread = encoderThread
            if (eThread != null && eThread !== Thread.currentThread()) {
                runCatching { eThread.join(2000L) }
            }
            encoderThread = null

            runCatching { encoder?.finish() }
            runCatching { encoder?.release() }
            encoder = null
            currentPlan = null

            runCatching { outputFile?.close() }
            outputFile = null

            recordingPaused.set(false)
            runCatching { Runtime.getRuntime().removeShutdownHook(shutdownHook) }
            runCatching { recordingCallback?.onRecordingStopped() }
            recordingCallback = null
            Log.i(TAG, "Recording stopped and resources released")
        }
    }
}
