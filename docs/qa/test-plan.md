# OmniShizuku — V1.0.0 Comprehensive Test Plan & QA Strategy

## 1. Quality Strategy & Objectives

The primary objective of the OmniShizuku V1.0.0 QA process is to guarantee stability, security boundary enforcement, memory efficiency, and cross-form-factor compatibility across phone, tablet, foldable, Android TV, and Wear OS devices.

### Quality Goals:
1. **Zero High/Critical Security Alert Regressions:** Maintain 100% clean status across CodeQL, Scorecard, and static analyzers.
2. **Deterministic Process Sandboxing:** Verify that local shell execution strictly adheres to process privilege boundaries without leaking system tokens.
3. **Robust Lifecycle Handling:** Ensure foreground services (call recorder, ADB pairing daemon, wireless server) survive process recreation and configuration changes without leaking WakeLocks or notification handles.
4. **Resilient CI/CD Pipelines:** Verify that GitHub Actions workflows execute reliably, gracefully handle missing optional secrets, and produce signed/verifiable artifacts.

---

## 2. Test Automation Matrix

| Test Suite | Module | Framework | Target / Focus | Execution Command |
| :--- | :--- | :--- | :--- | :--- |
| **Security IPC & Redirection** | `:manager` | JUnit 4 | `SafeIntentUtilsTest.kt`, flag sanitization, package verification | `./gradlew :manager:testDebugUnitTest --tests "*SafeIntent*"` |
| **Core Architecture & App** | `:manager` | JUnit 4 | `ShizukuAppTest.kt`, DI container, initialization | `./gradlew :manager:testDebugUnitTest --tests "*ShizukuApp*"` |
| **Shell & Session Engine** | `:manager` | JUnit 4 / Mock | `ShellCommandsTest.kt`, `ShellSession`, placeholder replacement | `./gradlew :manager:testDebugUnitTest --tests "*Shell*"` |
| **Local Terminal Execution** | `:manager` | JUnit 4 | `LocalShellManager`, process execution, stream buffering | `./gradlew :manager:testDebugUnitTest --tests "*LocalShell*"` |
| **Call Recorder & Audio** | `:manager` | JUnit 4 | Codec selection, preference persistence, state transitions | `./gradlew :manager:testDebugUnitTest --tests "*Recording*"` |
| **Database & Persistence** | `:database` | AndroidX Room Test | SQLite migrations, DAO queries, app authorization grants | `./gradlew :database:testDebugUnitTest` |
| **Full Unit Test Suite** | `:manager` | JUnit 4 / Robolectric | Complete manager unit test verification | `./gradlew :manager:testDebugUnitTest --no-daemon` |

---

## 3. Security & Boundary Validation Test Cases

### TC-SEC-01: Intent Redirection & Forwarding Hijack Protection
- **Target:** `moe.shizuku.manager.utils.SafeIntentUtils`
- **Preconditions:** Malicious intent received containing forward flags (`FLAG_GRANT_READ_URI_PERMISSION`, `FLAG_GRANT_WRITE_URI_PERMISSION`) and third-party target component.
- **Action:** Pass intent through `SafeIntentUtils.filterOrReject()`.
- **Expected Result:** Dangerous URI grant flags are cleared; only trusted internal packages or approved installer packages are allowed.

### TC-SEC-02: Tapjacking & Window Obscuration Protection
- **Target:** `HomeActivity`, `ManageAppsActivity`, `LocalShellScreen`
- **Preconditions:** Overlay application creates a floating `TYPE_APPLICATION_OVERLAY` window over OmniShizuku.
- **Action:** Attempt touch interaction on "Grant Permission" or "Execute Shell Command".
- **Expected Result:** Touches are filtered out (`filterTouchesWhenObscured = true`); sensitive activities invoke `setHideOverlayWindows(true)`.

