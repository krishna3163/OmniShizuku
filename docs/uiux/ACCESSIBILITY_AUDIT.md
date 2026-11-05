# Accessibility Audit - OmniShizuku

## Audit Date
2026-11-05

## Standards Applied
- WCAG 2.1 AA (contrast 4.5:1 normal, 3:1 large text)
- Android Accessibility Guidelines (touch targets 48x48dp, content descriptions)
- Material Design 3 accessibility specifications

## Results by Category

### TalkBack / Screen Reader
| Check | Status | Details |
|-------|--------|---------|
| Content descriptions on icons | Partial | Some icons lack contentDescription |
| Role announcements | Partial | Switches, buttons generally correct |
| State announcements | Partial | Enabled/disabled states announced |
| Focus order | Partial | Logical in most Compose screens |
| Dynamic text announcements | ❌ | Not implemented |

### Touch Targets
| Check | Status | Details |
|-------|--------|---------|
| Minimum 48x48dp | Partial | Most Material 3 components meet this |
| Icon buttons | Partial | Some may be smaller |
| Switch toggles | ✅ | Material 3 switches are accessible |
| List items | ✅ | Full-width touch targets |

### Text Contrast
| Check | Status | Details |
|-------|--------|---------|
| Normal text (4.5:1) | ✅ | Material 3 on-surface provides good contrast |
| Large text (3:1) | ✅ | Headings exceed contrast requirements |
| Disabled text | ⚠️ | May be below 3:1 in some themes |

### Font Scaling
| Check | Status | Details |
|-------|--------|---------|
| Uses sp units | Partial | Most text uses sp, some dp |
| Respects system scale | Partial | Compose respects system font scale |

### Reduced Motion
| Check | Status | Details |
|-------|--------|---------|
| Checks isSystemInReducedMotionMode() | ❌ | Not implemented |
| Non-essential animations disabled | ❌ | All animations run regardless |

### Error Handling
| Check | Status | Details |
|-------|--------|---------|
| Error messages announced | Partial | Some errors shown visually only |
| Error recovery guidance | Partial | Some screens provide guidance |
| Destructive action confirmation | ❌ | Not consistently implemented |

## Top Accessibility Issues

### P0
1. No reduced-motion support — violates accessibility guidelines for users with vestibular disorders
2. Some icons lack content descriptions — TalkBack users cannot understand icon-only buttons

### P1
3. Disabled state contrast may be below 3:1 — hard to read for low-vision users
4. Inconsistent error announcements — some errors not announced to screen readers

### P2
5. Font scaling not fully respected — some text uses dp instead of sp
6. Touch targets on some icon buttons may be below 48x48dp

---

## Files Created
- `docs/uiux/ACCESSIBILITY_AUDIT.md` (this file)
