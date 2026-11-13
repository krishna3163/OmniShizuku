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
- **Branch**: `feat/labs-feature-registry-v2`
- **Commit**: `1abfe5e` (Labs registry implementation)
- **Uncommitted Changes**: Phase 2 Audio Stream Master profile implementation

## Build Status
- **Current validation**: Gradle cannot resolve `com.android.library:9.5.0-rc01`
  from the active repositories, so this change requires a connected environment or
  a locally available plugin cache.
- **Changes verified**: Static consistency review and focused test invocation up to
  dependency resolution.
- **Next verification**: Run `./gradlew :manager:testDebugUnitTest` and
  `./gradlew :manager:compileDebugKotlin` where the configured AGP is available.

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