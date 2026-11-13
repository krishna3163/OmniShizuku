package moe.shizuku.manager.display

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class DisplayConfigurationStore(context: Context) {

    private val prefs = context.getSharedPreferences("display_configuration", Context.MODE_PRIVATE)

    fun savePending(pending: PendingDisplayChange) {
        prefs.edit().putString("pending", encodePending(pending)).apply()
    }

    fun pending(): PendingDisplayChange? =
        prefs.getString("pending", null)?.let(::decodePending)

    fun clearPending() {
        prefs.edit().remove("pending").apply()
    }

    fun saveProfiles(profiles: List<DisplayProfile>) {
        prefs.edit().putString("profiles", encodeProfiles(profiles)).apply()
    }

    fun profiles(): List<DisplayProfile> =
        prefs.getString("profiles", null)?.let(::decodeProfiles).orEmpty()

    fun appendHistory(snapshot: DisplaySnapshot) {
        val history = history().takeLast(19) + snapshot
        prefs.edit().putString("history", encodeSnapshots(history)).apply()
    }

    fun history(): List<DisplaySnapshot> =
        prefs.getString("history", null)?.let(::decodeSnapshots).orEmpty()

    private fun encodePending(value: PendingDisplayChange): String =
        JSONObject().apply {
            put("before", encodeSnapshot(value.before))
            put("deadline", value.deadlineMillis)
            put("state", value.state.name)
        }.toString()

    private fun decodePending(value: String): PendingDisplayChange? = runCatching {
        val item = JSONObject(value)
        PendingDisplayChange(
            before = decodeSnapshot(item.getString("before")),
            deadlineMillis = item.getLong("deadline"),
            state = DisplayRollbackState.valueOf(item.getString("state"))
        )
    }.getOrNull()

    private fun encodeProfiles(values: List<DisplayProfile>): String {
        val array = JSONArray()
        values.forEach { profile ->
            array.put(JSONObject().apply {
                put("name", profile.name)
                put("builtIn", profile.builtIn)
                put("change", encodeChange(profile.change))
            })
        }
        return array.toString()
    }

    private fun decodeProfiles(value: String): List<DisplayProfile> = runCatching {
        val array = JSONArray(value)
        (0 until array.length()).mapNotNull { index ->
            val item = array.optJSONObject(index) ?: return@mapNotNull null
            DisplayProfile(
                name = item.optString("name").takeIf(String::isNotBlank) ?: return@mapNotNull null,
                change = decodeChange(item.optString("change")),
                builtIn = item.optBoolean("builtIn")
            )
        }
    }.getOrDefault(emptyList())

    private fun encodeSnapshots(values: List<DisplaySnapshot>): String {
        val array = JSONArray()
        values.forEach { array.put(encodeSnapshot(it)) }
        return array.toString()
    }

    private fun decodeSnapshots(value: String): List<DisplaySnapshot> = runCatching {
        val array = JSONArray(value)
        (0 until array.length()).mapNotNull { index ->
            array.optString(index).takeIf(String::isNotBlank)?.let(::decodeSnapshot)
        }
    }.getOrDefault(emptyList())

    private fun encodeSnapshot(value: DisplaySnapshot): String =
        JSONObject().apply {
            put("size", value.size)
            put("density", value.density)
            put("min", value.minRefreshRate)
            put("peak", value.peakRefreshRate)
            put("mode", value.modeId)
            put("rate", value.currentRefreshRate)
            put("capturedAt", value.capturedAt)
        }.toString()

    private fun decodeSnapshot(value: String): DisplaySnapshot {
        val item = JSONObject(value)
        return DisplaySnapshot(
            size = item.optString("size").takeUnless { it == "null" || it.isBlank() },
            density = item.optInt("density").takeIf { it > 0 },
            minRefreshRate = item.optString("min").takeUnless { it == "null" || it.isBlank() },
            peakRefreshRate = item.optString("peak").takeUnless { it == "null" || it.isBlank() },
            modeId = item.optInt("mode").takeIf { it > 0 },
            currentRefreshRate = item.optDouble("rate").takeIf { !it.isNaN() }?.toFloat(),
            capturedAt = item.optLong("capturedAt")
        )
    }

    private fun encodeChange(value: DisplayChange): String =
        JSONObject().apply {
            put("size", value.size)
            put("density", value.density)
            put("min", value.minRefreshRate)
            put("peak", value.peakRefreshRate)
        }.toString()

    private fun decodeChange(value: String): DisplayChange {
        val item = JSONObject(value)
        return DisplayChange(
            size = item.optString("size").takeUnless { it == "null" || it.isBlank() },
            density = item.optInt("density").takeIf { it > 0 },
            minRefreshRate = item.optString("min").takeUnless { it == "null" || it.isBlank() },
            peakRefreshRate = item.optString("peak").takeUnless { it == "null" || it.isBlank() }
        )
    }
}
