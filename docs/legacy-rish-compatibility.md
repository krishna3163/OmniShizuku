# OmniShizuku — Legacy Rish Compatibility

## Purpose
Legacy Rish Compatibility keeps manager-side availability for the existing legacy Rish Binder request flow while the Shizuku server is running. It does not replace or change the Binder relay contract (`rikka.shizuku.intent.action.REQUEST_BINDER`, `${applicationId}.intent.action.REQUEST_BINDER`, `moe.shizuku.privileged.api.intent.action.REQUEST_BINDER`) implemented by `ShellBinderRequestHandler`, `ShellRequestHandlerActivity`, and `ShizukuReceiver`.

## Components
- `LegacyRishCompatibilityStateMachine` (`legacy/LegacyRishCompatibilityStateMachine.kt`): pure reducer/state model.
- `LegacyRishCompatibilityManager` (`legacy/LegacyRishCompatibilityManager.kt`): observes `ShizukuStateMachine` and Shizuku binder callbacks, persists/reconciles the `legacy_rish_compatibility` preference, and controls the compatibility service via an injectable controller.
- `LegacyRishCompatibilityService` (`legacy/LegacyRishCompatibilityService.kt`): foreground service with low-importance notification channel `OmniShizuku - Legacy Rish Compatibility`.

## Lifecycle Behavior
- Preference disabled: state `DISABLED`; service stopped; no keep-alive claim.
- Preference enabled, server/binder unavailable: state `WAITING_FOR_SERVER`; no foreground service is kept running.
- Server running + binder available + enabled: manager requests service start (`STARTING`) and becomes `ACTIVE` only after `ServiceStarted` is reported for the current generation.
- Server shutdown or binder loss: transitions to `WAITING_FOR_SERVER` (or reconnect/wait path) and service is stopped/reconciled; existing Binder relay cannot provide a binder when Shizuku is unavailable.
- Binder death/reconnect: stale events are rejected by generation checks; ready server+binder triggers start/reconnect.
- Process recreation: `ShizukuApplication`, boot/unlock receivers, and service startup call `init/refresh`; manager re-reads preference and current server/binder state before reconciling.
- Boot/unlock: preference is restored and reconciled only against actual server/binder state; direct-boot/background-start failures are reported as errors and do not claim ACTIVE.

## Notification
- Channel: `OmniShizuku - Legacy Rish Compatibility` (`IMPORTANCE_LOW`, no sound, no vibration, no badge/heads-up).
- Title: `OmniShizuku Compatibility`.
- Active text: `Keeping legacy Rish shell Binder available for compatible apps.`
- Includes open-app content intent and Disable action.
- Notification/state text is derived from observed snapshot; `ACTIVE` is not shown solely because the service started.

## Settings UI
- Added in Compose settings (`ui/screen/SettingsScreen.kt`) as a distinct control from Legacy Pairing (`KEY_LEGACY_PAIRING`).
- Shows enable/disable, server status, compatibility lifecycle, binder availability, service status, last event, and error/recovery text from `LegacyRishCompatibilityManager` snapshot.

## Limitations / Verification Status
- No public AIDL/protocol changes.
- Continuity Bridge is out of scope.
- End-to-end real MT Manager/legacy client compatibility requires device-side/manual verification and was not performed in this environment.
- Android SDK is unavailable in this environment, so compile/test/lint/assemble/manifest validation runs are **BLOCKED** (not PASS).
