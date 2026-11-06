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

## 2026-11-06 — Labs Batch 2 (partial verification, this session)
| Task | Result | Details |
|------|--------|---------|
| `:manager:compileDebugKotlin` | TIMEOUT/NOT VERIFIED | Gradle compile exceeded session timeout (120s+) repeatedly; Kotlin daemon active. Rerun locally: `./gradlew :manager:compileDebugKotlin` |
| `:manager:assembleDebug` | TIMEOUT/NOT VERIFIED | Timed out in session. Rerun locally. |
| `:manager:testDebugUnitTest` | NOT RUN | Not executed in this session. |
| Resource merge (labs_strings duplicate) | FIXED | Duplicate `labs_debloater_desc` entries removed; prior merge failure resolved in-session. |
| Code-level checks (Batch 2) | PARTIAL | Confirmations present in Debloater/Freezer/Optimizer; header/description context present in LAB-001..008 screens; `FreezerScreen` header subtitle corrected. Runtime/navigation/states not verified. |

## 2026-11-06 — Labs Batch 2 & Verification Run
| Task | Result | Details |
|------|--------|---------|
| `:manager:compileDebugKotlin` | PASS | Successfully compiled Kotlin code (BUILD SUCCESSFUL in 5m 31s). |
| `:manager:testDebugUnitTest` | PASS | All 15 unit test suites executed and passed (BUILD SUCCESSFUL in 3m 30s). |
| `:manager:assembleDebug` | PASS | Debug APK built successfully (`manager-debug.apk`, 62MB, BUILD SUCCESSFUL in 2m 5s). |
| Resource & UI Validation | PASS | Batch 2 (LAB-001..LAB-008) validations, confirmations, and LabToolScaffold standardized. |
