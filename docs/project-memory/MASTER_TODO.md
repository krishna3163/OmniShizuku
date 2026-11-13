# Master TODO - OmniShizuku UI/UX Engineering Program

## Status Legend
- [ ] Pending
- [~] In Progress
- [x] Completed
- [!] Blocked
- [-] Deferred

---

## Phase 0 — Discovery

| ID | Priority | Title | Requirements | Affected | Dependencies | Acceptance Criteria | Status |
|----|----------|-------|-------------|----------|--------------|---------------------|--------|
| P0-01 | P0 | Inspect source architecture | Read all modules, packages, build files | All | None | Architecture documented in BASELINE_AUDIT.md | [x] |
| P0-02 | P0 | Inspect existing UI | Read all screens, components, theme | All screens | None | UI patterns documented | [x] |
| P0-03 | P0 | Inspect feature completeness | List all features, status | All features | None | FEATURE_STATUS.md created | [x] |
| P0-04 | P0 | Establish baseline build | Run compile + tests | Build | None | Build SUCCESSFUL, tests PASS | [x] |
| P0-05 | P0 | Inspect Git state | Branch, commits, uncommitted changes | Git | None | Git state documented | [x] |

---

## Phase 1 — Tooling

| ID | Priority | Title | Requirements | Affected | Dependencies | Acceptance Criteria | Status |
|----|----------|-------|-------------|----------|--------------|---------------------|--------|
| P1-01 | P1 | Research UI UX Pro Max | Inspect repo, installation, skills | docs/uiux/research/ | None | UI_UX_PRO_MAX_RESEARCH.md created | [ ] |
| P1-02 | P1 | Research Impeccable | Inspect repo, commands, methods | docs/uiux/research/ | None | IMPECCABLE_RESEARCH.md created | [ ] |
| P1-03 | P1 | Research Emil Kowalski Skills | Read skill files, adapt to Android | docs/uiux/research/ | None | EMIL_SKILLS_RESEARCH.md created | [ ] |
| P1-04 | P1 | Research Awesome Design MD | Inspect DESIGN.md references | docs/uiux/research/ | None | AWESOME_DESIGN_MD_RESEARCH.md created | [ ] |
| P1-05 | P1 | Install compatible tools | Install skills that work with this agent | docs/ | None | Tools verified available | [ ] |
| P1-06 | P1 | Document commands | Record all available commands | docs/uiux/research/ | None | Command reference documented | [ ] |

---

## Phase 2 — Product and Design System

| ID | Priority | Title | Requirements | Affected | Dependencies | Acceptance Criteria | Status |
|----|----------|-------|-------------|----------|--------------|---------------------|--------|
| P2-01 | P1 | Define product identity | User personas, core principles, navigation model | docs/uiux/PRODUCT.md | P1 phase | PRODUCT.md written | [ ] |
| P2-02 | P1 | Define design system | Color tokens, typography, spacing, shapes, elevation | docs/uiux/DESIGN.md | P2-01 | DESIGN.md written with token system | [ ] |
| P2-03 | P1 | Create component inventory | List all existing components, variants | docs/uiux/COMPONENT_INVENTORY.md | P2-02 | Inventory complete | [ ] |
| P2-04 | P1 | Create screen inventory | List all screens, current state | docs/uiux/SCREEN_INVENTORY.md | P2-02 | Inventory complete | [ ] |

---

## Phase 3 — UI Audit

| ID | Priority | Title | Requirements | Affected | Dependencies | Acceptance Criteria | Status |
|----|----------|-------|-------------|----------|--------------|---------------------|--------|
| P3-01 | P1 | Audit Home Dashboard | Review against DESIGN.md | HomeScreen | P2 phase | Issues identified and prioritized | [ ] |
| P3-02 | P1 | Audit Settings Screen | Review against DESIGN.md | SettingsScreen | P2 phase | Issues identified and prioritized | [ ] |
| P3-03 | P1 | Audit Apps Screen | Review against DESIGN.md | AppsScreen | P2 phase | Issues identified and prioritized | [ ] |
| P3-04 | P1 | Audit Labs Screen | Review against DESIGN.md | LabsScreen | P2 phase | Issues identified and prioritized | [ ] |
| P3-05 | P1 | Audit all remaining screens | 15+ screens | All screens | P2 phase | All screens audited | [ ] |
| P3-06 | P1 | Identify duplicated components | Find copy-paste UI patterns | All components | P3 phase | Duplication documented | [ ] |
| P3-07 | P1 | Accessibility audit | TalkBack, contrast, touch targets | All screens | P3 phase | ACCESSIBILITY_AUDIT.md created | [ ] |
| P3-08 | P1 | Performance audit | Scroll jank, recomposition, main thread | All screens | P3 phase | PERFORMANCE_AUDIT.md created | [ ] |

---

## Phase 4 — Foundation

