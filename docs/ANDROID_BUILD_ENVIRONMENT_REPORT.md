# Android Build Environment Report

Generated: 2026-11-14T12:05:00.3624313+05:30
Repository: OmniShizuku-main (main @ 35885d2)
Working tree: dirty (many pre-existing modified/untracked files preserved; no git reset/clean/checkout/stash performed)

## A. Original Environment (discovered)

| Item | Value |
|---|---|
| Windows | Windows 10 Pro 2009, 64-bit (reported as Windows 11 10.0 amd64 by Gradle) |
| RAM / disk | ~8.47 GB physical; C: ~117 GB free, D: ~753 GB free |
| Java (PATH / JAVA_HOME) | JDK 25.0.4.1 JetBrains JBR (C:\Program Files\JetBrains\IntelliJ IDEA 2026.3\jbr); java, javac resolve to JBR bin |
| User env JAVA_HOME | C:\Program Files\JetBrains\IntelliJ IDEA 2026.3\jbr (User) |
| Gradle wrapper | 9.8.2 (gradle/wrapper/gradle-wrapper.properties, dist sha256 pinned) |
| Gradle runtime | Daemon/launcher JVM currently JBR 25.0.4.1; wrapper reports Kotlin 2.4.10/Groovy 4.0.33 bundled with Gradle distribution |
| Android SDK env | ANDROID_HOME unset (process/user/machine), ANDROID_SDK_ROOT unset |
| Android SDK install | **Not found** in checked locations (%LOCALAPPDATA%\Android\Sdk, C:\Android\Sdk, D:\Android\Sdk, C:\ProgramData\Android\Sdk, Program Files/common dirs). Prior cmdline-tools search on C:/D: timed out/incomplete. |
| local.properties | **Missing** (OmniShizuku-main\local.properties absent; .gitignore contains /local.properties) |
| Platform Tools / adb | Present: C:\ProgramData\platform-tools\adb.exe (ADB 1.0.41, 37.0.1-15733141). db devices -l: none attached. |
| Build Tools / platforms / NDK / CMake | Not verifiable (no SDK installation found) |
| sdkmanager / avdmanager / emulator | Not found on PATH |

## B. Project Build Requirements (from repo config)

| Item | Value |
|---|---|
| Gradle plugins | Android Gradle Plugin 9.5.0-rc01 (com.android.application/library), Kotlin 2.4.21, Compose/serialization plugins 2.4.21, KSP 2.3.12 (settings) |
| compileSdk | 37, compileSdkMinor 1 |
| targetSdk | 37 |
| minSdk | 26 (root subprojects default); manager module present |
| Build Tools | 37.0.0 |
| NDK | 29.0.14206865 |
| CMake | 3.22.1 (manager/src/main/jni/CMakeLists.txt, also CMakeLists in api demo/porsh/rish, dfroot) |
| Java compile target | JavaVersion.VERSION_21 (source/target compatibility) |
| Native modules | Yes (manager externalNativeBuild cmake, prefab/boringssl deps) |
| CI (android.yml) | Ubuntu, JDK 21 Temurin, installs SDK channel=3 platforms;android-37.1 uild-tools;37.0.0 
dk;29.0.14206865, runs :manager:testDebugUnitTest :manager:lintDebug :manager:assembleDebug |
| CI (instrumentation) | JDK 21, installs platform/build-tools/ndk/cmake + emulator API 35 image, runs :manager:connectedDebugAndroidTest |

## C. Compatibility Assessment (pre-change)

| Area | Status | Notes |
|---|---|---|
| Java vs Gradle/AGP/Kotlin | **Needs verification** | Machine runs JDK 25 (JBR). CI uses JDK 21. Project targets Java 21 bytecode. AGP 9.x/Kotlin 2.4.x typically require modern JDK (17+); JDK 25 may work but is not proven here and differs from CI. No toolchain configured in inspected build files. **Did not change JAVA_HOME/Gradle/AGP/Kotlin.** |
| Android SDK/Build Tools | **FAIL (missing)** | Required packages (platform 37.1 preview/channel 3, build-tools 37.0.0, NDK 29.0.14206865, CMake 3.22.1) cannot be verified/installed without SDK root and cmdline-tools/sdkmanager. |
| local.properties | **FAIL (missing)** | Must point to real SDK path; cannot be created safely without a valid SDK installation. |

## D. Changes Made

- Created docs/ANDROID_BUILD_ENVIRONMENT_REPORT.md (this report) for environment/build status documentation.
- Ran .\gradlew.bat --stop to stop existing daemons after discovery (no project build config changes).
- **No** SDK installation, no environment variable changes, no local.properties creation/modification, no Gradle/AGP/Kotlin/dependency/CI/workflow changes, no app code changes.

