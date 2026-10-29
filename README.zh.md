# OmniShizuku

OmniShizuku 是一个用于 Shizuku、ADB 和 Dhizuku 工作流的开源 Android 实用工具。它通过 Shizuku 服务提供系统级 API 访问，包含设备管理、自动化、优化等模块。

[![GitHub](https://img.shields.io/badge/GitHub-krishna3163/OmniShizuku-blue?logo=github)](https://github.com/krishna3163/OmniShizuku)
[![License: Apache-2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Android-5.0%2B-green?logo=android)](https://developer.android.com)

---

## 功能

### 必备功能中心
- **动态岛** — 用于实时媒体、电池和警报的浮动打孔屏药丸覆盖层
- **智能像素（AMOLED 节电）** — 禁用 25–75% 的 OLED 子像素网格，节省 15–44% 显示功耗
- **显示调整器** — 分辨率、屏幕密度 (DPI) 和刷新率覆盖
- **强制深色模式** — 在所有已安装应用中强制深色主题
- **电池中心与守护** — 实时电池健康、循环计数、温度和寿命守护
- **口袋模式守护** — 基于接近度和光线传感器防止口袋误触
- **屏幕安全与 DRM 解锁** — 绕过 FLAG_SECURE 以在银行和流媒体应用中截屏
- **权限与 AppOps** — 特权权限控制器和后台服务限制
- **SystemUI 调整器** — 状态栏图标黑名单、电池图标样式和沉浸式模式
- **Axeron 游戏与 CPU 优化器** — 性能配置文件、零动画延迟和缓存修剪
- **强制 Doze** — 屏幕关闭时的激进待机深度空闲自动化
- **高级电源控制** — 通过 shell 进行重启、恢复、Bootloader、关机和睡眠键
- **私有 DNS 控制器** — 加密零 VPN DNS 预设（Cloudflare、AdGuard、Quad9、Google）
- **运营商共享绕过** — 解锁运营商限制的热点
- **已保存 Wi-Fi 密码** — 读取和显示已保存的 Wi-Fi 接入点和密钥
- **Shut Up! 媒体静音器** — 嘈杂应用激活时按应用静音媒体音频
- **音频流大师** — 媒体、铃声、闹钟、来电独立音量流控制
- **音量键跳过轨道** — 屏幕关闭时使用音量上/下键跳过曲目
- **通知边缘照明** — 通知时沿显示屏边缘的霓虹边缘发光动画
- **自动 DND（禅定模式）** — 基于应用焦点和计划的自动化请勿打扰调度
- **Caffeinate** — 带停止通知和快速设置磁贴的保持显示屏唤醒
- **照片水印与 EXIF 工作室** — 奢华 Leica 框架和 Hasselblad 极简品牌，带实时 EXIF 条纹
- **通用 Android 删除工具（UAD）** — 社区安全排名的包删除和 1 点恢复
- **应用冻结器** — 无需卸载即可瞬间冻结后台应用
- **特权文件资源管理器** — 浏览受保护的系统路径和 Android/data 目录
- **安装包专业版** — 安装分割 APK、APKS、XAPK，带降级和低 SDK 标志

### 无线 ADB 自动 PIN（辅助功能）
- **关闭** — 自己输入配对 PIN
- **手动** — 管理器告诉您对话框何时出现
- **自动** — 辅助功能服务在“配对设备”对话框中输入存储的 PIN
- 仅限选择加入 — 在设置中启用之前什么都不会运行

### 性能与稳定性
- **惰性动态模糊** — 帧采样器循环仅在滚动时运行，消除每帧 CPU 消耗
- **Crashlytics + 未捕获异常处理程序** — 崩溃会被记录，而不是静默关闭应用
- **防御性 Shizuku 绑定** — 空安全绑定访问，防止 Realme/OEM NPE 崩溃
- **设备信息受限加载** — dumpsys 查询的 20 秒超时，显示部分数据而不是卡住的加载指示器

### 首次启动引导
- 6 个免责声明屏幕（特权、设备所有者副作用、Root 风险、第三方行为、许可证、银行应用警告）
- 带有 5 秒倒计时计时器和“我已阅读并同意”复选框的协议
- 语言选择（系统 + 12 种语言）
- 主题选择（跟随系统/深色/浅色/黑夜 AMOLED/使用系统颜色）
- 激活方法（无线调试/Root/连接计算机）

---

## 要求

- Android 5.0（API 21）或更高版本
- Shizuku 服务正在运行（通过无线调试、Root 或 ADB）
- 已向应用授予 Shizuku 权限

---

## 安装

1. 从 [GitHub Releases](https://github.com/krishna3163/OmniShizuku/releases) 安装 APK
2. 在您的设备上启用 Shizuku（无线调试或 ADB）
3. 向 OmniShizuku 授予 Shizuku 权限
4. 首次启动时，完成引导流程

---

## 从源代码构建

```bash
git clone git@github.com:krishna3163/OmniShizuku.git
cd OmniShizuku
./gradlew :manager:assembleDebug
```

调试 APK 将位于 `manager/build/outputs/apk/debug/manager-debug.apk`。

---

## 安全

此应用通过 Shizuku 请求提升的系统权限。请负责任地使用：

- 不要向不可信应用授予访问权限
- 无线调试和 ADB 暴露系统接口
- Root 访问绕过 Android 安全模型
- 设备所有者模式可能有副作用（出厂重置限制、应用删除限制）

请参阅首次启动免责声明了解完整详情。

---

## 许可证

Apache 许可证 2.0。详情请参阅 [LICENSE](LICENSE)。

---

## 免责声明

OmniShizuku 按原样提供，不提供任何明示或暗示的担保。请自行承担使用风险。我们对数据丢失、保修失效或第三方应用程序行为不承担责任。
