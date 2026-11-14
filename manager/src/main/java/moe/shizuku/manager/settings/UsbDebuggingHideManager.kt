package moe.shizuku.manager.settings

import android.app.AppOpsManager
import android.content.Context
import android.os.Process
import android.os.SystemClock
import android.provider.Settings
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.adb.AdbArm
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

object UsbDebuggingHideManager {

    private const val KEY_TARGET_PACKAGES = "usb_debugging_hide_packages"

    private val stateRef = AtomicReference(UsbDebuggingSnapshot())

    val snapshot: UsbDebuggingSnapshot
        get() = stateRef.get()

    val currentState: UsbDebuggingState
        get() = stateRef.get().state

    private fun storage(): UsbDebuggingJournalStorage =
        SharedPreferencesJournalStorage(ShizukuSettings.getPreferences())

    fun targetPackages(): Set<String> =
        ShizukuSettings.getPreferences()
            .getStringSet(KEY_TARGET_PACKAGES, emptySet())
            .orEmpty()
            .toSet()

    fun hasPendingRestore(): Boolean {
        val s = storage()
        return UsbDebuggingRecoveryJournal.read(s) != null
    }

    fun setTarget(context: Context, packageName: String, enabled: Boolean) {
        val targets = targetPackages().toMutableSet()
        if (enabled) targets.add(packageName) else targets.remove(packageName)
        ShizukuSettings.getPreferences().edit()
            .putStringSet(KEY_TARGET_PACKAGES, targets)
            .apply()
        UsbDebuggingHideService.refresh(context)
    }

    fun retainInstalledTargets(context: Context, installedPackages: Set<String>) {
        val targets = targetPackages()
        val retained = targets intersect installedPackages
        if (targets == retained) return
        ShizukuSettings.getPreferences().edit()
            .putStringSet(KEY_TARGET_PACKAGES, retained)
            .apply()
        UsbDebuggingHideService.refresh(context)
    }

    fun hasUsageAccess(context: Context): Boolean = runCatching {
        val appOps = context.getSystemService(AppOpsManager::class.java)
        appOps?.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        ) == AppOpsManager.MODE_ALLOWED
    }.getOrDefault(false)

    fun dispatchEvent(event: UsbDebuggingEvent) {
        stateRef.updateAndGet { current ->
            UsbDebuggingStateMachine.reduce(current, event)
        }
    }

    /**
     * Hides USB debugging by first persisting a recovery journal, executing the change,
     * and verifying the resulting state.
     */
    fun hide(context: Context, targetPackage: String = ""): Boolean = runCatching {
        val s = storage()
        val currentSetting = Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.ADB_ENABLED,
            0
        )

        // If USB debugging is already disabled, nothing to hide
        if (currentSetting == 0) {
            dispatchEvent(UsbDebuggingEvent.HideCompleted(verifiedSetting = 0))
            return@runCatching true
        }

        var record = UsbDebuggingRecoveryJournal.read(s)
        val opId = record?.operationId ?: UUID.randomUUID().toString()

        if (record == null) {
            record = UsbDebuggingJournalRecord(
                operationId = opId,
                schemaVersion = UsbDebuggingJournalRecord.CURRENT_SCHEMA_VERSION,
                originalSetting = currentSetting,
                requestedSetting = 0,
                lastAppliedSetting = currentSetting,
                phase = UsbDebuggingPhase.HIDING,
                timestampWallClockMs = System.currentTimeMillis(),
                timestampElapsedRealtimeMs = SystemClock.elapsedRealtime(),
                targetPackage = targetPackage,
                recoveryRequired = false
            )
            // Persist journal BEFORE making the system setting change
            if (!UsbDebuggingRecoveryJournal.write(s, record)) {
                dispatchEvent(UsbDebuggingEvent.JournalPersistenceFailed("Failed to commit journal"))
                return@runCatching false
            }
            dispatchEvent(UsbDebuggingEvent.JournalPersisted(opId, currentSetting, targetPackage))
        }

        // Apply disable command
        val success = AdbArm.setUsbDebuggingEnabled(context, false)
        val verified = Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.ADB_ENABLED,
            0
        )

        if (verified == 0) {
            // Update journal with verified result
            val updatedRecord = record.copy(
                phase = UsbDebuggingPhase.HIDDEN,
                lastAppliedSetting = 0,
                recoveryRequired = true
            )
            UsbDebuggingRecoveryJournal.write(s, updatedRecord)
            dispatchEvent(UsbDebuggingEvent.HideCompleted(verifiedSetting = 0))
            true
        } else {
            dispatchEvent(UsbDebuggingEvent.HideFailed("Verification failed; setting is still $verified"))
            false
        }
    }.getOrDefault(false)

    /**
     * Restores USB debugging safely using the persistent recovery journal.
     * Evaluates conflict policy: if the user manually re-enabled USB debugging externally,
     * the user's manual choice is respected without being overwritten.
     */
    fun restore(context: Context): Boolean = runCatching {
        val s = storage()
        val record = UsbDebuggingRecoveryJournal.read(s) ?: return@runCatching true

        val currentSetting = Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.ADB_ENABLED,
            0
        )

        // Evaluate conflict policy
        val conflict = UsbDebuggingRecoveryJournal.evaluateConflict(record, currentSetting)
        if (conflict is ConflictPolicyResult.UserOverrideDetected) {
            // User modified setting externally in Developer Options: respect user override!
            dispatchEvent(
                UsbDebuggingEvent.ExternalConflictDetected(
                    currentSetting = conflict.currentSetting,
                    expectedOriginal = conflict.expectedOriginal
                )
            )
            UsbDebuggingRecoveryJournal.clear(s)
            dispatchEvent(UsbDebuggingEvent.RestoreCompleted(verifiedSetting = currentSetting))
            return@runCatching true
        }

        dispatchEvent(UsbDebuggingEvent.RestoreRequested("Initiating restore"))

        // Update journal phase to RESTORING
        val restoringRecord = record.copy(phase = UsbDebuggingPhase.RESTORING)
        UsbDebuggingRecoveryJournal.write(s, restoringRecord)

        // If setting needs to be re-enabled
        if (currentSetting == 0 && record.originalSetting != 0) {
            val success = AdbArm.setUsbDebuggingEnabled(context, true)
            val verified = Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.ADB_ENABLED,
                0
            )
            if (verified == 0) {
                dispatchEvent(UsbDebuggingEvent.RestoreFailed("Setting still 0 after enable command"))
                return@runCatching false
            }
        }

        val finalVerified = Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.ADB_ENABLED,
            0
        )
        UsbDebuggingRecoveryJournal.clear(s)
        dispatchEvent(UsbDebuggingEvent.RestoreCompleted(verifiedSetting = finalVerified))
        true
    }.getOrDefault(false)

    /**
     * Recover from a previous crash or service kill when active journal exists.
     */
    fun recoverFromCrashIfPending(context: Context): Boolean {
        val s = storage()
        val record = UsbDebuggingRecoveryJournal.read(s) ?: return true

        dispatchEvent(
            UsbDebuggingEvent.ProcessDeathOrCrashDetected(
                operationId = record.operationId,
                originalSetting = record.originalSetting,
                phase = record.phase
            )
        )
        return restore(context)
    }
}
