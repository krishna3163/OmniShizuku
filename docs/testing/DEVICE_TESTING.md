# Trusted device testing

Physical-device tests are optional and must run only on repository-controlled self-hosted runners. Fork pull requests must never receive secrets or access to privileged devices.

Recommended runner labels:

```text
android-device
android-shizuku
android-root
android-oem-samsung
```

The repository currently has no verified physical-device runner configured. Therefore physical-device status is `NOT RUN`, not `PASS`.

