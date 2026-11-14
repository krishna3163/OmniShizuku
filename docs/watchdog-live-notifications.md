# Watchdog Live Activity Notifications

Status: **IMPLEMENTED / INTEGRATED / UNIT_TESTED: VERIFIED (8/8 PASSING) / RUNTIME DEVICE VERIFICATION: BLOCKED (NO ANDROID SDK/DEVICE)**

## Purpose

Reflect real-time Shizuku server and Watchdog supervisor activity directly in the existing Watchdog foreground service ongoing notification without creating competing notifications or flickering the notification shade.

## Preserved Behavior

- Preserves existing foreground service notification channel: `shizuku_watchdog_v2` (IMPORTANCE_LOW) and ID: `1001` (`NotificationRegistry.Ids.WATCHDOG`).
- Preserves existing Watchdog recovery algorithm, restart limits, exponential backoff, circuit breaker, and manual-stop tracking.
- Preserves crash, gave-up, and not-running notification alerts.

## State Mapping (`WatchdogLiveStateMapper`)

Pure Kotlin state reducer maps authoritative subsystem states to discrete, user-facing decisions (`WatchdogLiveDecision`):

- **Running / Monitoring**: Server running with active Binder; displays start method (Root, Wireless ADB, USB ADB) and active health monitoring status.
- **Stopped / Unexpected Crash**: Server stopped without manual stop request; displays crash context and prepares restart.
- **Recovery Pending / Retrying**: Displays active recovery attempt count (`Server stopped - Recovery attempt N of MAX`).
- **Restarting**: Displays active daemon respawn execution status.
- **Successfully Recovered**: Confirms daemon recovery and Binder re-establishment.
- **Circuit Breaker / Paused**: Displays pause notification with manual "Start Now" action button.
- **Manual Stop**: Respects user stop request; displays stopped status without initiating recovery loops.

## Anti-Flicker & Throttling

- Minimum update interval of 1,000ms enforced between non-critical text refreshes to eliminate notification shade jitter.
- Critical status changes (e.g. crash, circuit breaker trip, recovery success) bypass throttling via `forceRefresh` in `WatchdogService.requestNotificationRefresh()`.
- Preference gating: `live_activity_notifications_enabled` (`ShizukuSettings`), default `true`; when disabled, falls back to static baseline notification text.

## Verification Status

- `WatchdogLiveStateMapperTest.kt`: 8/8 tests passing (running state, stopped state, attempt count formatting, circuit breaker mapping, manual stop respect, throttle interval verification).
