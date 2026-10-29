# Porter — Project Action Inventory

**Repository:** `d4rken-org/porter`
**Local path:** `/home/krishna/Downloads/tv/Shizuku implementations/porter`
**Upstream:** `https://github.com/d4rken-org/porter.git`
**Branch:** `main` / `27021bcb9c450fcc1209057b0598f328f69e12c9`

## Workflow Inventory

| Workflow file | Trigger | Jobs | Description |
|---|---|---|---|
| `app.yml` | push, pull_request, workflow_dispatch | submodule, build, lint, wrapper | Development build: submodule pin verification, Android unit tests, lint, APK assembly and artifact upload |
| `emulator.yml` | push, pull_request | — | Emulator-based instrumentation test execution |
| `pages.yml` | push to main | — | GitHub Pages deployment for project documentation |
| `pr-labeler.yml` | pull_request | — | Automated PR labeling based on changed paths |
| `release-prepare.yml` | workflow_dispatch, release | — | Release preparation: version bump, changelog generation |
| `release-tag.yml` | push tags | — | Release tag creation and push |

## Reusable Composite Actions

| Action path | Description |
|---|---|
| `.github/actions/android-setup` | Android SDK setup: license acceptance, platform/components installation |
| `.github/actions/device-setup` | Emulator/device provisioning for instrumentation tests |
| `.github/actions/sdk-branch` | SDK branch selection and submodule pinning for porter-api |

## Key Actions Referenced

- `actions/checkout@v7` — source checkout with recursive submodules
- `gradle/actions/wrapper-validation@v6.2.0` — Gradle wrapper integrity validation
- `actions/upload-artifact@v6.0.0` — test reports, lint reports, and APK artifact upload
- `actions/attest-build-provenance@v3` — build provenance attestation (SLSA)

## Notes

- Porter uses `ubuntu-26.04` runners (future Ubuntu release).
- Builds two manager flavors: `fossDebug` and `gplayDebug`, plus `compat` and `probe` modules.
- APK identity verification via `tools/verify-apks.py` checks embedded module presence/absence.
- Submodule pin (`api/`) must be reachable from `porter-api` main branch before builds proceed.
