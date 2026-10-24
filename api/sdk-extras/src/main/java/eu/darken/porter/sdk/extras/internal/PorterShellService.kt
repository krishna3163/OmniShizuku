package eu.darken.porter.sdk.extras.internal

import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.os.RemoteException
import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.system.exitProcess

/**
 * The user service behind `PorterConnection.exec`: runs in its own process at the server's identity,
 * loaded by name from the app's APK, and starts the processes the app asks for.
 */
internal class PorterShellService : IPorterShellService.Stub() {

    /** Started and not yet seen to exit, so stopping the service does not orphan them. */
    private val running = HashSet<ShellProcess>()

    override fun destroy() {
        synchronized(running) { running.toList() }.forEach { it.destroy() }
        exitProcess(0)
    }

    override fun start(command: Array<String>, dir: String?, owner: IBinder): IPorterShellProcess {
        // setsid gives the command a process group of its own, so a kill reaches what it starts as
        // well. Either way a command that is not found exits with 127, as in a shell.
        val grouped = File(SETSID).exists()
        val argv = if (grouped) arrayOf(SETSID) + command else arrayOf("sh", "-c", "exec \"\$@\"", "sh") + command
        val process = try {
            Runtime.getRuntime().exec(argv, null, dir?.let(::File))
        } catch (e: Exception) {
            // Only a few exception types cross a binder; this is one of them.
            throw IllegalStateException(e.message ?: e.toString())
        }
        return try {
            ShellProcess(process, grouped, owner, running)
        } catch (e: Exception) {
            kill(process, grouped)
            throw IllegalStateException(e.message ?: e.toString())
        }
    }

    internal companion object {

        const val SETSID = "/system/bin/setsid"

        /**
         * Sends SIGKILL to [process] and, where it leads its own group, to everything in that group.
         * A child that made a session of its own is out of reach, and so is everything once [process]
         * has exited: its group can no longer be told from a later one that got the same id.
         */
        fun kill(process: Process, grouped: Boolean) {
            val pid = pidOf(process)
            if (pid != null && !hasExited(process)) {
                try {
                    Os.kill(if (grouped) -pid else pid, OsConstants.SIGKILL)
                } catch (e: ErrnoException) {
                    // Gone already, or not ours to kill; Process.destroy() below is all that is left.
                }
            }
            // After an exit too: it signals nothing then, and closes pipes a leftover may hold open.
            process.destroy()
        }

        private fun hasExited(process: Process): Boolean = try {
            process.exitValue()
            true
        } catch (e: IllegalThreadStateException) {
            false
        }

        /** `Process[pid=123, hasExited=false]` on Android, `Process[pid=123, exitValue=...]` on a JDK. */
        fun pidOf(process: Process): Int? =
            Regex("""pid=(\d+)""").find(process.toString())?.groupValues?.get(1)?.toIntOrNull()
    }
}

