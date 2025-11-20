package org.logoce.lmf.editorfx.symbol;

import java.nio.file.Path;

public record Reference(String name, Path path, int offset, int length, int line, int column) {
}
