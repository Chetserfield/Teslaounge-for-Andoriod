#!/usr/bin/env bash
# Source this file to use this workspace's isolated Android toolchain.
TESLOUNGE_PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TESLOUNGE_TOOLS_DIR="$(cd "$TESLOUNGE_PROJECT_DIR/.." && pwd)/.android-tools"
export JAVA_HOME="$TESLOUNGE_TOOLS_DIR/jdk/Contents/Home"
export ANDROID_HOME="$TESLOUNGE_TOOLS_DIR/sdk"
export ANDROID_USER_HOME="$TESLOUNGE_TOOLS_DIR/user-home"
export ANDROID_AVD_HOME="$ANDROID_USER_HOME/avd"
export GRADLE_USER_HOME="$TESLOUNGE_TOOLS_DIR/gradle-home"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$ANDROID_HOME/cmdline-tools/latest/bin:$TESLOUNGE_TOOLS_DIR/gradle-8.9/bin:$PATH"
