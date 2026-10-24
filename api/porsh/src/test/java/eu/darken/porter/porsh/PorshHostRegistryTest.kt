package eu.darken.porter.porsh

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PorshHostRegistryTest {

    private val created = ConcurrentHashMap<Int, FakeHost>()
    private val now = AtomicLong(0)

    private val registry = PorshHostRegistry(
        { args, _, _, _, _, _, _ ->
            val pid = args[0].toInt()
            FakeHost(pid).also { created[pid] = it }
        },
        { now.get() },
    )

    /**
     * The registry keys by calling pid while the factory only sees the argv, so the pid
     * rides along in argv[0] to let a test tie a created host back to its client.
     */
    private fun createHost(pid: Int) {
        registry.createHost(pid, arrayOf(pid.toString()), null, null, 0, null, null, null)
    }

    @Test
    fun concurrentCreateHost_keepsEveryHostReachableByItsPid() {
        val clients = 64
        val go = CountDownLatch(1)
        val done = CountDownLatch(clients)
        val threads = ArrayList<Thread>()

        for (i in 0 until clients) {
            val pid = 1000 + i
            val thread = Thread {
                try {
                    go.await()
                    createHost(pid)
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                } finally {
                    done.countDown()
                }
            }
            threads.add(thread)
            thread.start()
        }

        go.countDown()
        assertTrue(done.await(30, TimeUnit.SECONDS))
        for (thread in threads) thread.join()

        assertEquals(clients, created.size)
        for (i in 0 until clients) {
            val pid = 1000 + i
            registry.setWindowSize(pid, pid.toLong())
            assertEquals(pid.toLong(), created[pid]!!.lastWindowSize)
        }
    }

    @Test
    fun createHost_replacesTheExistingHostOfTheSamePid() {
        createHost(100)
        val first = created[100]!!

        createHost(100)
        val second = created[100]!!

        registry.setWindowSize(100, 42)

        assertEquals(-1L, first.lastWindowSize)
        assertEquals(42L, second.lastWindowSize)
    }

    @Test
    fun anotherClientStarting_doesNotDiscardAnExitedButUncollectedHost() {
        createHost(100)
        created[100]!!.onExited(7)

        createHost(200)

        assertEquals(7, registry.getExitCode(100))
    }

    @Test
    fun collectionRemovesConditionally_soANewerHostOfTheSamePidSurvives() {
        createHost(100)
        val first = created[100]!!

        val collected = AtomicInteger(Int.MIN_VALUE)
        val collector = Thread { collected.set(registry.getExitCode(100)) }
        collector.start()

        // The collector now holds the first host and is parked on its latch. Install a
        // replacement under the same pid before releasing it.
        assertTrue(first.awaitEntered.await(10, TimeUnit.SECONDS))
        createHost(100)
        val second = created[100]!!
        first.onExited(7)

        collector.join(TimeUnit.SECONDS.toMillis(10))
        assertFalse(collector.isAlive)
        assertEquals(7, collected.get())

        registry.setWindowSize(100, 42)
        assertEquals(42L, second.lastWindowSize)
    }

    @Test
    fun createHost_reapsExitedHostsOnlyOnceTheyAreStale() {
        createHost(100)
        val stale = created[100]!!
        stale.onExited(7)
        stale.exitedAt = 0

        createHost(200)
        val recent = created[200]!!
        recent.onExited(8)
        recent.exitedAt = 2_000

        now.set(61_000)
        createHost(300)

        assertEquals(-1, registry.getExitCode(100))
        assertEquals(8, registry.getExitCode(200))
    }

    @Test
    fun createHost_doesNotWaitOnStillRunningHostsOfOtherClients() {
        for (i in 0 until 8) createHost(1000 + i)

        val startedAt = System.nanoTime()
        createHost(2000)
        val elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt)

        assertTrue(
            "createHost took ${elapsedMillis}ms, so it blocked on a running host",
            elapsedMillis < PorshHostRegistry.EXIT_CODE_TIMEOUT_MILLIS,
        )
    }

    @Test
    fun getExitCode_timingOutDoesNotEvictTheEntry() {
        val host = object : FakeHost(100) {
            override fun awaitExitCode(timeoutMillis: Long): Int = -1
        }
        val registry = PorshHostRegistry({ _, _, _, _, _, _, _ -> host }) { now.get() }
        registry.createHost(100, arrayOf("100"), null, null, 0, null, null, null)

        assertEquals(-1, registry.getExitCode(100))

        registry.setWindowSize(100, 42)
        assertEquals(42L, host.lastWindowSize)
    }

    @Test
    fun getExitCode_exitPublishedAfterTheWaitTimedOut_reportsItToTheSameCall() {
        val host = object : FakeHost(100) {
            override fun awaitExitCode(timeoutMillis: Long): Int {
                // The racing window: the timed wait has given up, and the host's exit is
                // published before the registry gets to read hasExited().
                onExited(7)
                return -1
            }
        }
        val registry = PorshHostRegistry({ _, _, _, _, _, _, _ -> host }) { now.get() }
        registry.createHost(100, arrayOf("100"), null, null, 0, null, null, null)

        assertEquals(
            "the status published during the wait window was reported as a timeout",
            7,
            registry.getExitCode(100),
        )

        registry.setWindowSize(100, 42)
        assertEquals("the collected host was left in the registry", -1L, host.lastWindowSize)
    }
}
