# Security review status

This is a preliminary status note, not a completed security assessment.

## Scope and constraints

OmniShizuku has privileged shell and Binder-facing functionality. Any integration that can run shell commands, alter package state, expose files, change global settings, install APKs, or affect networking requires review of authorization, input validation, least privilege, timeout/cancellation, logging, and recovery behavior.

## Findings from this audit

- A narrowly scoped input-validation change was made after comparing the relevant source utilities; no complete feature subsystem was copied.
- The target worktree includes uncommitted user changes; these must be preserved during later work.
- The workspace feature encyclopedia contains claims involving high-impact operations. Treat them as unverified until the corresponding current implementation and safeguards are inspected.
- Static review of the target ADB module installer found an entry cap, unpacked-byte cap, script-size cap, normalized ZIP paths and canonical containment checks. Static review of the local WebUI found a file-origin/root check, opt-in shell bridge, per-command approval UI, and network access controlled by module settings. This is limited to inspected paths and does not establish the absence of other vulnerabilities.
- The module WebView enables JavaScript and local file access by design. Bridge use must stay restricted to module-local file URLs and explicit module permissions; navigation/redirect behavior and crafted archives need runtime tests before expansion.
- The inspected Shizuku and Shizuku-Next checkouts declare Apache-2.0. The package-name helper is adapted from ShizukuPlus (Apache-2.0) and the DNS parser from ADNS (MIT); the target `NOTICE` and `licenses/ADNS-MIT.txt` retain their attributions and license terms.
- A repository-wide search found ShizukuPlus `InputValidationUtils.isValidPackageName` (Apache-2.0), Canta's package validator (LGPL-3.0), and an AxManager package guard. The target now uses a small attributed adaptation of the ShizukuPlus package-name rule and tests reject shell metacharacters, malformed names and oversized values.
- The target's package strings are now validated at the reviewed privileged operation boundaries: debloating, app freeze/unfreeze, call-recording app-ops, package detail and app-op operations, app disable/suspend/uninstall/restore, force-stop, battery/network restrictions, and firewall changes.
- The target APK installer already quoted paths but had user-controlled install options joined directly into shell commands. It now uses the existing POSIX quote logic for every option token in single and split installs. `ShellArgumentsTest` checks metacharacters, spaces and embedded single quotes.
- Private DNS hostnames come from editable UI input. The target now adapts ADNS's MIT-licensed `PrivateDnsHostname` checks: IDN normalization, FQDN label/length checks, and rejection of IP-like input before writing through shell. `NOTICE` and `licenses/ADNS-MIT.txt` retain attribution and license terms. Invalid custom input returns before changing the selected DNS mode.
- The Essentials battery/thermal source is now integrated without its separate app shell engine: a small `ShellUtils` adapter checks the Shizuku binder and manager permission, then delegates fixed utility commands through the existing `PrivilegedFiles.exec` service. The upstream utility uses fixed sysfs paths and fixed `dumpsys` commands; this boundary has not been device-tested, and binder service timeout behavior needs review.
- The Essentials Watermark engine preserves original EXIF fields while creating a processed image, including GPS location tags when present. The Watermark screen now tells users this before export. Image processing/output behavior still needs Android build and device validation.
- JUnit tests cover package names, DNS hostname normalization and preset-host validation, and POSIX shell quoting. Six focused tests pass when compiled and run directly with the cached Kotlin compiler/JUnit. The Gradle test task and Android build still fail before project configuration because the sandbox denies Java network-interface enumeration.
- No device-level security testing was performed.

## Required before integration

For each feature, trace caller-to-privileged-operation paths, validate external inputs and file paths, confirm explicit user authorization for destructive actions, inspect exported Android components and Binder permissions, review sensitive-data logging, and check upstream security advisories. Record specific findings and mitigations here after evidence is collected.
