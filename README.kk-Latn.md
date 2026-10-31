# OmniShizuku

OmniShizuku — bũl Shizuku, ADB jäne Dhizuku jumys procesterine arnalğan bastapqy kody aşyq Android utilitası. Ol qūrylğyny basqaru, avtomattandyru, oñtaılandyru jäne t.b. arnalğan modülderi bar Shizuku qyzmeti arqyly jüielik deñgeıdegi API qatynasyn qamtamasyz etedi.

[![GitHub](https://img.shields.io/badge/GitHub-krishna3163/OmniShizuku-blue?logo=github)](https://github.com/krishna3163/OmniShizuku)
[![License: Apache-2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Android-5.0%2B-green?logo=android)](https://developer.android.com)

🌐 **Documentation available in:** [English](./README.md) • [हिन्दी](./README.hi.md) • [Español](./README.es.md) • [Français](./README.fr.md) • [中文（简体）](./README.zh.md) • [中文（繁體）](./README.zh_tw.md) • [Русский](./README.ru.md) • [العربية](./README.ar.md) • [日本語](./README.ja.md) • [Português](./README.pt.md) • [Қазақша](./README.kk.md) • [Qazaqşa](./README.kk-Latn.md)

---

## Mümkindikter

### Negizgi modülder haby (Essentials Hub)
- **Dynamic Island** — Tikeleı media, batareya jäne habarlandyrularğa arnalğan kamera oıyğynyñ qalqyma overleıi
- **Smart Pixels (AMOLED quatyn ünemdeu)** — Ekran energiyasyn 15–44% ünemdeu üşin OLED subpiksel torynyñ 25–75%-yn öşiredi
- **Displeı baptauyşy (Display Tweaker)** — Ajyratymdylyqty, ekran tyğyzdyğyn (DPI) jäne jañaru jiiligin qaıta anyqtau
- **Mäjbürli qarañğy rejim (Force Dark Mode)** — Barlyq ornatylğan qoldanbalarda qarañğy taqyrypty mäjbürlep qoldanu
- **Batareya ortalyğy jäne qorğauşysy** — Batareyanyñ küıin, zaryadtau tsikldaryn, temperaturasyn naqty ýaqytta qadağalau jäne ūzaq qyzmet etuin qorğau
- **Qalta rejiminiñ saqşysy (Pocket Mode Guard)** — Jaqyndau jäne jaryq sensorlary arqyly qaltada kezdeısoq basyludan qorğau
- **Ekran qauipsizdigi jäne DRM bũğattauyn aşu** — Bank jäne striming qoldanbalarynda skrinşot jasau üşin FLAG_SECURE şekteuin aınalyp ötu
- **Rūqsathtar jäne AppOps** — Artyqşylyqty rūqsathtardy basqaru jäne fondyq qyzmetterdi şekteu
- **SystemUI rettegişi** — Küi jolağy belgişeleriniñ qara tizimi, batareya belgişesiniñ stilderi jäne immersivti rejim
- **Axeron oıyn jäne processor oñtaılandyrğyşy** — Önimdilik profilderi, nöldik animatsiya kidirisi jäne keshti tazalau
- **Mäjbürli Doze rejimi** — Ekran öşken kezde tereñ ūıqy rejimin (deep idle) avtomattandyrylğan türde küşeiytu
- **Keñeıtilgen quat basqaru elementteri** — Shell arqyly qaıta iske qosu, recovery, bootloader, söndiru jäne ūıqy batyrmasy
- **Jeke DNS rettegişi** — VPN qajet etpeıtin şifrlanğan DNS parametrleri (Cloudflare, AdGuard, Quad9, Google)
- **Operator şekteulerin aınalyp ötu** — Operator bũğattağan internet taratudy (hotspot) aşu
- **Saqtalğan Wi-Fi qūpiya sözderi** — Qūrylğyda saqtalğan Wi-Fi jelileri men qūpiya sözderin oqu jäne körsetu
- **Shut Up! Media dybysyn öşiru** — Är qoldanba üşin bölek media dybysyn öşiru
- **Audio ağyndaryn basqaru** — Är arnanyñ dybys deñgeıin bölek basqaru
- **Dybys tüımelerimen trek auystyru** — Ekran öşiruli kezde dybys deñgeıi batyrmalary arqyly änderdi auystyru
- **Habarlandyru jiegiñ jaryğy** — Habarlandyru tüsken kezde ekran jiekterindegi neon jarqyly
- **Avtomatty mazalamaú rejimi (Zen Mode)** — Avtomattandyrylğan keste boıynşa «Mazalamaú» rejimin qosu
- **Caffeinate** — Toqtatu habarlandyruy jäne jyldam parametrler plitkasy arqyly ekrandy öşpeitin küıde saqtau
- **Fotosuret sutañbasy jäne EXIF studiyasy** — Leica klassikalyq jaqtauy jäne Hasselblad stili, naqty ýaqyttağy EXIF jolağy
- **Ämbebap Android tazartqyşy (UAD)** — Qauymdastyq qauipsizdik reıtingi boıynşa qajetsiz qoldanbalardy öşiru jäne 1 türtu arqyly qalpyna keltiru
- **Qoldanbalardy qatyruşy (App Freezer)** — Fondyq bağdarlamalardy joımaı-aq lezde mūzdatu
- **Artyqşylyqty faıl şolğyşy** — Qorğalğan jüielik faıldar men qaltalardy köru
- **Paket ornatuşy Pro** — Bölip jasalğan APKs, APKS, XAPK paketterin ornatu

### Qosymşa kiriktirilgen modülder
- **Theme Patcher** — Jüie taqyrybyn özgertu jäne baptau
- **Sound Master** — Keñeıtilgen dybysty baptau
- **Mixed Audio** — Dybys ağyndaryn aralastyru jäne bağyttau
- **LookBack** — Ekrandy beınege tüsiru jäne jyldam köru
- **Universal PiP** — Kez kelgen qoldanba üşin «Suret işindegi suret» rejimi
- **Intent Shell** — Intentter arqyly shell komandalaryn oryndau
- **Local Shell** — Kiristirilgen terminal emulyatory

### Symsyz ADB avtomatty PIN kody (Qoljetimdilik)
- **Öşiruli (Off)** — Jūptastyru PIN kodyn qolmen engizu
- **Qolmen (Manual)** — Menedjer sūhbat terezesi paıda bolğan kezde eskertedi
- **Avtomatty (Automatic)** — Qoljetimdilik qyzmeti saqtalğan PIN kodty «Qūrylğymen jūptastyru» terezesine avtomatty türde engizedi
- Tek paıdalanuşy tañdauymen — Baptaularda qosqanğa deıin fonda eşteñe jūmys istemeıdi

### Önimdilik pen turaqtylyq
- **Ünemdi qozğalys bulyñğyrlyğy (Lazy motion blur)** — Kadrlardy tañdau tek ekrandy aınaldyru kezinde ğana oryndalady, processor jüktemesin ünemdeıdi
- **Crashlytics jäne ūstalmağan qateler öñdeuşisi** — Qoldanba kenet jabylyp qalmaı, qatelikter jurnaldarda tolyq saqtalady
- **Qorğanysty Shizuku Binder qabyğy** — Null-qauipsiz baılanys Realme jäne basqa OEM qūrylğylarynda NPE qatelerin boldyrmaıdy
- **DeviceInfo şekteuli jüktelui** — dumpsys sūraularyna 20 sekundyq taım-aut qoıylyp, jüie qatyp qalmaı alynğan derekter körsetiledi

### Alğaşqy iske qosu nusqaulyğy
- 6 eskertu ekrany (artyqşylyqtar, Device Owner äserleri, Root qauipteri, üşinşi tarap äreketteri, litsenziya, bank qoldanbalarynyñ eskertuleri)
- 5 sekundyq keri sanaq taımeri jäne «Oqydym jäne kelisemin» qūsqabelgisi bar kelisim
- Tildi tañdau (jüielik + 12 til)
- Taqyrypty tañdau (jüie boıynşa / qarañğy / aşyq / AMOLED taza qara / jüie tüsi)
- Belsendiru ädisi (symsyz jöndeu / root / kompyuterge qosylu)

---

## Basqa Shizuku forktarymen salystyru

| Joba | Avtoqosylu | TCP rejimi | Dhizuku | Modülderdi qoldau | Jasyryn rejim | Terminal | AI integratsiyasy | Expressive UI | Bastapqy nusqaulyq | Litsenziya |
|---------|-----------|----------|---------|----------------|---------|----------|----------------|---------------|------------|---------|
| **OmniShizuku** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | Apache-2.0 |
| Porter | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | Apache-2.0 |
| shevery | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | Material 3 | ❌ | Apache-2.0 |
| Shizako | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | Apache-2.0 |
| Shizuku (thedjchi forky) | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | Apache-2.0 |
| Shizuku Next | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | Material 3 | ❌ | Apache-2.0 |
| ShizukuPlus | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | Apache-2.0 |
| Stellar | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | MPL-2.0 |
| Xhizuku | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | Material 3 Expressive | ✅ | Apache-2.0 |

---

## Talaptar

- Android 5.0 (API 21) nemese odan joğary
- Jūmys istep tūrğan Shizuku qyzmeti (symsyz jöndeu, root nemese ADB arqyly)
- Qoldanbağa berilgen Shizuku rūqsathtyğy

---

## Ornatu

1. APK faılyn [GitHub Releases](https://github.com/krishna3163/OmniShizuku/releases) böliminen jüktep ornatyñyz
2. Qūrylğyñyzda Shizuku qyzmetin qosyñyz (symsyz jöndeu nemese ADB)
3. OmniShizuku qoldanbasyna Shizuku rūqsatyn beriñiz
4. Alğaş ret iske qosqanda, bastapqy baptau qadamdaryn ayaqtañyz

---

## Bastapqy kodtan qurastyru

```bash
git clone git@github.com:krishna3163/OmniShizuku.git
cd OmniShizuku
./gradlew :manager:assembleDebug
```

Qūrastyrylğan debag APK faıly myna mekenjaıda bolady: `manager/build/outputs/apk/debug/manager-debug.apk`.

---

## Üles qosuşylar men alğystar

GitHub jelisinde OmniShizuku jobasyna üles qosqan barlyq jandarğa alğys aıtamyz:

- **krishna3163** — Joba jetekşisi jäne negizgi äzirleuşi (Maintainer & Lead Developer)

Sondaı-aq kelesi bastapqy kody aşyq jobalarğa şynaıy rızaşylyğymyzdy bildiremiz:

- **Shizuku** (rikkaw) — Negizgi Shizuku freımvorki jäne Binder API
- **Dhizuku** — Device Owner qyzmetimen integratsiya
- **shevery** — Material 3 dızaıny, avtoqosylu, TCP rejimi, modülder qoldauy, terminal jäne jandy intellekt
- **Shizuku Next** — Avtomatty jūptastyru, iske qosu ädisin tañdau, watchdog jäne App-Ops/brandmauer menedjeri
- **Essentials (sameerasw)** — Negizgi paıdaly modülder (Dynamic Island, Smart Pixels, Caffeinate jäne t.b.)
- **App Ops** — Rūqsathtardy basqaru ülgileri
- **Active Launcher** — Avtomattandyru tañbalary
- **EnforceDoze** — Doze ūıqy rejimin avtomattandyru täsilderi
- **Xhizuku** — Material 3 Expressive interfeısi, baptau şeberi jäne server monıtory
- **Stellar** — Qarapayım terminal şeşimi
- **ShizukuPlus** — Äzirleuşilerge arnalğan keñeıtilgen API
- **Porter** — Yqşam Shizuku fork ülgisi
- **Shizako** — Tikeleı auystyru menedjeriniñ arhitekturalyq anyqtamasy
- **DFRoot (diabl0w)** — Kiristirilgen mümkindik retindegi DirtyFrag (CVE-2026-43284) yadro eksploıtynyñ integratsiyasy (GPL-2.0)

---

## Jyldam parametrler taqtalary (Quick Settings Tiles)

OmniShizuku habarlandyru perdesine arnalğan jyldam baptau belgişelerin ūsynady:
- Caffeinate (ekrandy qosuly saqtau)
- Symsyz ADB auystyryp-qosqyşy
- Shizuku serverin iske qosu / toqtatu
- Jasyryn rejimdi (Stealth) qosu/öşiru

Olardy qosu üşin habarlandyru taqtasyn tömen tüsirip, öñdeu qaryndaş belgişesin türtiñiz.

---

## Qauipsizdik

Būl qoldanba Shizuku arqyly joğary jüielik artyqşylyqtardy sūraıdy. Ony jauapkerşilikpen paıdalanyñyz:

- Senimsiz nemese beıtanys qoldanbalarğa rūqsat bermeñiz
- Symsyz jöndeu jäne ADB jüieniñ osal interfeısterin jelige aşady
- Root rūqsaty Android qauipsizdik ülgisin aınalyp ötedi
- Qūrylğy iesi (Device Owner) rejimi belgili bir şekteuler äkelui mümkin (zauyttyq qalpyna keltiruge tyıyım salu, qoldanbalardy öşirudi şekteu)

Tolyq aqparatty birinşi iske qosu kezindegi eskertulerden oqyñyz.

---

## Litsenziya

Apache License 2.0 boıynşa litsenziyalanğan. Tolyq aqparatty [LICENSE](LICENSE) faılynan qarañyz.

---

## Jauapkerşilikten bas tartu

OmniShizuku «sol küıinde» (as-is), eşqandaı tikeleı nemese janama kepildiksiz ūsynylady. Barlyq täuekelderdi paıdalanuşy öz moınyna alady. Biz derekterdiñ joğaluyna, qūrylğy kepildiginiñ küşin joyuyna nemese üşinşi tarap qoldanbalarynyñ äreketine jauapty emespiz.
