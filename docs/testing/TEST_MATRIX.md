# Test matrix

| Area | JVM unit | Instrumentation | Emulator | Physical device |
|---|---:|---:|---:|---:|
| Feature registry and search | Required | — | — | — |
| Shell argument/path validation | Required | — | — | — |
| Audio and display models | Required | Required where framework APIs are used | Required | Required for hardware claims |
| Main activity and Compose root | — | Required | Required | Recommended |
| Shizuku/root/ADB/Dhizuku authorization | Logic only | Lifecycle only | Not proof of provider access | Required |
| OEM-specific behavior | — | — | Not proof | Required |

`NOT RUN` is distinct from `PASS`. The repository does not claim physical-device or OEM verification unless a trusted runner produces evidence.

