package moe.shizuku.manager.debloater

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.shizuku.manager.utils.runShellCommand

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
            val cmd = "pm uninstall -k --user 0 $packageName"
            val output = runShellCommand(cmd) ?: throw IllegalStateException("Failed to run shell command")
            if (output.contains("Success", ignoreCase = true)) {
                output
            } else {
                throw IllegalStateException(output)
            }
        }
    }

    suspend fun restoreUser0(packageName: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val cmd = "pm install-existing --user 0 $packageName"
            val output = runShellCommand(cmd) ?: throw IllegalStateException("Failed to run shell command")
            if (output.contains("installed", ignoreCase = true) || output.contains("Package", ignoreCase = true)) {
                output
            } else {
                throw IllegalStateException(output)
            }
        }
    }

    suspend fun disablePackage(packageName: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val cmd = "pm disable-user --user 0 $packageName"
            val output = runShellCommand(cmd) ?: throw IllegalStateException("Failed to run shell command")
            if (output.contains("new state", ignoreCase = true) || output.contains("disabled", ignoreCase = true)) {
                output
            } else {
                throw IllegalStateException(output)
            }
        }
    }

    suspend fun enablePackage(packageName: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val cmd = "pm enable --user 0 $packageName"
            val output = runShellCommand(cmd) ?: throw IllegalStateException("Failed to run shell command")
            if (output.contains("new state", ignoreCase = true) || output.contains("enabled", ignoreCase = true)) {
                output
            } else {
                throw IllegalStateException(output)
            }
        }
    }

    suspend fun clearData(packageName: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
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
