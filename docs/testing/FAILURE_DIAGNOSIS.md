# Failure diagnosis

Classify failures before changing code:

- `APPLICATION_BUG`: source or behavior regression.
- `BUILD_CONFIGURATION_FAILURE`: Gradle, plugin, SDK, or manifest configuration.
- `DEPENDENCY_RESOLUTION_FAILURE`: repository or artifact unavailable.
- `TEST_INFRASTRUCTURE_FAILURE`: emulator, runner, or device setup.
- `SECURITY_VULNERABILITY`: exploitable unsafe behavior or scanner finding.
- `COMPATIBILITY_FAILURE`: behavior differs on a supported API/OEM.
- `FLAKY_TEST`: a test changes outcome without a code change.

Every failed workflow uploads its available logs and reports. A retry may diagnose infrastructure flakiness, but a fail-then-pass result remains flaky until investigated.

