package org.logoce.lmf.editorfx.semantic;

import org.logoce.lmf.editorfx.symbol.Symbol;
import org.logoce.lmf.editorfx.symbol.SymbolKind;
import org.logoce.lmf.model.resource.ResourceUtil;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.resource.parsing.PToken;
import org.logoce.lmf.model.util.tree.Tree;

import java.nio.file.Path;
import java.util.*;

public final class SemanticModelBuilder {
	private SemanticModelBuilder() {}

	public static SemanticModel build(Path path, ResourceUtil.ParseResult parse) {
		if (parse == null || parse.roots() == null) return SemanticModel.empty();
		final List<SemanticModel.Container> containers = new ArrayList<>();
		final Map<SemanticModel.Container, List<Symbol>> byContainer = new HashMap<>();
		for (Tree<PNode> root : parse.roots()) {
			collect(path, parse.source(), root, null, containers, byContainer);
		}
		return new SemanticModel(List.copyOf(containers), Collections.unmodifiableMap(byContainer));
	}

	private static void collect(Path path, CharSequence src, Tree<PNode> node, SemanticModel.Container currentContainer,
								List<SemanticModel.Container> containers, Map<SemanticModel.Container, List<Symbol>> byContainer) {
		final List<PToken> tokens = node.data().tokens();
		final String head = tokens.isEmpty() ? "" : tokens.getFirst().value();
			final SymbolKind kind = mapKind(head);
			final int start = tokens.stream().mapToInt(PToken::offset).min().orElse(0);
			final int end = tokens.stream().mapToInt(t -> t.offset() + t.length()).max().orElse(start);
			final int line = lineFor(src, start);
			final int col = columnFor(src, start);

		SemanticModel.Container container = currentContainer;
		if (kind == SymbolKind.METAMODEL || kind == SymbolKind.DEFINITION || kind == SymbolKind.GROUP) {
			final String name = resolveName(tokens);
			container = new SemanticModel.Container(name, kind, path, start, Math.max(1, end - start), line, col);
			containers.add(container);
		} else if (kind == SymbolKind.GENERIC || kind == SymbolKind.FEATURE) {
			final String name = resolveName(tokens);
			if (name != null && currentContainer != null) {
				final var sym = new Symbol(name, kind, path, start, Math.max(1, end - start), line, col);
				byContainer.computeIfAbsent(currentContainer, c -> new ArrayList<>()).add(sym);
			}
		}

		for (Tree<PNode> child : node.children()) {
			collect(path, src, child, container, containers, byContainer);
		}
	}

	private static SymbolKind mapKind(String head) {
		return switch (head) {
			case "MetaModel" -> SymbolKind.METAMODEL;
			case "Group" -> SymbolKind.GROUP;
			case "Definition" -> SymbolKind.DEFINITION;
			case "Enum" -> SymbolKind.ENUM;
			case "Unit" -> SymbolKind.UNIT;
			case "Generic" -> SymbolKind.GENERIC;
			default -> head.startsWith("+") || head.startsWith("-") || "reference".equals(head) ? SymbolKind.FEATURE : null;
		};
	}

	private static String resolveName(List<PToken> tokens) {
		for (int i = 1; i < tokens.size(); i++) {
			final String val = tokens.get(i).value();
			if (val.startsWith("name=") && val.length() > 5) {
				return val.substring(5);
			}
			if ("name".equals(val) && i + 1 < tokens.size()) {
				return tokens.get(i + 1).value();
			}
			if (i == 1 && !val.contains("=")) {
				return val;
			}
		}
		return null;
	}

	private static int lineFor(CharSequence text, int offset) {
		int line = 1;
		for (int i = 0; i < offset && i < text.length(); i++) {
			if (text.charAt(i) == '\n') line++;
		}
		return line;
	}

	private static int columnFor(CharSequence text, int offset) {
		int col = 1;
		for (int i = offset - 1; i >= 0 && i < text.length(); i--) {
			if (text.charAt(i) == '\n') break;
			col++;
		}
		return col;
	}
}
