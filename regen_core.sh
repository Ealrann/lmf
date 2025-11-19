#!/bin/sh

set -e

ROOT_DIR="$(CDPATH= cd -- "$(dirname "$0")" && pwd)"

MODEL_FILE="$ROOT_DIR/logoce.lmf.model/src/main/model/asset/model.lm"
OUTPUT_DIR="$ROOT_DIR/logoce.lmf.model/src/main/generated"

"$ROOT_DIR/gradlew" :logoce.lmf.generator:run --args="$MODEL_FILE $OUTPUT_DIR"

