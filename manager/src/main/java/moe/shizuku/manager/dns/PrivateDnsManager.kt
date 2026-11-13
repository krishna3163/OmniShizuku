package moe.shizuku.manager.dns

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.shizuku.manager.utils.DnsHostnameValidator
import moe.shizuku.manager.utils.runShellCommand

object PrivateDnsManager {

    enum class DnsMode(val modeKey: String, val title: String) {
        OFF("off", "Off"),
        AUTOMATIC("opportunistic", "Automatic"),
        CUSTOM("hostname", "Private DNS Provider Hostname")
    }

    suspend fun getCurrentMode(): DnsMode = withContext(Dispatchers.IO) {
        val out = runShellCommand("settings get global private_dns_mode")?.trim()
        when (out) {
            "off" -> DnsMode.OFF
            "hostname" -> DnsMode.CUSTOM
            "opportunistic" -> DnsMode.AUTOMATIC
            else -> DnsMode.AUTOMATIC
        }
    }

    suspend fun getCurrentHostname(): String? = withContext(Dispatchers.IO) {
        val out = runShellCommand("settings get global private_dns_specifier")?.trim()
        if (out == "null" || out.isNullOrBlank()) null else out
    }

    suspend fun applyDns(mode: DnsMode, hostname: String? = null): Boolean = withContext(Dispatchers.IO) {
        val normalizedHost = if (mode == DnsMode.CUSTOM) {
            hostname?.let(DnsHostnameValidator::normalize) ?: return@withContext false
        } else {
            null
        }

        val previousMode = runShellCommand("settings get global private_dns_mode")?.trim()
        val previousHostRaw = runShellCommand("settings get global private_dns_specifier")?.trim()
            ?.takeUnless { it.isNullOrBlank() || it == "null" }
        val previousHost = previousHostRaw?.let(DnsHostnameValidator::normalize)
        if (previousMode !in setOf("off", "hostname", "opportunistic") ||
            (previousHostRaw != null && previousHost == null)
        ) {
            return@withContext false
        }

        fun commandSucceeded(command: String): Boolean =
            runShellCommand(command) != null

        val hostApplied = normalizedHost == null ||
            commandSucceeded("settings put global private_dns_specifier $normalizedHost")
        val modeApplied = hostApplied &&
            commandSucceeded("settings put global private_dns_mode ${mode.modeKey}")
        if (!modeApplied) {
            // Settings are two independent keys. Restore both when either write fails so a
            // failed provider change cannot leave the device in a half-applied state.
            commandSucceeded("settings put global private_dns_mode $previousMode")
            if (previousHost == null) {
                commandSucceeded("settings delete global private_dns_specifier")
            } else {
                commandSucceeded("settings put global private_dns_specifier $previousHost")
            }
            return@withContext false
        }

        val verifiedMode = runShellCommand("settings get global private_dns_mode")?.trim()
        val verifiedHost = runShellCommand("settings get global private_dns_specifier")?.trim()
            ?.takeUnless { it.isNullOrBlank() || it == "null" }
        verifiedMode == mode.modeKey &&
            (mode != DnsMode.CUSTOM || verifiedHost == normalizedHost)
    }

    suspend fun cycleNextDns(): String = withContext(Dispatchers.IO) {
        val current = getCurrentMode()
        val currentHost = getCurrentHostname()

        when {
            current == DnsMode.OFF -> {
                applyDns(DnsMode.AUTOMATIC)
                "Automatic"
            }
            current == DnsMode.AUTOMATIC -> {
                applyDns(DnsMode.CUSTOM, "one.one.one.one")
                "Cloudflare"
            }
            current == DnsMode.CUSTOM && currentHost == "one.one.one.one" -> {
                applyDns(DnsMode.CUSTOM, "dns.adguard-dns.com")
                "AdGuard"
            }
            current == DnsMode.CUSTOM && currentHost in setOf("dns.adguard-dns.com", "dns.adguard.com") -> {
                applyDns(DnsMode.OFF)
                "Off"
            }
            else -> {
                applyDns(DnsMode.AUTOMATIC)
                "Automatic"
            }
        }
    }
}
