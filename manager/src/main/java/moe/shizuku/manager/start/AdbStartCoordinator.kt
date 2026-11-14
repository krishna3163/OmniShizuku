package moe.shizuku.manager.start

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference

/**
 * Thread-safe atomic state coordinator wrapping [AdbStartStateMachine].
 * Provides atomic compare-and-set state transitions, listener dispatches,
 * and generation-safe execution tracking.
 */
class AdbStartCoordinator(
    initialSnapshot: AdbStartSnapshot = AdbStartSnapshot()
) {
    private val current = AtomicReference(initialSnapshot)
    private val listeners = CopyOnWriteArrayList<(old: AdbStartSnapshot, new: AdbStartSnapshot, event: AdbStartEvent) -> Unit>()

    val snapshot: AdbStartSnapshot
        get() = current.get()

    val state: AdbStartState
        get() = snapshot.state

    val generation: Long
        get() = snapshot.generation

    fun addListener(listener: (old: AdbStartSnapshot, new: AdbStartSnapshot, event: AdbStartEvent) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (old: AdbStartSnapshot, new: AdbStartSnapshot, event: AdbStartEvent) -> Unit) {
        listeners.remove(listener)
    }

    /**
     * Atomically transitions the state machine using [AdbStartStateMachine.reduce].
     * Returns the updated [AdbStartSnapshot].
     */
    fun transition(event: AdbStartEvent): AdbStartSnapshot {
        var prev: AdbStartSnapshot
        var next: AdbStartSnapshot
        do {
            prev = current.get()
            next = AdbStartStateMachine.reduce(prev, event)
            if (prev == next) return prev
        } while (!current.compareAndSet(prev, next))

        for (listener in listeners) {
            try {
                listener(prev, next, event)
            } catch (_: Throwable) {
                // Prevent listener errors from corrupting state machine transitions
            }
        }
        return next
    }

    /**
     * Reset coordinator back to clean IDLE state.
     */
    fun reset(): AdbStartSnapshot = transition(AdbStartEvent.Reset)

    /**
     * Request cancellation of the current attempt.
     */
    fun cancel(reason: String): AdbStartSnapshot =
        transition(AdbStartEvent.CancelRequested(generation = snapshot.generation, reason = reason))
}
