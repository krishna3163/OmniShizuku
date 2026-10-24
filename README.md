# OmniShizuku

<div align="center">

[![Stars](https://img.shields.io/github/stars/krishna3163/OmniShizuku?style=for-the-badge&color=yellow)](https://github.com/krishna3163/OmniShizuku/stargazers) [![Forks](https://img.shields.io/github/forks/krishna3163/OmniShizuku?style=for-the-badge&color=orange)](https://github.com/krishna3163/OmniShizuku/network/members) [![Build Android APK](https://img.shields.io/github/actions/workflow/status/krishna3163/OmniShizuku/apk-build.yml?branch=main&style=for-the-badge&label=build)](https://github.com/krishna3163/OmniShizuku/actions/workflows/apk-build.yml)

</div>


[English](README.md) | [Русский](README.ru.md) | [Қазақша](README.kk.md) | [Qazaqşa (Latın)](README.kk-Latn.md) | [Português](README.pt.md) | [Español](README.es.md) | [العربية](README.ar.md) | [简体中文](README.zh-CN.md) | [日本語](README.ja.md)

## Project Overview

> [!IMPORTANT]
> **Migration Action Required:** Due to the application package identifier (`com.krishna.omnishizuku`), you **MUST UNINSTALL** any older official Shizuku Manager app or previous builds from your device before installing OmniShizuku. Otherwise, they will conflict.
> Upstream project references: <https://github.com/RikkaApps/Shizuku> and <https://github.com/HmnDev-Tech/shevery>

## Fork additions

- Jetpack Compose manager UI with Material 3 Expressive components, motion, switches, and rounded icon treatment.
- **Device Owner Management & Integration**: Full Device Owner management (app delegation scopes, ownership transfer, DPM-assisted ADB) inspired by and compatible with Dhizuku.
- **Biometric Security & Protection**: Master switch to require biometric or device PIN/pattern/password authentication with configurable timeouts and granular action protection.
- **Dhizuku Experimental Support**: Dedicated Device-Owner bridging system available inside Laboratory features.
- **Better shell/adb based "Comput"** feature with Gemini Explaination, Macros and Commandium AI Command create.
- Android 16/17 target work with current SDK/build tooling in this fork.
- ADB Modules screen for installing and managing ZIP modules.
- Module features: `module.prop`, banner, enable/disable switch, `action.sh`, policy-gated `service.sh`, local WebUI, delete, path checks, size limits, output limits, and last-run logs.
- Module policy settings: Safe mode, Full access, and background action control.
- Module Catalog with direct install.

## Documentation

- [Shevery Wiki](https://github.com/HmnDev-Tech/shevery/wiki)
- [Comput AI Agent guide](docs/comput-agent.md)
- [Device Owner & Profile Owner guide](docs/device-owner.md)
- [Stealth Edition & Compatibility Stubs](docs/stealth-and-stubs.md)
- [Accessibility Manager, USB Debugging Hiding & Network Firewall](docs/lab-and-extras.md)
- [Tasker & MacroDroid Automation Guide](docs/tasker-plugin.md)
- [Intents Reference](docs/intents.md)
- [ADB Modules guide](docs/adb-modules-guide.md)
- [ADB Modules API reference](docs/adb-modules-api.md)
- [ADB Modules publishing & GitHub Catalog guide](docs/github-catalog.md)
- [Shizuku Connectors API](docs/shizuku-connectors.md)
- [Android 17 Compatibility](docs/android-17-compatibility.md)
- [NightDog Watchdog & Starter Recovery](docs/nightdog.md)

## Background

When developing apps that requires root, the most common method is to run some commands in the su shell. For example, there is an app that uses the `pm enable/disable` command to enable/disable components.

This method has very big disadvantages:

1. **Extremely slow** (Multiple process creation)
2. Needs to process texts (**Super unreliable**)
3. The possibility is limited to available commands
4. Even if ADB has sufficient permissions, the app requires root privileges to run

Shizuku uses a completely different way. See detailed description below.

## User guide & Download

<https://shizuku.rikka.app/>

## Screenshots

<details>
  <summary> Click to open Screenshots</summary>
  <br/>
  <table>
    <tr>
      <td align="center"><img src="screenshots/main.png" width="300" /><br/><b>Main Screen</b></td>
      <td align="center"><img src="screenshots/comput.png" width="300" /><br/><b>Comput Console</b></td>
    </tr>
    <tr>
      <td align="center"><img src="screenshots/modules.png" width="300" /><br/><b>ADB Modules</b></td>
      <td align="center"><img src="screenshots/settings.png" width="300" /><br/><b>Settings</b></td>
    </tr>
  </table>
</details>

## How does Shevery works?

First, we need to talk about how app use system APIs. For example, if the app wants to get installed apps, we all know we should use `PackageManager#getInstalledPackages()`. This is actually an interprocess communication (IPC) process of the app process and system server process, just the Android framework did the inner works for us.

Android uses `binder` to do this type of IPC. `Binder` allows the server-side to learn the uid and pid of the client-side, so that the system server can check if the app has the permission to do the operation.

Usually, if there is a "manager" (e.g., `PackageManager`) for apps to use, there should be a "service" (e.g., `PackageManagerService`) in the system server process. We can simply think if the app holds the `binder` of the "service", it can communicate with the "service". The app process will receive binders of system services on start.

Shizuku guides users to run a process, Shizuku server, with root or ADB first. When the app starts, the `binder` to Shizuku server will also be sent to the app.

The most important feature Shevery provides is something like be a middle man to receive requests from the app, sent them to the system server, and send back the results. You can see the `transactRemote` method in `rikka.shizuku.server.ShizukuService` class, and `moe.shizuku.api.ShizukuBinderWrapper` class for the detail.

So, we reached our goal, to use system APIs with higher permission. And to the app, it is almost identical to the use of system APIs directly.

## Developer guide

### API & sample

https://github.com/RikkaApps/Shizuku-API

### Migrating from pre-v11

> Existing applications still works, of course.

https://github.com/RikkaApps/Shizuku-API#migration-guide-for-existing-applications-use-shizuku-pre-v11

### Attention

1. ADB permissions are limited

   ADB has limited permissions and different on various system versions. You can see permissions granted to ADB [here](https://github.com/aosp-mirror/platform_frameworks_base/blob/master/packages/Shell/AndroidManifest.xml).

   Before calling the API, you can use `ShizukuService#getUid` to check if Shizuku is running user ADB, or use `ShizukuService#checkPermission` to check if the server has sufficient permissions.

2. Hidden API limitation from Android 9

   As of Android 9, the usage of the hidden APIs is limited for normal apps. Please use other methods (such as <https://github.com/LSPosed/AndroidHiddenApiBypass>).

3. Android 8.0 & ADB

   At present, the way Shizuku service gets the app process is to combine `IActivityManager#registerProcessObserver` and `IActivityManager#registerUidObserver` (26+) to ensure that the app process will be sent when the app starts. However, on API 26, ADB lacks permissions to use `registerUidObserver`, so if you need to use Shizuku in a process that might not be started by an Activity, it is recommended to trigger the send binder by starting a transparent activity.

4. Direct use of `transactRemote` requires attention

   * The API may be different under different Android versions, please be sure to check it carefully. Also, the `android.app.IActivityManager` has the aidl form in API 26 and later, and `android.app.IActivityManager$Stub` exists only on API 26.

   * `SystemServiceHelper.getTransactionCode` may not get the correct transaction code, such as `android.content.pm.IPackageManager$Stub.TRANSACTION_getInstalledPackages` does not exist on API 25 and there is `android.content.pm.IPackageManager$Stub.TRANSACTION_getInstalledPackages_47` (this situation has been dealt with, but it is not excluded that there may be other circumstances). This problem is not encountered with the `ShizukuBinderWrapper` method.

## Developing Shizuku itself

### Build

- Clone with `git clone --recurse-submodules`
- Run gradle task `:manager:assembleDebug` or `:manager:assembleRelease`

The `:manager:assembleDebug` task generates a debuggable server. You can attach a debugger to `shizuku_server` to debug the server. Be aware that, in Android Studio, "Run/Debug configurations" - "Always install with package manager" should be checked, so that the server will use the latest code.

### CI builds

Every push to `main`, `dev` and `nightly`, and every pull request, runs the [Build Android APK](https://github.com/krishna3163/OmniShizuku/actions/workflows/apk-build.yml) workflow. Each run uploads both artifacts:

| Artifact | Gradle task | Description |
| --- | --- | --- |
| `omnishizuku-debug` | `:manager:assembleDebug` | Debuggable APK for testing; the server can be debugged with an attached debugger |
| `omnishizuku-release` | `:manager:assembleRelease` | Release APK, signed with keystore or debug signing fallback |

The release artifact is signature-verified with `apksigner` in every run. Fork pull requests build both variants with a generated keystore, so untrusted code never touches release secrets.

## How "Start (via Dhizuku)" Works

1. First, start **OmniShizuku** via PC/OTG or Wireless Debugging.
2. Next, start **Dhizuku**.
   - *Note: Do not start OmniShizuku via Dhizuku first.*

## 📜 License

All original code files in this project are licensed under the [Apache License 2.0](LICENSE).

## 🙏 Special Thanks & Upstream Acknowledgements

OmniShizuku is built upon and inspired by incredible open-source projects across the Android ecosystem. We extend our sincere gratitude and appreciation to all upstream authors and contributors:

* **[RikkaApps/Shizuku](https://github.com/RikkaApps/Shizuku)** - The foundational privileged service architecture, binder IPC protocol, and core APIs created by Rikka.
* **[HmnDev-Tech/shevery](https://github.com/HmnDev-Tech/shevery)** - The upstream project providing rich integration modules and modern capabilities.
* **[iamr0s/Dhizuku](https://github.com/iamr0s/Dhizuku)** & **[rosan/Dhizuku](https://github.com/rosan/Dhizuku)** - Revolutionary Device Owner architecture and API concepts integrated into OmniShizuku.
* **[timschneeb/ShizuStore](https://github.com/timschneeb/ShizuStore)** - Shizuku App Store catalog models and repository synchronization inspiration.
* **[wxxsfxyzm/InstallerX-Revived](https://github.com/wxxsfxyzm/InstallerX-Revived)** - Modern APK installer techniques and package staging mechanisms.
* **[zacharee/InstallWithOptions](https://github.com/zacharee/InstallWithOptions)** - Advanced package installer options and bypass flags.
* **[d4rken/porter](https://github.com/d4rken/porter)** - Clean Android IPC helper primitives.
* **[topjohnwu/libsu](https://github.com/topjohnwu/libsu)** - Root shell service primitives and robust binder transactions.
* **[kerneldroid](https://github.com/kerneldroid)** - App UI components, Catalog Modules System, and Android 17 / compileSdk 37 compatibility.
* **[DP-Hridayan/aShellYou](https://github.com/DP-Hridayan/aShellYou)** - ADB shell command terminal and system tool concepts.
* **[protonpony/Shizuku-Keeper](https://github.com/protonpony/Shizuku-Keeper)** - Autostart on boot via Wireless Debugging daemon architecture.
* **[Landon Moran](https://github.com/LandonMoran)** - Key contributions to Dhizuku bridging, TCP networking, and stability fixes.

---

## 📦 OmniShizuku Modules

You can create and distribute custom ADB modules for OmniShizuku:
1. Create your module with your custom ADB script or binary.
2. Create a GitHub repository for your module.
3. Add the GitHub topic `omnishizuku-modules`.
4. Publish a GitHub Release containing your module archive.

> [!CAUTION]
> **Free App Notice**
> 
> OmniShizuku is a **completely free and open-source project**. Do not pay any third-party app stores, file hosts, or cloud services to download this application.

### CI/CD Builds
Fresh CI builds are generated on every commit at [OmniShizuku Actions](https://github.com/krishna3163/OmniShizuku/actions/workflows/apk-build.yml). Every successful workflow run attaches both `omnishizuku-debug` and `omnishizuku-release` APK artifacts at the bottom of the summary page under **Artifacts**.
