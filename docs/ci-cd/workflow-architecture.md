# OmniShizuku — CI/CD Pipeline Architecture & Execution Flow

## 1. Architectural Overview

OmniShizuku employs a layered, event-driven CI/CD architecture designed to balance rapid developer feedback on pull requests with enterprise-grade security analysis and production release automation.

```mermaid
flowchart TD
    subgraph Triggers ["GitHub Event Triggers"]
        PR["Pull Request (opened / synced)"]
        PushMain["Push to main / dev / release/*"]
        Tag["Git Tag (v*.*.*)"]
        Dispatch["Workflow Dispatch (Manual Release)"]
    end

    subgraph CI_Stage ["Phase 1: Validation & Build Verification"]
        AndroidCI["Android CI (android.yml)\n• Setup JDK 21 & SDK 37.1\n• Unit Tests\n• Assemble Debug APK"]
        WrapperCheck["Wrapper Validation"]
        TestReports["Publish Test Artifacts"]
    end

    subgraph Security_Stage ["Phase 2: Security & Static Analysis"]
        CodeQL["CodeQL SAST\n(Java/Kotlin semantic)"]
        MobSF["MobSF Mobile Security Scan"]
        Scorecard["OpenSSF Supply Chain Scorecard"]
        CommercialScans["Enterprise Scanners (Opt-in)\n• NowSecure\n• Appknox\n• Zimperium\n• Checkmarx\n• Black Duck"]
    end

    subgraph Release_Stage ["Phase 3: Release Packaging & Promotion"]
        Gate["Owner Gate (krishna3163 check)"]
        SignAPK["APK Signing & Alignment\n(v1/v2/v3 signing schemes)"]
        PublishRelease["Publish GitHub Release\n• Universal APK\n• Debug APK\n• Release Notes"]
    end

    PR --> AndroidCI
    PR --> CodeQL
    PR --> MobSF
    PR --> CommercialScans

    PushMain --> AndroidCI
    PushMain --> CodeQL
    PushMain --> Scorecard

    Tag --> Dispatch
    Dispatch --> Gate
    Gate --> SignAPK
    SignAPK --> PublishRelease
```

---

## 2. Pipeline Execution Pathways

### 2.1 The Pull Request Pipeline (Developer Fast Gate)
- **Workflows Triggered:** `android.yml`, `codeql.yml`, `mobsf.yml`, `checkmarx-one.yml`, `copilot-auto.yml`, `issue-similarity.yml`.
- **Target Tasks:**
  - Execute `:manager:testDebugUnitTest` to guarantee regression prevention.
  - Compile `:manager:assembleDebug` to verify resource merging and DEX compilation.
  - Run CodeQL and MobSF security scans to catch regressions before merge.
- **Fail-Safe Policy:** Third-party commercial security tools gracefully detect absent vendor tokens and exit with notices rather than failing PR checks.

### 2.2 The Main Branch Pipeline (Integration & Supply Chain)
- **Workflows Triggered:** `android.yml`, `codeql.yml`, `openssf.yml`, `apk-build.yml`, Dependabot.
- **Target Tasks:**
  - Full regression test execution.
  - Scorecard security evaluation verifying commit signing, branch protection, and dependency pinning.
  - Incremental version computation and continuous development APK artifact generation.

### 2.3 The V1.0.0 Release Promotion Pipeline (`release.yml`)
- **Workflow:** `release.yml`
- **Trigger:** Manual dispatch by repository owner or tag push.
- **Execution Flow:**
  1. **Owner Gate:** Validates that the initiating actor is `krishna3163`; aborts with `exit 1` for unauthorized callers.
  2. **Tag Validation:** Verifies semantic versioning format (e.g. `v1.0.0`).
  3. **Keystore Extraction:** Decodes `${{ secrets.KEYSTORE_BASE64 }}`; if omitted, generates a valid fallback debug keystore for automated testing.
  4. **Compilation & Packaging:** Runs `./gradlew :manager:assembleRelease :manager:assembleDebug "-PomniVersionName=v1.0.0"`.
  5. **Artifact Publishing:** Creates GitHub Release `v1.0.0`, attaches signed APKs, and generates markdown release changelog.

