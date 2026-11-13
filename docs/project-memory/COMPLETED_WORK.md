# Completed Work - OmniShizuku

## Bug & Security Fix Session (2026-11-10)

### Security Fixes

| # | Issue | File | Fix |
|---|-------|------|-----|
| 1 | ThemePatcherActivity exported without permission | `AndroidManifest.xml:849` | Set `android:exported="false"` |
| 2 | InstallZipModuleActivity accepts untrusted ZIPs from any app | `module/InstallZipModuleActivity.kt` | Added 256MB size cap, streaming copy with overflow detection |
| 3 | IntentReceiver stores auth key in plaintext SharedPreferences | `receiver/IntentReceiver.kt` | Kept as-is (already disabled by default); documented as remaining risk |

### Crash & Hang Fixes

| # | Issue | File | Fix |
|---|-------|------|-----|
| 4 | ThemePatcherService.patchAll never invokes callback when no trial active → hang | `themepatcher/ThemePatcherService.kt:140-160` | Added `else` branch calling `handler.post { done("") }` |
| 5 | PocketModeService.onSensorChanged empty body | `essentials/PocketModeService.kt:36-49` | Implemented proximity sensor detection with overlay show/hide |
| 6 | PocketModeEngine.showOverlay never adds view to window manager | `essentials/PocketModeScreen.kt:176-208` | Properly creates LayoutParams, adds view via `wm.addView()`, tracks state |

### Null Safety / Cast Fixes

| # | Issue | File | Fix |
|---|-------|------|-----|
| 7 | Unsafe `as HttpURLConnection` cast in Contributors.kt:152 | `ui/component/Contributors.kt:152` | Changed to `as? HttpURLConnection` with `?: error(...)` |
| 8 | Unsafe `as HttpURLConnection` cast in ContributorAvatars.kt:91 | `ui/component/ContributorAvatars.kt:91` | Changed to `as? HttpURLConnection` with `?: return false` |
| 9 | Unsafe `as ClipboardManager` cast in HomeScreen.kt:633 | `ui/screen/HomeScreen.kt:633` | Changed to `getSystemService(ClipboardManager::class.java)` with null check |

### Silent Exception Swallowing

| # | File | Fix |
|---|------|-----|
| 10 | `utils/ShizukuSystemApis.kt` (3 catch blocks) | Added `Log.w` for getPackageInfo, getApplicationInfo, checkPermission fallback failures |
| 11 | `doze/EnforceDozeManager.kt:358` | Added `Log.w` for getMostRecentApp reflection failure |
| 12 | `themepatcher/ThemePatcherManager.kt:242` | Added `Log.w` + `return false` on service start failure |

### Thread & Resource Leaks

| # | Issue | File | Fix |
|---|-------|------|-----|
| 13 | DhizukuService.runCommand creates unbounded threads, no cleanup | `dhizuku/DhizukuService.kt:17-57` | Added `finally` block calling `process?.destroy()`, bounded `join()` calls to 5000ms |

### Localization

| # | Issue | File | Fix |
|---|-------|------|-----|
| 14 | Hardcoded section headings in HomeScreen | `ui/screen/HomeScreen.kt` | Replaced 6 hardcoded strings with `stringResource(R.string.home_section_*)` |
| 15 | Hardcoded subtitle in IntentsScreen | `ui/screen/IntentsScreen.kt:67` | Replaced with `stringResource(R.string.intents_subtitle)` |
| 16 | Missing string resources | `res/values/strings.xml` | Added 7 new string resources |

### Files Modified
- `manager/src/main/AndroidManifest.xml`
- `manager/src/main/java/moe/shizuku/manager/module/InstallZipModuleActivity.kt`
- `manager/src/main/java/moe/shizuku/manager/themepatcher/ThemePatcherService.kt`
- `manager/src/main/java/moe/shizuku/manager/themepatcher/ThemePatcherManager.kt`
- `manager/src/main/java/moe/shizuku/manager/essentials/PocketModeService.kt`
- `manager/src/main/java/moe/shizuku/manager/essentials/PocketModeScreen.kt`
- `manager/src/main/java/moe/shizuku/manager/utils/ShizukuSystemApis.kt`
- `manager/src/main/java/moe/shizuku/manager/doze/EnforceDozeManager.kt`
- `manager/src/main/java/moe/shizuku/manager/dhizuku/DhizukuService.kt`
- `manager/src/main/java/moe/shizuku/manager/ui/component/Contributors.kt`
- `manager/src/main/java/moe/shizuku/manager/ui/component/ContributorAvatars.kt`
- `manager/src/main/java/moe/shizuku/manager/ui/screen/HomeScreen.kt`
- `manager/src/main/java/moe/shizuku/manager/ui/screen/IntentsScreen.kt`
- `manager/src/main/res/values/strings.xml`

### Build Status
- Full Gradle build could not be run in this sandbox (no network access to Maven repositories)
- All changes verified via manual syntax review
- Build must be verified on the user's machine: `./gradlew :manager:compileDebugKotlin`