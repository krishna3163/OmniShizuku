# Baseline UI Audit - OmniShizuku

## Audit Date
2026-11-05

## Auditor
Kilo (UI/UX Engineering Program)

## Methodology
Inspected all Compose screens, XML Activities, theme system, components, and navigation patterns. Compared against DESIGN.md principles and Impeccable audit criteria (accessibility, performance, theming, responsive design, implementation integrity).

## Overall Score: 2.5/4

The application has a solid Material Design 3 Expressive foundation but lacks design system consistency, token usage, and component reuse across screens.

---

## Screen-by-Screen Audit

| Screen | File | Consistency | Accessibility | Performance | Notes |
|--------|------|-------------|---------------|-------------|-------|
| Home | `ui/screen/HomeScreen.kt` (1242 lines) | 2/4 | 2/4 | 2/4 | Large file, mixed patterns, no token system |
| Apps | `ui/screen/AppsScreen.kt` | 2/4 | 2/4 | 2/4 | List-based, needs empty/error states |
| Settings | `settings/SettingsScreen.kt` (1542 lines) | 2/4 | 2/4 | 2/4 | Mixed XML/Compose, very large file |
| Modules | `module/ModulesScreen.kt` | 3/4 | 3/4 | 3/4 | Better consistency, Compose-based |
| Labs | `ui/screen/LabsScreen.kt` | 2/4 | 2/4 | 2/4 | Needs reorganization |
| Shell | `ui/screen/ShellScreen.kt` | 2/4 | 2/4 | 2/4 | Terminal UI needs improvement |
| Terminal | `ui/screen/TerminalScreen.kt` | 2/4 | 2/4 | 2/4 | Monospace, output history |
| Device Info | `ui/screen/DeviceInfoScreen.kt` | 2/4 | 2/4 | 2/4 | Metrics display needs cards |
| Display | `ui/screen/DisplayScreen.kt` | 2/4 | 2/4 | 2/4 | Resolution/density/refresh controls |
| Power | `ui/screen/PowerScreen.kt` | 2/4 | 2/4 | 2/4 | Reboot/power actions |
| Debloater | `debloater/DebloaterScreen.kt` | 2/4 | 2/4 | 2/4 | Needs confirmation dialogs |
| Freezer | `freeze/FreezerScreen.kt` | 2/4 | 2/4 | 2/4 | Freeze/unfreeze actions |
| Doze | `doze/DozeScreen.kt` | 2/4 | 2/4 | 2/4 | Recently ported, needs UI polish |
| ThemePatcher | `themepatcher/ThemePatcherScreen.kt` | 2/4 | 2/4 | 2/4 | Recently ported, XML layout |
| SoundMaster | `audio/SoundMasterScreen.kt` | 3/4 | 3/4 | 3/4 | Audio controls, decent consistency |
| MixedAudio | `audio/MixedAudioLauncher.kt` | 3/4 | 3/4 | 3/4 | Audio mixing UI |
| Lookback | `lookback/LookbackLauncher.kt` | 3/4 | 3/4 | 3/4 | App downgrade UI |
| Universal PiP | `pip/UniversalPiPScreen.kt` | 3/4 | 3/4 | 3/4 | PiP configuration |
| Local Shell | `shell/LocalShellScreen.kt` | 2/4 | 2/4 | 2/4 | Local shell without Shizuku |
| Call Recording | `callrecorder/CallRecorderScreen.kt` | 2/4 | 2/4 | 2/4 | Recording controls |
| DFRoot | `dfroot/DFRootScreen.kt` | 2/4 | 2/4 | 2/4 | Root exploit UI |
| WallYou | `wallyou/WallYouScreen.kt` | 2/4 | 2/4 | 2/4 | Wallpaper browser |
| Stealth | `ui/screen/StealthScreen.kt` | 2/4 | 2/4 | 2/4 | Stealth mode |
| Intents | `ui/screen/IntentsScreen.kt` | 2/4 | 2/4 | 2/4 | Intent management |
| Permissions | `ui/screen/PermissionsScreen.kt` | 2/4 | 2/4 | 2/4 | Permission management |
| Network Firewall | `settings/NetworkFirewallActivity.kt` | 1/4 | 1/4 | 2/4 | XML Activity, outdated pattern |
| Bug Report | `settings/BugReportDialogActivity.kt` | 1/4 | 1/4 | 2/4 | XML Activity, outdated pattern |

## Component Audit

