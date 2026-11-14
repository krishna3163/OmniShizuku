# Helper Notifications

Status: **IMPLEMENTED / INTEGRATED / UNIT_TESTED: VERIFIED (6/6 PASSING) / RUNTIME DEVICE VERIFICATION: BLOCKED (NO ANDROID SDK/DEVICE)**

## Purpose
Inform users when Shizuku stops unexpectedly, needs attention, recovers, or requires genuine user intervention, without requiring the application to remain open.

## Notification Channel & ID
- Channel: `helper_notifications` (`NotificationRegistry.Channels.HELPER`, IMPORTANCE_DEFAULT)
- Notification ID: `1460` (`NotificationRegistry.Ids.HELPER`)

## Preference and Gating
- Preference key: `helper_notifications_enabled` (`ShizukuSettings`), default `false`, independent of other notification preferences.
- Requires both preference enabled and `POST_NOTIFICATIONS` runtime permission on API 33+.
- Checks channel blocking via `NotificationCore.isChannelBlocked(context, channelId)`.
- Disabling helper notifications does not affect Watchdog foreground service, native process recovery, or Home screen `ServerHealth` visibility.

## Architecture & Event Model
Helper notifications are driven by `HelperNotificationPolicy.kt` (pure Kotlin domain model) and managed by `HelperNotificationManager.kt`:
- **Event Types**:
  - `SERVER_STOPPED`: Triggered on unexpected daemon termination or process crash.
  - `NEEDS_ATTENTION`: Triggered when Watchdog recovery circuit breaker trips or retries are exhausted.
  - `RECOVERED`: Triggered when the Shizuku server successfully restarts and Binder rebinds.
  - `PERMISSION_REQUIRED`: Triggered when privileged permissions are revoked or required for startup.
- **Deterministic Dedupe Key**: `type|serverGeneration|errorIdentity|recoveryAttemptId` prevents repeated identical alerts.
- **Debounce Window**: 5,000ms minimum interval between successive notifications.
- **Dismissal Suppression**: When a user dismisses a helper notification, the notification's `deleteIntent` fires `HelperDismissReceiver`, registering the dismissal key in `HelperNotificationPolicy` to suppress re-posting until a new distinct event occurs.
- **Healthy State Auto-Clear**: Transition to running/healthy clears active error notifications and resets suppression state.

## Runtime Lifecycle Wiring
1. **Watchdog Lifecycle**: Wired in `WatchdogService.kt` on unexpected process crash (`SERVER_STOPPED`), circuit breaker tripping (`NEEDS_ATTENTION`), and successful recovery (`RECOVERED`).
2. **Startup Lifecycle**: Wired in `StartStatus.kt` on startup failure (`SERVER_STOPPED` or `PERMISSION_REQUIRED`) and startup success (`RECOVERED`).
3. **Binder Lifecycle**: Wired in `SheveryNotificationManager.kt` on Binder death (`SERVER_STOPPED`) and Binder connection (`RECOVERED`).
4. **Action Intent**: Action button allows immediate one-tap server restart (`AdbStartRun` or `WatchdogService.requestRestart()`).

## Verification Status
- `HelperNotificationPolicyTest.kt`: 6/6 tests passing (deduplication, 5000ms debounce, user dismissal suppression, healthy state reset, restart action evaluation).
