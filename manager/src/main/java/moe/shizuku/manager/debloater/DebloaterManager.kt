package moe.shizuku.manager.debloater

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.shizuku.manager.utils.runShellCommand
import moe.shizuku.manager.utils.PackageNameValidator

object DebloaterManager {
    private const val TAG = "DebloaterManager"

    suspend fun getInstalledApps(context: Context): List<DebloatApp> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val uadDb = UadRepository.getDatabase(context)

        // Read uninstalled system packages via shell if available
        val uninstalledSystemPkgs = getUninstalledUser0Packages()

        val flags = PackageManager.GET_META_DATA or
                PackageManager.MATCH_UNINSTALLED_PACKAGES or
                PackageManager.MATCH_DISABLED_COMPONENTS

        val installedList = try {
            pm.getInstalledApplications(flags)
        } catch (e: Exception) {
            pm.getInstalledApplications(0)
        }

        val result = mutableListOf<DebloatApp>()
        val seenPackages = HashSet<String>()

        for (appInfo in installedList) {
            val pkg = appInfo.packageName
            seenPackages.add(pkg)

            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                    (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

            val state = when {
                uninstalledSystemPkgs.contains(pkg) -> AppState.UNINSTALLED
                !appInfo.enabled -> AppState.DISABLED
                else -> AppState.ACTIVE
            }

            val label = try {
                appInfo.loadLabel(pm).toString()
            } catch (e: Exception) {
                pkg
            }

            val icon = try {
                appInfo.loadIcon(pm)
            } catch (e: Exception) {
                null
            }

            val uadEntry = uadDb[pkg]

            result.add(
                DebloatApp(
                    packageName = pkg,
                    label = label,
                    isSystem = isSystem,
                    state = state,
                    uadEntry = uadEntry,
                    icon = icon
                )
            )
        }

        // Add any uninstalled system packages that were not in getInstalledApplications
        for (pkg in uninstalledSystemPkgs) {
            if (!seenPackages.contains(pkg)) {
                val uadEntry = uadDb[pkg]
                result.add(
                    DebloatApp(
                        packageName = pkg,
                        label = pkg.substringAfterLast('.').replaceFirstChar { it.uppercase() },
                        isSystem = true,
                        state = AppState.UNINSTALLED,
                        uadEntry = uadEntry,
                        icon = null
                    )
                )
            }
        }

        result.sortedWith(
            compareBy<DebloatApp> {
                when (it.uadEntry?.removal) {
                    UadRemoval.RECOMMENDED -> 0
                    UadRemoval.ADVANCED -> 1
                    UadRemoval.EXPERT -> 2
                    UadRemoval.UNSAFE -> 3
                    UadRemoval.SYSTEM -> 4
                    UadRemoval.UNKNOWN, null -> 5
                }
            }.thenBy { it.label.lowercase() }
        )
    }

    private fun getUninstalledUser0Packages(): Set<String> {
        val output = runShellCommand("pm list packages -u") ?: return emptySet()
        val allPkgs = output.lines()
            .mapNotNull { line ->
                val trimmed = line.trim()
                if (trimmed.startsWith("package:")) trimmed.removePrefix("package:") else null
            }
            .toSet()

        val activeOutput = runShellCommand("pm list packages") ?: return emptySet()
        val activePkgs = activeOutput.lines()
            .mapNotNull { line ->
                val trimmed = line.trim()
                if (trimmed.startsWith("package:")) trimmed.removePrefix("package:") else null
            }
            .toSet()

        return allPkgs - activePkgs
    }

