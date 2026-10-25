package moe.shizuku.manager.audio

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.shizuku.manager.utils.runShellCommand

data class AudioStreamInfo(
    val streamType: Int,
    val name: String,
    val currentVolume: Int,
    val maxVolume: Int
)

data class AudioOutputDevice(
    val id: Int,
    val name: String,
    val typeName: String,
    val isSink: Boolean
)

object AudioMasterManager {

    val STREAMS = listOf(
        AudioManager.STREAM_MUSIC to "Media & Music",
        AudioManager.STREAM_RING to "Ring Tone",
        AudioManager.STREAM_NOTIFICATION to "Notifications",
        AudioManager.STREAM_ALARM to "Alarm",
        AudioManager.STREAM_VOICE_CALL to "Voice Call",
        AudioManager.STREAM_SYSTEM to "System Sounds"
    )

    fun getStreamVolumes(context: Context): List<AudioStreamInfo> {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return STREAMS.map { (stream, name) ->
            val curr = runCatching { am.getStreamVolume(stream) }.getOrDefault(0)
            val max = runCatching { am.getStreamMaxVolume(stream) }.getOrDefault(15)
            AudioStreamInfo(stream, name, curr, max)
        }
    }

    suspend fun setStreamVolume(context: Context, stream: Int, volume: Int): Boolean = withContext(Dispatchers.IO) {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val success = runCatching {
            am.setStreamVolume(stream, volume, 0)
            true
        }.getOrDefault(false)

        if (!success) {
            val cmd = runShellCommand("cmd media_session volume --stream $stream --set $volume")
            cmd != null
        } else {
            true
        }
    }

    fun getOutputDevices(context: Context): List<AudioOutputDevice> {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val devices = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        return devices.map { d ->
            val typeStr = when (d.type) {
                AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "Speaker"
                AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> "Earpiece"
                AudioDeviceInfo.TYPE_WIRED_HEADSET -> "Wired Headset"
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "Headphones"
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Bluetooth Audio"
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "Bluetooth Call"
                AudioDeviceInfo.TYPE_USB_DEVICE, AudioDeviceInfo.TYPE_USB_HEADSET -> "USB Audio"
                AudioDeviceInfo.TYPE_HDMI -> "HDMI"
                AudioDeviceInfo.TYPE_BLE_HEADSET, AudioDeviceInfo.TYPE_BLE_SPEAKER -> "BLE Audio"
                else -> "Audio Device (${d.type})"
            }
            val name = d.productName?.toString()?.takeIf { it.isNotBlank() } ?: typeStr
            AudioOutputDevice(d.id, name, typeStr, d.isSink)
        }
    }

    suspend fun grantCallRecordingAppOps(packageName: String): Boolean = withContext(Dispatchers.IO) {
        val cmd1 = runShellCommand("cmd appops set $packageName RECORD_AUDIO allow") != null
        val cmd2 = runShellCommand("cmd appops set $packageName READ_PHONE_STATE allow") != null
        runShellCommand("pm grant $packageName android.permission.RECORD_AUDIO")
        runShellCommand("pm grant $packageName android.permission.READ_PHONE_STATE")
        cmd1 && cmd2
    }
}
