<div align="center">

<img src="manager/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" width="128" height="128" alt="OmniShizuku Logo">

# OmniShizuku

OmniShizuku 是一款專為 Shizuku、ADB 與 Dhizuku 工作流程設計的開源 Android 實用工具。它透過 Shizuku 服務提供系統級別的 API 存取權限，並整合裝置管理、自動化流程及效能最佳化等強大模組。
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

## 功能特色

### 核心必備工具箱 (Essentials Hub)
- **動態島 (Dynamic Island)** — 浮動挖孔藥丸視窗，呈現即時媒體、電量及系統通知
- **智慧像素 (Smart Pixels - AMOLED 省電)** — 關閉 25–75% 的 OLED 子像素點陣列，節省 15–44% 螢幕功耗
- **螢幕微調器 (Display Tweaker)** — 強制覆寫螢幕解析度、螢幕密度 (DPI) 與更新率
- **強制深色模式 (Force Dark Mode)** — 強制所有已安裝的應用程式啟用深色主題
- **電池中心與防護 (Battery Center & Guard)** — 即時檢測電池健康狀態、循環次數、溫度及延長壽命守護
- **防誤觸口袋模式 (Pocket Mode Guard)** — 利用距離與環境光感應器防止放在口袋時產生誤觸
- **螢幕安全與 DRM 解鎖 (Screen Security & DRM Unlock)** — 繞過 FLAG_SECURE 限制，允許在銀行與串流應用中擷取螢幕畫面
- **權限與 AppOps 控制 (Permissions & AppOps)** — 特權權限控制器與後台背景服務限制
- **SystemUI 調整器 (SystemUI Tuner)** — 狀態列圖示黑名單、電池圖示自訂樣式及沉浸全螢幕模式
- **Axeron 遊戲與 CPU 最佳化 (Axeron Gaming & CPU Optimizer)** — 效能設定檔、動畫零延遲與快取清理
- **強制打盹 (Enforce Doze)** — 螢幕關閉時立即強制執行深度待機休眠自動化
- **進階電源控制 (Advanced Power Controls)** — 透過 Shell 快速實現重新開機、Recovery、Bootloader、關機與休眠
- **私有 DNS 控制器 (Private DNS Controller)** — 免 VPN 加密 DNS 預設組態（Cloudflare、AdGuard、Quad9、Google）
- **電信商熱點限制突破 (Carrier Tethering Bypass)** — 解鎖電信業者對行動熱點分享的限制
- **已儲存 Wi-Fi 密碼檢視 (Saved Wi-Fi Passwords)** — 讀取並顯示系統中已儲存的 Wi-Fi 熱點與金鑰
- **Shut Up! 媒體靜音 (Shut Up! Media Muter)** — 針對特定嘈雜應用程式獨立靜音媒體音訊
- **獨立音訊流控制 (Audio Stream Master)** — 各聲道音量獨立分離調控
- **音量鍵切歌 (Volume Key Track Skipper)** — 螢幕關閉時使用音量鍵快速切換音樂曲目
- **通知邊緣跑馬燈 (Notification Edge Lighting)** — 收到通知時螢幕邊緣展現霓虹光暈呼吸效果
- **自動禪定模式 (Auto DND - Zen Mode)** — 智慧自動化勿擾排程
- **螢幕常亮 (Caffeinate)** — 防止螢幕自動休眠，附帶常駐通知與快速設定面板圖塊
- **照片浮水印與 EXIF 工坊 (Photo Watermark & EXIF Studio)** — 經典徠卡框與哈蘇極簡風格框，附帶即時相機 EXIF 資訊條
- **通用 Android 瘦身工具 (Universal Android Debloater - UAD)** — 具備社群安全評級的套件卸載與一鍵復原
- **應用程式凍結器 (App Freezer)** — 無需解除安裝即可瞬間凍結背景執行應用程式
- **特權檔案瀏覽器 (Privileged File Explorer)** — 瀏覽受保護的系統內部目錄與路徑
- **專業版套件安裝器 (Package Installer Pro)** — 支援安裝各類分割 APK（Split APKs、APKS、XAPK）

### 額外整合模組 (Additional Integrated Modules)
- **主題補丁器 (Theme Patcher)** — 系統主題外觀自訂
- **音訊大師 (Sound Master)** — 進階音訊音效細緻控制
- **混合音訊 (Mixed Audio)** — 多聲道音訊混合與路由
- **回溯錄影 (LookBack)** — 螢幕畫面快速錄製與播放
- **全域子母畫面 (Universal PiP)** — 讓任何應用程式強制支援子母畫面 (PiP)
- **Intent Shell** — 透過標準 Intent 意圖廣播發送執行 Shell 指令
- **本地終端機 (Local Shell)** — 內建即開即用的終端機模擬器

### 無線 ADB 自動 PIN 碼 (協助工具)
- **關閉 (Off)** — 手動輸入配對驗證碼 PIN
- **手動 (Manual)** — 出現配對視窗時由應用程式通知提示
- **自動 (Automatic)** — 協助工具服務自動將儲存的 PIN 輸入至「與裝置配對」對話方塊
- 嚴格僅限主動選擇加入 (Opt-in) — 在設定中啟用之前完全不會在背景執行

