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

The existing `.github/workflows/apk-build.yml` supports `workflow_dispatch`, but the sandbox cannot contact GitHub: `git ls-remote origin HEAD` failed because `github.com` did not resolve, and `gh` is not installed. A local `~/.ssh/id_ed25519` private key file and matching public key exist, but `ssh-add -l` could not contact the SSH agent, so registration or remote authentication could not be confirmed. No private key contents were read. The workflow now explicitly installs `platforms;android-37.1` and runs `:manager:testDebugUnitTest` before building APKs. It has not been pushed or run remotely.

## Security-validation change verification

Added tests for `PackageNameValidator`, `DnsHostnameValidator` and the bundled DNS preset catalog, and `ShellArguments` under `manager/src/test`. Both `./gradlew :manager:testDebugUnitTest --no-daemon` and `./gradlew assembleDebug --no-daemon` were attempted after the change and stopped at Gradle startup with the same network-interface `SocketException: Operation not permitted`. The six focused Kotlin tests were compiled with the cached project Kotlin compiler and executed directly with JUnit 4.13.2: **6 tests passed**. `git diff --check` completed with no whitespace errors. This verifies only the pure Kotlin helpers/catalog and their tests, not Android call-site compilation or Gradle task wiring.
