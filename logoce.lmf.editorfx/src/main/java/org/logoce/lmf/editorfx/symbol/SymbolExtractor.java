package org.logoce.lmf.editorfx.symbol;

import org.logoce.lmf.model.resource.ResourceUtil;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.resource.parsing.PToken;
import org.logoce.lmf.model.util.tree.Tree;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class SymbolExtractor {
	private static final Set<String> TOP_LEVEL_KWS = Set.of("MetaModel", "Group", "Definition", "Enum", "Unit", "Generic");

	public static List<Symbol> extract(Path path, ResourceUtil.ParseResult parseResult) {
		final List<Symbol> symbols = new ArrayList<>();
		if (parseResult == null || parseResult.roots() == null) {
			return symbols;
		}
		final CharSequence source = parseResult.source();
		for (Tree<PNode> root : parseResult.roots()) {
			collectSymbols(path, source, root, symbols);
		}
		return symbols;
	}

	private static void collectSymbols(Path path, CharSequence source, Tree<PNode> node, List<Symbol> symbols) {
		final List<PToken> tokens = node.data().tokens();
		if (tokens.size() >= 2) {
			final String head = tokens.getFirst().value();
			final SymbolKind kind = mapKind(head);
			if (kind != null) {
				final PToken nameTok = resolveNameToken(tokens);
				if (nameTok != null) {
					final LineCol lc = lineCol(source, nameTok.offset());
					symbols.add(new Symbol(nameTok.value(), kind, path, nameTok.offset(), nameTok.length(), lc.line(), lc.col()));
				}
			}
		}
		for (Tree<PNode> child : node.children()) {
			collectSymbols(path, source, child, symbols);
		}
	}

	private static PToken resolveNameToken(List<PToken> tokens) {
		for (int i = 1; i < tokens.size(); i++) {
			final PToken tok = tokens.get(i);
			final String val = tok.value();
			// Inline "name=Foo"
			if (val.startsWith("name=") && val.length() > "name=".length()) {
				return new PToken(val.substring("name=".length()), tok.type(), tok.offset(), tok.length());
			}
			// Split tokens: name = Foo
			if ("name".equals(val)) {
				if (i + 2 < tokens.size() && "=".equals(tokens.get(i + 1).value())) {
					final PToken candidate = tokens.get(i + 2);
					return new PToken(candidate.value(), candidate.type(), candidate.offset(), candidate.length());
				}
				if (i + 1 < tokens.size()) {
					final PToken candidate = tokens.get(i + 1);
					return new PToken(candidate.value(), candidate.type(), candidate.offset(), candidate.length());
				}
			}
			// Inline "foo=Bar" fallback
			final int eq = val.indexOf('=');
			if (eq > 0 && eq + 1 < val.length()) {
				return new PToken(val.substring(eq + 1), tok.type(), tok.offset() + eq + 1, tok.length() - eq - 1);
			}
		}
		// fallback: first identifier-looking token after head that is not a keyword
		for (int i = 1; i < tokens.size(); i++) {
			final PToken tok = tokens.get(i);
			final String val = tok.value();
			if (val.isEmpty()) continue;
			if (!Character.isJavaIdentifierStart(val.charAt(0))) continue;
			if (val.startsWith("+") || val.startsWith("-")) continue;
			if (val.contains("=")) continue;
			if (TOP_LEVEL_KWS.contains(val) || "includes".equals(val)) continue;
			return tok;
		}
		return null;
	}

	private static SymbolKind mapKind(String head) {
		final String trimmed = head.trim();
		return switch (trimmed) {
			case "MetaModel" -> SymbolKind.METAMODEL;
			case "Group" -> SymbolKind.GROUP;
			case "Definition" -> SymbolKind.DEFINITION;
			case "Enum" -> SymbolKind.ENUM;
			case "Unit" -> SymbolKind.UNIT;
			case "Generic" -> SymbolKind.GENERIC;
			default -> {
				if (trimmed.startsWith("+") || trimmed.startsWith("-") || "reference".equals(trimmed)) {
					yield SymbolKind.FEATURE;
				}
				yield null;
			}
		};
	}

	private static LineCol lineCol(CharSequence src, int offset) {
		int line = 1;
		int col = 1;
		for (int i = 0; i < src.length() && i < offset; i++) {
			if (src.charAt(i) == '\n') {
				line++;
				col = 1;
			} else {
				col++;
			}
		}
		return new LineCol(line, col);
	}

	private record LineCol(int line, int col) {}
}
