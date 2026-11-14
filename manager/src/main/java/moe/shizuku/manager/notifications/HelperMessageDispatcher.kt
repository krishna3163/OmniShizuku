package moe.shizuku.manager.notifications

import java.util.ArrayDeque
import java.util.UUID

enum class HelperMessageType { INFO, SUCCESS, WARNING, ERROR }
enum class HelperMessagePriority { INFORMATIONAL, SUCCESS, WARNING, ERROR }

data class HelperMessage(
    val id: String = UUID.randomUUID().toString(),
    val type: HelperMessageType,
    val text: String,
    val createdAtElapsed: Long = 0L,
    val durationMs: Long = 2500L,
    val priority: HelperMessagePriority = when (type) {
        HelperMessageType.INFO -> HelperMessagePriority.INFORMATIONAL
        HelperMessageType.SUCCESS -> HelperMessagePriority.SUCCESS
        HelperMessageType.WARNING -> HelperMessagePriority.WARNING
        HelperMessageType.ERROR -> HelperMessagePriority.ERROR
    },
    val destination: String? = null,
    val deduplicationKey: String = "${type.name}|$text"
)

object HelperMessageDispatcher {

    interface Clock {
        fun elapsedRealtime(): Long
    }

    interface Scheduler {
        fun postDelayed(delayMs: Long, action: () -> Unit)
    }

    private val defaultClock = object : Clock {
        override fun elapsedRealtime(): Long = System.currentTimeMillis()
    }

    private val defaultScheduler = object : Scheduler {
        override fun postDelayed(delayMs: Long, action: () -> Unit) {
            action()
        }
    }

    @Volatile var clock: Clock = defaultClock
    @Volatile var scheduler: Scheduler = defaultScheduler

    private val queue = ArrayDeque<HelperMessage>()
    private val recent = LinkedHashMap<String, Long>()
    @Volatile private var current: HelperMessage? = null
    @Volatile private var appForeground: Boolean = false

    interface OverlayController {
        fun canShow(): Boolean
        fun show(message: HelperMessage, onDone: () -> Unit)
        fun removeAll()
    }

    interface InAppController {
        fun show(message: HelperMessage)
    }

    @Volatile var overlayController: OverlayController? = null
    @Volatile var inAppController: InAppController? = null

    @Synchronized
    fun resetForTests() {
        queue.clear()
        recent.clear()
        current = null
        appForeground = false
        overlayController = null
        inAppController = null
        clock = defaultClock
        scheduler = defaultScheduler
    }

    fun setAppForeground(foreground: Boolean) {
        appForeground = foreground
        if (foreground) synchronized(this) { drain() }
    }

    fun isAppForeground(): Boolean = appForeground

    @Synchronized
    fun enqueue(message: HelperMessage) {
        val now = clock.elapsedRealtime()
        val last = recent[message.deduplicationKey]
        if (last != null && now - last < 5000L) return
        recent[message.deduplicationKey] = now
        val it = recent.entries.iterator()
        while (it.hasNext()) {
            if (now - it.next().value > 60_000L) it.remove()
        }
        val msg = if (message.createdAtElapsed == 0L) message.copy(createdAtElapsed = now) else message
        queue.addLast(msg)
        drain()
    }

    @Synchronized
    fun clearQueue() {
        queue.clear()
        val cur = current
        current = null
        if (cur != null) overlayController?.removeAll()
    }

    @Synchronized
    fun onOverlayUnavailable() {
        overlayController?.removeAll()
        if (current != null) current = null
        drain()
    }

    @Synchronized fun currentMessage(): HelperMessage? = current
    @Synchronized fun queueSize(): Int = queue.size

    @Synchronized
    private fun drain() {
        if (current != null) return
        val next = queue.pollFirst() ?: return
        current = next
        dispatch(next)
    }

    private fun dispatch(message: HelperMessage) {
        val overlay = overlayController
        val inApp = inAppController
        val overlayEnabled = overlay?.canShow() == true
        when {
            overlayEnabled -> overlay.show(message) { onDone(message) }
            appForeground -> {
                inApp?.show(message)
                scheduleDone(message)
            }
            else -> {
                // Overlay disabled/unavailable and app not foreground: in-app only when open.
                // Pairing results are delivered as normal notifications independently.
                onDone(message)
            }
        }
    }

    private fun scheduleDone(message: HelperMessage) {
        scheduler.postDelayed(message.durationMs) { onDone(message) }
    }

    @Synchronized
    fun onDone(message: HelperMessage) {
        if (current?.id == message.id) current = null
        drain()
    }
}
