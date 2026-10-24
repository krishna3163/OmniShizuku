# Stealth Edition & Compatibility Stubs Guide

Some apps and games scan installed package names for `com.hamondev.shevery`, `moe.shizuku.privileged.api`, or `com.rosan.dhizuku` and refuse to run. Conversely, some legacy Shizuku/Dhizuku client apps hardcode package-existence checks for `moe.shizuku.privileged.api` or `com.rosan.dhizuku`.

Shevery solves both problems from **Settings → Application** through the **On-Device Stealth APK Generator** and **Compatibility Stubs**.

Wiki mirror: https://github.com/HmnDev-Tech/shevery/wiki/Stealth-and-Stubs

---

## Part 1: Stealth Edition (On-Device Random Package Generator)

Available on Android 10+ (API 29+) in standard builds under **Settings → Application → Stealth version**.

### How It Works

1. The standard Shevery APK embeds a prebuilt stealth template (`assets/shevery-stealth.apk`) compiled with the placeholder package ID `com.stealthp.lacehold` and launcher label `System Toolkit`.
2. When you open **Stealth version**, Shevery generates a fresh random package name of the exact same length (`com.<8chars>.<8chars>`, e.g., `com.k8m2x9qp.r4v7n1tw`) using `SecureRandom`.
3. You can customize:
   - **App name** (launcher label, default `System Toolkit`)
   - **File name** (default `SystemToolkit-v<version>.apk`)
4. When you tap **Save**, `StealthApkGenerator`:
   - Rewrites all occurrences of the template package ID and launcher label across `AndroidManifest.xml`, `resources.arsc`, and `classes*.dex` in both **ASCII/UTF-8** and **UTF-16LE** encodings while keeping binary string-pool offsets intact.
   - Recalculates the **SHA-1 signature** and **Adler32 checksum** in every modified DEX header so Android's package verifier accepts the rewritten bytecode.
   - Preserves nested asset APKs (`shevery-stub.apk`, `shevery-dhizuku-stub.apk`) untouched so their signatures remain valid.
   - Signs the rebuilt APK on-device using a freshly generated 2048-bit RSA keypair and self-signed X.509 certificate (`SHA256withRSA`, v1 JAR signature).
   - Saves the finished APK directly to your device's **`Downloads/`** folder via `MediaStore.Downloads`.

### Installing the Stealth APK

> [!IMPORTANT]
> The stealth APK intentionally retains Shevery's Shizuku binder and provider authorities so that all Shizuku client apps continue to work with full privileges. Because Android does not allow two apps on the same device to declare the same `ContentProvider` authority, **you must uninstall the standard Shevery app before installing the generated Stealth APK** from `Downloads/`.

Tip: Use **Settings → Backups → Backup** before switching to the Stealth APK, then restore your settings, authorized apps, and modules in one tap after installing the stealth copy.

---

## Part 2: Compatibility Stubs

Available under **Settings → Application → Compatibility Stubs** (`CompatStubsScreen`).

### Available Stubs

| Stub | Package Name | Asset File | Purpose |
|---|---|---|---|
| **Shizuku Stub** | `moe.shizuku.privileged.api` | `shevery-stub.apk` | Satisfies apps that check if the original Shizuku Manager package is installed and forwards permission/binder intents to Shevery. |
| **Dhizuku Stub** | `com.rosan.dhizuku` | `shevery-dhizuku-stub.apk` | Satisfies apps that check if the official Dhizuku package is installed when Shevery acts as the Device Owner / Dhizuku provider. |

### Multi-Channel Installation Pipeline

When you tap **Install** or **Uninstall** on a stub card, `StubManager` automatically tries available privileged channels in order until one succeeds:

1. **Device Owner / Profile Owner**: Uses Android's `PackageInstaller` session API (`--bypass-low-target-sdk-block` enabled automatically on Android 14 / API 34+).
2. **Shevery Server**: Streams the APK to `/data/local/tmp/` and runs `pm install -r -d -t -g` through the active Shizuku binder.
3. **Root (`libsu`)**: Installs directly from private storage via a root shell.
4. **Local ADB**: Connects to the local ADB daemon (`127.0.0.1` on the live ADB port, configured TCP port, or `5555`) and installs from external app storage.

> **Biometric Protection**: Installing or removing stubs can be guarded with biometric or device credential authentication via **Settings → Security → Installing or removing stubs** (`ProtectedAction.STUB_MANAGEMENT`).

---

## Source Files

- `manager/src/main/java/moe/shizuku/manager/stealth/StealthApkGenerator.kt`
- `manager/src/main/java/moe/shizuku/manager/compat/StubManager.kt`
- `manager/src/main/java/moe/shizuku/manager/settings/CompatStubsScreen.kt`
