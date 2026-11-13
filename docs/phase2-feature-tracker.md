# Phase 2 Labs implementation tracker

Status is based on source inspection, not catalogue presence alone.

| Feature | Status | Evidence / remaining work |
|---|---|---|
| Display Manager | PARTIAL | `DisplayScreen` reads and applies `wm` size/density and refresh settings with validation. Temporary confirmation countdown, profiles, and persisted rollback are pending. |
| Audio Stream Master | PARTIAL | `AudioMasterManager` controls real streams and output devices. Reversible built-in sound profiles and snapshots are now implemented; mute controls and scheduled profiles remain. |
| Sound Profile Manager | PARTIAL | Normal, silent, vibrate, night, meeting, and custom profile models apply real `AudioManager` state. Notification-policy access is detected; scheduling/history remain. |
| Battery Dashboard | PARTIAL | `DeviceInfo` and `BatteryInfoUtil` expose real battery telemetry and graceful unavailable values. A dedicated dashboard/provider status surface remains. |
| Thermal Monitor | PARTIAL | `ThermalUtil` parses `dumpsys thermalservice` and reports sensors/throttling. Public `PowerManager` thermal status integration and dedicated controls remain. |
| App Inspector | PARTIAL | `AppDetailScreen` and `PackageTools` inspect permissions, AppOps, standby, enabled state, and package metadata. Exportable certificate/size reporting remains. |
| Activity Launcher | PARTIAL | Activity discovery, exported status, search, intent builder, and failure outcomes exist. Favorites and richer required-permission display remain. |
| AppOps Manager | PARTIAL | Existing app detail operations support real modes and restoration paths. A dedicated operation catalogue/history UI remains. |
| Private DNS | PARTIAL | Existing controller validates hostnames, verifies writes, and restores both keys on failure. Profiles and connectivity checks remain. |
| Caffeinate | PARTIAL | Wake-lock controller, timed sessions, cleanup, and a tile exist. Charging-only mode and stronger battery warnings remain. |
| Quick Settings Tiles | PARTIAL | Caffeinate and DNS-related tile services exist. A complete synchronized profile tile suite remains. |
| Privacy Dashboard | MISSING | Individual permission/settings screens exist, but no unified inspection surface is present. |
| Advanced File Explorer | PARTIAL | Privileged file browsing and operations exist. Operation history and broader provider fallback remain. |
| Automation Engine | PARTIAL | Auto-DND and reapply mechanisms exist. A reusable trigger/action/condition editor is missing. |
| Doze Controller | PARTIAL | Existing Doze and EnforceDoze implementations inspect and apply supported commands. A unified temporary override/recovery surface remains. |
| Advanced Haptics | MISSING | No unified vibrator capability/profile manager is present. |
| Network Firewall | PARTIAL | Existing Shizuku/Root and companion firewall paths exist. Provider-specific persistent rule verification remains. |

## Current milestone

The current implementation milestone adds reversible Audio Stream Master profiles
without introducing a new provider or database. It uses `SharedPreferences` for
small local snapshots and `AudioManager` for actual state. This is intentionally
separate from the larger automation and history work.
