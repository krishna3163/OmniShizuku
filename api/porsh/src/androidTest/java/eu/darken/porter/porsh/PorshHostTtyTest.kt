package eu.darken.porter.porsh

import android.os.ParcelFileDescriptor
import android.system.Os
import android.system.OsConstants
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PorshHostTtyTest {

    /**
     * Shizuku's rish, run as `rish -c cmd | grep x` from a terminal, marks stderr as a tty and sends
     * no stderr pipe. The child's stderr then lands on the host's pty, which has to be drained or a
     * megabyte of it blocks the child long before `printf done`.
     */
    @Test
    fun ttyStderrWithoutTtyStdout_doesNotBlockTheChild() {
        System.loadLibrary("porsh")
        val stdin = ParcelFileDescriptor.createPipe()
        val stdout = ParcelFileDescriptor.createPipe()
        val script = "dd if=/dev/zero bs=4096 count=256 >&2 2>/dev/null; printf done"
        val host = PorshHost(
            arrayOf("-c", script), null, null, PorshConstants.ATTY_ERR.toByte(),
            stdin[0], stdout[1], null,
        )

        host.start()
        stdin[1].close()
        try {
            assertEquals(0, host.awaitExitCode(10_000))
        } finally {
            if (!host.hasExited()) Os.kill(-host.pid, OsConstants.SIGKILL)
        }

        val output = ParcelFileDescriptor.AutoCloseInputStream(stdout[0]).use { it.readBytes() }
        assertEquals("done", output.decodeToString())
    }

    /** The same client with stdin on the terminal too: two relays share the pty, each closes once. */
    @Test
    fun ttyStdinAndStderr_closesThePtyOnce() {
        System.loadLibrary("porsh")
        val stdin = ParcelFileDescriptor.createPipe()
        val stdout = ParcelFileDescriptor.createPipe()
        val tty = PorshConstants.ATTY_IN or PorshConstants.ATTY_ERR
        val host = PorshHost(
            arrayOf("-c", "printf err >&2; printf out; exit 3"), null, null, tty.toByte(),
            stdin[0], stdout[1], null,
        )

        host.start()
        try {
            assertEquals(3, host.awaitExitCode(10_000))
        } finally {
            if (!host.hasExited()) Os.kill(-host.pid, OsConstants.SIGKILL)
        }
        val output = ParcelFileDescriptor.AutoCloseInputStream(stdout[0]).use { it.readBytes() }
        assertEquals("out", output.decodeToString())

        // Take the numbers the session released, then end its stdin: a relay closing a number it
        // no longer owns takes one of these probes with it. New descriptors take the lowest free
        // numbers, so there are enough probes to reach past any older gaps.
        Thread.sleep(200)
        val probes = List(256) { ParcelFileDescriptor.open(File("/dev/null"), ParcelFileDescriptor.MODE_READ_ONLY) }
        stdin[1].close()
        Thread.sleep(200)
        try {
            probes.forEach { Os.fstat(it.fileDescriptor) }
        } finally {
            probes.forEach { it.close() }
        }
    }

    /** The pty relay ends as the child exits; that alone must not reach the child as a SIGKILL. */
    @Test
    fun ttyChild_keepsItsExitCode() {
        System.loadLibrary("porsh")
        repeat(20) { run ->
            val stdin = ParcelFileDescriptor.createPipe()
            val stdout = ParcelFileDescriptor.createPipe()
            val all = PorshConstants.ATTY_IN or PorshConstants.ATTY_OUT or PorshConstants.ATTY_ERR
            val host = PorshHost(
                arrayOf("-c", "printf x; exit 7"), null, null, all.toByte(),
                stdin[0], stdout[1], null,
            )

            host.start()
            try {
                assertEquals("run $run", 7, host.awaitExitCode(10_000))
            } finally {
                if (!host.hasExited()) Os.kill(-host.pid, OsConstants.SIGKILL)
                stdin[1].close()
                stdout[0].close()
            }
        }
    }

    /** A client that stops reading is gone; its child must not outlive it. */
    @Test
    fun clientGone_killsAWritingChild() {
        System.loadLibrary("porsh")
        val stdin = ParcelFileDescriptor.createPipe()
        val stdout = ParcelFileDescriptor.createPipe()
        val stderr = ParcelFileDescriptor.createPipe()
        val host = PorshHost(
            arrayOf("-c", "while :; do printf x; done"), null, null, 0,
            stdin[0], stdout[1], stderr[1],
        )

        // Closed before the fork, or a copy the shell inherits keeps the pipe readable.
        stdout[0].close()
        host.start()
        try {
            host.awaitExitCode(10_000)
            assertTrue(host.hasExited())
        } finally {
            if (!host.hasExited()) Os.kill(-host.pid, OsConstants.SIGKILL)
            stdin[1].close()
            stderr[0].close()
        }
    }

    /** Same, for a child that writes nothing, with and without a terminal. */
    @Test
    fun clientGone_killsAQuietChild() {
        System.loadLibrary("porsh")
        val all = PorshConstants.ATTY_IN or PorshConstants.ATTY_OUT or PorshConstants.ATTY_ERR
        for (tty in listOf(0, all)) {
            val stdin = ParcelFileDescriptor.createPipe()
            val stdout = ParcelFileDescriptor.createPipe()
            val stderr = ParcelFileDescriptor.createPipe()
            val host = PorshHost(
                arrayOf("-c", "sleep 30"), null, null, tty.toByte(),
                stdin[0], stdout[1], if (tty == 0) stderr[1] else null,
            )

            stdout[0].close()
            host.start()
            try {
                host.awaitExitCode(10_000)
                assertTrue("tty $tty", host.hasExited())
            } finally {
                if (!host.hasExited()) Os.kill(-host.pid, OsConstants.SIGKILL)
                stdin[1].close()
                stderr[0].close()
                if (tty != 0) stderr[1].close()
            }
        }
    }

    /** A pty that stdout is not on still has to be read, or writes to /dev/tty block the child. */
    @Test
    fun ttyStdinOnly_doesNotBlockWritesToTheTerminal() {
        System.loadLibrary("porsh")
        val stdin = ParcelFileDescriptor.createPipe()
        val stdout = ParcelFileDescriptor.createPipe()
        val stderr = ParcelFileDescriptor.createPipe()
        val host = PorshHost(
            arrayOf("-c", "dd if=/dev/zero bs=4096 count=256 > /dev/tty 2>/dev/null; printf done"),
            null, null, PorshConstants.ATTY_IN.toByte(), stdin[0], stdout[1], stderr[1],
        )

        host.start()
        try {
            assertEquals(0, host.awaitExitCode(10_000))
        } finally {
            if (!host.hasExited()) Os.kill(-host.pid, OsConstants.SIGKILL)
            stdin[1].close()
            stderr[0].close()
        }
        val output = ParcelFileDescriptor.AutoCloseInputStream(stdout[0]).use { it.readBytes() }
        assertEquals("done", output.decodeToString())
    }

    /** A signal ends the shell with 128 plus its number, as a shell reports it. */
    @Test
    fun signalledChild_reports128PlusTheSignal() {
        System.loadLibrary("porsh")
        val stdin = ParcelFileDescriptor.createPipe()
        val stdout = ParcelFileDescriptor.createPipe()
        val stderr = ParcelFileDescriptor.createPipe()
        val host = PorshHost(
            arrayOf("-c", "kill -TERM $$"), null, null, 0,
            stdin[0], stdout[1], stderr[1],
        )

        host.start()
        try {
            assertEquals(128 + OsConstants.SIGTERM, host.awaitExitCode(10_000))
        } finally {
            if (!host.hasExited()) Os.kill(-host.pid, OsConstants.SIGKILL)
            stdin[1].close()
            stdout[0].close()
            stderr[0].close()
        }
    }
}
