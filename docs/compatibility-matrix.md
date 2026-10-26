# Compatibility matrix

Status: pending source-by-source verification. Repository names and the existing feature encyclopedia are not compatibility evidence.

| Source | Android SDK range | Kotlin / Java / Gradle | Native dependencies | Target compatibility | Build / test |
|---|---|---|---|---|---|
| Shizuku | compile/target 37, min 24 | Java 21; Java/Kotlin; Gradle source present | C++/JNI; native pairing and PTY | Plausible SDK/JVM fit (target API 37.1, min 25); source not integrated; inspect server deltas | Source build not run; target baseline `assembleDebug` failed at AAR metadata; rerun blocked before config |
| Shizuku-Next | compile/target 36, min 24 | Java 21; Kotlin JVM 21; Gradle source present | Native server/shell components | Plausible SDK/JVM fit; target already has a module subsystem; no wholesale port planned | Upstream module unit tests exist; target has helper/catalog tests but no module-specific tests; builds not run in this audit |
| Shizako | compile/target 36, min 24 | Java 21; API source subproject has separate MIT license | Shizuku-compatible server/API, Device Owner bridge | Alternate engine; duplicates target modules; no port planned | Not built in this audit |
| Porter | compile/target 37, min 24 | Java 21; Kotlin JVM 21 | Server/shell/starter | Alternate engine; target has equivalent roles; compare deltas only | Not built in this audit |
| ShizukuPlus | Pending | Java/Kotlin; exact plugin review pending | Pending | Only package validator assessed; broader engine compatibility unknown | Not built in this audit |
| ADNS | compile 36.1, target 36, min 27 | Kotlin 2.2.10; AGP 9.2.1 | None needed by the hostname helper | Helper is JVM-only and portable; whole app has min SDK 27 and is not integrated | ADNS app/tests not built in this audit |
| InstallerX-Revived | Pending | Kotlin 2.4.21; plugin/dependency review pending | Native/build details pending | GPL-3.0 app architecture and session installer differ from target's privileged `pm` flag installer; no direct port | Not built in this audit |
| InstallWithOptions | compile/target 37, min 24 | Kotlin 2.4.0 | AIDL-backed shell/package installer | AIDL source exists in target but no runtime wiring was found; target currently uses its own shell installer | Not built in this audit |
| ShizuStore | compile/target 37.0, min 24 | Java 21; Kotlin version catalog pending | Compose, Room, Shizuku API, scheduled updates | Candidate catalog reference; GPL-3.0 and app architecture need review | Not built in this audit |
| sing-box-for-android | compile 37.1, target 37, min 24 | Kotlin/JVM 17; plugin/dependency review pending | Native sing-box engine, VPN service | Candidate proxy backend; GPL-3.0 and project-name restriction, ABI/service integration not reviewed | Not built in this audit |
| Canta | compile/target 37, min 28 | Kotlin Multiplatform 2.4.20; Gradle source present | Android/KMP source sets | Min SDK exceeds target's 25; architecture and LGPL-3.0 make whole-module adoption unsuitable | Source tests exist; not run in this audit |
| essentials | compile 37.1, target 37, min 26 | Java/Kotlin JVM 21; AGP 9.3.2 | Full app has broad service/resource graph; selected battery utilities are Kotlin + platform APIs | Full app does not drop in as a library. Selected BatteryInfoUtil/ThermalUtil/DeviceUtils/ChargingModeUtil/ChargeLimitUtil source files are included with an adapter; API calls are guarded where needed, but target min SDK 25 compatibility/build is unverified | Upstream app not built here; target build blocked before configuration |
| Xhizuku | Pending | Pending | Pending | Pending | Pending |
| Stellar | Pending | Pending | Pending | Pending | Pending |
| Shevery | Pending | Pending | Pending | Pending | Pending |
| Remaining 15 source projects | Pending | Pending | Pending | Pending | Pending |

Target configuration now requests Android API 37.1 to match `island-sdk.aar` metadata; this change is not yet verified by Gradle because the sandbox blocks network-interface enumeration before project configuration. Target Java compatibility is 21 and min SDK is 25. The remaining source checkouts need exact Kotlin/AGP/dependency review. Values above come from checked-out Gradle files and are not claims of successful builds.
