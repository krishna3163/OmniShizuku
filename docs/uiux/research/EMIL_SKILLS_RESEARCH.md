# Emil Kowalski Skills Research - OmniShizuku

## Repository
- **URL**: https://github.com/emilkowalski/skills
- **License**: MIT
- **Commits**: 59
- **Author**: Emil Kowalski (ex-Vercel, ex-Linear)
- **Description**: Skills for Designers and Engineers — animation and interaction quality

## Skills Relevant to OmniShizuku (Native Android)

### emil-design-eng
Main skill covering animation and design philosophy:
- **Unseen details compound**: Small details users never notice make the difference
- **Ease curves**:
  - Strong ease-out for UI interactions: `cubic-bezier(0.23, 1, 0.32, 1)`
  - Strong ease-in-out for on-screen movement: `cubic-bezier(0.77, 0, 0.175, 1)`
  - iOS-like drawer curve: `cubic-bezier(0.32, 0.72, 0, 1)`
  - **Never use ease-in for UI animations**
- **Spring animations**: For drag with momentum, alive elements, interruptible gestures
- **Stagger delays**: 30-80ms between items (short, decorative, never block interaction)
- **Continuous feedback**: During interaction, not just at end
- **Design interaction and visuals together**

### animate
Builds animations from scratch with correct decisions:
- **UI animations stay under 300ms**
- Dropdowns/selects: 150-250ms
- Modals/drawers: 200-500ms
- Springs for draggable/interruptible motion
- Transitions (not keyframes) for rapid triggers (toasts, toggles)
- Exit the way it entered (symmetric paths)
- Transitions retarget from current value; keyframes restart from zero

### animation-performance
Fixes frame drops:
- 60fps = 16.7ms per frame; 120Hz = 8ms
- Animate only transform/opacity (composite-only), avoid layout/paint
- `will-change: transform` only when needed
- Layout recalculation can take 100ms+ (dozens of missed frames)

### review-animations
Strict animation review based on Emil's rules.

### improve-animations
Audit all animations, get prioritized execution plans.

### find-animation-opportunities
Find places that genuinely benefit from motion; also identifies what NOT to animate.

### animation-vocabulary
Motion terminology for precise communication with AI.

### apple-design
Apple's interface design principles:
- Feedback must be continuous during interaction
- Spring defaults: damping 1.0 (no overshoot) for most UI
- Reserve bounce (0.1-0.3) for momentum-driven physical interactions
- Velocity handoff between gesture and animation
- Anchor interactions to their source element

## Android Adaptation (Not React Native/Web)

| Web/React Concept | Android Compose Equivalent |
|-------------------|---------------------------|
| `cubic-bezier()` | `FastOutSlowInInterpolator`, `LinearOutSlowInInterpolator`, custom `PathInterpolator` |
| Springs (friction/tension) | `spring()` with `SpringSpec(dampingRatio, stiffness)` |
| `will-change: transform` | Compose `animate*` with `animationSpec` targeting specific properties |
| Stagger 30-80ms | `animateItemPlacement` with staggered start delays |
| Continuous feedback | `InteractiveVector`, `Transition` with state-based animation |

## Recommendations Adopted for OmniShizuku

| Recommendation | Adoption | Reason |
|----------------|----------|--------|
| UI animations under 300ms | Yes | Standard Compose motion spec durations |
| Ease-out curves for UI | Yes | `FastOutLinearInInterpolator`, `FastOutSlowInInterpolator` |
| No ease-in for UI | Yes | Avoid in all Compose animations |
| Springs for gestures/interruptible | Yes | `spring { dampingRatio = ... stiffness = ... }` |
| Stagger 30-80ms | Yes | For list item appearances |
| Exit symmetric with entry | Yes | For dialogs, bottom sheets, toasts |
| Continuous feedback | Yes | For toggle switches, sliders, progress |
| Never animate decorative only | Yes | Motion must communicate state |
| Respect reduced-motion | Yes | Check `isSystemInReducedMotionMode()` |

## Files Created
- `docs/uiux/research/EMIL_SKILLS_RESEARCH.md` (this file)

## Integration Plan
Use Emil's principles to:
1. Define animation specs for screen transitions, dialogs, bottom sheets
2. Review existing animations in OmniShizuku against his rules
3. Remove unnecessary animations, add high-value ones
4. Ensure all motion respects accessibility (reduced motion)
