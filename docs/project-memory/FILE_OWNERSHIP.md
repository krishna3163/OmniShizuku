# File Ownership - OmniShizuku

## Core Architecture
| File/Directory | Owner | Description |
|----------------|-------|-------------|
| `manager/src/main/java/moe/shizuku/manager/ui/theme/Theme.kt` | Design System | Theme, color scheme, dark/light/AMOLED modes |
| `manager/src/main/java/moe/shizuku/manager/ui/ShizukuApp.kt` | Navigation | App shell, tab bar, detail screen overlay |
| `manager/src/main/java/moe/shizuku/manager/home/HomeActivity.kt` | Home | Main entry point, hosts Compose UI |
| `manager/src/main/AndroidManifest.xml` | Build | App manifest, component declarations |

## UI Components
| File/Directory | Owner | Description |
|----------------|-------|-------------|
| `manager/src/main/java/moe/shizuku/manager/ui/component/` | Components | Reusable Compose components (PillButton, ChoiceRow, etc.) |
| `manager/src/main/java/moe/shizuku/manager/ui/compose/` | Compose Utils | Compose utilities (MotionBlur, ShizukuExpressive, TvComponents) |

## Feature Areas
| Feature | Files | Owner |
|---------|-------|-------|
| EnforceDoze | `manager/src/main/java/moe/shizuku/manager/doze/` | System utilities |
| ThemePatcher | `manager/src/main/java/moe/shizuku/manager/themepatcher/` | System utilities |
| Settings | `manager/src/main/java/moe/shizuku/manager/settings/` | Configuration |
| Home | `manager/src/main/java/moe/shizuku/manager/home/` | Dashboard |
| Apps | `manager/src/main/java/moe/shizuku/manager/ui/screen/AppsScreen.kt` | App management |
| Modules | `manager/src/main/java/moe/shizuku/manager/module/` | Module system |
| Debloater | `manager/src/main/java/moe/shizuku/manager/debloater/` | App debloating |
| Shell/Terminal | `manager/src/main/java/moe/shizuku/manager/ui/screen/TerminalScreen.kt` | Terminal |
| Device Info | `manager/src/main/java/moe/shizuku/manager/ui/screen/DeviceInfoScreen.kt` | Diagnostics |

## Documentation
| File | Owner |
|------|-------|
| `docs/project-memory/` | Project memory system |
| `docs/uiux/` | UI/UX design system |
| `README.md` | Project README |
