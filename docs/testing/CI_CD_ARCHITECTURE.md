# CI/CD architecture

`apk-build.yml` is the APK artifact and release workflow. It runs unit tests before assembling the debug APK, verifies the generated APK with `apksigner`, and uploads logs/reports for failures.

`android-instrumentation.yml` runs the real `connectedDebugAndroidTest` task on an API 35 emulator and uploads Android test reports even when tests fail. Existing CodeQL, MobSF, dependency-submission, and third-party scanning workflows remain separate so their permissions and findings are visible.

Required checks must fail on compilation, test, signing, or scan errors. Missing hardware is reported as `NOT RUN`; it is never reported as a successful compatibility result.

