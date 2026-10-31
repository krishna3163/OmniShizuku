<div align="center">

<img src="manager/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" width="128" height="128" alt="OmniShizuku Logo">

# OmniShizuku

OmniShizuku 是一个用于 Shizuku、ADB 和 Dhizuku 工作流的开源 Android 实用工具。它通过 Shizuku 服务提供系统级 API 访问，包含设备管理、自动化、系统优化等强大功能模块。
**A powerful, feature-rich fork of Shizuku with enhanced modules, modern UI, and extended system control capabilities.**

[![GitHub](https://img.shields.io/badge/GitHub-krishna3163%2FOmniShizuku-blue?logo=github&style=for-the-badge)](https://github.com/krishna3163/OmniShizuku)
[![License: Apache-2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=for-the-badge)](LICENSE)
[![Android](https://img.shields.io/badge/Android-5.0%2B-green?logo=android&style=for-the-badge)](https://developer.android.com)
[![API](https://img.shields.io/badge/API%20Level-21%2B-brightgreen?style=for-the-badge)](https://developer.android.com/guide/topics/manifest/uses-sdk-element)
[![Shizuku](https://img.shields.io/badge/Powered%20by-Shizuku%20API-orange?style=for-the-badge)](https://github.com/RikkaApps/Shizuku)

🌐 **Documentation available in:** [English](./README.md) • [हिन्दी](./README.hi.md) • [Español](./README.es.md) • [Français](./README.fr.md) • [中文（简体）](./README.zh.md) • [中文（繁體）](./README.zh_tw.md) • [Русский](./README.ru.md) • [日本語](./README.ja.md) • [العربية](./README.ar.md)

</div>

🌐 **Documentation available in:** [English](./README.md) • [हिन्दी](./README.hi.md) • [Español](./README.es.md) • [Français](./README.fr.md) • [中文（简体）](./README.zh.md) • [中文（繁體）](./README.zh_tw.md) • [Русский](./README.ru.md) • [العربية](./README.ar.md) • [日本語](./README.ja.md) • [Português](./README.pt.md) • [Қазақша](./README.kk.md) • [Qazaqşa](./README.kk-Latn.md)

---

## 功能特性

### 核心必备功能 (Essentials Hub)
- **灵动岛 (Dynamic Island)** — 悬浮打孔药丸弹窗，展示实时媒体、电量与系统通知
- **智能像素 (Smart Pixels - AMOLED 节电)** — 关闭 25–75% 的 OLED 像素点网格，节省 15–44% 屏幕功耗
- **屏幕调节器 (Display Tweaker)** — 强制修改分辨率、屏幕密度 (DPI) 及屏幕刷新率
- **强制深色模式 (Force Dark Mode)** — 强制所有已安装应用使用深色模式主题
- **电池中心与保护 (Battery Center & Guard)** — 实时监测电池健康度、充放电循环次数、温度及电池寿命保护
- **防误触口袋模式 (Pocket Mode Guard)** — 利用接近光传感器防止手机在口袋中意外误触
- **屏幕安全与 DRM 解锁 (Screen Security & DRM Unlock)** — 绕过 FLAG_SECURE 限制，允许在银行与流媒体软件中截屏
- **权限与 AppOps 控制 (Permissions & AppOps)** — 深度特权权限管理与后台服务限制
- **SystemUI 调整器 (SystemUI Tuner)** — 状态栏图标黑名单、电池图标自定义样式及沉浸模式切换
- **Axeron 游戏与 CPU 优化 (Axeron Gaming & CPU Optimizer)** — 性能调度配置、动画零延迟与缓存清理
- **强制打盹 (Enforce Doze)** — 息屏后立即进入深度休眠自动化
- **高级电源控制 (Advanced Power Controls)** — 通过 Shell 实现一键重启、Recovery、Bootloader、关机与休眠
- **私有 DNS 控制器 (Private DNS Controller)** — 免 VPN 加密 DNS 预设（Cloudflare、AdGuard、Quad9、Google）
- **运营商热点限制绕过 (Carrier Tethering Bypass)** — 解锁运营商对移动热点共享的限制
- **已存 Wi-Fi 密码查看 (Saved Wi-Fi Passwords)** — 读取并显示系统已保存的 Wi-Fi 热点及密钥
- **Shut Up! 媒体静音 (Shut Up! Media Muter)** — 针对特定应用单独静音媒体音量
- **独立音频流控制 (Audio Stream Master)** — 各声道音量独立分离调节
- **音量键切歌 (Volume Key Track Skipper)** — 息屏状态下长按音量上下键切换曲目
- **通知边缘跑马灯 (Notification Edge Lighting)** — 接收通知时屏幕边缘发光呼吸特效
- **自动禅定模式 (Auto DND - Zen Mode)** — 智能自动化免打扰排程
- **屏幕常亮 (Caffeinate)** — 防止屏幕自动熄灭，提供常驻通知与快捷设置磁贴
- **照片水印与 EXIF 工坊 (Photo Watermark & EXIF Studio)** — 徕卡经典画框与哈苏极简画框，附带实时 EXIF 参数水印
- **通用安卓瘦身工具 (Universal Android Debloater - UAD)** — 社区安全评级软件包精简及一键恢复
- **应用冻结器 (App Freezer)** — 无需卸载即可即时冻结后台应用
- **特权文件浏览器 (Privileged File Explorer)** — 浏览受保护的系统内部目录
- **安装包专业版 (Package Installer Pro)** — 无缝安装各类分包应用（Split APKs、APKS、XAPK）

### 额外集成模块 (Additional Integrated Modules)
- **主题补丁器 (Theme Patcher)** — 系统主题个性化定制
- **声音大师 (Sound Master)** — 高级系统声音调谐
- **音频混合 (Mixed Audio)** — 混合音频流与声音路由控制
- **回溯录屏 (LookBack)** — 快捷屏幕录制与回放
- **全局画中画 (Universal PiP)** — 让任意应用强制进入画中画模式
- **Intent Shell** — 通过标准 Intent 广播执行 Shell 命令行
- **本地终端 (Local Shell)** — 内置开箱即用的终端模拟器

### 无线 ADB 自动 PIN 码 (无障碍辅助)
- **关闭 (Off)** — 手动输入配对验证码 PIN
- **手动 (Manual)** — 出现配对对话框时由应用发出通知提示
- **自动 (Automatic)** — 无障碍服务自动将存储的 PIN 填入“与设备配对”弹窗
- 严格遵循选择加入机制 — 只有在设置中主动开启后才会运行

### 性能与稳定性
- **惰性动态模糊 (Lazy motion blur)** — 采样循环仅在滑动列表时触发，彻底消除逐帧计算的 CPU 消耗
- **Crashlytics 与异常捕获** — 完整记录异常奔溃堆栈，杜绝静默闪退
- **防御性 Shizuku Binder 封装** — 空安全 Binder 通信保护，有效防止 Realme 及各大 OEM 定制系统的 NPE 崩溃
- **DeviceInfo 限制超时加载** — dumpsys 系统查询添加 20 秒超时机制，展示已读取数据而非一直转圈卡死

### 首次启动引导 (First-Launch Onboarding)
- 6 重风险免责声明页面（系统特权、设备管理员影响、Root 风险、第三方应用行为、开源协议、网银风控提示）
- 5 秒倒计时阅读验证与“我已阅读并同意”协议确认
- 多语言切换（跟随系统 + 12 种国际语言）
- 个性化主题（跟随系统 / 深色 / 浅色 / AMOLED 纯黑 / 壁纸动态色彩取色）
- 激活模式指导（无线调试 / Root 授权 / 电脑 ADB 启动）

---

## 与其他 Shizuku 分支对比

| 项目 | 自启动 | TCP 模式 | Dhizuku | 模块支持 | 隐身防检 | 终端 | AI 集成 | Expressive UI | 引导向导 | 开源协议 |
|---------|-----------|----------|---------|----------------|---------|----------|----------------|---------------|------------|---------|
| **OmniShizuku** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | Apache-2.0 |
| Porter | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | Apache-2.0 |
| shevery | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | Material 3 | ❌ | Apache-2.0 |
| Shizako | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | Apache-2.0 |
| Shizuku (thedjchi 分支) | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | Apache-2.0 |
| Shizuku Next | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | Material 3 | ❌ | Apache-2.0 |
| ShizukuPlus | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | Apache-2.0 |
| Stellar | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | MPL-2.0 |
| Xhizuku | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | Material 3 Expressive | ✅ | Apache-2.0 |

---

## 系统要求

- Android 5.0 (API 21) 或更高版本
- 设备已成功运行 Shizuku 服务（通过无线调试、Root 或电脑 ADB 激活）
- 已授予 OmniShizuku 对应的 Shizuku 权限

---

## 安装指南

1. 从 [GitHub Releases](https://github.com/krishna3163/OmniShizuku/releases) 下载并安装最新版 APK
2. 在设备上启动 Shizuku（无线调试或电脑 ADB）
3. 在弹出的权限请求中为 OmniShizuku 授权 Shizuku 权限
4. 首次启动时，按照屏幕指引完成初始设置向导

---

## 源码编译
## 📖 Table of Contents

- [Overview](#-overview)
- [Why Use OmniShizuku?](#-why-use-omnishizuku)
- [Key Features](#-key-features)
  - [Essentials Hub](#essentials-hub)
  - [Labs - Advanced System Tools](#labs---advanced-system-tools)
  - [Modules System](#modules-system)
  - [Call Recording (ShizuCallRecorder Integration)](#call-recording-shizucallrecorder-integration)
  - [AI & Developer Features](#ai--developer-features)
  - [UI/UX Enhancements](#uiux-enhancements)
- [Feature Comparison](#-feature-comparison-with-other-shizuku-forks)
- [System Requirements](#-system-requirements)
- [Installation](#-installation)
- [Getting Started](#-getting-started)
- [Building from Source](#-building-from-source)
- [Architecture & Security](#-architecture--security)
- [Permissions Explained](#-permissions-explained)
- [Contributing](#-contributing)
- [Credits & Acknowledgments](#-credits--acknowledgments)
- [License](#-license)
- [Support](#-support)

---

## 🔍 Overview

**OmniShizuku** is an extensively enhanced, open-source fork of the original [Shizuku](https://github.com/RikkaApps/Shizuku) project. It extends Shizuku's powerful system-level API access by combining it with a vast collection of practical utilities, system tweaks, privacy controls, and an intuitive modern interface.

Built with **Material Design 3 Expressive**, OmniShizuku empowers users to take full control of their Android device without requiring full root access in most cases - leveraging Shizuku's ADB/root bridging capabilities. From display customization and battery optimization to app management, debloating, call recording, and advanced automation, OmniShizuku is your all-in-one system utility suite.

---

## 💡 Why Use OmniShizuku?

- **All-in-One Solution** - No need for multiple root/ADB apps. Everything from debloating to display tweaks in one place.
- **No Root Required** - Works with Shizuku via Wireless ADB (Android 11+) or ADB, making it accessible to a wider audience.
- **Modern & Intuitive UI** - Clean Material 3 Expressive interface with smooth animations, dynamic theming, and optimized layouts for phones, tablets, TVs and Wear OS.
- **Actively Enhanced** - Regular updates with new features, bug fixes, and improvements beyond the original Shizuku.
- **Privacy-Focused** - Granular control over app permissions, appops, hiding features, and system-level restrictions.
- **Safe & Reversible** - Most changes are reversible, with backup/restore capabilities for debloating and module management.
- **Extensible Module System** - Community-driven modules with built-in catalog, discovery, and installation support.
- **Enterprise-Grade Stability** - Defensive coding practices, crash handling, timeout safeguards, and OEM compatibility fixes.

---

## ✨ Key Features

### Essentials Hub

The Essentials Hub is OmniShizuku's crown jewel - a curated collection of powerful, everyday utilities:

| Feature | Description |
| --- | --- |
| **Dynamic Island** | A floating punch-hole pill overlay inspired by iOS Dynamic Island. Displays live media playback, battery status, alerts, and more with smooth animations. |
| **Smart Pixels (AMOLED Saver)** | Intelligently disables 25%, 50%, or 75% of OLED subpixels to drastically reduce power consumption on AMOLED displays, saving 15-44% battery life. |
| **Display Tweaker** | Override screen resolution, change display density (DPI), adjust refresh rate, and fine-tune display parameters beyond stock limitations. |
| **Force Dark Mode** | Forces dark theme across all applications, even those that don't natively support dark mode. |
| **Battery Center & Guard** | Comprehensive battery monitoring with real-time health data, cycle count estimation, temperature tracking, charging stats, and intelligent longevity protection. |
| **Pocket Mode Guard** | Uses proximity and light sensors to prevent accidental touches when the device is in your pocket, bag, or face-down. |
| **Screen Security & DRM Unlock** | Bypass `FLAG_SECURE` to take screenshots and screen recordings in banking apps, DRM-protected content, and apps that normally block them. |
| **Caffeinate** | Keep your screen awake indefinitely with a persistent notification and Quick Settings tile. Perfect for reading, navigation, or presentations. |
| **Photo Watermark & EXIF Studio** | Add luxurious watermarks with Leica and Hasselblad-style frames. View, edit, and manage EXIF metadata with a beautiful live preview. |
| **Notification Edge Lighting** | Stunning neon edge glow animations that illuminate the screen edges when notifications arrive. Fully customizable colors and effects. |
| **Auto DND (Zen Mode)** | Automate Do Not Disturb based on time schedules, location, events, or custom triggers. |
| **Pocket Mode, Shut Up! & More** | Per-app media muting, volume key track skipping when screen is off, and intelligent audio controls. |

### Labs - Advanced System Tools

Labs houses powerful system-level utilities for advanced users and power users:

| Feature | Description |
| --- | --- |
| **App Ops Manager** | Granular control over app permissions and AppOps. Revoke or grant hidden permissions with precision. |
| **App Freezer** | Instantly freeze background apps to save battery and RAM. Frozen apps won't run until unfrozen. |
| **Universal Android Debloater (UAD)** | Community safety-ranked package debloating with detailed descriptions. Safely remove bloatware with easy restore functionality. |
| **Firewall** | Network-level app firewall to block Wi-Fi and mobile data per app without VPN dependencies. |
| **Autostart Manager** | Control which apps can start automatically on boot, improving boot time and reducing background activity. |
| **Hide Apps/Features** | Hide developer options, USB/Wireless debugging, accessibility services, private DNS, and VPN from other apps/system detection. |
| **Force Dark (Advanced)** | System-wide force dark with additional customization options. |
| **Activity Launcher** | Launch hidden activities, exported components, and internal app activities directly. |
| **Advanced Power Controls** | Reboot to system, recovery, bootloader/fastboot, safe mode, or shutdown directly from the app via privileged shell. |
| **Display Tweaker (Labs)** | Deep display configuration including resolution scaling, DPI changes, and refresh rate forcing. |
| **Saved Wi-Fi Passwords** | View saved Wi-Fi networks and their passwords (where accessible via system APIs). |
| **Privileged File Explorer** | Browse, view, and manage files in protected system directories with shell-level access. |
| **Package Installer Pro** | Install split APKs (APKS), XAPK files, and multiple APK bundles with advanced installation options. |
| **SystemUI Tuner** | Customize status bar icons, battery icon styles, clock position, network indicators, and enable immersive mode tweaks. |
| **Enforce Doze** | Force aggressive Doze mode on screen off for maximum battery savings on compatible devices. |
| **Private DNS Controller** | Quick-switch between encrypted DNS providers (Cloudflare, AdGuard, Quad9, Google, NextDNS, and custom). |
| **Carrier Tethering Bypass** | Bypass carrier restrictions on mobile hotspot/tethering functionality. |
| **Audio Stream Master** | Independent volume control for all audio streams with fine-grained precision. |
| **Network Firewall (Advanced)** | Enhanced firewall controls with persistent rules and per-app data/Wi-Fi restrictions. |
| **Screen Security** | Global control over `FLAG_SECURE` for screenshot/recording permissions. |
| **Auto DND Scheduler** | Time-based and event-based Do Not Disturb automation. |
| **Axeron Gaming & CPU Optimizer** | Performance profiles, animation scaling tweaks, zero-latency optimizations, and cache trimming for gaming. |
| **Terminal Emulator** | Built-in terminal with Shizuku shell access for direct command execution. |
| **Intent Shell** | Execute shell commands via Android Intents for automation and external app integration. |
| **Stealth Mode** | Advanced hiding and stealth capabilities for enhanced privacy. |
| **Permissions Manager** | Centralized privileged permission management. |

### Modules System

OmniShizuku features a powerful, extensible module system inspired by Magisk/KernelSU modules but designed for Shizuku-based mods:

- **Module Catalog** - Browse community modules from a curated GitHub-based catalog with search, filtering, and sorting.
- **Module Discovery** - Automatic discovery of modules from GitHub repositories tagged with `shevery-modules` or official sources.
- **One-Tap Installation** - Download, verify, and install modules directly from the app with progress tracking.
- **Module Manager** - Enable, disable, update, and remove installed modules with ease.
- **WebUI Support** - Many modules include web-based configuration interfaces accessible from within the app.
- **Module Scripts** - Execute install, uninstall, service, and boot scripts with proper logging and error handling.
- **Update Checker** - Automatic update detection for installed modules from their GitHub releases.
- **Safe Installation** - Validation of module structure, props, and scripts before installation.
- **Cross-Platform UI** - Optimized module screens for phones, tablets, TVs, and Wear OS.

### Call Recording (ShizuCallRecorder Integration)

Built-in support for call recording through Shizuku's privileged access:

- **Shell-Based Recording Engine** - Uses privileged shell services for reliable audio capture.
- **Foreground Service** - Persistent recording service with proper notifications.
- **InCallService Integration** - Automatic call detection and recording management.
- **Recording Management** - View, play, rename, and delete recordings with an intuitive UI.
- **Configurable Options** - Customize recording format, codec, source, and storage location.
- **AIDL Interfaces** - Secure IPC between app components for recording control.
- **Permission Handling** - Proper runtime permission management for call recording requirements.
- **Background Operation** - Reliable recording even with app in background.

### AI & Developer Features

- **AI Manager** - Integrated AI capabilities with support for multiple providers (configurable in settings).
- **Developer Options** - Advanced developer tools, debugging utilities, and experimental features.
- **Bug Report System** - Built-in bug reporting with logs, device info, and crash reports.
- **Labs Reapply** - Reapply persistent lab modifications after updates or reboots.
- **Device Info Dashboard** - Comprehensive device information with detailed hardware, software, and system properties.
- **Log Viewer** - Real-time logcat viewer with filtering and search capabilities.

### UI/UX Enhancements

- **Material 3 Expressive Design** - Latest Material Design with dynamic color theming (Material You) support.
- **Multi-Form Factor Support** - Optimized for phones, tablets, foldables, Android TV, and Wear OS.
- **Dark Mode & AMOLED Black** - Full dark theme support with pure black option for AMOLED displays.
- **Smooth Animations** - Polished transitions, lazy motion blur optimization, and fluid interactions.
- **Adaptive Layouts** - Responsive UI that adapts to screen sizes and orientations.
- **Localization** - Extensive multi-language support (English, Hindi, Spanish, French, Chinese, Russian, Japanese, Arabic, Kazakh, and more).
- **Accessibility** - Wireless ADB Auto-PIN with accessibility service support (opt-in only).
- **Performance Optimized** - Lazy loading, bounded data fetching, timeout safeguards, and efficient memory usage.
- **Crash Resilience** - Defensive binder access, uncaught exception handling, and graceful degradation on OEM-specific issues.

---

## 📊 Feature Comparison with Other Shizuku Forks

| Feature | **OmniShizuku** | **Shizuku (Original)** | **Shizuku Next** | **Xhizuku** | **Shevery** | **ShizukuPlus** | **Stellar** |
| --- | :------------: | :--------------------: | :------------: | :--------: | :--------: | :------------: | :--------: |
| **Base Functionality** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Wireless ADB / TCP Mode** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Root Support** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Dhizuku Support** | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ | ❌ |
| **Autostart** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Module System** | ✅ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ |
| **Essentials Hub** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Labs/Advanced Tools** | ✅ | ❌ | Partial | Partial | Partial | ❌ | Partial |
| **App Debloater (UAD)** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **App Freezer** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Firewall** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Display Tweaker** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Dynamic Island** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Smart Pixels** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Call Recording** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Theme Patcher** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Built-in Terminal** | ✅ | ❌ | ✅ | ❌ | ✅ | ❌ | ✅ |
| **AI Integration** | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ |
| **Material 3 Expressive** | ✅ | ❌ | Material 3 | Material 3 | Material 3 | ❌ | ❌ |
| **TV/Wear OS Support** | ✅ | Partial | Partial | Partial | ❌ | ❌ | ❌ |
| **Onboarding Flow** | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| **Stealth Features** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Wireless ADB Auto-PIN** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **License** | Apache-2.0 | Apache-2.0 | Apache-2.0 | Apache-2.0 | Apache-2.0 | Apache-2.0 | MPL-2.0 |

*Comparison is based on publicly available features at the time of writing. Features may change over time.*

---

## 📱 System Requirements

| Component | Requirement |
| --- | --- |
| **Android Version** | Android 5.0 (Lollipop) API 21 or higher |
| **Architecture** | arm64-v8a, armeabi-v7a, x86, x86_64 |
| **Shizuku** | Shizuku service must be running (via Wireless Debugging, ADB over USB, or Root) |
| **Permissions** | Shizuku permission grant required for most features |
| **Storage** | ~50-80 MB for installation (varies by features) |
| **Optional** | Accessibility service for Wireless ADB Auto-PIN feature |

---

## 📥 Installation

### From GitHub Releases (Recommended)

1. Go to the [Releases page](https://github.com/krishna3163/OmniShizuku/releases)
2. Download the latest `manager-debug.apk` or `manager-release.apk`
3. Install the APK on your Android device
4. Follow the setup process

### Via ADB

```bash
adb install -r OmniShizuku.apk
```

### Important Notes

- If you have the original Shizuku installed, you can usually install OmniShizuku alongside it, or replace it. Check compatibility for your use case.
- Some features require specific Android versions or device capabilities.
- For call recording features, additional permissions may be required depending on Android version.

---

## 🚀 Getting Started

1. **Install OmniShizuku** - Download and install the APK
2. **Setup Shizuku** - Start Shizuku service via Wireless Debugging (Android 11+) or ADB/Root
3. **Grant Permission** - Open OmniShizuku and grant Shizuku permission when prompted
4. **Explore** - Navigate through Home, Apps, Modules, Labs, and Settings tabs
5. **Customize** - Head to Essentials Hub or Labs to enable features you need
6. **Stay Safe** - Read feature descriptions before applying system-level changes

**For Wireless Debugging (Android 11+):**
- Go to `Settings > Developer Options > Wireless debugging` and pair with OmniShizuku or Shizuku

**For ADB (via PC):**
```bash
adb shell sh /storage/emulated/0/Android/data/moe.shizuku.privileged.api/start.sh
```
*(Or use the appropriate start command for your setup)*

---

## 🔨 Building from Source

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (latest stable) or [Android SDK](https://developer.android.com/studio#command-tools)
- [JDK 17](https://adoptium.net/) or higher
- [Git](https://git-scm.com/)
- Gradle (wrapper included)

### Clone and Build

```bash
# Clone the repository
git clone https://github.com/krishna3163/OmniShizuku.git
cd OmniShizuku

# Build debug APK
./gradlew :manager:assembleDebug

# Build release APK (requires signing configuration)
./gradlew :manager:assembleRelease

# Install to connected device
./gradlew :manager:installDebug
```

编译生成的调试版 APK 位于：`manager/build/outputs/apk/debug/manager-debug.apk`。

---

## 贡献者与致谢

感谢所有在 GitHub 上为 OmniShizuku 做出贡献的开发者：

- **krishna3163** — 项目维护者与核心开发者 (Maintainer & Lead Developer)

我们同时向以下开源项目致敬，其架构理念、代码实现及 API 为 OmniShizuku 提供了重要灵感与集成基础：

- **Shizuku** (rikkaw) — 核心 Shizuku 框架与 Binder IPC 通信体系
- **Dhizuku** — Device Owner 特权集成
- **shevery** — Material 3 界面风格、自启服务、TCP 模式、模块拓展、终端及 AI 集成
- **Shizuku Next** — 自动配对机制、启动方式选择、服务看门狗、App-Ops 防火墙管理
- **Essentials (sameerasw)** — 核心工具箱模块（灵动岛、智能像素、屏幕常亮等）
- **App Ops** — 特权权限调控模式
- **Active Launcher** — 自动化快捷启动
- **EnforceDoze** — Doze 深度休眠自动化方案
- **Xhizuku** — Material 3 Expressive UI、引导向导与服务端监控
- **Stellar** — 轻量终端实现方案
- **ShizukuPlus** — 针对开发者的拓展 API 支持
- **Porter** — 极简 Shizuku 分支实现参考
- **Shizako** — 模块化管理器替代实现参考
- **DFRoot (diabl0w)** — 作为内置功能的 DirtyFrag (CVE-2026-43284) 内核提权利用程序集成 (GPL-2.0)

---

## 快捷设置磁贴 (Quick Settings Tiles)

OmniShizuku 提供了以下下拉面板快捷开关：
- 屏幕常亮 (Caffeinate)
- 无线 ADB 开关
- Shizuku 服务启动 / 停止
- 隐身模式 (Stealth Mode) 开关

只需下拉通知栏并点击“编辑”铅笔图标即可将它们添加到快捷面板。

---

## 安全声明

本应用通过 Shizuku 请求高特权系统权限，请审慎使用：

- 切勿将高权限随意授予不可信的第三方应用
- 无线调试与 ADB 会向局域网暴露设备系统级接口
- Root 权限将跳过 Android 原生沙盒安全隔离机制
- 设备管理员 (Device Owner) 模式可能会带来副作用（如恢复出厂设置受限、部分应用无法卸载）

详情请参阅首次启动时的风险告知书。

---

## 开源协议

本项目采用 Apache License 2.0 协议开源。详细内容请参阅 [LICENSE](LICENSE)。
The debug APK will be located at: `manager/build/outputs/apk/debug/manager-debug.apk`

### Build Requirements

- `compileSdk`: 36
- `minSdk`: 21
- `targetSdk`: 36
- Kotlin with Jetpack Compose
- R8/ProGuard for release builds

---

## 🔐 Architecture & Security

### Security Measures

- **Defensive Programming** - Null-safe binder access, try-catch blocks around critical operations, and graceful fallbacks
- **Permission Scoping** - Minimal permission usage with runtime permission requests where necessary
- **Secure IPC** - AIDL interfaces for inter-process communication with proper service binding
- **No Telemetry** - No analytics, tracking, or data collection
- **Open Source** - Fully transparent, auditable codebase
- **Reversible Changes** - Most modifications can be undone through the app
- **Validation** - Module validation, package verification, and safe installation practices
- **OEM Compatibility** - Special handling for Realme, OPPO, Xiaomi, OnePlus and other OEMs with aggressive battery optimization

### Architecture Highlights

- **MVVM Pattern** - Clean separation of concerns with ViewModels and UI state
- **Jetpack Compose** - Modern declarative UI framework
- **Kotlin Coroutines** - Asynchronous operations with proper lifecycle handling
- **Room/Preferences** - Local data persistence
- **Hilt/Dagger** - Dependency injection (where applicable)
- **Modular Design** - Feature modules for better organization and maintainability

---

## 🔑 Permissions Explained

| Permission | Purpose | Required For |
| --- | --- | --- |
| `QUERY_ALL_PACKAGES` | List and manage installed apps | App Ops, Debloater, Freezer, Modules |
| `READ_EXTERNAL_STORAGE` / `MANAGE_EXTERNAL_STORAGE` | File access | File Explorer, APK installation |
| `POST_NOTIFICATIONS` | Show notifications | Services, Caffeinate, Recording |
| `FOREGROUND_SERVICE` | Run background services | Call recording, Dynamic Island, Caffeinate |
| `READ_PHONE_STATE`, `RECORD_AUDIO` | Call detection and recording | ShizuCallRecorder |
| `BIND_ACCESSIBILITY_SERVICE` | Auto-fill Wireless ADB PIN | Wireless ADB Auto-PIN (opt-in) |
| `PACKAGE_USAGE_STATS` | Usage data access | Some optimization features |
| `REQUEST_INSTALL_PACKAGES` | Install APKs | Package Installer Pro |
| `SYSTEM_ALERT_WINDOW` | Overlay display | Dynamic Island, Edge Lighting |

*Most permissions are only requested when needed for specific features.*

---

## 🤝 Contributing

Contributions are welcome! Whether it's bug reports, feature requests, documentation improvements, or code contributions.

### How to Contribute

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/AmazingFeature`)
3. **Commit** your changes (`git commit -m 'Add some AmazingFeature'`)
4. **Push** to the branch (`git push origin feature/AmazingFeature`)
5. **Open** a Pull Request

### Guidelines

- Follow existing code style and conventions
- Write clear commit messages
- Test your changes thoroughly
- Update documentation if needed
- For major features, open an issue first to discuss

See [CONTRIBUTING.md](CONTRIBUTING.md) for more details.

---

## 🙏 Credits & Acknowledgments

### Core Projects

- **[RikkaApps/Shizuku](https://github.com/RikkaApps/Shizuku)** - The original project that made all of this possible. Huge thanks to Rikka and all contributors.
- **[Dhizuku](https://github.com/iamr0s/Dhizuku)** - For Dhizuku API integration support.

### Communities & Inspirations

- **[Universal Android Debloater (UAD)](https://github.com/0x192/universal-android-debloater)** - Safety rankings and debloating methodology
- **[LSPosed](https://github.com/LSPosed/LSPosed)** - Inspiration for module system concepts
- **[Magisk](https://github.com/topjohnwu/Magisk)** - Module system architecture inspiration
- **[KernelSU](https://github.com/tiann/KernelSU)** - Modern root solution inspiration

### Module Developers & Integrations

- **[kitsumed/ShizuCallRecorder](https://github.com/kitsumed/ShizuCallRecorder)** - Call recording functionality integration
- **[RBN-Apps/Quick-Tile-Settings](https://github.com/RBN-Apps/Quick-Tile-Settings)** - Companion app integration
- **[AhmetCanArslan/ShizuWall](https://github.com/AhmetCanArslan/ShizuWall)** - Companion app integration
- **[Shevery Modules Community](https://github.com/topics/shevery-modules)** - Community module ecosystem

### Special Thanks

A heartfelt thank you to:

- **The Shizuku Community** - For continuous support, feedback, and testing
- **All Contributors** - Everyone who has reported bugs, suggested features, submitted PRs, or helped improve OmniShizuku
- **Translators** - For making OmniShizuku accessible in multiple languages
- **Beta Testers** - For helping catch bugs and ensuring stability
- **OEM Community** - For sharing device-specific workarounds and fixes

### Why Choose OmniShizuku?

OmniShizuku stands apart by combining the power of Shizuku with a comprehensive suite of practical tools in one beautifully designed app. Instead of juggling multiple apps for debloating, tweaking, optimizing, and managing your device, OmniShizuku brings everything together with a cohesive experience. Whether you're a casual user looking to disable bloatware or a power user seeking deep system customization, OmniShizuku provides the tools you need with safety, reversibility, and ease of use at its core.

---

## 📄 License

This project is licensed under the **Apache License 2.0**. See the [LICENSE](LICENSE) file for full details.

```
Copyright 2024-2026 krishna3163

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

---

## 💬 Support

- **GitHub Issues**: [Report bugs or request features](https://github.com/krishna3163/OmniShizuku/issues)
- **GitHub Discussions**: [Ask questions and share ideas](https://github.com/krishna3163/OmniShizuku/discussions)
- **Documentation**: Check the README in your preferred language
- **Source Code**: [github.com/krishna3163/OmniShizuku](https://github.com/krishna3163/OmniShizuku)

---

<div align="center">

**Made with ❤️ by [krishna3163](https://github.com/krishna3163) and the amazing OmniShizuku community**

*If you find OmniShizuku useful, please consider giving it a ⭐ on GitHub!*

OmniShizuku 按“原样”提供，不提供任何明示或暗示的担保。使用本软件的所有风险由用户自行承担。开发者不对数据丢失、设备保修失效或第三方应用异常行为承担任何法律责任。
</div>
