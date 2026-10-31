# OmniShizuku

OmniShizuku — бұл Shizuku, ADB және Dhizuku жұмыс процестеріне арналған бастапқы коды ашық Android утилитасы. Ол құрылғыны басқару, автоматтандыру, оңтайландыру және т.б. арналған модульдері бар Shizuku қызметі арқылы жүйелік деңгейдегі API қатынасын қамтамасыз етеді.

[![GitHub](https://img.shields.io/badge/GitHub-krishna3163/OmniShizuku-blue?logo=github)](https://github.com/krishna3163/OmniShizuku)
[![License: Apache-2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Android-5.0%2B-green?logo=android)](https://developer.android.com)

🌐 **Documentation available in:** [English](./README.md) • [हिन्दी](./README.hi.md) • [Español](./README.es.md) • [Français](./README.fr.md) • [中文（简体）](./README.zh.md) • [中文（繁體）](./README.zh_tw.md) • [Русский](./README.ru.md) • [العربية](./README.ar.md) • [日本語](./README.ja.md) • [Português](./README.pt.md) • [Қазақша](./README.kk.md) • [Qazaqşa](./README.kk-Latn.md)

---
<div align="center">

<img src="manager/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" width="128" height="128" alt="OmniShizuku Logo">

# OmniShizuku

**A powerful, feature-rich fork of Shizuku with enhanced modules, modern UI, and extended system control capabilities.**

[![GitHub](https://img.shields.io/badge/GitHub-krishna3163%2FOmniShizuku-blue?logo=github&style=for-the-badge)](https://github.com/krishna3163/OmniShizuku)
[![License: Apache-2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=for-the-badge)](LICENSE)
[![Android](https://img.shields.io/badge/Android-5.0%2B-green?logo=android&style=for-the-badge)](https://developer.android.com)
[![API](https://img.shields.io/badge/API%20Level-21%2B-brightgreen?style=for-the-badge)](https://developer.android.com/guide/topics/manifest/uses-sdk-element)
[![Shizuku](https://img.shields.io/badge/Powered%20by-Shizuku%20API-orange?style=for-the-badge)](https://github.com/RikkaApps/Shizuku)

🌐 **Documentation available in:** [English](./README.md) • [हिन्दी](./README.hi.md) • [Español](./README.es.md) • [Français](./README.fr.md) • [中文（简体）](./README.zh.md) • [中文（繁體）](./README.zh_tw.md) • [Русский](./README.ru.md) • [日本語](./README.ja.md) • [العربية](./README.ar.md)

## Мүмкіндіктер

### Негізгі модульдер хабы (Essentials Hub)
- **Dynamic Island** — Тікелей медиа, батарея және хабарландыруларға арналған камера ойығының қалқымалы оверлейі
- **Smart Pixels (AMOLED қуатын үнемдеу)** — Экран энергиясын 15–44% үнемдеу үшін OLED субпиксель торының 25–75%-ын өшіреді
- **Дисплей баптауышы (Display Tweaker)** — Ажыратымдылықты, экран тығыздығын (DPI) және жаңару жиілігін қайта анықтау
- **Мәжбүрлі қараңғы режим (Force Dark Mode)** — Барлық орнатылған қолданбаларда қараңғы тақырыпты мәжбүрлеп қолдану
- **Батарея орталығы және қорғаушысы** — Батареяның күйін, зарядтау циклдарын, температурасын нақты уақытта қадағалау және ұзақ қызмет етуін қорғау
- **Қалта режимінің сақшысы (Pocket Mode Guard)** — Жақындау және жарық сенсорлары арқылы қалтада кездейсоқ басылудан қорғау
- **Экран қауіпсіздігі және DRM бұғаттауын ашу** — Банк және стриминг қолданбаларында скриншот жасау үшін FLAG_SECURE шектеуін айналып өту
- **Рұқсаттар және AppOps** — Артықшылықты рұқсаттарды басқару және фондық қызметтерді шектеу
- **SystemUI реттегіші** — Күй жолағы белгішелерінің қара тізімі, батарея белгішесінің стильдері және иммерсивті режим
- **Axeron ойын және процессор оңтайландырғышы** — Өнімділік профильдері, нөлдік анимация кідірісі және кэшті тазалау
- **Мәжбүрлі Doze режимі** — Экран өшкен кезде терең ұйқы режимін (deep idle) автоматты түрде күшейту
- **Кеңейтілген қуат басқару элементтері** — Shell арқылы қайта іске қосу, recovery, bootloader, сөндіру және ұйқы батырмасы
- **Жеке DNS реттегіші** — VPN қажет етпейтін шифрланған DNS параметрлері (Cloudflare, AdGuard, Quad9, Google)
- **Оператор шектеулерін айналып өту** — Оператор бұғаттаған интернет таратуды (хотспот) ашу
- **Сақталған Wi-Fi құпия сөздері** — Құрылғыда сақталған Wi-Fi желілері мен құпия сөздерін оқу және көрсету
- **Shut Up! Медиа дыбысын өшіру** — Әр қолданба үшін бөлек медиа дыбысын өшіру
- **Аудио ағындарын басқару** — Әр арнаның дыбыс деңгейін бөлек басқару
- **Дыбыс түймелерімен трек ауыстыру** — Экран өшірулі кезде дыбыс деңгейі батырмалары арқылы әндерді ауыстыру
- **Хабарландыру жиегінің жарығы** — Хабарландыру түскен кезде экран жиектеріндегі неон жарқылы
- **Автоматты мазаламау режимі (Zen Mode)** — Автоматтандырылған кесте бойынша «Мазаламау» режимін қосу
- **Caffeinate** — Тоқтату хабарландыруы және жылдам параметрлер плиткасы арқылы экранды өшпейтін күйде сақтау
- **Фотосурет сутаңбасы және EXIF студиясы** — Leica классикалық жақтауы және Hasselblad стилі, нақты уақыттағы EXIF жолағы
- **Әмбебап Android тазартқышы (UAD)** — Қауымдастық қауіпсіздік рейтингі бойынша қажетсіз қолданбаларды өшіру және 1 түрту арқылы қалпына келтіру
- **Қолданбаларды қатырушы (App Freezer)** — Фондық бағдарламаларды жоймай-ақ лезде мұздату
- **Артықшылықты файл шолғышы** — Қорғалған жүйелік файлдар мен қалталарды көру
- **Пакет орнатушы Pro** — Бөлінген APKs, APKS, XAPK пакеттерін орнату

### Қосымша кіріктірілген модульдер
- **Theme Patcher** — Жүйе тақырыбын өзгерту және баптау
- **Sound Master** — Кеңейтілген дыбысты баптау
- **Mixed Audio** — Дыбыс ағындарын араластыру және бағыттау
- **LookBack** — Экранды бейнеге түсіру және жылдам көру
- **Universal PiP** — Кез келген қолданба үшін «Сурет ішіндегі сурет» режимі
- **Intent Shell** — Интенттер арқылы shell командаларын орындау
- **Local Shell** — Кірістірілген терминал эмуляторы

### Сымсыз ADB автоматты PIN коды (Қолжетімділік)
- **Өшірулі (Off)** — Жұптастыру PIN кодын қолмен енгізу
- **Қолмен (Manual)** — Менеджер сұхбат терезесі пайда болған кезде ескертеді
- **Автоматты (Automatic)** — Қолжетімділік қызметі сақталған PIN кодты «Құрылғымен жұптастыру» терезесіне автоматты түрде енгізеді
- Тек пайдаланушы таңдауымен — Баптауларда қосқанға дейін фонда ештеңе жұмыс істемейді

### Өнімділік пен тұрақтылық
- **Үнемді қозғалыс бұлыңғырлығы (Lazy motion blur)** — Кадрларды таңдау тек экранды айналдыру кезінде ғана орындалады, процессор жүктемесін үнемдейді
- **Crashlytics және ұсталмаған қателер өңдеушісі** — Қолданба кенет жабылып қалмай, қателіктер журналға тіркеледі
- **Қорғанысты Shizuku Binder қабығы** — Null-қауіпсіз байланыс Realme және басқа OEM құрылғыларында NPE қателерін болдырмайды
- **DeviceInfo шектеулі жүктелуі** — dumpsys сұрауларына 20 секундтық тайм-аут қойылып, жүйе қатып қалмай алынған деректер көрсетіледі

### Алғашқы іске қосу нұсқаулығы
- 6 ескерту экраны (артықшылықтар, Device Owner әсерлері, Root қауіптері, үшінші тарап әрекеттері, лицензия, банк қолданбаларының ескертулері)
- 5 секундтық кері санақ таймері және «Оқыдым және келісемін» құсбелгісі бар келісім
- Тілді таңдау (жүйелік + 12 тіл)
- Тақырыпты таңдау (жүйе бойынша / қараңғы / ашық / AMOLED таза қара / жүйе түсі)
- Белсендіру әдісі (сымсыз жөндеу / root / компьютерге қосылу)

---

## Басқа Shizuku форктарымен салыстыру

| Жоба | Автоқосылу | TCP режимі | Dhizuku | Модульдерді қолдау | Жасырын режим | Терминал | AI интеграциясы | Expressive UI | Бастапқы нұсқаулық | Лицензия |
|---------|-----------|----------|---------|----------------|---------|----------|----------------|---------------|------------|---------|
| **OmniShizuku** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | Apache-2.0 |
| Porter | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | Apache-2.0 |
| shevery | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | Material 3 | ❌ | Apache-2.0 |
| Shizako | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | Apache-2.0 |
| Shizuku (thedjchi форкы) | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | Apache-2.0 |
| Shizuku Next | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | Material 3 | ❌ | Apache-2.0 |
| ShizukuPlus | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | Apache-2.0 |
| Stellar | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | MPL-2.0 |
| Xhizuku | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | Material 3 Expressive | ✅ | Apache-2.0 |

---

## Талаптар

- Android 5.0 (API 21) немесе одан жоғары
- Жұмыс істеп тұрған Shizuku қызметі (сымсыз жөндеу, root немесе ADB арқылы)
- Қолданбаға берілген Shizuku рұқсаты

---

## Орнату

1. APK файлын [GitHub Releases](https://github.com/krishna3163/OmniShizuku/releases) бөлімінен жүктеп орнатыңыз
2. Құрылғыңызда Shizuku қызметін қосыңыз (сымсыз жөндеу немесе ADB)
3. OmniShizuku қолданбасына Shizuku рұқсатын беріңіз
4. Алғаш рет іске қосқанда, бастапқы баптау қадамдарын аяқтаңыз

---

## Бастапқы кодтан құрастыру

```bash
git clone git@github.com:krishna3163/OmniShizuku.git
cd OmniShizuku
./gradlew :manager:assembleDebug
```

Құрастырылған дебаг APK файлы мына мекенжайда болады: `manager/build/outputs/apk/debug/manager-debug.apk`.

---

## Үлес қосушылар мен алғыстар

GitHub желісінде OmniShizuku жобасына үлес қосқан барлық жандарға алғыс айтамыз:

- **krishna3163** — Жоба жетекшісі және негізгі әзірлеуші (Maintainer & Lead Developer)

Сондай-ақ келесі бастапқы коды ашық жобаларға шынайы ризашылығымызды білдіреміз:

- **Shizuku** (rikkaw) — Негізгі Shizuku фреймворкі және Binder API
- **Dhizuku** — Device Owner қызметімен интеграция
- **shevery** — Material 3 дизайны, автоқосылу, TCP режимі, модульдер қолдауы, терминал және жасанды интеллект
- **Shizuku Next** — Автоматты жұптастыру, іске қосу әдісін таңдау, watchdog және App-Ops/брандмауэр менеджері
- **Essentials (sameerasw)** — Негізгі пайдалы модульдер (Dynamic Island, Smart Pixels, Caffeinate және т.б.)
- **App Ops** — Рұқсаттарды басқару үлгілері
- **Active Launcher** — Автоматтандыру таңбашалары
- **EnforceDoze** — Doze ұйқы режимін автоматтандыру тәсілдері
- **Xhizuku** — Material 3 Expressive интерфейсі, баптау шебері және сервер мониторы
- **Stellar** — Қарапайым терминал шешімі
- **ShizukuPlus** — Әзірлеушілерге арналған кеңейтілген API
- **Porter** — Ықшам Shizuku форк үлгісі
- **Shizako** — Тікелей ауыстыру менеджерінің архитектуралық анықтамасы
- **DFRoot (diabl0w)** — Кірістірілген мүмкіндік ретіндегі DirtyFrag (CVE-2026-43284) ядро эксплойтының интеграциясы (GPL-2.0)

---

## Жылдам параметрлер тақталары (Quick Settings Tiles)

OmniShizuku хабарландыру пердесіне арналған жылдам баптау белгішелерін ұсынады:
- Caffeinate (экранды қосулы сақтау)
- Сымсыз ADB ауыстырып-қосқышы
- Shizuku серверін іске қосу / тоқтату
- Жасырын режимді (Stealth) қосу/өшіру

Оларды қосу үшін хабарландыру тақтасын төмен түсіріп, өңдеу қарындаш белгішесін түртіңіз.

---

## Қауіпсіздік

Бұл қолданба Shizuku арқылы жоғары жүйелік артықшылықтарды сұрайды. Оны жауапкершілікпен пайдаланыңыз:

- Сенімсіз немесе бейтаныс қолданбаларға рұқсат бермеңіз
- Сымсыз жөндеу және ADB жүйенің осал интерфейстерін желіге ашады
- Root рұқсаты Android қауіпсіздік үлгісін айналып өтеді
- Құрылғы иесі (Device Owner) режимі белгілі бір шектеулер әкелуі мүмкін (зауыттық қалпына келтіруге тыйым салу, қолданбаларды өшіруді шектеу)

Толық ақпаратты бірінші іске қосу кезіндегі ескертулерден оқыңыз.

---

## Лицензия

Apache License 2.0 бойынша лицензияланған. Толық ақпаратты [LICENSE](LICENSE) файлынан қараңыз.

---

## Жауапкершіліктен бас тарту

OmniShizuku «сол күйінде» (as-is), ешқандай тікелей немесе жанама кепілдіксіз ұсынылады. Барлық тәуекелдерді пайдаланушы өз мойнына алады. Біз деректердің жоғалуына, құрылғы кепілдігінің күшін жоюына немесе үшінші тарап қолданбаларының әрекетіне жауапты емеспіз.
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

</div>
