# USB Connection & Switch Notifications

Status: **IMPLEMENTED / INTEGRATED / UNIT_TESTED: VERIFIED (5/5 PASSING) / RUNTIME DEVICE VERIFICATION: BLOCKED (NO ANDROID SDK/DEVICE)**

## Purpose
Provide contextual status and quick-access navigation when a relevant USB connection event occurs, strictly disambiguating physical USB cabling from ADB debugging authorization and Shizuku server availability.

## Independent State Modeling (`UsbConnectionState`)
The pure Kotlin reducer (`UsbConnectionReducer.kt`) models connection facets independently:
1. `physicalConnected`: Electrical VBUS connection detected via `UsbManager.ACTION_USB_STATE` (`connected` extra).
2. `usbConfigured`: USB endpoints enumerated by the host (`configured` extra).
3. `adbEnabledOnUsb`: USB debugging function enabled on the USB interface (`adb` extra).
4. `hostDeviceAttached`: External USB device or accessory attached to Android device as host.
5. `wirelessAdbActive`: Wireless ADB port or Wi-Fi debugging detected (orthogonal to physical USB).
6. `shizukuRunning`: Shizuku daemon actively running and Binder responsive.

## Formatting & Truthful Reporting
`UsbConnectionReducer.formatUsbNotificationText` ensures honest reporting:
- Explicitly states that physical USB connection does **NOT** imply ADB authorization or Shizuku daemon availability.
- Surfaces specific states: "USB connected (Charging only)", "USB connected (ADB interface ready)", "USB debugging enabled (Unauthorized)", or "USB connected & Shizuku active".

## Notification & Quick Actions
- Notification Channel: `usb_connection` (`NotificationRegistry.Channels.USB_CONNECTION`, IMPORTANCE_LOW).
- Notification ID: `1461` (`NotificationRegistry.Ids.USB_CONNECTION`).
- Quick Actions:
  1. Open OmniShizuku Dashboard.
  2. Open Developer Options (`Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS`).
- Ongoing notification posted only when preference is enabled, permissions granted, and physical USB or accessory is actively connected; automatically cancelled on disconnect.

## Lifecycle & Broadcast Registration
- Registered dynamically via `UsbConnectionMonitor` using `ContextCompat.RECEIVER_EXPORTED` on Android 13/14+.
- Seeds initial state immediately from sticky `ACTION_USB_STATE` broadcast on application start.
- Enforces 600ms debounce interval to suppress duplicate broadcasts during cable insertion/negotiation.
- Dynamically started/stopped when user toggles `usb_connection_notification_enabled` preference in Settings.

## Verification Status
- `UsbConnectionMonitorTest.kt`: 5/5 tests passing (state reduction, physical vs wireless ADB separation, text formatting truthfulness, debounce handling).
