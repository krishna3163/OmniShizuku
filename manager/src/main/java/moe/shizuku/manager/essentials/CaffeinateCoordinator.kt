package moe.shizuku.manager.essentials

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

/**
 * Thread-safe atomic coordinator wrapping [CaffeinateStateMachine].
 * Provides generation tracking, transition dispatching, and thread-safe listener updates.
 */
class CaffeinateCoordinator(
    initialSnapshot: CaffeinateSnapshot = CaffeinateSnapshot()
) {
    private val current = AtomicReference(initialSnapshot)
    private val generationSequence = AtomicLong(initialSnapshot.generation)
    private val listeners = CopyOnWriteArrayList<(old: CaffeinateSnapshot, new: CaffeinateSnapshot, event: CaffeinateEvent) -> Unit>()

    val snapshot: CaffeinateSnapshot
        get() = current.get()

    val state: CaffeinateState
        get() = snapshot.state

    val generation: Long
        get() = snapshot.generation

    val isActive: Boolean
        get() = snapshot.isActive

    val isStarting: Boolean
        get() = snapshot.isStarting

    val isRunning: Boolean
        get() = snapshot.isRunning

    fun nextGeneration(): Long = generationSequence.incrementAndGet()

    fun addListener(listener: (old: CaffeinateSnapshot, new: CaffeinateSnapshot, event: CaffeinateEvent) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (old: CaffeinateSnapshot, new: CaffeinateSnapshot, event: CaffeinateEvent) -> Unit) {
        listeners.remove(listener)
    }

    /**
     * Atomically transitions the state machine using [CaffeinateStateMachine.reduce].
     */
    fun transition(event: CaffeinateEvent): CaffeinateSnapshot {
        var prev: CaffeinateSnapshot
        var next: CaffeinateSnapshot
        do {
            prev = current.get()
            next = CaffeinateStateMachine.reduce(prev, event)
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

    fun stop(reason: String = "User requested"): CaffeinateSnapshot =
        transition(CaffeinateEvent.StopRequested(reason))

    fun onScreenOff(currentTimeMs: Long): CaffeinateSnapshot =
        transition(CaffeinateEvent.ScreenTurnedOff(currentTimeMs))

    fun onWakeLockStateChanged(held: Boolean): CaffeinateSnapshot =
        transition(CaffeinateEvent.WakeLockStateChanged(generation, held))

    fun reset(): CaffeinateSnapshot = transition(CaffeinateEvent.Reset)
}
