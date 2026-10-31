package moe.shizuku.manager.callrecorder

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.SystemClock
import android.util.Log
import moe.shizuku.manager.callrecorder.wrappers.ActivityManager
import java.nio.ByteBuffer

/**
 * Handles capturing 16kHz PCM audio from an audio source using AudioRecord
 * in the Shizuku shell process environment.
 * Incorporates Android 11 foreground workarounds and graceful hardware fallback.
 */
class AudioRecordCapture(private val audioSource: Int) {
    companion object {
        private const val TAG = "SCR:AudioRecordCapture"
    }

    private var recorder: AudioRecord? = null

    fun start() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R) {
            startWorkaroundAndroid11()
            try {
                tryStartRecording(attempts = 5, delayMs = 120L)
            } finally {
                stopWorkaroundAndroid11()
            }
        } else {
            startRecording()
        }
    }

    private fun tryStartRecording(attempts: Int, delayMs: Long) {
        var remainingAttempts = attempts
        while (remainingAttempts-- > 0) {
            SystemClock.sleep(delayMs)
            try {
                startRecording()
                return
            } catch (e: Exception) {
                if (remainingAttempts == 0) {
                    Log.e(TAG, "Failed to start audio capture after retries on Android 11", e)
                    throw e
                } else {
                    Log.d(TAG, "Retrying AudioRecord capture ($remainingAttempts left)...", e)
                }
            }
        }
    }

    fun read(buffer: ByteBuffer, size: Int): Int {
        val currentRecorder = recorder ?: return -1
        return currentRecorder.read(buffer, size, AudioRecord.READ_BLOCKING)
    }

    fun stop() {
        val rec = recorder
        recorder = null
        try {
            rec?.let {
                if (it.state == AudioRecord.STATE_INITIALIZED && it.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    it.stop()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping AudioRecord", e)
        } finally {
            rec?.release()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startRecording() {
        // First try the requested audio source with standard builder
        var candidate = runCatching { createAudioRecord(audioSource) }.getOrNull()
        if (candidate == null) {
            Log.w(TAG, "AudioRecord.Builder failed for source $audioSource, attempting reflection fallback")
            candidate = runCatching {
                ShellWorkarounds.createAudioRecordViaReflection(
                    source = audioSource,
                    sampleRate = AudioConfig.SAMPLE_RATE,
                    channelConfig = AudioConfig.CAPTURE_CHANNEL_CONFIG,
                    channels = AudioConfig.CAPTURE_CHANNEL_COUNT,
                    channelMask = AudioConfig.CAPTURE_CHANNEL_CONFIG,
                    encoding = AudioConfig.PCM_ENCODING
                )
            }.getOrNull()
        }

        // If specific call source (VOICE_COMMUNICATION / VOICE_CALL) failed on this device, fall back to standard MIC
        if (candidate == null && audioSource != MediaRecorder.AudioSource.MIC) {
            Log.w(TAG, "Source $audioSource failed, falling back to MIC source")
            candidate = runCatching { createAudioRecord(MediaRecorder.AudioSource.MIC) }.getOrNull()
                ?: runCatching {
                    ShellWorkarounds.createAudioRecordViaReflection(
                        source = MediaRecorder.AudioSource.MIC,
                        sampleRate = AudioConfig.SAMPLE_RATE,
                        channelConfig = AudioConfig.CAPTURE_CHANNEL_CONFIG,
                        channels = AudioConfig.CAPTURE_CHANNEL_COUNT,
                        channelMask = AudioConfig.CAPTURE_CHANNEL_CONFIG,
                        encoding = AudioConfig.PCM_ENCODING
                    )
                }.getOrNull()
        }

        recorder = candidate ?: throw IllegalStateException("AudioRecord initialization failed on this device")
        recorder?.startRecording()
    }

    @SuppressLint("MissingPermission")
    private fun createAudioRecord(source: Int): AudioRecord {
        val builder = AudioRecord.Builder()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setContext(FakeContext.get())
        }

        builder.setAudioSource(source)

        when (source) {
            MediaRecorder.AudioSource.MIC,
            MediaRecorder.AudioSource.CAMCORDER,
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            MediaRecorder.AudioSource.UNPROCESSED,
            MediaRecorder.AudioSource.VOICE_PERFORMANCE -> {
                runCatching {
                    builder.setPrivacySensitive(false)
                }
            }
        }

        builder.setAudioFormat(
            AudioFormat.Builder()
                .setEncoding(AudioConfig.PCM_ENCODING)
                .setSampleRate(AudioConfig.SAMPLE_RATE)
                .setChannelMask(AudioConfig.CAPTURE_CHANNEL_CONFIG)
                .build()
        )

        val minBufferSize = AudioRecord.getMinBufferSize(
            AudioConfig.SAMPLE_RATE,
            AudioConfig.CAPTURE_CHANNEL_CONFIG,
            AudioConfig.PCM_ENCODING
        )
        if (minBufferSize > 0) {
            builder.setBufferSizeInBytes(8 * minBufferSize)
        }

        return builder.build()
    }

    private fun startWorkaroundAndroid11() {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addCategory(Intent.CATEGORY_LAUNCHER)
                component = ComponentName(
                    FakeContext.PACKAGE_NAME,
                    "com.android.shell.HeapDumpActivity"
                )
            }
            ActivityManager.startActivity(intent)
            SystemClock.sleep(200)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch Android 11 foreground workaround", e)
        }
    }

    private fun stopWorkaroundAndroid11() {
        try {
            ActivityManager.forceStopPackage(FakeContext.PACKAGE_NAME)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop Android 11 foreground workaround", e)
        }
    }
}
