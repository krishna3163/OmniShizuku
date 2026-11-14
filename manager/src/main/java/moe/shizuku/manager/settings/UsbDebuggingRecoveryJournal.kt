package moe.shizuku.manager.settings

import java.util.UUID

/**
 * Persistent recovery journal for USB Debugging Auto-Hide.
 * Ensures recovery after app crash, service kill, or device reboot.
 * Pure Kotlin data model and parser - zero Android SDK dependencies for deterministic JVM testing.
 */
data class UsbDebuggingJournalRecord(
    val operationId: String,
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val originalSetting: Int,
    val requestedSetting: Int,
    val lastAppliedSetting: Int,
    val phase: UsbDebuggingPhase,
    val timestampWallClockMs: Long,
    val timestampElapsedRealtimeMs: Long,
    val targetPackage: String,
    val recoveryRequired: Boolean
) {
    fun toJson(): String {
        return "{" +
                "\"operationId\":\"$operationId\"," +
                "\"schemaVersion\":$schemaVersion," +
                "\"originalSetting\":$originalSetting," +
                "\"requestedSetting\":$requestedSetting," +
                "\"lastAppliedSetting\":$lastAppliedSetting," +
                "\"phase\":\"${phase.name}\"," +
                "\"timestampWallClockMs\":$timestampWallClockMs," +
                "\"timestampElapsedRealtimeMs\":$timestampElapsedRealtimeMs," +
                "\"targetPackage\":\"$targetPackage\"," +
                "\"recoveryRequired\":$recoveryRequired" +
                "}"
    }

    companion object {
        const val CURRENT_SCHEMA_VERSION = 1

        private fun extractString(raw: String, key: String): String? {
            val pattern = Regex("\"$key\"\\s*:\\s*\"([^\"]*)\"")
            return pattern.find(raw)?.groupValues?.get(1)
        }

        private fun extractInt(raw: String, key: String): Int? {
            val pattern = Regex("\"$key\"\\s*:\\s*(-?\\d+)")
            return pattern.find(raw)?.groupValues?.get(1)?.toIntOrNull()
        }

        private fun extractLong(raw: String, key: String): Long? {
            val pattern = Regex("\"$key\"\\s*:\\s*(-?\\d+)")
            return pattern.find(raw)?.groupValues?.get(1)?.toLongOrNull()
        }

        private fun extractBoolean(raw: String, key: String): Boolean? {
            val pattern = Regex("\"$key\"\\s*:\\s*(true|false)")
            return pattern.find(raw)?.groupValues?.get(1)?.toBooleanStrictOrNull()
        }

        fun fromJson(raw: String): UsbDebuggingJournalRecord? = runCatching {
            val opId = extractString(raw, "operationId") ?: return@runCatching null
            val schema = extractInt(raw, "schemaVersion") ?: 1
            val original = extractInt(raw, "originalSetting") ?: return@runCatching null
            val requested = extractInt(raw, "requestedSetting") ?: 0
            val applied = extractInt(raw, "lastAppliedSetting") ?: 0
            val phaseStr = extractString(raw, "phase") ?: UsbDebuggingPhase.IDLE.name
            val phase = runCatching { UsbDebuggingPhase.valueOf(phaseStr) }.getOrDefault(UsbDebuggingPhase.IDLE)
            val wallClock = extractLong(raw, "timestampWallClockMs") ?: 0L
            val elapsed = extractLong(raw, "timestampElapsedRealtimeMs") ?: 0L
            val pkg = extractString(raw, "targetPackage") ?: ""
            val rec = extractBoolean(raw, "recoveryRequired") ?: false

            UsbDebuggingJournalRecord(
                operationId = opId,
                schemaVersion = schema,
                originalSetting = original,
                requestedSetting = requested,
                lastAppliedSetting = applied,
                phase = phase,
                timestampWallClockMs = wallClock,
                timestampElapsedRealtimeMs = elapsed,
                targetPackage = pkg,
                recoveryRequired = rec
            )
        }.getOrNull()

        fun migrateFromLegacy(originalEnabled: Int): UsbDebuggingJournalRecord {
            val now = System.currentTimeMillis()
            return UsbDebuggingJournalRecord(
                operationId = "legacy-${UUID.randomUUID()}",
                schemaVersion = CURRENT_SCHEMA_VERSION,
                originalSetting = originalEnabled,
                requestedSetting = 0,
                lastAppliedSetting = 0,
                phase = UsbDebuggingPhase.HIDDEN,
                timestampWallClockMs = now,
                timestampElapsedRealtimeMs = 0L,
                targetPackage = "legacy_migrated",
                recoveryRequired = true
            )
        }
    }
}

sealed interface ConflictPolicyResult {
    data object NoConflict : ConflictPolicyResult
    data class UserOverrideDetected(val currentSetting: Int, val expectedOriginal: Int) : ConflictPolicyResult
}

interface UsbDebuggingJournalStorage {
    fun readRaw(): String?
    fun writeRaw(raw: String): Boolean
    fun clear(): Boolean
    fun getLegacyOriginalEnabled(): Int?
    fun clearLegacyOriginalEnabled(): Boolean
}

object UsbDebuggingRecoveryJournal {

    const val KEY_RECOVERY_JOURNAL = "usb_debugging_hide_journal_v1"
    const val KEY_LEGACY_ORIGINAL_ENABLED = "usb_debugging_hide_original_enabled"

    fun evaluateConflict(record: UsbDebuggingJournalRecord, currentSystemSetting: Int): ConflictPolicyResult {
        // If we hid USB debugging (applied 0), but the current system setting is 1 (enabled),
        // the user has explicitly re-enabled USB debugging in developer options while hidden!
        if (record.phase == UsbDebuggingPhase.HIDDEN && record.lastAppliedSetting == 0 && currentSystemSetting != 0) {
            return ConflictPolicyResult.UserOverrideDetected(
                currentSetting = currentSystemSetting,
                expectedOriginal = record.originalSetting
            )
        }
        return ConflictPolicyResult.NoConflict
    }

    fun read(storage: UsbDebuggingJournalStorage): UsbDebuggingJournalRecord? {
        val raw = storage.readRaw()
        if (!raw.isNullOrBlank()) {
            val record = UsbDebuggingJournalRecord.fromJson(raw)
            if (record != null) return record
        }

        // Migrate legacy preference if present
        val legacy = storage.getLegacyOriginalEnabled()
        if (legacy != null) {
            val migrated = UsbDebuggingJournalRecord.migrateFromLegacy(legacy)
            storage.writeRaw(migrated.toJson())
            storage.clearLegacyOriginalEnabled()
            return migrated
        }
        return null
    }

    fun write(storage: UsbDebuggingJournalStorage, record: UsbDebuggingJournalRecord): Boolean {
        val ok = storage.writeRaw(record.toJson())
        if (ok) {
            storage.clearLegacyOriginalEnabled()
        }
        return ok
    }

    fun clear(storage: UsbDebuggingJournalStorage): Boolean {
        storage.clearLegacyOriginalEnabled()
        return storage.clear()
    }
}