internal class ShellProcess(
    private val process: Process,
    private val grouped: Boolean,
    private val owner: IBinder,
    private val running: MutableSet<ShellProcess>,
) : IPorterShellProcess.Stub() {

    private val ownerDied = IBinder.DeathRecipient { destroy() }

    private val exited = CountDownLatch(1)

    private val pid: Int? = PorterShellService.pidOf(process)

    private var stdin: ParcelFileDescriptor? = null
    private var stdout: ParcelFileDescriptor? = null
    private var stderr: ParcelFileDescriptor? = null

    init {
        try {
            stdin = pipeTo(process.outputStream)
            stdout = pipeFrom(process.inputStream)
            stderr = pipeFrom(process.errorStream)
        } catch (e: IOException) {
            closeUntaken()
            throw e
        }
        synchronized(running) { running.add(this) }
        try {
            owner.linkToDeath(ownerDied, 0)
        } catch (e: RemoteException) {
            destroy()
        }
        // Started last, so a process that exits at once is finished only after it was tracked.
        thread(isDaemon = true, name = "porter-shell-exit") {
            try {
                process.waitFor()
            } catch (e: InterruptedException) {
                return@thread
            }
            exited.countDown()
            finish()
        }
    }

    // Returned with PARCELABLE_WRITE_RETURN_VALUE, which closes this side's descriptor once sent.
    @Synchronized
    override fun takeStdin(): ParcelFileDescriptor = checkNotNull(stdin) { "stdin was taken" }.also { stdin = null }

    @Synchronized
    override fun takeStdout(): ParcelFileDescriptor = checkNotNull(stdout) { "stdout was taken" }.also { stdout = null }

    @Synchronized
    override fun takeStderr(): ParcelFileDescriptor = checkNotNull(stderr) { "stderr was taken" }.also { stderr = null }

    /** Bounded, so a caller waiting for a long command does not hold one of this service's binder threads. */
    override fun waitFor(timeoutMillis: Long): Boolean = try {
        exited.await(timeoutMillis, TimeUnit.MILLISECONDS)
    } catch (e: InterruptedException) {
        throw IllegalStateException(e.message)
    }

    override fun alive(): Boolean = exited.count > 0

    override fun exitValue(): Int {
        check(!alive()) { "the process has not exited" }
        return process.exitValue()
    }

    override fun pid(): Int = pid ?: -1

    override fun signal(signal: Int) {
        if (!alive()) return
        val pid = checkNotNull(pid) { "the process id is unknown" }
        try {
            Os.kill(pid, signal)
        } catch (e: ErrnoException) {
            when (e.errno) {
                OsConstants.ESRCH -> Unit // exited since the check above
                OsConstants.EINVAL -> throw IllegalArgumentException("not a signal: $signal")
                // Such as EPERM, once the command changed identity.
                else -> throw IllegalStateException(e.message)
            }
        }
    }

    override fun destroy() {
        PorterShellService.kill(process, grouped)
        closeUntaken()
        finish()
    }

    @Synchronized
    private fun closeUntaken() {
        for (pipe in listOfNotNull(stdin, stdout, stderr)) {
            try {
                pipe.close()
            } catch (e: IOException) {
            }
        }
        stdin = null
        stdout = null
        stderr = null
    }

    /** Stops tracking the process, which one owner token links for every process it started. */
    private fun finish() {
        if (synchronized(running) { running.remove(this) }) {
            try {
                owner.unlinkToDeath(ownerDied, 0)
            } catch (e: NoSuchElementException) {
                // The link never took: the owner was dead before this process was tracked.
            }
        }
    }

    private companion object {

        fun pipeFrom(source: InputStream): ParcelFileDescriptor {
            val (read, write) = ParcelFileDescriptor.createPipe()
            try {
                transfer(source, ParcelFileDescriptor.AutoCloseOutputStream(write))
            } catch (e: Throwable) {
                read.close()
                write.close()
                throw e
            }
            return read
        }

        fun pipeTo(sink: OutputStream): ParcelFileDescriptor {
            val (read, write) = ParcelFileDescriptor.createPipe()
            try {
                transfer(ParcelFileDescriptor.AutoCloseInputStream(read), sink)
            } catch (e: Throwable) {
                read.close()
                write.close()
                throw e
            }
            return write
        }

        /**
         * Copies until either side closes: the process exiting, or the app closing its end. Flushes
         * every read, because a process's stdin is buffered and a prompt waits for its answer.
         */
        fun transfer(from: InputStream, to: OutputStream) {
            thread(isDaemon = true, name = "porter-shell-pipe") {
                try {
                    from.use { input ->
                        to.use { output ->
                            val buffer = ByteArray(8192)
                            while (true) {
                                val read = input.read(buffer)
                                if (read < 0) break
                                output.write(buffer, 0, read)
                                output.flush()
                            }
                        }
                    }
                } catch (e: IOException) {
                    // The other end went away; there is nobody left to tell.
                }
            }
        }
    }
}
