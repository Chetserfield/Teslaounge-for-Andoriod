#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/local-env.sh"
if [[ ! -x "$JAVA_HOME/bin/java" || ! -d "$ANDROID_HOME/platforms/android-35" ]]; then
    echo "로컬 Android 도구가 없습니다. README.md의 개발 환경 안내를 확인하세요." >&2
    exit 1
fi
cd "$TESLOUNGE_PROJECT_DIR"
if [[ $# -eq 0 ]]; then
    set -- assembleDebug lintDebug
fi
exec ./gradlew "$@" --no-daemon
