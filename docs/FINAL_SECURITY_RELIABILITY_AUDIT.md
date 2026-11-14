# Final Security & Reliability Audit (Preliminary)

Generated: 2026-11-14
Status: **PARTIAL / NOT_VERIFIED** (static audit only; build/runtime/device verification blocked)

## Scope
Static inspection of notification, Legacy Rish, watchdog/start, manifest, and permission-related code paths in current repository state. No dynamic testing, instrumentation, fuzzing, or device validation performed.

## Findings / Checks

| Area | Status | Notes |
|---|---|---|
| Binder authorization / Shizuku security model | NOT_VERIFIED | No weakening changes intentionally introduced in inspected notification/Legacy Rish code; Legacy Rish preserves existing `ShellBinderRequestHandler`/request actions/receivers and relies on existing Shizuku authorization. **Requires runtime and integration verification.** |
| Shell execution boundaries | NOT_VERIFIED | Existing privileged flows not fully audited in this pass; no new shell execution added in notification modules. |
| PendingIntent mutability | PARTIAL | Inspected new/modified notification code uses `FLAG_IMMUTABLE` for PendingIntents (helper/USB/legacy Rish/overlay-related paths sampled). Full repository audit required. |
| Exported components | PARTIAL (static) | Manifest contains expected exported receivers/activities/services for existing functionality (request binder/permission, boot, QS tiles, etc.). No new exported component added for helper dismiss receiver (declared non-exported). Full manifest security review still required. |
| Broadcast receiver security | PARTIAL | `HelperDismissReceiver` non-exported with explicit action/extra; USB monitor registers runtime receivers. Permission scoping and intent validation need comprehensive review. |
| Foreground service declarations/types | PARTIAL | Existing services declare FGS types in manifest as before; Legacy Rish uses `specialUse`. Compliance across Android versions/start contexts not runtime-verified. |
| Notification permission handling | PARTIAL | `NotificationPreferences.notificationsAllowed`/channel checks exist; UI/settings refresh and all posting paths need complete enforcement verification (POST_NOTIFICATIONS on API 33+). |
| Overlay permission behavior | PARTIAL | `OverlayMessageController` checks `messagesOverOtherAppsEnabled` and `Settings.canDrawOverlays`, uses `TYPE_APPLICATION_OVERLAY`, non-focusable/non-touchable flags, removes on `removeAll`. Explicit consent UX, revocation detection, lifecycle cleanup, and fallback need completion/proof. |
| Sensitive info in logs/notifications | NOT_VERIFIED | Not fully audited; no intentional secrets added. |
| Lifecycle cleanup / leaks | PARTIAL | `NotificationLifecycleOwner` uses ProcessLifecycleOwner and avoids Activity refs; receiver/observer registration/unregistration and overlay/view cleanup need comprehensive verification. |
| Thread/coroutine/WakeLock cleanup | NOT_VERIFIED | Existing subsystems not re-audited in full; no new wake locks added. |
| Stale callback/generation protection | PARTIAL | Legacy Rish/watchdog designs include generation/stale-event concepts; end-to-end coverage across async paths not proven. |
| Race conditions / resource leaks | NOT_VERIFIED | Requires tests/runtime/instrumentation. |
| Dependency vulnerabilities | NOT_RUN | No dependency scan/build executed in this environment. |
| Native code safety | NOT_RUN | Native modules unchanged by this pass; no analysis executed. |

## Critical Risks / Unknowns
- **Build not verified:** compilation/lint/test failures may exist.
- **Runtime behavior unproven:** notifications, overlays, USB correlation, watchdog live updates, and Legacy Rish lifecycle not validated on device/emulator.
- **Authorization guarantees unproven:** no claim that Binder/shell/permission boundaries are correct beyond static preservation intent.

## Required Next Actions
1. Configure Android SDK/JDK and run compile, unit tests, lint, assembleDebug.
2. Complete notification integration gaps and add/execute required tests (including Robolectric/instrumentation where applicable).
3. Perform Legacy Rish lifecycle/authorization/cleanup audit with evidence.
4. Run device/emulator validation for notification permissions/channels, overlay grant/revoke/foreground/background, USB/wireless ADB states, watchdog recovery states, and Legacy Rish scenarios.
5. Update this report with concrete evidence, file/line references, and PASS/FAIL/NOT_TESTED outcomes.
