package moe.shizuku.manager.callrecorder

import android.os.ParcelFileDescriptor
import android.os.Process
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

/**
 * Shell recording engine managing the audio capture pipeline, encoder pipeline,
 * and state within the privileged Shizuku shell process.
 */
class ShellRecordingEngine {
    companion object {
        private const val TAG = "SCR:ShellRecordingEngine"
    }

    private enum class EngineState { IDLE, STARTING, RECORDING, PAUSED, STOPPING, ERROR }

    private val state = AtomicReference(EngineState.IDLE)
    private val recordingPaused = AtomicBoolean(false)

    private var outputFile: ParcelFileDescriptor? = null
    private var currentPlan: RecordingPlan? = null
    private var recordingCallback: IRecordingCallback? = null

    private var primaryCapture: AudioRecordCapture? = null
    private var secondaryCapture: AudioRecordCapture? = null

    @Volatile private var captureThread: Thread? = null
    @Volatile private var encoderThread: Thread? = null
    @Volatile private var encoder: AudioEncoder? = null

    // 2 seconds buffer pool (100 frames at 20ms each)
    private val poolSize = 100
    private val emptyQueue = ArrayBlockingQueue<AudioFrame>(poolSize)
    private val readyQueue = ArrayBlockingQueue<AudioFrame>(poolSize)

    private val shutdownHook = Thread({
        if (isActiveState()) {
            Log.w(TAG, "Shell process dying unexpectedly with active session. Finalizing recording...")
            stopInternal(notifyStopped = false)
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

    fun isRecording(): Boolean = isActiveState()

    private fun isActiveState(): Boolean = when (state.get()) {
        EngineState.STARTING, EngineState.RECORDING, EngineState.PAUSED, EngineState.STOPPING -> true
        else -> false
    }

    fun start(outputFile: ParcelFileDescriptor, plan: RecordingPlan, callback: IRecordingCallback) {
        synchronized(this) {
            val cur = state.get()
            if (cur != EngineState.IDLE && cur != EngineState.ERROR) {
                Log.w(TAG, "start() ignored: engine state=$cur")
                return
            }
            state.set(EngineState.STARTING)

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
                state.set(EngineState.RECORDING)
                callback.onRecordingStarted()
                Log.i(TAG, "Native audio capture and encoding pipelines started successfully")
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to start recording session", t)
                state.set(EngineState.ERROR)
                runCatching { callback.onRecordingFailure(1, t.message ?: "Failed to start capture") }
                stopInternal(notifyStopped = false)
            }
        }
    }

    private fun captureLoop() {
        val primary = primaryCapture ?: return
        val secondary = secondaryCapture
        val trashPrimary = ByteBuffer.allocateDirect(AudioConfig.BLOCK_SIZE_BYTES).order(ByteOrder.nativeOrder())
        val trashSecondary = secondary?.let { ByteBuffer.allocateDirect(AudioConfig.BLOCK_SIZE_BYTES).order(ByteOrder.nativeOrder()) }

        try {
            while (true) {
                val s = state.get()
                if (s == EngineState.IDLE || s == EngineState.ERROR) break
                if (s == EngineState.STOPPING) break

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

                if (frame == null) continue

                if (recordingPaused.get() || state.get() == EngineState.PAUSED) {
                    emptyQueue.offer(frame)
                    continue
                }

                frame.primaryRead = primaryRead
                frame.secondaryRead = secondaryRead
                if (!readyQueue.offer(frame, 5, TimeUnit.MILLISECONDS)) {
                    emptyQueue.offer(frame)
                }
            }
        } catch (t: Throwable) {
            if (isActiveState()) {
                Log.e(TAG, "Capture loop exception", t)
                state.set(EngineState.ERROR)
                runCatching { recordingCallback?.onRecordingFailure(2, "Capture read failure") }
                stopInternal(notifyStopped = false)
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
            while (true) {
                val s = state.get()
                val active = s == EngineState.STARTING || s == EngineState.RECORDING || s == EngineState.PAUSED || s == EngineState.STOPPING
                if (!active && readyQueue.isEmpty()) break

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
                    emptyQueue.offer(frame)
                    continue
                }

                val presentationTimeUs = clock.currentPresentationTimeUs()
                val outputChannels = if (plan.isAudioChannelSplitEnabled) 2 else 1
                val outputFrameSize = outputChannels * AudioConfig.PCM_BYTES_PER_SAMPLE
                val frameCount = processedBytes / outputFrameSize

                outputBuffer.flip()
                encoder?.encode(outputBuffer, presentationTimeUs)
                clock.advance(frameCount.toLong())

                if (blocksProcessed % AudioConfig.LOG_INTERVAL_BLOCKS == 0L) {
                    val durationMs = presentationTimeUs / 1000L
                    val fileSizeBytes = blocksProcessed * processedBytes
                    runCatching {
                        recordingCallback?.onRecordingProgress(durationMs, fileSizeBytes)
                    }
                }

                emptyQueue.offer(frame)
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Encoder loop exception", t)
            if (isActiveState()) {
                state.set(EngineState.ERROR)
                runCatching { recordingCallback?.onRecordingFailure(3, "Encoder failure") }
                stopInternal(notifyStopped = false)
            }
        }
    }

    fun pause() {
        synchronized(this) {
            if (state.compareAndSet(EngineState.RECORDING, EngineState.PAUSED)) {
                recordingPaused.set(true)
                Log.i(TAG, "Recording paused")
                runCatching { recordingCallback?.onRecordingPaused() }
            }
        }
    }

    fun resume() {
        synchronized(this) {
            if (state.compareAndSet(EngineState.PAUSED, EngineState.RECORDING)) {
                recordingPaused.set(false)
                Log.i(TAG, "Recording resumed")
                runCatching { recordingCallback?.onRecordingResumed() }
            }
        }
    }

    fun stop() {
        stopInternal(notifyStopped = true)
    }

    private fun stopInternal(notifyStopped: Boolean) {
        synchronized(this) {
            val prev = state.getAndSet(EngineState.STOPPING)
            if (prev == EngineState.IDLE && primaryCapture == null && encoder == null && outputFile == null) {
                state.set(EngineState.IDLE)
                return
            }

            Log.i(TAG, "Stopping recording session...")
            recordingPaused.set(false)

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

            emptyQueue.clear()
            readyQueue.clear()

            runCatching { Runtime.getRuntime().removeShutdownHook(shutdownHook) }

            val wasError = prev == EngineState.ERROR
            if (notifyStopped && !wasError) {
                runCatching { recordingCallback?.onRecordingStopped() }
            }
            if (!wasError) {
                state.set(EngineState.IDLE)
                recordingCallback = null
            } else {
                state.set(EngineState.ERROR)
            }
            Log.i(TAG, "Recording stopped and resources released (prev=$prev)")
        }
    }
}
