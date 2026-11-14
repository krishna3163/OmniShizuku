# OmniShizuku Labs Feature Registry

`manager/src/main/java/moe/shizuku/manager/feature/registry/FeatureRegistry.kt`
is the machine-readable catalogue for Labs features. It complements localized UI
strings and existing `Detail` navigation without moving privileged execution into
Compose screens.

Each definition provides a stable ID, category, searchable keywords, minimum API,
provider alternatives, reversibility, and experimental classification. Provider
requirements are alternatives: a shell-backed feature is available when either
Shizuku or root is currently usable. The registry never infers availability from a
previous success; `FeatureRegistry.capability()` checks the current process state,
Android version, ADB setting, and root shell.

## Provider policy

| Provider | Use |
|---|---|
| Android API | Public platform APIs and device information |
| Shizuku | Privileged shell and system-service operations |
| ADB | ADB-backed settings and package operations |
| Root | Root-only shell and filesystem operations |
| Dhizuku | Reserved for device-owner integrations when a feature explicitly supports it |

Features that require privileged execution are reported as unavailable until a
supported provider is detected. Experimental entries remain visible in the
catalogue but are marked as experimental rather than being presented as universal
hardware support.

## Essentials mapping

The Essentials wiki describes the same broad areasâ€”display, sound, power, privacy,
network, package management, developer tools, automation, and quick settings.
OmniShizuku already contains native implementations for the compatible subset:
display controls, audio streams, battery/device diagnostics, private DNS, doze,
package inspection/freezing/debloating, activity launching, shell/logging,
caffeinate, and quick-tile integrations. Smart pixels, OEM-only SystemUI changes,
per-app audio, unrestricted firewall behavior, and exact background automation remain
experimental or device/provider dependent because Android does not expose a
universal API for them.

| Essentials reference area | OmniShizuku mapping | Status |
|---|---|---|
| Sound & Haptics | Audio Master, SoundMaster, MixedAudio, Quick Tiles | Native audio controls; device limits are surfaced |
| Display & Visual | Display, SystemUI Tuner, Caffeinate, Pocket Mode | Existing controls; OEM-only options remain gated |
| Notifications & Alerts | Auto DND, notification/security tools | Supported where Android exposes policy access |
| Security & Privacy | App Ops, Permissions, Screen Security, Stealth | Provider and Android security boundaries are preserved |
| Input & Actions | Pocket Mode and shell-backed action tools | Pocket mode is experimental/device dependent |
| Power & Battery | Device Info, Doze, Caffeinate, Freezer | Real platform data and reversible controls |
| Network & Connectivity | Private DNS, Tethering, Firewall, ShizuWall | DNS is verified and rollback-safe; firewall paths are provider dependent |
| Widgets / Watch | Quick tiles and companion integrations | No fake Wear OS or widget support is advertised |
| DIY Automation | Auto DND and existing reapply infrastructure | Full trigger/action editor remains future work |
| App Freezing | Freezer | Existing provider-backed implementation |
| Tools | Files, Logs, Activities, Installer, WallYou | Existing native screens and capability limits |


### Legacy Rish Compatibility (Phase 1)
| Feature | Mapping | Status | Notes |
|---|---|---|---|
| Legacy Rish Binder compatibility keep-alive | Reuses existing `ShellBinderRequestHandler` / `ShellRequestHandlerActivity` / `ShizukuReceiver` request-binder contract; adds lifecycle manager, service, notification, settings state, and tests | IMPLEMENTED / UNIT_TESTED (authored) / RUNTIME BLOCKED | Preserves AIDL/protocol/actions; active only when enabled and Shizuku server+binder are actually available. Real legacy-client compatibility not verified in this environment. Continuity Bridge out of scope. |

### ADB Start & Port Discovery State Machine (Phase 1)
| Feature | Mapping | Status | Notes |
|---|---|---|---|
| ADB Start & Port Discovery Engine | Formalized pure state machine in `AdbStartStateMachine.kt`, `AdbStartCoordinator.kt`; integrates with `AdbStartRun.kt` | IMPLEMENTED / UNIT_TESTED | Generation-based stale callback rejection, single-flight startup protection, dead port probe revalidation, command-sent protection, and USB auto-hide coordination. |

### Doze & Power Policy State Machine (Phase 2)
| Feature | Mapping | Status | Notes |
|---|---|---|---|
| Enforce Doze & Power Policy Supervisor | Pure state machine in `DozeStateMachine.kt`, `DozeCoordinator.kt`; immutable `DeviceRadioSnapshot` | IMPLEMENTED / UNIT_TESTED | Replaced raw `java.util.Timer` with structured coroutines, monotonic deadline tracking, non-clobbering selective restoration, guaranteed WakeLock release, and rapid screen on/off debounce handling. |

