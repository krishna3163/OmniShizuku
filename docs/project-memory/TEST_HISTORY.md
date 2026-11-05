# Test History - OmniShizuku

## Baseline Test Results (2026-11-05)

| Test Task | Result | Details |
|-----------|--------|---------|
| `:manager:compileDebugKotlin` | PASS | All Kotlin compiles, including new ThemePatcher and EnforceDoze files |
| `:manager:testDebugUnitTest` | PASS | All 15 unit test files pass |
| `:manager:assembleDebug` | PASS | Debug APK built and installed on tablet MBRKCEQGUKOJLV6X |

## Test Files (manager module)
1. `manager/src/test/java/moe/shizuku/manager/shell/ShellCommandsTest.kt`
2. `manager/src/test/java/moe/shizuku/manager/shell/ShellOutputTest.kt`
3. `manager/src/test/java/moe/shizuku/manager/callrecorder/CallRecorderModelTest.kt`
4. `manager/src/test/java/moe/shizuku/manager/ui/screen/LabsSearchTest.kt`
5. `manager/src/test/java/moe/shizuku/manager/home/HomeMetricsCalculatorTest.kt`
6. `manager/src/test/java/moe/shizuku/manager/utils/ShellArgumentsTest.kt`
7. `manager/src/test/java/moe/shizuku/manager/utils/PackageNameValidatorTest.kt`
8. `manager/src/test/java/moe/shizuku/manager/utils/DnsHostnameValidatorTest.kt`
9. `manager/src/test/java/moe/shizuku/manager/utils/SafeIntentUtilsTest.kt`

## Future Test Results
*(Record new test results here as they are executed)*
