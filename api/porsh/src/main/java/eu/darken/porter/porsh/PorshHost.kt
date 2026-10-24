package eu.darken.porter.porsh

import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.util.Log
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

open class PorshHost(
    private val args: Array<String>,
    private val env: Array<String>?,
    private val dir: String?,
    private val tty: Byte,
    stdin: ParcelFileDescriptor?,
    stdout: ParcelFileDescriptor?,
    stderr: ParcelFileDescriptor?,
) {

    private val stdin: Int = detachFd(stdin)
    private val stdout: Int = detachFd(stdout)
    private val stderr: Int = detachFd(stderr)
    private val exited = CountDownLatch(1)
    private var forkedPid = 0

    /** The pty master, -1 without a tty or once closed; guarded by [ptmxLock]. */
    private var ptmx = -1
    private val ptmxLock = Any()

    @Volatile
    internal var exitCode: Int = Int.MAX_VALUE
        private set

    @Volatile
    private var exitedAtMillisValue = 0L

    /**
     * Fork and execute, start transfer threads.
     */
    open fun start() {
        Log.d(TAG, "start")

        val argBlock = createCBytesForStringArray(args)
        val envBlock = createCBytesForStringArray(env)
        val dirBlock = createCBytesForString(dir)

        val result = start(
            argBlock, args.size,
            envBlock, env?.size ?: -1,
            dirBlock,
            tty, stdin, stdout, stderr,
        )

        forkedPid = result[0]
        synchronized(ptmxLock) { ptmx = result[1] }

        Thread { onExited(waitFor(forkedPid)) }.start()
    }

    open val pid: Int
        get() = forkedPid

    internal fun onExited(code: Int) {
        closePtmx()
        exitCode = code
        exitedAtMillisValue = SystemClock.elapsedRealtime()
        exited.countDown()
    }

    internal fun hasExited(): Boolean = exited.count == 0L

    internal open val exitedAtMillis: Long
        get() = exitedAtMillisValue

    internal open fun awaitExitCode(timeoutMillis: Long): Int {
        try {
            if (!exited.await(timeoutMillis, TimeUnit.MILLISECONDS)) {
                Log.w(TAG, "Timed out waiting for $forkedPid to exit")
                return -1
            }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            return -1
        }
        return exitCode
    }

    open fun setWindowSize(size: Long) {
        Log.d(TAG, "setWindowSize")

        // Under the lock, so a resize can never reach a descriptor number reused after the close.
        synchronized(ptmxLock) {
            if (ptmx < 0) return
            setWindowSize(ptmx, size)
        }
    }

    private fun closePtmx() {
        synchronized(ptmxLock) {
            if (ptmx < 0) return
            try {
                ParcelFileDescriptor.adoptFd(ptmx).close()
            } catch (e: IOException) {
                Log.w(TAG, "close ptmx", e)
            }
            ptmx = -1
        }
    }

    companion object {

        private const val TAG = "PorshHost"

        // libcore/ojluni/src/main/java/java/lang/ProcessImpl.java

        private fun createCBytesForStringArray(array: Array<String>?): ByteArray? {
            if (array == null) return null

            val bytes = Array(array.size) { array[it].toByteArray() }
            var count = bytes.size // For added NUL bytes
            for (arg in bytes) count += arg.size
            val block = ByteArray(count)
            var i = 0
            for (arg in bytes) {
                System.arraycopy(arg, 0, block, i, arg.size)
                i += arg.size + 1
                // No need to write NUL bytes explicitly
            }
            return block
        }

        private fun createCBytesForString(s: String?): ByteArray? {
            if (s == null) return null

            val bytes = s.toByteArray()
            val result = ByteArray(bytes.size + 1)
            System.arraycopy(bytes, 0, result, 0, bytes.size)
            result[result.size - 1] = 0
            return result
        }

        private fun detachFd(pfd: ParcelFileDescriptor?): Int = pfd?.detachFd() ?: -1

        // Registered by name and signature from porsh_host.cpp, on this class.
        @JvmStatic
        private external fun start(
            argBlock: ByteArray?, argc: Int,
            envBlock: ByteArray?, envc: Int,
            dirBlock: ByteArray?,
            tty: Byte, stdin: Int, stdout: Int, stderr: Int,
        ): IntArray

        @JvmStatic
        private external fun setWindowSize(ptmx: Int, size: Long)

        @JvmStatic
        private external fun waitFor(pid: Int): Int
    }
}
