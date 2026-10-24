package eu.darken.porter.porsh

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PorshHostTest {

    @Test
    fun awaitExitCode_returnsAStatusPublishedAfterTheReaderIsWaiting() {
        val host = FakeHost(100)

        val observed = AtomicInteger(Int.MIN_VALUE)
        val reader = Thread { observed.set(host.awaitExitCode(10_000)) }
        reader.start()

        assertTrue(host.awaitEntered.await(10, TimeUnit.SECONDS))
        host.onExited(3)

        reader.join(TimeUnit.SECONDS.toMillis(10))
        assertFalse(reader.isAlive)
        assertEquals(3, observed.get())
    }

    @Test
    fun awaitExitCode_returnsMinusOneWhenTheProcessOutlivesTheTimeout() {
        val host = FakeHost(100)

        assertEquals(-1, host.awaitExitCode(50))
    }

    @Test
    fun hasExited_staysFalseUntilTheWaiterPublishes() {
        val host = FakeHost(100)

        assertFalse(host.hasExited())

        host.onExited(0)

        assertTrue(host.hasExited())
    }
}
