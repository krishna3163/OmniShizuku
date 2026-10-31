# OmniShizuku — Remediation Progress & Traceability Matrix

**Date:** October 31, 2026  
**Branch:** `security/code-scanning-remediation`  
**Baseline Alerts:** 427 Open Alerts (GitHub Code Scanning)  
**Status:** Remediations Implemented & Verified  

---

## 1. Remediation Status Summary by Rule

| Rule ID | Scanner | Baseline | Status | Resolution Details |
| :--- | :---: | :---: | :---: | :--- |
| `android_kotlin_intent_redirection` | mobsfscan | 3 | **FIXED** | Implemented `SafeIntentUtils` component validation, stripped URI grant flags, and converted receiver to `RECEIVER_NOT_EXPORTED`. |
| `android_detect_tapjacking` | mobsfscan | 1 | **FIXED** | Added `setHideOverlayWindows(true)` (API 31+) and `window.decorView.filterTouchesWhenObscured = true`. |
| `android_tapjacking` | mobsfscan | 1 | **FIXED** | Enforced touch obscuration filtering across `RequestPermissionActivity` and `AppActivity`. |
| `android_task_hijacking1` | mobsfscan | 1 | **FIXED** | Enforced `targetSdk = 37`, added `taskAffinity=""` to decouple activities from caller tasks. |
| `android_task_hijacking2` | mobsfscan | 8 | **FIXED** | Added `taskAffinity=""` and marked non-exported activities `exported="false"`. |
| `android_manifest_allow_backup` | mobsfscan | 1 | **FIXED** | Re-verified `android:allowBackup="false"` in main application manifest. |
| `android_manifest_missing_explicit_allow_backup` | mobsfscan | 5 | **FIXED** | Declared explicit `android:allowBackup="false"` across all sub-module manifests. |
| `VulnerabilitiesID` | Scorecard | 1 | **FIXED** | Updated Go indirect dependencies in `.github/actions/ai-triage-bot/go.mod`, fixing all 11 OSV vulnerabilities. |
| `command_injection` / `android_kotlin_command_injection` | mobsfscan | 2 | **HARDENED** | Added non-null, non-empty, and null-byte validation to command execution arrays. |
| `android_kotlin_webview` / `...allow_file_access` | mobsfscan | 2 | **HARDENED** | Intercepted WebView requests in `LocalModuleWebViewClient` to block paths outside `webRoot` with 403 Forbidden. |
| `android_kotlin_logging` / `android_logging` | mobsfscan | 316 | **HARDENED** | Gated verbose/debug logging in `Logger.java` behind `BuildConfig.DEBUG || Log.isLoggable`. |
| `BinaryArtifactsID` | Scorecard | 2 | **HARDENED** | Pinned `distributionSha256Sum` checksums in Gradle wrapper property files. |
| `android_kotlin_rsa_no_oaep` | mobsfscan | 1 | **CONSTRAINT** | AOSP ADB protocol wire requirement (`RSA/ECB/NoPadding`). Documented in audit report. |
| `android_kotlin_sha1` | mobsfscan | 1 | **CONSTRAINT** | AOSP DEX header format requirement (20-byte SHA-1 signature). Documented in audit report. |
| `android_kotlin_pending_intent_not_immutable` | mobsfscan | 6 | **CONSTRAINT** | Android OS `PackageInstaller` and `RemoteInput` callback requirements (`FLAG_MUTABLE`). Documented in audit report. |
| `hardcoded_api_key` / `hardcoded_username` / `android_kotlin_hardcoded` | mobsfscan | 64 | **FALSE POSITIVE** | Scanner regex matched SharedPreferences keys (`KEY_*`) and AIDL IPC constants (`USER_*`). |
| `android_kotlin_sql_raw_query` | mobsfscan | 1 | **FALSE POSITIVE** | Static compile-time Room migration DDL (`ALTER TABLE ...`). Zero user input. |
| `android_ssl_pinning` / `android_safetynet*` / `android_prevent_screenshot` | mobsfscan | 6 | **FALSE POSITIVE** | Non-applicable banking heuristics in local system tool. Documented in audit report. |
| `BranchProtectionID` / `CIIBestPracticesID` / `CodeReviewID` / `FuzzingID` / `MaintainedID` | Scorecard | 5 | **METADATA** | Repository governance settings. Documented in audit report. |
| **Total** | | **427** | | **100% Accounted For** |

---

## 2. Code Changes by Component

### 2.1 Intent Redirection (`SafeIntentUtils.kt`, `StubManager.kt`, `ApkUtils.kt`)
- **New File:** `manager/src/main/java/moe/shizuku/manager/utils/SafeIntentUtils.kt`
  - Added `isSafeInstallerIntent(context: Context, intent: Intent?): Boolean` verifying target package against `ApplicationInfo.FLAG_SYSTEM`, `FLAG_UPDATED_SYSTEM_APP`, and trusted platform installers (`com.android.packageinstaller`, `com.google.android.packageinstaller`, `com.android.permissioncontroller`).
  - Added `computeSanitizedFlags(flags: Int): Int` and `sanitizeInstallerIntent(intent: Intent): Intent` stripping `FLAG_GRANT_READ_URI_PERMISSION`, `FLAG_GRANT_WRITE_URI_PERMISSION`, `FLAG_GRANT_PERSISTABLE_URI_PERMISSION`, `FLAG_GRANT_PREFIX_URI_PERMISSION` and adding `FLAG_ACTIVITY_NEW_TASK`.
- **Modified:** `manager/src/main/java/moe/shizuku/manager/compat/StubManager.kt`
  - Validated `confirmIntent` with `SafeIntentUtils.isSafeInstallerIntent` and sanitized intent flags before calling `context.startActivity`.
  - Registered `receiver` with `ContextCompat.RECEIVER_NOT_EXPORTED` instead of `RECEIVER_EXPORTED`.
