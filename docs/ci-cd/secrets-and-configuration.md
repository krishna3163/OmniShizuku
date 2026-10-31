# OmniShizuku — CI/CD Secrets, Variables & Configuration Reference

## Overview
This document specifies all GitHub Actions Secrets and Repository Variables used across the OmniShizuku CI/CD infrastructure, their sensitivity level, required permissions, and automated fallback behaviors.

---

## 1. Secrets Reference Catalog

| Secret Name | Associated Workflows | Required / Optional | Sensitivity | Fallback When Absent |
| :--- | :--- | :--- | :--- | :--- |
| `KEYSTORE_BASE64` | `release.yml`, `apk-build.yml` | Recommended | Critical | Auto-generates standard debug keystore for automated signing |
| `SIGNING_STORE_PASSWORD` | `release.yml`, `apk-build.yml` | Conditional | Critical | Uses default keystore credentials (`android`) |
| `SIGNING_KEY_ALIAS` | `release.yml`, `apk-build.yml` | Conditional | Medium | Uses default alias (`androiddebugkey`) |
| `SIGNING_KEY_PASSWORD` | `release.yml`, `apk-build.yml` | Conditional | Critical | Uses default password (`android`) |
| `GEMINI_API_KEYS` | `ai-triage.yml` | Optional | High | Skips AI triage step with a `::notice` without failing the workflow |
| `MODERATION_TOKEN` | `ai-triage.yml` | Optional | High | Operates in "soft" moderation mode (redact & close without deletion) |
| `NS_TOKEN` | `nowsecure.yml`, `nowsecure-mobile-sbom.yml` | Optional | High | Skips scan & upload; issues warning notice |
| `APPKNOX_ACCESS_TOKEN` | `appknox.yml` | Optional | High | Skips scan & upload; issues warning notice |
| `ZSCAN_CLIENT_SECRET` | `zscan.yml` | Optional | High | Skips scan & upload; issues warning notice |
| `CX_CLIENT_ID` | `checkmarx-one.yml` | Optional | Medium | Skips Checkmarx scan; issues notice |
| `CX_CLIENT_SECRET` | `checkmarx-one.yml` | Optional | High | Skips Checkmarx scan; issues notice |
| `CX_TENANT` | `checkmarx-one.yml` | Optional | Low | Skips Checkmarx scan; issues notice |
| `CODACY_PROJECT_TOKEN` | `codacy.yml` | Optional | High | Runs default open rules; guards SARIF upload |
| `NEURALEGION_TOKEN` | `neuralegion.yml` | Optional | High | Skips DAST scan; issues warning notice |
| `BLACKDUCKSCA_TOKEN` | `black-duck-security-scan-ci.yml` | Optional | High | Skips Black Duck SCA; issues notice |
| `COVERITY_PASSPHRASE` | `black-duck-security-scan-ci.yml` | Optional | High | Skips Coverity scan |
| `POLARIS_ACCESS_TOKEN`| `black-duck-security-scan-ci.yml` | Optional | High | Skips Polaris scan |
| `SRM_API_KEY` | `black-duck-security-scan-ci.yml` | Optional | High | Skips SRM scan |

---

## 2. Repository Variables Catalog

| Variable Name | Associated Workflows | Required / Optional | Purpose |
| :--- | :--- | :--- | :--- |
| `NOWSECURE_GROUP_ID` | `nowsecure.yml`, `nowsecure-mobile-sbom.yml` | Optional | Platform group ID for NowSecure vulnerability dashboard |
| `ZSCAN_CLIENT_ID` | `zscan.yml` | Optional | Zimperium client identification string |
| `ZSCAN_CLIENT_ENV` | `zscan.yml` | Optional | Zimperium tenant environment (`default` if omitted) |
| `BLACKDUCKSCA_URL` | `black-duck-security-scan-ci.yml` | Optional | Server URL for Black Duck SCA endpoint |
| `COVERITY_URL` | `black-duck-security-scan-ci.yml` | Optional | Server URL for Coverity static analysis |
| `POLARIS_SERVER_URL` | `black-duck-security-scan-ci.yml` | Optional | Server URL for Polaris DevSecOps hub |
| `SRM_URL` | `black-duck-security-scan-ci.yml` | Optional | Server URL for Software Risk Manager |

---

## 3. Recommended Production Configuration

To enable complete end-to-end official releases and automated security scanning:

1. **Release Signing Setup:**
   ```bash
   # Encode your production Android keystore to base64
   base64 -w 0 release.keystore > keystore_base64.txt
   ```
   Add the following secrets under **Settings -> Secrets and variables -> Actions**:
   - `KEYSTORE_BASE64`: Paste contents of `keystore_base64.txt`.
   - `SIGNING_STORE_PASSWORD`: Keystore store password.
   - `SIGNING_KEY_ALIAS`: Key alias.
   - `SIGNING_KEY_PASSWORD`: Key password.

2. **AI Triage Setup:**
   - Add `GEMINI_API_KEYS`: Comma-separated Google Gemini API keys obtained from Google AI Studio.

