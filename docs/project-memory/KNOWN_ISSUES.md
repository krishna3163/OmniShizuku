# Known Issues - OmniShizuku

## Active Issues

1. **ThemePatcherActivity exported="true"**: The ThemePatcher activity is exported for direct ADB launch during testing. This may need to be reverted to exported="false" for release builds to avoid security exposure.

2. **No AGENTS.md**: No AGENTS.md exists in the repository root. Needs to be created to guide future agents.

3. **XML Activities mixed with Compose**: SettingsScreen.kt (XML), BugReportDialogActivity.kt (XML), NetworkFirewallActivity.kt (XML) use older View-based Activities while most of the app uses Compose. Inconsistent UI patterns.

4. **ThemePatcher not fully testable on current tablet**: No OPPO/OnePlus/Realme theme store installed on device MBRKCEQGUKOJLV6X, so end-to-end trial patching cannot be verified.

## Resolved Issues
None yet

## Issue Template
When recording an issue:
- ID
- Date identified
- Description
- Severity (P0/P1/P2/P3)
- Affected files
- Workaround
- Status (open/resolved)
- Date resolved

5. **Labs Batch 2 not fully verified (2026-11-06)**: LAB-001..LAB-008 have code-level improvements (descriptions, confirmations) but `compileDebugKotlin`/`assembleDebug`/tests timed out in the agent session and runtime navigation/state coverage is unverified. `docs/uiux/LABS_TEST_MATRIX.md` evidence pending. Severity: P1. Affected: App Management Labs screens.
6. **Gradle build timeouts in agent environment (2026-11-06)**: Kotlin/Gradle compilation exceeded 120s tool timeout multiple times; builds may still complete but output was not captured. Workaround: run builds locally with sufficient timeout and capture logs.
