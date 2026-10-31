# OmniShizuku

OmniShizuku 是一个用于 Shizuku、ADB 和 Dhizuku 工作流的开源 Android 实用工具。它通过 Shizuku 服务提供系统级 API 访问，包含设备管理、自动化、系统优化等强大功能模块。

[![GitHub](https://img.shields.io/badge/GitHub-krishna3163/OmniShizuku-blue?logo=github)](https://github.com/krishna3163/OmniShizuku)
[![License: Apache-2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Android-5.0%2B-green?logo=android)](https://developer.android.com)

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

```bash
git clone git@github.com:krishna3163/OmniShizuku.git
cd OmniShizuku
./gradlew :manager:assembleDebug
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

---

## 免责声明

OmniShizuku 按“原样”提供，不提供任何明示或暗示的担保。使用本软件的所有风险由用户自行承担。开发者不对数据丢失、设备保修失效或第三方应用异常行为承担任何法律责任。
