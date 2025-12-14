#!/usr/bin/env bash

set -euo pipefail

if [ $# -lt 1 ] || [ $# -gt 2 ]; then
  echo "Usage: $0 <model.lm> [imports comma-separated]" >&2
  exit 1
fi

ROOT_DIR="$(CDPATH= cd -- "$(dirname "$0")" && pwd)"
MODEL="$1"
IMPORTS="${2:-}"

# normalize paths
if [[ "$MODEL" != /* ]]; then
  MODEL="$ROOT_DIR/$MODEL"
fi

if [[ -n "$IMPORTS" ]]; then
  IFS=',' read -ra PARTS <<< "$IMPORTS"
  for i in "${!PARTS[@]}"; do
    if [[ "${PARTS[$i]}" != /* ]]; then
      PARTS[$i]="$ROOT_DIR/${PARTS[$i]}"
    fi
  done
  IMPORTS="$(printf "%s," "${PARTS[@]}")"
  IMPORTS="${IMPORTS%,}"
fi

GRADLE_USER_HOME="${GRADLE_USER_HOME:-$ROOT_DIR/.gradle}" \
"$ROOT_DIR/gradlew" :logoce.lmf.core:run --args="$MODEL $IMPORTS"
