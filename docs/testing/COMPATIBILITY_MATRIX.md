# Android compatibility matrix

The checked-in configuration is authoritative: `minSdk 26`, `targetSdk 37`, and `compileSdk 37.1`. The pull-request emulator workflow currently exercises API 35 instrumentation. API 26 and API 37 coverage remain separate follow-up matrix jobs because they require compatible emulator images and the repository's preview SDK toolchain.

| API | Instrumentation evidence | Privileged-provider evidence |
|---:|---|---|
| 26 | Planned | Not run |
| 35 | GitHub emulator workflow | Not run |
| 37/37.1 | Planned | Not run |

An emulator result never substitutes for Shizuku, root, ADB, Dhizuku, or OEM evidence.

