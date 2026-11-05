# Awesome Design MD Research - OmniShizuku

## Repository
- **URL**: https://github.com/VoltAgent/awesome-design-md
- **Description**: Collection of DESIGN.md files from popular brand design systems
- **Concept**: Google Stitch's DESIGN.md — plain-text design system document AI agents read to generate consistent UI

## DESIGN.md Structure (Standard)
1. Visual identity summary
2. Colors (with token references like `{colors.primary}`)
3. Typography rules (font families, full hierarchy table)
4. Component stylings (buttons, cards, inputs, navigation with states)
5. Layout principles (spacing scale, grid, whitespace)
6. Depth & elevation (shadow system, surface hierarchy)
7. Do's and Don'ts (guardrails, anti-patterns)
8. Responsive behavior (breakpoints, touch targets, collapsing)
9. Agent prompt guide (color reference, ready prompts)

## Design Systems Reviewed (Relevant to OmniShizuku)

### Raycast (design-md/raycast/DESIGN.md)
- **Key traits**: Hairline 1px borders, no drop shadows, Inter with ss03 stylistic set, dark mode only
- **Relevance**: Utility app aesthetic, minimal shadows, hairline borders — good for system utility
- **Adopt**: Hairline borders, no shadows, Inter typography, dark-first approach

### Warp (design-md/warp/DESIGN.md)
- **Key traits**: Dark canvas (#2b2622), Inter, hairline borders, rounded.none to pill, understated geometry
- **Relevance**: Terminal/developer tool aesthetic — highly relevant for OmniShizuku's shell/terminal features
- **Adopt**: Dark-first canvas, Inter, hairline borders, restrained accent

### Linear (design-md/linear.app/DESIGN.md)
- **Key traits**: (Listed in directory, not read in detail)
- **Relevance**: Known for excellent utility app design
- **Adopt**: Review for reference

### VoltAgent (design-md/voltagent/DESIGN.md)
- **Key traits**: Dark canvas (#101010), Inter, hairline borders, no shadows
- **Relevance**: Developer tool aesthetic
- **Adopt**: Dark canvas, Inter, minimal shadows

### NVIDIA (design-md/nvidia/DESIGN.md)
- **Key traits**: Single accent green (#76b900), 2px rounded, monochrome, technical CAD feel
- **Relevance**: Technical utility aesthetic, single accent color
- **Adopt**: Single accent color principle, technical restraint

### Zapier (design-md/zapier/DESIGN.md)
- **Key traits**: Degular Display + Inter, rounded.md 12px, cream canvas
- **Relevance**: Automation/utility platform
- **Adopt**: Two-face typography, moderate rounding

### Figma (design-md/figma/DESIGN.md)
- **Key traits**: Monochrome core, color blocks, figmaSans, block-lime accent
- **Relevance**: Color block system for section differentiation
- **Adopt**: Color block sections for feature grouping

## Design Principles Extracted for OmniShizuku

| Principle | Source | Application |
|-----------|--------|-------------|
| Dark-first canvas | Raycast, Warp, VoltAgent, NVIDIA | OmniShizuku defaults to dark theme |
| Hairline borders, no shadows | Raycast, Warp, VoltAgent | Clean utility aesthetic, less visual noise |
| Inter typography | Raycast, Warp, VoltAgent, NVIDIA, Zapier | System font, highly readable |
| Single accent color | NVIDIA | One brand color for CTAs and active states |
| Technical restraint | NVIDIA, Warp | No decorative gradients, glassmorphism, or excess |
| Color block sections | Figma | Feature grouping with subtle background variation |
| 4px base spacing | Wired, Clay | Consistent spacing system |

## Recommendations Adopted for OmniShizuku

| Recommendation | Adoption | Reason |
|----------------|----------|--------|
| Dark-first design | Yes | OmniShizuku already defaults to dark; matches utility aesthetic |
| Hairline borders over shadows | Yes | Cleaner, more technical look appropriate for system tools |
| Inter/system typography | Yes | Already using system fonts via Compose |
| Single accent color | Yes | Brand indigo (#3F51B5) as the single accent |
| No decorative gradients/glassmorphism | Yes | Already avoided in existing UI |
| DESIGN.md as single source of truth | Yes | Required by master execution prompt |

## Files Created
- `docs/uiux/research/AWESOME_DESIGN_MD_RESEARCH.md` (this file)

## Integration Plan
Use Awesome Design MD principles to:
1. Define OmniShizuku's visual identity in DESIGN.md
2. Select references: Raycast (utility), Warp (developer tool), NVIDIA (technical restraint), Linear (utility app)
3. Create one consistent DESIGN.md for OmniShizuku
4. Use as reference for all screen redesigns
