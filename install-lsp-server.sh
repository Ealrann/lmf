#!/usr/bin/env bash
set -euo pipefail

# Rebuild the LSP server and install it to a stable location so the IntelliJ
# plugin (and users) can run it as a plain `logoce.lmf.lsp` command.

PROJECT=":logoce.lmf.lsp"

echo "[LMF LSP] Building installable distribution for $PROJECT..."
./gradlew "$PROJECT:installDist"

# Discover the effective buildDir (respects any init.gradle overrides).
build_dir=$(./gradlew -q "$PROJECT:properties" | awk -F': ' '/^buildDir: / {print $2; exit}')
if [[ -z "${build_dir:-}" ]]; then
	echo "Error: could not determine buildDir for $PROJECT" >&2
	exit 1
fi

src_root="$build_dir/install/logoce.lmf.lsp"
src_bin="$src_root/bin/logoce.lmf.lsp"

if [[ ! -x "$src_bin" ]]; then
	echo "Error: LSP server launcher not found at $src_bin" >&2
	exit 1
fi

# Where to install the LSP server distribution.
INSTALL_ROOT="${LMF_LSP_INSTALL_ROOT:-$HOME/.local/share/logoce-lmf-lsp}"
BIN_DIR="${LMF_LSP_BIN_DIR:-$HOME/.local/bin}"

echo "[LMF LSP] Installing server to: $INSTALL_ROOT"
rm -rf "$INSTALL_ROOT"
mkdir -p "$(dirname "$INSTALL_ROOT")"
cp -R "$src_root" "$INSTALL_ROOT"

echo "[LMF LSP] Creating launcher in: $BIN_DIR"
mkdir -p "$BIN_DIR"
launcher="$BIN_DIR/logoce.lmf.lsp"

cat > "$launcher" <<EOF
#!/usr/bin/env bash
INSTALL_ROOT="$INSTALL_ROOT"
exec "\$INSTALL_ROOT/bin/logoce.lmf.lsp" "\$@"
EOF

chmod +x "$launcher"

echo "[LMF LSP] Installed launcher:"
echo "  $launcher"
echo
echo "Make sure '$BIN_DIR' is on your PATH so the IntelliJ plugin can run 'logoce.lmf.lsp'."

