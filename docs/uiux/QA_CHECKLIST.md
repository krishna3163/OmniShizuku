# QA Checklist - OmniShizuku

## Design Quality Gates

A screen cannot be marked complete until these checks are satisfied:

### Design
- [ ] Follows DESIGN.md
- [ ] Uses approved design tokens
- [ ] Consistent typography (from defined scale)
- [ ] Consistent spacing (from 4px base scale)
- [ ] Clear visual hierarchy
- [ ] No obvious AI-slop patterns (gradients, glassmorphism, emoji icons)

### Functionality
- [ ] Existing actions still work
- [ ] No fake system state
- [ ] Correct permissions requested
- [ ] Correct loading state shown
- [ ] Correct error handling with user-facing messages
- [ ] Correct empty state shown when no data
- [ ] Destructive operations require confirmation

### Accessibility
- [ ] Proper content descriptions on all icons
- [ ] Touch targets minimum 48x48dp
- [ ] Adequate contrast (4.5:1 normal, 3:1 large)
- [ ] Font scaling respected (sp units)
- [ ] Reduced-motion support (isSystemInReducedMotionMode())
- [ ] Logical focus order for D-pad and TalkBack

### Technical
- [ ] Compiles successfully (`:manager:compileDebugKotlin`)
- [ ] Relevant tests pass (`:manager:testDebugUnitTest`)
- [ ] No new critical crashes
- [ ] No avoidable main-thread work
- [ ] No broken navigation
- [ ] No new security regressions

## Screen QA Status

| Screen | Design | Functionality | Accessibility | Technical | Overall |
|--------|--------|---------------|---------------|-----------|---------|
| Home | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| Apps | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| Settings | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| Modules | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| Labs | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| Shell | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| Terminal | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| Device Info | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| All others | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |

⬜ = Not yet assessed

---

## Files Created
- `docs/uiux/QA_CHECKLIST.md` (this file)
