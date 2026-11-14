package moe.shizuku.manager.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UsbConnectionMonitorTest {

    @Test
    fun reduce_usbState_updatesDistinctFields() {
        val prev = UsbConnectionState()
        val extras = mapOf(
            "connected" to true,
            "configured" to true,
            "adb" to true
        )
        val next = UsbConnectionReducer.reduce(prev, UsbConnectionReducer.ACTION_USB_STATE, extras)
        assertTrue(next.physicalConnected)
        assertTrue(next.usbConfigured)
        assertTrue(next.adbEnabledOnUsb)
        assertFalse(next.hostDeviceAttached)
    }

    @Test
    fun reduce_physicalConnectedWithoutAdb_doesNotAssumeAdb() {
        val prev = UsbConnectionState()
        val extras = mapOf(
            "connected" to true,
            "configured" to true,
            "adb" to false
        )
        val next = UsbConnectionReducer.reduce(prev, UsbConnectionReducer.ACTION_USB_STATE, extras)
        assertTrue("Physical USB must be true", next.physicalConnected)
        assertTrue("USB configured must be true", next.usbConfigured)
        assertFalse("ADB over USB must NOT be assumed without adb extra", next.adbEnabledOnUsb)

        val formattedText = UsbConnectionReducer.formatUsbNotificationText("OmniShizuku USB", next)
        assertTrue(formattedText.contains("not proof of ADB"))
    }

    @Test
    fun reduce_withAdbEnabled_formatsAdbAvailableText() {
        val state = UsbConnectionState(
            physicalConnected = true,
            usbConfigured = true,
            adbEnabledOnUsb = true
        )
        val formattedText = UsbConnectionReducer.formatUsbNotificationText("OmniShizuku USB", state)
        assertTrue(formattedText.contains("available (ADB enabled on USB)"))
    }

    @Test
    fun reduce_detach_clearsHostAttachmentOnly() {
        val prev = UsbConnectionState(
            physicalConnected = true,
            usbConfigured = true,
            adbEnabledOnUsb = true,
            hostDeviceAttached = true
        )
        val next = UsbConnectionReducer.reduce(prev, UsbConnectionReducer.ACTION_USB_DEVICE_DETACHED, emptyMap())
        assertFalse(next.hostDeviceAttached)
        assertTrue(next.physicalConnected)
        assertTrue(next.usbConfigured)
        assertTrue(next.adbEnabledOnUsb)
    }

    @Test
    fun reduce_attach_marksHostDeviceAndPhysical() {
        val prev = UsbConnectionState()
        val next = UsbConnectionReducer.reduce(prev, UsbConnectionReducer.ACTION_USB_DEVICE_ATTACHED, emptyMap())
        assertTrue(next.hostDeviceAttached)
        assertTrue(next.physicalConnected)
    }
}
