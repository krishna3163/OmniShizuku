package eu.darken.porter.porsh

import java.util.concurrent.CountDownLatch

/**
 * A [PorshHost] that never forks. `start()` is the only member that reaches JNI, so
 * overriding it is enough to exercise the registry off-device.
 */
internal open class FakeHost(override val pid: Int) : PorshHost(emptyArray(), null, null, 0, null, null, null) {

    /** Opens once a thread has entered awaitExitCode on this host. */
    val awaitEntered = CountDownLatch(1)

    @Volatile
    var lastWindowSize = -1L

    @Volatile
    var exitedAt = 0L

    override fun start() {
    }

    override fun setWindowSize(size: Long) {
        lastWindowSize = size
    }

    override val exitedAtMillis: Long
        get() = exitedAt

    override fun awaitExitCode(timeoutMillis: Long): Int {
        awaitEntered.countDown()
        return super.awaitExitCode(timeoutMillis)
    }
}
