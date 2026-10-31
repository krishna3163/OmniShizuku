# OmniShizuku — CI/CD Workflow Inventory

## Overview
This document catalogs all 22 GitHub Actions workflows and GitHub-managed services associated with the OmniShizuku repository (`https://github.com/krishna3163/OmniShizuku`).

---

## 1. Inventory Summary Table

| # | Workflow Name | File / Identifier | Type | Triggers | Required Secrets | Purpose |
| :- | :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | **AI Triage** | `.github/workflows/ai-triage.yml` | Action | `issues`, `pull_request_target` | `GEMINI_API_KEYS` | Automated issue/PR categorization and spam detection |
| 2 | **Android CI** | `.github/workflows/android.yml` | Action | `push`, `pull_request` | None | Primary CI: compiles code, executes unit tests, builds debug APK |
| 3 | **Build Android APK** | `.github/workflows/apk-build.yml` | Action | `push`, `tags: [v*]`, `workflow_dispatch` | `KEYSTORE_BASE64` (Optional) | Produces signed universal and architecture-specific release APKs |
| 4 | **Appknox** | `.github/workflows/appknox.yml` | Action | `push`, `pull_request` | `APPKNOX_ACCESS_TOKEN` | Commercial mobile security vulnerability scanner |
| 5 | **CI Black Duck Security Scan** | `.github/workflows/black-duck-security-scan-ci.yml` | Action | `push`, `pull_request`, `schedule` | Black Duck / Coverity / Polaris / SRM credentials | Enterprise SCA and SAST scanning |
| 6 | **Checkmarx Scan** | `.github/workflows/checkmarx-one.yml` | Action | `pull_request` | `CX_CLIENT_ID`, `CX_CLIENT_SECRET`, `CX_TENANT` | Enterprise SAST and KICS security scanning |
| 7 | **Codacy Security Scan** | `.github/workflows/codacy.yml` | Action | `push`, `pull_request`, `schedule` | `CODACY_PROJECT_TOKEN` | Static code analysis and code quality scanner |
| 8 | **CodeQL SAST** | `.github/workflows/codeql.yml` | Action | `push`, `pull_request`, `schedule` | `GITHUB_TOKEN` | Semantic code analysis for Kotlin and Java vulnerabilities |
| 9 | **Copilot Auto-Assign** | `.github/workflows/copilot-auto.yml` | Action | `issues`, `pull_request` | `GITHUB_TOKEN` | Assigns AI assistant and maintainers to new issues |
| 10 | **Issue Similarity Check** | `.github/workflows/issue-similarity.yml` | Action | `issues` | `GITHUB_TOKEN` | Detects duplicate issues and prompts reporter with existing threads |
| 11 | **MobSF** | `.github/workflows/mobsf.yml` | Action | `push`, `pull_request`, `schedule` | `GITHUB_TOKEN` | Mobile Security Framework static analysis for Android |
| 12 | **NeuraLegion** | `.github/workflows/neuralegion.yml` | Action | `push`, `pull_request`, `schedule` | `NEURALEGION_TOKEN` | Dynamic application security testing (DAST) |
| 13 | **NowSecure Mobile SBOM** | `.github/workflows/nowsecure-mobile-sbom.yml` | Action | `push`, `pull_request`, `schedule` | `NS_TOKEN`, `NOWSECURE_GROUP_ID` | Generates mobile Software Bill of Materials (SBOM) |
| 14 | **NowSecure** | `.github/workflows/nowsecure.yml` | Action | `push`, `pull_request` | `NS_TOKEN`, `NOWSECURE_GROUP_ID` | Automated mobile security assessment and SARIF export |
| 15 | **OpenSSF Scorecard** | `.github/workflows/openssf.yml` | Action | `push`, `schedule` | `GITHUB_TOKEN` | Supply chain security evaluation and Scorecard badge |
| 16 | **Create Latest Release** | `.github/workflows/release.yml` | Action | `workflow_dispatch` | `KEYSTORE_BASE64`, `SIGNING_*` | Production release pipeline: tags, compiles, signs, publishes APKs |
| 17 | **Zimperium zScan** | `.github/workflows/zscan.yml` | Action | `push`, `pull_request` | `ZSCAN_CLIENT_SECRET`, `ZSCAN_CLIENT_ID` | Mobile application security and compliance scanning |
| 18 | **GitHub Advanced Security** | Managed Service (ID 401397452) | GitHub Feature | Continuous | GitHub Managed | Code scanning, secret scanning, and dependency alerts |
| 19 | **Copilot Cloud Agent** | Managed Service (ID 401398428) | GitHub Feature | On Demand | GitHub Managed | GitHub Copilot coding workspace integration |
| 20 | **Dependabot Updates** | Managed Service (ID 395260485) | GitHub Feature | Scheduled (Daily) | GitHub Managed | Automated dependency version update pull requests |
| 21 | **Dependency Graph** | Managed Service (ID 395354144) | GitHub Feature | Continuous | GitHub Managed | Tracks repository manifest dependencies and vulnerabilities |
| 22 | **Automatic Dependency Submission** | Managed Service (ID 395354812) | GitHub Feature | Push | GitHub Managed | Submits build-time Gradle dependency snapshots to GitHub |

---

## 2. Detailed Workflow Specifications

### 2.1 Core Engineering & Release Pipelines
- **`android.yml`**: Uses JDK 21 and Android SDK 37.1 to execute `./gradlew :manager:testDebugUnitTest :manager:assembleDebug`. Retains test report XML/HTML and debug APK artifacts for 30 days.
- **`apk-build.yml`**: Multi-stage build that computes semantic versioning from Git tags or `build.gradle`, builds signed release APKs if signing keystores are configured, or creates debug-signed binaries.
- **`release.yml`**: Release gate restricted strictly to repository owner (`krishna3163`). Runs manager unit tests with custom version override, builds release and debug APKs, and publishes GitHub Release assets with automated release notes.

### 2.2 Security Scanners & Dynamic Gates
- **`codeql.yml`**: Runs GitHub CodeQL engine analyzing `java-kotlin` queries with extended security suites.
- **`mobsf.yml`**: Runs `mobsfscan` Python CLI scanning Android manifests, permissions, and crypto usage.
- **Third-Party Enterprise Scanners (`nowsecure.yml`, `appknox.yml`, `zscan.yml`, `checkmarx-one.yml`, `black-duck-security-scan-ci.yml`, `neuralegion.yml`)**:
  - All configured with graceful fallback checks (`if: env.CONFIGURED == 'true'`).
  - When commercial license secrets are not populated, workflows issue clean warnings/notices and complete successfully without blocking repository PRs.

