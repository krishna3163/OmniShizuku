# Labs Complete Audit — OmniShizuku

Generated: 2026-11-06. Verified against source: `manager/src/main/java/.../ui/screen/LabsScreen.kt`, `manager/src/main/java/.../home/DetailHost.kt`, `manager/src/main/java/.../ui/ShizukuApp.kt` (Detail enum), and per-tool screen files.

## Method
- Entry inventory from `LabsScreen.kt` `entries` and `hiddenLabDetails`.
- Navigation verified via `DetailHost.kt` routing for all 39 visible entries.
- Implementation sampled from destination screens/backends (e.g., `LabsToggleScreen` reads/writes blocked state via `PackageTools`/`Hiding`; `ManageScreen` reads installed packages; `PowerScreen` executes shell commands; `DebloaterScreen`/`FreezerScreen` perform package operations; companion entries use `openCompanionTool`).

## Summary
- **Total Labs entries audited:** 39 (37 Detail-backed + 2 companion-app entries).
- **Visible in Labs:** 37 (6 hidden details filtered out).
- **Categories:** App Management (8), System Controls (15), Utilities & Developer Tools (14), Companion Apps (2).
- **Hidden from Labs list:** `ESSENTIALS`, `SMART_PIXELS`, `MEDIA_KEYS`, `WATERMARK`, `EDGE_LIGHTING`, `ISLAND` (still routable as Detail destinations in parts of the app; not shown as Labs cards).

## Traceability Matrix

