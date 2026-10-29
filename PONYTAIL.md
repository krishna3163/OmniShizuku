# PONYTAIL — OmniShizuku Codebase Index

Purpose: single-file map of the repository so you never have to read every file to
find what you need. Use the lookup tables below to jump straight to the code that
implements a feature, a UI screen, or a bug area.

Root: `/home/krishna/Downloads/OmniShizuku` (clone of `git@github.com:krishna3163/OmniShizuku.git`, `main`)
Package: `moe.shizuku.manager` | AppId: `com.krishna.omnishizuku`
Stack: Kotlin + Jetpack Compose + Material3 Expressive + Gradle multi-project, minSdk 26 / targetSdk 37.

---

## 1. Gradle modules (settings.gradle)

| Module | Path | Role |
|--------|------|------|
| `:manager` | `manager/` | Main app UI (Compose), entry `MainActivity` |
| `:server` | `server/` | Shizuku server / Binder service host |
| `:starter` | `starter/` | Server start flow (ADB/root/Dhizuku) |
| `:shell` | `shell/` | Shell session / binder bridge |
| `:common` | `common/` | Shared utilities |
| `:database` | `database/` | Room DB (activity log, script snippets) |
| `:tasker` | `tasker/` | Tasker plugin contract & receiver |
| `:nightdog` | `nightdog/` | NightDog client (compile-only dep) |
| `:app-process` | `app-process/` | App process tooling |
| `:stub` | `stub/` | Compatibility stub APK |
| `:api` | `api/` | API surface (manager-protocol, porsh, sdk-extras) |
| `:provider` / `:server-shared` / `:aidl` / `:rish` / `:shared` | `api/` | AIDL, shared kernel, provider |

Build: `./gradlew :manager:assembleDebug` | Release: `./gradlew :manager:assembleRelease`

---

## 2. Manager source map — `manager/src/main/java/moe/shizuku/manager/`

### Entry / App shell
| File | What it does |
|------|-------------|
| `MainActivity.kt` | Launcher activity — checks first launch, routes to `FirstLaunchActivity` or `HomeActivity` |
| `FirstLaunchActivity.kt` | **First-time onboarding**: 6 disclaimers, agreement with 5s timer, language, theme, activation method |
| `ShizukuApplication.kt` | Application class, Crashlytics + uncaught handler |
| `app/AppActivity.kt` | Base activity (edge-to-edge, theme, system bars) |
| `app/AppBarActivity.kt` | AppBar host |
| `ui/ShizukuApp.kt` | Compose app root |

### Home & navigation (the 5 tabs)
| File | What it does |
|------|-------------|
| `home/HomeActivity.kt` | **Tab host**: 5-tab pager/AnimatedContent, nav bar, motion blur, dialogs |
| `home/HomeViewModel.kt` | Server status + service status LiveData |
| `home/DetailHost.kt` | Detail-screen host (back stack) |
| `home/AppOpeningSplashScreen.kt` | Launch splash animation |
| `ui/screen/HomeScreen.kt` | Tab 0 — Home dashboard |
| `ui/component/...` | Reusable Compose UI components |
| `ui/compose/ShizukuExpressive.kt` | Theme + `ExpressiveFloatingNavigationBar` + `ShizukuLazyScaffold` |
| `ui/compose/MotionBlur.kt` | **Directional scroll/pager blur** (lag fix lives here) |
| `ui/theme/Theme.kt` | `ShizukuTheme`, dynamic color, AMOLED mode |

Tab screens (index → file):
- 0 Home → `ui/screen/HomeScreen.kt`
- 1 Modules → `module/ModulesScreen.kt`
- 2 Labs → `ui/screen/LabsScreen.kt`
- 3 Comput → `logs/ComputScreen.kt`
- 4 Settings → `ui/screen/SettingsScreen.kt`

### Feature areas (where to find code)

