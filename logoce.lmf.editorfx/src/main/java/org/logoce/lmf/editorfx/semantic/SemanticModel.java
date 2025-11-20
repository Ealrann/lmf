package org.logoce.lmf.editorfx.semantic;

import org.logoce.lmf.editorfx.symbol.Symbol;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.logoce.lmf.editorfx.symbol.SymbolKind;

public record SemanticModel(List<Container> containers, Map<Container, List<Symbol>> symbolsByContainer) {
	public static SemanticModel empty() {
		return new SemanticModel(List.of(), Map.of());
	}

	public record Container(String name, SymbolKind kind, Path path, int offset, int length, int line, int column) {}
}
