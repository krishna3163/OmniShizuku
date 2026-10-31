# ShizukuPlus — Project Action Inventory

**Repository:** `thejaustin/ShizukuPlus`
**Local path:** `/home/krishna/Downloads/tv/Shizuku implementations/ShizukuPlus`
**Upstream:** `https://github.com/thejaustin/ShizukuPlus.git`
**Branch:** `master` / `212d3a2adec30529ce7e9b896a4819c02d45b1ad`

## Workflow Inventory

| Workflow file | Trigger | Jobs | Description |
|---|---|---|---|
| `app.yml` | push, pull_request | — | Android application build and test workflow |
| `crowdin-sync.yml` | push, schedule | — | Crowdin translation synchronization |
| `gitguardian.yaml` | push, pull_request | — | Secret scanning via GitGuardian |
| `issue-auto-label.yml` | issues, pull_request | — | Automatic issue labeling |
| `pr-check.yml` | pull_request | — | PR validation and merge readiness checks |
| `security.yml` | push, pull_request, schedule | — | Security scanning and vulnerability assessment |

## Reusable Composite Actions

None defined.

## Key Actions Referenced

- Standard GitHub Actions for Android builds and security scanning

## Notes

- ShizukuPlus is an enterprise/feature-expanded fork with Room DB audit logs, 15+ server extensions, and app-process module.
- Dual flavor builds: `dropin` (official package compatibility) and `shizukuplus` (enhanced features).
- Includes automated diagnostic and AI tooling (`crash-diagnostics.sh`, LLM skills).
