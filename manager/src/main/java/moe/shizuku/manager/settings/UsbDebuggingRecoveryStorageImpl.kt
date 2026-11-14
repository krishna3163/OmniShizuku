package moe.shizuku.manager.settings

import android.content.SharedPreferences

class SharedPreferencesJournalStorage(
    private val preferences: SharedPreferences
) : UsbDebuggingJournalStorage {

    override fun readRaw(): String? =
        preferences.getString(UsbDebuggingRecoveryJournal.KEY_RECOVERY_JOURNAL, null)

    override fun writeRaw(raw: String): Boolean =
        preferences.edit()
            .putString(UsbDebuggingRecoveryJournal.KEY_RECOVERY_JOURNAL, raw)
            .commit()

    override fun clear(): Boolean =
        preferences.edit()
            .remove(UsbDebuggingRecoveryJournal.KEY_RECOVERY_JOURNAL)
            .commit()

    override fun getLegacyOriginalEnabled(): Int? {
        return if (preferences.contains(UsbDebuggingRecoveryJournal.KEY_LEGACY_ORIGINAL_ENABLED)) {
            preferences.getInt(UsbDebuggingRecoveryJournal.KEY_LEGACY_ORIGINAL_ENABLED, 0)
        } else {
            null
        }
    }

    override fun clearLegacyOriginalEnabled(): Boolean =
        preferences.edit()
            .remove(UsbDebuggingRecoveryJournal.KEY_LEGACY_ORIGINAL_ENABLED)
            .commit()
}
