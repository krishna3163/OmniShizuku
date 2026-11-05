# OmniShizuku — Agent Instructions

This file guides any AI agent working on the OmniShizuku project.

## Essential Context

- **Project**: OmniShizuku — Android system utility platform (Shizuku fork)
- **Package**: `com.krishna.omnishizuku` (namespace: `moe.shizuku.manager`)
- **Root**: `/home/krishna/Downloads/tv/OmniShizuku`
- **UI Framework**: Jetpack Compose + Material Design 3 Expressive
- **Min SDK**: 26 | **Target SDK**: 37 | **Compile SDK**: 37
- **Build**: Gradle 10

## Project Memory (READ FIRST)

Before doing any work, read these files in order:

1. `docs/project-memory/CURRENT_STATE.md` — Project identity, build status, Git state
2. `docs/project-memory/SESSION_HANDOFF.md` — What was done, what's next
3. `docs/project-memory/MASTER_TODO.md` — All tasks with priorities and acceptance criteria
4. `docs/project-memory/IN_PROGRESS.md` — Currently active tasks
5. `docs/project-memory/DECISION_LOG.md` — Past architectural decisions
6. `docs/uiux/PRODUCT.md` — Product identity, principles, personas
7. `docs/uiux/DESIGN.md` — Design system, tokens, components, motion

## Golden Rules

1. **Preserve existing code** — Do not erase working features, integrations, or architecture
2. **No fake system state** — Real device data only; no fabricated connection/battery/storage metrics
3. **Destructive operations need confirmation** — Debloat, freeze, uninstall require user confirmation
4. **Never claim success without evidence** — Build and test before marking complete
5. **Update project memory** — After every meaningful task, update MASTER_TODO.md, COMPLETED_WORK.md, CURRENT_STATE.md
6. **No AGENTS.md edits that destroy existing instructions** — Append only
7. **Support all form factors** — Phone, tablet, TV, Wear OS

## Build & Test Commands

```bash
cd /home/krishna/Downloads/tv/OmniShizuku

# Compile Kotlin
./gradlew :manager:compileDebugKotlin

# Run unit tests
./gradlew :manager:testDebugUnitTest

# Build debug APK
./gradlew :manager:assembleDebug

# Install on tablet (device MBRKCEQGUKOJLV6X)
adb -s MBRKCEQGUKOJLV6X install -r manager/build/outputs/apk/debug/manager-debug.apk

# Check Git status
git status && git log --oneline -5
```

## Current State (as of 2026-11-05)

- **Branch**: `main`, commit `4a67615`
- **Uncommitted**: `manager/src/main/AndroidManifest.xml` (ThemePatcher activity + service)
- **Untracked**: `themepatcher/` directory (Activity, Service, layout)
- **Build**: BUILD SUCCESSFUL (compile + tests + assemble)
- **Tests**: All 15 unit test files pass
- **APK**: Installed on tablet MBRKCEQGUKOJLV6X

## UI/UX Engineering Program Status

| Phase | Status |
|-------|--------|
| Phase 0: Discovery | Complete |
| Phase 1: Tooling (research) | Complete |
| Phase 2: Product & Design System | Complete (PRODUCT.md, DESIGN.md) |
| Phase 3: UI Audit | Complete (BASELINE_AUDIT.md, accessibility, performance) |
| Phase 4: Foundation | In Progress (design tokens, components) |
| Phase 5: Screen Redesign | Not started |
| Phase 6: Motion & Interaction | Not started |
| Phase 7: Quality | Not started |
| Phase 8: Final Review | Not started |

## Key Files

| Path | Purpose |
|------|---------|
| `manager/build.gradle` | Manager module build config, dependencies |
| `manager/src/main/AndroidManifest.xml` | App manifest |
| `manager/src/main/java/moe/shizuku/manager/ui/theme/Theme.kt` | Theme system (dark/light/AMOLED/dynamic color) |
| `manager/src/main/java/moe/shizuku/manager/ui/ShizukuApp.kt` | App shell, navigation, tab bar |
| `manager/src/main/java/moe/shizuku/manager/MainActivity.kt` | Entry point |
| `manager/src/main/java/moe/shizuku/manager/ui/component/` | Reusable Compose components |
| `manager/src/main/java/moe/shizuku/manager/ui/screen/` | All Compose screens |
| `docs/uiux/PRODUCT.md` | Product identity |
| `docs/uiux/DESIGN.md` | Design system |

## UI Component Patterns

Use existing components from `ui/component/`:
- `PillButton`, `ChoiceRow`, `ExpressiveCard`, `ExpressiveSwitch`
- `SegmentedList`, `CardOutline`, `ScrollToTopButton`

When creating new components, follow DESIGN.md token system and add to `ui/component/`.

## Do NOT

- Do not use decorative gradients, glassmorphism, or emoji as icons
- Do not create cards inside cards
- Do not fake connection status or device metrics
- Do not break root/ADB/Shizuku functionality
- Do not mark tasks complete without build + test verification