| ID | Priority | Title | Requirements | Affected | Dependencies | Acceptance Criteria | Status |
|----|----------|-------|-------------|----------|--------------|---------------------|--------|
| P4-01 | P1 | Implement shared design tokens | Color, typography, spacing, shape tokens | ui/theme/ | P2-02 | Token system in code | [ ] |
| P4-02 | P1 | Implement reusable components | Buttons, switches, status indicators, section headings | ui/component/ | P4-01 | Components compiled and used | [ ] |
| P4-03 | P1 | Establish navigation consistency | Tab bar, detail screens, back handling | ShizukuApp.kt | P4-02 | Navigation unified | [ ] |
| P4-04 | P1 | Empty/loading/error states | Consistent states across screens | All screens | P4-02 | States implemented | [ ] |
| P4-05 | P1 | Theme consistency | Dark, light, AMOLED, system color | ui/theme/ | P4-01 | All themes verified | [ ] |

---

## Phase 5 — Screen Redesign

| ID | Priority | Title | Requirements | Affected | Dependencies | Acceptance Criteria | Status |
|----|----------|-------|-------------|----------|--------------|---------------------|--------|
| P5-01 | P1 | Home Dashboard | Device identity, status, quick actions | HomeScreen | P4 phase | Redesigned, quality gates pass | [x] |
| P5-02 | P1 | Connection Centre | Shizuku, ADB, wireless debugging, root | Settings-related | P4 phase | Redesigned, quality gates pass | [x] |
| P5-03 | P1 | App Manager | List, filters, search, actions | AppsScreen | P4 phase | Redesigned, quality gates pass | [~] |
| P5-04 | P1 | Debloater | Discovery, risk, selection, preview | DebloaterScreen | P4 phase | Redesigned, quality gates pass | [ ] |
| P5-05 | P1 | Automation Centre | Tasks, triggers, execution state | Labs-related | P4 phase | Redesigned, quality gates pass | [ ] |
| P5-06 | P1 | Module Manager | Installed modules, status, actions | ModulesScreen | P4 phase | Redesigned, quality gates pass | [ ] |
| P5-07 | P1 | Terminal | Command input, output history | TerminalScreen | P4 phase | Redesigned, quality gates pass | [ ] |
| P5-08 | P1 | File Manager | Navigation, info, permissions | FilesScreen | P4 phase | Redesigned, quality gates pass | [ ] |
| P5-09 | P1 | Call Recording | Status, controls, recordings | CallRecorderScreen | P4 phase | Redesigned, quality gates pass | [ ] |
| P5-10 | P1 | Device Diagnostics | CPU, memory, storage, battery | DeviceInfoScreen | P4 phase | Redesigned, quality gates pass | [ ] |
| P5-11 | P1 | Settings | Appearance, theme, behavior, permissions | SettingsScreen | P4 phase | Redesigned, quality gates pass | [ ] |
| P5-12 | P2 | Remaining screens | All other screens not listed above | All remaining | P4 phase | All redesigned | [ ] |

---

## Phase 6 — Motion and Interaction

| ID | Priority | Title | Requirements | Affected | Dependencies | Acceptance Criteria | Status |
|----|----------|-------|-------------|----------|--------------|---------------------|--------|
| P6-01 | P2 | Audit existing animations | Review all animated transitions | All screens | P5 phase | Animation inventory | [ ] |
| P6-02 | P2 | Identify animation opportunities | High-value transitions only | All screens | P6-01 | Opportunities documented | [ ] |
| P6-03 | P2 | Remove unnecessary animations | Eliminate decorative motion | All screens | P6-02 | Unnecessary animations removed | [ ] |
| P6-04 | P2 | Implement high-value transitions | Screen navigation, dialogs, bottom sheets | All screens | P6-03 | Transitions implemented | [ ] |
| P6-05 | P2 | Optimize animation performance | Frame rate, main thread | All screens | P6-04 | No jank | [ ] |
| P6-06 | P2 | Verify reduced-motion support | Accessibility setting respected | All screens | P6-04 | Reduced motion works | [ ] |

---

## Phase 7 — Quality

| ID | Priority | Title | Requirements | Affected | Dependencies | Acceptance Criteria | Status |
|----|----------|-------|-------------|----------|--------------|---------------------|--------|
| P7-01 | P1 | Accessibility audit | TalkBack, labels, contrast, touch targets | All screens | P5 phase | All issues fixed | [ ] |
| P7-02 | P1 | Theme audit | Dark, light, AMOLED, system color | All screens | P5 phase | All themes verified | [ ] |
| P7-03 | P1 | Screen-size audit | Phone, tablet, foldable | All screens | P5 phase | All form factors work | [ ] |
| P7-04 | P1 | UI overflow testing | Long text, many items | All screens | P5 phase | No overflow | [ ] |
| P7-05 | P1 | Interaction testing | Button taps, switches, gestures | All screens | P5 phase | All interactions work | [ ] |
| P7-06 | P1 | Build testing | Debug + release builds | Build | P5 phase | Both build successfully | [ ] |
| P7-07 | P1 | Regression testing | Existing features still work | All features | P5 phase | No regressions | [ ] |

---

## Phase 8 — Final Review

