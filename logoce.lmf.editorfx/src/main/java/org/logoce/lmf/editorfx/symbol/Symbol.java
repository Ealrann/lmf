package org.logoce.lmf.editorfx.symbol;

import java.nio.file.Path;

public record Symbol(String name, SymbolKind kind, Path path, int offset, int length, int line, int column) {
}
