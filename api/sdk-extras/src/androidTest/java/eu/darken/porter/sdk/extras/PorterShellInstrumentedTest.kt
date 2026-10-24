package eu.darken.porter.sdk.extras

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import eu.darken.porter.protocol.PorterProtocol
import eu.darken.porter.sdk.Porter
import eu.darken.porter.sdk.PorterConnection
import eu.darken.porter.sdk.extras.internal.PorterShellService
import eu.darken.porter.server.IPorterApplication
import eu.darken.porter.server.IPorterService
import eu.darken.porter.server.IPorterServiceConnection
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * What a command writes and reads, through real pipes. The shell service runs in this process at
 * this app's identity, which is all the pipes need; nothing here needs a Porter or Shizuku server.
 */
@RunWith(AndroidJUnit4::class)
internal class PorterShellInstrumentedTest {

    /** A server that grants everything and connects every bind to one shell service. */
    private class ShellServer : IPorterService.Stub() {

        private val service = PorterShellService()

        override fun attach(application: IPorterApplication, args: Bundle): Bundle = Bundle().apply {
            putInt(PorterProtocol.REPLY_PROTOCOL_VERSION, PorterProtocol.VERSION)
            putInt(PorterProtocol.REPLY_MIN_PROTOCOL_VERSION, PorterProtocol.MIN_VERSION)
            putInt(PorterProtocol.REPLY_SERVER_UID, 2000)
            putBoolean(PorterProtocol.REPLY_PERMISSION_GRANTED, true)
        }

        override fun addUserService(conn: IPorterServiceConnection?, args: Bundle): Int {
            conn?.connected(service)
            return 0
        }

        override fun removeUserService(conn: IPorterServiceConnection?, args: Bundle): Int = 0

        override fun getSystemProperty(name: String, defaultValue: String?): String? = defaultValue

        override fun setSystemProperty(name: String, value: String) {
        }

        override fun getUid(): Int = 2000

        override fun checkPermission(permission: String): Int = PackageManager.PERMISSION_DENIED

        override fun getSELinuxContext(): String? = null

        override fun requestPermission(requestCode: Int) {
        }

        override fun checkSelfPermission(): Boolean = true

        override fun shouldShowRequestPermissionRationale(): Boolean = false
    }

    private lateinit var context: Context
    private lateinit var connection: PorterConnection

    @Before
    fun setup() = runBlocking {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        val server = ShellServer()
        Porter.onBinderReceived(server, context.packageName)
        connection = withTimeout(5_000) { Porter.connection.filter { it?.binder === server }.first()!! }
    }

    @Test(timeout = 20_000)
    fun execReturnsTheExitCodeAndBothStreams() = runBlocking {
        val result = connection.exec("sh", "-c", "echo out; echo err >&2; exit 3")

        assertEquals(3, result.exitCode)
        assertEquals("out\n", result.output)
        assertEquals("err\n", result.errors)
    }

    /** Both streams fill their pipes, which only works if both are read while the command runs. */
    @Test(timeout = 20_000)
    fun outputLargerThanAPipeIsReadWhole() = runBlocking {
        val line = "0123456789".repeat(10)
        val script = "i=0; while [ \$i -lt 3000 ]; do echo $line; echo $line >&2; i=\$((i+1)); done"

        val result = connection.exec("sh", "-c", script)

        assertEquals(0, result.exitCode)
        assertEquals(3000 * (line.length + 1), result.output.length)
        assertEquals(3000 * (line.length + 1), result.errors.length)
    }

    @Test(timeout = 20_000)
    fun aStartedProcessTakesInputAndReportsItsExit() = runBlocking {
        val process = connection.startProcess("cat")

        process.outputStream.use { it.write("hello".toByteArray()) }
        val output = process.inputStream.bufferedReader().use { it.readText() }

        assertEquals("hello", output)
        assertEquals(0, process.waitFor())
        assertEquals(0, process.exitValue())
    }

    /** A command that has to finish something gets the signal it asks for, not SIGKILL. */
    @Test(timeout = 20_000)
    fun aSignalReachesTheCommand() = runBlocking {
        val process = connection.startProcess("sh", "-c", "trap 'echo interrupted; exit 3' INT; echo ready; while true; do sleep 1; done")
        val output = process.inputStream.bufferedReader()
        assertEquals("ready", output.readLine())

        process.signal(OsConstants.SIGINT)

        assertEquals("interrupted", output.readLine())
        assertEquals(3, process.waitFor())
    }

    @Test(timeout = 20_000)
    fun aValueThatIsNotASignalIsRefused() = runBlocking {
        val process = connection.startProcess("sleep", "30")
        try {
            process.signal(9999)
            fail("9999 was sent as a signal")
        } catch (e: IllegalArgumentException) {
            // expected
        } finally {
            process.destroy()
        }
    }

    /** `sh -c` forks the command it runs, and that child dies with the cancellation too. */
    @Test(timeout = 20_000)
    fun cancellingKillsWhatTheCommandStarted() = runBlocking {
        val pidFile = File(context.cacheDir, "porter-shell-child.pid").apply { delete() }
        try {
            withTimeoutOrNull(1_500) {
                connection.exec("sh", "-c", "sleep 30 & echo \$! > '${pidFile.absolutePath}'; wait")
            }
            val pid = pidFile.readText().trim()
            val deadline = System.currentTimeMillis() + 5_000
            while (File("/proc/$pid").exists() && System.currentTimeMillis() < deadline) Thread.sleep(20)
            assertFalse("the command's child survived the cancellation", File("/proc/$pid").exists())
        } finally {
            pidFile.delete()
        }
    }

    /** The command exits at once and leaves `sleep` in its group; destroy() afterwards signals nothing. */
    @Test(timeout = 20_000)
    fun destroyAfterTheCommandExitedLeavesItsGroupAlone() = runBlocking {
        val process = connection.startProcess("sh", "-c", "sleep 30 >/dev/null 2>&1 & echo \$!")
        val child = process.inputStream.bufferedReader().readLine().trim()
        try {
            assertEquals(0, process.waitFor())

            process.destroy()

            Thread.sleep(500)
            // A killed child can linger as a zombie, so its state is what tells.
            val state = File("/proc/$child/stat").readText().substringAfterLast(") ").first()
            assertTrue("destroy() reached the exited command's group (state $state)", state != 'Z')
        } finally {
            try {
                Os.kill(child.toInt(), OsConstants.SIGKILL)
            } catch (e: ErrnoException) {
                // Already gone: the assertion above says why.
            }
        }
    }

    /** A prompt only gets its answer if what the app writes reaches the process at once. */
    @Test(timeout = 20_000)
    fun inputReachesTheProcessBeforeItIsClosed() = runBlocking {
        val process = connection.startProcess("sh", "-c", "read line; echo got \$line; read rest")
        try {
            process.outputStream.write("ping\n".toByteArray())
            process.outputStream.flush()

            assertEquals("got ping", process.inputStream.bufferedReader().readLine())
        } finally {
            process.destroy()
        }
    }
}
