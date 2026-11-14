# Notifications Architecture (Phase 7 Production Integration)

Status: **IMPLEMENTED / INTEGRATED / UNIT_TESTED: VERIFIED (33/33 PASSING) / RUNTIME DEVICE VERIFICATION: BLOCKED (NO ANDROID SDK/DEVICE)**

## Overview
OmniShizuku notification architecture integrates and hardens notification infrastructure across the entire Android application lifecycle without replacing existing working notification channels. Components reside under `manager/src/main/java/moe/shizuku/manager/notifications/`.

## Authoritative Components
- `NotificationRegistry` — Authoritative channel and notification ID registry derived from codebase audit; maps all 23 managed IDs and 23 notification channels with guaranteed non-collision.
- `NotificationMigration` — Pure idempotent upgrade migration engine that cancels exact obsolete notification IDs (`1002`, `1005`, `1458`) once per upgrade, tracking migration in SharedPreferences with mockable interfaces.
- `NotificationCore` — Core notification utilities: channel creation (`createAllChannels`), blocked channel checks (`isChannelBlocked`, `isChannelEnabled`), permission queries (`canShowNotifications`, `canShowUsb`), and independent preferences (`NotificationPreferences`).
- `HelperNotificationPolicy` — Pure domain policy defining event types (`SERVER_STOPPED`, `NEEDS_ATTENTION`, `RECOVERED`, `PERMISSION_REQUIRED`), deduplication keys, 5000ms debounce windows, user dismissal suppression, and automatic clearance when healthy.
- `HelperNotificationManager` — Production notification controller wired to `HelperNotificationPolicy`, `WatchdogService`, `StartStatus`, and `SheveryNotificationManager`. Dispatches delete intents to `HelperDismissReceiver` to record user dismissals.
- `HelperDismissReceiver` — Manifest-registered broadcast receiver recording notification dismissals in `HelperNotificationPolicy`.
- `HelperMessageDispatcher` — Non-blocking FIFO message dispatcher with clock and scheduler abstractions for pure JVM testability, overlay routing, and in-app Compose fallback.
- `OverlayMessageController` — WindowManager overlay using `TYPE_APPLICATION_OVERLAY` with least-privileged flags (`FLAG_NOT_FOCUSABLE or FLAG_NOT_TOUCHABLE or FLAG_LAYOUT_IN_SCREEN`), timed removal, and clean revocation teardown.
- `ServerHealth` — Single source of truth for dashboard health, combining Shizuku state, Binder pings, Watchdog state, and startup status independent of notification preferences.
- `UsbConnectionState` & `UsbConnectionReducer` — Pure Kotlin USB state model and reducer disambiguating physical USB connection, USB configuration, ADB interface, host/accessory attach, wireless ADB, and Shizuku server availability.
- `UsbConnectionMonitor` — USB lifecycle monitor using `ACTION_USB_STATE` sticky broadcast seeding, dynamic registration with `RECEIVER_EXPORTED`, and 600ms debouncing.
- `UsbConnectionNotificationManager` — Contextual ongoing notification manager posting on channel `usb_connection` (ID 1461) with quick-action intents for Dashboard and Developer Options.
- `WatchdogLiveStateMapper` — Pure Kotlin live state mapper translating watchdog recovery states, attempt counts, and server status into distinct user-facing decisions without fake progress.
- `NotificationLifecycleOwner` — Application-level lifecycle observer managing startup migration, overlay permission checks, process foreground/background tracking, and USB monitor reconciliation.

## Preserved Existing Systems
- Watchdog foreground service ongoing notification (`shizuku_watchdog_v2`, ID 1001) and recovery/crash/gave-up/not-running notifications preserved.
- `IslandService` reassigned from ID 1001 to ID 1459 (`NotificationRegistry.Ids.ISLAND`) to eliminate collision with Watchdog.
- ADB pairing notifications/results in `AdbPairingService` (`adb_pairing`, ID 1) preserved; pairing results remain normal notifications regardless of overlay setting.
- Legacy Rish compatibility notification (`legacy_rish_compatibility`, ID 1458) preserved.
- Shevery and Startup control notifications reassigned to IDs 1456 and 1457 to eliminate collision with Watchdog crash/not-running IDs.
- USB debugging auto-hide, ADB start/receiver, call recording, caffeinate, display rollback, and auth notifications preserved.

## Production Lifecycle Integration
- **Application Startup**: `NotificationLifecycleOwner.init(context)` executed in `ShizukuApplication.onCreate()`, running `NotificationMigration` and initializing channels.
- **Process Lifecycle**: `ProcessLifecycleOwner` observers update `HelperMessageDispatcher.setAppForeground()` and trigger permission reconciliation on resume.
- **Settings Screen**: Toggle switches for all four independent preferences (`helper_notifications_enabled`, `messages_over_other_apps_enabled`, `usb_connection_notification_enabled`, `live_activity_notifications_enabled`), with inline warning banners when notification channels are blocked by the user in system settings.
- **Watchdog Service**: Integrated `WatchdogLiveStateMapper` for real-time notification text and shade flicker throttling. Wired crash and circuit breaker events directly to `HelperNotificationManager`.
- **Start Engine**: Wired start failures and successes in `StartStatus.kt` to `HelperNotificationManager`.
- **Binder Lifecycle**: Wired Binder disconnect and reconnect events in `SheveryNotificationManager.kt` to `HelperNotificationManager`.

## Verification Status
- **Pure JVM Unit Tests**: 33 tests across 6 suites passing:
  - `NotificationRegistryTest`: 4/4 passing (channel uniqueness, ID uniqueness, collision prevention).
  - `NotificationMigrationTest`: 3/3 passing (idempotency, obsolete ID cancellation, non-repetition).
  - `HelperNotificationPolicyTest`: 6/6 passing (deduplication, debounce, dismissal suppression, healthy reset).
  - `HelperMessageDispatcherTest`: 6/6 passing (FIFO queue, overlay routing, in-app fallback, dedupe window).
  - `UsbConnectionMonitorTest`: 5/5 passing (reducer states, physical vs wireless ADB disambiguation, text formatting).
  - `WatchdogLiveStateMapperTest`: 8/8 passing (running, stopped, recovery attempts, circuit breaker, throttling).
- **Subsystem & Cross-Subsystem Suite**: Total 226 unit tests passing across all 19 test suites in 1.08s.
- **Android Device Verification**: Blocked due to lack of physical Android device / emulator in execution environment.
