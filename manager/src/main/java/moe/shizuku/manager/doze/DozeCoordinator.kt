package moe.shizuku.manager.doze

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference

/**
 * Thread-safe atomic state coordinator wrapping [DozeStateMachine].
 * Provides atomic compare-and-set state transitions, listener dispatches,
 * generation safety, and clean isolation from raw timers and Android framework locks.
 */
class DozeCoordinator(
    initialSnapshot: DozeSnapshot = DozeSnapshot()
) {
    private val current = AtomicReference(initialSnapshot)
    private val listeners = CopyOnWriteArrayList<(old: DozeSnapshot, new: DozeSnapshot, event: DozeEvent) -> Unit>()

    val snapshot: DozeSnapshot
        get() = current.get()

    val state: DozeState
        get() = snapshot.state

    val generation: Long
        get() = snapshot.generation

    val isDozing: Boolean
        get() = snapshot.isDozing

    fun addListener(listener: (old: DozeSnapshot, new: DozeSnapshot, event: DozeEvent) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (old: DozeSnapshot, new: DozeSnapshot, event: DozeEvent) -> Unit) {
        listeners.remove(listener)
    }

    /**
     * Atomically transitions the state machine using [DozeStateMachine.reduce].
     * Dispatches callbacks to registered listeners on state modification.
     */
    fun transition(event: DozeEvent): DozeSnapshot {
        var prev: DozeSnapshot
        var next: DozeSnapshot
        do {
            prev = current.get()
            next = DozeStateMachine.reduce(prev, event)
            if (prev == next) return prev
        } while (!current.compareAndSet(prev, next))

        for (listener in listeners) {
            try {
                listener(prev, next, event)
            } catch (_: Throwable) {
                // Safeguard against listener exceptions disrupting state progression
            }
        }
        return next
    }

    /**
     * Reset coordinator back to clean INACTIVE state.
     */
    fun reset(): DozeSnapshot = transition(DozeEvent.Reset)

    /**
     * Notify coordinator that the host service is stopping.
     */
    fun stopService(): DozeSnapshot = transition(DozeEvent.ServiceStopped)
}
