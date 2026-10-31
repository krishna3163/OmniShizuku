# Shevery — Project Action Inventory

**Repository:** `HmnDev-Tech/shevery`
**Local path:** `/home/krishna/Downloads/tv/Shizuku implementations/shevery`
**Upstream:** `https://github.com/HmnDev-Tech/shevery.git`
**Branch:** `main` / `53d875fcf4c7908db7c3451cef155a9f3c1fa46d`

## Workflow Inventory

| Workflow file | Trigger | Jobs | Description |
|---|---|---|---|
| `app.yml` | workflow_dispatch, push tags (`v*`) | build | Release build: signing config, Gradle build with build scans, artifact upload, GitHub Release |
| `ai-triage.yml` | issues, pull_request | — | AI-powered issue triage and labeling |
| `apk-build.yml` | push, pull_request | — | APK build workflow for debug/release variants |
| `copilot-auto.yml` | pull_request | — | GitHub Copilot automation for PR reviews |
| `issue-similarity.yml` | issues | — | Duplicate issue detection using similarity matching |
| `openssf.yml` | push, pull_request | — | OpenSSF security scorecard evaluation |
| `release.yml` | push tags | — | Release publishing and artifact distribution |

## Reusable Composite Actions

| Action path | Description |
|---|---|
| `.github/actions/ai-triage-bot` | AI issue triage bot: categorizes, labels, and routes incoming issues |

## Key Actions Referenced

- `actions/checkout@v7` — source checkout with recursive submodules
- `actions/setup-java@v5` — JDK 21 Temurin setup
- `gradle/actions/setup-gradle@v6` — Gradle setup with dependency graph and build scans
- `actions/upload-artifact@v7` — APK and mapping file artifact upload
- `softprops/action-gh-release@v3` — GitHub Release creation with auto-generated notes

## Notes

- Builds the `standard` release variant only (stealth variant is generated on-device).
- Signing via base64-encoded keystore secret decoded at runtime.
- Gradle build scans are published with terms-of-service acceptance.
- Release only triggers on tag pushes (`v*`).
