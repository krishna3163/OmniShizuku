# Shizako — Project Action Inventory

**Repository:** `cr1437/Shizako`
**Local path:** `/home/krishna/Downloads/tv/Shizuku implementations/Shizako`
**Upstream:** `https://github.com/cr1437/Shizako.git`
**Branch:** `main` / `e5288c4cea779f67fd21dcdfdf07dc8f193bd37c`

## Workflow Inventory

| Workflow file | Trigger | Jobs | Description |
|---|---|---|---|
| `app.yml` | push, pull_request | — | Android application build and test workflow |
| `badges.yml` | push, schedule | — | Repository build status badge generation and update |

## Reusable Composite Actions

None defined.

## Key Actions Referenced

- Standard GitHub Actions for Android builds (checkout, setup-java, Gradle)

## Notes

- Shizako is an Apache-2.0 Shizuku-compatible manager with compile/target SDK 36, min SDK 24, Java 21.
- Features hardware-accelerated OpenGL ES blur pipeline and Quick Settings tiles.
- API subproject has a separate MIT license.
