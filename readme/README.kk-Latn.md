<div align="center">

<img src="../manager/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" width="128" height="128" alt="OmniShizuku Logo">

# OmniŞidzuku

**Jaqsartylğan modulderi, zamanaui interfeisi jäne keñeitilgen jüieni basqaru mümkindikteri bar quatty, mümkindikteri mol Shizuku şanyşqysy.**

[![GitHub](https://img.shields.io/badge/GitHub-krishna3163%2FOmniShizuku-blue?logo=github&style=for-the-badge)](https://github.com/krishna3163/OmniShizuku)
[![Litsenziya: Apache-2.0](https://img.shields.io/badge/Litsenziya-Apache%202.0-blue.svg?style=for-the-badge)](../LICENSE)
[![Android](https://img.shields.io/badge/Android-5.0%2B-green?logo=android&style=for-the-badge)](https://developer.android.com)
[![API](https://img.shields.io/badge/API%20Level-21%2B-brightgreen?style=for-the-badge)](https://developer.android.com/guide/topics/manifest/uses-sdk-element)
[![Shizuku](https://img.shields.io/badge/Powered%20by-Shizuku%20API-orange?style=for-the-badge)](https://github.com/RikkaApps/Shizuku)

🌐 **Documentation available in:** [English](../README.md) • [हिन्दी](./README.hi.md) • [Español](./README.es.md) • [Français](./README.fr.md) • [中文（简体）](./README.zh.md) • [中文（繁體）](./README.zh_tw.md) • [Русский](./README.ru.md) • [العربية](./README.ar.md) • [日本語](./README.ja.md) • [Português](./README.pt.md) • [Қазақша](./README.kk.md) • [Qazaqşa](./README.kk-Latn.md)

</div>

---

## 📖 Mazmūny

- [Şolu](#-şolu)
- [Skrinşottar](#-skrinşottar)
- [OmniShizuku ne üşin qajet?](#-why-use-omnishizuku)
- [Negizgi mümkindikter](#-kilt-mümkindikter)
  - [Zerthana - Keñeitilgen jüielik qūraldar (40 Qūral)](#labs---advanced-jüie qūraldary)
  - [Modulder jüiesi](#modules-jüie)
  - [Qoñyraulardy jazu (ShizuCallRecorder Integration)](#call-recording-shizucallrecorder-integration)
  - [AI jäne äzirleuşi mümkindikteri](#ai--äzirleuşi mümkindikteri)
  - [UI/UX jaqsartulary](#uiux-jetildiruler)
- [Mümkindikterdi salystyru](#-funktsiyany-basqa-şizuku-şanyşqylarymen-salystyru)
- [Jüie talaptary](#-jüie talaptary)
- [Ornatu](#-ornatu)
- [Bastau](#-bastau)
- [Qūrylys közinen](#-Building-from-source)
- [Arhitektura jäne qauipsizdik](#-arhitektura--qauipsizdik)
- [Rūqsattar tüsindirildi](#-rūqsattar-tüsindirildi)
- [Üles qosu](# üles qosu)
- [Nesieler jäne alğys](#-kreditter--aqpyndamalar)
- [Litsenziya](#-litsenziya)
- [Qoldau](#-qoldau)

---

## 🔍 Şolu

**OmniSidzuku** — tüpnūsqa [Shizuku](https://github.com/RikkaApps/Shizuku) jobasynyñ keñeitilgen, aşyq kody bar şanyşqysy. Ol praktikalyq utilitalardyñ keñ jiyntyğymen, jüielik tüzetulermen, qūpiyalylyqty basqaru elementterimen jäne intuitivti zamanaui interfeispen biriktiru arqyly Shizuku-nyñ quatty jüielik deñgeidegi API qoljetimdiligin keñeitedi.

🌐 **Documentation available in:** [English](../README.md) • [हिन्दी](./README.hi.md) • [Español](./README.es.md) • [Français](./README.fr.md) • [中文（简体）](./README.zh.md) • [中文（繁體）](./README.zh_tw.md) • [Русский](./README.ru.md) • [العربية](./README.ar.md) • [日本語](./README.ja.md) • [Português](./README.pt.md) • [Қазақша](./README.kk.md) • [Qazaqşa](./README.kk-Latn.md)
**Material Design 3 Expressive** kömegimen jasalğan OmniShizuku paidalanuşylarğa köp jağdaida tolyq tübirlik qatynasty qajet etpei-aq Android qūrylğysyn tolyq basqaruğa mümkindik beredi - Shizuku-nyñ ADB/root köpirlik mümkindikterin paidalanady. Displeidi teñşeu men batareyany oñtailandyrudan qoldanbalardy basqaruğa, delotatsiyağa, qoñyraulardy jazuğa jäne keñeitilgen avtomattandyruğa deiin OmniShizuku sizdiñ barlyğy bir jüiedegi utilitalar jiyntyğy bolyp tabylady.

---

## 📸 Skrinşottar

| **Üi jäne küi** | **Qoldanba rūqsattary** | **ADB modulderi** | **Zerthana katalogy** | **Kompyuter (Terminal jäne AI)** | **Parametrler** |
|:---:|:---:|:---:|:---:|:---:|:---:|
| <img src="../screenshots/home.png" width="165" alt="Home Screen" /> | <img src="../screenshots/manage_apps.png" width="165" alt="Manage Authorized Apps" /> | <img src="../screenshots/modules.png" width="165" alt="ADB Modules" /> | <img src="../screenshots/labs.png" width="165" alt="Labs Catalog" /> | <img src="../screenshots/comput.png" width="165" alt="Comput Console" /> | <img src="../screenshots/settings.png" width="165" alt="Settings Screen" /> |
| Naqty uaqyttağy qyzmet küii jäne symsyz ADB jūptauy | Aq tizim rejimi, egjei-tegjeili rūqsattar jäne tek seans üşin rūqsat | Magisk/KernelSU stilindegi ZIP moduliniñ ornatuşysy jäne menedjeri | 39+ quatty jüielik basqaru elementteri jäne tüzetuler | Gemini/AI generatsiyasy bar artyqşylyqty qabyq terminaly | Qūrylğy iesi, saqtyq köşirmeler, avtomattandyru jäne qauipsizdik konfiguratsiyasy |

---

### 🔬 Tañdauly zerthanalar jäne jüie mümkindikteri

| **Jüielik basqaru elementteri jäne elektr qūraldary** | **Native ShizuWall brandmauer** |
|:---:|:---:|
| <img src="../screenshots/labs_tools.png" width="260" alt="Labs System Tools" /> | <img src="../screenshots/shizuwall.png" width="260" alt="ShizuWall Firewall" /> |
| Doze, aralas audio, jeke DNS, ekran qauipsizdigi jäne DFRoot funktsiyalaryn oryndañyz | Treker skaneri jäne qoldanba erejeleri bar nöldik VPN jergilikti brandmauer |

---

## 💡 Nelikten OmniShizuku paidalanu kerek?

- **Barlyğy bir şeşim** - Birneşe tübirlik/ADB qoldanbalarynyñ qajeti joq. Deblotatsiyadan bastap būrmalardy körsetuge deiin barlyğy bir jerde.
- **Tübir qajet emes** - Wireless ADB (Android 11+) nemese ADB arqyly Shizuku-men jūmys isteidi, būl ony keñirek auditoriyağa qoljetimdi etedi.
- **Zamanaui jäne intuitivtik UI** - Clean Material 3 Telefondar, planşetter, teledidarlar jäne Wear OJ üşin tegis animatsiyalary, dinamikalyq taqyryptary jäne oñtailandyrylğan maketteri bar ekspressivti interfeis.
- **Belsendi jetildirilgen** - Jaña mümkindiktermen, qatelerdi tüzetumen jäne bastapqy Shizuku-dan tys jaqsartularmen tūraqty jañartular.
- **Qūpiyalylyqqa bağyttalğan** - qoldanba rūqsattaryn, qoldanbalardy, jasyru mümkindikterin jäne jüie deñgeiindegi şekteulerdi egjei-tegjeili basqaru.
- **Qauipsiz jäne qaitymdy** - Özgeristerdiñ köpşiligi qaitymdy, delotatsiyağa jäne moduldi basqaruğa arnalğan saqtyq köşirme/qalpyna keltiru mümkindikteri bar.
- **Keñeitiletin modul jüiesi** - kiristirilgen katalog, tabu jäne ornatu qoldauy bar qauymdastyq basqaratyn modulder.
- **Käsiporyn deñgeiindegi tūraqtylyq** - qorğanystyq kodtau täjiribeleri, apatty öñdeu, kütu uaqytynan qorğau şaralary jäne OEM üilesimdiligin tüzetu.

---

## ✨ Negizgi mümkindikter

### Zerthana - Jetildirilgen jüielik qūraldar

Labs katalogynda qoldanbalardy basqaru, jüielik basqaru elementteri jäne utilitalar arasynda taza türde sanattalğan jüie deñgeiindegi 40 qūral bar:

#### 📱 Qoldanbalardy basqaru (8 qūral)
| Erekşelik | Sipattama |
| --- | --- |
| **Qoldanba äreketteri** | Jasyryn qoldanba rūqsattaryn, jūmys uaqytynyñ operatsiyalyq şlyuzderin jäne fondyq artyqşylyqtardy egjei-tegjeili basqaru. |
| **Brandmauer** | Jeli deñgeiindegi qoldanba brandmaueri VPN ūyaşyğyn paidalanbai bir qoldanbağa Wi-Fi jäne mobildi derekterdi bloktaidy. |
| **Avtomatty qosu** | Qūrylğyny jükteu kezinde qoldanbalardyñ oyatuyn nemese qyzmetterdi avtomatty türde bastauyn şekteñiz. |
| **Jasyru rejimderi** | Äzirleuşi optsiyalaryn, USB/symsyz jöndeu, Arnaiy mümkindikter qyzmetterin, Jeke DNS jäne VPN küilerin anyqtaudan jasyryñyz. |
| **Küşti qarañğy rejim** | Tüpnūsqa qarañğy taqyryp qoldauynsyz bolsa da, üşinşi tarap qoldanbalarynda jüie boiynşa mäjbürli qarañğy taqyryp. |
| **Ämbebap Debloater (UAD)** | Qauipsizdik reitingteri, tüsiniktemeleri jäne 1 ret türtu arqyly qalpyna keltirui bar qauymdastyqtyñ qauipsizdik deñgeiindegi bumanyñ debloatory. |
| **Qoldanbaly mūzdatqyş** | JJQ qalpyna keltiru jäne batareyanyñ tausyluyn toqtatu üşin fondyq qoldanbalardy dereu toqtatyñyz jäne toqtatyñyz. |
| **Axeron oiyn oñtailandyrğyşy** | Jüie deñgeiindegi önimdilikti retteu, oiyndy küşeitu, animatsiyany özgertu jäne protsesti oñtailandyru. |

#### ⚙️ Jüieni basqaru elementteri (18 qūral)
| Erekşelik | Sipattama |
| --- | --- |
| **Quat mäziri** | Jüiege, qalpyna keltiruge, jükteuşi/jyldam jükteuge, qauipsiz rejimge artyqşylyqty qaita jükteu nemese lezde öşiru. |
| **Displei tvikeri** | Displei ajyratymdylyğyn qaita anyqtau, ekran tyğyzdyğyn (DPI) özgertu jäne teñşeletin jañartu jiiligin mäjbürleu. |
| **Wi-Fi qūpiya sözderi** | Saqtalğan Wi-Fi jelisiniñ tirkelgi derekterin artyqşylyqty qabyq arqyly tikelei qarañyz, köşiriñiz jäne eksporttañyz. |
| **SystemUI tyuner** | Küi jolağy belgişesiniñ körinuin, batareya belgişesiniñ mänerlerin, sağat sekundtaryn jäne immersivti şarlaudy teñşeñiz. |
| **Enforce Doze (tereñ ūiqy)** | Ekran öşkende birden agressivti Doze tereñ ūiqy rejimin mäjbürleñiz. |
| **Jeke DNS kontrolleri** | Avtomatty qaitaru taimerleri bar şifrlanğan DNS provaiderleri (Cloudflare, AdGuard, Quad9, Google, NextDNS, Custom) arasynda auysyñyz. |
| **Audio ağynynyñ şeberi** | Media, Qoñyrau, Dabyl, Qoñyrau, Dauys jäne Jüie audio ağyndary üşin täuelsiz ūsaq tüiirşikti dybys syrğytpalary. |
| **Tasymaldauşy bailanystyru** | Wi-Fi hotspotyna jäne USB bailanystyruğa qatysty tasymaldauşynyñ qūqyqtaryn tekseruler men şekteulerdi ainalyp ötiñiz. |
| **Aralas audio** | Köp qoldanbaly bir mezgilde dybysty oinatudy ainalyp ötu (basqa qoldanba dybysty oinatqanda fondyq qoldanbalardyñ kidirtuine jol bermeu). |
| **SoundMaster** | Är qoldanbağa arnalğan dybys fokusy jäne dybys deñgeiin bağyttau aralastyrğyşy. |
| **Ämbebap suret-surettegi suret** | Virtualdy displei proektsiyasyn paidalanyp, kez kelgen qoldau körsetilmeitin qoldanbany «Surettegi suret» (PiP) rejimine küştep engiziñiz. |
| **Ekran qauipsizdigi jäne DRM** | Qauipsiz/banktik qoldanbalarda skrinşottar men ekrandy jazuğa rūqsat beru üşin `FLAG_SECURE` şekteulerin ainalyp ötiñiz. |
| **Avto DND (Zen rejimi)** | Mazalamau rejiminiñ avtomattandyrylğan erejeleri aldyñğy qatardağy qoldanba fokusyna, retteletin kestelerge jäne triggerlerge negizdelgen. |
| **DFRoot (DirtyFrag Exploit)** | Qoldau körsetiletin yadro nūsqalary üşin CVE-2026-43284 qoldanatyn kiristirilgen artyqşylyqty arttyru kömekşisi. |
| **Jyldam parametrler taqtaişalary** | 18 retteletin jyldam parametrler taqtasyna arnalğan interaktivti basqaruşy jäne ornatu jinağy. |
| **ShizuWall brandmauer** | Biriktirilgen treker jäne telemetriya skaneri jäne ūyaşyq monitory bar keñeitilgen brandmauer interfeisi. |
| **Kofeinat** | Oqu nemese navigatsiya üşin taqta jäne tūraqty habarlandyru arqyly qūrylğy ekranyn sergek ūstañyz. |
| **Taqyryp patcheri** | Jüie deñgeiindegi ekpin tüsteri, qarip stili jäne UI taqyrybynyñ modifikatsiyalary. |

#### 🛠️ Utilitalar (14 Qūral)
| Erekşelik | Sipattama |
| --- | --- |
| **Şell** | Naqty uaqyttağy ağyndyq şyğysy bar interaktivti artyqşylyqty pärmen joly terminaly. |
| **Jurnaldy qarau qūraly** | Teg, buma, deñgeidi süzu jäne eksporttau mümkindikteri bar naqty uaqyttağy jüielik logkat qarau qūraly. |
| **Qūrylğy aqparaty** | Keşendi apparattyq qūral, yadro, batareya densaulyğy, jylu sensorlary jäne SoC baqylau taqtasy. |
| **Äreketterdi iske qosu qūraly** | Eksporttalğan, eksporttalmağan nemese jasyryn Android äreketteri men nietterin tekseriñiz jäne iske qosyñyz. |
| **Faildar** | Qorğalğan `/data` jäne qoldanba katalogtaryna oqu/jazu rūqsaty bar artyqşylyqty faildyq jüie şolğyşy. |
| **App Installer Pro** | APK, APKS bölingen bumalaryn jäne XAPK pişimderin qoldaityn artyqşylyqty paket ornatuşysy. |
| **Terminal qoldanbalary** | Syrtqy terminal emulyatorlary üşin jyldam auystyrğyş jäne iske qosqyş. |
| **LocalShell** | Jeke jergilikti qabyq interfeisi. |
| **Nietter** | Arnauly aiqyn nemese jasyryn jüie nietterin, taratylatyn qabyldağyştardy jäne qyzmetterdi örteu. |
| **Jasyryn rejim** | Maqsatty qoldanbalardyñ Shizuku, tübir nemese jöndeu küiin anyqtauyna jol bermeu üşin anyqtauğa qarsy şaralar. |
| **Rūqsattar menedjeri** | Barlyq ornatylğan paketter boiynşa joğary rūqsattardy şolu jäne qaitaryp alu/beru. |
| **Şidzuku dükeni** | Shizuku üşin jasalğan aşyq bastapqy qoldanbalardy tabuğa jäne jüktep aluğa arnalğan kiristirilgen katalog. |
| **Qoñyraudy jazu (ShizuCallRecorder)** | Artyqşylyqty qabyqşa audio tüsiru arqyly quattalğan jergilikti eki jaqty qoñyrau jazbasy. |
| **WallYou tūsqağazdary** | Qoldanbadağy tūsqağaz brauzeri, alu jäne tūsqağaz auystyrğyşy. |

### Modulder jüiesi

OmniShizuku Magisk/KernelSU modulderinen şabyttandyrylğan, biraq Şizuku negizindegi modderge arnalğan quatty, keñeitiletin modul jüiesin ūsynady:

- **Modul katalogy** - izdeu, süzu jäne sūryptau arqyly tañdalğan GitHub negizindegi katalogtan qauymdastyq modulderin şolyñyz.
- **Moduldi tabu** - `shevery-modules` nemese resmi derekközdermen belgilengen GitHub repozitariilerinen modulderdi avtomatty türde tabu.
- **Bir ret türtu arqyly ornatu** - oryndalu barysyn baqylau arqyly modulderdi tikelei qoldanbadan jüktep alyñyz, tekseriñiz jäne ornatyñyz.
- **Modul menedjeri** - ornatylğan modulderdi oñai qosu, öşiru, jañartu jäne joyu.
- **WebUI qoldauy** - Köptegen modulder qoldanbanyñ işinen qol jetimdi veb-negizdelgen konfiguratsiya interfeisterin qamtidy.
- **Modul stsenariileri** - dūrys tirkeu jäne qatelerdi öñdeu arqyly ornatu, joyu, qyzmet körsetu jäne jükteu stsenariilerin oryndañyz.
- **Jañartulardy tekseru qūraly** - GitHub şyğarylymdarynan ornatylğan modulder üşin avtomatty jañartudy anyqtau.
- **Qauipsiz ornatu** - ornatu aldynda modul qūrylymyn, tirekterdi jäne stsenariilerdi tekseru.
- **Cross-Platform UI** - telefondar, planşetter, teledidarlar jäne Wear OS üşin oñtailandyrylğan modul ekrandary.

### Qoñyraulardy jazu (ShizuCallRecorder integratsiyasy)

Şidzuku artyqşylyqty qatynasy arqyly qoñyraudy jazuğa kiristirilgen qoldau:

- **Shell-based Recording Engine** - senimdi audio tüsiru üşin artyqşylyqty qabyq qyzmetterin paidalanady.
- **Foreground Service** - Tiisti habarlandyrulary bar tūraqty jazu qyzmeti.
- **InCallService Integration** - Qoñyraulardy avtomatty türde anyqtau jäne jazudy basqaru.
- **Jazudy basqaru** - intuitivti UI kömegimen jazbalardy qarañyz, oinatyñyz, atyn özgertiñiz jäne joiyñyz.
- **Konfiguratsiyalanatyn optsiyalar** - Jazu pişimin, kodekti, közdi jäne saqtau ornyn teñşeñiz.
- **AIDL interfeisteri** - jazudy basqaru üşin qoldanba qūramdastary arasyndağy qauipsiz IPC.
- **Rūqsatty öñdeu** - Qoñyraudy jazu talaptary üşin rūqsatty oryndau uaqytyn dūrys basqaru.
- **Fondyq jūmys** - fondyq rejimde qoldanba arqyly da senimdi jazu.

### AI jäne äzirleuşi mümkindikteri

- **AI Manager** - Birneşe provaiderlerge qoldau körsetetin biriktirilgen AI mümkindikteri (parametrlerde konfiguratsiyalanady).
- **Äzirleuşi optsiyalary** - äzirleuşiniñ keñeitilgen qūraldary, jöndeu utilitalary jäne eksperimenttik mümkindikter.
- **Qate turaly esep beru jüiesi** - jurnaldar, qūrylğy aqparaty jäne būzylu esepteri bar kiristirilgen qate turaly esep beru.
- **Labs Reapply** - Jañartulardan nemese qaita jükteuden keiin tūraqty zerthanalyq özgertulerdi qaita qoldanyñyz.
- **Qūrylğy aqparatynyñ baqylau taqtasy** - egjei-tegjeili apparattyq qūral, bağdarlamalyq qūral jäne jüie sipattary bar qūrylğy turaly tolyq aqparat.
- **Jurnaldy qarau qūraly** - süzu jäne izdeu mümkindikteri bar naqty uaqyttağy logkat qarau qūraly.

### UI/UX jaqsartulary

- **3-materialdyñ mänerli dizainy** - dinamikalyq tüs taqyryby (Material You) qoldauy bar soñğy material dizainy.
- **Köp formaly faktordy qoldau** - telefondar, planşetter, jinalmaly qūrylğylar, Android TV jäne Wear OS üşin oñtailandyrylğan.
- **Qara rejim jäne AMOLED qara** - AMOLED displeileri üşin taza qara optsiyasy bar tolyq qarañğy taqyrypty qoldau.
- **Tegis animatsiyalar** - jyltyratylğan auysular, jalqau qozğalysty būlyñğyr oñtailandyru jäne sūiyqtyqpen ärekettesu.
- **Beiimdeletin ornalasular** - ekran ölşemderi men bağdarlaryna beiimdeletin jauap beretin paidalanuşy interfeisi.
- **Lokalizatsiya** - köp tildi keñ qoldau (ağylşyn, hindi, ispan, frantsuz, qytai, orys, japon, arab, qazaq jäne t.b.).
- **Arnaiy mümkindikter** - Arnaiy mümkindikter qyzmetin qoldauy bar symsyz ADB Auto-PIN kody (tek qosylu üşin).
- **Önimdilik oñtailandyrylğan** - Jalqau jükteu, şektelgen derekterdi alu, kütu uaqytynan qorğau şaralary jäne jadty tiimdi paidalanu.
- **Crash Resilience** - Qorğanys bailanystyrğyşqa qol jetkizu, ūstalmağan erekşelikterdi öñdeu jäne OEM-arnaiy mäselelerde äsem degradatsiya.

---

## 📊 Basqa Şidzuku şanyşqylarymen mümkindikterdi salystyru

| Erekşelik | **OmniSidzuku** | **Şidzuku (tüpnūsqa)** | **Şidzuku Kelesi** | **Hidzuku** | **Şeveri** | **ShizukuPlus** | **Jūldyz** |
| --- | :------------: | :--------------------: | :------------: | :--------: | :--------: | :------------: | :--------: |
| **Negizgi funktsionaldyq** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Symsyz ADB / TCP rejimi** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Tübirlik qoldau** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Dhizuku qoldauy** | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ | ❌ |
| **Avtomatty qosu** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Modul jüiesi** | ✅ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ |
| **Labs Advanced Tools (40 Qūral)** | ✅ | ❌ | Işinara | Işinara | Işinara | ❌ | Işinara |
| **App Debloater (UAD)** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Qoldanbaly mūzdatqyş** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Brandmauer (nöldik VPN)** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Jyldam parametrler taqtaişalary jiyntyğy (18 plitka)** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **ShizuWall Native Firewall** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Treker jäne telemetriya skaneri** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Kofeindi ekran sergek bolu** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Jeke DNS avtomatty türde qaitaru jäne tsiklder** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **DFRoot (yadrony paidalanu kömekşisi)** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **WallYou dinamikalyq tūsqağazdary** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Ämbebap suret-surettegi suret** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Displei tvikeri** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Qoñyraudy jazu** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Taqyryp patcheri** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Kiristirilgen terminal** | ✅ | ❌ | ✅ | ❌ | ✅ | ❌ | ✅ |
| **AI integratsiyasy** | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ |
| **3-material Ekspressivti** | ✅ | ❌ | Material 3 | Material 3 | Material 3 | ❌ | ❌ |
| **TV/Wear OS qoldauy** | ✅ | Işinara | Işinara | Işinara | ❌ | ❌ | ❌ |
| **Oquşy ağyny** | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| **Stealth mümkindikteri** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Symsyz ADB avtomatty PIN kody** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Litsenziya** | Apache-2.0 | Apache-2.0 | Apache-2.0 | Apache-2.0 | Apache-2.0 | Apache-2.0 | MPL-2.0 |

*Salystyru jazu kezinde jalpyğa qoljetimdi mümkindikterge negizdelgen. Mümkindikter uaqyt öte özgerui mümkin.*

---

## 📱 Jüie talaptary

| Qūramdas | Talap |
| --- | --- |
| **Android nūsqasy** | Android 5.0 (Lollipop) API 21 nemese odan joğary |
| **Säulet** | arm64-v8a, armeabi-v7a, x86, x86_64 |
| **Sidzuku** | Shizuku qyzmeti jūmys istep tūruy kerek (symsyz jöndeu, USB arqyly ADB nemese Root arqyly) |
| **Rūqsattar** | Köptegen mümkindikter üşin Shizuku rūqsat berui qajet |
| **Saqtau** | Ornatu üşin ~50-80 MB (mümkindikter boiynşa özgeredi) |
| **Qosymşa** | Wireless ADB Auto-PIN mümkindigine arnalğan arnaiy mümkindikter qyzmeti |

---

## 📥 Ornatu

### GitHub şyğarylymdarynan (ūsynylady)

1. [Şyğarylymdar betine](https://github.com/krishna3163/OmniShizuku/releases) ötiñiz.
2. Eñ soñğy `manager-debug.apk` nemese `manager-release.apk` jüktep alyñyz
3. Android qūrylğyñyzğa APK ornatyñyz
4. Ornatu protsesin oryndañyz

### ADB arqyly

```bash
adb install -r OmniShizuku.apk
```

### Mañyzdy eskertpeler

- Tüpnūsqa Shizuku ornatylğan bolsa, ädette onyñ janyna OmniShizuku ornatuğa nemese ony auystyruğa bolady. Paidalanu jağdaiyñyz üşin üilesimdilikti tekseriñiz.
- Keibir mümkindikter arnaiy Android nūsqalaryn nemese qūrylğy mümkindikterin qajet etedi.
- Qoñyraudy jazu mümkindikteri üşin Android nūsqasyna bailanysty qosymşa rūqsattar qajet boluy mümkin.

---

## 🚀 Jūmysty bastau

1. **OmniShizuku ornatu** - APK jüktep alyñyz jäne ornatyñyz
2. **Sizukuyu ornatu** - Symsyz jöndeu (Android 11+) nemese ADB/Root arqyly Shizuku qyzmetin iske qosyñyz
3. **Rūqsat beru** - OmniShizuku aşyñyz jäne sūralğan kezde Şizuku rūqsatyn beriñiz
4. **Zertteu** - Üi, qoldanbalar, modulder, zerthanalar jäne parametrler qoiyndylary arqyly şarlau
5. **Teñşeu** - qajet mümkindikterdi qosu üşin Essentials Hub nemese Labs bölimine ötiñiz
6. **Qauipsiz bolyñyz** - jüie deñgeiindegi özgeristerdi qoldanbas būryn mümkindik sipattamalaryn oqyñyz

**Symsyz jöndeu üşin (Android 11+):**
- «Parametrler > Äzirleuşi optsiyalary > Symsyz jöndeu» tarmağyna ötip, OmniShizuku nemese Shizuku arqyly jūptañyz.

**ADB üşin (DK arqyly):**
```bash
adb shell sh /storage/emulated/0/Android/data/moe.shizuku.privileged.api/start.sh
```
*(Nemese ornatuyñyzğa säikes bastau pärmenin paidalanyñyz)*

---

## 🔨 Derekközden ğimarat

### Alğy şarttar

- [Android Studio](https://developer.android.com/studio) (soñğy tūraqty) nemese [Android SDK](https://developer.android.com/studio#command-tools)
- [JDK 17](https://adoptium.net/) nemese odan joğary
- [Git](https://git-scm.com/)
- Gradle (qaptama kiredi)

### Klondau jäne qūrastyru

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

Tüzetu APK myna jerde ornalasady: `manager/build/outputs/apk/debug/manager-debug.apk`

### Qūrylymğa qoiylatyn talaptar

- `compileSdk`: 36
- `minSdk`: 21
- `targetSdk`: 36
- Kotlin Jetpack Compose kömegimen
- Şyğarylym qūrastyrularyna arnalğan R8/ProGuard

---

## 🔐 Arhitektura jäne qauipsizdik

### Qauipsizdik şaralary

- **Qorğanys bağdarlamalau** - nöldik qauipsiz bailanystyrğyşqa qol jetkizu, mañyzdy operatsiyalardyñ ainalasyndağy bloktardy ūstap alu jäne keremet qalpyna keltiruler
- **Rūqsattar auqymyn anyqtau** - qajet bolğan jağdaida oryndau uaqytynyñ rūqsat sūraularymen minimaldy rūqsatty paidalanu
- **Secure IPC** - tiisti qyzmettik bailanystyrumen protsessaralyq bailanysqa arnalğan AIDL interfeisteri
- **Telemetriya joq** - analitika, baqylau nemese derekterdi jinau joq
- **Aşyq bastapqy kod** - Tolyğymen möldir, tekseriletin kodtyq baza
- **Qaitarylatyn özgerister** - özgertulerdiñ köpşiligin qoldanba arqyly joyuğa bolady
- **Validatsiya** - moduldi tekseru, paketti tekseru jäne qauipsiz ornatu täjiribeleri
- **OEM üilesimdiligi** - Realme, OPPO, Xiaomi, OnePlus jäne batareyany agressivti oñtailandyruy bar basqa OEM qūrylğylaryna arnalğan arnaiy öñdeu

### Arhitekturanyñ mañyzdy sätteri

- **MVVM ülgisi** - ViewModels jäne UI küiimen alañdauşylyqtardy taza bölu
- **Jetpack Compose** - Qazirgi deklarativti UI qūrylymy
- **Kotlin Coroutines** - ömirlik tsikldi dūrys öñdeumen asinhrondy operatsiyalar
- **Bölme/Parametrler** - Jergilikti derekter tūraqtylyğy
- **Töbe/qanjar** - täueldilik inektsiyasy (qoldanylatyn jerde)
- **Moduldik dizain** - jaqsyraq ūiymdastyruğa jäne tehnikalyq qyzmet körsetuge arnalğan mümkindik modulderi

---

## 🔑 Rūqsattar tüsindirildi

| Rūqsat | Maqsat | üşin qajet |
| --- | --- | --- |
| `QUERY_ALL_PACKAGES` | Ornatylğan qoldanbalardy tizimdeñiz jäne basqaryñyz | App Ops, Debloater, mūzdatqyş, modulder |
| `READ_EXTERNAL_STORAGE` / `SYRTQY_SAQTAUDY BASQARU` | Failğa qol jetkizu | File Explorer, APK ornatu |
| `POST_NOTIFICATIONS` | Habarlandyrulardy körsetu | Qyzmetter, Kofeinat, Jazu |
| `FOREGROUND_SERVICE` | Fondyq qyzmetterdi iske qosyñyz | Qoñyraulardy jazu, ShizuWall, Kofeinat |
| `READ_PHONE_STATE`, `RECORD_AUDIO` | Qoñyraudy anyqtau jäne jazu | ShizuCallRecorder |
| `BIND_ACCESSIBILITY_SERVICE` | Symsyz ADB PIN kodyn avtomatty türde toltyru | Symsyz ADB avtomatty PIN kody (qosu) |
| `PACKAGE_USAGE_STATS` | Qoldanu derekterine qol jetkizu | Keibir oñtailandyru mümkindikteri |
| `REQUEST_INSTALL_PACKAGES` | APK faildaryn ornatyñyz | Package Installer Pro |
| `SYSTEM_ALERT_WINDOW` | Üstindegi displei | Ämbebap PiP, ekran qabattary |

*Rūqsattardyñ köpşiligi naqty mümkindikter üşin qajet bolğanda ğana sūralady.*

---

## 🤝 Üles qosu

Ülester qūptalady! Qate turaly esepter, mümkindik sūraulary, qūjattamany jaqsartu nemese kod ülesteri.

### Qalai üles qosuğa bolady

1. **Şanyşqy** repozitorii
2. **Funktsiya tarmağyn jasau** (`git checkout -b feature/AmazingErekşelik`)
3. **Özgeristerdi oryndañyz** (`git commit -m 'Biraz keremet mümkindik qosu'')
4. **Būtaqqa iteriñiz** (`git push source funktsiyasy/AmazingErekşelik`)
5. **Aşu** tartu sūrauyn

### Ädistemelik nūsqaular

- Qoldanystağy kod stili men konventsiyalardy oryndañyz
- Anyq mindetteme habarlamalaryn jazyñyz
- Özgeristerdi mūqiyat tekseriñiz
- Qajet bolsa, qūjattamany jañartyñyz
- Negizgi mümkindikter üşin aldymen talqylau üşin mäseleni aşyñyz

See [CONTRIBUTING.md](../CONTRIBUTING.md) for more details.

---

## 🙏 Nesieler jäne alğystar

### Negizgi jobalar

- **[RikkaApps/Shizuku](https://github.com/RikkaApps/Shizuku)** - Mūnyñ bärin jasauğa mümkindik bergen tüpnūsqa joba. Rikkağa jäne barlyq üles qosqandarğa ülken rahmet.
- **[Dhizuku](https://github.com/iamr0s/Dhizuku)** - Dhizuku API integratsiyasyn qoldau üşin.

### Qauymdastyqtar jäne şabyt

- **[Ämbebap Android Debloater (UAD)](https://github.com/0x192/universal-android-debloater)** - Qauipsizdik reitingileri jäne delotatsiya ädistemesi
- **[LSPosed](https://github.com/LSPosed/LSPosed)** - Modul jüiesiniñ tūjyrymdamalary üşin şabyt
- **[Magisk](https://github.com/topjohnwu/Magisk)** - Modul jüiesiniñ säuleti şabyty
- **[KernelSU](https://github.com/tiann/KernelSU)** - Zamanaui tübirlik şeşimniñ şabyty

### Modul äzirleuşileri jäne integratsiyalary

- **[kitsumed/ShizuCallRecorder](https://github.com/kitsumed/ShizuCallRecorder)** - Qoñyraulardy jazu funktsiyalaryn biriktiru
- **[RBN-Apps/Quick-Tile-Settings](https://github.com/RBN-Apps/Quick-Tile-Settings)** - Seriktes qoldanbalardy biriktiru
- **[AhmetCanArslan/ShizuWall](https://github.com/AhmetCanArslan/ShizuWall)** - seriktes qoldanba integratsiyasy
- **[Shevery Modules qauymdastyğy](https://github.com/topics/shevery-modules)** - Qauymdastyq moduliniñ ekojüiesi

### Arnaiy rahmet

Şyn jürekten alğys aitamyz:

- **Sidzuku qauymdastyğy** - üzdiksiz qoldau, keri bailanys jäne testileu üşin
- **Barlyq üles qosuşylar** - qateler turaly habarlağan, ūsynylğan mümkindikter, PR jibergen nemese OmniShizuku-ny jaqsartuğa kömektesken ärbir adam
- **Audarmaşylar** - OmniShizuku qoldanbasyn birneşe tilde qoljetimdi etu üşin
- **Beta testerler** - qatelerdi anyqtauğa jäne tūraqtylyqty qamtamasyz etuge kömektesu üşin
- **OEM qauymdastyğy** - qūrylğyğa qatysty uaqytşa şeşimder men tüzetulerdi ortaq paidalanu üşin

### Nelikten OmniShizuku tañdau kerek?

OmniShizuku bir ädemi jobalanğan qoldanbada Shizuku quatyn praktikalyq qūraldardyñ jan-jaqty jiyntyğymen biriktiru arqyly erekşelenedi. OmniShizuku qūrylğyñyzdy öñdeu, retteu, oñtailandyru jäne basqaru üşin birneşe qoldanbalardy jonglerlik etudiñ ornyna, barlyğyn birtūtas täjiribemen biriktiredi. Siz bloatware bağdarlamasyn öşirgisi keletin kezdeisoq paidalanuşy bolsañyz da, jüieni tereñ baptaudy izdeitin quatty paidalanuşy bolsañyz da, OmniShizuku sizge qajetti qūraldardy qauipsizdik, qaitarymdylyq jäne paidalanudyñ qarapaiymdylyğymen qamtamasyz etedi.

---

## 📄 Litsenziya

This project is licensed under the **Apache Litsenziya 2.0**. See the [LICENSE](../LICENSE) file for full details.

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

## 💬 Qoldau

- **GitHub mäseleleri**: [Qateler turaly habarlau nemese mümkindikterdi sūrau](https://github.com/krishna3163/OmniShizuku/issues)
- **GitHub talqylaulary**: [Sūraqtar qoiyñyz jäne ideyalarmen bölisiñiz](https://github.com/krishna3163/OmniShizuku/discussions)
- **Qūjattama**: README parametrin qalağan tiliñizde tekseriñiz
- **Derekköz kody**: [github.com/krishna3163/OmniShizuku](https://github.com/krishna3163/OmniShizuku)

---

<div align="center">

**[krishna3163](https://github.com/krishna3163) jäne tañğajaiyp OmniShizuku qauymdastyğy ❤️ arqyly jasalğan**

*Eger OmniShizuku paidaly dep tapsañyz, GitHub saitynda ⭐ bağasyn beriñiz!*

</div>
