# Phase 1 Study Report

## Repository: VoltAgent/awesome-design-md
- 84 DESIGN.md files from real brand design systems
- Cloned to /tmp/awesome-design-md

## Most Relevant Systems

### Raycast - Utility app aesthetic (highest match)
- Dark canvas #07080a, hairline 1px borders, no drop shadows
- Inter typography, single white CTA pill, 4-step surface ladder
- Adopt: Hairline borders over shadows, dark-first, restrained accent

### Warp - Terminal/developer tool
- Warm near-charcoal #2b2622, Inter, hairline borders
- Tight geometry (3-4px radius), monospace for code
- Adopt: Dark-first canvas, monospace terminal typography

### NVIDIA - Technical utility
- Single accent green #76b900, 2px radius, no gradients/shadows
- Adopt: Single accent color, technical restraint, tight radius

### Linear - Utility app design
- Near-black #010102, lavender accent, dense technical layout
- Adopt: Dense readable info, dark panels for content

### Ollama - Terminal-first
- Paper-white, 36px heading, single black pill CTA, ui-monospace
- Adopt: Minimal decoration, monospace for terminal content

## Extracted Principles

| Principle | Source | Application |
|-----------|--------|-------------|
| Dark-first canvas | Raycast, Warp | OmniShizuku defaults to dark |
| Hairline borders, no shadows | Raycast, Warp, VoltAgent | Clean utility aesthetic |
| Inter/system typography | Raycast, Warp, NVIDIA | System font, readable |
| Single accent color | NVIDIA | Brand indigo #3F51B5 |
| Technical restraint | NVIDIA, Warp | No gradients/glassmorphism |
| Color block sections | Figma | Feature grouping |
| 4px base spacing | Wired, Clay | Consistent spacing |
| Tight radius (2-12px) | NVIDIA, Warp | No excessive rounding |
| Surface ladder for depth | Raycast, Linear | Background steps not shadows |

## Key Findings
1. All top utility/developer tools use hairline borders instead of shadows
2. Single accent color is universal across developer tools
3. Dark-first canvas dominates developer tools (6 of 7 most relevant)
4. Tight radius vocabulary (2-12px) - none use 16-24px common in consumer apps
5. Monospace for terminal/code content is standard
6. Surface ladder replaces shadows for depth

## OmniShizuku Design Direction
- Keep dark-first Material Design 3 foundation
- Replace remaining box shadows with hairline borders
- Tighten corner radii from 14-16px to 8-12px range
- Use brand indigo #3F51B5 as single accent for CTAs/active states
- Add monospace font family for terminal/shell screens
- Use surface ladder for card elevation instead of shadows
- Apply 4px base spacing system consistently