| Area | Key files |
|------|-----------|
| Server lifecycle / state | `utils/ShizukuStateMachine.kt`, `service/WatchdogManager.kt`, `service/WatchdogService.kt`, `service/StartCircuitBreaker.kt` |
| Start ADB / root / Dhizuku | `starter/Starter.kt`, `start/AdbStartRun.kt`, `start/StartTransport.kt`, `dhizuku/DhizukuService.kt` |
| Wireless ADB / pairing | `adb/AdbStarter.kt`, `adb/AdbPairingClient.kt`, `adb/AdbPairingService.kt`, `adb/WirelessPortHunt.kt` |
| Wireless ADB auto-PIN (accessibility) | `adb/WirelessAdbPinManager.kt`, `adb/WirelessAdbPinService.kt`, `res/xml/wireless_adb_pin_service_config.xml` |
| First-launch onboarding | `FirstLaunchActivity.kt`, `ShizukuSettings.java` (KEY_FIRST_LAUNCH, KEY_ACTIVATION_METHOD) |
| App management / freeze / debloat | `management/ApplicationManagementActivity.kt`, `freeze/FreezerScreen.kt`, `debloater/DebloaterScreen.kt`, `manage/PackageTools.kt` |
| Modules (ADB/ZIP) | `module/AdbModuleManager.kt`, `module/ModulesScreen.kt`, `module/ModulePackage.kt`, `module/update/ModuleInstaller.kt` |
| Settings | `ui/screen/SettingsScreen.kt`, `settings/` |
| Security / auth / app lock | `security/AuthManager.kt`, `security/SecuritySettings.kt`, `authorization/AuthorizationManager.kt` |
| Shell / terminal | `shell/ShellSession.kt`, `shell/ShellCommands.kt`, `ui/screen/ShellScreen.kt` |
| DNS / firewall / tether | `dns/PrivateDnsManager.kt`, `dns/DnsPresetCatalog.kt`, `manage/FirewallApi.kt`, `tether/TetherManager.kt` |
| Doze / power / battery | `doze/DozeManager.kt`, `essentials/CaffeinateController.kt`, `ui/screen/PowerScreen.kt` |
| Device info | `manage/DeviceInfo.kt`, `ui/screen/DeviceInfoScreen.kt` |
| Device owner / hiding / stealth | `deviceowner/DeviceOwnerManager.kt`, `manage/Hiding.kt`, `stealth/`, `receiver/SheveryDeviceAdminReceiver.kt` |
| Comput / AI agent | `comput/agent/ComputAgent.kt`, `commandium/AiProvider.kt`, `logs/ComputScreen.kt` |
| Tasker automation | `tasker/PluginContract.kt`, `tasker/PluginReceiver.kt` |
| Utils (ktx, diag, shell) | `ktx/`, `utils/Diag.kt`, `utils/ShellArguments.kt`, `utils/BootTimeline.kt` |

### UI screens (`ui/screen/`)
`ActivitiesScreen`, `AppDetailScreen`, `AppsScreen`, `CatalogScreen`, `DeviceInfoScreen`,
`DisplayScreen`, `FilesScreen`, `ForceDarkScreen`, `HidingScreen`, `HomeDiagnostics`,
`HomeScreen`, `InstallerScreen`, `IntentsScreen`, `LabsScreen`, `LabsToggleScreen`,
`LogScreen`, `ManageScreen`, `PermissionsScreen`, `PowerScreen`, `SettingsScreen`,
`ShellHistorySheet`, `ShellLibrary`, `ShellScreen`, `StealthScreen`, `TerminalScreen`,
`WifiPasswordsScreen`

### Other packages
`about/`, `accessory/`, `accessibility/`, `adb/`, `app/`, `audio/`, `authorization/`,
`commandium/`, `compat/`, `comput/agent/`, `connector/`, `debloater/`, `deviceowner/`,
`dhizuku/`, `dnd/`, `dns/`, `doze/`, `essentials/`, `files/`, `freeze/`, `home/`,
`install/`, `ktx/`, `legacy/`, `logs/`, `manage/`, `management/`, `model/`, `module/`,
`nightdog/`, `optimizer/`, `receiver/`, `security/`, `service/`, `settings/`, `shell/`,
`start/`, `starter/`, `stealth/`, `tether/`, `tile/`, `tuner/`, `ui/`, `utils/`, `worker/`

---

## 3. Bug-area quick lookup (from your report)

