package eu.darken.porter.sdk.extras

import android.os.Bundle
import eu.darken.porter.protocol.PorterProtocol
import eu.darken.porter.protocol.PorterProtocol.REPLY_MIN_PROTOCOL_VERSION
import eu.darken.porter.protocol.PorterProtocol.REPLY_PERMISSION_GRANTED
import eu.darken.porter.protocol.PorterProtocol.REPLY_PROTOCOL_VERSION
import eu.darken.porter.protocol.PorterProtocol.REPLY_SERVER_UID
import eu.darken.porter.sdk.Porter
import eu.darken.porter.sdk.PorterConnection
import eu.darken.porter.sdk.PorterSecurityException
import eu.darken.porter.sdk.extras.internal.PorterShellService
import eu.darken.porter.server.IPorterServiceConnection
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Commands run through the built-in shell service, here in the test's own process. Robolectric's
 * pipes are files that read as ended wherever the writer has not got to yet, so what a command
 * writes is checked on a device, in the instrumented tests; these check what an exit code shows.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
internal class PorterShellTest {

    /** A server that connects every bind to one shell service, and counts the binds. */
    private class ShellServer : FakeExtrasService() {

        val service = PorterShellService()

        @Volatile
        var adds = 0

        @Volatile
        var refuse = false

        override fun addUserService(conn: IPorterServiceConnection?, args: Bundle): Int {
            if (refuse) throw SecurityException("not granted")
            adds++
            conn?.connected(service)
            return 0
        }
    }

    private lateinit var server: ShellServer
    private lateinit var connection: PorterConnection

    @Before
    fun setup() {
        server = ShellServer()
        server.attachReply = Bundle().apply {
            putInt(REPLY_PROTOCOL_VERSION, PorterProtocol.VERSION)
            putInt(REPLY_MIN_PROTOCOL_VERSION, PorterProtocol.MIN_VERSION)
            putInt(REPLY_SERVER_UID, 2000)
            putBoolean(REPLY_PERMISSION_GRANTED, true)
        }
        Porter.onBinderReceived(server, PACKAGE)
        connection = Porter.connection.value!!
    }

    @After
    fun teardown() {
        Porter.resetForTest()
    }

    /**
     * Blocks this thread, the main one, on [block], as an app bridging to the SDK from blocking code
     * does: binding the shell service must not need the main looper to run.
     */
    private fun <T> onMain(timeoutMs: Long = 10_000, block: suspend () -> T): T =
        runBlocking { withTimeout(timeoutMs) { block() } }

    @Test
    fun execReturnsTheExitCode() {
        val result = onMain { connection.exec("sh", "-c", "exit 3") }

        assertEquals(3, result.exitCode)
    }

    @Test
    fun execRunsInTheGivenDirectory() {
        val dir = java.nio.file.Files.createTempDirectory("porter-shell").toFile()
        try {
            val result = onMain {
                connection.exec("sh", "-c", "[ \"$(pwd -P)\" = '${dir.canonicalPath}' ]", dir = dir.absolutePath)
            }

            assertEquals(0, result.exitCode)
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun aBindCompletesWhileEveryDefaultWorkerWaitsForIt() {
        // The default size of Dispatchers.Default's pool.
        val workers = Runtime.getRuntime().availableProcessors().coerceAtLeast(2)
        val started = CountDownLatch(workers)
        val release = CountDownLatch(1)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        try {
            repeat(workers) {
                scope.launch {
                    started.countDown()
                    release.await()
                }
            }
            assertTrue("not every Default worker got busy", started.await(5, TimeUnit.SECONDS))

            val result = onMain { connection.exec("true") }

            assertEquals(0, result.exitCode)
        } finally {
            release.countDown()
            scope.cancel()
        }
    }

    @Test
    fun callsShareOneBinding() {
        onMain { connection.exec("true") }
        onMain { connection.exec("true") }

        assertEquals(1, server.adds)
    }

    @Test
    fun aCommandThatIsNotFoundExitsWith127() {
        val result = onMain { connection.exec("/nonexistent/porter-shell-test") }

        assertEquals(127, result.exitCode)
    }

    @Test
    fun aDirectoryThatDoesNotExistThrows() {
        try {
            onMain { connection.exec("true", dir = "/nonexistent/porter-shell-test") }
            fail("a command started in a directory that does not exist")
        } catch (e: PorterShellException) {
            // expected
        }
    }

    @Test
    fun anEmptyCommandIsRefusedWithoutBinding() {
        try {
            onMain { connection.exec() }
            fail("an empty command was run")
        } catch (e: IllegalArgumentException) {
            // expected
        }
        assertEquals(0, server.adds)
    }

    /** A refusal is the SDK's own exception, as for any user service, not a shell failure. */
    @Test
    fun aRefusedBindThrowsTheSdksSecurityException() {
        server.refuse = true
        try {
            onMain { connection.exec("true") }
            fail("a refused bind ran the command")
        } catch (e: PorterSecurityException) {
            // expected
        }
    }

    @Test
    fun cancellingReturnsAtOnceAndKillsTheProcess() {
        val pidFile = File.createTempFile("porter-shell", ".pid")
        try {
            val started = System.currentTimeMillis()
            try {
                onMain {
                    withTimeout(1_000) {
                        connection.exec("sh", "-c", "echo $$ > '${pidFile.absolutePath}'; exec sleep 30")
                    }
                }
                fail("the command outlived its timeout")
            } catch (e: TimeoutCancellationException) {
                // expected
            }
            assertTrue("cancelling waited for the process", System.currentTimeMillis() - started < 5_000)

            val pid = pidFile.readText().trim()
            val deadline = System.currentTimeMillis() + 5_000
            while (File("/proc/$pid").exists() && System.currentTimeMillis() < deadline) Thread.sleep(20)
            assertFalse("the process survived the cancellation", File("/proc/$pid").exists())
        } finally {
            pidFile.delete()
        }
    }

    @Test
    fun aStartedProcessKnowsItsPid() {
        val pidFile = File.createTempFile("porter-shell", ".pid")
        try {
            val process = onMain { connection.startProcess("sh", "-c", "echo $$ > '${pidFile.absolutePath}'; exec sleep 30") }
            try {
                val deadline = System.currentTimeMillis() + 5_000
                while (pidFile.length() == 0L && System.currentTimeMillis() < deadline) Thread.sleep(20)

                assertEquals(pidFile.readText().trim().toInt(), process.pid)
            } finally {
                process.destroy()
            }
        } finally {
            pidFile.delete()
        }
    }

    @Test
    fun aRunningProcessHasNoExitValueYet() {
        val process = onMain { connection.startProcess("sleep", "30") }
        try {
            try {
                process.exitValue()
                fail("a running process had an exit value")
            } catch (e: IllegalThreadStateException) {
                // expected
            }
        } finally {
            process.destroy()
        }
    }

    private companion object {
        const val PACKAGE = "eu.darken.porter.probe"
    }
}
