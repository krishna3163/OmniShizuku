# OmniShizuku testing strategy

The repository uses the smallest test level that can prove a behavior:

- JVM unit tests for deterministic business logic and validation.
- Android instrumentation tests for framework, lifecycle, and Compose behavior.
- Emulator tests for Android API compatibility.
- Trusted physical-device tests only for claims involving Shizuku, root, ADB, Dhizuku, or OEM behavior.

A passing JVM test does not prove privileged-provider compatibility. Provider and hardware claims must name their execution environment in the report.

## Local commands

```bash
./gradlew :manager:testDebugUnitTest
./gradlew :manager:compileDebugKotlin
./gradlew :manager:assembleDebug
./gradlew :manager:connectedDebugAndroidTest
```

Dependency-resolution failures are reported as build/infrastructure failures, not test passes.

