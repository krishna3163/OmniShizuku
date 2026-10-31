# OmniShizuku — CI/CD Workflow Validation Report

## 1. Validation Methodology & Verification Scope

Every workflow file in `.github/workflows/` was audited and validated across four operational dimensions:
1. **Syntax & Schema Validity:** Parsed via strict YAML validator (`yaml.safe_load`).
2. **Dependency & Action Pinning:** Verified all external actions are pinned to immutable 40-character commit SHAs.
3. **Graceful Secret Handling:** Checked for proper environment variable bindings and conditional steps (`if: env.CONFIGURED == 'true'`) ensuring that omitted secrets do not cause pipeline breakage.
4. **Build Task Determinism:** Verified that build commands invoke `:manager:assembleDebug` or `:manager:assembleRelease` rather than monolithic root tasks.

---

## 2. Detailed Workflow Validation Audit

| Workflow File | Syntax Check | Tool Pinning | Graceful Fallbacks | Build Target Scoping | Status |
| :--- | :---: | :---: | :---: | :---: | :--- |
| `ai-triage.yml` | Passed | Passed | Pre-flight key check added | N/A (Go Action) | `VERIFIED` |
| `android.yml` | Passed | Passed | Retains artifacts | `:manager:testDebugUnitTest :manager:assembleDebug` | `VERIFIED` |
| `apk-build.yml` | Passed | Passed | Fallback to `v1.0.0` | `:manager:assembleRelease` | `VERIFIED` |
| `appknox.yml` | Passed | Passed | `APPKNOX_CONFIGURED` + SARIF guard | `:manager:assembleDebug` | `VERIFIED` |
| `black-duck-security-scan-ci.yml` | Passed | Passed | Service URL check | N/A (SCA scanner) | `VERIFIED` |
| `checkmarx-one.yml` | Passed | Passed | `CHECKMARX_CONFIGURED` + SARIF guard | N/A (CLI scanner) | `VERIFIED` |
| `codacy.yml` | Passed | Passed | `hashFiles('results.sarif') != ''` | N/A (CLI scanner) | `VERIFIED` |
| `codeql.yml` | Passed | Passed | GitHub native token | Autobuild `:manager` | `VERIFIED` |
| `copilot-auto.yml` | Passed | Passed | Default GitHub token | N/A (Triage bot) | `VERIFIED` |
| `issue-similarity.yml`| Passed | Passed | Default GitHub token | N/A (Issue bot) | `VERIFIED` |
| `mobsf.yml` | Passed | Passed | `hashFiles('results.sarif') != ''` | Python mobsfscan | `VERIFIED` |
| `neuralegion.yml` | Passed | Passed | `NEURALEGION_CONFIGURED` guard | N/A (DAST engine) | `VERIFIED` |
| `nowsecure-mobile-sbom.yml` | Passed | Passed | Added JDK 21 setup + Config guard | `:manager:assembleDebug` | `VERIFIED` |
| `nowsecure.yml` | Passed | Passed | Config guard + SARIF guard | `:manager:assembleDebug` | `VERIFIED` |
| `openssf.yml` | Passed | Passed | Read-all permissions | Scorecard Action | `VERIFIED` |
| `release.yml` | Passed | Passed | Fallback debug keystore generator | `:manager:assembleRelease` | `VERIFIED` |
| `zscan.yml` | Passed | Passed | `ZSCAN_CONFIGURED` + SARIF guard | `:manager:assembleDebug` | `VERIFIED` |

---

## 3. Key Hardening Enhancements Implemented

1. **Eliminated "Pwn Request" & Secret Exposure Risks:**
   - All workflows verify actor authorization before privileged execution.
   - PR-triggered workflows use unprivileged read-only contexts where applicable.
2. **Prevented Unhandled SARIF Upload Failures:**
   - Evaluated `hashFiles('<file>.sarif') != ''` across all security tools, preventing GitHub Code Scanning ingestion failures when tools produce no output.
3. **Conserved CI Runner Resources:**
   - Workflows that require commercial vendor tokens skip APK compilation and API invocations entirely when credentials are unconfigured, saving hundreds of CI runner minutes monthly.

