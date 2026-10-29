# Action Catalog — Shizuku Ecosystem GitHub Actions Organization

Master catalog organizing all GitHub Actions workflows and reusable actions across the Shizuku ecosystem. Cross-references project-level inventories and shared end-to-end workflow patterns.

## Project Inventory Index

| # | Project | Repository | Local Path | Workflow Count | Custom Actions | Primary Focus |
|---|---|---|---|---|---|---|
| 1 | Porter | `d4rken-org/porter` | `Shizuku implementations/porter` | 6 | 3 (android-setup, device-setup, sdk-branch) | Kotlin DSL, C++ PTY engine, probe |
| 2 | Shevery | `HmnDev-Tech/shevery` | `Shizuku implementations/shevery` | 7 | 1 (ai-triage-bot) | TV-oriented, Tasker, Comput AI |
| 3 | Shizako | `cr1437/Shizako` | `Shizuku implementations/Shizako` | 2 | 0 | OpenGL blur, Quick Settings tiles |
| 4 | Shizuku Next | `rushiranpise/Shizuku-Next` | `Shizuku implementations/Shizuku-Next` | 8 | 0 | ADB modules, 15 Labs engines, API 36 |
| 5 | ShizukuPlus | `thejaustin/ShizukuPlus` | `Shizuku implementations/ShizukuPlus` | 6 | 0 | Room DB, 15+ server extensions, enterprise |
| 6 | Stellar | `roro2239/Stellar` | `Shizuku implementations/Stellar` | 4 | 0 | Pure Kotlin, native PTY, dual bridge |
| 7 | Xhizuku | `xeonleonreal/Xhizuku` | `Shizuku implementations/Xhizuku` | 2 | 0 | ADB modules, NightDog, TV/Wear UI |
| 8 | Shizuku (thedjchi) | `thedjchi/Shizuku` | `Shizuku implementations/Shizuku` | 1 | 0 | Official base, Binder proxy, PTY |

**Total:** 36 workflow files across 8 projects, 4 reusable composite actions

---

## Shared End-to-End Workflow Patterns

| # | Pattern | Projects Using | Description |
|---|---|---|---|
| 1 | Android CI Build | All 8 projects | Build, test, lint on push/PR |
| 2 | APK Build & Release | Porter, Shevery, Shizuku Next, Stellar | Version bump, sign, assemble, publish |
| 3 | Security Scanning | ShizukuPlus, Shevery, OmniShizuku | CodeQL, dependency review, secret scanning, SAST |
| 4 | Translation & Localization | Shizuku Next, ShizukuPlus | Crowdin sync, translation merge |
| 5 | Issue & PR Lifecycle | Shizuku Next, ShizukuPlus, Shevery, Porter | Auto-labeling, similarity, AI triage, stale closure |

---

## OmniShizuku Native Workflows

OmniShizuku (`/home/krishna/Downloads/OmniShizuku/.github/workflows/`) maintains 17 native workflows:

| Workflow | Purpose |
|---|---|
| `android.yml` | Android CI build and test |
| `apk-build.yml` | APK build, sign, publish to GitHub Releases |
| `ai-triage.yml` | AI issue triage |
| `apknox.yml` | APKnox security scan |
| `black-duck-security-scan-ci.yml` | Black Duck security scanning |
| `checkmarx-one.yml` | Checkmarx SAST |
| `codacy.yml` | Codacy code quality |
| `codeql.yml` | CodeQL analysis |
| `copilot-auto.yml` | Copilot PR automation |
| `issue-similarity.yml` | Duplicate issue detection |
| `mobsf.yml` | MobSF mobile security |
| `neuralegion.yml` | Neuralegion DAST |
| `nowsecure.yml` | NowSecure mobile security |
| `nowsecure-mobile-sbom.yml` | NowSecure SBOM |
| `openssf.yml` | OpenSSF Scorecard |
| `release.yml` | Release publishing |
| `zscan.yml` | ZAP security scan |

OmniShizuku custom actions:
- `.github/actions/ai-triage-bot/` — AI issue triage bot

---

## Reusable Action Cross-Reference

### GitHub-hosted Actions (public)

| Action | Version | Projects |
|---|---|---|
| `actions/checkout` | v6, v7 | All 8 projects |
| `actions/setup-java` | v5 | Porter, Shevery, Shizuku Next, OmniShizuku |
| `actions/upload-artifact` | v6, v7 | All 8 projects + OmniShizuku |
| `actions/download-artifact` | v7 | Shizuku Next |
| `gradle/actions/wrapper-validation` | v6.2.0 | Porter, OmniShizuku |
| `gradle/actions/setup-gradle` | v5, v6 | Shizuku Next, Shevery |
| `actions/attest-build-provenance` | v3 | Shizuku Next |
| `softprops/action-gh-release` | v3 | Shevery, Shizuku Next |

### Custom Composite Actions (per-project)

| Project | Action | Path | Purpose |
|---|---|---|---|
| Porter | android-setup | `.github/actions/android-setup` | Android SDK setup |
| Porter | device-setup | `.github/actions/device-setup` | Emulator/device provisioning |
| Porter | sdk-branch | `.github/actions/sdk-branch` | SDK branch and submodule pinning |
| Shevery | ai-triage-bot | `.github/actions/ai-triage-bot` | AI issue triage |

---

## Runner Matrix

| Runner | Projects |
|---|---|
| `ubuntu-latest` | Shevery, Shizuku Next, ShizukuPlus, Stellar, Xhizuku, Shizuku, Shizako, OmniShizuku |
| `ubuntu-26.04` | Porter |

---

## Branch/Tag Patterns

| Pattern | Projects |
|---|---|
| Push tags `v*` | Shevery (`app.yml`), Shizuku Plus (`security.yml` implied) |
| Push all branches | Porter, Shizako, ShizukuPlus, Stellar, Xhizuku, Shizuku |
| `workflow_dispatch` | Shizuku Next, Shevery, Porter (release-prepare) |
| Pull request | All 8 projects |

---

## File Organization

```
docs/workflows/
├── action-catalog.md           # This file — master catalog
├── shared-e2e-workflows.md     # 5 shared E2E workflow patterns
├── porter.md                   # Porter project inventory
├── shevery.md                  # Shevery project inventory
├── shizako.md                  # Shizako project inventory
├── shizuku-next.md             # Shizuku Next project inventory
├── shizukuplus.md              # ShizukuPlus project inventory
├── stellar.md                  # Stellar project inventory
├── xhizuku.md                  # Xhizuku project inventory
└── shizuku-thedjchi.md         # Shizuku (thedjchi) project inventory
```

---

## Related Documentation

- `docs/source-repository-inventory.md` — Source repository acquisition and audit status
- `docs/github-catalog.md` — GitHub module discovery system (Shevery ADB modules)
- `docs/integration-progress.md` — Integration progress tracking
- `.github/workflows/` — OmniShizuku native workflow YAML files
- `.github/actions/` — OmniShizuku custom composite actions
