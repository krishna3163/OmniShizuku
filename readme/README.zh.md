<div align="center">

<img src="../manager/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" width="128" height="128" alt="OmniShizuku Logo">

# 全雫

**功能强大、功能丰富的 Shizuku 分支，具有增强的模块、现代 UI 和扩展的系统控制功能。**

[![GitHub](https://img.shields.io/badge/GitHub-krishna3163%2FOmniShizuku-blue?logo=github&style=for-the-badge)](https://github.com/krishna3163/OmniShizuku)
[![许可证: 阿帕奇-2.0](https://img.shields.io/badge/许可证-Apache%202.0-blue.svg?style=for-the-badge)](../LICENSE)
[![Android](https://img.shields.io/badge/Android-5.0%2B-green?logo=android&style=for-the-badge)](https://developer.android.com)
[![API](https://img.shields.io/badge/API%20Level-21%2B-brightgreen?style=for-the-badge)](https://developer.android.com/guide/topics/manifest/uses-sdk-element)
[![Shizuku](https://img.shields.io/badge/Powered%20by-Shizuku%20API-orange?style=for-the-badge)](https://github.com/RikkaApps/Shizuku)

🌐 **Documentation available in:** [English](../README.md) • [हिन्दी](./README.hi.md) • [Español](./README.es.md) • [Français](./README.fr.md) • [中文（简体）](./README.zh.md) • [中文（繁體）](./README.zh_tw.md) • [Русский](./README.ru.md) • [العربية](./README.ar.md) • [日本語](./README.ja.md) • [Português](./README.pt.md) • [Қазақша](./README.kk.md) • [Qazaqşa](./README.kk-Latn.md)

</div>

---

## 📖 目录

- [概述](#-概述)
- [屏幕截图](#-屏幕截图)
- [为什么使用 OmniShizuku？](#-why-use-omnishizuku)
- [主要特点](#-关键-特点)
  - [实验室 - 高级系统工具（40 个工具）](#labs---advanced-system-tools)
  - [模块系统](#modules-system)
  - [通话录音（Shizu通话记录器 集成）](#call-recording-shizucallrecorder-integration)
  - [人工智能和开发者功能](#ai--developer-features)
  - [UI/UX 增强](#uiux-enhancements)
- [功能比较](#-feature-comparison-with-other-shizuku-forks)
- [系统要求](#-系统要求)
- [安装](#-安装)
- [入门](#-getting-started)
- [从源代码构建](#-building-from-source)
- [架构与安全](#-架构--安全)
- [权限解释](#-permissions-explained)
- [贡献](#-贡献)
- [致谢与致谢](#-致谢--致谢)
- [许可证](#-许可证)
- [支持](#-支持)

---

## 🔍 概述

**全雫** 是原始 [Shizuku](https://github.com/RikkaApps/Shizuku) 项目的一个广泛增强的开源分支。它通过将 Shizuku 强大的系统级 API 访问与大量实用实用程序、系统调整、隐私控制和直观的现代界面相结合，扩展了 Shizuku 强大的系统级 API 访问。

🌐 **Documentation available in:** [English](../README.md) • [हिन्दी](./README.hi.md) • [Español](./README.es.md) • [Français](./README.fr.md) • [中文（简体）](./README.zh.md) • [中文（繁體）](./README.zh_tw.md) • [Русский](./README.ru.md) • [العربية](./README.ar.md) • [日本語](./README.ja.md) • [Português](./README.pt.md) • [Қазақша](./README.kk.md) • [Qazaqşa](./README.kk-Latn.md)
OmniShizuku 采用 **Material Design 3 Expressive** 构建，使用户能够完全控制其 Android 设备，在大多数情况下无需完全 root 访问权限 - 利用 Shizuku 的 ADB/root 桥接功能。从显示定制和电池优化到应用程序管理、消胀、通话录音和高级自动化，OmniShizuku 是您的一体化系统实用套件。

---

## 📸 截图

| **主页和状态** | **应用程序权限** | **亚行模块** | **实验室目录** | **计算（终端和人工智能）** | **设置** |
|:---:|:---:|:---:|:---:|:---:|:---:|
| <img src="../screenshots/home.png" width="165" alt="Home Screen" /> | <img src="../screenshots/manage_apps.png" width="165" alt="Manage Authorized Apps" /> | <img src="../screenshots/modules.png" width="165" alt="ADB Modules" /> | <img src="../screenshots/labs.png" width="165" alt="Labs Catalog" /> | <img src="../screenshots/comput.png" width="165" alt="Comput Console" /> | <img src="../screenshots/settings.png" width="165" alt="Settings Screen" /> |
| 实时服务状态&无线ADB配对 | 白名单模式、精细权限和仅限会话访问 | Magisk/KernelSU 风格的 ZIP 模块安装程序和管理器 | 39+ 强大的系统控制和调整 | 具有 Gemini/AI 生成功能的特权 shell 终端 | 设备所有者、备份、自动化和安全配置 |

---

### 🔬 特色实验室和系统功能

| **系统控制和电动工具** | **本机 ShizuWall 防火墙** |
|:---:|:---:|
| <img src="../screenshots/labs_tools.png" width="260" alt="Labs System Tools" /> | <img src="../screenshots/shizuwall.png" width="260" alt="ShizuWall Firewall" /> |
| 强制打瞌睡、混合音频、私有 DNS、屏幕安全和 DFRoot | 带有跟踪器扫描仪和 appop 规则的零 VPN 本机防火墙 |

---

## 💡 为什么使用 OmniShizuku？

- **一体化解决方案** - 无需多个 root/ADB 应用程序。从膨胀到显示调整的一切都集中在一处。
- **无需 Root** - 通过无线 ADB (Android 11+) 或 ADB 与 Shizuku 配合使用，使其可供更广泛的受众使用。
- **现代且直观的 UI** - 干净的 材料3 富有表现力的界面，具有流畅的动画、动态主题以及针对手机、平板电脑、电视和 Wear OS 的优化布局。
- **主动增强** - 定期更新新功能、错误修复以及原始 Shizuku 之外的改进。
- **注重隐私** - 对应用程序权限、应用程序、隐藏功能和系统级限制进行精细控制。
- **安全且可逆** - 大多数更改都是可逆的，具有用于膨胀和模块管理的备份/恢复功能。
- **可扩展模块系统** - 社区驱动的模块，具有内置目录、发现和安装支持。
- **企业级稳定性** - 防御性编码实践、崩溃处理、超时保护和 OEM 兼容性修复。

---

## ✨ 主要特点

### 实验室 - 高级系统工具

实验室目录包含 40 个系统级工具，按应用程序管理、系统控制和实用程序进行清晰分类：

#### 📱 应用程序管理（8 个工具）
| 特点 | 描述 |
| --- | --- |
| **应用程序操作** | 对隐藏的应用程序权限、运行时操作门和后台权限进行精细控制。 |
| **防火墙** | 网络级应用程序防火墙可阻止每个应用程序的 Wi-Fi 和移动数据，而无需占用 VPN 插槽。 |
| **自动启动** | 限制应用程序在设备启动时自动唤醒或启动服务。 |
| **隐藏模式** | 隐藏开发人员选项、USB/无线调试、辅助服务、私有 DNS 和 VPN 状态，使其免遭检测。 |
| **强制黑暗模式** | 即使没有本机深色主题支持，系统范围内的第三方应用程序也会强制使用深色主题。 |
| **通用去膨胀器 (UAD)** | 社区安全排名的软件包 debloater，具有安全评级、解释和一键恢复功能。 |
| **应用程序冷冻室** | 立即冻结和暂停后台应用程序以回收 RAM 并停止电池消耗。 |
| **Axeron 游戏优化器** | 系统级性能调整、游戏提升、动画调整和流程优化。 |

#### ⚙️ 系统控制（18 个工具）
| 特点 | 描述 |
| --- | --- |
| **电源菜单** | 特权重新启动到系统、恢复、引导加载程序/快速启动、安全模式或即时关机。 |
| **显示调整器** | 覆盖显示分辨率、修改屏幕密度 (DPI) 并强制自定义刷新率。 |
| **Wi-Fi 密码** | 直接通过特权 shell 查看、复制和导出保存的 Wi-Fi 网络凭据。 |
| **系统UI调节器** | 自定义状态栏图标可见性、电池图标样式、时钟秒数和沉浸式导航。 |
| **强制打瞌睡（深度睡眠）** | 每当屏幕关闭时，强制立即进入打瞌睡深度睡眠模式。 |
| **私有 DNS 控制器** | 使用自动恢复计时器在加密 DNS 提供商（Cloudflare、AdGuard、Quad9、Google、NextDNS、Custom）之间切换。 |
| **音频流大师** | 用于媒体、铃声、闹钟、呼叫、语音和系统音频流的独立细粒度音量滑块。 |
| **运营商网络共享** | 绕过运营商的权利检查以及 Wi-Fi 热点和 USB 网络共享的限制。 |
| **混合音频** | 多应用程序并发音频播放旁路（防止后台应用程序在另一个应用程序播放音频时暂停）。 |
| **声音大师** | 每个应用程序的音频焦点和音量路由混合器。 |
| **通用画中画** | 使用虚拟显示投影强制任何不受支持的应用程序进入画中画 (PiP) 模式。 |
| **屏幕安全和 DRM** | 绕过“FLAG_SECURE”限制，允许在安全/银行应用程序中进行屏幕截图和屏幕录制。 |
| **自动免打扰（Zen 模式）** | 基于前台应用程序焦点、自定义计划和触发器的自动请勿打扰规则。 |
| **DFRoot（DirtyFrag 漏洞利用）** | 针对受支持的内核版本使用 CVE-2026-43284 的内置权限提升帮助程序。 |
| **快速设置图块** | 用于 18 个自定义快速设置图块的交互式管理器和设置套件。 |
| **ShizuWall 防火墙** | 先进的防火墙接口具有集成的跟踪器和遥测扫描仪以及套接字监视器。 |
| **咖啡因** | 通过磁贴和持久通知保持设备屏幕处于唤醒状态以进行阅读或导航。 |
| **主题补丁** | 系统级强调色、字体样式和 UI 主题修改。 |

#### 🛠️ 实用程序（14 种工具）
| 特点 | 描述 |
| --- | --- |
| **外壳** | 具有实时流输出的交互式特权命令行终端。 |
| **日志查看器** | 实时系统 logcat 查看器，具有标签、包、级别过滤和导出功能。 |
| **设备信息** | 全面的硬件、内核、电池健康状况、热传感器和 SoC 仪表板。 |
| **活动启动器** | 检查并启动导出的、未导出的或隐藏的 Android 活动和意图。 |
| **文件** | 特权文件系统浏览器，具有对受保护的“/data”和应用程序目录的读/写访问权限。 |
| **应用程序安装专业版** | 支持 APK、APKS 拆分捆绑包和 XAPK 格式的特权包安装程序。 |
| **终端应用程序** | 外部终端仿真器的快速切换器和启动器。 |
| **本地外壳** | 独立的本地 shell 界面。 |
| **意图** | 激发自定义显式或隐式系统意图、广播接收器和服务。 |
| **隐身模式** | 反检测措施，防止目标应用程序检测 Shizuku、root 或调试状态。 |
| **权限管理器** | 概述和撤销/授予所有已安装软件包的提升权限。 |
| **雫店** | 用于发现和下载为 Shizuku 构建的开源应用程序的内置目录。 |
| **通话录音（Shizu通话记录器）** | 由特权 shell 音频捕获支持的本机双向通话录音。 |
| **WallYou壁纸** | 应用内壁纸浏览器、获取器和壁纸切换器。 |

### 模块系统

OmniShizuku 具有强大的、可扩展的模块系统，其灵感来自 Magisk/KernelSU 模块，但专为基于 Shizuku 的 mods 设计：

- **模块目录** - 通过搜索、过滤和排序从基于 GitHub 的精选目录中浏览社区模块。
- **模块发现** - 从 GitHub 存储库自动发现标有“shevery-modules”或官方来源的模块。
- **一键安装** - 直接从应用程序下载、验证和安装模块，并跟踪进度。
- **模块管理器** - 轻松启用、禁用、更新和删除已安装的模块。
- **WebUI 支持** - 许多模块包括可从应用程序内访问的基于 Web 的配置界面。
- **模块脚本** - 通过正确的日志记录和错误处理来执行安装、卸载、服务和启动脚本。
- **更新检查器** - 从 GitHub 版本自动检测已安装模块的更新。
- **安全安装** - 安装前验证模块结构、道具和脚本。
- **跨平台 UI** - 针对手机、平板电脑、电视和 Wear OS 优化的模块屏幕。

### 通话录音（Shizu通话记录器 集成）

通过 Shizuku 的特权访问内置通话录音支持：

- **基于 Shell 的录音引擎** - 使用特权 shell 服务实现可靠的音频捕获。
- **前台服务** - 具有适当通知的持久录制服务。
- **InCallService 集成** - 自动呼叫检测和录音管理。
- **录音管理** - 使用直观的用户界面查看、播放、重命名和删除录音。
- **可配置选项** - 自定义录制格式、编解码器、源和存储位置。
- **AIDL 接口** - 应用程序组件之间的安全 IPC，用于录制控制。
- **权限处理** - 针对通话录音要求的适当的运行时权限管理。
- **后台操作** - 即使应用程序在后台运行，也能进行可靠的录制。

### 人工智能和开发者功能

- **AI 管理器** - 集成 AI 功能，支持多个提供商（可在设置中配置）。
- **开发人员选项** - 高级开发人员工具、调试实用程序和实验功能。
- **错误报告系统** - 内置错误报告，包括日志、设备信息和崩溃报告。
- **实验室重新应用** - 更新或重新启动后重新应用持久实验室修改。
- **设备信息仪表板** - 全面的设备信息，包括详细的硬件、软件和系统属性。
- **日志查看器** - 具有过滤和搜索功能的实时 logcat 查看器。

### 用户界面/用户体验增强

- **材料3 Expressive Design** - 最新的 Material Design，具有动态颜色主题 (Material You) 支持。
- **多外形支持** - 针对手机、平板电脑、可折叠设备、Android TV 和 Wear OS 进行了优化。
- **深色模式和 AMOLED 黑色** - 完全深色主题支持，并为 AMOLED 显示屏提供纯黑色选项。
- **平滑动画** - 精美的过渡、惰性运动模糊优化和流畅的交互。
- **自适应布局** - 响应式 UI，可适应屏幕尺寸和方向。
- **本地化** - 广泛的多语言支持（英语、印地语、西班牙语、法语、中文、俄语、日语、阿拉伯语、哈萨克语等）。
- **辅助功能** - 具有辅助功能服务支持的无线 ADB 自动 PIN（仅限选择加入）。
- **性能优化** - 延迟加载、有界数据获取、超时保护和高效的内存使用。
- **崩溃弹性** - 防御性绑定访问、未捕获的异常处理以及针对 OEM 特定问题的优雅降级。

---

## 📊 与其他 Shizuku 前叉的功能比较

| 特点 | **全雫** | **雫（原版）** | **下一步雫** | **西筑** | **谢弗里** | **雫加** | **恒星** |
| --- | :------------: | :--------------------: | :------------: | :--------: | :--------: | :------------: | :--------: |
| **基本功能** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **无线ADB / TCP模式** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **根支持** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Dhizuku 支持** | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ | ❌ |
| **自动启动** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **模块系统** | ✅ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ |
| **实验室高级工具（40 个工具）** | ✅ | ❌ | 部分 | 部分 | 部分 | ❌ | 部分 |
| **应用程序去膨胀器 (UAD)** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **应用程序冷冻室** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **防火墙（零 VPN）** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **快速设置瓷砖套件（18 个瓷砖）** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **ShizuWall 本机防火墙** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **跟踪器和遥测扫描仪** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **咖啡因屏幕保持清醒** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **私有 DNS 自动恢复和周期** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **DFRoot（内核漏洞利用助手）** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **WallYou动态壁纸** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **通用画中画** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **显示调整器** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **通话录音** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **主题补丁** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **内置终端** | ✅ | ❌ | ✅ | ❌ | ✅ | ❌ | ✅ |
| **人工智能集成** | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ |
| **材料 3 表现力** | ✅ | ❌ | 材料3 | 材料3 | 材料3 | ❌ | ❌ |
| **TV/Wear 操作系统支持** | ✅ | 部分 | 部分 | 部分 | ❌ | ❌ | ❌ |
| **入职流程** | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| **隐形功能** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **无线 ADB 自动 PIN** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **许可证** | 阿帕奇-2.0 | 阿帕奇-2.0 | 阿帕奇-2.0 | 阿帕奇-2.0 | 阿帕奇-2.0 | 阿帕奇-2.0 | MPL-2.0 |

*比较基于撰写本文时公开的功能。功能可能会随着时间的推移而改变。*

---

## 📱 系统要求

| 组件 | 要求 |
| --- | --- |
| **安卓版本** | Android 5.0 (Lollipop) API 21 或更高版本 |
| **架构** | arm64-v8a、armeabi-v7a、x86、x86_64 |
| **雫** | Shizuku 服务必须正在运行（通过无线调试、ADB over USB 或 Root） |
| **权限** | 大多数功能都需要 Shizuku 许可 |
| **存储** | ~50-80 MB 用于安装（因功能而异） |
| **可选** | 无线 ADB 自动 PIN 功能的辅助服务 |

---

## 📥 安装

### 来自 GitHub 版本（推荐）

1. 前往[发布页面](https://github.com/krishna3163/OmniShizuku/releases)
2. 下载最新的`manager-debug.apk`或`manager-release.apk`
3. 在您的 Android 设备上安装 APK
4. 遵循设置过程

### 通过亚行

```bash
adb install -r OmniShizuku.apk
```

### 重要提示

- 如果您安装了原始 Shizuku，通常可以同时安装 OmniShizuku 或替换它。检查您的用例的兼容性。
- 某些功能需要特定的 Android 版本或设备功能。
- 对于通话录音功能，根据 Android 版本，可能需要额外的权限。

---

## 🚀 开始使用

1. **安装 OmniShizuku** - 下载并安装 APK
2. **设置 Shizuku** - 通过无线调试 (Android 11+) 或 ADB/Root 启动 Shizuku 服务
3. **授予权限** - 打开 OmniShizuku 并在出现提示时授予 Shizuku 权限
4. **探索** - 浏览主页、应用程序、模块、实验室和设置选项卡
5. **自定义** - 前往 Essentials Hub 或实验室启用您需要的功能
6. **保持安全** - 在应用系统级更改之前阅读功能描述

**对于无线调试（Android 11+）：**
- 转到“设置 > 开发者选项 > 无线调试”并与 OmniShizuku 或 Shizuku 配对

**对于 ADB（通过 PC）：**
```bash
adb shell sh /storage/emulated/0/Android/data/moe.shizuku.privileged.api/start.sh
```
*（或使用适合您的设置的启动命令）*

---

## 🔨 从源头构建

### 先决条件

- [Android Studio](https://developer.android.com/studio)（最新稳定）或[Android SDK](https://developer.android.com/studio#command-tools)
- [JDK 17](https://adoptium.net/) 或更高版本
- [Git](https://git-scm.com/)
- Gradle（包括包装器）

### 克隆和构建

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

调试 APK 将位于：`manager/build/outputs/apk/debug/manager-debug.apk`

### 构建要求

- `编译SDK`：36
- `minSdk`: 21
- `目标SDK`：36
- Kotlin 与 Jetpack Compose
- R8/ProGuard 用于发布版本

---

## 🔐 架构与安全

### 安全措施

- **防御性编程** - 空安全绑定器访问、围绕关键操作的 try-catch 块以及优雅的回退
- **权限范围** - 必要时使用运行时权限请求的最小权限使用
- **安全 IPC** - AIDL 接口，用于通过正确的服务绑定进行进程间通信
- **无遥测** - 无分析、跟踪或数据收集
- **开源** - 完全透明、可审计的代码库
- **可逆更改** - 大多数修改可以通过应用程序撤消
- **验证** - 模块验证、包验证和安全安装实践
- **OEM 兼容性** - 针对 Realme、OPPO、小米、OnePlus 和其他具有积极电池优化的 OEM 的特殊处理

### 建筑亮点

- **MVVM 模式** - ViewModel 和 UI 状态的关注点完全分离
- **Jetpack Compose** - 现代声明式 UI 框架
- **Kotlin 协程** - 具有适当生命周期处理的异步操作
- **房间/首选项** - 本地数据持久性
- **Hilt/Dagger** - 依赖注入（如果适用）
- **模块化设计** - 功能模块可实现更好的组织和可维护性

---

## 🔑 权限解释

| 许可 | 目的 | 需要用于 |
| --- | --- | --- |
| `QUERY_ALL_PACKAGES` | 列出和管理已安装的应用程序 | 应用程序操作、Debloater、冷冻机、模块 |
| `READ_EXTERNAL_STORAGE` / `MANAGE_EXTERNAL_STORAGE` | 文件访问 | 文件资源管理器、APK 安装 |
| `POST_通知` | 显示通知 | 服务、咖啡因、录音 |
| `前台服务` | 运行后台服务 | 通话录音、ShizuWall、咖啡因 |
| `READ_PHONE_STATE`、`RECORD_AUDIO` | 呼叫检测和录音 | Shizu通话记录器 |
| `BIND_ACCESSIBILITY_SERVICE` | 自动填写无线 ADB PIN | 无线 ADB 自动 PIN（选择加入） |
| `PACKAGE_USAGE_STATS` | 使用数据访问 | 一些优化功能 |
| `请求安装包` | 安装APK | 软件包安装程序专业版 |
| `SYSTEM_ALERT_WINDOW` | 叠加显示 | 通用画中画、屏幕覆盖 |

*大多数权限仅在需要特定功能时才请求。*

---

## 🤝 贡献

欢迎贡献！无论是错误报告、功能请求、文档改进还是代码贡献。

### 如何贡献

1. **分叉**存储库
2. **创建**一个功能分支（`git checkout -b feature/Amazing特点`）
3. **提交**您的更改（`git commit -m 'Add some Amazing特点'`）
4. **推送**到分支（`git push origin feature/Amazing特点`）
5. **打开**拉取请求

### 指南

- 遵循现有的代码风格和约定
- 编写清晰的提交消息
- 彻底测试您的更改
- 如果需要更新文档
- 对于主要功能，先开issue讨论

See [CONTRIBUTING.md](../CONTRIBUTING.md) for more details.

---

## 🙏 致谢与致谢

### 核心项目

- **[RikkaApps/Shizuku](https://github.com/RikkaApps/Shizuku)** - 使这一切成为可能的原始项目。非常感谢 Rikka 和所有贡献者。
- **[Dhizuku](https://github.com/iamr0s/Dhizuku)** - 用于 Dhizuku API 集成支持。

### 社区与灵感

- **[Universal Android Debloater (UAD)](https://github.com/0x192/universal-android-debloater)** - 安全排名和 debloater 方法
- **[LSPosed](https://github.com/LSPosed/LSPosed)** - 模块系统概念的灵感
- **[Magisk](https://github.com/topjohnwu/Magisk)** - 模块系统架构灵感
- **[KernelSU](https://github.com/tiann/KernelSU)** - 现代根解决方案灵感

### 模块开发人员和集成

- **[kitsumed/Shizu通话记录器](https://github.com/kitsumed/Shizu通话记录器)** - 通话录音功能集成
- **[RBN-Apps/Quick-Tile-Settings](https://github.com/RBN-Apps/Quick-Tile-Settings)** - 配套应用程序集成
- **[AhmetCanArslan/ShizuWall](https://github.com/AhmetCanArslan/ShizuWall)** - 配套应用程序集成
- **[Shevery Modules Community](https://github.com/topics/shevery-modules)** - 社区模块生态系统

### 特别感谢

衷心感谢：

- **Shizuku 社区** - 提供持续支持、反馈和测试
- **所有贡献者** - 报告错误、建议功能、提交 PR 或帮助改进 OmniShizuku 的每个人
- **翻译器** - 使 OmniShizuku 能够以多种语言访问
- **Beta 测试人员** - 帮助发现错误并确保稳定性
- **OEM 社区** - 用于分享特定于设备的解决方法和修复

### 为什么选择 OmniShizuku？

OmniShizuku 的独特之处在于将 Shizuku 的强大功能与一套全面的实用工具结合在一个设计精美的应用程序中。 OmniShizuku 无需同时使用多个应用程序来缩减、调整、优化和管理您的设备，而是将所有内容整合在一起，提供具有凝聚力的体验。无论您是希望禁用臃肿软件的临时用户，还是寻求深度系统定制的高级用户，OmniShizuku 都能提供您所需的工具，并以安全性、可逆性和易用性为核心。

---

## 📄 许可证

This project is licensed under the **Apache 许可证 2.0**. See the [LICENSE](../LICENSE) file for full details.

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

## 💬 支持

- **GitHub 问题**：[报告错误或请求功能](https://github.com/krishna3163/OmniShizuku/issues)
- **GitHub 讨论**：[提出问题并分享想法](https://github.com/krishna3163/OmniShizuku/discussions)
- **文档**：查看您首选语言的自述文件
- **源代码**：[github.com/krishna3163/OmniShizuku](https://github.com/krishna3163/OmniShizuku)

---

<div align="center">

**由 [krishna3163](https://github.com/krishna3163) 和令人惊叹的 OmniShizuku 社区的 ❤️ 制作**

*如果您发现 OmniShizuku 有用，请考虑在 GitHub 上给它 ⭐！*

</div>