| ID | Priority | Title | Requirements | Affected | Dependencies | Acceptance Criteria | Status |
|----|----------|-------|-------------|----------|--------------|---------------------|--------|
| P8-01 | P2 | Impeccable critique | Review against design principles | All screens | P7 phase | Critique documented | [ ] |
| P8-02 | P2 | Impeccable audit | Quality audit | All screens | P8-01 | Audit completed | [ ] |
| P8-03 | P2 | Impeccable polish | Final visual polish | All screens | P8-02 | Polish applied | [ ] |
| P8-04 | P2 | Emil animation review | Motion quality review | All screens | P6 phase | Animations verified | [ ] |
| P8-05 | P2 | Final screenshots | Document final state | docs/uiux/screenshots/ | P8 phase | Screenshots captured | [ ] |
| P8-06 | P2 | Documentation update | All memory files current | docs/ | P8 phase | All docs updated | [ ] |
| P8-07 | P2 | Handoff preparation | SESSION_HANDOFF.md complete | docs/project-memory/ | P8 phase | Handoff ready | [ ] |

---

## Notes
- Only mark a task [x] after implementation AND verification
- If a feature doesn't exist, classify as planned or missing
- Never erase previous project history
- Update all memory files at every meaningful checkpoint

---

## Labs Redesign (LAB-001..LAB-039)

| ID | Priority | Title | Requirements | Affected | Dependencies | Acceptance Criteria | Status |
|----|----------|-------|-------------|----------|--------------|---------------------|--------|
| LAB-000 | P0 | Labs complete audit | Audit all 39 entries | docs/uiux/LABS_COMPLETE_AUDIT.md | None | Audit doc created | [x] |
| LAB-001 | P1 | App Ops detail redesign | Per-app ops | AppOpsScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-002 | P1 | Firewall detail redesign | Network rules | FirewallScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-003 | P1 | Autostart detail redesign | Startup mgmt | AutostartScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-004 | P1 | Hiding detail redesign | Visibility mgmt | HidingScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-005 | P1 | Force Dark detail redesign | Per-app dark | ForceDarkScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-006 | P1 | Debloater detail redesign | Package mgmt | DebloaterScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-007 | P1 | App Freezer detail redesign | Freeze apps | FreezerScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-008 | P1 | Axeron Gaming Optimizer redesign | Perf profiles | OptimizerScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-009 | P1 | Power detail redesign | Restart actions | PowerScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-010 | P1 | Display detail redesign | Resolution/dpi | DisplayScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-011 | P1 | Wi-Fi Passwords redesign | Saved networks | WifiPasswordsScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-012 | P1 | SystemUI Tuner redesign | Status bar | SystemUiTunerScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-013 | P1 | Enforce Doze redesign | Doze controls | EnforceDozeScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-014 | P1 | Private DNS redesign | DNS config | PrivateDnsScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-015 | P1 | Audio Stream Master redesign | Volume streams | AudioMasterScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-016 | P1 | Carrier Tethering redesign | Tethering | TetherScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-017 | P1 | Mixed Audio redesign | Audio focus | MixedAudioScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-018 | P1 | SoundMaster redesign | Per-app volume | SoundMasterScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-019 | P1 | UniversalPiP redesign | PiP controls | UniversalPipScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-020 | P1 | Screen Security & DRM redesign | FLAG_SECURE info | SecurityScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-021 | P1 | Auto DND redesign | DND automation | AutoDndScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-022 | P1 | Theme Patcher redesign | Theme patching | ThemePatcherScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-023 | P1 | DFRoot redesign | Kernel compat | DfrootScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-024 | P1 | Shell redesign | Terminal | ShellScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-025 | P1 | Log redesign | Logcat | LogScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-026 | P1 | Device Info redesign | Hardware info | DeviceInfoScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-027 | P1 | Activities Launcher redesign | Activity launch | ActivitiesScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-028 | P1 | Files redesign | File browser | FilesScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-029 | P1 | App Installer redesign | APK install | InstallScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-030 | P1 | Terminal Apps redesign | Terminal integration | TerminalScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-031 | P1 | LocalShell redesign | Local ADB bridge | LocalShellScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-032 | P1 | Intents redesign | Intent builder | IntentsScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-033 | P1 | Stealth Mode redesign | Package visibility | StealthScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-034 | P1 | Permissions redesign | Runtime perms | PermissionsScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-035 | P1 | Shizuku Store redesign | App catalog | MoreAppsScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-036 | P1 | Call Recording redesign | Recording engine | CallRecordingScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-037 | P1 | WallYou redesign | Wallpaper engine | WallYouScreen | LAB-000 | Redesigned + tested | [ ] |
| LAB-038 | P1 | Quick Settings Tiles companion | Integration | Companion entry | LAB-000 | Redesigned + tested | [ ] |
| LAB-039 | P1 | ShizuWall companion | Integration | Companion entry | LAB-000 | Redesigned + tested | [ ] |
