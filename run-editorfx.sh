#!/usr/bin/env bash
set -euo pipefail

# Launch the JavaFX-based LMF editor against the current working directory as workspace.
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
WORKSPACE="$(pwd)"

cd "$SCRIPT_DIR"
exec ./gradlew :logoce.lmf.editorfx:run --args="$WORKSPACE"
