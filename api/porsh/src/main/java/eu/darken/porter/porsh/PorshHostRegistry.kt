package eu.darken.porter.porsh

import android.os.ParcelFileDescriptor
import android.util.Log
import java.util.concurrent.ConcurrentHashMap

/**
 * Keeps one shell host per calling pid. Every method here runs on a binder thread.
 */
internal class PorshHostRegistry(
    private val factory: HostFactory,
    private val clock: Clock,
) {

    fun interface HostFactory {
        fun create(
            args: Array<String>,
            env: Array<String>?,
            dir: String?,
            tty: Byte,
            stdin: ParcelFileDescriptor?,
            stdout: ParcelFileDescriptor?,
            stderr: ParcelFileDescriptor?,
        ): PorshHost
    }

    fun interface Clock {
        fun elapsedMillis(): Long
    }

    private val hosts = ConcurrentHashMap<Int, PorshHost>()

    fun createHost(
        callingPid: Int,
        args: Array<String>,
        env: Array<String>?,
        dir: String?,
        tty: Byte,
        stdin: ParcelFileDescriptor?,
        stdout: ParcelFileDescriptor?,
        stderr: ParcelFileDescriptor?,
    ) {
        reapUncollected()

        val host = factory.create(args, env, dir, tty, stdin, stdout, stderr)
        host.start()
        Log.d(TAG, "Forked " + host.pid)

        hosts[callingPid] = host
    }

    fun setWindowSize(callingPid: Int, size: Long) {
        val host = hosts[callingPid]
        if (host == null) {
            Log.d(TAG, "Not existing host created by $callingPid")
            return
        }

        host.setWindowSize(size)
    }

    fun getExitCode(callingPid: Int): Int {
        val host = hosts[callingPid]
        if (host == null) {
            Log.d(TAG, "Not existing host created by $callingPid")
            return -1
        }

        val exitCode = host.awaitExitCode(EXIT_CODE_TIMEOUT_MILLIS)
        if (!host.hasExited()) {
            return exitCode
        }
        // Conditional: another transaction from the same pid may already have
        // installed a newer host while this one was waiting.
        hosts.remove(callingPid, host)
        return host.exitCode
    }

    /**
     * Drops hosts whose client never came back for the exit status. An entry that has
     * merely exited is not abandoned: its client drains output first and asks afterwards.
     */
    private fun reapUncollected() {
        val now = clock.elapsedMillis()
        for ((callingPid, host) in hosts) {
            if (!host.hasExited()) continue
            if (now - host.exitedAtMillis <= UNCOLLECTED_MAX_AGE_MILLIS) continue
            if (hosts.remove(callingPid, host)) {
                Log.d(TAG, "Reaped uncollected host of $callingPid")
            }
        }
    }

    companion object {

        private const val TAG = "PorshHostRegistry"

        const val EXIT_CODE_TIMEOUT_MILLIS = 5_000L

        /**
         * How long an exited host is kept so its client can still collect the status.
         */
        const val UNCOLLECTED_MAX_AGE_MILLIS = 60_000L
    }
}
