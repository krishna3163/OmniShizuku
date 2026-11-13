# Current State - OmniShizuku

## Project Identity
- **Name**: OmniShizuku
- **Package**: `com.krishna.omnishizuku` (namespace: `moe.shizuku.manager`)
- **Description**: A powerful, feature-rich fork of Shizuku with enhanced modules, modern UI, and extended system control capabilities
- **Repository**: `/workspace/app`

## Application Architecture
- **UI Framework**: Jetpack Compose with Material Design 3 Expressive
- **Language**: Kotlin (primary), Java (some legacy), C++ (JNI via CMake)
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 37
- **Compile SDK**: 37
- **Build Tool**: Gradle 9.8.2

## Git State
- **Branch**: `kilo/indigo-mosaic-chk`
- **Commit**: `dde9107` (Merge PR #71 - fix bugs and security issues)
- **Uncommitted Changes**: Multiple bug/security fixes applied in this session

## Build Status
- **`:manager:compileDebugKotlin`**: Could not verify in sandbox (no network access to Maven repos)
- **Changes verified**: Manual syntax review of all 14 modified files
- **Next verification**: Run `./gradlew :manager:compileDebugKotlin` on user's machine

## Current Phase
- Phase 5: Screen Redesign (In Progress)
- Phase 7: Quality (Bug fixes applied)

## Recent Bug & Security Fixes (2026-11-10)
- ThemePatcherActivity exported=false (security)
- InstallZipModuleActivity size cap + streaming copy (security)
- ThemePatcherService.patchAll hang fix
- PocketModeService/PocketModeEngine implemented
- Unsafe cast fixes (HttpURLConnection, ClipboardManager)
- Silent exception swallowing addressed (ShizukuSystemApis, EnforceDozeManager, ThemePatcherManager)
- DhizukuService thread leak fix
- Hardcoded strings externalized (HomeScreen, IntentsScreen)

## Remaining Known Issues
1. IntentReceiver stores auth key in plaintext SharedPreferences (documented risk)
2. Gradle build cannot run in sandbox environment
3. Some screens still use direct `MaterialTheme.colorScheme` instead of OmniTokens
4. XML/Compose mixed UI stack in some activities