package moe.shizuku.manager.callrecorder

import android.content.Context
import android.os.Binder
import android.os.ParcelFileDescriptor
import androidx.annotation.Keep
import moe.shizuku.manager.callrecorder.IRecordingCallback.Stub as RecordingCallbackStub
import moe.shizuku.manager.callrecorder.IShellService.Stub as ShellServiceStub
import moe.shizuku.manager.callrecorder.ILogCallback.Stub as LogCallbackStub
import java.util.concurrent.atomic.AtomicBoolean

@Keep
class ShellService @Keep constructor(context: Context?) : ShellServiceStub() {

    private val isInitialized = AtomicBoolean(false)
    private val recordingEngine by lazy { ShellRecordingEngine() }
    private var logCallback: ILogCallback? = null
    private var recordingCallback: IRecordingCallback? = null

    private fun ensureInitialized() {
        if (isInitialized.compareAndSet(false, true)) {
            ShellWorkarounds.apply()
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

    private fun clamp(value: Int, min: Int, max: Int): Int = value.coerceIn(min, max)

    override fun startRecording(
        outputFile: ParcelFileDescriptor,
        primarySource: Int,
        secondarySource: Int,
        channelSplit: Boolean,
        codec: Int,
        bitrate: Int
    ) {
        clearIdentity {
            ensureInitialized()
            val sources = AudioSource.values()
            val primaryIdx = clamp(primarySource, 0, sources.lastIndex)
            val secondaryIdx = if (secondarySource >= 0) clamp(secondarySource, 0, sources.lastIndex) else -1
            val plan = RecordingPlan(
                primary = CapturePlan(source = sources[primaryIdx]),
                secondary = if (secondaryIdx >= 0) CapturePlan(source = sources[secondaryIdx]) else null,
                isAudioChannelSplitEnabled = channelSplit,
                codec = RecordingCodec.fromOrdinal(codec),
                bitrate = bitrate
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
    }

    override fun stopRecording() {
        clearIdentity {
            ensureInitialized()
            recordingEngine.stop()
        }
    }

    override fun pauseRecording() {
        clearIdentity {
            ensureInitialized()
            recordingEngine.pause()
        }
    }

    override fun resumeRecording() {
        clearIdentity {
            ensureInitialized()
            recordingEngine.resume()
        }
    }

    override fun isRecording(): Boolean {
        return clearIdentity {
            ensureInitialized()
            recordingEngine.isRecording()
        }
    }

    override fun setLogCallback(callback: ILogCallback, redactionEnabled: Boolean) {
        clearIdentity {
            logCallback = callback
            ensureInitialized()
        }
    }

    override fun setRecordingCallback(callback: IRecordingCallback) {
        clearIdentity {
            recordingCallback = callback
        }
    }

    override fun destroy() {
        recordingEngine.stop()
        clearIdentity {
            logCallback = null
            recordingCallback = null
        }
        kotlin.system.exitProcess(0)
    }
}