| ID | Tool | Category | Entry Exists | Route / Destination (DetailHost) | Backend (observed) | UI Status | Evidence |
|---|---|---|---|---|---|---|---|
| LAB-001 | App Ops | App Management | Yes | `Detail.APP_OPS -> ManageScreen` | PackageManager; Shizuku for privileged ops | Partially implemented | `ManageScreen.kt` lists packages, filters, opens detail; ops depend on backend. |
| LAB-002 | Firewall | App Management | Yes | `FIREWALL -> LabsToggleScreen(AppToggleFeature.FIREWALL)` | Shizuku/ADB/PackageTools | Partially implemented | `LabsToggleScreen` reads/writes blocked/network state. |
| LAB-003 | Autostart | App Management | Yes | `AUTOSTART -> LabsToggleScreen(AppToggleFeature.AUTOSTART)` | Shizuku/ADB app ops | Partially implemented | Uses `RUN_ANY_IN_BACKGROUND` op path. |
| LAB-004 | Hiding | App Management | Yes | `HIDING -> HidingScreen`; per-mode `HIDE_* -> LabsToggleScreen` | Shizuku/ADB/Hiding service | Partially implemented | Grouped entry + 6 mode details; mode screens manage app lists/signals. |
| LAB-005 | Force Dark Mode | App Management | Yes | `FORCE_DARK -> ForceDarkScreen` | Shizuku/ADB/ForceDark | Partially implemented | Gate + per-app toggles; closes app on toggle for effect. |
| LAB-006 | Universal Debloater (UAD) | App Management | Yes | `DEBLOATER -> DebloaterScreen` | Shizuku/ADB package ops | Partially implemented | UAD list/filter/actions; destructive ops require care (verify confirmations). |
| LAB-007 | App Freezer | App Management | Yes | `FREEZER -> FreezerScreen` | Shizuku/ADB package ops | Partially implemented | Freeze/restore operations; state depends on backend. |
| LAB-008 | Axeron Gaming Optimizer | App Management | Yes | `OPTIMIZER -> OptimizerScreen` | Shizuku/ADB/dexopt profiles | Partially implemented | Profile apply; no FPS claims in UI copy added. |
| LAB-009 | Power | System Controls | Yes | `POWER -> PowerScreen` | Shizuku/ADB shell | Partially implemented | Confirmation dialog + shell commands. |
| LAB-010 | Display | System Controls | Yes | `DISPLAY -> DisplayScreen` | Shizuku/ADB `wm` | Partially implemented | Reads/applies size/density; validation present. |
| LAB-011 | Wi-Fi Passwords | System Controls | Yes | `WIFI_PASSWORDS -> WifiPasswordsScreen` | Shizuku/ADB/WifiControl | Partially implemented | Reads networks; password visibility backend-dependent. |
| LAB-012 | SystemUI Tuner | System Controls | Yes | `SYSTEMUI_TUNER -> SystemUiTunerScreen` | Shizuku/ADB settings | Partially implemented | Status bar/immersive/settings explorer tabs. |
| LAB-013 | Enforce Doze | System Controls | Yes | `ENFORCE_DOZE -> DozeScreen` | Shizuku/ADB/dumpsys/deviceidle | Partially implemented | Doze states/toggles/service integration. |
| LAB-014 | Private DNS Controller | System Controls | Yes | `PRIVATE_DNS -> PrivateDnsScreen` | Shizuku/ADB settings | Partially implemented | Mode/hostname apply; refresh. |
| LAB-015 | Audio Stream Master | System Controls | Yes | `AUDIO_MASTER -> AudioMasterScreen` | Shizuku/ADB/audio | Partially implemented | Stream controls; device support varies. |
| LAB-016 | Carrier Tethering | System Controls | Yes | `TETHER -> TetherScreen` | Shizuku/ADB/settings | Partially implemented | Tether controls; carrier/device support varies (no bypass claims in copy). |
| LAB-017 | Mixed Audio | System Controls | Yes | `MIXED_AUDIO -> MixedAudioLauncher` | Shizuku/ADB/appops | Partially implemented | Launcher-based flow. |
| LAB-018 | SoundMaster | System Controls | Yes | `SOUND_MASTER -> SoundMasterScreen` | Shizuku/ADB/audio | Partially implemented | Per-app/stream controls where supported. |
| LAB-019 | UniversalPiP | System Controls | Yes | `UNIVERSAL_PIP -> UniversalPiPScreen` | Shizuku/ADB/settings/appops | Partially implemented | PiP controls; app support varies. |
| LAB-020 | Screen Security & DRM | System Controls | Yes | `SECURITY -> ScreenSecurityScreen` | Shizuku/ADB/appops/settings | Partially implemented | Security/capture controls info; no universal bypass claimed. |
| LAB-021 | Auto DND (Zen Mode) | System Controls | Yes | `AUTODND -> AutoDndScreen` | Shizuku/ADB/notification policy | Partially implemented | Automation/toggles; permission-dependent. |
| LAB-022 | Theme Patcher | System Controls | Yes | `THEME_PATCHER -> ThemePatcherScreen` | Shizuku/ADB/files | Partially implemented | Patching workflow; compatibility/risk vary. |
| LAB-023 | DFRoot | System Controls | Yes | `DFROOT -> DFRootScreen` | Kernel/exploit integration (user-initiated) | Partially implemented | Diagnostics/integration UI; must remain explicit and compatibility-gated. |
| LAB-024 | Shell | Utilities | Yes | `SHELL -> ShellScreen` | Shizuku/ADB/local shell | Partially implemented | Interactive shell, history/bookmarks; complex screen. |
| LAB-025 | Log | Utilities | Yes | `LOG -> LogScreen` | logcat/diag export | Partially implemented | App/capture tabs, filters, export/share. |
| LAB-026 | Device Info | Utilities | Yes | `DEVICE_INFO -> DeviceInfoScreen` | System APIs | Partially implemented | Hardware/build info sections. |
| LAB-027 | Activities Launcher | Utilities | Yes | `ACTIVITIES -> ActivitiesScreen` | PackageManager/intents | Partially implemented | Activity discovery/launch workflows. |
| LAB-028 | Files | Utilities | Yes | `FILES -> FilesScreen` | Shizuku/ADB/file ops | Partially implemented | Privileged file browser; storage restrictions apply. |
| LAB-029 | App Installer | Utilities | Yes | `INSTALL -> InstallerScreen` | Package installer/Shizuku | Partially implemented | APK/split install paths; Android constraints apply. |
| LAB-030 | Terminal Apps | Utilities | Yes | `TERMINAL -> TerminalScreen` | External terminal intents | Partially implemented | Integration/launch UI. |
| LAB-031 | LocalShell | Utilities | Yes | `LOCAL_SHELL -> LocalShellScreen` | Local ADB/IntentShell bridge | Partially implemented | Connection/command flow. |
| LAB-032 | Intents | Utilities | Yes | `INTENTS -> IntentsScreen` | Intent/broadcast APIs | Partially implemented | Intent builder/execution; review before send. |
| LAB-033 | Stealth Mode | Utilities | Yes | `STEALTH -> StealthScreen` | Shizuku/ADB/hiding integrations | Partially implemented | Visibility controls; no universal evasion claim. |
| LAB-034 | Permissions | Utilities | Yes | `PERMISSIONS -> PermissionsScreen` | Permission APIs | Partially implemented | Runtime/permission overview/management where supported. |
| LAB-035 | Shizuku Store (More Apps) | Utilities | Yes | `MORE_APPS -> CatalogScreen(APPS)` | Network/catalog | Partially implemented | Catalog/search UI; content must remain real. |
| LAB-036 | Call Recording | Utilities | Yes | `CALL_RECORDING -> CallRecorderScreen` | Shizuku/ADB/audio/call | Partially implemented | Recording depends on Android/device/permissions. |
| LAB-037 | WallYou Wallpapers | Utilities | Yes | `WALLYOU -> WallYouScreen` | Network/wallpaper APIs | Partially implemented | Wallpaper browse/apply; settings route exists. |
| LAB-038 | Quick Settings Tiles | Companion Apps | Yes | Companion entry (`com.rbn.qtsettings`) | External app | Partially implemented | Launches companion or opens release URL via `openCompanionTool`. |
| LAB-039 | ShizuWall Firewall | Companion Apps | Yes | Companion entry (`com.arslan.shizuwall`) | External app | Partially implemented | Launches companion or opens release URL via `openCompanionTool`. |

## Inconsistencies / Notes (to resolve in later batches)
- **Hidden details:** `ESSENTIALS`, `SMART_PIXELS`, `MEDIA_KEYS`, `WATERMARK`, `EDGE_LIGHTING`, `ISLAND` are filtered from Labs; some are still routable in DetailHost. Keep navigation safe; do not remove entries without product decision.
- **FIREWALL/AUTOSTART routing:** Routed via shared `LabsToggleScreen` with feature-specific behavior — correct but UI must clearly identify per-tool context.
- **Launchers vs screens:** `MIXED_AUDIO` uses `MixedAudioLauncher`; some screens have specialized launchers. Redesigns must preserve these flows.
- **Claims/safety:** Carrier bypass, DRM bypass, universal detection evasion, and FPS guarantees must remain non-absolute and backend/device-dependent.
- **State standardization:** Many screens now have header descriptions (partial batch work) but still need full standardized loading/empty/error/permission/backend-unavailable/success/danger states and confirmations where applicable.

## Next Audit Steps
- Per-tool deep audit (ViewModel/service/permissions/known bugs/missing functionality) during LAB-001..LAB-039 execution.
- Update classifications with build/test evidence and mark Fully implemented only when verified.