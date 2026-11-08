<div align="center">

<img src="../manager/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" width="128" height="128" alt="OmniShizuku Logo">

# 全雫

**功能強大、功能豐富的 Shizuku 分支，具有增強的模組、現代 UI 和擴展的系統控制功能。 **

[![GitHub](https://img.shields.io/badge/GitHub-krishna3163%2FOmniShizuku-blue?logo=github&style=for-the-badge)](https://github.com/krishna3163/OmniShizuku)
[![許可證: 阿帕契-2.0](https://img.shields.io/badge/許可證-Apache%202.0-blue.svg?style=for-the-badge)](../LICENSE)
[![Android](https://img.shields.io/badge/Android-5.0%2B-green?logo=android&style=for-the-badge)](https://developer.android.com)
[![API](https://img.shields.io/badge/API%20Level-21%2B-brightgreen?style=for-the-badge)](https://developer.android.com/guide/topics/manifest/uses-sdk-element)
[![Shizuku](https://img.shields.io/badge/Powered%20by-Shizuku%20API-orange?style=for-the-badge)](https://github.com/RikkaApps/Shizuku)

🌐 **Documentation available in:** [English](../README.md) • [हिन्दी](./README.hi.md) • [Español](./README.es.md) • [Français](./README.fr.md) • [中文（简体）](./README.zh.md) • [中文（繁體）](./README.zh_tw.md) • [Русский](./README.ru.md) • [العربية](./README.ar.md) • [日本語](./README.ja.md) • [Português](./README.pt.md) • [Қазақша](./README.kk.md) • [Qazaqşa](./README.kk-Latn.md)

</div>

---

## 📖 目錄

- [概述](#-概述)
- [螢幕截圖](#-螢幕截圖)
- [為什麼要使用 OmniShizuku？ ](#-why-use-omnishizuku)
- [主要特點](#-關鍵-特點)
  - [實驗室 - 進階系統工具（40 個工具）](#labs---advanced-system-tools)
  - [模組系統](#modules-system)
  - [通話錄音（Shizu通話記錄器 整合）](#call-recording-shizucallrecorder-integration)
  - [人工智慧與開發者功能](#ai--developer-features)
  - [UI/UX 增強](#uiux-enhancements)
- [功能比較](#-feature-comparison-with-other-shizuku-forks)
- [系統需求](#-系統需求)
- [安裝](#-安裝)
- [入門](#-getting-started)
- [從原始碼建構](#-building-from-source)
- [架構與安全性](#-架構--安全)
- [權限解釋](#-permissions-explained)
- [貢獻](#-貢獻)
- [致謝與致謝](#-致謝--致謝)
- [許可證](#-許可證)
- [支持](#-支持)

---

## 🔍 概述

**全雫** 是原始 [Shizuku](https://github.com/RikkaApps/Shizuku) 專案的一個廣泛增強的開源分支。它透過將 Shizuku 強大的系統級 API 存取與大量實用實用程式、系統調整、隱私控制和直覺的現代介面相結合，擴展了 Shizuku 強大的系統級 API 存取。

🌐 **Documentation available in:** [English](../README.md) • [हिन्दी](./README.hi.md) • [Español](./README.es.md) • [Français](./README.fr.md) • [中文（简体）](./README.zh.md) • [中文（繁體）](./README.zh_tw.md) • [Русский](./README.ru.md) • [العربية](./README.ar.md) • [日本語](./README.ja.md) • [Português](./README.pt.md) • [Қазақша](./README.kk.md) • [Qazaqşa](./README.kk-Latn.md)
OmniShizuku 採用 **Material Design 3 Expressive** 構建，使用戶能夠完全控制其 Android 設備，在大多數情況下無需完全 root 訪問權限 - 利用 Shizuku 的 ADB/root 橋接功能。從顯示客製化和電池優化到應用程式管理、消脹、通話錄音和高級自動化，OmniShizuku 是您的一體化系統實用套件。

---

## 📸 截圖

| **主頁和狀態** | **應用程式權限** | **亞行模組** | **實驗室目錄** | **計算（終端和人工智慧）** | **設定** |
|:---:|:---:|:---:|:---:|:---:|:---:|
| <img src="../screenshots/home.png" width="165" alt="Home Screen" /> | <img src="../screenshots/manage_apps.png" width="165" alt="Manage Authorized Apps" /> | <img src="../screenshots/modules.png" width="165" alt="ADB Modules" /> | <img src="../screenshots/labs.png" width="165" alt="Labs Catalog" /> | <img src="../screenshots/comput.png" width="165" alt="Comput Console" /> | <img src="../screenshots/settings.png" width="165" alt="Settings Screen" /> |
| 即時服務狀態&無線ADB配對 | 白名單模式、精細權限和僅限會話訪問 | Magisk/KernelSU 風格的 ZIP 模組安裝程式和管理器 | 39+ 强大的系统控制和调整 | 具有 Gemini/AI 生成功能的特權 shell 終端 | 設備所有者、備份、自動化和安全性配置 |

---

### 🔬 特色實驗室和系統功能

| **系統控制與電動工具** | **本機 ShizuWall 防火牆** |
|:---:|:---:|
| <img src="../screenshots/labs_tools.png" width="260" alt="Labs System Tools" /> | <img src="../screenshots/shizuwall.png" width="260" alt="ShizuWall Firewall" /> |
| 強制打瞌睡、混音音訊、私有 DNS、螢幕安全和 DFRoot | 帶有追蹤器掃描器和 appop 規則的零 VPN 本機防火牆 |

---

## 💡 為什麼要使用 OmniShizuku？

- **一體化解決方案** - 無需多個 root/ADB 應用程式。從膨脹到顯示調整的一切都集中在一個地方。
- **無需 Root** - 透過無線 ADB (Android 11+) 或 ADB 與 Shizuku 配合使用，使其可供更廣泛的受眾使用。
- **現代且直覺的 UI** - 乾淨的 材料3 富有表現力的介面，具有流暢的動畫、動態主題以及針對手機、平板電腦、電視和 Wear OS 的優化佈局。
- **主動增強** - 定期更新新功能、錯誤修復以及原始 Shizuku 以外的改進。
- **注重隱私** - 對應用程式權限、應用程式、隱藏功能和系統級限制進行精細控制。
- **安全且可逆** - 大多數更改都是可逆的，具有用於膨脹和模組管理的備份/恢復功能。
- **可擴展模組系統** - 社群驅動的模組，具有內建目錄、發現和安裝支援。
- **企業級穩定性** - 防禦性編碼實踐、崩潰處理、逾時保護和 OEM 相容性修復。

---

## ✨ 主要特點

### 實驗室 - 高階系統工具

實驗室目錄包含 40 個系統級工具，按應用程式管理、系統控制和實用程式進行清晰分類：

#### 📱 应用程序管理（8 个工具）
| 特點 | 描述 |
| --- | --- |
| **應用程式操作** | 對隱藏的應用程式權限、執行時間操作門和背景權限進行精細控制。 |
| **防火牆** | 网络级应用程序防火墙可阻止每个应用程序的 Wi-Fi 和移动数据，而无需占用 VPN 插槽。 |
| **自動啟動** | 限制應用程式在裝置啟動時自動喚醒或啟動服務。 |
| **隱藏模式** | 隱藏開發人員選項、USB/無線偵錯、輔助服務、私人 DNS 和 VPN 狀態，使其免於偵測。 |
| **強制黑暗模式** | 即使沒有本機深色主題支持，系統範圍內的第三方應用程式也會強制使用深色主題。 |
| **通用去膨脹器 (UAD)** | 社群安全排名的軟體包 debloater，具有安全評級、解釋和一鍵恢復功能。 |
| **应用程序冷冻室** | 立即冻结和暂停后台应用程序以回收 RAM 并停止电池消耗。 |
| **Axeron 游戏优化器** | 系統級效能調整、遊戲提升、動畫調整和流程最佳化。 |

#### ⚙️ 系統控制（18 個工具）
| 特點 | 描述 |
| --- | --- |
| **電源選單** | 特權重新啟動到系統、復原、引導程式/快速啟動、安全模式或即時關機。 |
| **顯示調整器** | 覆蓋顯示解析度、修改螢幕密度 (DPI) 並強制自訂更新率。 |
| **Wi-Fi 密碼** | 直接透過特權 shell 檢視、複製和匯出已儲存的 Wi-Fi 網路憑證。 |
| **系統UI調節器** | 自訂狀態列圖示可見性、電池圖示樣式、時鐘秒數和沈浸式導覽。 |
| **強迫打瞌睡（深度睡眠）** | 每當螢幕關閉時，強制立即進入打瞌睡深度睡眠模式。 |
| **私人 DNS 控制器** | 使用自動復原計時器在加密 DNS 供應商（Cloudflare、AdGuard、Quad9、Google、NextDNS、Custom）之間切換。 |
| **音訊串流大師** | 用于媒体、铃声、闹钟、呼叫、语音和系统音频流的独立细粒度音量滑块。 |
| **運營商網路共享** | 繞過運營商的權利檢查以及 Wi-Fi 熱點和 USB 網路共享的限制。 |
| **混音音訊** | 多应用程序并发音频播放旁路（防止后台应用程序在另一个应用程序播放音频时暂停）。 |
| **聲音大師** | 每個應用程式的音訊焦點和音量路由混合器。 |
| **通用畫中畫** | 使用虛擬顯示投影強制任何不受支援的應用程式進入畫中畫 (PiP) 模式。 |
| **螢幕安全和 DRM** | 繞過“FLAG_SECURE”限制，允許在安全/銀行應用程式中進行螢幕截圖和螢幕錄製。 |
| **自动免打扰（Zen 模式）** | 基於前台應用程式焦點、自訂計畫和觸發器的自動請勿打擾規則。 |
| **DFRoot（DirtyFrag 漏洞利用）** | 針對受支援的核心版本使用 CVE-2026-43284 的內建權限提升幫助程式。 |
| **快速設定圖塊** | 用于 18 个自定义快速设置图块的交互式管理器和设置套件。 |
| **ShizuWall 防火牆** | 先進的防火牆介面具有整合的追蹤器和遙測掃描器以及套接字監視器。 |
| **咖啡因** | 通过磁贴和持久通知保持设备屏幕处于唤醒状态以进行阅读或导航。 |
| **主題補丁** | 系統級強調色、字體樣式和 UI 主題修改。 |

#### 🛠️ 實用程式（14 種工具）
| 特點 | 描述 |
| --- | --- |
| **外殼** | 具有实时流输出的交互式特权命令行终端。 |
| **日誌檢視器** | 即時系統 logcat 檢視器，具有標籤、套件、等級過濾和匯出功能。 |
| **設備資訊** | 全面的硬體、核心、電池健康狀況、熱感應器和 SoC 儀表板。 |
| **活動啟動器** | 检查并启动导出的、未导出的或隐藏的 Android 活动和意图。 |
| **文件** | 特權檔案系統瀏覽器，具有對受保護的“/data”和應用程式目錄的讀取/寫入存取權限。 |
| **应用程序安装专业版** | 支援 APK、APKS 分割捆綁包和 XAPK 格式的特權包安裝程式。 |
| **終端應用程式** | 外部終端模擬器的快速切換器和啟動器。 |
| **本地外殼** | 獨立的本地 shell 介面。 |
| **意圖** | 激發自訂顯式或隱式系統意圖、廣播接收器和服務。 |
| **隱身模式** | 反檢測措施，防止目標應用程式檢測 Shizuku、root 或偵錯狀態。 |
| **權限管理員** | 概述並撤銷/授予所有已安裝軟體包的提升權限。 |
| **雫店** | 用於發現和下載為 Shizuku 建置的開源應用程式的內建目錄。 |
| **通話錄音（Shizu通話記錄器）** | 由特权 shell 音频捕获支持的本机双向通话录音。 |
| **WallYou壁紙** | 應用內壁紙瀏覽器、獲取器和壁紙切換器。 |

### 模組系統

OmniShizuku 具有强大的、可扩展的模块系统，其灵感来自 Magisk/KernelSU 模块，但专为基于 Shizuku 的 mods 设计：

- **模組目錄** - 透過搜尋、過濾和排序從基於 GitHub 的精選目錄中瀏覽社群模組。
- **模組發現** - 從 GitHub 儲存庫自動發現標有「shevery-modules」或官方來源的模組。
- **一鍵安裝** - 直接從應用程式下載、驗證和安裝模組，並追蹤進度。
- **模組管理器** - 輕鬆啟用、停用、更新和刪除已安裝的模組。
- **WebUI 支持** - 许多模块包括可从应用程序内访问的基于 Web 的配置界面。
- **模組腳本** - 透過正確的日誌記錄和錯誤處理來執行安裝、卸載、服務和啟動腳本。
- **更新检查器** - 从 GitHub 版本自动检测已安装模块的更新。
- **安全安裝** - 安裝前先驗證模組結構、道具和腳本。
- **跨平台 UI** - 針對手機、平板電腦、電視和 Wear OS 最佳化的模組螢幕。

### 通話錄音（Shizu通話記錄器 整合）

透過 Shizuku 的特權存取內建通話錄音支援：

- **基於 Shell 的錄音引擎** - 使用特權 shell 服務實現可靠的音訊擷取。
- **前台服務** - 具有適當通知的持久錄製服務。
- **InCallService 整合** - 自動呼叫偵測和錄音管理。
- **錄音管理** - 使用直覺的使用者介面檢視、播放、重新命名和刪除錄音。
- **可設定選項** - 自訂錄製格式、編解碼器、來源和儲存位置。
- **AIDL 介面** - 應用程式元件之間的安全 IPC，用於錄製控制。
- **權限處理** - 針對通話錄音要求的適當的執行時間權限管理。
- **背景操作** - 即使應用程式在背景運行，也能進行可靠的錄製。

### 人工智能和开发者功能

- **AI 管理器** - 整合 AI 功能，支援多個提供者（可在設定中設定）。
- **開發人員選項** - 進階開發人員工具、除錯實用程式和實驗功能。
- **錯誤報告系統** - 內建錯誤報告，包括日誌、設備資訊和崩潰報告。
- **實驗室重新應用** - 更新或重新啟動後重新應用持久實驗室修改。
- **設備資訊儀表板** - 全面的設備信息，包括詳細的硬體、軟體和系統屬性。
- **日誌檢視器** - 具有過濾和搜尋功能的即時 logcat 檢視器。

### 使用者介面/使用者體驗增強

- **材料3 Expressive Design** - 最新的 Material Design，具有動態色彩主題 (Material You) 支援。
- **多外形支援** - 針對手機、平板電腦、可折疊裝置、Android TV 和 Wear OS 進行了最佳化。
- **深色模式和 AMOLED 黑色** - 完全深色主題支持，並為 AMOLED 顯示器提供純黑色選項。
- **平滑動畫** - 精美的過渡、惰性運動模糊優化和流暢的互動。
- **自適應佈局** - 響應式 UI，可適應螢幕尺寸和方向。
- **在地化** - 廣泛的多語言支援（英語、印地語、西班牙語、法語、中文、俄語、日語、阿拉伯語、哈薩克語等）。
- **輔助使用** - 具有輔助使用服務支援的無線 ADB 自動 PIN（僅限選擇加入）。
- **效能最佳化** - 延遲載入、有界資料取得、逾時保護和高效的記憶體使用。
- **崩潰彈性** - 防禦性綁定存取、未捕獲的異常處理以及針對 OEM 特定問題的優雅降級。

---

## 📊 與其他 Shizuku 前叉的功能比較

| 特點 | **全雫** | **雫（原版）** | **下一步雫** | **西築** | **謝弗里** | **雫加** | **恆星** |
| --- | :------------: | :--------------------: | :------------: | :--------: | :--------: | :------------: | :--------: |
| **基本功能** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **無線ADB / TCP模式** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **根支持** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Dhizuku 支持** | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ | ❌ |
| **自動啟動** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **模組系統** | ✅ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ |
| **實驗室高階工具（40 個工具）** | ✅ | ❌ | 部分 | 部分 | 部分 | ❌ | 部分 |
| **应用程序去膨胀器 (UAD)** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **应用程序冷冻室** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **防火牆（零 VPN）** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **快速設定磁磚套件（18 個磁磚）** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **ShizuWall 本機防火牆** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **追蹤器和遙測掃描器** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **咖啡因螢幕保持清醒** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **私有 DNS 自動復原和週期** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **DFRoot（核心漏洞利用助手）** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **WallYou動態壁紙** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **通用畫中畫** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **顯示調整器** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **通話錄音** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **主題補丁** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **內建終端** | ✅ | ❌ | ✅ | ❌ | ✅ | ❌ | ✅ |
| **人工智慧整合** | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ |
| **材料 3 表現力** | ✅ | ❌ | 材料3 | 材料3 | 材料3 | ❌ | ❌ |
| **TV/Wear 作業系統支援** | ✅ | 部分 | 部分 | 部分 | ❌ | ❌ | ❌ |
| **入職流程** | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| **隱形功能** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **無線 ADB 自動 PIN** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **許可證** | 阿帕契-2.0 | 阿帕契-2.0 | 阿帕契-2.0 | 阿帕契-2.0 | 阿帕契-2.0 | 阿帕契-2.0 | MPL-2.0 |

*比较基于撰写本文时公开的功能。功能可能会随着时间的推移而改变。 *

---

## 📱 系統需求

| 組件 | 要求 |
| --- | --- |
| **安卓版本** | Android 5.0 (Lollipop) API 21 或更高版本 |
| **架構** | arm64-v8a、armeabi-v7a、x86、x86_64 |
| **雫** | Shizuku 服務必須正在運作（透過無線偵錯、ADB over USB 或 Root） |
| **權限** | 大多數功能都需要 Shizuku 許可 |
| **存儲** | ~50-80 MB 用於安裝（因功能而異） |
| **可選** | 无线 ADB 自动 PIN 功能的辅助服务 |

---

## 📥 安裝

### 來自 GitHub 版本（推薦）

1. 前往[發佈頁面](https://github.com/krishna3163/OmniShizuku/releases)
2. 下載最新的`manager-debug.apk`或`manager-release.apk`
3. 在您的 Android 裝置上安裝 APK
4. 遵循設定過程

### 透過亞銀

```bash
adb install -r OmniShizuku.apk
```

### 重要提示

- 如果您安裝了原始 Shizuku，通常可以同時安裝 OmniShizuku 或替換它。檢查您的用例的兼容性。
- 某些功能需要特定的 Android 版本或裝置功能。
- 對於通話錄音功能，根據 Android 版本，可能需要額外的權限。

---

## 🚀 開始使用

1. **安裝 OmniShizuku** - 下載並安裝 APK
2. **設定 Shizuku** - 透過無線偵錯 (Android 11+) 或 ADB/Root 啟動 Shizuku 服務
3. **授予權限** - 打開 OmniShizuku 並在出現提示時授予 Shizuku 權限
4. **探索** - 瀏覽主頁、應用程式、模組、實驗室和設定標籤
5. **自訂** - 前往 Essentials Hub 或實驗室啟用您需要的功能
6. **保持安全** - 在應用系統級更改之前閱讀功能描述

**對於無線調試（Android 11+）：**
- 前往「設定 > 開發者選項 > 無線偵錯」並與 OmniShizuku 或 Shizuku 配對

**對於 ADB（透過 PC）：**
```bash
adb shell sh /storage/emulated/0/Android/data/moe.shizuku.privileged.api/start.sh
```
*（或使用適合您的設定的啟動命令）*

---

## 🔨 從源頭構建

### 先決條件

- [Android Studio](https://developer.android.com/studio)（最新穩定）或[Android SDK](https://developer.android.com/studio#command-tools)
- [JDK 17](https://adoptium.net/) 或更高版本
- [Git](https://git-scm.com/)
- Gradle（含包裝器）

### 克隆和構建

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

調試 APK 將位於：`manager/build/outputs/apk/debug/manager-debug.apk`

### 建置要求

- `編譯SDK`：36
- `minSdk`: 21
- `目標SDK`：36
- Kotlin 與 Jetpack Compose
- R8/ProGuard 用於發布版本

---

## 🔐 架構與安全

### 安全措施

- **防禦性程式設計** - 空白安全綁定器存取、圍繞關鍵操作的 try-catch 區塊以及優雅的回退
- **權限範圍** - 必要時使用運行時權限請求的最小權限使用
- **安全 IPC** - AIDL 接口，用於透過正確的服務綁定進行進程間通訊
- **無遙測** - 無分析、追蹤或資料收集
- **開源** - 完全透明、可審計的程式碼庫
- **可逆更改** - 大多數修改可以透過應用程式撤銷
- **验证** - 模块验证、包验证和安全安装实践
- **OEM 相容性** - 針對 Realme、OPPO、小米、OnePlus 和其他具有積極電池優化的 OEM 的特殊處理

### 建築亮點

- **MVVM 模式** - ViewModel 和 UI 狀態的關注點完全分離
- **Jetpack Compose** - 現代聲明式 UI 框架
- **Kotlin 協程** - 具有適當生命週期處理的非同步操作
- **房間/首選項** - 本地資料持久性
- **Hilt/Dagger** - 依賴注入（如果適用）
- **模組化設計** - 功能模組可實現更好的組織和可維護性

---

## 🔑 權限解釋

| 授權 | 目的 | 需要用於 |
| --- | --- | --- |
| `QUERY_ALL_PACKAGES` | 列出並管理已安裝的應用程式 | 應用程式操作、Debloater、冷凍機、模組 |
| `READ_EXTERNAL_STORAGE` / `MANAGE_EXTERNAL_STORAGE` | 文件訪問 | 檔案總管、APK 安裝 |
| `POST_通知` | 顯示通知 | 服務、咖啡因、錄音 |
| `前台服務` | 運行後台服務 | 通話錄音、ShizuWall、咖啡因 |
| `READ_PHONE_STATE`、`RECORD_AUDIO` | 呼叫偵測與錄音 | Shizu通話記錄器 |
| `BIND_ACCESSIBILITY_SERVICE` | 自动填写无线 ADB PIN | 無線 ADB 自動 PIN（選擇加入） |
| `PACKAGE_USAGE_STATS` | 使用資料存取 | 一些優化功能 |
| `請求安裝包` | 安裝APK | 軟體包安裝程式專業版 |
| `SYSTEM_ALERT_WINDOW` | 疊加顯示 | 通用畫中畫、螢幕覆蓋 |

*大多數權限僅在需要特定功能時才請求。 *

---

## 🤝 貢獻

歡迎貢獻！無論是錯誤報告、功能請求、文件改進或程式碼貢獻。

### 如何貢獻

1. **分叉**存儲庫
2. **建立**一個功能分支（`git checkout -b feature/Amazing特點`）
3. **提交**您的更改（`git commit -m 'Add some Amazing特點'`）
4. **推送**到分支（`git push origin feature/Amazing特點`）
5. **打開**拉取請求

### 指南

- 遵循現有的程式碼風格和約定
- 編寫清晰的提交訊息
- 徹底測試您的更改
- 如果需要更新文檔
- 對於主要功能，先開issue討論

See [CONTRIBUTING.md](../CONTRIBUTING.md) for more details.

---

## 🙏 致謝與致謝

### 核心專案

- **[RikkaApps/Shizuku](https://github.com/RikkaApps/Shizuku)** - 使这一切成为可能的原始项目。非常感謝 Rikka 和所有貢獻者。
- **[Dhizuku](https://github.com/iamr0s/Dhizuku)** - 用于 Dhizuku API 集成支持。

### 社區與靈感

- **[Universal Android Debloater (UAD)](https://github.com/0x192/universal-android-debloater)** - 安全排名和 debloater 方法
- **[LSPosed](https://github.com/LSPosed/LSPosed)** - 模块系统概念的灵感
- **[Magisk](https://github.com/topjohnwu/Magisk)** - 模块系统架构灵感
- **[KernelSU](https://github.com/tiann/KernelSU)** - 现代根解决方案灵感

### 模組開發人員和集成

- **[kitsumed/Shizu通話記錄器](https://github.com/kitsumed/Shizu通話記錄器)** - 通話錄音功能集成
- **[RBN-Apps/Quick-Tile-Settings](https://github.com/RBN-Apps/Quick-Tile-Settings)** - 配套应用程序集成
- **[AhmetCanArslan/ShizuWall](https://github.com/AhmetCanArslan/ShizuWall)** - 配套应用程序集成
- **[Shevery Modules Community](https://github.com/topics/shevery-modules)** - 社區模組生態系統

### 特別感謝

衷心感謝：

- **Shizuku 社区** - 提供持续支持、反馈和测试
- **所有貢獻者** - 報告錯誤、建議功能、提交 PR 或幫助改進 OmniShizuku 的每個人
- **翻译器** - 使 OmniShizuku 能够以多种语言访问
- **Beta 測試人員** - 協助發現錯誤並確保穩定性
- **OEM 社群** - 用於分享特定於設備的解決方法和修復

### 為什麼選擇 OmniShizuku？

OmniShizuku 的獨特之處在於將 Shizuku 的強大功能與一套全面的實用工具結合在一個設計精美的應用程式中。 OmniShizuku 無需同時使用多個應用程式來縮減、調整、優化和管理您的設備，而是將所有內容整合在一起，提供具有凝聚力的體驗。無論您是希望停用臃腫軟體的臨時用戶，還是尋求深度系統客製化的高級用戶，OmniShizuku 都能提供您所需的工具，並以安全性、可逆性和易用性為核心。

---

## 📄 許可證

This project is licensed under the **Apache 許可證 2.0**. See the [LICENSE](../LICENSE) file for full details.

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

## 💬 支援

- **GitHub 問題**：[回報錯誤或請求功能](https://github.com/krishna3163/OmniShizuku/issues)
- **GitHub 討論**：[提問並分享想法](https://github.com/krishna3163/OmniShizuku/discussions)
- **文件**：查看您首選語言的自述文件
- **原始碼**：[github.com/krishna3163/OmniShizuku](https://github.com/krishna3163/OmniShizuku)

---

<div align="center">

**由 [krishna3163](https://github.com/krishna3163) 和令人驚嘆的 OmniShizuku 社區的 ❤️ 製作**

*如果您發現 OmniShizuku 有用，請考慮在 GitHub 上給它 ⭐！ *

</div>
