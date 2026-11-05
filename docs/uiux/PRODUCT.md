# OmniShizuku PRODUCT.md

## Product Identity

**Name**: OmniShizuku  
**Tagline**: One coherent system utility platform — not a collection of unrelated tools  
**Package**: `com.krishna.omnishizuku`  
**Namespace**: `moe.shizuku.manager`  
**Min SDK**: 26 (Android 8.0)  
**Target SDK**: 37

## What OmniShizuku Is

OmniShizuku is an all-in-one Android system utility platform that brings Shizuku's system-level API access, ADB capabilities, root-based tools, application management, automation, and system diagnostics into one modular experience.

It is a **fork of Shizuku** that extends the original with enhanced modules, modern UI, and extended system control capabilities.

## Core Principles

| Principle | Meaning |
|-----------|---------|
| **Control** | Users have full control over their device; nothing is hidden or obfuscated |
| **Clarity** | Every screen, button, and setting communicates its purpose clearly |
| **Reliability** | Operations succeed or fail with clear error messages; no silent failures |
| **Transparency** | Real device data only; no fake connection status, no fabricated metrics |
| **Modularity** | Features are organized into logical modules; users use what they need |
| **Performance** | Smooth scrolling, fast launch, no main-thread blocking |
| **User Safety** | Destructive operations communicate impact and require confirmation |
| **Consistency** | One design language across all screens and form factors |

## User Personas

### 1. Android Power User
- Rooted or Shizuku-enabled device
- Wants to debloat, freeze apps, tweak system settings
- Comfortable with ADB and terminal
- Needs: Debloater, Freezer, Shell, Terminal, App Manager

### 2. Shizuku User
- Uses Shizuku for ADB/root bridging without full root
- Needs: Wireless ADB, Shizuku connection management, permission grants
- Needs: Connection Centre, ADB tools, permission screens

### 3. Developer
- Tests apps, inspects intents, views logs
- Needs: Terminal, Intents, Logcat, Device Info, WiFi Passwords
- Needs: Shell, ADB pairing, wireless debugging

### 4. Advanced Android Enthusiast
- Customizes themes, fonts, live wallpapers
- Needs: ThemePatcher, SoundMaster, Display tuning, Doze optimization
- Needs: ThemePatcher, Doze/EnforceDoze, SoundMaster

## Navigation Model

### Primary Navigation (Bottom Floating Tab Bar)
1. **Home** — Device status, quick actions, diagnostics
2. **Apps** — Installed app management, permissions, debloating
3. **Modules** — Shizuku module catalog and management
4. **Labs** — Advanced tweaks, app ops, shell, tools
5. **Settings** — Appearance, theme, behavior, permissions

### Detail Screens (Slide Over Tabs)
Each tab can open detail screens that slide over the tab content:
- Device Info, Display, Power, Shell, Terminal
- Doze/EnforceDoze, ThemePatcher, SoundMaster, MixedAudio
- Debloater, Freezer, Call Recording, DFRoot, WallYou
- And 30+ more detail screens

### TV Mode (Navigation Rail)
- Left-edge rail instead of bottom floating bar
- D-pad navigable, focus-visible states
- Same five tabs, single-page composition

## Form Factors
- **Phone**: Primary target, bottom tab bar, single column
- **Tablet**: Centered content (max 600dp width), same tab bar
- **TV/Foldable**: Navigation rail, D-pad focus, safe areas
- **Wear OS**: Limited subset, complication support

## Safety Commitments

- **No fake system state**: Connection status, battery, storage are real
- **No silent destruction**: Debloat, freeze, uninstall require confirmation
- **Honest unavailable states**: Features that need Shizuku/root show "unavailable" honestly
- **Reversible where possible**: Most operations can be undone; backups where applicable

## What OmniShizuku Is NOT

- Not a root replacement (works with or without root via Shizuku)
- Not a game booster or fake RAM cleaner
- Not a skin/theme engine that replaces the system UI
- Not a VPN or network proxy