### 效能與穩定度
- **延遲動態模糊 (Lazy motion blur)** — 畫面取樣迴圈僅在捲動時運作，避免每幀多餘的 CPU 消耗
- **Crashlytics 與未擷取例外處理** — 完整記錄應用程式崩潰記錄，取代無預警閃退
- **防禦型 Shizuku Binder 包裝** — Null-safe Binder 呼叫機制，防止在 Realme 及各 OEM 客製系統上發生 NPE 崩潰
- **DeviceInfo 逾時保護載入** — dumpsys 查詢設定 20 秒逾時保護，顯示部分已讀取資料避免無限卡頓

### 首次啟動導覽精靈 (First-Launch Onboarding)
- 6 重風險免責聲明頁面（特權存取、裝置擁有人限制、Root 風險、第三方應用行為、開源授權條款、網銀風控警示）
- 5 秒倒數計時閱讀機制與「我已閱讀並同意」核取方塊
- 多語言切換（跟隨系統 + 12 種語言）
- 個性化主題（跟隨系統 / 深色 / 淺色 / AMOLED 純黑 / 壁紙動態取色）
- 啟動方式指引（無線除錯 / Root 授權 / 電腦連接 ADB）

---

## 與其他 Shizuku 分支版本對比

| 專案 | 自動啟動 | TCP 模式 | Dhizuku | 模組支援 | 隱身防偵測 | 終端機 | AI 整合 | Expressive UI | 導覽精靈 | 開源授權 |
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

## 系統需求

- Android 5.0 (API 21) 或更高版本
- 運作中的 Shizuku 服務（透過無線除錯、Root 或電腦 ADB 啟動）
- 已授予 OmniShizuku 完整的 Shizuku 權限

---

## 安裝指南

1. 從 [GitHub Releases](https://github.com/krishna3163/OmniShizuku/releases) 下載並安裝最新的 APK
2. 在您的裝置上啟用 Shizuku（無線除錯或電腦 ADB）
3. 授予 OmniShizuku 相應的 Shizuku 存取權限
4. 首次啟動時，跟隨螢幕說明完成初始導覽精靈

---

## 從原始碼編譯
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

編譯完成的 Debug APK 位於：`manager/build/outputs/apk/debug/manager-debug.apk`。

---

## 貢獻者與致謝

感謝所有在 GitHub 上為 OmniShizuku 做出貢獻的開發者：

- **krishna3163** — 專案維護者與首席開發者 (Maintainer & Lead Developer)

我們由衷感謝以下開源專案，其創新思維、程式碼實作與 API 為 OmniShizuku 帶來了極大的啟發與整合基礎：

- **Shizuku** (rikkaw) — 核心 Shizuku 架構與 Binder API
- **Dhizuku** — Device Owner 特權整合
- **shevery** — Material 3 風格、自動啟動、TCP 模式、模組擴充、終端機與 AI 整合
- **Shizuku Next** — 自動配對、啟動方式選擇、看門狗服務、App-Ops 防火牆管理
- **Essentials (sameerasw)** — 核心工具箱模組（動態島、智慧像素、螢幕常亮等）
- **App Ops** — 細部權限管理架構
- **Active Launcher** — 自動化快捷啟動捷徑
- **EnforceDoze** — Doze 深度休眠自動化機制
- **Xhizuku** — Material 3 Expressive UI、啟動導覽精靈與伺服器狀態監控
- **Stellar** — 極簡終端機實作
- **ShizukuPlus** — 為開發者延伸的 API 支援
- **Porter** — 輕量化 Shizuku 分支實作參考
- **Shizako** — 取代型管理器架構參考
- **DFRoot (diabl0w)** — 整合 DirtyFrag (CVE-2026-43284) 核心提權漏洞利用 (GPL-2.0) 為內建功能

---

## 快速設定面板圖塊 (Quick Settings Tiles)

OmniShizuku 提供以下下拉面板快速圖塊開關：
- 螢幕常亮 (Caffeinate)
- 無線 ADB 開關
- Shizuku 伺服器啟動 / 停止
- 隱身模式 (Stealth Mode) 開關

只要下拉通知面板並輕觸編輯鉛筆圖示，即可將它們新增至快速面板。

---

## 安全聲明

本應用程式透過 Shizuku 請求較高權限的系統級操作，請審慎使用：

- 切勿授權給不可信賴的應用程式
- 無線除錯與 ADB 會對區域網路暴露底層系統介面
- Root 存取將會跳過 Android 原生的沙盒安全防護
- 裝置擁有者 (Device Owner) 模式可能會造成部分系統限制（例如恢復原廠設定受限、部分應用無法解除安裝）

詳細說明請參閱初次啟動時的免責條款。

---

## 開源授權

本專案採用 Apache License 2.0 條款授權。詳情請參閱 [LICENSE](LICENSE)。
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

OmniShizuku 按「現狀」(as-is) 提供，不附帶任何明示或暗示之保證。使用本軟體的風險完全由使用者自行承擔。我們對於資料遺失、原廠保固失效或第三方應用程式異常行為概不負責。
</div>
