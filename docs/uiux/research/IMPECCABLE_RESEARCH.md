# Impeccable Research - OmniShizuku

## Repository
- **URL**: https://github.com/pbakaus/impeccable
- **License**: Apache-2.0
- **Commits**: 2,600+
- **Description**: The design vocabulary for your coding agent. Steer with polish and bolder, review with critique and audit, catch AI slop before it ships.

## What It Is
A design system and quality framework with 24 commands:

### Steer Commands
- `bolder` — Apply a playbook for a specific visual move to one target
- `clarify` — Rewrite unclear copy
- `colorize` — Bring color to a gray interface
- `document` — Document the visual system
- `shape` — Fix spacing and rhythm
- `typeset` — Fix fonts, hierarchy, and sizing
- `layout` — Fix spacing and rhythm
- `animate` — Add transitions and interaction feedback
- `delight` — Add personality
- `overdrive` — Shaders, spring physics, scroll-driven effects

### Review Commands
- `critique` — Scored UX review against Nielsen's 10 heuristics
- `audit` — Technical quality report (accessibility, performance, theming, responsive)

### Finish Commands
- `polish` — Final pass before shipping
- `harden` — Errors, i18n, overflow, edge cases
- `optimize` — Performance fixes
- `adapt` — Screen sizes and devices
- `onboard` — Onboarding flows

## Key Methodologies

### Critique (Design Review)
- Scores design against Nielsen's 10 heuristics (0-4 each)
- Two assessments: Assessment A (design review) + Assessment B (detector/browser evidence)
- On native platforms (Android): design review only, detector not applicable
- Design Health Score presented as table with key issues
- Report header declares method (dual-agent, degraded, native)

### Audit (Technical Quality)
- 5 dimensions scored 0-4:
  1. Accessibility (contrast 4.5:1, touch targets 44x44px, dark mode, tokens)
  2. Performance (launch time, scroll smoothness, bundle/binary weight)
  3. Theming (token usage, dark mode variants, dynamic color)
  4. Responsive Design (fixed widths, touch targets, tablet layouts)
  5. Implementation Integrity (design-system drift, hard-coded values, decoy content)
- Rating bands: 18-20 Excellent, 14-17 Good, 10-13 Acceptable

### Slop Detector
- 62 deterministic rules, no LLM, no API key
- Flags: cramped padding, skipped headings, hard-coded colors, design-system drift
- Native platform routes to `audit.native.md`

## Native Platform Support (Android)
- `skill/reference/audit.native.md` exists for iOS/Android/adaptive
- Audits from source (Compose, View system, etc.)
- Checks: hard-coded colors vs Material color roles, broken dark appearance, Dynamic Color (Android 12+), inset violations, off-platform navigation, icon drift (Material Symbols), tablet stretched layouts

## Repo Artifacts
- `config.json`, `live/config.json`, `design.json`, `surfaces/*.md`, `critique/*.md`
- `PRODUCT.md` and `DESIGN.md` kept in repo
- `.gitignore` entries for screenshots, caches, runtime state

## Recommendations Adopted for OmniShizuku

| Recommendation | Adoption | Reason |
|----------------|----------|--------|
| Critique scoring (Nielsen's 10 heuristics, 0-4) | Yes | Provides objective quality measurement for each screen |
| Audit 5-dimension scoring (0-4) | Yes | Accessibility, performance, theming, responsive, implementation integrity |
| Native audit for Android (audit.native.md patterns) | Yes | Directly applicable to Compose-based OmniShizuku |
| DESIGN.md + PRODUCT.md as repo artifacts | Yes | Required by master execution prompt |
| Surfaces/*.md and Critique/*.md pattern | Yes | Per-screen design documentation |
| Slop detector principles (62 rules) | Yes | Use as checklist for UI quality review |

## Files Created
- `docs/uiux/research/IMPECCABLE_RESEARCH.md` (this file)

## Integration Plan
Use Impeccable's critique/audit methodology to:
1. Score each OmniShizuku screen against Nielsen's 10 heuristics
2. Audit accessibility, theming, and implementation integrity
3. Document findings in `critique/*.md` and `audit/*.md` per screen
4. Use `polish`, `harden`, `shape`, `typeset` commands for targeted fixes
