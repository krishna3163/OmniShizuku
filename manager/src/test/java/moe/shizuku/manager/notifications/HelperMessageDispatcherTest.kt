package moe.shizuku.manager.notifications

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HelperMessageDispatcherTest {

    private class FakeClock(var currentTime: Long = 1000L) : HelperMessageDispatcher.Clock {
        override fun elapsedRealtime(): Long = currentTime
    }

    private class ManualScheduler : HelperMessageDispatcher.Scheduler {
        val pendingActions = mutableListOf<() -> Unit>()
        override fun postDelayed(delayMs: Long, action: () -> Unit) {
            pendingActions.add(action)
        }
        fun triggerAll() {
            val copy = ArrayList(pendingActions)
            pendingActions.clear()
            for (action in copy) action()
        }
    }

    private class FakeOverlayController(var allowShow: Boolean = true) : HelperMessageDispatcher.OverlayController {
        val shown = mutableListOf<HelperMessage>()
        var onDoneCallback: (() -> Unit)? = null
        var removeAllCalled = false

        override fun canShow(): Boolean = allowShow

        override fun show(message: HelperMessage, onDone: () -> Unit) {
            shown.add(message)
            onDoneCallback = onDone
        }

        override fun removeAll() {
            removeAllCalled = true
        }

        fun completeCurrent() {
            val cb = onDoneCallback
            onDoneCallback = null
            cb?.invoke()
        }
    }

    private class FakeInAppController : HelperMessageDispatcher.InAppController {
        val shown = mutableListOf<HelperMessage>()
        override fun show(message: HelperMessage) {
            shown.add(message)
        }
    }

    private lateinit var clock: FakeClock
    private lateinit var scheduler: ManualScheduler

    @Before
    fun setUp() {
        HelperMessageDispatcher.resetForTests()
        clock = FakeClock()
        scheduler = ManualScheduler()
        HelperMessageDispatcher.clock = clock
        HelperMessageDispatcher.scheduler = scheduler
    }

    @After
    fun tearDown() {
        HelperMessageDispatcher.resetForTests()
    }

    @Test
    fun duplicateMessages_areDedupedWithinWindow() {
        val overlay = FakeOverlayController(allowShow = true)
        HelperMessageDispatcher.overlayController = overlay

        val m = HelperMessage(type = HelperMessageType.INFO, text = "Test Message", deduplicationKey = "k1")
        HelperMessageDispatcher.enqueue(m)
        assertEquals(1, HelperMessageDispatcher.queueSize() + (if (HelperMessageDispatcher.currentMessage() != null) 1 else 0))

        // Duplicate within 5s
        clock.currentTime += 2000L
        HelperMessageDispatcher.enqueue(m.copy(id = "other_id"))
        assertEquals("Duplicate must be ignored within 5s", 1, HelperMessageDispatcher.queueSize() + (if (HelperMessageDispatcher.currentMessage() != null) 1 else 0))

        // Complete the first message
        overlay.completeCurrent()
        assertNull(HelperMessageDispatcher.currentMessage())

        // Same message after 5s window
        clock.currentTime += 5001L
        HelperMessageDispatcher.enqueue(m.copy(id = "third_id"))
        assertEquals("third_id", HelperMessageDispatcher.currentMessage()?.id)
    }

    @Test
    fun overlayRouting_showsViaOverlayWhenCanShowIsTrue() {
        val overlay = FakeOverlayController(allowShow = true)
        HelperMessageDispatcher.overlayController = overlay

        val msg = HelperMessage(type = HelperMessageType.SUCCESS, text = "Server Running")
        HelperMessageDispatcher.enqueue(msg)

        assertEquals(1, overlay.shown.size)
        assertEquals("Server Running", overlay.shown[0].text)
        assertEquals(msg.id, HelperMessageDispatcher.currentMessage()?.id)

        // Once overlay finishes, current message is cleared
        overlay.completeCurrent()
        assertNull(HelperMessageDispatcher.currentMessage())
    }

    @Test
    fun fallbackRouting_routesToInAppWhenOverlayUnavailableAndAppForeground() {
        val overlay = FakeOverlayController(allowShow = false)
        val inApp = FakeInAppController()
        HelperMessageDispatcher.overlayController = overlay
        HelperMessageDispatcher.inAppController = inApp
        HelperMessageDispatcher.setAppForeground(true)

        val msg = HelperMessage(type = HelperMessageType.WARNING, text = "In-App Notice")
        HelperMessageDispatcher.enqueue(msg)

        assertEquals("Overlay must not receive message when canShow is false", 0, overlay.shown.size)
        assertEquals("In-app controller must receive message as fallback", 1, inApp.shown.size)
        assertEquals("In-App Notice", inApp.shown[0].text)

        // Manual scheduler triggers completion
        scheduler.triggerAll()
        assertNull(HelperMessageDispatcher.currentMessage())
    }

    @Test
    fun backgroundDrop_dropsMessageWhenOverlayDisabledAndAppNotForeground() {
        val overlay = FakeOverlayController(allowShow = false)
        val inApp = FakeInAppController()
        HelperMessageDispatcher.overlayController = overlay
        HelperMessageDispatcher.inAppController = inApp
        HelperMessageDispatcher.setAppForeground(false)

        val msg = HelperMessage(type = HelperMessageType.INFO, text = "Background notice")
        HelperMessageDispatcher.enqueue(msg)

        assertEquals(0, overlay.shown.size)
        assertEquals(0, inApp.shown.size)
        assertNull("Message should be immediately retired", HelperMessageDispatcher.currentMessage())
    }

    @Test
    fun sequentialDraining_processesQueueInFifoOrder() {
        val overlay = FakeOverlayController(allowShow = true)
        HelperMessageDispatcher.overlayController = overlay

        val msg1 = HelperMessage(type = HelperMessageType.INFO, text = "Msg 1", deduplicationKey = "1")
        val msg2 = HelperMessage(type = HelperMessageType.INFO, text = "Msg 2", deduplicationKey = "2")

        HelperMessageDispatcher.enqueue(msg1)
        HelperMessageDispatcher.enqueue(msg2)

        assertEquals(1, overlay.shown.size)
        assertEquals("Msg 1", overlay.shown[0].text)
        assertEquals(1, HelperMessageDispatcher.queueSize())

        // Complete first message -> second message is dispatched immediately
        overlay.completeCurrent()
        assertEquals(2, overlay.shown.size)
        assertEquals("Msg 2", overlay.shown[1].text)
        assertEquals(0, HelperMessageDispatcher.queueSize())

        // Complete second message
        overlay.completeCurrent()
        assertNull(HelperMessageDispatcher.currentMessage())
    }

    @Test
    fun clearQueue_removesPendingAndActiveMessages() {
        val overlay = FakeOverlayController(allowShow = true)
        HelperMessageDispatcher.overlayController = overlay

        HelperMessageDispatcher.enqueue(HelperMessage(type = HelperMessageType.INFO, text = "A", deduplicationKey = "a"))
        HelperMessageDispatcher.enqueue(HelperMessage(type = HelperMessageType.INFO, text = "B", deduplicationKey = "b"))

        HelperMessageDispatcher.clearQueue()
        assertEquals(0, HelperMessageDispatcher.queueSize())
        assertNull(HelperMessageDispatcher.currentMessage())
        assertTrue(overlay.removeAllCalled)
    }
}
