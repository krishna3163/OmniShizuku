# OmniShizuku

OmniShizuku は、Shizuku、ADB、および Dhizuku ワークフロー向けに設計されたオープンソースの Android ユーティリティです。Shizuku サービスを介してシステムレベルの API アクセスを提供し、デバイス管理、自動化、各種最適化などの強力なモジュールを搭載しています。

[![GitHub](https://img.shields.io/badge/GitHub-krishna3163/OmniShizuku-blue?logo=github)](https://github.com/krishna3163/OmniShizuku)
[![License: Apache-2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Android-5.0%2B-green?logo=android)](https://developer.android.com)

🌐 **Documentation available in:** [English](./README.md) • [हिन्दी](./README.hi.md) • [Español](./README.es.md) • [Français](./README.fr.md) • [中文（简体）](./README.zh.md) • [中文（繁體）](./README.zh_tw.md) • [Русский](./README.ru.md) • [العربية](./README.ar.md) • [日本語](./README.ja.md) • [Português](./README.pt.md) • [Қазақша](./README.kk.md) • [Qazaqşa](./README.kk-Latn.md)

---

## 主な機能

### エッセンシャルズ・ハブ (Essentials Hub)
- **ダイナミック・アイランド (Dynamic Island)** — パンチホール周囲にメディア再生、バッテリー、通知を表示するフローティングピル
- **スマートピクセル (Smart Pixels - AMOLED 省電力)** — OLED サブピクセルの 25–75% を無効化し、画面の消費電力を 15–44% 削減
- **ディスプレイ微調整 (Display Tweaker)** — 解像度、画面密度 (DPI)、リフレッシュレートの上書き
- **強制ダークモード (Force Dark Mode)** — インストール済み全アプリでダークテーマを強制適用
- **バッテリーセンター＆ガード (Battery Center & Guard)** — リアルタイムのバッテリー健全性、充電サイクル、温度、長寿命保護
- **ポケットモード・ガード (Pocket Mode Guard)** — 近接・照度センサーによるポケット内での誤タッチ防止
- **画面セキュリティ＆DRM解除 (Screen Security & DRM Unlock)** — FLAG_SECURE を回避し、銀行やストリーミングアプリでのスクリーンショットを許可
- **権限＆AppOps管理 (Permissions & AppOps)** — 特権権限の制御およびバックグラウンドサービスの制限
- **SystemUIチューナー (SystemUI Tuner)** — ステータスバーアイコンの非表示、バッテリーアイコンのスタイル変更、没入モード
- **Axeron ゲーム＆CPUオプティマイザ (Axeron Gaming & CPU Optimizer)** — パフォーマンスプロファイル、アニメーション遅延ゼロ化、キャッシュクリーン
- **強制Dozeモード (Enforce Doze)** — 画面オフ時にディープアイドル・スタンバイを積極的に自動化
- **高度な電源制御 (Advanced Power Controls)** — シェル経由での再起動、リカバリー、ブートローダー、シャットダウン、スリープ
- **プライベートDNSコントローラー (Private DNS Controller)** — VPN不要の暗号化DNSプリセット (Cloudflare, AdGuard, Quad9, Google)
- **テザリング制限の回避 (Carrier Tethering Bypass)** — キャリアによって制限されたテザリング・ホットスポットを解除
- **保存済みWi-Fiパスワード表示 (Saved Wi-Fi Passwords)** — 端末に保存されているWi-Fiアクセスポイントとパスワードを表示
- **アプリ別メディアミュート (Shut Up! Media Muter)** — 特定のアプリ起動時にメディア音声を自動ミュート
- **オーディオストリームマスター (Audio Stream Master)** — 各オーディオチャンネルの音量を個別に独立制御
- **音量ボタンでの曲送り (Volume Key Track Skipper)** — 画面オフ時に音量ボタンで音楽トラックをスキップ
- **通知エッジライティング (Notification Edge Lighting)** — 通知受信時にディスプレイの縁をネオングローアニメーションで点灯
- **自動おやすみモード (Auto DND - Zen Mode)** — アプリのフォーカスやスケジュールに基づいたサイレントモードの自動化
- **スリープ防止 (Caffeinate)** — 通知やクイック設定タイルから画面を常に点灯状態に維持
- **写真透かし＆EXIFスタジオ (Photo Watermark & EXIF Studio)** — 高級ライカ風フレームやハッセルブラッド風ブランドロゴとライブEXIF情報付与
- **ユニバーサルAndroidデブロート (UAD)** — コミュニティ安全評価に基づく不要アプリの一括削除と1タップ復元
- **アプリフリーザー (App Freezer)** — アンインストールすることなくバックグラウンドアプリを即座に凍結
- **特権ファイルブラウザ (Privileged File Explorer)** — 保護されたシステムディレクトリの参照
- **パッケージインストーラーPro (Package Installer Pro)** — 分割APK (Split APKs, APKS, XAPK) の直接インストール

### 追加統合モジュール (Additional Integrated Modules)
- **テーマパッチャー (Theme Patcher)** — システムテーマのカスタマイズ
- **サウンドマスター (Sound Master)** — 高度なオーディオ設定
- **ミックスオーディオ (Mixed Audio)** — 複数音源のミキシングおよびルーティング
- **ルックバック (LookBack)** — 画面の録画およびクイック再生
- **ユニバーサルPiP (Universal PiP)** — すべてのアプリでピクチャーインピクチャーを有効化
- **インテントシェル (Intent Shell)** — インテントブロードキャストを介したシェルコマンド実行
- **ローカルシェル (Local Shell)** — アプリ内蔵のターミナルエミュレータ

### ワイヤレスADB自動PIN入力 (アクセシビリティ)
- **オフ (Off)** — ペアリングPINを手動で入力
- **手動 (Manual)** — ペアリングダイアログ表示時にアプリが通知
- **自動 (Automatic)** — ユーザー補助サービスが「デバイスとペア設定」ダイアログに保存済みPINを自動入力
- 完全オプトイン制 — 設定画面で明示的に有効化するまでバックグラウンドで動作することはありません

### パフォーマンスと安定性
- **遅延モーションブラー (Lazy motion blur)** — スクロール時のみサンプリングループを動作させ、フレーム毎の無駄なCPU消費を排除
- **Crashlytics + 未処理例外ハンドラ** — 突然のクラッシュ時にもサイレント終了せず詳細なログを記録
- **防御的Shizuku Binderラッパー** — Null安全なバインダーアクセスにより、Realmeや各社OEM製端末でのNPEクラッシュを防止
- **DeviceInfoのタイムアウト制御** — dumpsysクエリに20秒のタイムアウトを設け、スピナーが無限ループすることなく取得済みデータを表示

### 初回起動オンボーディング
- 6つの免責事項画面（特権権限、デバイス管理者機能の影響、Rootリスク、サードパーティ動作、ライセンス、銀行アプリ警告）
- 5秒のカウントダウンタイマーと「上記を読み同意します」チェックボックスによる同意確認
- 言語選択（システム言語 + 12言語）
- テーマ選択（システム追従 / ダーク / ライト / AMOLEDブラック / システムダイナミックカラー）
- 有効化方法の案内（ワイヤレスデバッグ / Root / PC接続）

---

## 他のShizukuフォークとの機能比較

| プロジェクト | 自動起動 | TCPモード | Dhizuku | モジュール対応 | ステルス機能 | ターミナル | AI統合 | Expressive UI | オンボーディング | ライセンス |
|---------|-----------|----------|---------|----------------|---------|----------|----------------|---------------|------------|---------|
| **OmniShizuku** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | Apache-2.0 |
| Porter | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | Apache-2.0 |
| shevery | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | Material 3 | ❌ | Apache-2.0 |
| Shizako | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | Apache-2.0 |
| Shizuku (thedjchi fork) | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | Apache-2.0 |
| Shizuku Next | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | Material 3 | ❌ | Apache-2.0 |
| ShizukuPlus | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | Apache-2.0 |
| Stellar | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | MPL-2.0 |
| Xhizuku | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | Material 3 Expressive | ✅ | Apache-2.0 |

---

## 動作環境

- Android 5.0 (API 21) 以上
- Shizuku サービスが動作していること（ワイヤレスデバッグ、Root、またはADB経由）
- 本アプリに Shizuku の権限が付与されていること

---

## インストール手順

1. [GitHub Releases](https://github.com/krishna3163/OmniShizuku/releases) から最新の APK をダウンロードしてインストールします
2. 端末で Shizuku を有効化します（ワイヤレスデバッグまたはPC ADB経由）
3. OmniShizuku に Shizuku の利用権限を許可します
4. 初回起動時に、画面に表示される初期セットアップウィザードを完了します

---

## ソースコードからのビルド

```bash
git clone git@github.com:krishna3163/OmniShizuku.git
cd OmniShizuku
./gradlew :manager:assembleDebug
```

ビルドされたデバッグAPKは `manager/build/outputs/apk/debug/manager-debug.apk` に生成されます。

---

## コントリビューターと謝辞

GitHub 上で OmniShizuku に貢献してくださったすべての方々に感謝いたします：

- **krishna3163** — メンテナー 兼 リード開発者 (Maintainer & Lead Developer)

また、OmniShizuku の設計に多大な影響とインスピレーションを与え、一部のコードやAPIが統合されている以下のオープンソースプロジェクトに深く感謝いたします：

- **Shizuku** (rikkaw) — コアとなるShizukuフレームワークおよびBinder API
- **Dhizuku** — Device Owner サービスの統合
- **shevery** — Material 3デザイン、自動起動、TCPモード、モジュール機能、ターミナル、AI統合
- **Shizuku Next** — 自動ペアリング、起動方式選択、ウォッチドッグ、App-Ops/ファイアウォール管理
- **Essentials (sameerasw)** — エッセンシャル機能モジュール（Dynamic Island、Smart Pixels、Caffeinate など）
- **App Ops** — 権限管理の設計パターン
- **Active Launcher** — 自動化ショートカット機能
- **EnforceDoze** — Dozeスリープ自動化パターン
- **Xhizuku** — Material 3 Expressive UI、初回導入ウィザード、サーバー監視機能
- **Stellar** — 軽量ターミナル実装
- **ShizukuPlus** — 開発者向け拡張APIサーフェス
- **Porter** — 最小限のShizukuフォーク設計リファレンス
- **Shizako** — ドロップイン置換マネージャーのリファレンス
- **DFRoot (diabl0w)** — 組み込み機能としての DirtyFrag (CVE-2026-43284) カーネルエクスプロイト統合 (GPL-2.0)

---

## クイック設定タイル

OmniShizuku には以下のクイック設定パネル用タイルが含まれています：
- Caffeinate（画面のスリープ防止）
- ワイヤレスADB切り替え
- Shizuku サーバーの起動 / 停止
- ステルスモード切り替え

通知シェードを下に引き下げ、鉛筆アイコン（編集）をタップしてタイルを追加してください。

---

## セキュリティに関する注意

本アプリは Shizuku を介して高いシステム権限を要求します。安全のため責任を持ってご利用ください：

- 信頼できないサードパーティ製アプリには権限を与えないでください
- ワイヤレスデバッグやADBは端末の内部システムインターフェースを外部に露出させます
- Root権限は Android 本来のセキュリティサンドボックスをバイパスします
- デバイス管理者（Device Owner）モードはシステム制限（初期化制限やアプリのアンインストール制限など）を伴う場合があります

詳細は初回起動時の免責事項をご確認ください。

---

## ライセンス

本ソフトウェアは Apache License 2.0 の下で公開されています。詳細は [LICENSE](LICENSE) をご覧ください。

---

## 免責事項

OmniShizuku は現状有姿（as-is）で提供され、いかなる明示または黙示の保証も行いません。自己責任のもとでご使用ください。データの損失、端末保証の無効化、またはサードパーティ製アプリの異常動作について、開発者は一切の責任を負いません。
