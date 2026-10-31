package moe.shizuku.manager.callrecorder

import android.content.Context
import android.os.Binder
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.annotation.Keep
import moe.shizuku.manager.callrecorder.ILogCallback.Stub as LogCallbackStub
import moe.shizuku.manager.callrecorder.IRecordingCallback.Stub as RecordingCallbackStub
import moe.shizuku.manager.callrecorder.IShellService.Stub as ShellServiceStub
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.exitProcess

/**
 * Privileged ShellService running inside the Shizuku shell process (UID 2000).
 */
@Keep
class ShellService : ShellServiceStub {
    companion object {
        private const val TAG = "SCR:ShellService"
    }

    private val isInitialized = AtomicBoolean(false)
    private val recordingEngine by lazy { ShellRecordingEngine() }
    private var logCallback: ILogCallback? = null
    private var recordingCallback: IRecordingCallback? = null

    @Keep
    constructor() : this(null)

    @Keep
    constructor(context: Context?)

    private fun ensureInitialized() {
        if (isInitialized.compareAndSet(false, true)) {
            Log.d(TAG, "Initializing ShellService in UID ${android.os.Process.myUid()}...")
            ShellWorkarounds.apply()
            Log.d(TAG, "ShellWorkarounds applied.")
        }
    }

    private inline fun <T> clearIdentity(block: () -> T): T {
        val identityToken = Binder.clearCallingIdentity()
        val workSourceToken = Binder.clearCallingWorkSource()
        try {
            return block()
        } finally {
            Binder.restoreCallingWorkSource(workSourceToken)
            Binder.restoreCallingIdentity(identityToken)
        }
    }

    override fun startRecording(
        outputFile: ParcelFileDescriptor,
        primarySource: Int,
        secondarySource: Int,
        channelSplit: Boolean,
        codec: Int,
        bitrate: Int
    ) = clearIdentity {
        ensureInitialized()
        val primarySrc = AudioSource.fromOrdinal(primarySource)
        val secondarySrc = if (secondarySource >= 0) AudioSource.fromOrdinal(secondarySource) else null
        val targetCodec = RecordingCodec.fromOrdinal(codec)

        val plan = RecordingPlan(
            primary = CapturePlan(source = primarySrc),
            secondary = secondarySrc?.let { CapturePlan(source = it) },
            isAudioChannelSplitEnabled = channelSplit,
            codec = targetCodec,
            bitrate = if (bitrate > 0) bitrate else targetCodec.defaultBitrate
        )

        val cb = object : RecordingCallbackStub() {
            override fun onRecordingStarted() {
                clearIdentity { recordingCallback?.onRecordingStarted(); Unit }
            }
            override fun onRecordingStopped() {
                clearIdentity { recordingCallback?.onRecordingStopped(); Unit }
            }
            override fun onRecordingPaused() {
                clearIdentity { recordingCallback?.onRecordingPaused(); Unit }
            }
            override fun onRecordingResumed() {
                clearIdentity { recordingCallback?.onRecordingResumed(); Unit }
            }
            override fun onRecordingFailure(errorCode: Int, message: String) {
                clearIdentity { recordingCallback?.onRecordingFailure(errorCode, message); Unit }
            }
            override fun onRecordingProgress(durationMs: Long, fileSizeBytes: Long) {
                clearIdentity { recordingCallback?.onRecordingProgress(durationMs, fileSizeBytes); Unit }
            }
        }

        recordingEngine.start(outputFile, plan, cb)
    }

    override fun stopRecording() = clearIdentity {
        ensureInitialized()
        recordingEngine.stop()
    }

    override fun pauseRecording() = clearIdentity {
        ensureInitialized()
        recordingEngine.pause()
    }

    override fun resumeRecording() = clearIdentity {
        ensureInitialized()
        recordingEngine.resume()
    }

    override fun isRecording(): Boolean = clearIdentity {
        ensureInitialized()
        recordingEngine.isRecording()
    }

    override fun setLogCallback(callback: ILogCallback, redactionEnabled: Boolean) = clearIdentity {
        logCallback = callback
        ensureInitialized()
    }

    override fun setRecordingCallback(callback: IRecordingCallback) = clearIdentity {
        recordingCallback = callback
    }

    override fun destroy() {
        Log.i(TAG, "ShellService.destroy() called, terminating shell process")
        runCatching { recordingEngine.stop() }
        clearIdentity {
            logCallback = null
            recordingCallback = null
        }
        exitProcess(0)
    }
}