| Symptom | Likely location | Fix applied |
|---------|----------------|-------------|
| Laggy / janky UI | `ui/compose/MotionBlur.kt` | Frame-sampler loops now run **only while scrolling** (`delay(16)` tick) instead of `while(isActive)` forever |
| Transitions not smooth | `home/HomeActivity.kt` | Tab `AnimatedContent` uses `fadeIn`/`fadeOut` tween(300) |
| Auto-close / crash | `manager/ShizukuApplication.kt` | Firebase Crashlytics init + `Thread.setDefaultUncaughtExceptionHandler` safety net |
| DeviceInfo infinite loading | `manage/DeviceInfo.kt`, `ui/screen/DeviceInfoScreen.kt` | `snapshot()` is now `suspend` with 20s `withTimeoutOrNull`; screen shows partial data instead of stuck spinner |
| Wireless ADB PIN typing | `adb/WirelessAdbPinManager.kt`, `adb/WirelessAdbPinService.kt`, `ui/screen/SettingsScreen.kt` | New opt-in accessibility service: Off / Manual / Automatic modes. Automatic requires accessibility permission - user is prompted to enable it in system settings if not granted. |
| First launch / onboarding | `FirstLaunchActivity.kt`, `MainActivity.kt` | 6-screen disclaimer, 5s agreement timer, language/theme/activation setup |

---

## 4. New files (this session)

| File | What it does |
|------|-------------|
| `FirstLaunchActivity.kt` | First-launch onboarding activity (disclaimers, agreement, language, theme, activation) |
| `adb/WirelessAdbPinManager.kt` | Auto-PIN mode persistence (Off/Manual/Automatic) + stored PIN |
| `adb/WirelessAdbPinService.kt` | Accessibility service that auto-enters pairing PIN into dialog |
| `res/xml/wireless_adb_pin_service_config.xml` | Accessibility service config (settings apps only) |
| `res/drawable/ic_info.xml` | Info icon vector |
| `res/drawable/ic_agreement.xml` | Agreement icon vector |
| `PONYTAIL.md` | This index |

---

## 5. Modified files (this session)

| File | Change |
|------|--------|
| `ui/compose/MotionBlur.kt` | Lazy velocity tracking — frame loop only during `isScrollInProgress` |
| `manager/ShizukuApplication.kt` | Crashlytics init + uncaught exception handler |
| `manage/DeviceInfo.kt` | `snapshot()` → `suspend` with 20s timeout |
| `ui/screen/DeviceInfoScreen.kt` | Handles nullable/timeout snapshot, shows partial data |
| `ui/screen/SettingsScreen.kt` | Added auto-PIN mode row + ChoiceDialog + accessibility permission check + strings |
| `manager/AndroidManifest.xml` | Registered `WirelessAdbPinService` + `FirstLaunchActivity` |
| `manager/ShizukuSettings.java` | Added `KEY_FIRST_LAUNCH`, `KEY_ACTIVATION_METHOD`, `ACTIVATION_*` constants, `isFirstLaunch/setFirstLaunch/getActivationMethod/setActivationMethod/setNightMode` |
| `manager/MainActivity.kt` | Routes to `FirstLaunchActivity` on first launch |
| `res/values/strings.xml` | Added onboarding + auto-PIN + accessibility strings |
| `README.md` | English documentation |
| `README.hi.md` | Hindi documentation |
| `README.es.md` | Spanish documentation |
| `README.fr.md` | French documentation |
| `README.zh.md` | Simplified Chinese documentation |
| `README.zh_tw.md` | Traditional Chinese documentation |
| `README.ru.md` | Russian documentation |

---

## 6. How to find anything fast

- Looking for a **screen**? → `ui/screen/<Name>Screen.kt`
- Looking for **navigation / tabs**? → `home/HomeActivity.kt`
- Looking for **theme / blur / nav bar**? → `ui/compose/` + `ui/theme/Theme.kt`
- Looking for **server on/off / crash**? → `utils/ShizukuStateMachine.kt`
- Looking for **start ADB / wireless**? → `starter/` + `start/` + `adb/`
- Looking for **settings pref**? → `ui/screen/SettingsScreen.kt` + `ShizukuSettings`
- Looking for **crash handling**? → `manager/ShizukuApplication.kt`
- Looking for **device info loading**? → `manage/DeviceInfo.kt` + `ui/screen/DeviceInfoScreen.kt`
- Looking for **auto wireless ADB PIN**? → `adb/WirelessAdbPin*` + `SettingsScreen.kt` wireless section
- Looking for **first launch / onboarding**? → `FirstLaunchActivity.kt` + `MainActivity.kt`

Docs: `docs/` (build results, security review, feature mapping, architecture).