    suspend fun uninstallUser0(packageName: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            require(PackageNameValidator.isValid(packageName)) { "Invalid package name" }
            // Try standard uninstall for user 0 with -k (keep data)
            val cmd1 = "pm uninstall -k --user 0 $packageName"
            val out1 = runShellCommand(cmd1)
            if (out1 != null && out1.contains("Success", ignoreCase = true)) {
                return@runCatching out1
            }

            // On Android 14+ / non-root shell, pm uninstall --user 0 on system apps may fail with
            // "only root can delete system app for a particular user" or "DELETE_FAILED_INTERNAL_ERROR"
            // For regular apps or updated system apps, pm uninstall without --user removes the update or app
            val cmd2 = "pm uninstall $packageName"
            val out2 = runShellCommand(cmd2)
            if (out2 != null && out2.contains("Success", ignoreCase = true)) {
                return@runCatching out2
            }

            // If system app cannot be uninstalled by non-root shell, fallback to disable-user --user 0
            val cmd3 = "pm disable-user --user 0 $packageName"
            val out3 = runShellCommand(cmd3)
            if (out3 != null && (out3.contains("new state", ignoreCase = true) || out3.contains("disabled", ignoreCase = true))) {
                return@runCatching "Disabled (System uninstall restricted on this Android version)"
            }

            // If disable-user also fails, try pm suspend
            val cmd4 = "pm suspend $packageName"
            val out4 = runShellCommand(cmd4)
            if (out4 != null && out4.contains("suspended", ignoreCase = true)) {
                return@runCatching "Suspended (System package protected)"
            }

            throw IllegalStateException(out1 ?: out2 ?: out3 ?: "Command failed without output")
        }
    }

    suspend fun restoreUser0(packageName: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            require(PackageNameValidator.isValid(packageName)) { "Invalid package name" }
            val cmd = "pm install-existing --user 0 $packageName"
            val output = runShellCommand(cmd)
            if (output != null && (output.contains("installed", ignoreCase = true) || output.contains("Package", ignoreCase = true))) {
                return@runCatching output
            }

            // Also ensure it is enabled and unsuspended
            runShellCommand("pm enable --user 0 $packageName")
            runShellCommand("pm unsuspend $packageName")

            if (output != null && output.contains("Success", ignoreCase = true)) {
                output
            } else if (output != null && !output.contains("Error", ignoreCase = true) && !output.contains("Exception", ignoreCase = true)) {
                output
            } else {
                throw IllegalStateException(output ?: "Failed to restore package")
            }
        }
    }

    suspend fun disablePackage(packageName: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            require(PackageNameValidator.isValid(packageName)) { "Invalid package name" }
            val cmd = "pm disable-user --user 0 $packageName"
            val output = runShellCommand(cmd)
            if (output != null && (output.contains("new state", ignoreCase = true) || output.contains("disabled", ignoreCase = true))) {
                return@runCatching output
            }

            // Fallback for non-disable packages: pm suspend
            val suspendCmd = "pm suspend $packageName"
            val suspendOut = runShellCommand(suspendCmd)
            if (suspendOut != null && suspendOut.contains("suspended", ignoreCase = true)) {
                return@runCatching "Suspended (Package cannot be fully disabled)"
            }

            throw IllegalStateException(output ?: suspendOut ?: "Failed to disable package")
        }
    }

    suspend fun enablePackage(packageName: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            require(PackageNameValidator.isValid(packageName)) { "Invalid package name" }
            // Try unsuspend first if it was suspended
            runShellCommand("pm unsuspend $packageName")

            val cmd = "pm enable --user 0 $packageName"
            val output = runShellCommand(cmd)
            if (output != null && (output.contains("new state", ignoreCase = true) || output.contains("enabled", ignoreCase = true))) {
                return@runCatching output
            }

            val cmdFallback = "pm enable $packageName"
            val outputFallback = runShellCommand(cmdFallback)
            if (outputFallback != null && (outputFallback.contains("new state", ignoreCase = true) || outputFallback.contains("enabled", ignoreCase = true))) {
                return@runCatching outputFallback
            }

            throw IllegalStateException(output ?: outputFallback ?: "Failed to enable package")
        }
    }

    suspend fun clearData(packageName: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            require(PackageNameValidator.isValid(packageName)) { "Invalid package name" }
            val cmd = "pm clear --user 0 $packageName"
            val output = runShellCommand(cmd) ?: throw IllegalStateException("Failed to run shell command")
            if (output.contains("Success", ignoreCase = true)) {
                output
            } else {
                throw IllegalStateException(output)
            }
        }
    }
}
