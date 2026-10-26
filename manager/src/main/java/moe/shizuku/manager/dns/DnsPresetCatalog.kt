package moe.shizuku.manager.dns

/** Public DNS-over-TLS profiles; hostnames and filtering categories are adapted from ADNS. */
internal data class DnsPreset(
    val name: String,
    val hostname: String,
    val description: String,
)

internal object DnsPresetCatalog {
    val presets = listOf(
        DnsPreset("Cloudflare Standard", "one.one.one.one", "Standard Cloudflare DNS-over-TLS"),
        DnsPreset("Cloudflare Security", "security.cloudflare-dns.com", "Cloudflare profile with malware blocking"),
        DnsPreset("Cloudflare Family", "family.cloudflare-dns.com", "Cloudflare profile with malware and adult content filtering"),
        DnsPreset("AdGuard Default", "dns.adguard-dns.com", "AdGuard profile with ad and tracker filtering"),
        DnsPreset("AdGuard Family", "family.adguard-dns.com", "AdGuard profile with family filtering and Safe Search"),
        DnsPreset("AdGuard Non-Filtering", "unfiltered.adguard-dns.com", "Unfiltered AdGuard DNS servers"),
        DnsPreset("Quad9 Secured", "dns.quad9.net", "Quad9 profile with malware blocking"),
        DnsPreset("Quad9 Secured with ECS", "dns11.quad9.net", "Quad9 malware blocking with EDNS Client Subnet"),
        DnsPreset("Quad9 Unsecured", "dns10.quad9.net", "Quad9 without threat blocking"),
        DnsPreset("OpenDNS Standard", "dns.opendns.com", "Standard OpenDNS resolver"),
        DnsPreset("OpenDNS FamilyShield", "dns.familyshield.opendns.com", "OpenDNS profile with adult content filtering"),
        DnsPreset("DNSforge Normal", "dnsforge.de", "DNSforge profile with ad, tracker, and malware filtering"),
        DnsPreset("DNSforge Clean", "clean.dnsforge.de", "DNSforge profile with family filtering and Safe Search"),
        DnsPreset("DNSforge Hard", "hard.dnsforge.de", "DNSforge stricter profile with ad, tracker, and malware filtering"),
        DnsPreset("DNSforge Blank", "blank.dnsforge.de", "DNSforge resolver without filtering"),
        DnsPreset("HaGeZi Root", "root.hagezi.org", "HaGeZi profile with ad, tracker, and malware filtering"),
        DnsPreset("HaGeZi Wurzn", "wurzn.hagezi.org", "HaGeZi profile with ad, tracker, and malware filtering"),
        DnsPreset("HaGeZi Juuri", "juuri.hagezi.org", "HaGeZi profile with ad, tracker, and malware filtering"),
        DnsPreset("HaGeZi CTIF", "ctif.hagezi.org", "HaGeZi profile focused on malware blocking"),
        DnsPreset("Google DNS", "dns.google", "Standard Google DNS-over-TLS"),
        DnsPreset("CleanBrowsing Family", "family-filter-dns.cleanbrowsing.org", "Safe Search and family filtering"),
    )
}
