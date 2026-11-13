# Release verification

Before publishing an APK:

1. Run manager unit tests.
2. Assemble the requested variant.
3. Confirm an APK exists at the expected output path.
4. Verify its signature with the Android SDK `apksigner`.
5. Upload the APK and build log as immutable workflow artifacts.
6. Publish only after all required build steps pass.

Release signing secrets are never used by pull-request builds from untrusted forks. A fallback debug-signed APK is clearly named and must not be treated as a production release.
