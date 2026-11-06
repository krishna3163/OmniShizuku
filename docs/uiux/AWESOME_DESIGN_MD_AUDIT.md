# Awesome Design MD — Phase 1 Study Report

## Repository Inspected
- **URL**: https://github.com/VoltAgent/awesome-design-md.git
- **Cloned to**: `/tmp/awesome-design-md`
- **Content**: 84 DESIGN.md files from real brand design systems

## Most Relevant Design Systems for OmniShizuku

### 1. Raycast (design-md/raycast/DESIGN.md)
**Relevance**: Utility app aesthetic — highest match for OmniShizuku
- Dark canvas (#07080a), hairline 1px borders, no drop shadows
- Inter typography with ss03 stylistic set
- Single white CTA pill, 4-step surface ladder
- **Adopt**: Hairline borders over shadows, dark-first, restrained accent

### 2. Warp (design-md/warp/DESIGN.md)
**Relevance**: Terminal/developer tool — matches OmniShizuku shell/terminal features
- Warm near-charcoal canvas (#2b2622), Inter, hairline borders
- Tight geometry (rounded.sm = 3-4px), monospace for code
- **Adopt**: Dark-first canvas, hairline borders, restrained accent, monospace terminal typography

### 3. NVIDIA (design-md/nvidia/DESIGN.md)
**Relevance**: Technical utility aesthetic
- Single accent green (#76b900), 2px radius, monochrome + technical CAD feel
- No decorative gradients, no atmospheric mesh, no soft drop shadow
- **Adopt**: Single accent color principle, technical restraint, tight radius vocabulary

### 4. Linear (design-md/linear.app/DESIGN.md)
**Relevance**: Known for excellent utility app design
- Near-black canvas (#010102), lavender-blue accent, dense technical layout
- Product UI screenshots framed in dark panels
- **Adopt**: Dense but readable information layout, dark panels for content

### 5. Ollama (design-md/ollama/DESIGN.md)
**Relevance**: Terminal-first, documentation-first system
- Paper-white canvas, 36px center-aligned heading, single black pill CTA
- Fully rounded pills (rounded.full), ui-monospace for code
- **Adopt**: Minimal decoration, pill-shaped primary actions, monospace for terminal content

## Design Principles Extracted

| Principle | Source | OmniShizuku Application |
|-----------|--------|------------------------|
| Dark-first canvas | Raycast, Warp, VoltAgent | OmniShizuku defaults to dark theme |
| Hairline borders, no shadows | Raycast, Warp, VoltAgent | Clean utility aesthetic |
| Inter/system typography | Raycast, Warp, NVIDIA, Zapier | System font, highly readable |
| Single accent color | NVIDIA | Brand indigo (#3F51B5) for CTAs and active states |
| Technical restraint | NVIDIA, Warp | No decorative gradients, glassmorphism, or excess |
| Color block sections | Figma | Feature grouping with subtle background variation |
| 4px base spacing | Wired, Clay | Consistent spacing system |
| Tight radius vocabulary | NVIDIA, Warp | 2-12px radius range, no excessive rounding |
| Surface ladder for depth | Raycast, Linear | Background color steps instead of shadows |

## Key Findings

1. **All top utility/developer tools use hairline borders instead of shadows** — this is the strongest pattern across the entire collection
2. **Single accent color is universal** — NVIDIA, Raycast, Linear, VoltAgent all use exactly one chromatic accent
3. **Dark-first canvas dominates developer tools** — 6 of 7 most relevant systems are dark-first
4. **Tight radius vocabulary (2-12px)** — none of the developer tools use the 16-24px rounding common in consumer apps
5. **Monospace for terminal/code content** — Warp, Ollama, Raycast all use dedicated monospace faces for code
6. **Surface ladder replaces shadows** — depth is created through background color steps, not drop shadows

## OmniShizuku Design Direction

Based on the research, OmniShizuku should:
- Keep the dark-first Material Design 3 foundation already in place
- Replace remaining box shadows with hairline borders
- Tighten corner radii from 14-16px to 8-12px range
- Use the brand indigo (#3F51B5) as the single accent for all CTAs and active states
- Add a monospace font family for terminal/shell screens
- Use the surface ladder (surfaceContainerLow → surfaceContainerHigh) for card elevation instead of shadows
- Apply 4px base spacing system consistently