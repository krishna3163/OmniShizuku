package moe.shizuku.manager.display

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

/**
 * Thread-safe atomic coordinator wrapping [DisplayRollbackStateMachine].
 * Dispatches state transitions, invokes callbacks, and maintains monotonic generation numbers.
 */
class DisplayRollbackCoordinator(
    initialSnapshot: DisplayRollbackSnapshot = DisplayRollbackSnapshot()
) {
    private val current = AtomicReference(initialSnapshot)
    private val generationSequence = AtomicLong(initialSnapshot.generation)
    private val listeners = CopyOnWriteArrayList<(old: DisplayRollbackSnapshot, new: DisplayRollbackSnapshot, event: DisplayRollbackEvent) -> Unit>()

    val snapshot: DisplayRollbackSnapshot
        get() = current.get()

    val state: DisplayRollbackState
        get() = snapshot.state

    val generation: Long
        get() = snapshot.generation

    fun nextGeneration(): Long = generationSequence.incrementAndGet()

    fun addListener(listener: (old: DisplayRollbackSnapshot, new: DisplayRollbackSnapshot, event: DisplayRollbackEvent) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (old: DisplayRollbackSnapshot, new: DisplayRollbackSnapshot, event: DisplayRollbackEvent) -> Unit) {
        listeners.remove(listener)
    }

    /**
     * Atomically transitions the state machine using [DisplayRollbackStateMachine.reduce].
     */
    fun transition(event: DisplayRollbackEvent): DisplayRollbackSnapshot {
        var prev: DisplayRollbackSnapshot
        var next: DisplayRollbackSnapshot
        do {
            prev = current.get()
            next = DisplayRollbackStateMachine.reduce(prev, event)
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

    fun reset(): DisplayRollbackSnapshot = transition(DisplayRollbackEvent.Reset)
}
