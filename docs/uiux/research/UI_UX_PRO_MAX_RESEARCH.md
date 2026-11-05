# UI UX Pro Max Research - OmniShizuku

## Repository
- **URL**: https://github.com/nextlevelbuilder/ui-ux-pro-max-skill
- **Version**: 2.15.0
- **License**: MIT
- **Homepage**: https://uupm.cc

## What It Is
AI-powered design intelligence with:
- 79 searchable UI styles (50 active, 29 supplemental)
- 192 color palettes and reasoning profiles
- 74 font pairings
- 119 UX guidelines
- 25 chart types
- 22 technology stacks (including Android/Material Design)

## Platform Support
Supports: claude, cursor, windsurf, copilot, kiro, roocode, **kilocode**, codex, qoder, gemini, trae, opencode, continue, codebuddy, droid, warp, augment, antigravity, openclaw

**kilocode is listed as a supported platform** — this skill is directly applicable to the current agent.

## Installation
```
npx ui-ux-pro-max-cli init --ai kilocode
```
Or via skills.sh:
```
npx skills add https://github.com/nextlevelbuilder/ui-ux-pro-max-skill --skill ui-ux-pro-max
```

## Key Features Relevant to OmniShizuku

### Design System Generation
- Master + Overrides pattern: `design-system/<project>/MASTER.md` + `design-system/<project>/pages/<page>.md`
- Three-layer token architecture: Primitive → Semantic → Component
- `--design-system` flag generates coherent product-wide visual direction
- `--domain` flag for targeted concerns (e.g., accessibility, touch interaction)
- `--stack` flag for stack-specific guidelines (Android, Material Design, etc.)
- Design Dials: variance (1-10), motion (1-10), density (1-10)

### Relevant Stacks
Android and Material Design are among the 22 supported stacks, with implementation-specific best practices.

### Searchable Data
- UI styles: 79 (50 active)
- Color palettes: 192
- Font pairings: 74
- UX guidelines: 119
- Icons: 105 curated
- Chart types: 25

## Recommendations Adopted for OmniShizuku

| Recommendation | Adoption | Reason |
|----------------|----------|--------|
| Three-layer token system (Primitive → Semantic → Component) | Yes | Matches Material Design 3's color role system; scalable for OmniShizuku's utility surface |
| Master + Overrides pattern for design system docs | Yes | Enables page-specific overrides while maintaining global consistency |
| Design Dials (variance, motion, density) | Yes | Useful for tuning the utility aesthetic vs. information density |
| Android/Material Design stack guidelines | Yes | Directly applicable to OmniShizuku's Compose + MD3 Expressive foundation |
| 119 UX guidelines | Yes | Reference for accessibility, touch targets, spacing, typography decisions |

## Files Created
- `docs/uiux/research/UI_UX_PRO_MAX_RESEARCH.md` (this file)

## Not Adopted
- Chart types (25): OmniShizuku is a system utility, not an analytics dashboard; charts are not a primary need
- Web-focused stacks (React, Next.js, etc.): Not applicable to native Android

## Integration Plan
Use UI UX Pro Max's design-system generation workflow to:
1. Define OmniShizuku's token system (color, typography, spacing, shape)
2. Generate page-specific design overrides for each screen
3. Reference UX guidelines for accessibility and interaction decisions
