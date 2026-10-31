package moe.shizuku.manager.callrecorder

import android.annotation.SuppressLint
import android.app.Application
import android.app.Instrumentation
import android.content.AttributionSource
import android.content.Context
import android.content.pm.ApplicationInfo
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Looper
import android.os.Parcel
import android.util.Log
import java.lang.ref.WeakReference

@SuppressLint("PrivateApi", "DiscouragedPrivateApi", "SoonBlockedPrivateApi")
object ShellWorkarounds {
    private const val TAG = "SCR:Workarounds"

    private val activityThreadClass: Class<*> by lazy { Class.forName("android.app.ActivityThread") }

    private val activityThreadInstance: Any by lazy {
        try {
            val currentMethod = activityThreadClass.getDeclaredMethod("currentActivityThread")
            currentMethod.isAccessible = true
            currentMethod.invoke(null) ?: createActivityThread()
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to resolve ActivityThread", e)
            throw RuntimeException(e)
        }
    }

    private fun createActivityThread(): Any {
        val constructor = activityThreadClass.getDeclaredConstructor().apply { isAccessible = true }
        val instance = constructor.newInstance()

        activityThreadClass.getDeclaredField("sCurrentActivityThread").apply {
            isAccessible = true
            set(null, instance)
        }

        activityThreadClass.getDeclaredField("mSystemThread").apply {
            isAccessible = true
            setBoolean(instance, true)
        }
        return instance
    }

