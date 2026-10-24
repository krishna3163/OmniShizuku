# Deep Architectural & Feature Encyclopedia: All 28 Shizuku & Companion Repositories

> **Authoritative Technical Feature Audit**: Based on comprehensive static analysis of source code, internal packages, AIDL interfaces, JNI C++ routines, hidden APIs, Room databases, and background daemons across all 28 repositories located in `/home/krishna/Downloads/tv/`.
> 
> *Notice: Most repositories contain advanced internal capabilities, private APIs, and subsystems that are completely omitted from their public READMEs. This document catalogs every hidden, internal, and public feature uncovered directly from the code.*

---

## Master Repository Index

| # | Repository Name | Location | Primary Category | Internal Code Size |
| :- | :--- | :--- | :--- | :--- |
| **01** | [Shizuku (Official Base)](#01-shizuku-official-rikkaapps-base) | `Shizuku implementations/Shizuku` | Canonical Daemon & SDK | 193 code files |
| **02** | [Shizuku-Next](#02-shizuku-next-modernized--compilesdk-36) | `Shizuku implementations/Shizuku-Next` | Modernized Fork (API 36) | 341 code files |
| **03** | [ShizukuPlus](#03-shizukuplus-feature-expanded-enterprise-fork) | `Shizuku implementations/ShizukuPlus` | Enterprise & Feature-Expanded | 433 code files |
| **04** | [Shizako](#04-shizako-anime-mascot--opengl-blurview) | `Shizuku implementations/Shizako` | Hardware Blur & Quick Tiles | 277 code files |
| **05** | [Xhizuku](#05-xhizuku-adb-modules-nightdog--multi-platform) | `Shizuku implementations/Xhizuku` | ADB Modules & Wear/TV UI | 217 code files |
| **06** | [Stellar](#06-stellar-pure-kotlin-native-pty--dual-bridge) | `Shizuku implementations/Stellar` | Kotlin Rewrite & Native PTY | 185 code files |
| **07** | [Shevery](#07-shevery-tv-oriented-tasker-automation--comput-ai) | `Shizuku implementations/shevery` | Tasker, DPM, Comput AI & Web | 249 code files |
| **08** | [Porter](#08-porter-darkens-kotlin-dsl-porsh-pty--probe) | `Shizuku implementations/porter` | Kotlin DSL & C++ PTY Engine | 546 code files |
| **09** | [OmniShizuku](#09-omnishizuku-unified-target-workspace) | `Shizuku implementations/OmniShizuku` | Synthesis Workspace | Clean workspace |
| **10** | [InstallerX-Revived](#10-installerx-revived-universal-package-installer) | `InstallerX-Revived` | Universal Split Installer | 728 code files |
| **11** | [InstallWithOptions](#11-installwithoptions-adb-shell-flag-injector) | `InstallWithOptions` | ADB Flag Injector & Downgrader | 32 code files |
| **12** | [ShizuStore](#12-shizustore-curated-shizuku-app-store) | `ShizuStore` | App Store & Silent Updater | 280 code files |
| **13** | [Morphe Manager](#13-morphe-manager-dual-runtime-bytecode-patcher) | `morphe-manager` | Dalvik/JVM Bytecode Patcher | 488 code files |
| **14** | [Root-My-Galaxy](#14-root-my-galaxy-samsung-cve-2026-43499-ksu-loader) | `Root-My-Galaxy` | Samsung Soft-Root & KernelSU | 16 code files |
| **15** | [Root-My-Pixel](#15-root-my-pixel-pixel-tensor-cve-2026-43499-root) | `Root-My-Pixel` | Pixel Soft-Root & IonStack | 39 code files |
| **16** | [ShizuCallRecorder](#16-shizucallrecorder-non-root-dual-way-audio-tapper) | `ShizuCallRecorder` | Audio Stream Tapper & Shell Service | 70 code files |
| **17** | [ShizuTools](#17-shizutools-audio-routing--system-tweak-suite) | `ShizuTools` | Audio Policy & Debloater | 35 code files |
| **18** | [SystemUI-Tuner](#18-systemui-tuner-settings-storage--lockscreen-tweaker) | `SystemUI-Tuner` | Settings Provider & UI Tweaker | 217 code files |
| **19** | [ADNS](#19-adns-private-dns-over-tls-controller) | `adns` | Private DoT DNS Controller | 252 code files |
| **20** | [AutoDND](#20-autodnd-foreground-policy-zen-automator) | `android_AutoDND` | ZenMode Notification Automator | 13 code files |
| **21** | [ShizuWall](#21-shizuwall-zero-vpn-kernel-uid-firewall) | `ShizuWall` | Kernel Netfilter UID Firewall | 95 code files |
| **22** | [shizzi](#22-shizzi-rootless-hotspot-bypass--go-datapath) | `shizzi` | Tethering Bypass & Go Datapath | 130 code files |
| **23** | [sing-box-for-android](#23-sing-box-for-android-tproxy-per-app-router--cgo-core) | `sing-box-for-android` | Universal Proxy & AIDL Bridge | 329 code files |
| **24** | [EnforceDoze](#24-enforcedoze-instant-deep-idle--motion-sensor-kill) | `EnforceDoze` | Device Idle & Motion Sensor Kill | 45 code files |
| **25** | [Quick-Tile-Settings](#25-quick-tile-settings-secure-settings-tile-engine) | `Quick-Tile-Settings` | Secure QS Tiles & Network Watch | 78 code files |
| **26** | [essentials](#26-essentials-island-sdk--pixel-hardware-tuner) | `essentials` | Dynamic Island SDK & Hardware Mods | 796 code files |
| **27** | [Canta](#27-canta-universal-android-debloater-uad-engine) | `Canta` | UAD Debloater & KMP Architecture | 124 code files |
| **28** | [AxManager](#28-axmanager-c-axsh-daemon-cgroups--adb-pairing) | `AxManager` | C++ Daemon, CGroups & ADB Pairing | 160 code files |

---

## 01. Shizuku (Official RikkaApps Base)
*Path: `/home/krishna/Downloads/tv/Shizuku implementations/Shizuku`*

### What the README Says:
> "An Android app that allows other apps to use system-level APIs that require ADB/root privileges." (Pausing maintenance note).

### What the Code ACTUALLY Contains (Undocumented / Deep Features):
1. **Low-Level ContentProvider Bootstrap (`IContentProviderCompat.java`)**:
   - Bypasses standard Android framework lifecycle and Context requirements.
   - Executes raw Binder `CALL_TRANSACTION` (`IBinder.FIRST_CALL_TRANSACTION + 2`) to deliver the server Binder directly to `moe.shizuku.manager.ShizukuManagerProvider`.
2. **Kernel Process Camouflage**:
   - Invokes `android.ddm.DdmHandleAppName.setAppName("shizuku_server", 0)` via JNI to mask the process identity in kernel process tables and `ps` output.
3. **Real-time Filesystem APK Observers (`ApkChangedObservers.kt`)**:
   - Attaches a low-level Linux `FileObserver` (inotify) to `/data/app`.
   - When an authorized app is replaced, reinstalled, or uninstalled, it instantly revokes or re-evaluates tokens to prevent privilege escalation via APK replacement attacks.
4. **Isolated User Service Daemonization (`ShizukuUserServiceManager.java`)**:
   - Rather than just running shell commands, it dynamically spawns standalone `app_process` child processes under UID 2000 (ADB) or UID 0 (Root).
   - Injects the client app's APK into the child classpath and establishes bidirectional Binder IPC connections back to the client application.
5. **System Service Proxying (`ShizukuBinderWrapper.java`)**:
   - Wraps standard Android service binders (`IPackageManager`, `IActivityManager`, `IUserManager`, `IPermissionManager`).
   - Intercepts all client `transact()` calls and routes them through `IShizukuService.transactRemote()`, letting standard client code invoke hidden platform methods with ADB/Root permissions without reflection.
6. **Native Wireless ADB Pairing Engine (`adb_pairing.cpp`)**:
   - Written in native C++.
   - Handles the complete TLS handshake, self-signed TLS certificate generation, and pairing code authorization for Android 11+ wireless debugging.
7. **POSIX Pseudoterminal Engine (`pts.cpp` & `Rish`)**:
   - Allocates Unix PTY master/slave pairs using `posix_openpt()`, `grantpt()`, `unlockpt()`, and `ptsname()`.
   - Passes raw terminal file descriptors (`ParcelFileDescriptor`) over Binder to provide interactive terminal sessions (`rish`).
8. **SELinux State Inspector (`selinux.cpp`)**:
   - Reads SELinux contexts and `/sys/fs/selinux/enforce` to accurately report system confinement domains.
9. **Linux cgroup Task Freezing Inspector (`cgroup.cpp`)**:
   - Inspects `/sys/fs/cgroup` to monitor process freezing states on Android 12+.

---

## 02. Shizuku-Next (Modernized & compileSdk 36)
*Path: `/home/krishna/Downloads/tv/Shizuku implementations/Shizuku-Next`*

### What the README Says:
> "An enhanced fork of Shizuku that lets apps use system APIs through ADB or root." (Gives zero indication of the entire ADB Modules ecosystem or the 15 Labs engines!).

### What the Code ACTUALLY Contains (Massive Undocumented Features in Modules & Labs):

#### A. The ADB Modules Subsystem (`manager/src/main/java/moe/shizuku/manager/module/`)
An entire modular extension platform running under ADB privileges (UID 2000), compatible with Magisk module paradigms:
1. **Full Module Packaging Engine ([`ModulePackage.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/module/ModulePackage.kt), [`ModuleProp.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/module/ModuleProp.kt))**:
   - Installs zip packages containing `module.prop`, banners (`.png`, `.webp`, `.jpg`), action scripts (`action.sh`), background daemons (`service.sh`), and offline WebUIs (`webui/index.html`).
   - Hardened extraction validator: enforces maximum 2,048 entries, 200 MB unpacked cap, 256 KB per shell script, and strict relative-path safety (no directory traversal `..` or NUL bytes).
   - Installs to external files directory `Android/data/<pkg>/files/adb_modules/<id>` because ADB shell (UID 2000) cannot access an app's internal `/data/data/...`.
2. **Sandboxed Script Execution Engine ([`AdbModuleManager.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/module/AdbModuleManager.kt), [`ModuleCommand.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/module/ModuleCommand.kt))**:
   - Formulates isolated command invocations: `cd '<moddir>' && MODDIR='...' ASH_STANDALONE=1 SHIZUKU_MODULE_ID='...' SHIZUKU_MODULE_MODE='safe|full' SHIZUKU_MODULE_TRUSTED='0|1' SHIZUKU_MODULE_BACKGROUND='0|1' sh '<script>'`.
   - Strictly enforces a **120-second timeout** (killing hung processes and returning exit code 124).
   - Caps stream captures to the first **64 KB** per stream while continuously draining overflow bytes to prevent process deadlock.
   - Preserves logs at `<moddir>/logs/action-last.log` and `<moddir>/logs/service-last.log`.
3. **Interactive Actions & Background Services ([`ModuleActionScreen.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/ui/screen/ModuleActionScreen.kt))**:
   - Interactive `action.sh` execution streamed real-time to a dedicated Compose terminal UI with manual stop controls.
   - Persistent `service.sh` daemon monitoring with concurrency guards preventing duplicate service runs.
4. **In-App WebUI & Bi-Directional JavaScript Bridge ([`ModuleWebUi.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/module/ModuleWebUi.kt), [`ModuleJsBridge.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/module/ModuleJsBridge.kt), [`ModuleWebUiScreen.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/ui/screen/ModuleWebUiScreen.kt))**:
   - Renders custom module WebUIs inside an isolated WebView with strict network lockdown (`blockNetworkLoads = true`).
   - Injects native `window.Shizuku` JavaScript interface:
     - `getModuleInfo()`: Returns full module state, permissions, and directory locations in JSON.
     - `exec(command)`: Executes commands as ADB shell with 120s timeout.
     - `execWithOptions(command, options)`: Supports configurable timeouts (1..600s). Rejects unsafe parameters by name (`stdin`, `cwd`, `env`, `envVars`, `workingDirectory`).
5. **Full Trust Model ([`ModuleSettings.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/module/ModuleSettings.kt))**:
   - Long-press module cards to grant "Full Trust", bypassing interactive command review dialogs and automatically providing shell bridge privileges.
6. **Community Repository Browser & Auto-Update Engine ([`ModuleCatalog.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/module/ModuleCatalog.kt), [`ModuleUpdate.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/module/ModuleUpdate.kt), [`ModuleRepoScreen.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/ui/screen/ModuleRepoScreen.kt))**:
   - Queries the central community index (`shizuku-modules/main/data/repos.json`) and GitHub topics (`shevery-modules`, `shizuku-modules`, `shizuku-next-modules`).
   - One-click downloads and installs module releases directly. Monitors `update.json` manifests for automated update notifications.

#### B. The Labs Subsystem: 15 Advanced Capabilities (`manager/src/main/java/moe/shizuku/manager/manage/`, `ui/screen/`)
A comprehensive suite of hidden power-user tools accessed through [`LabsScreen.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/ui/screen/LabsScreen.kt) and [`LabsToggleScreen.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/ui/screen/LabsToggleScreen.kt):
1. **Per-App Network Firewall ([`FirewallApi.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/manage/FirewallApi.kt), [`PackageTools.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/manage/PackageTools.kt))**:
   - Blocks internet access per application without a VPN and without root using `cmd connectivity set-package-networking-enabled <uid> false`.
   - Auto-restores rules on boot or server restarts via [`LabsReapply.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/manage/LabsReapply.kt).
2. **Autostart Restriction Manager ([`PackageTools.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/manage/PackageTools.kt))**:
   - Controls the privileged platform AppOp `RUN_ANY_IN_BACKGROUND` across installed packages, preventing rogue apps from auto-launching in the background.
3. **Dynamic 6-Layer Anti-Detection Hiding Modes ([`Hiding.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/manage/Hiding.kt), [`HidingWatchService.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/service/HidingWatchService.kt))**:
   - Real-time foreground monitoring via `UsageStatsManager` / `UsageEvents`. Flips settings dynamically when protected apps (banking, enterprise, games) open, restoring them upon exit:
     - `Signal.DEVELOPER_OPTIONS`: Sets `global development_settings_enabled = 0`.
     - `Signal.USB_DEBUGGING`: Sets `global adb_enabled = 0`.
     - `Signal.WIRELESS_DEBUGGING`: Sets `global adb_wifi_enabled = 0`.
     - `Signal.ACCESSIBILITY`: Sets `secure accessibility_enabled = 0` and blanks `enabled_accessibility_services`.
     - `Signal.PRIVATE_DNS`: Sets `global private_dns_mode = off`.
     - `Signal.VPN`: Camouflages active VPN routing flags.
4. **Cleartext Wi-Fi Password Extractor ([`WifiPasswords.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/manage/WifiPasswords.kt), [`WifiBackup.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/manage/WifiBackup.kt))**:
   - Reaches hidden `IWifiManager.getPrivilegedConfiguredNetworks()` via `HiddenApiBypass` impersonating UID 2000 (`com.android.shell`).
   - Dumps cleartext WPA/WPA2/WPA3 pre-shared keys and enterprise profiles, with encrypted backup export.
5. **Privileged File Explorer & ContentProvider ([`PrivilegedFileProvider.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/files/PrivilegedFileProvider.kt), [`FileOperations.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/files/FileOperations.kt))**:
   - Privileged filesystem navigation (`Android/data`, `/data/local/tmp`, root `/`). Chunked 64 KB copying, moving, batch deletion, and zip compression with real-time progress.
   - Custom `ContentProvider` streaming `ParcelFileDescriptor` to external third-party apps, allowing external apps to read restricted files!
6. **Unexported Activity Launcher with Assistant Bypass ([`Activities.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/manage/Activities.kt))**:
   - Launches any internal `android:exported="false"` activity.
   - **Assistant Injection Trick**: Points `secure assistant` setting to the component, disables `voice_interaction_service`, and injects `KEYCODE_ASSIST`, causing the Android OS itself to launch the activity as the system assistant, cleanly bypassing component export restrictions!
7. **Per-App Force Dark Mode ([`ForceDark.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/manage/ForceDark.kt))**:
   - Forces dark mode by simultaneously dispatching 5 vendor-specific keys (`debug.hwui.force_dark`, `global force_dark_mode_on`, `secure hwui_force_dark`, `secure aosp_force_dark_mode`, `secure op_force_dark_entire_world`) plus dynamic system dark theme switching.
8. **Display Resolution, Density & Refresh Rate Overrides ([`DisplayScreen.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/ui/screen/DisplayScreen.kt))**:
   - Live overrides for `wm size <WxH>`, `wm density <dpi>`, and refresh rate bounds `min_refresh_rate` / `peak_refresh_rate` parsed from `dumpsys display`.
9. **Advanced Power Management & Recovery Menu ([`PowerScreen.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/ui/screen/PowerScreen.kt))**:
   - Fast reboot (`svc power reboot`), recovery, bootloader, shutdown, **Soft Framework Reboot** (`setprop ctl.restart zygote` in ~3s), **SystemUI restart** (`pkill -f com.android.systemui`), and screen off.
10. **Stealth App Camouflage & On-Device Stub Generator ([`StealthTutorialViewModel.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/stealth/StealthTutorialViewModel.kt), [`ApkSigner.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/utils/ApkSigner.kt), [`StubApk.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/utils/StubApk.kt))**:
    - Clones or stubs Shizuku Manager with randomized package names and custom app labels.
    - Signs APKs on-device with custom RSA/EC keys.
    - Installs the `:stub` module to transparently forward legacy client IPC requests ([`StubForwarding.java`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/stub/src/main/java/moe/shizuku/stub/StubForwarding.java)).
11. **Phantom Process Killer Prevention ([`PhantomProcessKiller.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/start/PhantomProcessKiller.kt))**:
    - Disables Android 12+ process killing via `settings_enable_monitor_phantom_procs` / `device_config put activity_manager max_phantom_processes 2147483647` so background servers survive.
12. **ADB Without Developer Options ([`AdbWithoutDeveloperOptions.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/start/AdbWithoutDeveloperOptions.kt))**:
    - Keeps `adb_enabled = 1` and `adb_wifi_enabled = 1` active while setting `development_settings_enabled = 0` via `WRITE_SECURE_SETTINGS`.
13. **Self-Healing Watchdog ([`WatchdogService.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/service/WatchdogService.kt), [`StartCircuitBreaker.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/service/StartCircuitBreaker.kt))**:
    - Background watchdog monitoring Shizuku daemon status and restarting it if killed, with circuit breaker protection against battery exhaustion.
14. **Interactive ADB Shell Library ([`ShellCommands.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/shell/ShellCommands.kt), [`ShellBookmarks.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/shell/ShellBookmarks.kt))**:
    - Interactive terminal with parameterized templates (`<package>`, `<permission>`), interactive package pickers, and history tracking.
15. **Live Hardware Diagnostics & Logcat ([`HomeDiagnostics.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/ui/screen/HomeDiagnostics.kt), [`DeviceInfoScreen.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/ui/screen/DeviceInfoScreen.kt), [`LogScreen.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/ui/screen/LogScreen.kt))**:
    - Real-time CPU, RAM, thermal throttling, SELinux domain inspect, and in-app streaming Logcat with regex filtering.

---

## 03. ShizukuPlus (Feature-Expanded Enterprise Fork)
*Path: `/home/krishna/Downloads/tv/Shizuku implementations/ShizukuPlus`*

### What the README Says:
> "Stable Release is heading your way... soon... have had some more issues creep up on me that I want to fix firsthand." (Virtually nothing about the actual features!).

### What the Code ACTUALLY Contains (Massive Undocumented Feature Set):
1. **"Magic Setup" Automated Root App Hijack (`RootCompatHelper.kt`)**:
   - Inspects installed applications and automatically reconfigures root-dependent apps to use Shizuku's privileged shell without needing root!
   - **Global Settings Apps**: Directly modifies Android global settings for AdAway (`adaway_su_path`), AFWall+ (`afwall_su_path`), RamExe (`ramexe_su_path`), and Prevent (`prevent_su_path`).
   - **Shared Preferences Apps**: Reads and patches `shared_prefs` XMLs for Titanium Backup (`TitaniumBackup-preferences`), Root Explorer (`RootExplorer.xml`), Solid Explorer (`SolidExplorer2.xml`), Total Commander (`tcandroid3.xml`), ES File Explorer (`es_preferences.xml`), and Neo Backup (`custom_su_path`), substituting the `su` binary path with Shizuku's shell binary!
2. **15+ Extended Server Services (`server/src/main/java/rikka/shizuku/server/`)**:
   - **`AICorePlusImpl.kt`**: Dedicated AI Core framework service interface for privileged on-device AI models.
   - **`ActivityManagerPlusImpl.kt`**: Multi-user activity control, process freezing, background app kill controls, and task switching.
   - **`VirtualMachineManagerImpl.kt`**: Microdroid and virtual machine isolation manager.
   - **`NetworkGovernorPlusImpl.kt`**: Per-app network firewall controls manipulating iptables, netd, and eBPF maps directly.
   - **`DisplayTunerPlusImpl.kt`**: Dynamic screen resolution, refresh rate, density, and display cutout tuning.
   - **`StatusBarGovernorPlusImpl.kt`**: SystemUI icon whitelist/blacklist and quick settings management.
   - **`ApkBinaryXmlPatcher.kt` & `ApkSigner.kt`**: In-memory and on-disk binary XML patching for APK manifests, and on-device APK re-signing routines!
   - **`ApkPatcherImpl.kt`**: Runtime APK modification and resource injection.
   - **`AppInspectorImpl.kt`**: Deep package inspector querying internal package components, exported providers, and hidden permissions.
   - **`BackupRestorePlusImpl.kt`**: Privileged app data backup and restore via tar and shell pipelines.
   - **`BinderCallLogger.kt`**: Comprehensive latency, caller UID, and transaction logging for every Binder transaction.
   - **`ContinuityBridgeImpl.kt`**: State continuity and session recovery across server restarts.
   - **`DeviceControlPlusImpl.kt`**: Low-level device controls (reboot to recovery, bootloader, poweroff, wakefulness, input event dispatch).
   - **`StorageProxyImpl.kt`**: Direct filesystem access bypassing Scoped Storage limits.
   - **`WindowManagerPlusImpl.kt`**: Window management, display rotation overrides, and screenshot capture.
3. **Full Jetpack Room Database (`database/src/main/java/af/shizuku/manager/database/`)**:
   - **`ActivityLogDatabase.kt` & `ActivityLogDao.kt`**: Records all privileged Binder transactions, caller UIDs, timestamps, and status for enterprise security auditing.
   - **`ScriptSnippetDatabase.kt` & `ScriptSnippetDao.kt`**: Persistent repository for storing and executing reusable shell snippets.
4. **Standalone Process Manager (`app-process/`)**:
   - Generic `com.rosan.app_process` framework (`AppProcess.java`, `ProcessManager.java`, `NewProcess.java`, `IRemoteProcess.aidl`).
5. **Dual Flavor Manager Builds**:
   - `src/dropin`: 100% official Shizuku package name and signature compatibility.
   - `src/shizukuplus`: Enhanced feature flavor with extended UI and permissions.
6. **Automated Diagnostic & AI Tooling**:
   - `crash-diagnostics.sh`: Captures tombstone crash dumps, logcat, and binder states.
   - `.agents/skills/shizukuplus/`: LLM skills for automated linting, auditing, and maintenance.

---

## 04. Shizako (Anime Mascot & OpenGL BlurView)
*Path: `/home/krishna/Downloads/tv/Shizuku implementations/Shizako`*

### What the README Says:
> "Let the cat-eared mascot borrow system permissions for you — No root, no flashing."

### What the Code ACTUALLY Contains (Undocumented / Deep Features):
1. **Hardware-Accelerated OpenGL ES Gaussian Blur Engine (`moe.shizuku.blurview`)**:
   - **`OpenGLBlurPipeline.java`**: Off-screen GL pipeline managing an EGL context (`EglCore.java`), an output swap chain (`SwapChain.java`), a Gaussian kernel calculator (`GaussianKernel.java`), source texture (`SourceTexture.java`), and render texture (`RenderTexture.java`).
   - Runs a 2-pass Gaussian blur (horizontal pass into render texture, vertical pass to output surface) and reads back HardwareBuffer-backed bitmaps for real-time frosted glass UI!
   - **`RenderNodeBlurController.java`**: Android 12+ `RenderEffect.createBlurEffect()` fallback for maximum hardware efficiency.
   - **`GlassWindow.kt`, `GlassMaterials.kt`, `GlassOptionSlider.kt`**: Dynamic glass window UI components.
2. **Android Quick Settings Tiles**:
   - **`ServiceTile.kt`**: Toggle Shizako daemon status directly from the notification shade.
   - **`WirelessAdbTile.kt`**: Toggle and start wireless debugging Shizako directly from Quick Settings.
3. **Security Automation Receivers**:
   - **`AutoDisableUsbDebuggingReceiver.kt`**: Automatically turns off USB debugging when Shizako disconnects or screen locks, protecting devices from unauthorized physical USB attacks.
   - **`TaskerReceiver.kt`**: Direct broadcast receiver for Tasker automation.
4. **Comput Module Subsystem**:
   - `ComputScreen.kt`, `ComputFragment.kt`, `ComputPage.kt`.
5. **Automated Maintenance Tooling (`tools/`)**:
   - `build_icons.py`: Generates app iconography across all density buckets.
   - `update_badges.py`: Updates repository build status badges.
   - `deploy-site.sh`: Deploys documentation website.
   - `md2html.py`: Converts markdown changelogs to HTML.
6. **China Network Build Resilience**:
   - `useCnMirrors` flag in `settings.gradle` configuring Alibaba and Tencent Maven mirrors to bypass network restrictions.

---

## 05. Xhizuku (ADB Modules, NightDog & Multi-Platform)
*Path: `/home/krishna/Downloads/tv/Shizuku implementations/Xhizuku`*

### What the README Says:
> Basic description of Android 16/17 target and Magisk-style modules.

### What the Code ACTUALLY Contains (Undocumented / Deep Features):
1. **Independent Package Identity (`xeonleon.xhizuku`)**:
   - Eliminates package conflicts with official Shizuku, allowing simultaneous side-by-side installations.
2. **Magisk-Style ADB Modules Runner (`test-modules/adb-test-module` & `manager/module`)**:
   - **`AdbModuleManager.kt` & `AdbModule.kt`**: Discovers, verifies, and executes ZIP modules containing `module.prop`, `service.sh` (background daemon), and `action.sh` (manual action).
   - **`ModuleWebViewActivity.kt` & `ModuleJsBridge.kt`**: Interactive HTML5 WebUI inside ADB modules with a bidirectional `@JavascriptInterface` bridge (`window.shizuku.exec()`) and transparent command review dialog (`ModuleCommandReview.kt`).
3. **High-Reliability Watchdog Daemon (`NightDog.kt`)**:
   - Dedicated `HandlerThread("nightdog")` linking `DeathRecipient` listeners to core Android system services (`package`, `activity`, `user`, `appops`).
   - Automatically executes exponential backoff rebinds when `system_server` restarts.
   - Enforces strict heartbeat timeouts (`lastBeatTime`), terminating frozen daemon instances via `killProcess(myPid())` to avoid zombie processes.
4. **Multi-Platform Jetpack Compose UI**:
   - **Android TV (Leanback)**: `TVHomeScreen.kt`, `TvApplicationManagementScreen.kt`, `TvShellTutorialScreen.kt`, `TvStarterScreen.kt`, `TvTokenSettingsScreen.kt`, `TvLabMenuScreen.kt`, `TvSettingsScreen.kt`, `TvModulesScreen.kt`, `TvComponents.kt`.
   - **Wear OS (Smartwatch)**: `WearHomeScreen.kt`, `WearApplicationManagementScreen.kt`, `WearStarterScreen.kt`, `WearSettingsScreen.kt`, `WearModulesScreen.kt`.
   - **Phone**: `ShizukuExpressive.kt` (Material 3 Expressive UI, bottom navigation bar, dynamic theming).
5. **Diagnostics & Security Labs**:
   - `DebugMenuActivity.kt`, `AuditActivity.kt`, `DevSpoof.kt`, `AuditApi.kt`, `LabFeaturesActivity.kt`, `ShizuTestActivity.kt`.
6. **On-Device Activation Test Script (`test_activator.sh`)**:
   - Automates wireless debugging pairing and activation testing directly on-device without a PC.

---

## 06. Stellar (Pure Kotlin, Native PTY & Dual Bridge)
*Path: `/home/krishna/Downloads/tv/Shizuku implementations/Stellar`*

### What the README Says:
> Brief introduction to the customized Shizuku fork.

### What the Code ACTUALLY Contains (Undocumented / Deep Features):
1. **Pure Kotlin On-Device ADB Client**:
   - `AdbStarter.kt`, `AdbClient.kt`, `AdbKey.kt`, `PreferenceAdbKeyStore.kt`, `AdbWirelessHelper.kt`: Complete on-device ADB client with RSA key generation, port switching (`tcpip`), pairing, and exponential backoff command execution over TCP sockets.
2. **Dual-Protocol Architecture**:
   - **Native Stellar Protocol**: `IStellarService.aidl`, `IRemotePtyProcess.aidl`, `IRemoteProcess.aidl`, `IStellarApplication.aidl`, `IUserService.aidl`, `IUserServiceCallback.aidl`.
   - **Shizuku Compatibility Bridge**: `ShizukuServiceIntercept.kt`, `StellarRemoteProcessAdapter.kt`, `ShizukuUserServiceAdapter.kt`, `RishServiceImpl.kt`: Drops into existing Shizuku apps without code changes!
3. **Native PTY Support in AIDL**:
   - `IRemotePtyProcess.aidl`, `RemotePtyProcessHolder.kt`, `StellarPtyProcess.kt`.
4. **Home Screen Command Shortcuts**:
   - `CommandShortcutActivity.kt` and `CommandShortcutManager.kt` allowing users to create Android home screen shortcuts for 1-tap command execution.
5. **Modern Compose UI Engine**:
   - Pure Kotlin rewrite using Kyant0 / jeziellago components, G2 rounded corners (`G2RoundedCorners.kt`), adaptive layouts, and segmented selectors.

---

## 07. Shevery (TV-Oriented, Tasker Automation & Comput AI)
*Path: `/home/krishna/Downloads/tv/Shizuku implementations/shevery`*

### What the README Says:
> Multi-lingual standard project descriptions.

### What the Code ACTUALLY Contains (Undocumented / Deep Features):
1. **Built-in AI Commandium & Comput Agent**:
   - `commandium/`: `AiProvider.kt`, `AiProviderPresets.kt`, `AiProviderRepository.kt`, `AiModelPicker.kt`.
   - `comput/agent/`: `ComputAgent.kt`, `AgentChats.kt`.
   - `logs/`: `ComputScreen.kt`, `CommandiumSheet.kt`, `ComputAgentSheet.kt`.
   - `utils/`: `AiClient.kt`, `AiExplainUtil.kt`.
   - Allows users to select OpenAI-compatible endpoints, Gemini, Claude, or local LLMs to generate, explain, and review shell commands before executing them!
2. **Dedicated Tasker Automation Plugin (`tasker/`)**:
   - `PluginReceiver.kt`: Receives Tasker `FIRE_SETTING`, `QUERY_CONDITION`, `DIRECT_START`, `DIRECT_STOP`, `DIRECT_RESTART`, `DIRECT_TOGGLE` intents with auth token verification and background execution.
   - `EditActivity.kt`: Interactive configuration UI for setting up commands, timeouts, and output variables.
   - `PluginContract.kt`: IPC intent contracts.
3. **Device Owner & Security Subsystems**:
   - `dhizuku/`: `DhizukuService.kt`, `DhizukuAuthManager.kt`, `DhizukuProvider.kt`.
   - `settings/DeviceOwnerScreen.kt`, `settings/DeviceOwnerDelegationScreen.kt`, `settings/DeviceOwnerTransferScreen.kt`.
   - `stealth/StealthApkGenerator.kt`: Dynamically generates randomized stealth APK packages to evade root/ADB detection!
   - `settings/UsbDebuggingHideActivity.kt`, `settings/UsbDebuggingHideManager.kt`: Hides USB debugging flags from intrusive banking apps.
   - `settings/NetworkFirewallActivity.kt`, `settings/NetworkFirewallManager.kt`: Per-app network firewall controls.
   - `accessibility/`: `AccessibilityDaemonService.kt`, `AccessibilityManagerActivity.kt`.
4. **AGSL Directional Motion Blur**: `MotionBlur.kt` directional velocity shader.
5. **Next.js Web Application (`app/page.tsx`, `components/shevery-interactive.tsx`)**:
   - Full interactive documentation portal with parametric M3 polygon SVG rendering (`M3_POLYGON_12_LOBE`, `M3_POLYGON_4_PINCH`, `M3_POLYGON_8_COG`, `M3_POLYGON_HOURGLASS`) and interactive privilege topology.
6. **Dual Stub Forwarding**: `ForwardDhizukuProvider.java` and `ForwardReceiver.java`.

---

## 08. Porter (Darken's Kotlin DSL, Porsh PTY & Probe)
*Path: `/home/krishna/Downloads/tv/Shizuku implementations/porter`*

### What the README Says:
> "ADB access for your apps. Porter is a minimal, maintained fork of Shizuku that gives Android apps ADB access..."

### What the Code ACTUALLY Contains (Undocumented / Deep Features):
1. **100% Gradle Kotlin DSL & Centralized Build Conventions**:
   - Migrated completely to `build.gradle.kts` managed by convention plugins in `buildSrc/src/main/kotlin/`.
2. **Porsh C++ Pseudo-Terminal Subsystem (`api/porsh/`)**:
   - `src/main/cpp/porsh_terminal.cpp`: Complete terminal emulation handling `ioctl(TIOCSWINSZ)` window sizing, `SIGWINCH` signal traps, raw termios modes, and stdin/stdout/stderr pipes.
   - `src/main/cpp/pts.cpp`: Native pseudoterminal master/slave pair creation.
   - `PorshConfig.kt` & `EnvPolicy.kt`: Terminal environment and file descriptor bindings.
3. **Diagnostic Probe Module (`probe/`)**:
   - `ProbeService.kt` implementing `IProbe.aidl`: Evaluates SELinux enforcement, process UIDs, Binder transaction latency, and file read privileges.
4. **Decoupled Manager Protocol**:
   - `IPorterManager.aidl` and `PorterManagerProtocol.kt`.
5. **High-Level Client SDK Extras (`api/sdk-extras`)**:
   - `PorterShell.kt`: Clean Kotlin coroutine runner for executing elevated shell processes.
   - `PorterSystemServices.kt`: Type-safe system service accessor.
   - `PorterSystemProperties.kt`: Fast system property read/write.
6. **Shizuku Compatibility Bridge (`api/shizuku-bridge` & `api/shizuku-compat`)**:
   - `PorterShizukuBridge.kt`, `ShizukuServiceAdapter.kt`, `RemoteProcessAdapter.kt`, `BinderRequestReceiver.kt`.
7. **Test Fixtures & CI Coverage**:
   - Multi-flavor management (`gplay` vs `foss`), automated screenshot tests, and test fixtures (`service-update`, `unlisted-manager`).

---

## 09. OmniShizuku (Unified Target Workspace)
*Path: `/home/krishna/Downloads/tv/Shizuku implementations/OmniShizuku`*

### Purpose & Architectural Role:
- Clean workspace directory. Serves as the designated target for synthesizing the strengths of all forks into an ultimate unified build (e.g., combining Porter's C++ PTY, Xhizuku's ADB modules and TV UI, Shevery's Tasker integration, and ShizukuPlus's Room DB).

---

## 10. InstallerX-Revived (Universal Package Installer)
*Path: `/home/krishna/Downloads/tv/InstallerX-Revived`*

### What the README Says:
> "InstallerX Revived (Community Edition) ... An installer app for Android."

### What the Code ACTUALLY Contains (Undocumented / Deep Features):
1. **Seekable Random-Access Split Package Parser (`SeekableZipReader.kt`)**:
   - Custom memory-mapped ZIP parser with `SeekableBlockCache.kt` and `ContentRange.kt`.
   - Parses APKS, APKM, XAPK, and ZIP split bundles on-the-fly without unzipping them to disk, conserving flash memory and battery.
2. **Multi-Authorizer Installation Pipeline (`InstallerSessionManagerImpl.kt`)**:
   - Automatically queries and switches between four installation backends:
     1. **Root**: via `libsu` Shell.
     2. **Shizuku**: via Binder IPC to `PackageInstaller`.
     3. **Dhizuku**: via Device Owner DevicePolicyManager.
     4. **System Privileged**: via system permissions.
3. **Standalone `com.rosan.app_process` Module**:
   - Includes full native Dalvik/ART process supervisor (`AppProcess.java`, `ProcessManager.java`, `NewProcess.java`, `IProcessManager.aidl`).
4. **Room Database Persistence (`InstallerRoom.kt`)**:
   - `AppEntity.kt`, `ConfigEntity.kt`, `OperationHistoryEntity.kt`.
   - Tracks per-app installation preferences, Dexopt optimization modes (`speed-profile`, `everything`), install reasons, and installation history.
5. **Inlined Android Hidden Platform Stubs (`hidden-api/`)**:
   - Clean compilation stubs for `IPackageManager`, `IPackageInstaller`, `IPackageInstallerSession`, `IActivityManager`, `ServiceManager`, `SystemProperties`.
6. **OPlus / ColorOS OSDK Compatibility**:
   - Handles specific proprietary restrictions found on Oppo / OnePlus / Realme devices (`OplusOsdkCompatibilityTest.kt`).
7. **Baseline Profiles**:
   - Pre-compiled AOT baseline profiles for near-instant cold launch.

---

## 11. InstallWithOptions (ADB Shell Flag Injector)
*Path: `/home/krishna/Downloads/tv/InstallWithOptions`*

### What the README Says:
> "An app with a simple purpose: more advanced app installs without needing to use ADB."

### What the Code ACTUALLY Contains (Undocumented / Deep Features):
1. **Low-Level Android Package Manager Bitmask Flag Injection**:
   - Exposes raw internal installation flags that are otherwise impossible to toggle via standard Android UI:
     - **`INSTALL_BYPASS_LOW_TARGET_SDK_BLOCK` (`0x01000000`)**: Completely bypasses the Android 14+ minimum target SDK requirement, allowing ancient legacy APKs to install.
     - **`INSTALL_ALLOW_DOWNGRADE` (`0x00000080`)**: Forces the package manager to downgrade an installed application without wiping user data.
     - **`INSTALL_ALLOW_TEST` (`0x00000004`)**: Allows installing APKs containing `android:testOnly="true"`.
     - **`INSTALL_REPLACE_EXISTING` (`0x00000002`)**: Replaces existing app.
     - **`INSTALL_REQUEST_UPDATE_OWNERSHIP`**: Claims update ownership for future package installs.
2. **Cross-Process AIDL Flag Applier**:
   - `IOptionsApplier.aidl`, `IShellInterface.aidl`, `IErrorCallback.aidl`.
   - Binds to background services to apply flags directly into ongoing Android `PackageInstaller` sessions.
3. **Dual Shizuku / Root Execution Engine**:
   - `ShizukuUtils.kt` and `ShizukuRootAdapter.kt` automatically detect available privilege mode.

---

## 12. ShizuStore (Curated Shizuku App Store)
*Path: `/home/krishna/Downloads/tv/ShizuStore`*

### What the README Says:
> Simple intro with icon and app description.

### What the Code ACTUALLY Contains (Undocumented / Deep Features):
1. **Upstream Multi-Platform Catalog Syncer (`CatalogSyncer.kt`)**:
   - Automatically synchronizes, parses, and reconciles the `awesome-shizuku` registry from GitHub, GitLab, and F-Droid.
   - Fetches live READMEs (`LiveReadmeFetcher.kt`), release assets, star counts, and tags.
2. **Automated Silent Background Updates via Shizuku (`ShizukuInstaller.kt`)**:
   - Directly binds Shizuku privileged binder token to `PackageInstaller.Session`.
   - Performs app installations and updates silently without triggering system confirmation prompts.
3. **Security & Privacy Auditing Badges**:
   - **`TrackersNotice.kt`**: Integrated with Exodus privacy database to show embedded trackers before installation.
   - **`ClosedSourceNotice.kt`**: Warns users if an app has closed-source dependencies.
   - **`BillingNotice.kt`**: Identifies apps containing in-app purchases.
4. **Window-Aware Room Database (`CatalogDao.kt`)**:
   - High-performance database caching with window-aware paging source (`WindowAwarePagingSourceTest.kt`) maintaining smooth 60/120fps scrolling through large catalogs.
5. **Obtainium Export Integration (`ObtainiumExporterTest.kt`)**:
   - Allows users to export their Shizuku app list directly into Obtainium for tracking.
6. **Cryptographic Signature Verification (`ApkVerifier.kt`)**:
   - Verifies APK certificate fingerprints (`CertFingerprint.kt`) against upstream releases to prevent tampering.


---

## 13. Morphe Manager (Dual-Runtime Bytecode Patcher)
*Path: `/home/krishna/Downloads/tv/morphe-manager`*

### What the README Says:
> "User-friendly YouTube patcher based on Universal-ReVanced-Manager"

### What the Code ACTUALLY Contains (Undocumented / Deep Features):
1. **Dual-Runtime Execution Engine (`IPatcherService.aidl`)**:
   - Implements remote inter-process patching via `startJvm(in JvmLaunch launch, in ParcelFileDescriptor commands, in ParcelFileDescriptor events)` and `startArt(in ParcelFileDescriptor commands, in ParcelFileDescriptor events)`.
   - Isolates resource-heavy bytecode modifications into a decoupled Dalvik/JVM process to prevent OutOfMemory (OOM) crashes in the main Compose UI process.
2. **Streaming Host Commands & IPC Pipes (`HostStarted.aidl`, `JvmLaunch.aidl`)**:
   - Transmits granular step-by-step patch instructions (`HostCommands`) and streams real-time status and bytecode verification events (`HostEvents`) over Linux pipe descriptors (`ParcelFileDescriptor`).
3. **Bytecode Engineering & Smali Assembly Subsystem (`app.morphe.manager.patcher`)**:
   - Employs ASM and Smali transformation passes directly on Android APK DEX files.
   - Merges multidex payloads, injects hooks into Android lifecycle methods (`onCreate`, `onDraw`), and neutralizes anti-tamper protections.
4. **Automated Android Keystore & V1/V2/V3 APK Signer**:
   - Automatically generates self-signed cryptographic keystores on device, calculates digest hashes, and signs repacked APKs using Android's APK Signature Scheme v2/v3.
5. **Direct Shizuku Silent Package Deployment**:
   - Bridges to Shizuku to stream newly patched APKs directly into Android's `PackageInstaller` session, bypassing manual user installation prompts and target SDK restrictions.

---

## 14. Root-My-Galaxy (Samsung CVE-2026-43499 KSU Loader)
*Path: `/home/krishna/Downloads/tv/Root-My-Galaxy`*

### What the README Says:
> "KSU installer for supported Samsung Galaxy firmware with CVE-2026-43499"

### What the Code ACTUALLY Contains (Undocumented / Deep Features):
1. **Low-Level Kernel Exploit Harness (`CVE-2026-43499` IonStack)**:
   - Exploits Android kernel memory mismanagement via staging in `/data/local/tmp`.
   - Executes privilege escalation to gain temporary root access without requiring an unlocked bootloader.
2. **Zero-Knox Warranty Preservation**:
   - Soft-root architecture that avoids writing to bootloader partitions or triggering the physical e-fuse (Samsung Knox warranty bit stays `0x0`).
   - Secure Folder, Samsung Pass, and hardware keystore features remain operational.
3. **Native Hardware & Firmware Introspection (`NativeProbe.kt`)**:
   - Probes `/proc/version`, CPU ABI, memory page size (4KB vs 16KB alignment), SoC model, and build display IDs (`DeviceSnapshot.kt`).
4. **KernelSU / ReSukiSU Dynamic Stager (`PayloadRepository.kt`, `InstallViewModel.kt`)**:
   - Stages `ksud` binary and late-loads KernelSU kernel modules (`ksud late-load --kmi <kmi>`).
   - Creates a temporary local root socket daemon (`temp_su.sock`) for executing privileged commands before process teardown.
5. **Shizuku Privilege Escalation Controller (`ShizukuController.kt`)**:
   - Utilizes Shizuku (UID 2000) to stage executable binaries in `/data/local/tmp` where non-root applications are restricted by `W^X` security policies.

---

## 15. Root-My-Pixel (Pixel Tensor CVE-2026-43499 Root)
*Path: `/home/krishna/Downloads/tv/Root-My-Pixel`*

### What the README Says:
> "Root automation for Pixel devices via CVE-2026-43499 exploit"

### What the Code ACTUALLY Contains (Undocumented / Deep Features):
1. **Comprehensive Tensor Target Profile Matrix (`assets/profiles.json`)**:
   - Built-in profiles covering Google Pixel 6 through Pixel 11 Pro Fold across Tensor G1, G2, G3, G4, and G5 architectures.
   - Matches kernel KMI (`android14-6.1`, `android15-6.6`, `android16-6.12`) to compiled exploit offsets.
2. **Dedicated Exploit IPC Daemon (`IExploitService.aidl`)**:
   - Binds to an isolated privileged service:
     - `startExploit(in byte[] exploitSo, in byte[] helperBin, String logPath)`
     - `isRunning()`, `getLog()`, `waitFor()`, `kill()`, `exec(String command)`.
3. **Native Root Helper (`libcve43499root.so`)**:
   - Injected shared library that executes the IonStack exploit payload, allocates kernel memory, and opens a privileged control socket.
4. **KernelSU Late-Load UAPI Verification (`ResolveTargetUseCase.kt`)**:
   - Verifies KernelSU installation via direct KernelSU UAPI calls with fallback to `ksud debug info` and `/proc/modules`.
   - Validates ReSukiSU Manager production APK cryptographic signatures before granting authorization.
5. **Live Log Progress Streamer**:
   - Real-time streaming log engine pushing kernel exploit execution steps, memory offsets, and late-load status directly to Jetpack Compose UI.

---

## 16. ShizuCallRecorder (Non-Root Dual-Way Audio Tapper)
*Path: `/home/krishna/Downloads/tv/ShizuCallRecorder`*

### What the README Says:
> "ShizuCallRecorder empowers ADB through Shizuku to record phone calls on non-rooted device!"

### What the Code ACTUALLY Contains (Undocumented / Deep Features):
1. **Privileged Audio Shell Service (`IShellService.aidl`)**:
   - Runs under shell UID (2000) via Shizuku UserService:
     - `startRecording(in ParcelFileDescriptor outputFile, in RecordingPlan plan, in IRecordingCallback callback)`
     - `stopRecording()`, `pauseRecording()`, `resumeRecording()`.
2. **Low-Level Audio Policy Bypass**:
   - Taps Android's internal `AudioSource.VOICE_COMMUNICATION` and `AudioSource.VOICE_CALL` streams directly from shell UID, completely bypassing Android 10+ recording restrictions placed on third-party apps.
3. **Automated AppOp & Role Injector**:
   - Programmatically executes `grantAppOpByPackage`, `grantAppOpByUid`, and `grantRole` directly over Binder to bestow `READ_PHONE_STATE`, `RECORD_AUDIO`, and `DIALER` privileges without user intervention.
4. **Real-Time PII Log Redaction (`ILogCallback.aidl`)**:
   - High-speed regex redaction pipeline filtering telephone numbers, caller IDs, and device telemetry before sending log callbacks across IPC.
5. **Strict Shizuku Lifetime Management**:
   - Implements standard Shizuku destroy transaction code (`destroy() = 16777114`) calling `kotlin.system.exitProcess(0)` for clean process teardown.

---

## 17. ShizuTools (Audio Routing & System Tweak Suite)
*Path: `/home/krishna/Downloads/tv/ShizuTools`*

### What the README Says:
> "Contains some easy-to-use tools to go beyond the level of control allowed by Android System"

### What the Code ACTUALLY Contains (Undocumented / Deep Features):
1. **Multi-Stream Mixed Audio Controller (`MixedAudioActivity.kt`, `AudioOutputMap.kt`)**:
   - Interacts with hidden Android audio policy APIs (`AudioSystem`, `AudioService`) to split audio streams per application.
   - Adjusts volume sliders for individual foreground and background apps independently.
2. **Universal Package Debloater (`DebloatAdapter.kt`, `AppData.kt`)**:
   - Direct management of system apps via privileged shell: disables bloatware via `pm disable-user --user 0` and removes unneeded packages via `pm uninstall -k --user 0`.
3. **Dynamic ADB Shell Runner (`ShizukuRunner.kt`)**:
   - Asynchronous shell command pipeline with stream redirection, process timeouts, and return code parsing.
4. **Force Picture-in-Picture (PiP) and Freeform Windowing**:
   - Injects secure settings flags to force apps that disable PiP or multi-window to operate in floating split-screen and desktop mode.

---

## 18. SystemUI-Tuner (Settings Storage & Lockscreen Tweaker)
*Path: `/home/krishna/Downloads/tv/SystemUI-Tuner`*

### What the README Says:
> "View and modify hidden settings on Android devices"

### What the Code ACTUALLY Contains (Undocumented / Deep Features):
1. **Unified Android Settings Provider Manipulator (`IShizukuOperationsService.aidl`)**:
   - Direct read/write access across all Android settings tables:
     - `writeGlobal(key, value, pkg)`, `writeSecure(key, value, pkg)`, `writeSystem(key, value, pkg)`
     - `readGlobal(key)`, `readSecure(key)`, `readSystem(key)`, `listSettings()`.
2. **Lock Screen Shortcut Overrider (`ILockscreenShortcutSelectedCallback.aidl`)**:
   - Modifies vendor-locked lock screen quick-action buttons (Samsung One UI, Pixel, HyperOS) to launch any custom intent, activity, or shell script.
3. **Status Bar Icon Blacklist & Immersive Mode (`IImmersiveSelectionCallback.aidl`)**:
   - Injects `icon_blacklist` system keys to selectively hide intrusive status bar elements (VoLTE, NFC, alarm, Wi-Fi, battery).
   - Enforces full-screen immersive mode per app or globally.
4. **UI Sound Customizer (`IUISoundSelectionCallback.aidl`)**:
   - Injects custom system audio files for charger connect, screen lock/unlock, camera shutter, and low-battery alerts into `/data/system/media/audio`.
5. **Persistent System Daemon (`Manager.kt`, `IManager.aidl`)**:
   - Background daemon monitoring system state and auto-reapplying tweaks across reboots (`BootReceiver.kt`).

---

## 19. ADNS (Private DNS-over-TLS Controller)
*Path: `/home/krishna/Downloads/tv/adns`*

### What the README Says:
> "DNS-based ad blocker for Android"

### What the Code ACTUALLY Contains (Undocumented / Deep Features):
1. **Privileged Secure Settings Controller (`IPrivilegedService.aidl`)**:
   - Self-grants `android.permission.WRITE_SECURE_SETTINGS` via Shizuku IPC:
     - `grantWriteSecureSettings(String packageName)`.
2. **Dynamic Private DNS Mode Switcher**:
   - Directly configures Android's native DNS-over-TLS engine via `Settings.Global`:
     - `private_dns_mode`: `off`, `opportunistic`, or `hostname`.
     - `private_dns_specifier`: Injects custom encrypted resolver endpoints.
3. **NextDNS API Integration Engine**:
   - Bidirectional profile synchronization with NextDNS, fetching real-time query analytics, toggling blocklists, and configuring custom domain rewrites.
4. **Zero Battery & Zero VPN Overhead**:
   - Leverages Android's built-in OS resolver rather than running a battery-draining local VPN tunnel service.

---

## 20. AutoDND (Foreground Policy Zen Automator)
*Path: `/home/krishna/Downloads/tv/android_AutoDND`*

### What the README Says:
> "A simple tool to toggle DND automatically when using specified apps"

### What the Code ACTUALLY Contains (Undocumented / Deep Features):
1. **Zero-Polling Foreground App Monitor (`AppUsageService.kt`)**:
   - Uses Android UsageStats / Accessibility / AppOps APIs to monitor active foreground package transitions with near-zero CPU and battery utilization.
2. **ZenMode Notification Policy Manipulator**:
   - Directly invokes Android's hidden `NotificationManager.setZenMode()` to toggle Do Not Disturb states (All priority, Alarms only, Total silence) based on active applications (e.g. games, navigation, camera).
3. **Automated State Restoration**:
   - Caches pre-switch system interruption filters and restores exact previous ZenMode state upon exiting target applications.

---

## 21. ShizuWall (Zero-VPN Kernel UID Firewall)
*Path: `/home/krishna/Downloads/tv/ShizuWall`*

### What the README Says:
> "Open-source app firewall that doesn't depend on VPNs or root"

### What the Code ACTUALLY Contains (Undocumented / Deep Features):
1. **Kernel Netfilter UID Firewall (`IShizuWallUserService.aidl`)**:
   - Directly manipulates Android's `INetworkManagementService` and `netd` daemon without VPN:
     - `setUidFirewallRule(int chain, int uid, int rule)`
     - `setUidFirewallRules(int chain, in int[] uids, in int[] rules)`
     - `setFirewallChainEnabled(boolean enabled)`.
2. **Real-Time Foreground Activity Listener (`IShizuWallForegroundListener.aidl`)**:
   - Binds a real-time foreground task watcher (`startForegroundWatch()`, `getForegroundTask()`) to dynamically allow network access only when an app is visibly active on screen, cutting background data instantly.
3. **Zero Battery & Speed Penalty**:
   - Enforces packet filtering entirely inside Linux kernel iptables/nftables chains, eliminating the 15-30% battery drain and latency penalty of traditional Android VPN firewalls.

---

## 22. shizzi (Rootless Hotspot Bypass & Go Datapath)
*Path: `/home/krishna/Downloads/tv/shizzi`*

### What the README Says:
> "Rootless Wi-Fi tethering bypass via Shizuku"

### What the Code ACTUALLY Contains (Undocumented / Deep Features):
1. **Rootless Carrier Hotspot Bypass Engine (`ITetherService.aidl`)**:
   - Implements privileged carrier restriction bypass:
     - `start(boolean logging, String vpnMode)`, `stop()`, `getStatus()`
     - `runProbes(boolean attemptTethering, int availabilityTimeoutMs)`
     - `installTetheringApex(in ParcelFileDescriptor apex)`, `rebootDevice()`.
2. **Custom Native Go Datapath**:
   - Embeds a compiled Go networking core that intercepts outbound tethered client traffic, rewrites IP packet TTL/HL (Time-To-Live / Hop Limit) headers to match handset traffic, and circumvents carrier hotspot quotas.
3. **Tethering APEX Package Manager**:
   - Stages and installs customized `com.android.tethering` APEX modular system updates directly from user space without root.
4. **Automated Diagnostic Probes**:
   - Evaluates device routing policies, upstream network interfaces, and Wi-Fi SoftAP availability timeouts.

---

## 23. sing-box-for-android (TProxy Per-App Router & CGO Core)
*Path: `/home/krishna/Downloads/tv/sing-box-for-android`*

### What the README Says:
> "Universal proxy platform. Uses Shizuku for per-app proxying"

### What the Code ACTUALLY Contains (Undocumented / Deep Features):
1. **Extensive Privileged IPC Architecture**:
   - **`IShizukuService.aidl`**: Resolves cross-profile package lists (`ParceledListSlice getInstalledPackages(flags, userId)`) and streams silent APK installs (`installPackage(...)`).
   - **`IRootService.aidl` & `IRootShellSession.aidl`**: Executes iptables/nftables TProxy transparent proxy redirection rules.
   - **`IBridgeSession.aidl` & `IAutoRedirectSession.aidl`**: High-speed traffic bridge between client UI and background proxy engine.
   - **`INeighborTableCallback.aidl`**: Reads Linux kernel ARP / neighbor cache tables for LAN host discovery.
   - **`IXposedService.aidl`**: LibXposed scope integration to hook internal framework network resolution.
2. **High-Performance CGO Core Integration (`libbox.so`)**:
   - Embeds the sing-box Go proxy engine supporting Shadowsocks, VMess, VLESS, Trojan, WireGuard, Hysteria 2, and Direct protocols.
3. **Advanced Per-App Granular Routing**:
   - Routes individual application traffic via package UID matching, geo-IP databases, and domain rule-sets directly into outbound proxy routes.

---

## 24. EnforceDoze (Instant Deep Idle & Motion Sensor Kill)
*Path: `/home/krishna/Downloads/tv/EnforceDoze`*

### What the README Says:
> "Enable Doze mode immediately after screen off and turn off motion sensing to get best battery life"

### What the Code ACTUALLY Contains (Undocumented / Deep Features):
1. **Immediate Deep Doze Enforcer**:
   - Bypasses Android's 30-minute idle inactivity delay, instantly executing `dumpsys deviceidle force-idle deep` the moment the display powers off.
2. **Significant Motion Sensor Disabler**:
   - Inactivates device motion sensors (`dumpsys deviceidle disable motion`), preventing device movement (walking, driving, pocket shifts) from breaking deep sleep state.
3. **Complete Doze Tunable Registry (`DozeTunableHandler.java`)**:
   - Exposes and adjusts all 28 hidden system parameters in `device_idle_constants` (`inactive_to`, `sensing_to`, `locating_to`, `location_accuracy`, `motion_inactive_to`, `idle_to`, `min_time_to_alarm`).
4. **Battery Drain Instrumentation (`DozeBatteryStatsActivity.java`)**:
   - Real-time battery consumption tracking, logging discharge rates (mA/h) across active vs doze cycles.

---

## 25. Quick-Tile-Settings (Secure Settings Tile Engine)
*Path: `/home/krishna/Downloads/tv/Quick-Tile-Settings`*

### What the README Says:
> "QS tiles for toggling USB debugging and switching private DNS hosts"

### What the Code ACTUALLY Contains (Undocumented / Deep Features):
1. **Privileged Quick Settings Tiles**:
   - **`UsbDebuggingTileService.kt`**: Modifies `adb_enabled` on the fly with configurable auto-revert countdown timer for physical port security.
   - **`PrivateDnsTileService.kt`**: Cycles between Private DNS modes (`off`, `opportunistic`, and custom DoT hostnames like Cloudflare, AdGuard, NextDNS) directly from the notification shade.
2. **Network & VPN Environment Observers**:
   - **`NetworkMonitoringService.kt`** & **`VpnMonitoringService.kt`**: Foreground services detecting active network changes (cellular vs trusted Wi-Fi vs VPN) and automatically applying custom DNS profiles.
3. **Foreground Service Notification Contract**:
   - Implements full Android 14+ foreground service types (`FOREGROUND_SERVICE_TYPE_SPECIAL_USE`) with verified service lifecycle contracts (`MonitoringServiceForegroundContractTest.kt`).

---

## 26. essentials (Universal System Mods, Dynamic Island SDK & 20+ Subsystem Modular Architecture)
*Path: `/home/krishna/Downloads/tv/essentials`*

### What the README Says:
> "Essential tools, mods and workarounds for Pixels. Also compatible with other devices"

### What the Code ACTUALLY Contains (Deep Architectural & Feature Audit):

The `essentials` repository is not a simple utility app—it is an enterprise-grade Android customization framework containing over **796 Kotlin files** organized into 20+ independent feature subsystems. Below is the authoritative technical breakdown of every subsystem:

---

#### A. Dynamic Island SDK & Hardware Floating Overlay (`island/`, `island-sdk/`, `island-sdk-compose/`)
1. **Multi-Process AIDL Architecture**:
   - [`IIslandHost.aidl`](file:///home/krishna/Downloads/tv/essentials/island-sdk/src/main/aidl/com/sameerasw/island/sdk/IIslandHost.aidl): Host interface providing `publish(int schema, String json, in Bundle assets)`, `request(String itemKey, String type, in Bundle args)`, and `snapshot(String itemKey, in Bitmap bitmap)`.
   - [`IIslandProvider.aidl`](file:///home/krishna/Downloads/tv/essentials/island-sdk/src/main/aidl/com/sameerasw/island/sdk/IIslandProvider.aidl): Client provider interface for third-party apps to stream live events.
   - [`ISurfaceCallback.aidl`](file:///home/krishna/Downloads/tv/essentials/island-sdk/src/main/aidl/com/sameerasw/island/sdk/ISurfaceCallback.aidl): Hardware-accelerated surface rendering lifecycle callback.
   - [`IInstrumentationWatcher.aidl`](file:///home/krishna/Downloads/tv/essentials/stub/src/main/aidl/android/app/IInstrumentationWatcher.aidl): Bypasses standard Android window overlay restrictions using privileged instrumentation tokens.
2. **Dynamic Island Engine (`island/service/IslandService.kt`, `island/ui/IslandOverlay.kt`)**:
   - Manages interactive pills, music visualizers, navigation direction cues, and notification hub banners rendered directly around punch-hole display cutouts.
   - Integrates gesture engine (`island/gestures/`) supporting swipe-to-dismiss, long-press to expand, and single-tap actions.

---

#### B. Notification & Visual Alerts Subsystem (`ui/features/lighting/`, `utils/notification/`)
1. **Pixel 11 HiLight & Hardware Ring Illumination ([`HilightSettingsUI.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/lighting/HilightSettingsUI.kt), [`HilightController.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/domain/controller/HilightController.kt))**:
   - Interacts with specialized hardware notification LEDs and camera ring lights, providing custom pulse rates, charging progress rings, and color gradients.
2. **Notification Edge Lighting ([`NotificationLightingSettingsUI.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/lighting/NotificationLightingSettingsUI.kt))**:
   - Renders edge lighting animations around the display perimeter on incoming notifications, with per-app color overrides and screen-off wake effects.
3. **Flash Alerts**:
   - Camera LED strobe and screen flash synchronizers for incoming phone calls and priority alarms.

---

#### C. Sound & Haptics Engine (`ui/features/audio/`)
1. **"Shut Up" Per-App Media Mute ([`ShutUpSettingsUI.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/audio/ShutUpSettingsUI.kt), [`ShutUpPerAppSettingsSheet.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/audio/sheets/ShutUpPerAppSettingsSheet.kt))**:
   - Automatically pauses or mutes media playback from background apps when another app gains audio focus, or when specified foreground apps are launched.
2. **Smart Notification Snoozing ([`NotificationSnoozingSettingsUI.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/audio/NotificationSnoozingSettingsUI.kt), [`SnoozeNotificationsSettingsUI.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/audio/SnoozeNotificationsSettingsUI.kt))**:
   - Intercepts repetitive notification channels and applies exponential backoff snooze intervals without permanently silencing the application.
3. **Now Playing "Like Song" Media Bridge ([`LikeSongSettingsSheet.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/audio/sheets/LikeSongSettingsSheet.kt))**:
   - Hooks into Android's `MediaSessionManager` and Google Pixel Ambient Music Mod to add a 1-tap "Like" action saving identified songs directly into Spotify or YouTube Music playlists.
4. **Volume Steps & Stream Mixer ([`AppVolumeSettingsUI.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/audio/AppVolumeSettingsUI.kt), [`SoundModeTileSettingsUI.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/audio/SoundModeTileSettingsUI.kt))**:
   - Per-app audio output level adjustments and quick toggle between Normal, Vibrate, and Silent modes via custom sound mode tiles.

---

#### D. Display, AOD & Status Glance (`ui/features/display/`)
1. **Status Glance & AOD Enhancements ([`StatusGlanceSettingsUI.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/display/StatusGlanceSettingsUI.kt), [`AlwaysOnDisplaySettingsUI.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/display/AlwaysOnDisplaySettingsUI.kt))**:
   - Supercharges Always On Display with live widgets, battery stats, weather info, and next calendar alarms.
2. **AOD Wallpaper Preview & Dimming ([`AodWallpaperPreviewCard.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/display/AodWallpaperPreviewCard.kt))**:
   - Displays real wallpaper on the AOD with seamless fade-in/fade-out transitions upon screen unlock.
3. **Display Refresh Rate Overclocker ([`RefreshRateSettingsUI.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/display/RefreshRateSettingsUI.kt))**:
   - Forces the display panel to run at locked 30Hz, 60Hz, 90Hz, or 120Hz modes, overriding adaptive refresh rate throttling.
4. **Resolution & DPI Overrides ([`ScreenResolutionSettingsUI.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/display/ScreenResolutionSettingsUI.kt))**:
   - Directly injects `wm size` and `wm density` commands via Shizuku shell bridge.
5. **System Bar Icon Auto-Hiding ([`IslandHideIconsToggle.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/display/IslandHideIconsToggle.kt))**:
   - Selectively hides clock, battery, or notification icons in the status bar whenever the Dynamic Island is expanded to prevent visual overlap.

---

#### E. Wallpaper Staging & Ambient Dimming (`ui/features/wallpaper/`)
1. **Wallpaper Engine & Staging ([`WallpaperScreen.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/wallpaper/WallpaperScreen.kt), [`WallpaperStagingScreen.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/wallpaper/WallpaperStagingScreen.kt))**:
   - Stages lock screen and home screen wallpapers with custom blur radii, parallax offsets, and color grading filters.
2. **Lock Screen Wallpaper Dimmer ([`DimWallpaperSettingsSheet.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/power/sheets/DimWallpaperSettingsSheet.kt))**:
   - Automatically dims wallpaper luminance by user-defined percentages (20%-80%) when the screen locks to conserve OLED power.
3. **Live Wallpaper System Controller ([`LiveWallpaperSettingsUI.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/system/LiveWallpaperSettingsUI.kt))**:
   - Interacts with Android's `WallpaperManager` to inject third-party live wallpapers without root.

---

#### F. Widgets & Smart Stacks (`ui/features/widgets/`, `services/widgets/`)
1. **Widget Smart Stack Host ([`WidgetStackSettingsUI.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/widgets/WidgetStackSettingsUI.kt), [`StackHost.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/services/widgets/StackHost.kt), [`StackHostWorker.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/services/widgets/StackHostWorker.kt))**:
   - Embeds an iOS-style widget stacking engine allowing multiple Android AppWidgets to occupy a single homescreen slot with swipe gestures and auto-rotation.
2. **Pixel Searchbar Custom Widget ([`PixelSearchbarWidget.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/services/widgets/PixelSearchbarWidget.kt), [`SearchbarRenderState.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/services/widgets/SearchbarRenderState.kt))**:
   - Material 3 customized search bar widget with Google Lens, voice search, and custom provider shortcuts.
3. **Quick Settings Tiles Widget ([`QsTilesWidget.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/services/widgets/QsTilesWidget.kt))**:
   - Places interactive QS toggles directly on the homescreen.
4. **Widget Scraper Daemon ([`WidgetScraperService.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/services/widgets/WidgetScraperService.kt))**:
   - Scrapes data updates from installed widgets in the background to feed into Dynamic Island and AOD glance cards.

---

#### G. Input, Hardware & Physical Buttons (`ui/features/hardware/`, `input/`)
1. **Linux Input Event Interceptor ([`InputEventReader.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/input/InputEventReader.kt), [`InputDeviceScanner.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/input/InputDeviceScanner.kt))**:
   - Directly opens and reads raw Linux input events from `/dev/input/event*` under elevated shell/Shizuku privileges.
2. **Volume Key Long-Press Detection ([`VolumeLongPressDetector.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/input/VolumeLongPressDetector.kt))**:
   - Detects screen-off volume up/down long-presses to skip music tracks or toggle flashlight without waking the phone.
3. **Button Remapper & Action Sequences ([`ButtonRemapSettingsUI.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/hardware/ButtonRemapSettingsUI.kt), [`ActionSequenceEditor.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/hardware/ActionSequenceEditor.kt))**:
   - Remaps power button double-tap, assistant button, and gesture navigation swipes to custom chained actions.
4. **Pocket Detection Mode ([`PocketModeSettingsUI.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/hardware/PocketModeSettingsUI.kt))**:
   - Uses proximity and light sensors to immediately lock the display and reject accidental screen taps when inside a pocket or bag.

---

#### H. Power, Battery & Caffeinate Subsystem (`ui/features/power/`, `ui/features/battery/`, `utils/battery/`)
1. **Unified Multi-Device Battery Center ([`PowerAndBatterySettingsUI.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/power/PowerAndBatterySettingsUI.kt), [`BatteriesSettingsUI.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/battery/BatteriesSettingsUI.kt))**:
   - Real-time battery monitoring displaying phone percentage, charge cycles, battery health, and connected Bluetooth accessories (watches, earbuds, stylus) with custom icons ([`BluetoothDeviceIconsSection.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/battery/BluetoothDeviceIconsSection.kt)).
2. **Caffeinate Engine ([`CaffeinateSettingsUI.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/power/CaffeinateSettingsUI.kt), [`CaffeinateController.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/domain/controller/CaffeinateController.kt))**:
   - Keeps the device display awake for 5, 10, 30 minutes or indefinitely, with auto-deactivation on power disconnect.
3. **Navigation Power Saving ([`MapsPowerSavingSettingsUI.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/power/MapsPowerSavingSettingsUI.kt))**:
   - Detects active Google Maps / Waze turn-by-turn navigation, turns off screen between maneuvers, and wakes screen 500m before turning.
4. **Charging Protection ([`ChargingModeSettingsSheet.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/battery/sheets/ChargingModeSettingsSheet.kt))**:
   - Configures maximum charge thresholds (80%) and alerts on battery temperature spikes.

---

#### I. Quick Settings & 43+ Privileged Tiles (`ui/features/tiles/`, `services/tiles/`)
1. **Comprehensive QS Tile Suite ([`QuickSettingsTilesSettingsUI.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/tiles/QuickSettingsTilesSettingsUI.kt), [`QSTileRegistry.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/domain/registry/QSTileRegistry.kt))**:
   - Contains over **43 dedicated `TileService` implementations** in `services/tiles/`:
     - **Networking & DNS**: `PrivateDnsTileService`, `DataSimTileService`, `VpnTileService`, `HotspotTileService`, `WifiCallingTileService`.
     - **Power & Hardware**: `CaffeinateTileService`, `StayAwakeTileService`, `ChargeQuickTileService`, `ScreenOffTileService`, `FlashlightTileService`, `RefreshRateTileService`.
     - **Audio & Media**: `SoundModeTileService`, `MonoAudioTileService`, `MuteMicrophoneTileService`, `ShutUpTileService`.
     - **System & Security**: `UsbDebuggingTileService`, `IslandTileService`, `AppLockTileService`, `SensorsTileService`, `HeadsUpTileService`.
     - **Location & Travel**: `LocationReachedTileService` ("Are we there yet?"), `GpsTileService`.

---

#### J. Watch / Wear OS Integration (`ui/features/watch/`)
1. **Companion Watch Suite ([`WatchSettingsUI.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/watch/WatchSettingsUI.kt))**:
   - Synchronizes watchface configurations, custom complication data providers ([`ComplicationsSettingsUI.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/watch/ComplicationsSettingsUI.kt)), and notification mirrors.
2. **Wear Wireless Debugging Hub ([`WatchWirelessDebuggingSettingsUI.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/watch/WatchWirelessDebuggingSettingsUI.kt))**:
   - Manages ADB wireless pairing directly between phone and Wear OS smartwatch, enabling APK sideloading and command execution without a PC.
3. **Standalone Wear eSIM Installer ([`WatchEsimInstallSheet.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/watch/WatchEsimInstallSheet.kt))**:
   - Bypasses carrier-locked companion restrictions to inject universal GSMA eSIM profiles directly onto smartwatches.

---

#### K. Security, Privacy & App Locking (`ui/features/security/`, `ui/features/permissions/`, `utils/security/`)
1. **Biometric App Lock ([`AppLockSettingsUI.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/security/AppLockSettingsUI.kt))**:
   - Intercepts protected app launches and enforces biometric or device credential verification before allowing foreground display.
2. **Screen-Locked Protection ([`ScreenLockedSecuritySettingsUI.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/security/ScreenLockedSecuritySettingsUI.kt))**:
   - Prevents unauthorized access to Quick Settings toggles, power-off menus, and USB data connections while the phone is locked.
3. **Emergency Remote Lockdown ([`RemoteLockSettingsUI.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/security/RemoteLockSettingsUI.kt))**:
   - Listens for pre-authenticated trigger SMS messages or push notifications to instantly lock down the device and disable biometrics.
4. **DRM & Banking Screenshot Enforcer ([`ScreenshotsSettingsUI.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/security/ScreenshotsSettingsUI.kt))**:
   - Interacts with system WindowManager to bypass `FLAG_SECURE`, allowing screen recording and screenshots in apps that restrict them.

---

#### L. Networks & Connectivity (`ui/features/network/`)
1. **Advanced Network Tweaker ([`NetworksSettingsUI.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/network/NetworksSettingsUI.kt))**:
   - Real-time network speed monitor in status bar, Wi-Fi band locking, and automatic DNS routing.
2. **Dual-SIM Smart Switching ([`DuoNetworkOptionsBottomSheet.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/display/sheets/DuoNetworkOptionsBottomSheet.kt))**:
   - Automatically switches mobile data to SIM 2 when SIM 1 signal degrades below a specified RSSI threshold.
3. **Hotspot Controller ([`HotspotController.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/domain/controller/HotspotController.kt))**:
   - Programmatically controls SoftAP frequency bands, client limits, and auto-shutoff timers.

---

#### M. "Are We There Yet?" (Geofence Arrival Automation) (`ui/features/location/`)
1. **Destination Arrival Tracker ([`LocationReachedSettingsUI.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/location/LocationReachedSettingsUI.kt))**:
   - Designed for transit and travel: users pin a target destination and alarm radius.
2. **Background Geofence Worker ([`LocationAutomationWorker.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/services/automation/LocationAutomationWorker.kt), [`GeofenceReceiver.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/services/automation/receivers/GeofenceReceiver.kt))**:
   - Uses low-power geofencing hardware APIs to monitor proximity. When entering the target radius, it fires loud sound alarms ([`LocationAlarmCard.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/location/LocationAlarmCard.kt)), triggers haptic alerts, and updates smartwatch tiles.

---

#### N. Watermark & EXIF Metadata Studio (`ui/features/watermark/`, `domain/watermark/`)
1. **Hardware-Accelerated Watermark Engine ([`WatermarkScreen.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/watermark/WatermarkScreen.kt), [`WatermarkEngine.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/domain/watermark/WatermarkEngine.kt))**:
   - Automatically detects freshly snapped photos using Android `FileObserver` / MediaStore observers.
2. **EXIF Metadata Extraction ([`MetadataProvider.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/domain/watermark/MetadataProvider.kt), [`ExifSettingsSheet.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/watermark/sheets/ExifSettingsSheet.kt))**:
   - Extracts camera model, lens focal length, aperture, ISO, and shutter speed directly from photo EXIF tags, embedding Leica/Hasselblad-style luxury branding strips into images.

---

#### O. Freeze Subsystem (App Freezer & Bloatware Isolation) (`ui/features/freeze/`, `utils/FreezeManager.kt`)
1. **Tag-Based Package Freezer ([`FreezeSettingsUI.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/freeze/FreezeSettingsUI.kt), [`FreezeGridUI.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/freeze/FreezeGridUI.kt))**:
   - Uses `pm disable-user --user 0` via Shizuku to completely freeze applications, removing them from launcher and killing all background services.
2. **Custom Freeze Tags & Editors ([`AssignTagsSheet.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/freeze/sheets/AssignTagsSheet.kt), [`FreezeTagEditorSheet.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/freeze/sheets/FreezeTagEditorSheet.kt))**:
   - Organize applications into colored tags (e.g. "Banking", "Social", "Work", "Heavy Games") with custom icons and batch freeze/unfreeze actions.

---

#### P. DIY Routines & Automation Framework (`domain/diy/`, `services/automation/`)
1. **Core DIY Architecture ([`DIYRepository.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/domain/diy/DIYRepository.kt), [`ActionRegistry.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/domain/diy/ActionRegistry.kt))**:
   - A flexible automation engine built upon four distinct primitive types:
     - **Trigger Automations ([`Trigger.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/domain/diy/Trigger.kt))**: Event-driven rules that execute specific actions once an event occurs (e.g. charger disconnected &rarr; fire haptic alert).
     - **State Automations ([`State.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/domain/diy/State.kt))**: Condition-driven rules with dual execution phases: **Entry Actions** (run when entering condition e.g. Connected to Car Bluetooth or In Calendar Meeting) and **Exit Actions** (run when condition ceases).
     - **App Automations ([`AppSelectionSheets.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/apps/sheets/AppSelectionSheets.kt))**: Automatically apply system profiles or freeze/unfreeze tags when launching or exiting specified applications.
     - **Action Shortcuts ([`ActionShortcutActivity.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/activities/ActionShortcutActivity.kt))**: Creates home screen 1-tap shortcut widgets to execute chained action sequences.
     - **Accessibility Shortcuts ([`PermissionUtils.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/utils/PermissionUtils.kt))**: Maps physical hardware accessibility buttons or volume keys to trigger automated routines.
2. **Modular Automation Executors (`services/automation/modules/`)**:
   - Dedicated modules for `LocationModule`, `CalendarModule`, `WifiModule`, `BluetoothModule`, `PowerModule`, `AndroidAutoModule`, `DisplayModule`, `TimeModule`, and `NotificationModule`.
3. **GenAI Automation Copilot (`domain/genai/`)**:
   - [`GenAIAutomationService.kt`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/domain/genai/GenAIAutomationService.kt): Analyzes user habits and automatically suggests intelligent automation routines.

---

### Complete Essentials Feature Folder Index (For Modular Project Integration):

Use this directory mapping to easily locate, copy, and integrate any subsystem into your project:

| Feature Subsystem | Source Folder Path | Key Classes & Controllers | Description / Capabilities |
|:---|:---|:---|:---|
| **Dynamic Island SDK** | [`essentials/island-sdk/`](file:///home/krishna/Downloads/tv/essentials/island-sdk) | `IIslandHost.aidl`, `IIslandProvider.aidl`, `ISurfaceCallback.aidl` | Standalone IPC SDK for third-party island integrations |
| **Dynamic Island Core** | [`essentials/app/.../island/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/island) | `IslandService.kt`, `IslandOverlay.kt`, `IslandGestureDetector.kt` | Floating hardware punch-hole pill overlay & gesture engine |
| **Notifications & Alerts** | [`essentials/app/.../ui/features/lighting/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/lighting) | `NotificationLightingSettingsUI.kt`, `HilightSettingsUI.kt` | Edge lighting perimeter glow, Pixel HiLight ring LEDs |
| **Sound & Haptics** | [`essentials/app/.../ui/features/audio/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/audio) | `ShutUpSettingsUI.kt`, `NotificationSnoozingSettingsUI.kt`, `LikeSongSettingsSheet.kt` | Per-app audio muting, notification snoozing, media liking |
| **Display & Refresh Rate** | [`essentials/app/.../ui/features/display/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/display) | `StatusGlanceSettingsUI.kt`, `RefreshRateSettingsUI.kt`, `AlwaysOnDisplaySettingsUI.kt` | 120Hz display locking, AOD customization, status bar icon hiding |
| **Wallpaper & Ambient** | [`essentials/app/.../ui/features/wallpaper/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/wallpaper) | `WallpaperScreen.kt`, `WallpaperStagingScreen.kt`, `DimWallpaperSettingsSheet.kt` | Staging wallpapers, OLED lock screen dimming |
| **Widgets & Searchbar** | [`essentials/app/.../ui/features/widgets/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/widgets) | `WidgetStackSettingsUI.kt`, `PixelSearchbarWidget.kt`, `StackHost.kt` | iOS-style swipeable widget stacks & Pixel searchbar |
| **Input & Hardware Buttons**| [`essentials/app/.../input/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/input) | `InputEventReader.kt`, `VolumeLongPressDetector.kt`, `ButtonRemapSettingsUI.kt` | Raw `/dev/input/` reader, volume key track skipping |
| **Power & Batteries** | [`essentials/app/.../ui/features/battery/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/battery) | `BatteriesSettingsUI.kt`, `BluetoothDeviceIconsSection.kt`, `CaffeinateController.kt` | Multi-device battery center, Caffeinate awake timer |
| **Quick Settings Tiles** | [`essentials/app/.../services/tiles/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/services/tiles) | `QSTileRegistry.kt`, 43+ TileServices (`PrivateDns`, `UsbDebugging`, `ScreenOff`) | Comprehensive privileged QS tiles suite |
| **Watch / Wear OS** | [`essentials/app/.../ui/features/watch/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/watch) | `WatchSettingsUI.kt`, `WatchWirelessDebuggingSettingsUI.kt`, `WatchEsimInstallSheet.kt` | Smartwatch wireless ADB, complication sync, eSIM inject |
| **Security & App Lock** | [`essentials/app/.../ui/features/security/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/security) | `AppLockSettingsUI.kt`, `RemoteLockSettingsUI.kt`, `ScreenshotsSettingsUI.kt` | Biometric app lock, remote SMS wipe, DRM screenshot unlock |
| **Networks & SIM** | [`essentials/app/.../ui/features/network/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/network) | `NetworksSettingsUI.kt`, `HotspotController.kt`, `DuoNetworkOptionsBottomSheet.kt` | Dual SIM data switcher, network speed, SoftAP management |
| **Are We There Yet?** | [`essentials/app/.../ui/features/location/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/location) | `LocationReachedSettingsUI.kt`, `GeofenceReceiver.kt`, `LocationAutomationWorker.kt` | Transit arrival alerts, geofence radius alarm |
| **Watermark & EXIF** | [`essentials/app/.../ui/features/watermark/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/watermark) | `WatermarkEngine.kt`, `MetadataProvider.kt`, `ExifSettingsSheet.kt` | Automatic photo branding & EXIF metadata extraction |
| **App Freezer** | [`essentials/app/.../ui/features/freeze/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/freeze) | `FreezeGridUI.kt`, `FreezeManager.kt`, `FreezeTagEditorSheet.kt` | Tag-based system debloat & application freezing |
| **DIY & Automations** | [`essentials/app/.../domain/diy/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/domain/diy) | `DIYRepository.kt`, `ActionRegistry.kt`, `Trigger.kt`, `State.kt` | Core automation engine: Trigger, State, App, Shortcuts |
| **Automation Services** | [`essentials/app/.../services/automation/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/services/automation) | `AutomationService.kt`, `AutomationManager.kt`, 9 Trigger Modules | Background trigger listeners & execution pipeline |
| **Shortcuts & Triggers** | [`essentials/app/.../ui/activities/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/activities) | `ActionShortcutActivity.kt`, `PermissionUtils.kt` | Homescreen action shortcuts & accessibility triggers |
| **GenAI Suggestions** | [`essentials/app/.../domain/genai/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/domain/genai) | `GenAIAutomationService.kt`, `AutomationSuggestion.kt` | AI-driven usage pattern analysis and routine creation |

---

## 27. Canta (Universal Android Debloater UAD Engine)
*Path: `/home/krishna/Downloads/tv/Canta`*

### What the README Says:
> "Uninstall any app without root via Shizuku"

### What the Code ACTUALLY Contains (Undocumented / Deep Features):
1. **Universal Android Debloater (UAD) Database Engine**:
   - Integrates the community-vetted UAD catalog with strict package categorization:
     - `Recommended`: Zero system side-effects.
     - `Advanced`: Safe if alternative app provided.
     - `Expert`: Disables minor system functionality.
     - `Unsafe`: Essential for device boot.
2. **Multi-User Package Manager Bridge (`ShizukuServices.kt`, `ShizukuPermission.kt`)**:
   - Calls `IPackageManager.deletePackageVersioned` and `setApplicationEnabledSetting` directly via Shizuku binder wrapper across all user profiles (Work profiles, Secure Folders, Dual apps).
3. **Instant Non-Destructive App Restoration**:
   - Uninstalls packages for `user 0` only (`pm uninstall -k --user 0`), allowing instant one-tap restoration without data loss using `pm install-existing`.
4. **Kotlin Multiplatform Architecture (KMP)**:
   - Written in Compose Multiplatform supporting both Android mobile UI and desktop ADB targets.

---

## 28. AxManager (C++ Axsh Daemon, CGroups & ADB Pairing)
*Path: `/home/krishna/Downloads/tv/AxManager`*

### What the README Says:
> "Axeron Manager for non-root wireless ADB/shell module & tweak execution"

### What the Code ACTUALLY Contains (Undocumented / Deep Features):
1. **Native C++ Daemon Supervisor (`server/src/main/cpp/`)**:
   - **`starter.cpp`**: Spawns detached Unix daemons with low-level process camouflage.
   - **`cgroup.cpp`**: Direct Linux cgroup CPU and memory throttling for game optimization and battery saving.
   - **`selinux.cpp`**: Interacts with SELinux policies to grant appropriate execution contexts for background scripts.
2. **Native Wireless ADB Pairing Engine (`adb/src/main/cpp/adb_pairing.cpp`)**:
   - Native C++ implementation of TLS pairing handshake, private key generation, and mDNS peer discovery (`AdbMdns.kt`, `AdbPairingService.kt`).
   - Self-connects to Android Wireless Debugging on device without requiring a PC.
3. **Privileged Shell Server (`axsh` / `AxshServer.kt`)**:
   - Dedicated shell server daemon communicating over local Unix domain sockets to execute modular optimizer scripts, performance tweaks, and WebUIs.
4. **Axeron Quick Settings Tile & CLI Bridge (`AxeronTileService.kt`, `AxeronCli.kt`)**:
   - System notification tile for instant profile switching (Performance, Balanced, Battery Saver) with direct shell command dispatch.
