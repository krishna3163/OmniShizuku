package moe.shizuku.manager.utils

import java.net.IDN
import java.util.Locale

/**
 * Validates and normalizes Private DNS hostnames before privileged shell use.
 * The parsing rules are adapted from ADNS `PrivateDnsHostname` (Eyal Meirom, MIT-licensed).
 */
internal object DnsHostnameValidator {

    fun normalize(value: String): String? {
        val input = value.trim()
        if (input.endsWith("..")) return null
        val candidate = input.removeSuffix(".").takeIf(String::isNotEmpty) ?: return null
        val ascii = try {
            IDN.toASCII(candidate, IDN.USE_STD3_ASCII_RULES).lowercase(Locale.ROOT)
        } catch (_: IllegalArgumentException) {
            return null
        }

        if (ascii.length > 253) return null
        val labels = ascii.split('.')
        if (labels.size < 2) return null
        if (labels.any { label ->
                label.isEmpty() || label.length > 63 ||
                    label.first() == '-' || label.last() == '-'
            }) return null
        if (ascii.all { it == '.' || it.isDigit() }) return null

        return ascii
    }
}