    fun apply() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                fillConfigurationController()
            }
            if (!Build.BRAND.equals("ONYX", ignoreCase = true)) {
                fillAppInfo()
            }
            fillAppContext()
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to apply full shell workarounds: ${t.message}")
        }
    }

    fun getSystemContext(): Context? {
        return try {
            val method = activityThreadClass.getDeclaredMethod("getSystemContext")
            method.isAccessible = true
            method.invoke(activityThreadInstance) as? Context
        } catch (e: Throwable) {
            Log.d(TAG, "Could not retrieve system context: ${e.message}")
            null
        }
    }

    private fun fillAppInfo() {
        try {
            val appBindDataClass = Class.forName("android.app.ActivityThread\$AppBindData")
            val appBindData = appBindDataClass.getDeclaredConstructor().apply { isAccessible = true }.newInstance()
            val appInfo = ApplicationInfo().apply { packageName = FakeContext.PACKAGE_NAME }
            appBindDataClass.getDeclaredField("appInfo").apply { isAccessible = true }.set(appBindData, appInfo)
            activityThreadClass.getDeclaredField("mBoundApplication").apply { isAccessible = true }.set(activityThreadInstance, appBindData)
        } catch (t: Throwable) {
            Log.d(TAG, "Could not fill app info: ${t.message}")
        }
    }

    private fun fillAppContext() {
        try {
            val app = Instrumentation.newApplication(Application::class.java, FakeContext.get())
            activityThreadClass.getDeclaredField("mInitialApplication").apply { isAccessible = true }.set(activityThreadInstance, app)
        } catch (t: Throwable) {
            Log.d(TAG, "Could not fill app context: ${t.message}")
        }
    }

    private fun fillConfigurationController() {
        try {
            val configControllerClass = Class.forName("android.app.ConfigurationController")
            val internalClass = Class.forName("android.app.ActivityThreadInternal")
            val controller = configControllerClass.getDeclaredConstructor(internalClass)
                .apply { isAccessible = true }
                .newInstance(activityThreadInstance)
            activityThreadClass.getDeclaredField("mConfigurationController")
                .apply { isAccessible = true }
                .set(activityThreadInstance, controller)
        } catch (t: Throwable) {
            Log.d(TAG, "Could not fill configuration controller: ${t.message}")
        }
    }

    fun createAudioRecordViaReflection(
        source: Int, sampleRate: Int, channelConfig: Int, channels: Int, channelMask: Int, encoding: Int
    ): AudioRecord {
        try {
            val audioRecordClass = AudioRecord::class.java
            val audioRecordConstructor = audioRecordClass.getDeclaredConstructor(Long::class.javaPrimitiveType).apply { isAccessible = true }
            val audioRecord = audioRecordConstructor.newInstance(0L)

            val mRecordingStateField = audioRecordClass.getDeclaredField("mRecordingState").apply { isAccessible = true }
            mRecordingStateField.set(audioRecord, AudioRecord.RECORDSTATE_STOPPED)

            val looper = Looper.myLooper() ?: Looper.getMainLooper()
            val mInitializationLooperField = audioRecordClass.getDeclaredField("mInitializationLooper").apply { isAccessible = true }
            mInitializationLooperField.set(audioRecord, looper)

            val audioAttributesBuilder = AudioAttributes.Builder()
            val setInternalCapturePresetMethod = AudioAttributes.Builder::class.java.getMethod("setInternalCapturePreset", Int::class.javaPrimitiveType)
            setInternalCapturePresetMethod.invoke(audioAttributesBuilder, source)

            when (source) {
                MediaRecorder.AudioSource.MIC,
                MediaRecorder.AudioSource.CAMCORDER,
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                MediaRecorder.AudioSource.UNPROCESSED,
                MediaRecorder.AudioSource.VOICE_PERFORMANCE -> {
                    runCatching {
                        val setPrivacySensitiveMethod = AudioAttributes.Builder::class.java.getMethod("setPrivacySensitive", Boolean::class.javaPrimitiveType)
                        setPrivacySensitiveMethod.invoke(audioAttributesBuilder, false)
                    }
                }
            }

            val attributes = audioAttributesBuilder.build()
            val mAudioAttributesField = audioRecordClass.getDeclaredField("mAudioAttributes").apply { isAccessible = true }
            mAudioAttributesField.set(audioRecord, attributes)

            val audioParamCheckMethod = audioRecordClass.getDeclaredMethod("audioParamCheck", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType).apply { isAccessible = true }
            audioParamCheckMethod.invoke(audioRecord, source, sampleRate, encoding)

            val mChannelCountField = audioRecordClass.getDeclaredField("mChannelCount").apply { isAccessible = true }
            mChannelCountField.set(audioRecord, channels)

            val mChannelMaskField = audioRecordClass.getDeclaredField("mChannelMask").apply { isAccessible = true }
            mChannelMaskField.set(audioRecord, channelMask)

            val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, encoding)
            val bufferSizeInBytes = if (minBufferSize > 0) minBufferSize * 8 else 4096
            val audioBuffSizeCheckMethod = audioRecordClass.getDeclaredMethod("audioBuffSizeCheck", Int::class.javaPrimitiveType).apply { isAccessible = true }
            audioBuffSizeCheckMethod.invoke(audioRecord, bufferSizeInBytes)

            val sampleRateArray = intArrayOf(sampleRate)
            val session = intArrayOf(AudioManager.AUDIO_SESSION_ID_GENERATE)
            val initResult: Int

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                val nativeSetupMethod = audioRecordClass.getDeclaredMethod(
                    "native_setup", Any::class.java, Any::class.java, IntArray::class.java,
                    Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType, IntArray::class.java, String::class.java, Long::class.javaPrimitiveType
                ).apply { isAccessible = true }

                initResult = nativeSetupMethod.invoke(
                    audioRecord, WeakReference(audioRecord), attributes, sampleRateArray,
                    channelMask, 0, audioRecord.audioFormat, bufferSizeInBytes, session,
                    FakeContext.PACKAGE_NAME, 0L
                ) as Int
            } else {
                val attributionSource = FakeContext.get().attributionSource
                val attributionSourceParcel: Parcel
                var closeableState: AutoCloseable? = null

                try {
                    val asScopedParcelStateMethod = runCatching {
                        AttributionSource::class.java.getDeclaredMethod("asScopedParcelState").apply { isAccessible = true }
                    }.getOrNull()

                    if (asScopedParcelStateMethod != null) {
                        val state = asScopedParcelStateMethod.invoke(attributionSource) as AutoCloseable
                        closeableState = state
                        val getParcelMethod = state.javaClass.getDeclaredMethod("getParcel")
                        attributionSourceParcel = getParcelMethod.invoke(state) as Parcel
                    } else {
                        attributionSourceParcel = Parcel.obtain()
                        attributionSource.writeToParcel(attributionSourceParcel, 0)
                        attributionSourceParcel.setDataPosition(0)
                    }

                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
                        val nativeSetupMethod = audioRecordClass.getDeclaredMethod(
                            "native_setup", Any::class.java, Any::class.java, IntArray::class.java,
                            Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType,
                            Int::class.javaPrimitiveType, IntArray::class.java, Parcel::class.java,
                            Long::class.javaPrimitiveType, Int::class.javaPrimitiveType
                        ).apply { isAccessible = true }

                        initResult = nativeSetupMethod.invoke(
                            audioRecord, WeakReference(audioRecord), attributes, sampleRateArray,
                            channelMask, 0, audioRecord.audioFormat, bufferSizeInBytes, session,
                            attributionSourceParcel, 0L, 0
                        ) as Int
                    } else {
                        val nativeSetupMethod = audioRecordClass.getDeclaredMethod(
                            "native_setup", Any::class.java, Any::class.java, IntArray::class.java,
                            Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType,
                            Int::class.javaPrimitiveType, IntArray::class.java, Parcel::class.java,
                            Long::class.javaPrimitiveType, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType
                        ).apply { isAccessible = true }

                        initResult = nativeSetupMethod.invoke(
                            audioRecord, WeakReference(audioRecord), attributes, sampleRateArray,
                            channelMask, 0, audioRecord.audioFormat, bufferSizeInBytes, session,
                            attributionSourceParcel, 0L, 0, 0
                        ) as Int
                    }
                } finally {
                    closeableState?.close()
                }
            }

            if (initResult != AudioRecord.SUCCESS) {
                throw RuntimeException("native_setup returned error code $initResult")
            }

            val mSampleRateField = audioRecordClass.getDeclaredField("mSampleRate").apply { isAccessible = true }
            mSampleRateField.set(audioRecord, sampleRateArray[0])

            val mSessionIdField = audioRecordClass.getDeclaredField("mSessionId").apply { isAccessible = true }
            mSessionIdField.set(audioRecord, session[0])

            val mStateField = audioRecordClass.getDeclaredField("mState").apply { isAccessible = true }
            mStateField.set(audioRecord, AudioRecord.STATE_INITIALIZED)

            return audioRecord
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create AudioRecord via reflection workaround", e)
            throw IllegalStateException("AudioRecord initialization reflection workaround failed", e)
        }
    }
}