### Existing Components (`ui/component/`)
| Component | File | Reuse | Consistency | Notes |
|-----------|------|-------|-------------|-------|
| PillButton | `PillButton.kt` | High | 3/4 | Used in multiple screens |
| ChoiceRow | `ChoiceRow.kt` | High | 3/4 | Settings rows |
| ExpressiveCard | `ExpressiveCard.kt` | Medium | 3/4 | MD3 Expressive card |
| ExpressiveSwitch | `ExpressiveSwitch.kt` | Medium | 3/4 | Themed switch |
| SegmentedList | `SegmentedList.kt` | Low | 2/4 | Segmented control |
| CardOutline | `CardOutline.kt` | Low | 2/4 | Card border |
| ScrollToTopButton | `ScrollToTopButton.kt` | Low | 3/4 | Scroll UX |
| FeaturePlateIcon | `FeaturePlateIcon.kt` | Low | 2/4 | Feature icon |
| TelevisionFocus | `TelevisionFocus.kt` | TV only | 3/4 | TV focus outline |

### Missing Components
- Status indicator (connected/disconnected/error)
- Section heading (consistent typography)
- Empty state component
- Error state component
- Loading state component
- Confirmation dialog component
- Connection status chip
- Metric display card

## Theme System Audit

### Current Theme (`ui/theme/Theme.kt`)
| Aspect | Status | Notes |
|--------|--------|-------|
| Dark theme | ✅ | Fully implemented |
| Light theme | ✅ | Via Material 3 dynamic |
| AMOLED black | ✅ | Custom black theme |
| Dynamic color | ✅ | Android 12+ wallpaper colors |
| Brand color | ✅ | Indigo #3F51B5 |
| Token system | ❌ | No design tokens; uses Material 3 roles directly |
| Component tokens | ❌ | No component-level tokens |

### Issues
- No semantic token aliases (success, warning, error)
- No spacing scale tokens
- No typography scale tokens
- Colors used directly from MaterialTheme, not from custom tokens

## Navigation Audit

### Current Navigation (`ui/ShizukuApp.kt`)
| Aspect | Status | Notes |
|--------|--------|-------|
| Tab bar | ✅ | 5 tabs, floating bottom bar |
| Detail screens | ✅ | Slide over tabs |
| Tab pill animation | ✅ | Spring-based, smooth |
| TV mode | ✅ | Navigation rail |
| Wear OS | ✅ | Limited support |
| Back handling | ✅ | Detail screens pop correctly |

### Issues
- No deep linking support
- No screen titles in tab bar (icons only)
- No breadcrumb navigation
- Tab count fixed at 5; no extensibility

## Accessibility Audit

| Check | Status | Notes |
|-------|--------|-------|
| TalkBack labels | Partial | Some icons have contentDescription |
| Touch targets | Partial | Most buttons > 48dp, some smaller |
| Text contrast | Partial | Material 3 provides good contrast by default |
| Font scaling | Partial | Uses sp for text in most places |
| Reduced motion | ❌ | No check for isSystemInReducedMotionMode() |
| Focus order | Partial | Logical in most screens |
| Error descriptions | Partial | Some errors shown, not all |

## Performance Audit

| Check | Status | Notes |
|-------|--------|-------|
| Scroll performance | Good | LazyColumn used for lists |
| Recomposition | Moderate | Some screens may over-recompose |
| Main thread work | Moderate | Shell commands on background threads |
| App launch | Good | Fast launch, warm-up after 1500ms |
| Bundle size | Unknown | Not analyzed |

## Implementation Integrity Audit

| Check | Status | Notes |
|-------|--------|-------|
| Hard-coded colors | ❌ | Some screens use raw Color values |
| Design system drift | ❌ | No design system; each screen varies |
| Token consistency | ❌ | No token system in place |
| Decorative UI | Minimal | Mostly functional UI |
| Fake data | ✅ | No fake system state observed |

## Top Issues (Priority Order)

### P0 (Blocking)
1. **No design token system** — Colors, typography, spacing used inconsistently across screens
2. **No component library** — Common patterns (status indicators, empty states, section headings) recreated per screen
3. **XML Activities mixed with Compose** — NetworkFirewallActivity, BugReportDialogActivity use old View system

### P1 (High)
4. **Large screen files** — HomeScreen.kt (1242 lines), SettingsScreen.kt (1542 lines) need decomposition
5. **No reduced-motion support** — Accessibility gap
6. **Inconsistent navigation patterns** — Some screens use TopAppBar, others don't

### P2 (Medium)
7. **No empty/error/loading states** — Inconsistent across screens
8. **No confirmation dialogs for destructive actions** — Debloater, freezer, uninstall
9. **Tablet layout not optimized** — Centered content exists but could be improved

### P3 (Polish)
10. **Motion could be more purposeful** — Some animations decorative rather than communicative
11. **Icon style inconsistency** — Mix of filled, outlined, rounded icons

---

## Files Created
- `docs/uiux/BASELINE_AUDIT.md` (this file)
