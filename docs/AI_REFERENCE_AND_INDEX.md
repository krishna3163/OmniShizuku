# AI Reference Manual & Workspace Ecosystem Index
> **Authoritative Technical Blueprint & System Prompt Reference for AI Agents, LLMs, and System Engineers**
> Workspace Root: `/home/krishna/Downloads/tv`
> Scope: 28 Android System, Shizuku, Kernel Exploit, Networking, and UI Modding Repositories

---

## 1. Executive AI Operating Directives

When developing, refactoring, or synthesizing code within this workspace, AI agents MUST follow these architectural rules:
1. **Zero Reinvention Policy**: Before writing new Android root/shell/AIDL code, check the cross-reference index below. Every core Android privilege escalation, package manipulation, audio tapping, network firewall, or QS tile capability is already implemented in one of these repositories.
2. **IPC Standard Convention**: All privileged background services managed by Shizuku MUST implement the standard destroy transaction code `void destroy() = 16777114;` (defined as `IBinder.FIRST_CALL_TRANSACTION + 16777215`) to ensure clean process termination via `kotlin.system.exitProcess(0)`.
3. **Hidden API Invocation**: Never use fragile reflection for core Android framework services (`IPackageManager`, `IActivityManager`, `INetworkManagementService`, `AudioService`). Instead, reference the stub interfaces in `InstallerX-Revived/hidden-api`, `Shizuku implementations/Shizuku/server`, or `SystemUI-Tuner/app/src/main/aidl`.
4. **Target SDK & Downgrade Flags**: When engineering package install logic, utilize bitmask flag injection discovered in `InstallWithOptions`: `0x01000000` (`INSTALL_BYPASS_LOW_TARGET_SDK_BLOCK`) and `0x00000080` (`INSTALL_ALLOW_DOWNGRADE`).

---

## 2. Complete 28-Repository Ecosystem Index

