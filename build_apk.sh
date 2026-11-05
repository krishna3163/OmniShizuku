#!/usr/bin/env bash
set -e

echo "=== Building OmniShizuku (Debug APK only) ==="
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

chmod +x ./gradlew 2>/dev/null || true
./gradlew :manager:assembleDebug "$@"

echo ""
echo "=== Debug APK Build Completed Successfully ==="
find "$SCRIPT_DIR/manager/build/outputs/apk/debug" -name "*.apk" 2>/dev/null || true