### Theme Patcher & OEM Trial Freeze Engine (Phase 3)
| Feature | Mapping | Status | Notes |
|---|---|---|---|
| OEM Theme Store Trial Freeze Engine | Pure state machine in `ThemePatcherStateMachine.kt`, `ThemePatcherCoordinator.kt`; integrates with `ThemePatcherService.kt` | IMPLEMENTED / UNIT_TESTED | Replaced unmanaged thread loops with generation-tracked structured coroutines, 5-minute monotonic timeouts, and post-patch settings verification. |

### Caffeinate WakeLock Controller (Phase 4)
| Feature | Mapping | Status | Notes |
|---|---|---|---|
| Caffeinate WakeLock Controller | Pure state machine in `CaffeinateStateMachine.kt`, `CaffeinateCoordinator.kt`; integrates with `CaffeinateController.kt` and `CaffeinateWakeLockService.kt` | IMPLEMENTED / UNIT_TESTED | Replaced deprecated `ActivityManager.getRunningServices()` polling, eliminated double-receiver races, and enforced monotonic deadlines and safety WakeLock timeouts. |

### Display Rollback & Safety Machine (Phase 5)
| Feature | Mapping | Status | Notes |
|---|---|---|---|
| Display Rollback & Safety Engine | Pure state machine in `DisplayRollbackStateMachine.kt`, `DisplayRollbackCoordinator.kt`; integrates with `DisplayRollbackManager.kt` | IMPLEMENTED / UNIT_TESTED | Prevents concurrent changes, guarantees single-flight execution, evaluates monotonic confirmation deadlines, and eliminates WorkManager delay races. |

### NightDog System Service Supervisor (Phase 6)
| Feature | Mapping | Status | Notes |
|---|---|---|---|
| NightDog System Service Supervisor | Pure state machine in `NightDogStateMachine.kt`, `NightDogCoordinator.kt`; integrates with `NightDog.kt` and `ShizukuService.java` | IMPLEMENTED / UNIT_TESTED | Generation-based stale rebind rejection, independent per-service states (`UNBOUND`, `LINKED`, `DEATH_DETECTED`, `REBIND_PENDING`, `REBINDING`, `RECOVERED`, `UNRECOVERABLE`), exponential backoff curve, monotonic heartbeat watchdog, and native starter daemon recovery coordination. |

### Notification Production Integration & Reliability (Phase 7)
| Feature | Mapping | Status | Notes |
|---|---|---|---|
| Centralized Notification & Channel Registry | Centralized registry `NotificationRegistry.kt` mapping 23 unique IDs and 23 unique channels; resolved legacy collisions | IMPLEMENTED / UNIT_TESTED | Guaranteed uniqueness and collision prevention. |
| Idempotent Notification Migration | Pure Kotlin `NotificationMigration.kt` with `NotificationCanceller` and `MigrationStorage` abstractions | IMPLEMENTED / UNIT_TESTED | Cancels obsolete IDs (1002, 1005, 1458) once per app upgrade. |
| Helper Notifications Engine | Pure Kotlin `HelperNotificationPolicy.kt` and `HelperNotificationManager.kt` with `HelperDismissReceiver` | IMPLEMENTED / UNIT_TESTED | Deterministic deduplication, 5000ms debounce, user dismissal suppression, healthy state clearance, restart action. |
| Messages Over Other Apps & Fallback | Pure Kotlin `HelperMessageDispatcher.kt` and `OverlayMessageController.kt` | IMPLEMENTED / UNIT_TESTED | Non-blocking FIFO queue, clock/scheduler decoupling, least-privileged `TYPE_APPLICATION_OVERLAY` (`FLAG_NOT_FOCUSABLE or FLAG_NOT_TOUCHABLE or FLAG_LAYOUT_IN_SCREEN`), and Compose in-app fallback. |
| USB Connection & Switch Notifications | Pure Kotlin `UsbConnectionState.kt`, `UsbConnectionReducer`, and `UsbConnectionMonitor` | IMPLEMENTED / UNIT_TESTED | Disambiguates physical USB, USB configured, ADB interface, external host devices, wireless ADB, and Shizuku server availability. |
| Watchdog Live Activity Notifications | Pure Kotlin `WatchdogLiveStateMapper.kt` integrated into `WatchdogService.kt` | IMPLEMENTED / UNIT_TESTED | Maps server/watchdog states to user-facing decisions without fake progress, throttles against notification shade flicker. |





Intentionally not presented as complete: universal smart-pixel hardware control,
per-application audio on stock Android, carrier bypass, unrestricted SystemUI
customization, and exact background automation timing. These require OEM APIs,
root/VPN/device-owner privileges, or behavior Android does not guarantee.

