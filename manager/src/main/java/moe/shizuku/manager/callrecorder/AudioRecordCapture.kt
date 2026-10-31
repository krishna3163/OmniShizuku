package moe.shizuku.manager.callrecorder

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.nio.ByteBuffer

/**
 * Handles capturing 16kHz PCM audio from an audio source using AudioRecord
 * in the Shizuku shell process environment.
 */
class AudioRecordCapture(private val audioSource: Int) {
    companion object {
        private const val TAG = "SCR:AudioRecordCapture"
    }

    private var recorder: AudioRecord? = null

    fun start() {
        startRecording()
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
        try {
            recorder = createAudioRecord(audioSource)
        } catch (e: Exception) {
            Log.w(TAG, "AudioRecord.Builder failed (${e.message}), attempting reflection fallback", e)
            recorder = ShellWorkarounds.createAudioRecordViaReflection(
                source = audioSource,
                sampleRate = AudioConfig.SAMPLE_RATE,
                channelConfig = AudioConfig.CAPTURE_CHANNEL_CONFIG,
                channels = AudioConfig.CAPTURE_CHANNEL_COUNT,
                channelMask = AudioConfig.CAPTURE_CHANNEL_CONFIG,
                encoding = AudioConfig.PCM_ENCODING
            )
        }

        recorder?.startRecording() ?: throw IllegalStateException("AudioRecord is null after initialization attempts")
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
}
