# Integration architecture audit

## Current target structure

The repository is a Gradle multi-project Android application with modules including `manager`, `server`, `starter`, `shell`, `stub`, `common`, `nightdog`, `tasker`, `database`, `app-process`, plus API modules (`aidl`, `rish`, `shared`, `api`, `provider`, and `server-shared`). `settings.gradle` is the source of truth for included modules.

The project also contains a Next.js site (`app/`, `components/`, `lib/`) and npm lockfiles. Android and website concerns share the repository but have separate build systems.

## Integration constraints observed

- The target worktree contains uncommitted user changes, including Essentials UI movement and Gradle changes. Keep these intact during integration planning.
- `manager` consumes `manager/libs/island-sdk.aar`; the root Android configuration now sets compile SDK 37.1 to match its metadata. A post-change Gradle run has not reached project configuration because Java network-interface enumeration is blocked in this sandbox.
- Multiple subprojects load Kotlin Gradle plugins with explicit versions; Gradle warns that this is unsupported and may break builds.
- The APK workflow lacked an explicit Android SDK 37.1 install despite the bundled AAR requiring minor API 1. It now requests that platform and runs `:manager:testDebugUnitTest` before APK builds. The remote workflow has not run yet.
- Private DNS uses the target screen and shell controller with an ADNS-derived preset data catalog; the upstream UI, Android service, and NextDNS account features are not part of this integration.
- Essentials Battery Center now uses upstream `BatteryInfoUtil`, `ThermalUtil`, `DeviceUtils`, `ChargingModeUtil`, `ChargeLimitUtil`, and vector assets. The Pixel charging-mode switch uses the original Secure Settings backend and is shown only on Google devices. A narrow `com.sameerasw.essentials.utils.ShellUtils` adapter checks Shizuku permission and calls the target's existing `PrivilegedFiles.exec`; the source keeps its original package and MIT attribution.
- Watermark now uses the original Essentials `WatermarkEngine`, `MetadataProvider`, and five EXIF vector assets. A narrow target adapter saves the engine's cache output into MediaStore, and the screen exposes the upstream frame/overlay styles, color modes, overlay alignment, padding, border, and rotation options. Those controls feed the engine's existing `WatermarkOptions` fields; the target did not reimplement its image renderer. The original MIT source remains in its package with the project license retained. This integration has not yet been compiled or device-tested.
- Other Essentials feature modules remain pending because they depend on the upstream app's registry, ViewModel, services, manifest, and resource graph.
- Caffeinate now reuses the upstream `CaffeinateController` and `CaffeinateWakeLockService`, with target-specific foreground service registration, a settings surface, and a Quick Settings adapter. It supports duration presets, a start countdown, optional screen-off cancellation, and a stop action in the ongoing notification. This source integration has not yet been compiled or device-tested.
- Confirmed decisions so far: keep the existing Shizuku engine/module manager/debloater rather than duplicating them; use narrowly scoped input-validation adapters with preserved upstream notices. Other integration choices remain pending source and compatibility review.

## Proposed integration boundary

For each candidate, record whether it is already present, a versioned dependency, a submodule, a minimal adapter, or a separately isolated module. Do not copy code until its license, full dependency chain, Android compatibility, and security behavior are checked. Avoid parallel implementations of the existing Shizuku engine.