| # | Project Identifier | Filesystem Path | Primary Role & Specialization | Tech Stack |
|:---|:---|:---|:---|:---|
| **01** | **Shizuku Base** | [`Shizuku implementations/Shizuku`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku) | Canonical Binder IPC bridge, daemon & user service host | Java, Kotlin, C++ (JNI) |
| **02** | **Shizuku-Next** | [`Shizuku implementations/Shizuku-Next`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next) | Modernized fork (API 36), AdbModuleManager, WebUI terminal bridge | Kotlin, Jetpack Compose |
| **03** | **ShizukuPlus** | [`Shizuku implementations/ShizukuPlus`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/ShizukuPlus) | Enterprise fork, expanded Binder wrappers, multi-user isolation | Kotlin, Java |
| **04** | **Shizako** | [`Shizuku implementations/Shizako`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizako) | Hardware-accelerated OpenGL blur UI, mascot, QS tiles | Kotlin, OpenGL ES |
| **05** | **Xhizuku** | [`Shizuku implementations/Xhizuku`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Xhizuku) | Wear OS / Android TV optimized UI, standalone modules | Kotlin, Compose TV/Wear |
| **06** | **Stellar** | [`Shizuku implementations/Stellar`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Stellar) | Pure Kotlin native PTY engine, TCP socket bridge, startup autostart | Kotlin Native, POSIX C |
| **07** | **Shevery** | [`Shizuku implementations/shevery`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/shevery) | TV automation, Tasker plugin, Device Policy Manager (DPM), Comput AI | Kotlin, Android DPM |
| **08** | **Porter** | [`Shizuku implementations/porter`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/porter) | Darken's Kotlin DSL, `porsh` C++ PTY engine, system probe | Kotlin DSL, C++ |
| **09** | **OmniShizuku** | [`Shizuku implementations/OmniShizuku`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/OmniShizuku) | Master synthesis workspace & unified target project | Gradle Multi-project |
| **10** | **InstallerX-Revived** | [`InstallerX-Revived`](file:///home/krishna/Downloads/tv/InstallerX-Revived) | Universal split installer (APKs/APKM/XAPK), Dalvik process supervisor | Kotlin, Room, Hidden APIs |
| **11** | **InstallWithOptions** | [`InstallWithOptions`](file:///home/krishna/Downloads/tv/InstallWithOptions) | Android PackageInstaller raw bitmask flag injector (downgrade, targetSdk bypass) | Kotlin, AIDL |
| **12** | **ShizuStore** | [`ShizuStore`](file:///home/krishna/Downloads/tv/ShizuStore) | Automated FOSS Shizuku app store, silent installer, Exodus tracker auditor | Kotlin, Compose, Room |
| **13** | **Morphe Manager** | [`morphe-manager`](file:///home/krishna/Downloads/tv/morphe-manager) | Dual-runtime APK bytecode patcher, ASM/Smali recompiler, auto-signing | Kotlin, Java, JVM/ART AIDL |
| **14** | **Root-My-Galaxy** | [`Root-My-Galaxy`](file:///home/krishna/Downloads/tv/Root-My-Galaxy) | Samsung CVE-2026-43499 IonStack soft-root, Knox 0x0 preservation | Kotlin, JNI, C |
| **15** | **Root-My-Pixel** | [`Root-My-Pixel`](file:///home/krishna/Downloads/tv/Root-My-Pixel) | Google Pixel Tensor G1-G5 IonStack root, KernelSU late-load, AIDL runner | Kotlin, AIDL, C |
| **16** | **ShizuCallRecorder** | [`ShizuCallRecorder`](file:///home/krishna/Downloads/tv/ShizuCallRecorder) | Non-root dual-direction phone call audio recorder via shell UID service | Kotlin, AIDL, AudioRecord |
| **17** | **ShizuTools** | [`ShizuTools`](file:///home/krishna/Downloads/tv/ShizuTools) | Per-app audio routing, multi-stream volume mixer, debloater, PiP enforcer | Kotlin, Hidden Audio APIs |
| **18** | **SystemUI-Tuner** | [`SystemUI-Tuner`](file:///home/krishna/Downloads/tv/SystemUI-Tuner) | Global/Secure/System settings manager, status bar icon blacklist, lockscreen mods | Kotlin, AIDL |
| **19** | **ADNS** | [`adns`](file:///home/krishna/Downloads/tv/adns) | Encrypted DNS-over-TLS switcher, NextDNS sync, zero-VPN adblocker | Kotlin, Compose, DoT |
| **20** | **AutoDND** | [`android_AutoDND`](file:///home/krishna/Downloads/tv/android_AutoDND) | Zero-polling foreground app watcher, automatic ZenMode DND switcher | Kotlin, NotificationManager |
| **21** | **ShizuWall** | [`ShizuWall`](file:///home/krishna/Downloads/tv/ShizuWall) | Kernel Netfilter UID firewall via Shizuku UserService, zero VPN overhead | Kotlin, AIDL, Netd |
| **22** | **shizzi** | [`shizzi`](file:///home/krishna/Downloads/tv/shizzi) | Rootless Wi-Fi hotspot bypass, TTL/HL header mangling, Go datapath | Kotlin, Go, AIDL, APEX |
| **23** | **sing-box-for-android** | [`sing-box-for-android`](file:///home/krishna/Downloads/tv/sing-box-for-android) | Universal proxy client, CGO core (`libbox.so`), multi-profile AIDL bridge | Kotlin, Go (CGO), AIDL |
| **24** | **EnforceDoze** | [`EnforceDoze`](file:///home/krishna/Downloads/tv/EnforceDoze) | Immediate deep idle trigger on screen off, motion sensor kill, tunable editor | Java, Kotlin, DeviceIdle |
| **25** | **Quick-Tile-Settings** | [`Quick-Tile-Settings`](file:///home/krishna/Downloads/tv/Quick-Tile-Settings) | QS tiles for USB debugging with countdown timer, Private DNS cyclic switcher | Kotlin, TileService |
| **26** | **essentials** | [`essentials`](file:///home/krishna/Downloads/tv/essentials) | Dynamic Island SDK, display refresh rate overclocking, haptic motor tuning | Kotlin, AIDL, SurfaceControl |
| **27** | **Canta** | [`Canta`](file:///home/krishna/Downloads/tv/Canta) | Universal Android Debloater (UAD) integration, non-destructive app freeze | Kotlin Multiplatform (KMP) |
| **28** | **AxManager** | [`AxManager`](file:///home/krishna/Downloads/tv/AxManager) | Axeron C++ server daemon, cgroup performance limits, wireless ADB pairing | Kotlin, C++ (POSIX), axsh |

---

## 3. Comprehensive AIDL Interface Catalog

This section indexes every custom IPC interface defined across the workspace. Use these definitions to bind services across processes:

### A. Process & Patcher Execution
```aidl
// morphe-manager/app/src/main/aidl/app/morphe/manager/patcher/runtime/IPatcherService.aidl
interface IPatcherService {
    HostStarted startJvm(in JvmLaunch launch, in ParcelFileDescriptor commands, in ParcelFileDescriptor events);
    HostStarted startArt(in ParcelFileDescriptor commands, in ParcelFileDescriptor events);
}
```

### B. Privilege Escalation & Exploit Runners
```aidl
// Root-My-Pixel/app/src/main/aidl/com/alex193a/rootmypixel/shizuku/IExploitService.aidl
interface IExploitService {
    void startExploit(in byte[] exploitSo, in byte[] helperBin, String logPath);
    boolean isRunning();
    String getLog();
    int waitFor();
    void kill();
    String exec(String command);
}
```

### C. Privileged Audio Recording & AppOp Management
```aidl
// ShizuCallRecorder/app/src/main/aidl/com/kitsumed/shizucallrecorder/IShellService.aidl
interface IShellService {
    void setLogCallback(ILogCallback appLoggerCallback, boolean isRedactionEnabled) = 1;
    void startRecording(in ParcelFileDescriptor outputFile, in RecordingPlan plan, in IRecordingCallback callback) = 2;
    void stopRecording() = 3;
    void pauseRecording() = 4;
    void resumeRecording() = 5;
    boolean grantAppOpByPackage(String packageName, String opName, int userProfileId) = 6;
    boolean grantAppOpByUid(int uid, String opName, int userProfileId) = 7;
    boolean grantRole(String packageName, String roleName, int userProfileId) = 8;
    void destroy() = 16777114;
}
```

### D. Kernel UID Firewall Control
```aidl
// ShizuWall/app/src/main/aidl/com/arslan/shizuwall/shizuku/IShizuWallUserService.aidl
interface IShizuWallUserService {
    void destroy() = 16777114;
    String setUidFirewallRule(int chain, int uid, int rule) = 1;
    String[] setUidFirewallRules(int chain, in int[] uids, in int[] rules) = 2;
    String getForegroundTask() = 3;
    void startForegroundWatch(IShizuWallForegroundListener listener) = 4;
    void stopForegroundWatch() = 5;
    String setFirewallChainEnabled(boolean enabled) = 6;
}
```

### E. Rootless Hotspot & Network Routing
```aidl
// shizzi/app/src/main/aidl/dev/shizzi/ITetherService.aidl
interface ITetherService {
    String start(boolean logging, String vpnMode);
    void setLogging(boolean enabled);
    String stop();
    String getStatus();
    String runProbes(boolean attemptTethering, int availabilityTimeoutMs);
    void clearLog();
    String checkCompatibility();
    String installTetheringApex(in ParcelFileDescriptor apex);
    String rebootDevice();
    int getContractVersion();
}
```

### F. System Settings Storage Tweaker
```aidl
// SystemUI-Tuner/app/src/main/aidl/com/zacharee1/systemuituner/IShizukuOperationsService.aidl
interface IShizukuOperationsService {
    boolean writeGlobal(String key, String value, String pkg) = 1;
    boolean writeSecure(String key, String value, String pkg) = 2;
    boolean writeSystem(String key, String value, String pkg) = 3;
    String readGlobal(String key) = 4;
    String readSecure(String key) = 5;
    String readSystem(String key) = 6;
    SavedOption[] listSettings() = 7;
    void destroy() = 16777114;
}
```

### G. Dynamic Island & Surface Hosting SDK
```aidl
// essentials/island-sdk/src/main/aidl/com/sameerasw/island/sdk/IIslandHost.aidl
oneway interface IIslandHost {
    void publish(int schema, String json, in Bundle assets);
    void request(String itemKey, String type, in Bundle args);
    void snapshot(String itemKey, in Bitmap bitmap);
}
```

---

## 4. Native C/C++, JNI & Kernel Exploit Directory

| Subsystem | File Path | Technical Responsibility |
|:---|:---|:---|
| **AxManager Cgroup** | [`AxManager/server/src/main/cpp/cgroup.cpp`](file:///home/krishna/Downloads/tv/AxManager/server/src/main/cpp/cgroup.cpp) | Direct Linux cgroup CPU & memory bandwidth throttling |
| **AxManager SELinux** | [`AxManager/server/src/main/cpp/selinux.cpp`](file:///home/krishna/Downloads/tv/AxManager/server/src/main/cpp/selinux.cpp) | SELinux execution domain transition for shell daemons |
| **AxManager Daemon** | [`AxManager/server/src/main/cpp/starter.cpp`](file:///home/krishna/Downloads/tv/AxManager/server/src/main/cpp/starter.cpp) | Detached Unix daemon launcher and process name masking |
| **AxManager ADB Pairing** | [`AxManager/adb/src/main/cpp/adb_pairing.cpp`](file:///home/krishna/Downloads/tv/AxManager/adb/src/main/cpp/adb_pairing.cpp) | Native TLS handshake and key exchange for wireless ADB |
| **Shizuku ADB Pairing** | [`Shizuku implementations/Shizuku/adb_pairing.cpp`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku/adb_pairing.cpp) | Standalone C++ TLS pairing engine for Android 11+ pairing codes |
| **Porter PTY Engine** | [`Shizuku implementations/porter/porsh`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/porter) | C++ POSIX pseudoterminal master/slave supervisor (`pts.cpp`) |
| **IonStack Exploit Helper** | `Root-My-Pixel/.../libcve43499root.so` | Exploit payload injecting temporary root socket via CVE-2026-43499 |

---

## 5. Architectural Capability Lookup Matrix

| Developer / AI Task | Recommended Repository | Primary Implementation Files | Key API / Command |
|:---|:---|:---|:---|
| **Record phone calls without root** | `ShizuCallRecorder` | [`IShellService.aidl`](file:///home/krishna/Downloads/tv/ShizuCallRecorder/app/src/main/aidl/com/kitsumed/shizucallrecorder/IShellService.aidl) | `AudioSource.VOICE_COMMUNICATION` over UID 2000 |
| **Bypass min target SDK block on Android 14+** | `InstallWithOptions` | [`OptionApplier.kt`](file:///home/krishna/Downloads/tv/InstallWithOptions) | `INSTALL_BYPASS_LOW_TARGET_SDK_BLOCK` (`0x01000000`) |
| **Downgrade installed apps without data loss** | `InstallWithOptions` | [`OptionApplier.kt`](file:///home/krishna/Downloads/tv/InstallWithOptions) | `INSTALL_ALLOW_DOWNGRADE` (`0x00000080`) |
| **Block app internet without VPN** | `ShizuWall` | [`IShizuWallUserService.aidl`](file:///home/krishna/Downloads/tv/ShizuWall/app/src/main/aidl/com/arslan/shizuwall/shizuku/IShizuWallUserService.aidl) | `setUidFirewallRule(chain, uid, rule)` |
| **Bypass carrier hotspot data limits** | `shizzi` | [`ITetherService.aidl`](file:///home/krishna/Downloads/tv/shizzi/app/src/main/aidl/dev/shizzi/ITetherService.aidl) | TTL/HL header mangling + Go datapath |
| **Gain temporary root on Galaxy without Knox trip** | `Root-My-Galaxy` | [`PayloadRepository.kt`](file:///home/krishna/Downloads/tv/Root-My-Galaxy/app/src/main/java/dev/busung/s25uroot/PayloadRepository.kt) | CVE-2026-43499 + `ksud late-load` |
| **Gain temporary root on Pixel (Tensor)** | `Root-My-Pixel` | [`IExploitService.aidl`](file:///home/krishna/Downloads/tv/Root-My-Pixel/app/src/main/aidl/com/alex193a/rootmypixel/shizuku/IExploitService.aidl) | CVE-2026-43499 + KMI matching (`ResolveTargetUseCase`) |
| **Batch debloat system apps safely** | `Canta` | [`ShizukuServices.kt`](file:///home/krishna/Downloads/tv/Canta/composeApp/src/androidMain/kotlin/io/github/samolego/canta/core/shizuku/ShizukuServices.kt) | Universal Android Debloater (UAD) categories |
| **Modify hidden Settings.Global/Secure/System** | `SystemUI-Tuner` | [`IShizukuOperationsService.aidl`](file:///home/krishna/Downloads/tv/SystemUI-Tuner/app/src/main/aidl/com/zacharee1/systemuituner/IShizukuOperationsService.aidl) | `writeGlobal()`, `writeSecure()`, `writeSystem()` |
| **Force deep Doze mode on screen off** | `EnforceDoze` | [`DozeTunableHandler.java`](file:///home/krishna/Downloads/tv/EnforceDoze/app/src/main/java/com/akylas/enforcedoze/DozeTunableHandler.java) | `dumpsys deviceidle force-idle deep` |
| **Host Dynamic Island overlays** | `essentials` | [`IIslandHost.aidl`](file:///home/krishna/Downloads/tv/essentials/island-sdk/src/main/aidl/com/sameerasw/island/sdk/IIslandHost.aidl) | Island SDK IPC + SurfaceControl |
| **Run privileged shell scripts in isolated WebUI** | `Shizuku-Next` | [`ModuleWebUi.kt`](file:///home/krishna/Downloads/tv/Shizuku%20implementations/Shizuku-Next/manager/src/main/java/moe/shizuku/manager/module/ModuleWebUi.kt) | `window.Shizuku.exec(command)` |
| **Decompile / patch APK bytecode in separate process** | `morphe-manager` | [`IPatcherService.aidl`](file:///home/krishna/Downloads/tv/morphe-manager/app/src/main/aidl/app/morphe/manager/patcher/runtime/IPatcherService.aidl) | `startJvm()` / `startArt()` IPC |
| **Automate silent background APK updates** | `ShizuStore` | [`ShizukuInstaller.kt`](file:///home/krishna/Downloads/tv/ShizuStore) | Direct Binder link to `PackageInstaller.Session` |


---

## 6. Essentials Deep-Feature Directory & Modular Code Index
*Source Base: [`/home/krishna/Downloads/tv/essentials`](file:///home/krishna/Downloads/tv/essentials)*

Every major system feature from the `essentials` repository is cataloged below with its source directory and primary controllers:

| Feature Domain | Module Directory | Key Classes & Controllers | Integration Purpose / Use in Project |
|:---|:---|:---|:---|
| **Dynamic Island SDK** | [`island-sdk/`](file:///home/krishna/Downloads/tv/essentials/island-sdk) | `IIslandHost.aidl`, `IIslandProvider.aidl`, `ISurfaceCallback.aidl` | External app IPC interface for floating pill widgets |
| **Dynamic Island Overlay**| [`island/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/island) | `IslandService.kt`, `IslandOverlay.kt`, `IslandGestureDetector.kt` | Screen cutout floating UI, music pills, gesture handlers |
| **Notification & Alerts** | [`ui/features/lighting/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/lighting) | `NotificationLightingSettingsUI.kt`, `HilightSettingsUI.kt`, `HilightController.kt` | Perimeter screen edge lighting & Pixel HiLight LED controller |
| **Sound & Haptics** | [`ui/features/audio/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/audio) | `ShutUpSettingsUI.kt`, `NotificationSnoozingSettingsUI.kt`, `LikeSongSettingsSheet.kt` | Background app audio muting, notification snoozer, media liker |
| **Display & Refresh Rate**| [`ui/features/display/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/display) | `RefreshRateSettingsUI.kt`, `StatusGlanceSettingsUI.kt`, `AlwaysOnDisplaySettingsUI.kt` | 120Hz display locking, custom AOD glance cards, DPI tuning |
| **Wallpaper & Dimming** | [`ui/features/wallpaper/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/wallpaper) | `WallpaperScreen.kt`, `WallpaperStagingScreen.kt`, `DimWallpaperSettingsSheet.kt` | Lockscreen wallpaper staging & automatic AMOLED dimming |
| **Widgets & Smart Stacks**| [`ui/features/widgets/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/widgets) | `WidgetStackSettingsUI.kt`, `PixelSearchbarWidget.kt`, `StackHost.kt` | Multi-widget iOS-style swipeable stack host & Pixel searchbar |
| **Input & Hardware Buttons**| [`input/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/input) | `InputEventReader.kt`, `VolumeLongPressDetector.kt`, `ButtonRemapSettingsUI.kt` | `/dev/input/` reader, volume key track skipper, pocket mode |
| **Power & Battery** | [`ui/features/power/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/power) | `PowerAndBatterySettingsUI.kt`, `BatteriesSettingsUI.kt`, `CaffeinateController.kt` | Multi-device battery hub, Bluetooth battery icons, Caffeinate timer |
| **Quick Settings (43+ Tiles)**| [`services/tiles/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/services/tiles) | `QSTileRegistry.kt`, `PrivateDnsTileService`, `UsbDebuggingTileService` | 43 privileged tile services (networking, power, security) |
| **Watch / Wear OS Suite** | [`ui/features/watch/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/watch) | `WatchSettingsUI.kt`, `WatchWirelessDebuggingSettingsUI.kt`, `WatchEsimInstallSheet.kt` | Smartwatch wireless ADB pairing, complication sync, eSIM inject |
| **Security & Privacy** | [`ui/features/security/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/security) | `AppLockSettingsUI.kt`, `RemoteLockSettingsUI.kt`, `ScreenshotsSettingsUI.kt` | Biometric app lock, remote SMS lock, FLAG_SECURE unlock |
| **Networks & Connectivity**| [`ui/features/network/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/network) | `NetworksSettingsUI.kt`, `HotspotController.kt`, `DuoNetworkOptionsBottomSheet.kt` | Dual SIM smart data switching, network speed meter, SoftAP |
| **Are We There Yet?** | [`ui/features/location/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/location) | `LocationReachedSettingsUI.kt`, `GeofenceReceiver.kt`, `LocationAutomationWorker.kt` | Destination proximity tracker & transit arrival alarm |
| **Watermark & EXIF Studio**| [`ui/features/watermark/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/watermark) | `WatermarkEngine.kt`, `MetadataProvider.kt`, `ExifSettingsSheet.kt` | Automatic photo branding, camera parameters, EXIF stamp |
| **App Freezer Subsystem** | [`ui/features/freeze/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/features/freeze) | `FreezeGridUI.kt`, `FreezeManager.kt`, `FreezeTagEditorSheet.kt` | Tag-based app freezing (`pm disable-user --user 0`) & isolation |
| **DIY Automation Engine** | [`domain/diy/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/domain/diy) | `DIYRepository.kt`, `ActionRegistry.kt`, `Trigger.kt`, `State.kt` | Core automation engine: Trigger, State, App, Shortcuts |
| **Trigger Modules** | [`services/automation/modules/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/services/automation/modules) | `LocationModule`, `WifiModule`, `BluetoothModule`, `PowerModule`, `NotificationModule` | Event-driven background trigger listeners |
| **Action & State Shortcuts**| [`ui/activities/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/ui/activities) | `ActionShortcutActivity.kt`, `PermissionUtils.kt` | Homescreen 1-tap macro shortcuts & accessibility button triggers |
| **GenAI Routine Copilot** | [`domain/genai/`](file:///home/krishna/Downloads/tv/essentials/app/src/main/java/com/sameerasw/essentials/domain/genai) | `GenAIAutomationService.kt`, `AutomationSuggestion.kt` | AI usage habit analyzer and automated routine generator |
