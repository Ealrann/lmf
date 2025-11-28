#!/usr/bin/env bash
set -euo pipefail

SESSION_NAME="${SESSION_NAME:-lmf-lsp}"
PROJECT=":logoce.lmf.lsp"

echo "[LMF LSP] Building installable distribution for $PROJECT..."
./gradlew "$PROJECT:installDist"

# Ask Gradle for the effective buildDir (respects ~/.gradle/init.gradle overrides).
build_dir=$(./gradlew -q "$PROJECT:properties" | awk -F': ' '/^buildDir: / {print $2; exit}')
if [[ -z "${build_dir:-}" ]]; then
	echo "Error: could not determine buildDir for $PROJECT" >&2
	exit 1
fi

INSTALL_DIR="$build_dir/install/logoce.lmf.lsp"
SERVER_BIN="$INSTALL_DIR/bin/logoce.lmf.lsp"

if ! command -v tmux >/dev/null 2>&1; then
	echo "Error: tmux is not installed or not on PATH." >&2
	exit 1
fi

if [[ ! -x "$SERVER_BIN" ]]; then
	echo "Error: LSP server binary not found at $SERVER_BIN" >&2
	exit 1
fi

if tmux has-session -t "$SESSION_NAME" 2>/dev/null; then
	echo "[LMF LSP] tmux session '$SESSION_NAME' already exists; reusing it."
else
	echo "[LMF LSP] Starting server in detached tmux session '$SESSION_NAME'..."
	tmux new-session -d -s "$SESSION_NAME" "$SERVER_BIN"
fi

if [[ "${1-}" == "--attach" ]]; then
	echo "[LMF LSP] Attaching to tmux session '$SESSION_NAME'..."
	tmux attach -t "$SESSION_NAME"
else
	echo "[LMF LSP] Server running in detached tmux session '$SESSION_NAME'."
	echo "          Run 'tmux attach -t $SESSION_NAME' to attach."
fi
