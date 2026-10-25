package moe.shizuku.manager.dns

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.shizuku.manager.utils.runShellCommand

object PrivateDnsManager {

    enum class DnsMode(val modeKey: String, val title: String) {
        OFF("off", "Off"),
        AUTOMATIC("opportunistic", "Automatic"),
        CUSTOM("hostname", "Private DNS Provider Hostname")
    }

    data class DnsPreset(
        val name: String,
        val hostname: String,
        val description: String
    )

    val PRESETS = listOf(
        DnsPreset("Cloudflare (1.1.1.1)", "one.one.one.one", "Fast, private DNS with no logs"),
        DnsPreset("Cloudflare Family", "family.cloudflare-dns.com", "Blocks malware and adult content"),
        DnsPreset("AdGuard DNS", "dns.adguard.com", "Blocks ads, trackers, and malicious domains"),
        DnsPreset("AdGuard Non-Filtering", "unfiltered.adguard-dns.com", "Unfiltered AdGuard DNS servers"),
        DnsPreset("Quad9 DNS", "dns.quad9.net", "Blocks malware and phishing with threat intelligence"),
        DnsPreset("Google DNS", "dns.google", "High availability public DNS"),
        DnsPreset("CleanBrowsing Family", "family-filter-dns.cleanbrowsing.org", "Safe search and family filtering")
    )

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
        var success = runShellCommand("settings put global private_dns_mode ${mode.modeKey}") != null
        if (mode == DnsMode.CUSTOM && !hostname.isNullOrBlank()) {
            success = success && (runShellCommand("settings put global private_dns_specifier ${hostname.trim()}") != null)
        }
        success
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
                applyDns(DnsMode.CUSTOM, "dns.adguard.com")
                "AdGuard"
            }
            current == DnsMode.CUSTOM && currentHost == "dns.adguard.com" -> {
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
