package moe.shizuku.manager.audio

import android.content.Context
import android.media.AudioManager
import android.app.NotificationManager
import org.json.JSONArray
import org.json.JSONObject

enum class SoundProfileKind {
    NORMAL,
    SILENT,
    VIBRATE,
    NIGHT,
    MEETING,
    CUSTOM
}

data class SoundProfile(
    val name: String,
    val kind: SoundProfileKind,
    val volumes: Map<Int, Int>,
    val ringerMode: Int? = null
)

data class SoundProfileResult(
    val applied: Boolean,
    val ringerModeApplied: Boolean,
    val restoredStreams: Int
)

/**
 * Stores and applies only audio state Android exposes through AudioManager.
 *
 * Ringer mode is optional because SILENT and VIBRATE can be blocked without notification-policy
 * access. Volume changes remain independently useful and are always attempted.
 */
object SoundProfileManager {

    private const val PREFS = "sound_profiles"
    private const val KEY_PROFILES = "profiles"
    private const val KEY_SNAPSHOT = "snapshot"

    fun builtIns(context: Context): List<SoundProfile> {
        val current = capture(context)
        return listOf(
            SoundProfile("Normal", SoundProfileKind.NORMAL, current.volumes, AudioManager.RINGER_MODE_NORMAL),
            SoundProfile("Silent", SoundProfileKind.SILENT, current.volumes, AudioManager.RINGER_MODE_SILENT),
            SoundProfile("Vibrate", SoundProfileKind.VIBRATE, current.volumes, AudioManager.RINGER_MODE_VIBRATE),
            SoundProfile("Night", SoundProfileKind.NIGHT, current.volumes.mapValues { (_, value) -> value / 2 }, AudioManager.RINGER_MODE_SILENT),
            SoundProfile("Meeting", SoundProfileKind.MEETING, current.volumes.mapValues { (_, value) -> value / 3 }, AudioManager.RINGER_MODE_VIBRATE)
        )
    }

    fun capture(context: Context): SoundProfile {
        val audio = context.getSystemService(AudioManager::class.java)
        val volumes = AudioMasterManager.STREAMS.associate { (stream, _) ->
            stream to runCatching { audio.getStreamVolume(stream) }.getOrDefault(0)
        }
        return SoundProfile(
            name = "Current",
            kind = SoundProfileKind.CUSTOM,
            volumes = volumes,
            ringerMode = runCatching { audio.ringerMode }.getOrNull()
        )
    }

    fun saveCustom(context: Context, profile: SoundProfile) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val stored = readCustom(prefs).filterNot { it.name == profile.name } + profile.copy(kind = SoundProfileKind.CUSTOM)
        prefs.edit().putString(KEY_PROFILES, encode(stored)).apply()
    }

    fun customProfiles(context: Context): List<SoundProfile> =
        readCustom(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE))

    fun saveSnapshot(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SNAPSHOT, encode(listOf(capture(context))))
            .apply()
    }

    fun restoreSnapshot(context: Context): SoundProfileResult {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val encoded = prefs.getString(KEY_SNAPSHOT, null) ?: return SoundProfileResult(false, false, 0)
        val profile = decode(encoded).firstOrNull() ?: return SoundProfileResult(false, false, 0)
        return apply(context, profile)
    }

    fun apply(context: Context, profile: SoundProfile): SoundProfileResult {
        val audio = context.getSystemService(AudioManager::class.java)
        var restored = 0
        profile.volumes.forEach { (stream, volume) ->
            val max = runCatching { audio.getStreamMaxVolume(stream) }.getOrDefault(0)
            if (volume !in 0..max) return@forEach
            if (runCatching { audio.setStreamVolume(stream, volume, 0); true }.getOrDefault(false)) {
                restored++
            }
        }

        val ringerApplied = if (profile.ringerMode == null) {
            true
        } else if (canChangeRingerMode(context)) {
            runCatching {
                audio.ringerMode = profile.ringerMode
                audio.ringerMode == profile.ringerMode
            }.getOrDefault(false)
        } else {
            false
        }
        return SoundProfileResult(
            applied = restored == profile.volumes.size && ringerApplied,
            ringerModeApplied = ringerApplied,
            restoredStreams = restored
        )
    }

    fun canChangeRingerMode(context: Context): Boolean {
        val audio = context.getSystemService(AudioManager::class.java)
        if (audio.isVolumeFixed) return false
        val notifications = context.getSystemService(NotificationManager::class.java)
        return runCatching { notifications.isNotificationPolicyAccessGranted }.getOrDefault(false)
    }

    private fun readCustom(prefs: android.content.SharedPreferences): List<SoundProfile> =
        decode(prefs.getString(KEY_PROFILES, null).orEmpty())

    private fun encode(profiles: List<SoundProfile>): String {
        val array = JSONArray()
        profiles.forEach { profile ->
            val volumes = JSONObject()
            profile.volumes.forEach { (stream, volume) -> volumes.put(stream.toString(), volume) }
            array.put(JSONObject().apply {
                put("name", profile.name)
                put("kind", profile.kind.name)
                put("volumes", volumes)
                profile.ringerMode?.let { put("ringerMode", it) }
            })
        }
        return array.toString()
    }

    private fun decode(raw: String): List<SoundProfile> = runCatching {
        if (raw.isBlank()) return@runCatching emptyList()
        val array = JSONArray(raw)
        (0 until array.length()).mapNotNull { index ->
            val item = array.optJSONObject(index) ?: return@mapNotNull null
            val volumesJson = item.optJSONObject("volumes") ?: return@mapNotNull null
            val volumes = buildMap {
                volumesJson.keys().forEach { key ->
                    val stream = key.toIntOrNull() ?: return@forEach
                    put(stream, volumesJson.optInt(key, -1))
                }
            }
            SoundProfile(
                name = item.optString("name").takeIf(String::isNotBlank) ?: return@mapNotNull null,
                kind = runCatching { SoundProfileKind.valueOf(item.optString("kind")) }
                    .getOrDefault(SoundProfileKind.CUSTOM),
                volumes = volumes,
                ringerMode = if (item.has("ringerMode")) item.optInt("ringerMode") else null
            )
        }
    }.getOrDefault(emptyList())
}
