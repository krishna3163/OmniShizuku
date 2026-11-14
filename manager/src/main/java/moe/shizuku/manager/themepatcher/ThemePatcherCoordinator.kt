package moe.shizuku.manager.themepatcher

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

/**
 * Thread-safe atomic coordinator wrapping [ThemePatcherStateMachine].
 * Manages state transitions, listener dispatches, and generation tracking.
 */
class ThemePatcherCoordinator(
    initialSnapshot: ThemePatcherSnapshot = ThemePatcherSnapshot()
) {
    private val current = AtomicReference(initialSnapshot)
    private val generationSequence = AtomicLong(initialSnapshot.generation)
    private val listeners = CopyOnWriteArrayList<(old: ThemePatcherSnapshot, new: ThemePatcherSnapshot, event: ThemePatcherEvent) -> Unit>()

    val snapshot: ThemePatcherSnapshot
        get() = current.get()

    val state: ThemePatcherState
        get() = snapshot.state

    val generation: Long
        get() = snapshot.generation

    val isRunning: Boolean
        get() = snapshot.isRunning

    fun nextGeneration(): Long = generationSequence.incrementAndGet()

    fun addListener(listener: (old: ThemePatcherSnapshot, new: ThemePatcherSnapshot, event: ThemePatcherEvent) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (old: ThemePatcherSnapshot, new: ThemePatcherSnapshot, event: ThemePatcherEvent) -> Unit) {
        listeners.remove(listener)
    }

    /**
     * Atomically transitions the state machine using [ThemePatcherStateMachine.reduce].
     */
    fun transition(event: ThemePatcherEvent): ThemePatcherSnapshot {
        var prev: ThemePatcherSnapshot
        var next: ThemePatcherSnapshot
        do {
            prev = current.get()
            next = ThemePatcherStateMachine.reduce(prev, event)
            if (prev == next) return prev
        } while (!current.compareAndSet(prev, next))

        for (listener in listeners) {
            try {
                listener(prev, next, event)
            } catch (_: Throwable) {
                // Safeguard against listener exceptions
            }
        }
        return next
    }

    fun cancel(): ThemePatcherSnapshot = transition(ThemePatcherEvent.CancelRequested)

    fun reset(): ThemePatcherSnapshot = transition(ThemePatcherEvent.Reset)
}
