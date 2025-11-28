#!/usr/bin/env bash
set -euo pipefail

PROJECT=":logoce.lmf.intellij"

echo "[LMF IntelliJ] Building plugin for $PROJECT..."
./gradlew "$PROJECT:buildPlugin"

# Ask Gradle for the effective buildDir of this project (respects ~/.gradle/init.gradle overrides).
build_dir=$(./gradlew -q "$PROJECT:properties" | awk -F': ' '/^buildDir: / {print $2; exit}')
if [[ -z "${build_dir:-}" ]]; then
	echo "Error: could not determine buildDir for $PROJECT" >&2
	exit 1
fi

dist_dir="$build_dir/distributions"
if [[ ! -d "$dist_dir" ]]; then
	echo "Error: distribution directory not found: $dist_dir" >&2
	exit 1
fi

plugin_zip=$(ls -t "$dist_dir"/*.zip 2>/dev/null | head -n 1 || true)
if [[ -z "$plugin_zip" ]]; then
	echo "Error: no plugin ZIP found in $dist_dir" >&2
	exit 1
fi

echo "[LMF IntelliJ] Plugin built:"
echo "  $plugin_zip"
