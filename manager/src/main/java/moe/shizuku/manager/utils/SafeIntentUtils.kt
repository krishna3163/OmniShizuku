package moe.shizuku.manager.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build

/**
 * Validates intents before starting activities, mitigating Intent Redirection
 * (CWE-926 / OWASP Mobile M1 / android_kotlin_intent_redirection).
 */
object SafeIntentUtils {

    val TRUSTED_INSTALLER_PACKAGES = setOf(
        "com.android.packageinstaller",
        "com.google.android.packageinstaller",
        "com.android.permissioncontroller",
        "com.google.android.permissioncontroller"
    )

    /**
     * Determines whether a package belongs to a trusted installer, system application,
     * or the host application itself.
     */
    fun isTrustedInstallerPackage(
        targetPackage: String?,
        isSystem: Boolean,
        isUpdatedSystem: Boolean,
        currentPackage: String
    ): Boolean {
        if (targetPackage.isNullOrEmpty()) return false
        return isSystem || isUpdatedSystem ||
                TRUSTED_INSTALLER_PACKAGES.contains(targetPackage) ||
                targetPackage == currentPackage
    }

    /**
     * Checks whether an intent received from PackageInstaller's STATUS_PENDING_USER_ACTION
     * resolves to a system application, a known platform package installer, or this app itself.
     */
    fun isSafeInstallerIntent(context: Context, intent: Intent?): Boolean {
        if (intent == null) return false

        val pm = context.packageManager
        val resolveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.resolveActivity(intent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()))
        } else {
            @Suppress("DEPRECATION")
            pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        } ?: return false

        val appInfo = resolveInfo.activityInfo?.applicationInfo ?: return false
        val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        val isUpdatedSystem = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
        val targetPackage = appInfo.packageName

        return isTrustedInstallerPackage(targetPackage, isSystem, isUpdatedSystem, context.packageName)
    }

    /**
     * Computes sanitized Intent flags:
     * - Strips URI permission grant flags that could unintentionally leak ContentProvider access
     * - Ensures FLAG_ACTIVITY_NEW_TASK is set
     */
    fun computeSanitizedFlags(originalFlags: Int): Int {
        val unsafeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
        return (originalFlags and unsafeFlags.inv()) or Intent.FLAG_ACTIVITY_NEW_TASK
    }

    /**
     * Sanitizes an installer confirmation intent before launching.
     */
    fun sanitizeInstallerIntent(intent: Intent): Intent {
        val sanitized = Intent(intent)
        sanitized.flags = computeSanitizedFlags(sanitized.flags)
        return sanitized
    }
}
