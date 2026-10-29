# OmniShizuku

OmniShizuku is an open-source Android utility for Shizuku, ADB, and Dhizuku workflows. It provides system-level API access through the Shizuku service, with modules for device management, automation, optimizations, and more.

[![GitHub](https://img.shields.io/badge/GitHub-krishna3163/OmniShizuku-blue?logo=github)](https://github.com/krishna3163/OmniShizuku)
[![License: Apache-2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Android-5.0%2B-green?logo=android)](https://developer.android.com)

---

## Features

### Essentials Hub
- **Dynamic Island** — Floating punch-hole pill overlay for live media, battery & alerts
- **Smart Pixels (AMOLED Saver)** — Disables 25–75% OLED subpixel grid to save 15–44% display power
- **Display Tweaker** — Resolution, screen density (DPI), and refresh rate overrides
- **Force Dark Mode** — Forces dark themes across all installed applications
- **Battery Center & Guard** — Real-time battery health, cycle count, temperature & longevity guard
- **Pocket Mode Guard** — Proximity & light sensor protection against accidental touches
- **Screen Security & DRM Unlock** — Bypasses FLAG_SECURE for screenshots in banking and streaming apps
- **Permissions & AppOps** — Privileged permission controller and background service limits
- **SystemUI Tuner** — Status bar icon blacklist, battery icon styles & immersive mode
- **Axeron Gaming & CPU Optimizer** — Performance profiles, zero animation latency & cache trim
- **Enforce Doze** — Aggressive standby deep idle automation on screen off
- **Advanced Power Controls** — Reboot, recovery, bootloader, shutdown & sleep key via shell
- **Private DNS Controller** — Encrypted zero-VPN DNS presets (Cloudflare, AdGuard, Quad9, Google)
- **Carrier Tethering Bypass** — Unlocks carrier-restricted hotspot
- **Saved Wi-Fi Passwords** — Reads saved Wi-Fi access points and passkeys
- **Shut Up! Media Muter** — Per-app media audio muting
- **Audio Stream Master** — Independent volume stream control
- **Volume Key Track Skipper** — Skip tracks using volume buttons when screen is off
- **Notification Edge Lighting** — Neon edge glow animation on notifications
- **Auto DND (Zen Mode)** — Automated Do-Not-Disturb scheduling
- **Caffeinate** — Keep the display awake with stop notification and Quick Settings tile
- **Photo Watermark & EXIF Studio** — Luxury Leica frame & Hasselblad branding with live EXIF strip
- **Universal Android Debloater (UAD)** — Community safety-ranked package removal & 1-tap restore
- **App Freezer** — Instantly freezes background apps
- **Privileged File Explorer** — Browse protected system paths
- **Package Installer Pro** — Installs split APKs, APKS, XAPK

### Wireless ADB Auto-PIN (Accessibility)
- **Off** — Type the pairing PIN yourself
- **Manual** — Manager tells you when the dialog is up
- **Automatic** — Accessibility service enters the stored PIN into the "Pair with device" dialog
- Opt-in only — nothing runs until you enable it in Settings

### Performance & Stability
- **Lazy motion blur** — Frame-sampler loops run only during scrolling, eliminating per-frame CPU burn
- **Crashlytics + uncaught handler** — Crashes are recorded instead of silently closing the app
- **Defensive Shizuku binder** — Null-safe binder access prevents Realme/OEM NPE crashes
- **DeviceInfo bounded loading** — 20s timeout on dumpsys queries, shows partial data instead of stuck spinner

### First-Launch Onboarding
- 6 disclaimer screens (privileges, device owner side effects, root risks, third-party behavior, license, bank app warnings)
- Agreement with 5-second countdown timer and "I have read and agree" checkbox
- Language selection (system + 12 languages)
- Theme selection (follow system / dark / light / black night AMOLED / use system color)
- Activation method (wireless debugging / root / connect to computer)

---

## Requirements

- Android 5.0 (API 21) or higher
- Shizuku service running (via wireless debugging, root, or ADB)
- Shizuku permission granted to the app

---

## Installation

1. Install the APK from [GitHub Releases](https://github.com/krishna3163/OmniShizuku/releases)
2. Enable Shizuku on your device (wireless debugging or ADB)
3. Grant Shizuku permission to OmniShizuku
4. On first launch, complete the onboarding flow

---

## Building from Source

```bash
git clone git@github.com:krishna3163/OmniShizuku.git
cd OmniShizuku
./gradlew :manager:assembleDebug
```

The debug APK will be at `manager/build/outputs/apk/debug/manager-debug.apk`.

---

## Security

This app requests elevated system privileges through Shizuku. Use responsibly:

- Do not grant access to untrusted applications
- Wireless debugging and ADB expose system interfaces
- Root access bypasses Android's security model
- Device owner mode may have side effects (factory reset restrictions, app removal limits)

See the first-launch disclaimer for full details.

---

## License

Apache License 2.0. See [LICENSE](LICENSE) for details.

---

## Disclaimer

OmniShizuku is provided as-is, without warranty. Use at your own risk. We are not responsible for data loss, warranty voiding, or third-party app behavior.