### TC-SEC-03: Process Boundary Separation (Local vs Shizuku vs Root)
- **Target:** `moe.shizuku.manager.shell.LocalShellScreen`
- **Preconditions:** Shizuku service is not running; device is non-rooted.
- **Action:** Select "Local Process", run `id` and `whoami`.
- **Expected Result:** Command returns local application UID (e.g. `u0_a123`). Root/system commands fail safely without crashing. If elevated backend is selected while unavailable, prompt user with clear guidance.

---

## 4. Manual End-to-End Verification Scenarios

### E2E-01: Wireless ADB Auto-Pairing Workflow
1. Open OmniShizuku on Android 11+ device.
2. Enter Split-Screen or Floating Window with Settings -> Developer Options -> Wireless Debugging.
3. Tap "Pair device with pairing code".
4. Enter 6-digit pairing code and port in OmniShizuku.
5. Tap "Connect and Start".
6. **Verification:** Notification appears indicating connection; service starts; Home screen updates to "Service is running (ADB, UID 2000)".

### E2E-02: Local Shell Console & Preset Execution
1. Navigate to "Local Shell".
2. Tap "List Installed Apps" preset.
3. **Verification:** Text field populates with `pm list packages -3`; command automatically executes; terminal card displays package list; exit code shows `exit: 0`.
4. Tap "Copy Output".
5. **Verification:** Toast "Output copied to clipboard" appears; pasting in external text editor matches terminal content.
6. Enter custom command `uname -a` and tap "Save".
7. **Verification:** "Saved as default command" feedback; reopening screen loads `uname -a`.

### E2E-03: Call Recorder Lifecycle & Media Playback
1. Open "Call Recorder" -> Tab "Settings".
2. Toggle "Record Incoming Calls" and choose "AAC" codec.
3. Switch to "Record" tab and tap "Start Recording".
4. **Verification:** Service starts in foreground with persistent notification; timer counts upward; progress bar animates.
5. Tap "Stop Recording".
6. Switch to "Recordings" tab.
7. **Verification:** New recording file appears with timestamp and file size.
8. Tap Play button.
9. **Verification:** Audio begins playing; button changes to Pause icon; playback finishes cleanly without error.
10. Tap Delete button on recording.
11. **Verification:** File is removed from storage; list updates; Toast confirms deletion.

---

## 5. Device Compatibility Matrix

| OS Version | API Level | Supported Architectures | Form Factors | Verification Status |
| :--- | :--- | :--- | :--- | :--- |
| Android 8.0 - 9.0 | API 26 - 28 | arm64-v8a, armeabi-v7a, x86, x86_64 | Phone, Tablet | Compatible (Root / USB ADB) |
| Android 10 | API 29 | arm64-v8a, armeabi-v7a, x86_64 | Phone, Tablet | Compatible (Scoped storage verified) |
| Android 11 - 13 | API 30 - 33 | arm64-v8a, armeabi-v7a, x86_64 | Phone, Tablet, Foldable | Full Wireless ADB Pairing |
| Android 14 - 15 | API 34 - 35 | arm64-v8a, x86_64 | Phone, Foldable, TV, Wear | 16 KB page size compatible |
| Android 16 (Preview) | API 36 - 37 | arm64-v8a, x86_64 | Phone, Tablet | Compile SDK 37.1 verified |
| Android TV 9.0+ | API 28+ | arm64-v8a, armeabi-v7a | Television, Android TV Stick | D-pad navigation verified |
| Wear OS 3.0+ | API 30+ | arm64-v8a, armeabi-v7a | Smartwatches (Round / Square) | Rotary & Wear Compose verified |

---

## 6. Release Sign-Off Criteria for V1.0.0
- [x] All Gradle compilation tasks complete without errors (`:manager:assembleDebug`, `:manager:compileReleaseKotlin`).
- [x] 100% unit tests passing (`:manager:testDebugUnitTest`).
- [x] Zero empty `onClick = {}` handlers or non-functional placeholder buttons in user-facing flows.
- [x] All 22 CI/CD workflow configurations validated for syntax, graceful secret handling, and correct target module invocation.
- [x] Version code and version name synchronized to `v1.0.0` across manifests and Gradle scripts.
- [x] Security vulnerability remediations verified against GitHub Code Scanning SARIF rules.

