package moe.shizuku.manager.adb

/**
 * The lines of the native starter's output worth keeping in the log of a background start.
 *
 * A start from the start screen shows all of it; a start with no screen used to throw it away, and
 * with it the only account of what the starter found and did: whether it met a server already
 * running, and whether it killed one. Only the lines that say so are kept, because the rest is
 * progress chatter and the log is read by people.
 */
private val KEPT_MARKERS = listOf(
    "found active server process",
    "killed process",
    "shizuku_server pid is",
    "the server was killed by signal",
    "the server exited with",
    "the server is still running",
    "fatal:",
    "warn:"
)

internal fun isStarterLineWorthLogging(line: String): Boolean {
    val text = line.trim()
    return text.isNotEmpty() && KEPT_MARKERS.any { text.contains(it, ignoreCase = true) }
}

/**
 * The starter's output arrives in whatever pieces the connection delivers, not in lines, so a
 * line can be split across two chunks. Collects them and hands over only the complete lines.
 */
internal class StarterLineBuffer(private val onLine: (String) -> Unit) {
    private val pending = StringBuilder()

    fun append(chunk: String) {
        pending.append(chunk)
        while (true) {
            val newline = pending.indexOf("\n")
            if (newline < 0) return
            val line = pending.substring(0, newline)
            pending.delete(0, newline + 1)
            if (isStarterLineWorthLogging(line)) onLine(line.trim())
        }
    }

    /** The last line has no newline after it when the command ends, so it is read here. */
    fun flush() {
        val line = pending.toString()
        pending.setLength(0)
        if (isStarterLineWorthLogging(line)) onLine(line.trim())
    }
}
