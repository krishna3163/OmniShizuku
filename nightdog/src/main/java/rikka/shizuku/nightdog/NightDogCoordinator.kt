package rikka.shizuku.nightdog

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

/**
 * Thread-safe atomic coordinator wrapping [NightDogStateMachine].
 * Provides atomic compare-and-set transitions, listener dispatching,
 * generation safety, and pluggable fatal-fault/recovery callbacks.
 */
class NightDogCoordinator(
    initialSnapshot: NightDogSnapshot = NightDogSnapshot(),
    var onFatalFaultAction: ((reason: String) -> Unit)? = null,
    var onServiceRecoveredAction: ((name: String) -> Unit)? = null
) {
    private val current = AtomicReference(initialSnapshot)
    private val generationSequence = AtomicLong(initialSnapshot.generation)
    private val listeners = CopyOnWriteArrayList<(old: NightDogSnapshot, new: NightDogSnapshot, event: NightDogEvent) -> Unit>()

    val snapshot: NightDogSnapshot
        get() = current.get()

    val supervisorState: NightDogSupervisorState
        get() = snapshot.supervisorState

    val generation: Long
        get() = snapshot.generation

    val isSupervising: Boolean
        get() = snapshot.isSupervising

    val isRunning: Boolean
        get() = snapshot.isRunning

    val boundCount: Int
        get() = snapshot.boundCount

    fun nextGeneration(): Long = generationSequence.incrementAndGet()

    fun addListener(listener: (old: NightDogSnapshot, new: NightDogSnapshot, event: NightDogEvent) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (old: NightDogSnapshot, new: NightDogSnapshot, event: NightDogEvent) -> Unit) {
        listeners.remove(listener)
    }

    /**
     * Atomically transitions the state machine using [NightDogStateMachine.reduce].
     * Dispatches callbacks to registered listeners and triggers recovery/fault actions.
     */
    fun transition(event: NightDogEvent): NightDogSnapshot {
        var prev: NightDogSnapshot
        var next: NightDogSnapshot
        do {
            prev = current.get()
            next = NightDogStateMachine.reduce(prev, event)
            if (prev == next) return prev
        } while (!current.compareAndSet(prev, next))

        for (listener in listeners) {
            try {
                listener(prev, next, event)
            } catch (_: Throwable) {
                // Safeguard against listener exceptions
            }
        }

        // Trigger action callbacks if fatal fault or service recovery occurred
        if (event is NightDogEvent.ServiceRecovered) {
            try {
                onServiceRecoveredAction?.invoke(event.name)
            } catch (_: Throwable) {}
        }

        if (next.supervisorState == NightDogSupervisorState.RESTART_REQUESTED &&
            prev.supervisorState != NightDogSupervisorState.RESTART_REQUESTED
        ) {
            val reason = next.fatalReason ?: "fatal_fault"
            try {
                onFatalFaultAction?.invoke(reason)
            } catch (_: Throwable) {}
        }

        return next
    }

    fun reset(): NightDogSnapshot = transition(NightDogEvent.Reset)

    fun stop(): NightDogSnapshot {
        transition(NightDogEvent.StopRequested(generation))
        return transition(NightDogEvent.Stopped)
    }
}
