package moe.shizuku.manager.utils

import android.os.SystemClock
import java.util.Locale

/**
 * How long after the phone was switched on each step of the first start happened.
 *
 * The log timestamps every line, but they are wall-clock times, and nothing in them says when the
 * phone booted, what woke this process, or how long the client waited for its binder. A start that
 * is quick once it begins can still feel slow when it begins late, and that is invisible without a
 * common zero. The zero is the elapsed-realtime clock, which starts at boot and keeps counting
 * through sleep.
 *
 * Only the first start of a boot is stamped (see [stamp]): later lines are about a phone that has
 * been up for hours, and a stamp there would only be noise on every line the log carries.
 */
object BootTimeline {

    /** Past this the phone is no longer "just booted", whatever this process has or has not seen. */
    private const val WINDOW_MS = 10 * 60 * 1000L

    @Volatile
    private var sawRunning = false

    /** `+12.3s since boot`, for lines that always carry it. */
    fun sinceBoot(): String = format(SystemClock.elapsedRealtime())

    /**
     * `, +12.3s since boot` to append to a line while the first start of this boot is still going,
     * and nothing after it.
     */
    fun suffix(): String {
        val now = SystemClock.elapsedRealtime()
        return if (stamp(now, sawRunning)) ", ${format(now)}" else ""
    }

    /**
     * The first server to come up ends the first start. Kept in this process only: a process
     * that dies and comes back inside the window will stamp again until it sees a server, which is
     * what is wanted when the boot start has not finished.
     */
    fun markRunning() {
        sawRunning = true
    }

    /** Whether a line at [elapsedMs] since boot is still part of the first start. */
    internal fun stamp(elapsedMs: Long, sawRunning: Boolean): Boolean =
        !sawRunning && elapsedMs < WINDOW_MS

    internal fun format(elapsedMs: Long): String =
        String.format(Locale.US, "+%.1fs since boot", elapsedMs / 1000.0)
}
