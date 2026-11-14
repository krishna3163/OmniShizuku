# Phase 7B Report — Notification Production Integration Status

Generated: 2026-11-14
Status: **PARTIAL / BLOCKED (verification)**

## Summary
Phase 7B continued audit and integration hardening for notification production readiness. Work focused on repository/build-environment discovery, production audit documentation, and targeted notification code fixes. End-to-end verification remains blocked by missing Android SDK/device.

## Completed in this pass
- Created `docs/ANDROID_BUILD_ENVIRONMENT_REPORT.md` with discovered environment, required SDK/toolchain matrix, and reproducible setup/verification steps.
- Created `docs/PRODUCTION_READINESS_AUDIT.md` covering implemented/partial/missing components, blockers, test inventory, and prioritized plan.
- Notification code fixes:
  - `notifications/UsbConnectionMonitor.kt`: removed duplicate `UsbConnectionState` definition, delegated state/reduction/formatting to `UsbConnectionReducer`, improved register/unregister reconciliation behavior (callback notified with current/sticky-seeded state), debounce/receiver handling preserved.
  - `notifications/UsbConnectionNotificationManager.kt`: uses `UsbConnectionReducer.formatUsbNotificationText` (single formatter).
  - `notifications/HelperDismissReceiver.kt`: passes `Context` to `HelperNotificationManager.onDismissed(...)` so dismissal state can be persisted.

## Preserved constraints
- Existing notification IDs/channels/FGS behavior for watchdog, ADB pairing, ADB start, USB debugging hide, legacy Rish, recording, etc. preserved in inspected code; reassigned IDs remain `SHEVERY_CONTROL=1456`, `STARTUP=1457`, `ISLAND=1459` per registry.
- Migration remains idempotent and does not cancel active watchdog IDs 1002/1005.
- No destructive git operations performed; existing modified/untracked files preserved.

## Remaining gaps (incomplete)
- Full authoritative wiring of all notification producers/cancellations/PendingIntents through `NotificationRegistry` (many producers still define local constants; consistency audit/fix required).
- Helper notifications: complete all required genuine sources (startup failures, binder reconnect paths, permission-required/critical ADB failures as specified), generation-aware dedupe across sources, persistent dismissal in all paths, blocked-channel/permission handling, no fabricated/stale warnings.
- Overlay/messages: explicit consent explanation before request, permission refresh/revocation detection and immediate removal, Compose/in-app fallback integration, queue drain/priority/timeout/cleanup, foreground/background behavior proof.
- USB/ADB correlation: full lifecycle registration/reconciliation (app start/foreground/background/process recreation/boot/unlock/pref/permission/channel changes), independent authoritative states (physical USB, USB debugging enabled, ADB over USB availability, actual ADB connection, wireless debugging, Shizuku server state), debounce/duplicate prevention, correct PendingIntent navigation.
- Watchdog live activity: complete all required genuine states/content fingerprint throttling/stale-generation safety while preserving recovery logic and ongoing notification identity.
- Settings/Home: permission/channel/overlay status refresh after system settings return, immediate reconciliation, clear-history correctness, Home health independence verification.
- Tests: add/complete required unit/Robolectric/instrumentation cases and execute when environment available.

## Verification
| Check | Status | Notes |
|---|---|---|
| `:manager:compileDebugKotlin` | NOT_RUN | Android SDK missing (`ANDROID_HOME`/`local.properties` absent) |
| `:manager:testDebugUnitTest` | NOT_RUN | Blocked by SDK |
| `:manager:lintDebug` | NOT_RUN | Blocked by SDK |
| `:manager:assembleDebug` | NOT_RUN | Blocked by SDK |
| `:manager:connectedDebugAndroidTest` | NOT_TESTED | No SDK/emulator/device |
| Runtime notification/overlay/USB/watchdog/Legacy Rish scenarios | NOT_TESTED | No device |

## Git state
- Existing modified/untracked files preserved.
- New/updated docs in this pass: `docs/ANDROID_BUILD_ENVIRONMENT_REPORT.md`, `docs/PRODUCTION_READINESS_AUDIT.md`, `docs/PHASE_7B_REPORT.md`.
