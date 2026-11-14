package moe.shizuku.manager.notifications

enum class HelperEventType {
    SERVER_STOPPED,
    NEEDS_ATTENTION,
    RECOVERED,
    PERMISSION_REQUIRED
}

data class HelperEvent(
    val type: HelperEventType,
    val serverGeneration: Long = 0L,
    val errorIdentity: String? = null,
    val recoveryAttemptId: Long = 0L,
    val message: String? = null,
    val showRestart: Boolean = false
)

fun helperDedupeKey(event: HelperEvent): String =
    listOf(
        event.type.name,
        event.serverGeneration.toString(),
        event.errorIdentity.orEmpty(),
        event.recoveryAttemptId.toString()
    ).joinToString("|")

/**
 * Pure policy for helper notification posting, deduplication, debounce, and dismissal.
 * Decoupled from Android framework for deterministic unit testing.
 */
class HelperNotificationPolicy(
    private val debounceMs: Long = 5000L
) {
    var lastKey: String? = null
        private set
    var dismissedKey: String? = null
        private set
    private var lastPostTimeElapsed: Long = 0L

    fun shouldPost(
        event: HelperEvent,
        nowElapsed: Long,
        helperEnabled: Boolean,
        notificationsAllowed: Boolean
    ): Boolean {
        if (!helperEnabled || !notificationsAllowed) return false
        val key = helperDedupeKey(event)
        if (key == lastKey || key == dismissedKey) return false
        if (nowElapsed - lastPostTimeElapsed < debounceMs && lastKey?.startsWith(event.type.name) == true) {
            return false
        }
        return true
    }

    fun recordPosted(event: HelperEvent, nowElapsed: Long) {
        lastKey = helperDedupeKey(event)
        lastPostTimeElapsed = nowElapsed
        if (event.type == HelperEventType.RECOVERED) {
            dismissedKey = null
        }
    }

    fun onDismissed(key: String? = null) {
        dismissedKey = key ?: lastKey
    }

    fun onHealthy(): Boolean {
        val prev = lastKey
        val shouldCancelOutdated = prev != null && !prev.startsWith(HelperEventType.RECOVERED.name)
        dismissedKey = null
        return shouldCancelOutdated
    }

    fun restore(last: String?, dismissed: String?) {
        lastKey = last
        dismissedKey = dismissed
    }

    fun reset() {
        lastKey = null
        dismissedKey = null
        lastPostTimeElapsed = 0L
    }
}
