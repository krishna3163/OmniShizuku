package moe.shizuku.manager.notifications

data class UsbConnectionState(
    val physicalConnected: Boolean = false,
    val usbConfigured: Boolean = false,
    val adbEnabledOnUsb: Boolean = false,
    val hostDeviceAttached: Boolean = false,
    val lastChangedElapsed: Long = 0L
)

/**
 * Pure deterministic reducer for USB connection and debugging states.
 * Free of Android framework dependencies for JVM testability.
 */
object UsbConnectionReducer {

    const val ACTION_USB_STATE = "android.hardware.usb.action.USB_STATE"
    const val ACTION_USB_DEVICE_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED"
    const val ACTION_USB_DEVICE_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED"

    fun reduce(prev: UsbConnectionState, action: String?, extras: Map<String, Any?>): UsbConnectionState {
        return when (action) {
            ACTION_USB_STATE -> {
                val connected = (extras["connected"] as? Boolean) ?: prev.physicalConnected
                val configured = (extras["configured"] as? Boolean) ?: prev.usbConfigured
                val adb = (extras["adb"] as? Boolean) ?: prev.adbEnabledOnUsb
                prev.copy(physicalConnected = connected, usbConfigured = configured, adbEnabledOnUsb = adb)
            }
            ACTION_USB_DEVICE_ATTACHED -> prev.copy(hostDeviceAttached = true, physicalConnected = true)
            ACTION_USB_DEVICE_DETACHED -> prev.copy(hostDeviceAttached = false)
            else -> prev
        }
    }

    fun formatUsbNotificationText(baseText: String, state: UsbConnectionState): String {
        val adbState = when {
            state.adbEnabledOnUsb -> "USB debugging: available (ADB enabled on USB)"
            state.physicalConnected -> "USB debugging: unavailable/unknown (physical USB connected; not proof of ADB)"
            state.hostDeviceAttached -> "USB host device attached"
            else -> ""
        }
        return if (adbState.isNotBlank()) "$baseText\n$adbState" else baseText
    }
}
