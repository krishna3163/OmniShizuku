package eu.darken.porter.sdk.extras

import android.os.ParcelFileDescriptor
import android.os.RemoteException
import eu.darken.porter.sdk.extras.internal.IPorterShellProcess
import java.io.Closeable
import java.io.InputStream
import java.io.OutputStream

/** How long one wait holds a binder thread of the shell service. */
private const val WAIT_SLICE_MS = 250L

/**
 * A command [startProcess] started, running at the connection's identity. It is the caller's once
 * started: nothing stops it but [destroy], the command ending, or this app's process dying.
 *
 * The streams are pipes to it. [destroy] sends SIGKILL to the command and everything in its process
 * group, or to the command alone on a device without `/system/bin/setsid`. That gives none of them
 * a chance to clean up, unlike the SIGTERM a local `Process.destroy()` sends; send [signal] first
 * to a command that has to finish something, such as `screenrecord` writing its file. Once the
 * command has exited, [destroy] only closes the pipes: what it left running in its group is no
 * longer reached, by [destroy] or by this app's process dying. The methods
 * [Process] declares block, and throw [PorterShellException] if the shell service stops answering;
 * [waitFor] also ends with `InterruptedException` when its thread is interrupted, so
 * `runInterruptible` makes it cancellable.
 */
public class PorterShellProcess internal constructor(private val remote: IPorterShellProcess) : Process() {

    private val stdin: OutputStream
    private val stdout: InputStream
    private val stderr: InputStream

    /** The command's process id, or null where the shell service could not read it. */
    public val pid: Int?

    init {
        val taken = ArrayList<Closeable>(3)
        try {
            pid = shellCall { remote.pid() }.takeIf { it > 0 }
            stdin = ParcelFileDescriptor.AutoCloseOutputStream(shellCall { remote.takeStdin() }).also { taken += it }
            stdout = ParcelFileDescriptor.AutoCloseInputStream(shellCall { remote.takeStdout() }).also { taken += it }
            stderr = ParcelFileDescriptor.AutoCloseInputStream(shellCall { remote.takeStderr() }).also { taken += it }
        } catch (e: Throwable) {
            taken.forEach { it.closeQuietly() }
            destroyRemote()
            throw e
        }
    }

    override fun getOutputStream(): OutputStream = stdin

    override fun getInputStream(): InputStream = stdout

    override fun getErrorStream(): InputStream = stderr

    override fun waitFor(): Int {
        // In slices, so no binder thread of the service is held for the whole run.
        while (!shellCall { remote.waitFor(WAIT_SLICE_MS) }) {
            if (Thread.interrupted()) throw InterruptedException()
        }
        return shellCall { remote.exitValue() }
    }

    override fun exitValue(): Int {
        if (shellCall { remote.alive() }) throw IllegalThreadStateException("the process has not exited")
        return shellCall { remote.exitValue() }
    }

    /**
     * Sends [signal], an `OsConstants.SIG*` value, to the command itself; the processes it started
     * do not get it. A command that already exited is left alone. Suspends and is safe on the main
     * thread; cancelling stops the wait, not a signal already sent.
     *
     * Throws `IllegalArgumentException` for a value that is not a signal, and [PorterShellException]
     * where [pid] is null or the signal cannot be delivered.
     */
    public suspend fun signal(signal: Int) {
        ShellCalls.detached { shellCall { remote.signal(signal) } }
    }

    override fun destroy() {
        // Local first: a service that stopped answering must not keep these open.
        stdin.closeQuietly()
        stdout.closeQuietly()
        stderr.closeQuietly()
        destroyRemote()
    }

    private fun destroyRemote() {
        try {
            remote.destroy()
        } catch (e: RemoteException) {
            // Nothing is left to ask. A process whose service died without stopping it keeps running.
        }
    }

    private fun Closeable.closeQuietly() {
        try {
            close()
        } catch (e: java.io.IOException) {
        }
    }
}
