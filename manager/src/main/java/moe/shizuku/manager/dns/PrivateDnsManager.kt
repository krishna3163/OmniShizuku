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

        val modeApplied = runShellCommand("settings put global private_dns_mode ${mode.modeKey}") != null
        if (!modeApplied) return@withContext false
        normalizedHost == null ||
            runShellCommand("settings put global private_dns_specifier $normalizedHost") != null
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
