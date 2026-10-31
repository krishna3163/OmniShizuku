# OmniShizuku — CI/CD Workflow Failure Analysis & Remediation Post-Mortem

## Executive Summary
Prior to this remediation, several GitHub Actions workflows experienced recurring failures or were at high risk of failing during pull requests, automated scheduled scans, or external fork contributions. This document details the exact root causes identified and the permanent engineering solutions implemented.

---

## 1. Case 1: AI Triage Bot Crash on Missing Secret
- **Workflow:** `.github/workflows/ai-triage.yml`
- **Historical Run ID:** `41216511202`
- **Symptom:** Workflow failed with `exit status 1` immediately during the triage step.
- **Log Snippet:**
  ```text
  config: GEMINI_API_KEYS is not set (need at least 1 key, up to 100 separated by commas/newlines)
  Error: Process completed with exit code 1.
  ```
- **Root Cause:** The triage Go binary requires at least one valid Google Gemini API key to operate. When community contributors or forks open a PR or issue, GitHub Secrets are intentionally not shared for security reasons. The workflow lacked a pre-flight condition to detect empty secrets.
- **Remediation Implemented:**
  Added a pre-check step (`id: check-key`) that evaluates `${{ secrets.GEMINI_API_KEYS }}`. If empty, the step emits a GitHub workflow `::notice` stating that `GEMINI_API_KEYS` is not configured, sets `has_key=false`, and gracefully skips execution of `ai-triage-bot`.
- **Status:** `RESOLVED & VERIFIED`

---

## 2. Case 2: NowSecure Mobile SBOM Missing Java Runtime
- **Workflow:** `.github/workflows/nowsecure-mobile-sbom.yml`
- **Symptom:** Workflow failed during step `Build your application`.
- **Log Snippet:**
  ```text
  ./gradlew: line 234: java: command not found / incompatible Java version
  ```
- **Root Cause:** The workflow runner (`ubuntu-latest`) executed `./gradlew assembleDebug` directly without invoking `actions/setup-java`. Because Gradle 9.8.2 and Android Gradle Plugin 9.5 require JDK 21, the default environment failed immediately. In addition, `./gradlew assembleDebug` attempted a global multi-project build instead of isolating `:manager`.
- **Remediation Implemented:**
  1. Added `actions/setup-java@v4` configuring Temurin JDK 21 with Gradle build caching.
  2. Added `chmod +x gradlew`.
  3. Scoped the build task strictly to `:manager:assembleDebug`.
  4. Wrapped the build step with `if: env.NOWSECURE_CONFIGURED == 'true'` so that runner time is not wasted compiling an APK when NowSecure platform credentials (`NS_TOKEN`, `NOWSECURE_GROUP_ID`) are absent.
- **Status:** `RESOLVED & VERIFIED`

---

## 3. Case 3: Appknox Monolithic Build & Missing SARIF Error
- **Workflow:** `.github/workflows/appknox.yml`
- **Symptom:** Build failed during Gradle compilation and SARIF upload.
- **Log Snippet:**
  ```text
  * What went wrong:
  Execution failed for task ':lint'.
  ...
  Error: Path does not exist /home/runner/work/OmniShizuku/OmniShizuku/report.sarif
  ```
- **Root Cause:**
  1. The workflow invoked `./gradlew build`, which runs all tasks across all subprojects, including Android Lint and unsigned release packaging, which fail in CI environments without full release signing configurations.
  2. The `upload-sarif` step had `if: always()`, which triggered even when Appknox failed to run due to missing `APPKNOX_ACCESS_TOKEN`. When `report.sarif` was absent, `upload-sarif` threw a fatal error.
- **Remediation Implemented:**
  1. Changed build command to `./gradlew :manager:assembleDebug`.
  2. Added `env.APPKNOX_CONFIGURED` check guarding both the APK build and the Appknox scanning action.
  3. Updated the SARIF upload step to: `if: always() && hashFiles('report.sarif') != ''`.
  4. Added a helpful notification warning repository maintainers when credentials need configuration.
- **Status:** `RESOLVED & VERIFIED`

---

## 4. Case 4: Zimperium zScan Broken Configuration & Placeholder Values
- **Workflow:** `.github/workflows/zscan.yml`
- **Symptom:** Scans failed due to dummy string parameters (`client_env: env_string`, `client_id: id_string`) and missing release APK.
- **Root Cause:**
  1. The workflow had placeholder strings `env_string` and `id_string` committed into the repository.
  2. It pointed to `manager/build/outputs/apk/release/manager-release.apk`, which does not exist when building without release signing credentials.
  3. `upload-sarif` ran unconditionally on `Zimperium.sarif`, causing downstream failures when the file was missing.
- **Remediation Implemented:**
  1. Added `env.ZSCAN_CONFIGURED: ${{ secrets.ZSCAN_CLIENT_SECRET != '' && vars.ZSCAN_CLIENT_ID != '' }}`.
  2. Changed build target to `:manager:assembleDebug` and targeted `manager/build/outputs/apk/debug/manager-debug.apk`.
  3. Configured inputs from dynamic variables `${{ vars.ZSCAN_CLIENT_ENV || 'default' }}` and `${{ vars.ZSCAN_CLIENT_ID }}`.
  4. Guarded SARIF upload with `if: env.ZSCAN_CONFIGURED == 'true' && hashFiles('Zimperium.sarif') != ''`.
- **Status:** `RESOLVED & VERIFIED`

---

## 5. Case 5: Codacy & Checkmarx SARIF Upload Guards
- **Workflows:** `.github/workflows/codacy.yml`, `.github/workflows/checkmarx-one.yml`
- **Symptom:** Flaky CI runs when analysis tools timed out or skipped file generation.
- **Root Cause:** `upload-sarif` actions failed when `results.sarif` or `cx_result.sarif` were empty or ungenerated.
- **Remediation Implemented:**
  Added `if: hashFiles('<file>.sarif') != ''` guards ensuring SARIF submission only executes when valid report files exist on disk.
- **Status:** `RESOLVED & VERIFIED`

