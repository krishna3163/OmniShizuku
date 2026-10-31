package moe.shizuku.manager.utils

import moe.shizuku.manager.dns.DnsPresetCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DnsHostnameValidatorTest {

    @Test
    fun normalizesValidDnsNames() {
        assertEquals("dns.google", DnsHostnameValidator.normalize(" dns.google "))
        assertEquals("one.one.one.one", DnsHostnameValidator.normalize("one.one.one.one."))
        assertEquals("xn--bcher-kva.example", DnsHostnameValidator.normalize("bücher.example"))
    }

    @Test
    fun rejectsShellSyntaxAndMalformedLabels() {
        listOf(
            "", "localhost", "127.0.0.1", "bad host", "example.com;id", "example.com..",
            "-bad.example", "bad-.example", "a..b"
        )
            .forEach { assertNull(it, DnsHostnameValidator.normalize(it)) }
    }

    @Test
    fun everyBundledProviderPresetHasAValidUniqueHostname() {
        val presets = DnsPresetCatalog.presets
        val normalized = presets.map { DnsHostnameValidator.normalize(it.hostname) }

        assertTrue(normalized.all { it != null })
        assertEquals(normalized.size, normalized.distinct().size)
    }
}
