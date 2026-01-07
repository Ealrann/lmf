#!/usr/bin/env bash
set -euo pipefail

# Build and install the CLI distribution so it can be used as a plain `lm` command.

PROJECT=":logoce.lmf.cli"

echo "[LMF CLI] Building installable distribution for $PROJECT..."
./gradlew "$PROJECT:installDist"

# Discover the effective buildDir (respects any init.gradle overrides).
build_dir=$(./gradlew -q "$PROJECT:properties" | awk -F': ' '/^buildDir: / {print $2; exit}')
if [[ -z "${build_dir:-}" ]]; then
	echo "Error: could not determine buildDir for $PROJECT" >&2
	exit 1
fi

install_dir="$build_dir/install"
src_root=""

for candidate in "$install_dir/logoce.lmf.cli" "$install_dir/lm"; do
	if [[ -d "$candidate" ]]; then
		src_root="$candidate"
		break
	fi
done

if [[ -z "$src_root" ]]; then
	found=$(find "$install_dir" -maxdepth 2 -type f -name "lm" -print -quit 2>/dev/null || true)
	if [[ -n "$found" ]]; then
		src_root=$(dirname "$(dirname "$found")")
	fi
fi

if [[ -z "$src_root" ]]; then
	echo "Error: CLI distribution not found under $install_dir" >&2
	exit 1
fi

src_bin="$src_root/bin/lm"
if [[ ! -x "$src_bin" ]]; then
	echo "Error: CLI launcher not found at $src_bin" >&2
	exit 1
fi

# Where to install the CLI distribution.
INSTALL_ROOT="${LMF_CLI_INSTALL_ROOT:-$HOME/.local/share/logoce-lmf-cli}"
BIN_DIR="${LMF_CLI_BIN_DIR:-$HOME/.local/bin}"

echo "[LMF CLI] Installing CLI to: $INSTALL_ROOT"
rm -rf "$INSTALL_ROOT"
mkdir -p "$(dirname "$INSTALL_ROOT")"
cp -R "$src_root" "$INSTALL_ROOT"

echo "[LMF CLI] Creating launcher in: $BIN_DIR"
mkdir -p "$BIN_DIR"
launcher="$BIN_DIR/lm"

cat > "$launcher" <<EOF
#!/usr/bin/env bash
INSTALL_ROOT="$INSTALL_ROOT"
exec "\$INSTALL_ROOT/bin/lm" "\$@"
EOF

chmod +x "$launcher"

echo "[LMF CLI] Installed launcher:"
echo "  $launcher"
echo
echo "Make sure '$BIN_DIR' is on your PATH so you can run 'lm'."
