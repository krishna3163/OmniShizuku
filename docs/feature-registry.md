# OmniShizuku Labs Feature Registry

`manager/src/main/java/moe/shizuku/manager/feature/registry/FeatureRegistry.kt`
is the machine-readable catalogue for Labs features. It complements localized UI
strings and existing `Detail` navigation without moving privileged execution into
Compose screens.

Each definition provides a stable ID, category, searchable keywords, minimum API,
provider alternatives, reversibility, and experimental classification. Provider
requirements are alternatives: a shell-backed feature is available when either
Shizuku or root is currently usable. The registry never infers availability from a
previous success; `FeatureRegistry.capability()` checks the current process state,
Android version, ADB setting, and root shell.

## Provider policy

| Provider | Use |
|---|---|
| Android API | Public platform APIs and device information |
| Shizuku | Privileged shell and system-service operations |
| ADB | ADB-backed settings and package operations |
| Root | Root-only shell and filesystem operations |
| Dhizuku | Reserved for device-owner integrations when a feature explicitly supports it |

Features that require privileged execution are reported as unavailable until a
supported provider is detected. Experimental entries remain visible in the
catalogue but are marked as experimental rather than being presented as universal
hardware support.

## Essentials mapping

The Essentials wiki describes the same broad areas—display, sound, power, privacy,
network, package management, developer tools, automation, and quick settings.
OmniShizuku already contains native implementations for the compatible subset:
display controls, audio streams, battery/device diagnostics, private DNS, doze,
package inspection/freezing/debloating, activity launching, shell/logging,
caffeinate, and quick-tile integrations. Smart pixels, OEM-only SystemUI changes,
per-app audio, unrestricted firewall behavior, and exact background automation remain
experimental or device/provider dependent because Android does not expose a
universal API for them.

| Essentials reference area | OmniShizuku mapping | Status |
|---|---|---|
| Sound & Haptics | Audio Master, SoundMaster, MixedAudio, Quick Tiles | Native audio controls; device limits are surfaced |
| Display & Visual | Display, SystemUI Tuner, Caffeinate, Pocket Mode | Existing controls; OEM-only options remain gated |
| Notifications & Alerts | Auto DND, notification/security tools | Supported where Android exposes policy access |
| Security & Privacy | App Ops, Permissions, Screen Security, Stealth | Provider and Android security boundaries are preserved |
| Input & Actions | Pocket Mode and shell-backed action tools | Pocket mode is experimental/device dependent |
| Power & Battery | Device Info, Doze, Caffeinate, Freezer | Real platform data and reversible controls |
| Network & Connectivity | Private DNS, Tethering, Firewall, ShizuWall | DNS is verified and rollback-safe; firewall paths are provider dependent |
| Widgets / Watch | Quick tiles and companion integrations | No fake Wear OS or widget support is advertised |
| DIY Automation | Auto DND and existing reapply infrastructure | Full trigger/action editor remains future work |
| App Freezing | Freezer | Existing provider-backed implementation |
| Tools | Files, Logs, Activities, Installer, WallYou | Existing native screens and capability limits |

Intentionally not presented as complete: universal smart-pixel hardware control,
per-application audio on stock Android, carrier bypass, unrestricted SystemUI
customization, and exact background automation timing. These require OEM APIs,
root/VPN/device-owner privileges, or behavior Android does not guarantee.
