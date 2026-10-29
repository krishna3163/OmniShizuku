# Stellar — Project Action Inventory

**Repository:** `roro2239/Stellar`
**Local path:** `/home/krishna/Downloads/tv/Shizuku implementations/Stellar`
**Upstream:** `https://github.com/roro2239/Stellar.git`
**Branch:** `main` / `bc03f6a2d7c95167d1853e24d3e52587634e7c58`

## Workflow Inventory

| Workflow file | Trigger | Jobs | Description |
|---|---|---|---|
| `manager-ci.yml` | push, pull_request | — | Manager CI: build, test, and validation |
| `manager-release.yml` | push tags | — | Manager release build and publishing |
| `publish.yml` | release | — | Artifact publishing to package repositories |
| `sync-release-gitee.yml` | release | — | Release mirroring to Gitee (Chinese code host) |

## Reusable Composite Actions

None defined.

## Key Actions Referenced

- Standard GitHub Actions for Android builds and publishing

## Notes

- Stellar is a pure Kotlin rewrite with native PTY AIDL and Shizuku backward-compatibility bridge.
- Dual-protocol architecture: native Stellar AIDL + Shizuku compatibility interceptor.
- Supports home screen command shortcuts for 1-tap command execution.
