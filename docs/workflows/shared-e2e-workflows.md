# Shared End-to-End Workflows

Common GitHub Actions workflow patterns used across the Shizuku ecosystem projects (Porter, Shevery, Shizako, Shizuku Next, ShizukuPlus, Stellar, Xhizuku, Shizuku/thedjchi).

## Workflow 1: Android CI Build

**Purpose:** Build, test, and lint Android applications on every push and pull request.

**Used by:** Porter (`app.yml`), Shizuku Next (`app.yml`), ShizukuPlus (`app.yml`), Stellar (`manager-ci.yml`), Xhizuku (`app.yml`), Shizuku (`app.yml`), Shevery (`app.yml`), Shizako (`app.yml`)

**Typical job sequence:**

1. **Checkout** — `actions/checkout@v6|v7` with recursive submodules
2. **Setup Java** — `actions/setup-java@v5`, JDK 21 Temurin distribution
3. **Setup Gradle** — `gradle/actions/setup-gradle@v5|v6`, with dependency graph and build scans
4. **Validate wrapper** — `gradle/actions/wrapper-validation@v6`
5. **Build & test** — `./gradlew :manager:testDebugUnitTest :manager:assembleDebug`
6. **Lint** — `./gradlew :manager:lintVitalFossRelease :manager:lintVitalGplayRelease`
7. **Upload artifacts** — `actions/upload-artifact@v6|v7` for test reports, lint reports, APKs

**Runner:** `ubuntu-latest` (Porter uses `ubuntu-26.04`)

---

## Workflow 2: APK Build & Release

**Purpose:** Version bump, sign, assemble debug/release APKs, and publish to GitHub Releases.

**Used by:** Shizuku Next (`app.yml`), Porter (`release-prepare.yml`, `release-tag.yml`), Shevery (`app.yml`, `release.yml`), Stellar (`manager-release.yml`, `publish.yml`)

**Typical job sequence:**

1. **Checkout** — source checkout, `fetch-depth: 0` for tag history
2. **Version bump** — read `build.gradle`, increment patch, commit and push back to branch
3. **Validate signing** — check keystore secrets are configured
4. **Create signing.properties** — decode base64 keystore, write signing config
5. **Build with Gradle** — `:manager:lintDebug`, `:manager:assembleDebug`, `:manager:assembleRelease`
6. **Attest provenance** — `actions/attest-build-provenance@v3` (SLSA build provenance)
7. **Upload artifacts** — `actions/upload-artifact@v6|v7` for debug and release APKs
8. **Create release tag** — `git tag` and push to origin
9. **Create draft release** — changelog from Conventional Commits, upload APK assets

**Runner:** `ubuntu-latest`

---

## Workflow 3: Security Scanning

**Purpose:** Detect vulnerabilities, secrets, and supply chain risks in code and dependencies.

**Used by:** ShizukuPlus (`security.yml`, `gitguardian.yaml`), Xhizuku (`openssf.yml` implied via Shevery pattern), Shevery (`openssf.yml`, `issue-similarity.yml`), OmniShizuku (multiple security workflows)

**Typical job sequence:**

1. **CodeQL analysis** — `github/codeql-action` for static analysis of Java/Kotlin/C++
2. **Dependency review** — `actions/dependency-review-action` for vulnerable dependency detection
3. **Secret scanning** — `gitguardian` for hardcoded secrets and API keys
4. **OpenSSF Scorecard** — `ossf/scorecard-action` for supply chain security scoring
5. **SAST/DAST** — MobSF, Appknox, NowSecure, Black Duck, Checkmarx, ZAP scans (OmniShizuku)

**Used in OmniShizuku:** `codeql.yml`, `black-duck-security-scan-ci.yml`, `checkmarx-one.yml`, `codacy.yml`, `mobsf.yml`, `appknox.yml`, `neuralegion.yml`, `nowsecure.yml`, `nowsecure-mobile-sbom.yml`, `openssf.yml`, `zscan.yml`, `apknox.yml`

---

## Workflow 4: Translation & Localization

**Purpose:** Synchronize translation strings between Crowdin and the repository.

**Used by:** Shizuku Next (`crowdin.yml`, `translations-merge.yml`, `contributors.yml`), ShizukuPlus (`crowdin-sync.yml`)

**Typical job sequence:**

1. **Crowdin sync** — download latest translations from Crowdin project
2. **Translation merge** — merge translated strings into repository resource files
3. **Contributor update** — synchronize contributor list from Crowdin
4. **Commit changes** — push merged translations back to repository

**Runner:** `ubuntu-latest`, triggered on schedule or push

---

## Workflow 5: Issue & PR Lifecycle

**Purpose:** Automate issue triage, labeling, duplicate detection, and stale closure.

**Used by:** Shizuku Next (`issue-labels.yml`, `labels-sync.yml`, `stale.yml`, `duplicate-suggest.yml`), ShizukuPlus (`issue-auto-label.yml`, `pr-check.yml`), Shevery (`issue-similarity.yml`, `ai-triage.yml`, `copilot-auto.yml`), Porter (`pr-labeler.yml`)

**Typical job sequence:**

1. **Auto-labeling** — label issues/PRs based on changed paths, keywords, or AI classification
2. **Similarity detection** — detect duplicate issues using text similarity matching
3. **AI triage** — AI-powered categorization, prioritization, and routing of incoming issues
4. **Stale closure** — close issues and PRs with no activity after a configured period
5. **Label sync** — synchronize labels across repositories for consistency
6. **PR checks** — validate PRs meet merge requirements (conventional commits, changelog, etc.)

**Runner:** `ubuntu-latest`, triggered on issues, pull_request, or schedule
