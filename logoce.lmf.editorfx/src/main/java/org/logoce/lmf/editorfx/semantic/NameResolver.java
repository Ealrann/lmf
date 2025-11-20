package org.logoce.lmf.editorfx.semantic;

import org.logoce.lmf.editorfx.core.Document;
import org.logoce.lmf.editorfx.symbol.Symbol;
import org.logoce.lmf.editorfx.symbol.SymbolKind;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NameResolver {
	private static final Pattern GENERIC_IDX = Pattern.compile("generics\\.(\\d+)");

	public Symbol resolve(Document doc, int caret, String token, List<String> candidates, List<Symbol> workspaceSymbols) {
		final SemanticModel.Container container = containerAt(doc, caret);
		final int containerStart = container != null ? container.offset() : 0;
		final int containerEnd = container != null ? container.offset() + container.length() : -1;

		final Symbol atCaret = symbolAtCaret(doc, caret, containerStart, containerEnd);
		if (atCaret != null) return atCaret;

		final Symbol generic = resolveGeneric(doc, token, container);
		if (generic != null) return generic;

		final Symbol local = resolveLocal(doc, candidates, containerStart, containerEnd);
		if (local != null) return local;

		return resolveWorkspace(candidates, workspaceSymbols);
	}

	private Symbol resolveGeneric(Document doc, String token, SemanticModel.Container container) {
		if (token == null || container == null) return null;
		final Matcher m = GENERIC_IDX.matcher(token);
		if (!m.find()) return null;
		final int idx = Integer.parseInt(m.group(1));
		final List<Symbol> locals = doc.semanticModel().symbolsByContainer().getOrDefault(container, List.of()).stream()
			.filter(sym -> sym.kind() == SymbolKind.GENERIC)
			.sorted(Comparator.comparingInt(Symbol::offset))
			.toList();
		return (idx >= 0 && idx < locals.size()) ? locals.get(idx) : null;
	}

	private Symbol resolveLocal(Document doc, List<String> candidates, int start, int end) {
		for (String name : candidates) {
			final var hit = doc.symbols().stream()
				.filter(sym -> sym.name().equals(name))
				.filter(sym -> sym.offset() >= start && (end < 0 || sym.offset() < end))
				.min(Comparator.comparingInt(Symbol::offset));
			if (hit.isPresent()) return hit.get();
		}
		return null;
	}

	private Symbol resolveWorkspace(List<String> candidates, List<Symbol> workspaceSymbols) {
		for (String name : candidates) {
			final var hit = workspaceSymbols.stream()
				.filter(sym -> sym.name().equals(name))
				.findFirst();
			if (hit.isPresent()) return hit.get();
		}
		return null;
	}

	private Symbol symbolAtCaret(Document doc, int caret, int start, int end) {
		return doc.symbols().stream()
			.filter(sym -> sym.offset() >= start && (end < 0 || sym.offset() < end))
			.filter(sym -> caret >= sym.offset() && caret <= sym.offset() + sym.length())
			.min(Comparator.comparingInt(Symbol::offset))
			.orElse(null);
	}

	public SemanticModel.Container containerAt(Document doc, int caret) {
		return doc.semanticModel().containers().stream()
			.filter(c -> caret >= c.offset() && caret <= c.offset() + c.length())
			.max(Comparator.comparingInt(SemanticModel.Container::offset))
			.orElse(null);
	}
}
