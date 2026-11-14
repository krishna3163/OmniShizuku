# OmniShizuku Production Readiness Audit

Generated: 2026-11-14 (local audit, no codebase reset)
Repository: `main` @ `35885d2` (working tree dirty; all existing modified/untracked files preserved)

## 1. Scope & Method
- Inspected repository structure, git state, Gradle/AGP/Kotlin config, CI workflows, AndroidManifest, notification modules/producers, Legacy Rish components, watchdog/start flows, and existing tests/docs.
- Did **not** modify unrelated code, did not install SDK/packages, did not run destructive git commands, and did not claim build/test success without execution evidence.

## 2. Implemented / Present Components

| Area | Components | Status | Evidence (static) |
|---|---|---|---|
| Reliability refactors | Call recording, watchdog recovery machine/guards, ADB pairing state machine, deterministic tests authored; CI lint/report steps present | PARTIAL (implemented/authored, verification blocked) | `manager/src/.../callrecorder/*`, `service/WatchdogRecoveryMachine.kt`, `adb/AdbPairingStateMachine.kt`, tests under `manager/src/test/...`, `.github/workflows/android.yml` |
| Legacy Rish Phase 1 | `LegacyRishCompatibilityStateMachine/Manager/Service`, settings/UI/app/boot/unlock/manifest wiring, docs/tests | PARTIAL | `manager/src/.../legacy/*`, `ShizukuApplication.kt`, receivers, `SettingsScreen.kt`, `AndroidManifest.xml`, `docs/legacy-rish-compatibility.md`, `manager/src/test/.../legacy/*` |
| Notification architecture scaffold | `NotificationRegistry`, `NotificationCore`, `NotificationMigration`, `NotificationLifecycleOwner`, helper/overlay/USB/watchdog-live modules, prefs/UI/docs/tests authored | PARTIAL | `manager/src/.../notifications/*`, `ShizukuSettings.java`, `HomeScreen.kt`, `SettingsScreen.kt`, `docs/notifications-architecture.md` et al., `manager/src/test/.../notifications/*` |
| Existing notification producers preserved | Watchdog FGS/crash/recovery/gave-up/not-running, ADB pairing, ADB start, USB debugging hide, legacy Rish, recording/accessibility/hiding/auth flows | PARTIAL/INTEGRATED (needs audit against registry) | `service/WatchdogService.kt`, `adb/AdbPairingService.kt`, `receiver/ShizukuReceiverStarter.kt`, `start/AdbStartRun.kt`, `settings/UsbDebuggingHideService.kt`, manifest |

## 3. Partially Implemented / Integration Gaps

| Gap | Details | Priority |
|---|---|---|
| Notification producer/channel/ID authority | Many producers still use local `NOTIFICATION_ID/CHANNEL_ID` constants (e.g. `WatchdogService`, `AdbPairingService`, `AdbStartRun`, `ShizukuReceiverStarter`, `UsbDebuggingHideService`) rather than consistently routing through `NotificationRegistry/NotificationCore`; PendingIntent/request-code consistency not fully audited. | High |
| Helper event integration | Some watchdog/start-status hooks exist, but not all required authoritative sources are fully wired with generation-aware dedupe, persistent dismissal/delete-intent correctness across all paths, permission/channel-block handling, and no fabricated notifications. | High |
| Overlay/messages lifecycle | `OverlayMessageController`/dispatcher exist but explicit consent explanation flow, permission refresh/revocation handling, queue drain/priority/timeout/cleanup, foreground/background behavior, and Compose/in-app fallback integration need completion and proof. | High |
| USB/ADB correlation | `UsbConnectionMonitor`/`UsbConnectionNotificationManager` need full lifecycle registration/unregistration reconciliation (boot/unlock/prefs/permission/channel/process restart), independent states for physical USB/USB debugging/ADB-over-USB/wireless debugging/wireless ADB/Shizuku server, debounce/duplicate prevention, and correct navigation actions to existing screens. | High |
| Watchdog live activity | Partial live text refresh integrated; must cover all required genuine states, state/content fingerprint throttling without fake progress, stale-generation protection, and preserve recovery logic/ongoing identity/channels/IDs. | High |
| Settings/Home behavior | Four prefs/status rows exist; permission/channel/overlay status refresh after returning from system settings, immediate reconciliation, clear-history correctness, and Home health independence need verification/hardening. | Medium |
| Migration safety | `NotificationMigration` currently uses generic version/key and placeholder obsolete IDs; must be safe/idempotent, not cancel still-owned IDs (1002/1005 remain active watchdog IDs), and align with required migration marker behavior. | High |
| Tests/docs completeness | Notification/legacy tests authored but not compiled/executed in this environment; required docs `PRODUCTION_READINESS_AUDIT.md` (this), `FINAL_SECURITY_RELIABILITY_AUDIT.md`, `FINAL_PRODUCTION_READINESS_REPORT.md`, `PHASE_7B_REPORT.md` missing/incomplete. | High |