## E. Verification Results

| Check | Result | Evidence |
|---|---|---|
| Java | PASS (installed) / UNVERIFIED (project compatibility) | java -version shows JDK 25.0.4.1 JBR; CI uses JDK 21 |
| Gradle wrapper | PASS (present) | gradlew.bat --version => Gradle 9.8.2; wrapper properties pinned |
| Android SDK | FAIL | No SDK root found; ANDROID_HOME/ANDROID_SDK_ROOT unset; no sdkmanager |
| SDK licenses | NOT_RUN | Blocked by missing SDK/cmdline-tools |
| Gradle configuration (projects/help) | BLOCKED/INCOMPLETE | gradlew projects --console=plain --no-daemon was started but command exceeded 120s timeout in this environment before completion; rerun needed after SDK/toolchain readiness |
| Kotlin compilation (:manager:compileDebugKotlin) | NOT_RUN | Blocked by missing Android SDK |
| Unit tests (:manager:testDebugUnitTest) | NOT_RUN | Blocked by missing Android SDK |
| Lint (:manager:lintDebug) | NOT_RUN | Blocked by missing Android SDK |
| assembleDebug (:manager:assembleDebug) | NOT_RUN | Blocked by missing Android SDK |
| Full 	est | NOT_RUN | Blocked |
| Instrumentation | NOT_TESTED | No device/emulator; SDK/emulator packages not installed |
| Physical device | NOT_TESTED | db devices -l shows no devices |

## F. Build Artifacts

- APK: **not produced** (build not run). No APK path verified.

## G. Remaining Blockers / Reproducible Steps

1. **Android SDK missing (primary blocker).**
   - Install Android SDK Command-line Tools into a stable location (e.g. C:\Android\Sdk or %LOCALAPPDATA%\Android\Sdk) with layout cmdline-tools\latest\bin\sdkmanager.bat.
   - Install required packages (match CI/project): platforms;android-37.1, uild-tools;37.0.0, 
dk;29.0.14206865, cmake;3.22.1, platform-tools (already present separately but SDK-managed copy recommended for consistency).
   - Set ANDROID_HOME/ANDROID_SDK_ROOT (user-level preferred), add %ANDROID_HOME%\platform-tools, %ANDROID_HOME%\cmdline-tools\latest\bin to PATH without duplicating existing entries.
   - Create local.properties in OmniShizuku-main with sdk.dir=<escaped Windows path> (do not commit).
2. **JDK compatibility risk.** JDK 25 JBR is active; CI uses JDK 21. If AGP/Kotlin/Gradle tasks fail under JDK 25, install/configure a compatible JDK (prefer LTS, likely 21) and point Gradle/JAVA_HOME accordingly without changing project version matrix.
3. **Verification commands (after SDK/JDK ready):**
   - .\gradlew.bat --stop
   - .\gradlew.bat projects --console=plain --no-daemon
   - .\gradlew.bat :manager:compileDebugKotlin --console=plain --no-daemon
   - .\gradlew.bat :manager:testDebugUnitTest --console=plain --no-daemon
   - .\gradlew.bat :manager:lintDebug --console=plain --no-daemon
   - .\gradlew.bat :manager:assembleDebug --console=plain --no-daemon
   - .\gradlew.bat test --console=plain --no-daemon (if supported)
4. **Gradle configuration not yet fully confirmed:** initial gradlew projects exceeded 120s; needs rerun with sufficient time/network/cache after environment is ready.

## H. Git Preservation

- git status --short before: showed existing modified/untracked files (recorded at task start; preserved).
- git status --short after: existing modified/untracked files remain; added docs/ANDROID_BUILD_ENVIRONMENT_REPORT.md (new, untracked documentation). No reset/clean/destructive checkout/stash/pop/delete of unrelated files performed. local.properties not created.
## Update (post-initial report)
- Attempted to download Android SDK command-line tools to `C:\Android\Sdk` for local verification.
- Download from `https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip` was **incomplete/stalled** in this environment (partial ~13.5 MB observed, commands timed out). Partial temp archive and empty SDK directory were removed.
- Result: **Android SDK remains unavailable**; `local.properties` not created; compile/test/lint/assemble/instrumentation still **NOT_RUN/BLOCKED**.

## Git preservation (current)
- `git status --short` still shows pre-existing modified/untracked files plus task docs (`docs/ANDROID_BUILD_ENVIRONMENT_REPORT.md`, `docs/PRODUCTION_READINESS_AUDIT.md`, `docs/PHASE_7B_REPORT.md`, `docs/FINAL_SECURITY_RELIABILITY_AUDIT.md`, `docs/FINAL_PRODUCTION_READINESS_REPORT.md`) and notification code edits listed in reports. No destructive git operations performed.
