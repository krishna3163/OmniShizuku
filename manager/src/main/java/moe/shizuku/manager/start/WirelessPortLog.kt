package moe.shizuku.manager.start

import java.util.concurrent.ConcurrentHashMap
import moe.shizuku.manager.AppConstants
import moe.shizuku.manager.utils.Diag

/**
 * What the port search says in the log, once.
 *
 * The same port is resolved again whenever mDNS repeats an announcement, probed by every step that
 * wants it and rejected by each of them in turn, and a line per event turns the one thing a report
 * needs - which port came from where, and why one was passed over - into a screen of repeats.
 * A line is written the first time its key is seen in an attempt; [reset] starts the next attempt
 * from a clean slate, so that each attempt's log is complete on its own.
 */
object WirelessPortLog {

    private val seen = ConcurrentHashMap.newKeySet<String>()

    fun reset() = seen.clear()

    /** Writes [message] unless a line with this [key] has been written since the last [reset]. */
    fun once(key: String, message: String) {
        if (seen.add(key)) Diag.info(AppConstants.TAG, message)
    }

    /** A port that answered or was announced, with where it came from. */
    fun resolved(port: Int, source: StartTransport.PortSource) =
        once("resolved:$port:${source.name}", "Port $port resolved (${source.label})")

    /** A candidate that was not used, and why. */
    fun rejected(port: Int, source: StartTransport.PortSource, reason: String) =
        once("rejected:$port:$reason", "Port $port (${source.label}) rejected: $reason")

    /** A search for a fresh port, and what asked for it. */
    fun searching(reason: String) = once("search:$reason", "Looking for a fresh wireless port: $reason")
}
