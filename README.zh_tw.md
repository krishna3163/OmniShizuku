# OmniShizuku

OmniShizuku 是一個開源的 Android 實用工具，用於 Shizuku、ADB 和 Dhizuku 工作流程。它透過 Shizuku 服務提供系統級 API 存取，包含裝置管理、自動化、最佳化等模組。

[![GitHub](https://img.shields.io/badge/GitHub-krishna3163/OmniShizuku-blue?logo=github)](https://github.com/krishna3163/OmniShizuku)
[![License: Apache-2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Android-5.0%2B-green?logo=android)](https://developer.android.com)

---

## 功能

### 必備功能中心
- **動態島** — 用於即時媒體、電池和警報的浮動打孔屏藥丸覆蓋層
- **智能像素（AMOLED 省電）** — 禁用 25–75% 的 OLED 子像素網格，節省 15–44% 顯示耗電
- **顯示調整器** — 解析度、螢幕密度 (DPI) 和刷新率覆蓋
- **強制深色模式** — 在所有已安裝應用程式中強制深色主題
- **電池中心與守護** — 即時電池健康、循環計數、溫度與壽命守護
- **口袋模式守護** — 基於接近度和光線感應器防止口袋誤觸
- **螢幕安全與 DRM 解鎖** — 繞過 FLAG_SECURE 以在銀行和串流應用程式中截圖
- **權限與 AppOps** — 特權權限控制器和後台服務限制
- **SystemUI 調整器** — 狀態列圖示黑名單、電池圖示樣式與沉浸式模式
- **Axeron 遊戲與 CPU 最佳化器** — 效能配置檔、零動畫延遲與快取修剪
- **強制 Doze** — 螢幕關閉時的激進待機深度閒置自動化
- **進階電源控制** — 透過 shell 進行重新開機、恢復、Bootloader、關機和睡眠鍵
- **私有 DNS 控制器** — 加密零 VPN DNS 預設（Cloudflare、AdGuard、Quad9、Google）
- **電信業者網路共用繞過** — 解鎖電信業者限制的熱點
- **已儲存 Wi-Fi 密碼** — 讀取並顯示已儲存的 Wi-Fi 接入點和密鑰
- **Shut Up! 媒體靜音器** — 嘈雜應用程式啟動時按應用程式靜音媒體音訊
- **音訊流大師** — 媒體、鈴聲、鬧鐘、來電獨立音量流控制
- **音量鍵跳過軌道** — 螢幕關閉時使用音量上/下鍵跳過曲目
- **通知邊緣照明** — 通知時沿顯示屏邊緣的霓虹邊緣發光動畫
- **自動 DND（禪定模式）** — 基於應用程式焦點和計劃的自動化請勿打擾排程
- **Caffeinate** — 帶停止通知和快速設定磁貼的保持顯示屏喚醒
- **照片水印與 EXIF 工作室** — 奢華 Leica 架構與 Hasselbak 極簡品牌，帶即時 EXIF 條紋
- **通用 Android 刪除工具（UAD）** — 社群安全排名的包刪除和 1 點恢復
- **應用程式冰結器** — 無需卸載即可瞬間冰結後台應用程式
- **特權檔案總管** — 瀏覽受保護的系統路徑和 Android/data 目錄
- **安裝包專業版** — 安裝分割 APK、APKS、XAPK，帶降級和低 SDK 標誌

### 無線 ADB 自動 PIN（輔助功能）
- **關閉** — 自己輸入配對 PIN
- **手動** — 管理器告訴您對話框何時出現
- **自動** — 輔助功能服務在「配對裝置」對話框中輸入儲存的 PIN
- 僅限選擇加入 — 在設定中啟用之前什麼都不會運行

### 效能與穩定性
- **惰性動態模糊** — 幀取樣器迴圈僅在捲動時運行，消除每幀 CPU 消耗
- **Crashlytics + 未捕捉異常處理程式** — 崩潰會被記錄，而不是靜默關閉應用程式
- **防禦性 Shizuku 綁定** — 空安全綁定存取，防止 Realme/OEM NPE 崩潰
- **裝置資訊受限載入** — dumpsys 查詢的 20 秒超時，顯示部分數據而不是卡住的載入指示器

### 首次啟動導覽
- 6 個免責聲明畫面（特權、裝置所有者副作用、Root 風險、第三方行為、授權、銀行應用程式警告）
- 帶有 5 秒倒計時計時器和「我已閱讀並同意」複選框的協議
- 語言選擇（系統 + 12 種語言）
- 主題選擇（跟隨系統/深色/淺色/黑夜 AMOLED/使用系統顏色）
- 啟用方法（無線調試/Root/連接電腦）

---

## 要求

- Android 5.0（API 21）或更高版本
- Shizuku 服務正在運行（透過無線調試、Root 或 ADB）
- 已向應用程式授予 Shizuku 權限

---

## 安裝

1. 從 [GitHub Releases](https://github.com/krishna3163/OmniShizuku/releases) 安裝 APK
2. 在您的裝置上啟用 Shizuku（無線調試或 ADB）
3. 向 OmniShizuku 授予 Shizuku 權限
4. 首次啟動時，完成導覽流程

---

## 從原始碼建構

```bash
git clone git@github.com:krishna3163/OmniShizuku.git
cd OmniShizuku
./gradlew :manager:assembleDebug
```

除錯 APK 將位於 `manager/build/outputs/apk/debug/manager-debug.apk`。

---

## 安全

此應用程式透過 Shizuku 請求提升的系統權限。請負責任地使用：

- 不要向不可信任應用程式授予存取權限
- 無線調試和 ADB 揭露系統介面
- Root 存取繞過 Android 安全模型
- 裝置所有者模式可能有副作用（出廠重設限制、應用程式刪除限制）

請參閱首次啟動免責聲明了解完整詳情。

---

## 授權條款

Apache 授權條款 2.0。詳情請參閱 [LICENSE](LICENSE)。

---

## 免責聲明

OmniShizuku 按原狀提供，不提供任何明示或默示的擔保。請自行承擔使用風險。我們對資料遺失、保固失效或第三方應用程式行為不負責任。