## 4. Build / Environment Blockers

| Blocker | Current state | Impact |
|---|---|---|
| Android SDK | `ANDROID_HOME`/`ANDROID_SDK_ROOT` unset; no SDK found (`%LOCALAPPDATA%\Android\Sdk`, `C:\Android\Sdk`, `D:\Android\Sdk` absent); no `local.properties`; `sdkmanager`/`avdmanager`/`emulator` not on PATH | Cannot run compile, unit tests, lint, assembleDebug, instrumentation |
| Platform tools partial | `adb` present at `C:\ProgramData\platform-tools\adb.exe` (37.0.1); no devices attached | Device verification not possible |
| JDK/Gradle | JDK 25.0.4.1 JBR active; Gradle wrapper 9.8.2; CI uses JDK 21 (AGP 9.5.0-rc01, Kotlin 2.4.21, Java target 21) | Compatibility unverified locally; may require JDK 21 for CI parity |
| Required SDK packages | platform `android-37.1` (channel 3), build-tools `37.0.0`, NDK `29.0.14206865`, CMake `3.22.1` | Missing/unverifiable |

## 5. Test Coverage (static inventory)

| Suite | Status |
|---|---|
| Authored unit/state-machine tests (recording/watchdog/adb pairing/legacy rish/notifications/helpers/USB/watchdog-live etc.) | **Authored, NOT_RUN** |
| `:manager:testDebugUnitTest` | **BLOCKED** (no Android SDK) |
| `:manager:lintDebug` | **BLOCKED** |
| `:manager:assembleDebug` | **BLOCKED** |
| `:manager:connectedDebugAndroidTest` / device scenarios | **NOT_TESTED** (no SDK/emulator/device) |

## 6. Legacy Rish Audit (initial)

- Contract-preservation intent is documented and reflected in components; manifest/service/settings/startup integration present.
- Production-audit items still required: Binder registration/authorization boundary verification, service destruction/process death/duplicate starts/reconnect cleanup, stale callback/leak checks, and lifecycle/authorization/reconnection/cleanup tests with evidence.
- **No security guarantees claimed without runtime/test evidence.**

## 7. Prioritized Implementation Plan

1. **Notification integration hardening (P0):** unify producer/cancel paths to registry/core where safe, complete helper/overlay/USB/watchdog live wiring and lifecycle reconciliation, fix migration semantics, preserve existing IDs/channels/FGS behavior and ADB pairing notifications.
2. **Settings/Home correctness (P0):** permission/channel/overlay status refresh, immediate reconciliation, clear history, Home independent of helper pref.
3. **Legacy Rish production audit (P1):** static/code audit for cleanup/authorization/lifecycle, add missing tests where possible.
4. **Build environment (P1, external dependency):** install/configure SDK+cmdline-tools+required packages+local.properties (machine-local, uncommitted), verify JDK compatibility, run Gradle config/compile/tests/lint/assemble.
5. **Instrumentation/device/security audit/docs (P2):** execute when device/emulator available; produce final security/reliability and production readiness reports with actual results.

## 8. Git Safety
- Pre-existing modified/untracked files preserved.
- No `git reset --hard`, no `git clean`, no destructive checkout, no auto-stash/pop, no commit/push performed.
- New documentation files added in this task: `docs/ANDROID_BUILD_ENVIRONMENT_REPORT.md`, `docs/PRODUCTION_READINESS_AUDIT.md` (this file).
