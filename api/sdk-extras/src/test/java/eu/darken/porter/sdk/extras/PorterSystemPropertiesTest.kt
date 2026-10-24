package eu.darken.porter.sdk.extras

import android.os.Bundle
import eu.darken.porter.protocol.PorterProtocol
import eu.darken.porter.protocol.PorterProtocol.CAPABILITIES_NONE
import eu.darken.porter.protocol.PorterProtocol.REPLY_CAPABILITIES
import eu.darken.porter.protocol.PorterProtocol.REPLY_MIN_PROTOCOL_VERSION
import eu.darken.porter.protocol.PorterProtocol.REPLY_PERMISSION_GRANTED
import eu.darken.porter.protocol.PorterProtocol.REPLY_PROTOCOL_VERSION
import eu.darken.porter.protocol.PorterProtocol.REPLY_SERVER_SECONTEXT
import eu.darken.porter.protocol.PorterProtocol.REPLY_SERVER_UID
import eu.darken.porter.protocol.PorterProtocol.REPLY_SHOULD_SHOW_REQUEST_PERMISSION_RATIONALE
import eu.darken.porter.sdk.Porter
import eu.darken.porter.sdk.PorterConnection
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** What the getters send as a default, and what they make of the answer. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
internal class PorterSystemPropertiesTest {

    private class Attached(val fake: FakeExtrasService, val connection: PorterConnection)

    @After
    fun teardown() {
        Porter.resetForTest()
    }

    private fun attached(): Attached {
        val reply = Bundle()
        reply.putInt(REPLY_PROTOCOL_VERSION, PorterProtocol.VERSION)
        reply.putInt(REPLY_MIN_PROTOCOL_VERSION, PorterProtocol.MIN_VERSION)
        reply.putInt(REPLY_SERVER_UID, 2000)
        reply.putString(REPLY_SERVER_SECONTEXT, "u:r:shell:s0")
        reply.putBoolean(REPLY_PERMISSION_GRANTED, true)
        reply.putBoolean(REPLY_SHOULD_SHOW_REQUEST_PERMISSION_RATIONALE, false)
        reply.putLong(REPLY_CAPABILITIES, CAPABILITIES_NONE)

        val fake = FakeExtrasService()
        fake.attachReply = reply
        Porter.onBinderReceived(fake, PACKAGE)
        return Attached(fake, Porter.connection.value!!)
    }

    @Test
    fun getIntDecodesTheFormsParseIntRejects() = runBlocking<Unit> {
        val (fake, connection) = attached().let { it.fake to it.connection }

        fake.propertyValue = "0x1f"
        assertEquals(31, connection.getSystemPropertyInt(KEY, 0))

        fake.propertyValue = "017"
        assertEquals(15, connection.getSystemPropertyInt(KEY, 0))
    }

    @Test
    fun getLongDecodesTheFormsParseLongRejects() = runBlocking<Unit> {
        val (fake, connection) = attached().let { it.fake to it.connection }

        fake.propertyValue = "0x1f"
        assertEquals(31L, connection.getSystemPropertyLong(KEY, 0))

        fake.propertyValue = "017"
        assertEquals(15L, connection.getSystemPropertyLong(KEY, 0))
    }

    @Test
    fun getBooleanReadsThePlatformsSpellings() = runBlocking<Unit> {
        val (fake, connection) = attached().let { it.fake to it.connection }

        for (value in listOf("1", "y", "yes", "on", "true")) {
            fake.propertyValue = value
            assertTrue(value, connection.getSystemPropertyBoolean(KEY, false))
        }
        for (value in listOf("0", "n", "no", "off", "false")) {
            fake.propertyValue = value
            assertFalse(value, connection.getSystemPropertyBoolean(KEY, true))
        }
    }

    @Test
    fun getBooleanFallsBackToTheDefaultForAnythingElse() = runBlocking<Unit> {
        val (fake, connection) = attached().let { it.fake to it.connection }

        for (value in listOf("TRUE", "2", "enabled")) {
            fake.propertyValue = value
            assertTrue(value, connection.getSystemPropertyBoolean(KEY, true))
            assertFalse(value, connection.getSystemPropertyBoolean(KEY, false))
        }

        fake.propertyValue = null
        assertTrue(connection.getSystemPropertyBoolean(KEY, true))
        assertEquals("the default travels as the server's own fallback", "true", fake.readDefault)
    }

    @Test
    fun getWithoutADefaultSendsNull() = runBlocking<Unit> {
        val (fake, connection) = attached().let { it.fake to it.connection }
        fake.propertyValue = "34"

        assertEquals("34", connection.getSystemProperty(KEY))

        assertEquals(KEY, fake.readName)
        assertNull(fake.readDefault)
    }

    @Test
    fun getSendsTheDefaultItWasGiven() = runBlocking<Unit> {
        val (fake, connection) = attached().let { it.fake to it.connection }

        assertEquals("fallback", connection.getSystemProperty(KEY, "fallback"))
        assertEquals("fallback", fake.readDefault)
    }

    @Test
    fun getIntEncodesTheDefaultAndReadsItBack() = runBlocking<Unit> {
        val (fake, connection) = attached().let { it.fake to it.connection }

        assertEquals(7, connection.getSystemPropertyInt(KEY, 7))
        assertEquals("7", fake.readDefault)
    }

    @Test
    fun setReachesTheServer() = runBlocking<Unit> {
        val (fake, connection) = attached().let { it.fake to it.connection }

        connection.setSystemProperty("persist.porter.test", "on")

        assertEquals("persist.porter.test", fake.writtenName)
        assertEquals("on", fake.writtenValue)
    }

    @Test
    fun aMalformedNumberReadsAsTheDefault() = runBlocking<Unit> {
        val (fake, connection) = attached().let { it.fake to it.connection }
        fake.propertyValue = "not-a-number"

        assertEquals(7, connection.getSystemPropertyInt(KEY, 7))
        assertEquals(7L, connection.getSystemPropertyLong(KEY, 7L))
    }

    private companion object {
        const val PACKAGE = "eu.darken.porter.probe"
        const val KEY = "ro.build.version.sdk"
    }
}
