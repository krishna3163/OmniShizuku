package moe.shizuku.manager.receiver

/** What the unlock does about a start that the foreground service began before it; see [decideLockedRunAtUnlock]. */
enum class LockedRunAtUnlock {
    /** No such start is under way: the unlock asks for a start of its own, as it always did. */
    NONE,

    /** It has reached STARTING (or the server is up): it is ahead of anything the unlock could begin. */
    KEEP,

    /** It has not got there yet, but has not had its time: look again. */
    WAIT,

    /** It is not going to get there in time: cancel it and begin the ordinary start. */
    REPLACE
}

/**
 * How long a start begun before the unlock gets, after the unlock, to reach STARTING.
 *
 * Ten seconds because that is what the ordinary start costs: measured on the phone this was written
 * for, the unlock came at +33.5 s since boot and STARTING at +40.9 s, 7.4 s later, of which 4.8 s
 * were the hotspot. A start from the service that has not reached STARTING by then is slower than
 * beginning again would be, so it has no claim to the single slot it holds: whatever it is stuck on
 * (the hotspot, discovery that is not delivered to a locked app) is not going to be cured by the
 * unlock. The margin over 7.4 s is for the same start to be slower on another boot.
 */
const val LOCKED_RUN_GRACE_MS = 10_000L

/**
 * What to do, at some moment after the unlock, about a start the service began before it.
 *
 * The service's start and the unlock's start must not both run, and the service's is the better one
 * while it is making progress - it began earlier. The unlock's own request joins it
 * ([decideStart]); this is the other half, which makes sure joining it does not mean waiting for a
 * start that has gone nowhere.
 *
 * [reachedStarting] is the state machine at STARTING or RUNNING, which is where the starter has
 * been sent or the server is up; before that the run is still finding a port or connecting.
 */
internal fun decideLockedRunAtUnlock(
    runInFlight: Boolean,
    reachedStarting: Boolean,
    msSinceUnlock: Long,
    graceMs: Long = LOCKED_RUN_GRACE_MS
): LockedRunAtUnlock = when {
    !runInFlight -> LockedRunAtUnlock.NONE
    reachedStarting -> LockedRunAtUnlock.KEEP
    msSinceUnlock < graceMs -> LockedRunAtUnlock.WAIT
    else -> LockedRunAtUnlock.REPLACE
}
