# Shizuku Next — Project Action Inventory

**Repository:** `rushiranpise/Shizuku-Next`
**Local path:** `/home/krishna/Downloads/tv/Shizuku implementations/Shizuku-Next`
**Upstream:** `https://github.com/rushiranpise/Shizuku-Next.git`
**Branch:** `master` / `7794a1e8ecf4eddbf48c62c641a77b35019b4860`

## Workflow Inventory

| Workflow file | Trigger | Jobs | Description |
|---|---|---|---|
| `app.yml` | workflow_dispatch, push, pull_request | build, release | Build with version bump, signing, lintDebug gate, APK assembly, provenance attestation, changelog generation, release tag and draft release creation |
| `contributors.yml` | schedule | — | Contributor list synchronization |
| `crowdin.yml` | push, pull_request | — | Crowdin translation synchronization |
| `duplicate-suggest.yml` | issues | — | Duplicate issue suggestion |
| `issue-labels.yml` | issues, pull_request | — | Issue label management |
| `labels-sync.yml | schedule | — | Label synchronization across repositories |
| `stale.yml` | schedule | — | Stale issue and PR closure |
| `translations-merge.yml` | push, pull_request | — | Translation merge from Crowdin |

## Reusable Composite Actions

None defined.

## Key Actions Referenced

- `actions/checkout@v6` — source checkout with recursive submodules
- `actions/setup-java@v5` — JDK 21 Temurin setup
- `gradle/actions/setup-gradle@v5` — Gradle setup with build scans
- `actions/upload-artifact@v6` / `v7` — debug and release APK artifact upload
- `actions/attest-build-provenance@v3` — build provenance attestation (SLSA)
- `actions/download-artifact@v7` — artifact download for release
- `softprops/action-gh-release@v3` — GitHub Release creation

## Notes

- Uses `ubuntu-latest` runners.
- Version bump happens before build (commit pushed to branch), ensuring APK version matches tag.
- `lintDebug` is the API-level gate: fails on calls to APIs newer than minSdk.
- Builds both `debug` and `release` variants every run.
- Release notes are generated from Conventional Commits (feat, fix, perf, refactor, etc.).
- Supports prerelease (beta) and stable release tracks.
