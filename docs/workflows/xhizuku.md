# Xhizuku — Project Action Inventory

**Repository:** `xeonleonreal/Xhizuku`
**Local path:** `/home/krishna/Downloads/tv/Shizuku implementations/Xhizuku`
**Upstream:** `https://github.com/xeonleonreal/Xhizuku.git`
**Branch:** `main` / `245d312732f830c653e2ab7e15366d0dad1560c5`

## Workflow Inventory

| Workflow file | Trigger | Jobs | Description |
|---|---|---|---|
| `app.yml` | push, pull_request | — | Android application build and test workflow |
| `issue-similarity.yml` | issues | — | Duplicate issue detection using similarity matching |

## Reusable Composite Actions

None defined.

## Key Actions Referenced

- Standard GitHub Actions for Android builds

## Notes

- Xhizuku is an independent Shizuku fork with package `xeonleon.xhizuku`, allowing side-by-side installation with official Shizuku.
- Features Magisk-style ADB modules runner (ZIP modules with `module.prop`, `service.sh`, `action.sh`, HTML5 WebUI).
- NightDog watchdog daemon for high-reliability daemon rebinding.
- Multi-platform Jetpack Compose UI: Android TV (Leanback), Wear OS, and Phone.
