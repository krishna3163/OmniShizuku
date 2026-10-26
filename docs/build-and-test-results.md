# Build and test results

Audit date: 2026-10-26. These are baseline results before any feature implementation in this audit.

| Command | Result | Details |
|---|---|---|
| `./gradlew assembleDebug` | Failed | Gradle 9.8.2 configured the project and ran module tasks. `:manager:checkDebugAarMetadata` failed: `manager/libs/island-sdk.aar` requires compile SDK 37.1 or later; manager is compiled against android-37.0. |

Warnings observed during the build:

- Kotlin Gradle plugin is loaded with explicit versions in `:aidl`, `:server`, `:database`, and `:manager`; Gradle warns this is unsupported and may break builds.
- Deprecated Gradle features are used and are incompatible with Gradle 10.

Unit tests, lint, release build, and device behavior were not run in this baseline step. Do not interpret a partially executed `assembleDebug` as a successful build.

## Baseline remediation decision

The local `manager/libs/island-sdk.aar` contains `minCompileSdk=37` and `minCompileMinorSdk=1`. Its package is `com.sameerasw.island.sdk`; the adjacent `essentials` checkout has the matching `island-sdk` Gradle module (`compileSdk = 37`, `compileSdkMinor = 1`) and documents consuming that SDK as a Gradle project dependency. The target's existing uncommitted Gradle change adds the AAR, and its SHA-256 is `93131aefac8b4d5e9a802ea84c8e790d0cfdaae3eab59750508c8fd32f73b7ce`.

To match the verified SDK requirement, root `build.gradle` now sets `compileSdkMinor = 1` for Android subprojects. This is a build configuration correction; no feature code or AAR contents changed.

| Command | Result | Details |
|---|---|---|
| `./gradlew assembleDebug` after setting compile SDK minor 1 | Could not start | Gradle failed while enumerating network interfaces for its file-lock contention handler: `SocketException: Operation not permitted (Socket creation failed)`. This sandbox-level failure occurs before project configuration/tasks, so the metadata fix is not yet verified. IPv4 preference did not change the result. |

The baseline unit tests, lint and device checks were not run at this stage. The later focused tests are recorded below. Rerun the Gradle build in an environment where Java can enumerate network interfaces to verify whether another project-level failure remains.

## GitHub Actions handoff

The `.github/workflows/apk-build.yml` workflow installs `platforms;android-37.1` and runs `:manager:testDebugUnitTest` before building APKs. The manual release workflow installs the required Android platform, build tools, and NDK. Both publishing workflows fail if the release APK is missing and upload a stable `omnishizuku.apk` asset for the latest-release download link. A GitHub Actions run against commit `a9a88ba` is recorded below.

## Security-validation change verification

Added tests for `PackageNameValidator`, `DnsHostnameValidator` and the bundled DNS preset catalog, and `ShellArguments` under `manager/src/test`. Both `./gradlew :manager:testDebugUnitTest --no-daemon` and `./gradlew assembleDebug --no-daemon` were attempted after the change and stopped at Gradle startup with the same network-interface `SocketException: Operation not permitted`. The six focused Kotlin tests were compiled with the cached project Kotlin compiler and executed directly with JUnit 4.13.2: **6 tests passed**. `git diff --check` completed with no whitespace errors. These tests do not compile or verify the newly integrated Android Essentials battery/thermal source; Android app compilation and Gradle task wiring remain unverified.

After adding the upstream Essentials battery/thermal/charging source slice, `./gradlew :manager:testDebugUnitTest --no-daemon` was attempted again. It still stops before project configuration with `Could not determine a usable wildcard IP for this machine`; the Essentials integration has not been compiled by Gradle.

## GitHub Actions run: 2026-10-26

The GitHub-hosted runner successfully installed Java, started Gradle, and executed Android resource/compile tasks. `:manager:testDebugUnitTest` failed before running tests at `:manager:processDebugMainManifest`:

```text
uses-sdk:minSdkVersion 25 cannot be smaller than version 26 declared in library island-sdk.aar
```

The root Android configuration set minSdk 25, while the bundled `manager/libs/island-sdk.aar` requires minSdk 26. Root `build.gradle` now sets minSdk 26; the README and compatibility matrix are updated. This fixes the reported manifest mismatch but still needs a new GitHub Actions run to verify the remaining compilation and test tasks.

Other log messages were non-fatal in this run: sdkmanager could not find the redundant package `platforms;android-37`, `yes` reported a broken pipe after sdkmanager closed its license-input pipe, and Android resource processing emitted Uzbek string formatting warnings. The run produced no manager APK; only shell/stub APKs were present, and the artifact step therefore had no manager APK to upload. Because unit tests did not complete, this run does not verify test results.
