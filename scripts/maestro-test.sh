#!/usr/bin/env bash
# Builds Tiny Launcher, makes it the home app on the connected device and runs the Maestro suite.
# Usage: ./scripts/maestro-test.sh [extra maestro args], e.g. --exclude-tags slow
set -euo pipefail
cd "$(dirname "$0")/.."

./gradlew installDebug -q
adb shell cmd package set-home-activity dev.tiny.launcher/.MainActivity >/dev/null
adb shell input keyevent KEYCODE_HOME

maestro test .maestro "$@"
