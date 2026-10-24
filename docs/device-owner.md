# Device Owner & Profile Owner Guide

Shevery includes a full-featured **Device Owner** and **Profile Owner** management system (`DeviceOwnerManager`, `DeviceOwnerScreen`), alongside native **Dhizuku API** compatibility (`DhizukuProvider`).

Wiki mirror: https://github.com/HmnDev-Tech/shevery/wiki/Device-Owner

---

## Overview

In Android's enterprise architecture (`DevicePolicyManager`), a **Device Owner** or **Profile Owner** app holds elevated privileges that persist across reboots without root:

- **DPM-assisted ADB**: Enable USB debugging (`adb_enabled`) and Wireless debugging (`adb_wifi_enabled` on Android 11+) programmatically via `DevicePolicyManager.setGlobalSetting`, even without `WRITE_SECURE_SETTINGS`.
- **Silent Package Operations**: Install and uninstall APKs (including compatibility stubs and self-updates) via `PackageInstaller` sessions with `--bypass-low-target-sdk-block` on Android 14+.
- **App Delegation Scopes**: Delegate granular `DevicePolicyManager` capabilities to other apps without handing over full ownership.
- **Native Dhizuku Server**: Act as a drop-in provider (`com.rosan.dhizuku.aidl.IDhizuku` v1 & v2) for Dhizuku-compatible apps.
- **Ownership Transfer**: Transfer Device/Profile Owner status to another `DeviceAdminReceiver` app on Android 9+ (API 28+).

---

## Activating Device Owner or Profile Owner

Navigate to **Settings → Device Owner**. When Shevery is not yet an owner, the screen shows a tab selector for **Device Owner** and **Profile Owner**.

### Method 1: 1-Tap Activation via Shizuku (Recommended)

If the Shevery/Shizuku server is already running (via Wireless Debugging, PC ADB, or Root):

1. Open **Settings → Device Owner**.
2. Select either the **Device Owner** or **Profile Owner** tab.
3. Tap **Activate via Shizuku**.
4. Shevery executes the `dpm` command through the active Shizuku binder and verifies ownership immediately.

### Method 2: ADB Command from PC

Run one of the following commands via `adb shell`:

**Device Owner** (requires no accounts or secondary users on the device during activation):
```bash
adb shell dpm set-device-owner com.hamondev.shevery/moe.shizuku.manager.receiver.SheveryDeviceAdminReceiver
```

**Profile Owner** (for the primary user profile):
```bash
adb shell dpm set-profile-owner com.hamondev.shevery/moe.shizuku.manager.receiver.SheveryDeviceAdminReceiver
```

---

## App Delegation & Native Dhizuku Support

Once Device Owner or Profile Owner is active, open **Settings → Device Owner → App Delegation** (`DeviceOwnerDelegationScreen`).

### Delegated Scopes

You can grant or revoke any of the five standard Android `DevicePolicyManager` delegation scopes per application:

| Scope Constant | Label | Description |
|---|---|---|
| `delegation-app-restrictions` | **App Restrictions** | Configure managed configurations and app restrictions |
| `delegation-block-uninstall` | **Block Uninstall** | Prevent specified applications from being uninstalled |
| `delegation-permission-grant` | **Permission Granting** | Grant or revoke runtime permissions without user prompts |
| `delegation-package-access` | **Package Access (Freeze)** | Hide, unhide, and suspend applications |
| `delegation-enable-system-app` | **Enable System Apps** | Enable pre-installed disabled system applications |

### Dhizuku API Authorization & Strict Whitelist Mode

Shevery exposes `DhizukuProvider` to serve apps that request `com.rosan.dhizuku.permission.API` or `com.rosan.dhizuku.permission.API_V2`:

- Filter the app list between **Dhizuku apps only** and **All installed apps**.
- Grant or revoke Dhizuku binder access (including one-time session grants).
- **Strict Whitelist Mode**: Toggle at the top of the App Delegation screen to restrict Device Owner operations exclusively to explicitly authorized packages.
- If a third-party app checks that `com.rosan.dhizuku` is installed by package name, install the **Dhizuku Compatibility Stub** under **Settings → Application → Compatibility Stubs** (see [Stealth Edition & Compatibility Stubs](stealth-and-stubs.md)).

---

## Transferring Ownership

To transfer Device Owner or Profile Owner status to another app (e.g., Dhizuku, Test DPC, Amarok, Hail):

1. Open **Settings → Device Owner → Transfer Ownership** (`DeviceOwnerTransferScreen`).
2. Select the target app from the list of installed apps that declare a `DeviceAdminReceiver`.
3. Tap the checkmark icon in the top bar and confirm the warning dialog.
4. Shevery automatically activates the target receiver if needed (`ensureAdminActive`) and calls `DevicePolicyManager.transferOwnership()`.

---

## Deactivating Owner Status

Under **Settings → Device Owner → Danger Zone**:

1. Tap **Deactivate Device Owner** (or **Deactivate Profile Owner**).
2. Wait for the **5-second safety countdown** in the confirmation dialog.
3. Confirm (biometric/device credential authentication is requested if **Device Owner settings & transfer** protection is enabled in **Settings → Security**).
4. Shevery calls `clearDeviceOwnerApp` / `clearProfileOwner` and removes the active device admin.

---

## Source Files

- `manager/src/main/java/moe/shizuku/manager/deviceowner/DeviceOwnerManager.kt`
- `manager/src/main/java/moe/shizuku/manager/settings/DeviceOwnerScreen.kt`
- `manager/src/main/java/moe/shizuku/manager/settings/DeviceOwnerDelegationScreen.kt`
- `manager/src/main/java/moe/shizuku/manager/settings/DeviceOwnerTransferScreen.kt`
- `manager/src/main/java/moe/shizuku/manager/dhizuku/DhizukuProvider.kt`
