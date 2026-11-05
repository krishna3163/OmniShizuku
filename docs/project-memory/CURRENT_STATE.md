# Current State - OmniShizuku

## Project Identity
- **Name**: OmniShizuku
- **Package**: `com.krishna.omnishizuku` (namespace: `moe.shizuku.manager`)
- **Description**: A powerful, feature-rich fork of Shizuku with enhanced modules, modern UI, and extended system control capabilities
- **Repository**: `/home/krishna/Downloads/tv/OmniShizuku`

## Application Architecture
- **UI Framework**: Jetpack Compose with Material Design 3 Expressive
- **Language**: Kotlin (primary), Java (some legacy), C++ (JNI via CMake)
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 37
- **Compile SDK**: 37
- **Build Tool**: Gradle 10
- **Modules**: `:manager`, `:server`, `:starter`, `:shell`, `:stub`, `:common`, `:nightdog`, `:tasker`, `:database`, `:app-process`, `:wallpaper-apis`, `:aidl`, `:rish`, `:shared`, `:api`, `:provider`, `:server-shared`
- **Navigation**: Custom HorizontalPager + detail screen overlay (no Navigation Component)
- **State Management**: Compose state (remember, rememberSaveable, mutableStateOf), ViewModel for some screens
- **Shizuku Integration**: Full Shizuku API integration via Rikka Shizuku client
- **Root Backend**: libsu (topjohnwu), DFRoot exploit integration
- **ADB Integration**: Wireless ADB, USB debugging, ADB pairing
- **Dependency Injection**: Manual (no Dagger/Hilt/Koin)

## Git State
- **Branch**: `main` (up to date with origin/main)
- **Commit**: `4a67615` feat(doze): integrate full EnforceDoze background service, tunables editor, app blocklist and QS tile
- **Uncommitted Changes**: `manager/src/main/AndroidManifest.xml` (added ThemePatcher activity + service)
- **Untracked Files**: `themepatcher/ThemePatcherActivity.kt`, `themepatcher/ThemePatcherService.kt`, `res/layout/activity_theme_patcher.xml`

## Build Status
- **`:manager:compileDebugKotlin`**: BUILD SUCCESSFUL (verified)
- **`:manager:testDebugUnitTest`**: BUILD SUCCESSFUL, all tests pass (verified)
- **`:manager:assembleDebug`**: BUILD SUCCESSFUL, APK installed on tablet (device MBRKCEQGUKOJLV6X)

## Test Status
- 15 unit test files in manager module
- All tests pass (verified)
- Test categories: shell commands, call recorder, labs search, home metrics, shell arguments, package name validator, DNS hostname validator, safe intent utils

## Current Implemented Features
- Home Dashboard (device status, quick actions)
- Apps Screen (installed app management)
- Modules Screen (Shizuku module management)
- Labs Screen (app ops, tweaks)
- Settings Screen (theme, night mode, ADB, wireless debugging)
- Shell Screen (terminal)
- Device Info Screen
- Display Screen (resolution, density, refresh rate)
- Power Screen (reboot, recovery)
- Debloater Screen
- Freezer Screen
- Doze Screen (EnforceDoze)
- ThemePatcher Screen (OPPO/OnePlus/Realme theme trial patcher)
- SoundMaster Screen
- MixedAudio Screen
- Lookback Screen
- Universal PiP Screen
- Local Shell Screen
- Call Recording Screen
- DFRoot Screen
- WallYou Screen
- Stealth Screen
- Terminal Screen
- Intents Screen
- Permissions Screen
- Network Firewall Screen
- Bug Report Screen
- And many more...

## Current UI State
- Material Design 3 Expressive theme with dynamic color support
- Dark mode + AMOLED black theme support
- System color (wallpaper-based) theming option
- Floating tab bar at bottom (Home, Apps, Modules, Labs, Settings)
- Detail screens slide over tabs
- TV mode with NavigationRail
- Wear OS support
- Some screens use older patterns (XML Activities for SettingsScreen, BugReportDialogActivity, etc.)

## Current Build Status
- Debug APK: BUILD SUCCESSFUL, installed on tablet
- Release APK: Not recently built (minify enabled, shrink resources)
- Signed with debug signing config

## Latest Completed Task
- UI/UX Engineering Program: HomeScreen.kt redesigned (1242 lines → design system applied)
  - Added SectionHeading component for section dividers (Status, Server Actions, Start Methods, Device Details, Live Metrics, Support)
  - Replaced inline MaterialTheme.colorScheme with OmniTokens semantic tokens
  - Used OmniTokens.shapeLarge, OmniTokens.displayMedium, OmniTokens.bodyMedium/Small, OmniTokens.captionStyle consistently
  - Preserved all existing business logic (refresh(), start methods, diagnostics, permissions, etc.)
  - Build: BUILD SUCCESSFUL, Tests: ALL PASS, APK installed and running on tablet MBRKCEQGUKOJLV6X

## UI/UX Engineering Program Status

| Phase | Status |
|-------|--------|
| Phase 0: Discovery | Complete |
| Phase 1: Tooling (research) | Complete |
| Phase 2: Product & Design System | Complete (PRODUCT.md, DESIGN.md) |
| Phase 3: UI Audit | Complete (BASELINE_AUDIT.md, accessibility, performance) |
| Phase 4: Foundation | Complete (OmniTokens.kt, StatusIndicator.kt) |
| Phase 5: Screen Redesign | In Progress (HomeScreen complete → SettingsScreen next) |
| Phase 6: Motion & Interaction | Not started |
| Phase 7: Quality | Not started |
| Phase 8: Final Review | Not started |

## Active Task
- Phase 5: Screen Redesign - SettingsScreen.kt (1542 lines)

## Next Recommended Task
- Redesign SettingsScreen.kt using OmniTokens and new components
- Then proceed to AppsScreen, ModulesScreen, etc.

## Current Blockers
- None

## Important Architectural Constraints
- Must preserve existing Shizuku integration and all working features
- Cannot break root/ADB functionality
- Must maintain Material Design 3 Expressive foundation
- Must support phone, tablet, TV, and Wear OS form factors
- No fake system state or fabricated data
- Destructive operations require confirmation
- HomeScreen.kt is 1242 lines with complex business logic mixed with UI - needs careful incremental refactoring
- SettingsScreen.kt is 1542 lines, also complex
- XML Activities (NetworkFirewallActivity, BugReportDialogActivity) use old View system mixed with Compose