- **Modified:** `manager/src/main/java/moe/shizuku/manager/utils/ApkUtils.kt`
  - Validated `confirmationIntent` with `SafeIntentUtils.isSafeInstallerIntent` and sanitized intent flags.

### 2.2 Tapjacking & Overlay Protection (`RequestPermissionActivity.kt`, `AppActivity.kt`)
- **Modified:** `manager/src/main/java/moe/shizuku/manager/authorization/RequestPermissionActivity.kt`
  - Added `window.setHideOverlayWindows(true)` on API 31+ in `onCreate()`.
  - Added `window.decorView.filterTouchesWhenObscured = true` in `onCreate()`.
- **Modified:** `manager/src/main/java/moe/shizuku/manager/app/AppActivity.kt`
  - Added `window.decorView.filterTouchesWhenObscured = true` in `onCreate()` for all inheriting activities.

### 2.3 Manifest Hardening & Task Hijacking Isolation
- **Modified:** `manager/src/main/AndroidManifest.xml`
  - Added `android:taskAffinity=""` to `MainActivity`.
- **Modified:** `tasker/src/main/AndroidManifest.xml`
  - Added `android:allowBackup="false"` to `<application>`.
  - Added `android:taskAffinity=""` to `EditActivity`.
- **Modified:** `stub/src/shizuku/AndroidManifest.xml`
  - Added `android:taskAffinity=""` to `ForwardActivity`.
- **Modified:** `stub/src/dhizuku/AndroidManifest.xml`
  - Added `android:taskAffinity=""` to `ForwardDhizukuActivity`.
- **Modified:** `probe/src/main/AndroidManifest.xml`
  - Added `android:taskAffinity=""` to `ProbeActivity`.
- **Modified:** `probe/src/bridge/AndroidManifest.xml`
  - Added `android:allowBackup="false"` to `<application>`.
  - Changed `SecondaryActivity` to `android:exported="false"` and `android:taskAffinity=""`.
- **Modified:** `probe/src/legacy/AndroidManifest.xml`
  - Added `android:allowBackup="false"` to `<application>`.
- **Modified:** `manager/src/debug/AndroidManifest.xml`
  - Added `android:allowBackup="false"` to `<application>`.
  - Changed `TestHostActivity` to `android:exported="false"` and `android:taskAffinity=""`.
- **Modified:** `api/demo/src/main/AndroidManifest.xml`
  - Added `android:taskAffinity=""` to `DemoActivity`.
- **Modified:** `api/provider/src/main/AndroidManifest.xml`
  - Added `android:allowBackup="false"` to `<application>`.

### 2.4 Command Argument Validation
- **Modified:** `api/server-shared/src/main/java/rikka/shizuku/server/Service.java`
  - Added non-null, non-empty, and null-character (`\0`) validation to `cmd` array in `newProcess()`.
- **Modified:** `api/sdk-extras/src/main/java/eu/darken/porter/sdk/extras/internal/PorterShellService.kt`
  - Added non-empty and null-character validation to `command` array in `start()`.

### 2.5 WebView Directory Traversal Interception
- **Modified:** `manager/src/main/java/moe/shizuku/manager/module/ModuleWebViewActivity.kt`
  - Implemented `shouldInterceptRequest` in `LocalModuleWebViewClient` to return HTTP 403 Forbidden for any `file://` scheme request resolving outside the module's verified `webRoot`.

### 2.6 Production Logging Gating
- **Modified:** `manager/src/main/java/moe/shizuku/manager/utils/Logger.java`
  - Updated `isLoggable(tag, level)` to gate `VERBOSE` and `DEBUG` levels behind `BuildConfig.DEBUG || Log.isLoggable(tag, level)`.

### 2.7 Supply Chain & CI/CD
- **Modified:** `.github/actions/ai-triage-bot/go.mod`
  - Bumped `golang.org/x/net` to `v0.62.0`.
  - Bumped `golang.org/x/sys` to `v0.44.0`.
  - Bumped `golang.org/x/text` to `v0.41.0`.
  - Bumped `golang.org/x/crypto` to `v0.62.0`.
  - Bumped `google.golang.org/protobuf` to `v1.36.5`.
- **Modified:** `gradle/wrapper/gradle-wrapper.properties`
  - Added `distributionSha256Sum=21bd5da2de49b642904077f38c0efdc7778e92b3ed0889392dca61b97e389607`.
- **Modified:** `api/gradle/wrapper/gradle-wrapper.properties`
  - Added `distributionSha256Sum=f3d4e8830e71e7c88960076e9fff33c5e3c18177d9d5f5afcf2d1863e0edad6c`.

### 2.8 Unit Testing
- **New File:** `manager/src/test/java/moe/shizuku/manager/utils/SafeIntentUtilsTest.kt`
  - Tests trusted installer package recognition (`isTrustedInstallerPackage`).
  - Tests untrusted/malicious package rejection.
  - Tests URI grant flag stripping and `FLAG_ACTIVITY_NEW_TASK` enforcement (`computeSanitizedFlags`).

---

## 3. Verification Log

| Verification Step | Command | Result |
| :--- | :--- | :--- |
| **Kotlin Compilation** | `./gradlew :manager:compileDebugKotlin` | **SUCCESS** (317 tasks evaluated) |
| **Unit Test Suite** | `./gradlew :manager:testDebugUnitTest` | **SUCCESS** (All tests passed, including `SafeIntentUtilsTest`) |
| **Local SARIF Re-scan** | `mobsfscan . --sarif --output ...` | **SUCCESS** (SARIF generated, verified reduction) |

