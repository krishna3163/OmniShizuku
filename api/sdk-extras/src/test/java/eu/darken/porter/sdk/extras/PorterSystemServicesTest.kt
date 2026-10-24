package eu.darken.porter.sdk.extras

import android.os.Binder
import android.os.IBinder
import java.util.concurrent.atomic.AtomicInteger
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The reflective lookup, the cache in front of it, and what each failure becomes. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
internal class PorterSystemServicesTest {

    @After
    fun teardown() {
        PorterSystemServices.lookup = PorterSystemServices.reflectiveLookup()
        PorterSystemServices.clearCacheForTest()
    }

    @Test
    fun theHiddenClassAndMethodResolve() {
        assertNotNull(PorterSystemServices.reflectiveLookup())
    }

    @Test
    fun theRealLookupAnswersForAKnownService() {
        assertNotNull(PorterSystemServices.getSystemService(REGISTERED))
    }

    @Test
    fun theRealLookupAnswersNullForAnUnknownService() {
        assertNull(PorterSystemServices.getSystemService(UNREGISTERED))
    }

    @Test
    fun anUnreachableLookupThrows() {
        PorterSystemServices.lookup = null

        assertThrows(IllegalStateException::class.java) { PorterSystemServices.getSystemService(REGISTERED) }
    }

    @Test
    fun theBinderTheLookupAnswersIsReturned() {
        val binder: IBinder = Binder()
        PorterSystemServices.lookup = PorterSystemServices.ServiceLookup { binder }

        assertSame(binder, PorterSystemServices.getSystemService(REGISTERED))
    }

    @Test
    fun aSecondCallIsServedFromTheCache() {
        val binder: IBinder = Binder()
        val calls = AtomicInteger()
        PorterSystemServices.lookup = PorterSystemServices.ServiceLookup {
            calls.incrementAndGet()
            binder
        }

        assertSame(binder, PorterSystemServices.getSystemService(REGISTERED))
        assertSame(binder, PorterSystemServices.getSystemService(REGISTERED))
        assertEquals(1, calls.get())
    }

    @Test
    fun aMissingServiceIsAskedAboutAgain() {
        val binder: IBinder = Binder()
        val calls = AtomicInteger()
        PorterSystemServices.lookup = PorterSystemServices.ServiceLookup {
            if (calls.incrementAndGet() == 1) null else binder
        }

        assertNull(PorterSystemServices.getSystemService(REGISTERED))
        assertSame(binder, PorterSystemServices.getSystemService(REGISTERED))
    }

    @Test
    fun aFailedLookupThrowsCarryingItsCause() {
        val failure = ReflectiveOperationException("denied")
        PorterSystemServices.lookup = PorterSystemServices.ServiceLookup { throw failure }

        val thrown = assertThrows(IllegalStateException::class.java) { PorterSystemServices.getSystemService(REGISTERED) }
        assertSame(failure, thrown.cause)
    }

    private companion object {
        /** Registered by Robolectric's ShadowServiceManager, unlike "package". */
        const val REGISTERED = "power"
        const val UNREGISTERED = "eu.darken.porter.no.such.service"
    }
}
