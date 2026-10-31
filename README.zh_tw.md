# OmniShizuku

OmniShizuku 是一款專為 Shizuku、ADB 與 Dhizuku 工作流程設計的開源 Android 實用工具。它透過 Shizuku 服務提供系統級別的 API 存取權限，並整合裝置管理、自動化流程及效能最佳化等強大模組。

[![GitHub](https://img.shields.io/badge/GitHub-krishna3163/OmniShizuku-blue?logo=github)](https://github.com/krishna3163/OmniShizuku)
[![License: Apache-2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Android-5.0%2B-green?logo=android)](https://developer.android.com)

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

```bash
git clone git@github.com:krishna3163/OmniShizuku.git
cd OmniShizuku
./gradlew :manager:assembleDebug
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

---

## 免責聲明

OmniShizuku 按「現狀」(as-is) 提供，不附帶任何明示或暗示之保證。使用本軟體的風險完全由使用者自行承擔。我們對於資料遺失、原廠保固失效或第三方應用程式異常行為概不負責。
