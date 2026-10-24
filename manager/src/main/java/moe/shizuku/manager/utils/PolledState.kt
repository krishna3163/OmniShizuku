package moe.shizuku.manager.utils

/**
 * What a binder poll means for the state machine, kept apart from it so it can be checked without
 * a device.
 *
 * A failed ping is only news about a server that was meant to be up. During STARTING it is the
 * normal answer: the starter has been told to run, the server is still booting, and the binder
 * takes several seconds to arrive after "Server started". Turning that into STOPPED made every
 * poll that landed in the window - the boot receiver's, the watchdog's, the ten second check in
 * `Starter.waitForBinder` - announce a stop that had not happened, and the next start then saw a
 * stopped server and launched a second one over the one coming up.
 *
 * The grace is bounded so that a STARTING which nothing ever resolves (a start that died without
 * saying so) cannot hold the state forever: once it has run out, the poll answers STOPPED again.
 *
 * @param startingAgeMs how long the state has been STARTING, or null when it is not
 */
internal fun resolvePolledState(
    current: ShizukuStateMachine.State,
    binderAlive: Boolean,
    startingAgeMs: Long?,
    graceMs: Long
): ShizukuStateMachine.State = when {
    binderAlive -> ShizukuStateMachine.State.RUNNING
    current == ShizukuStateMachine.State.STARTING &&
        startingAgeMs != null && startingAgeMs < graceMs -> ShizukuStateMachine.State.STARTING
    else -> ShizukuStateMachine.State.STOPPED
}
