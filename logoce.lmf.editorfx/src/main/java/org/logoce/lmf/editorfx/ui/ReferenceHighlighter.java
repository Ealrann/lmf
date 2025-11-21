package org.logoce.lmf.editorfx.ui;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.logoce.lmf.editorfx.core.Document;
import org.logoce.lmf.editorfx.core.ModelService;
import org.logoce.lmf.editorfx.semantic.NameResolver;
import org.logoce.lmf.editorfx.semantic.SemanticModel;
import org.logoce.lmf.editorfx.symbol.Reference;
import org.logoce.lmf.editorfx.symbol.ReferenceFinder;
import org.logoce.lmf.editorfx.symbol.Symbol;
import org.logoce.lmf.editorfx.lang.SyntaxHighlighter;
import org.logoce.lmf.editorfx.diagnostic.Diagnostic;
import org.logoce.lmf.editorfx.diagnostic.DiagnosticSeverity;

import java.nio.file.Path;
import java.util.*;

public final class ReferenceHighlighter {
	private final SyntaxHighlighter highlighter;
	private final Map<Path, List<Reference>> highlights = new HashMap<>();
	private final Map<Path, List<Diagnostic>> transientErrors = new HashMap<>();

	public ReferenceHighlighter(SyntaxHighlighter highlighter) {
		this.highlighter = highlighter;
	}

	public List<Reference> update(Document document, CodeArea area, List<String> names, String token, NameResolver resolver, ModelService modelService, int fallbackStart, int fallbackEnd) {
		if (document == null || names.isEmpty()) {
			return List.of();
		}
		final int caret = area.getCaretPosition();
		final SemanticModel.Container container = resolver.containerAt(document, caret);
		final int start = container != null ? container.offset() : 0;
		final int end = container != null ? container.offset() + container.length() : -1;
		final var refs = new ArrayList<>(ReferenceFinder.findReferences(document, names.getFirst(), start, end));
		final Symbol target = resolver.resolve(document, caret, token, names, modelService.allSymbols());
		if (target != null) {
			refs.add(symbolToReference(target));
			transientErrors.remove(document.path());
		} else if (!names.isEmpty() && fallbackEnd > fallbackStart) {
			final var lc = lineCol(document.getText(), fallbackStart);
			refs.add(new Reference(names.getFirst(), document.path(), fallbackStart, fallbackEnd - fallbackStart, lc.line(), lc.col()));
			final Diagnostic diag = new Diagnostic(document.path(), lc.line(), lc.col(), Math.max(1, fallbackEnd - fallbackStart), DiagnosticSeverity.ERROR, "Unresolved reference: " + names.getFirst());
			transientErrors.put(document.path(), List.of(diag));
		} else {
			transientErrors.remove(document.path());
		}
		highlights.put(document.path(), refs);
		return refs;
	}

	private LineCol lineCol(String text, int offset) {
		int line = 1;
		int col = 1;
		for (int i = 0; i < text.length() && i < offset; i++) {
			if (text.charAt(i) == '\n') {
				line++;
				col = 1;
			} else {
				col++;
			}
		}
		return new LineCol(line, col);
	}

	private record LineCol(int line, int col) {}

	public void clear(Path path) {
		highlights.remove(path);
		transientErrors.remove(path);
	}

	public void applyHighlight(CodeArea area, Document doc) {
		final String text = area.getText();
		final var base = highlighter.computeHighlighting(text);
		final var diag = new ArrayList<>(doc.diagnostics());
		diag.addAll(transientErrors.getOrDefault(doc.path(), List.of()));
		var spans = highlighter.withDiagnostics(text, base, diag);
		final var refs = highlights.getOrDefault(doc.path(), List.of());
		if (!refs.isEmpty()) {
			final var overlay = buildOverlay(text.length(), refs);
			spans = spans.overlay(overlay, (a, b) -> {
				final var merged = new ArrayList<>(a);
				merged.addAll(b);
				return merged;
			});
		}
		final var parens = findParenMatch(text, area.getCaretPosition());
		if (parens != null) {
			final var overlay = buildParenOverlay(text.length(), parens.open(), parens.close());
			spans = spans.overlay(overlay, (a, b) -> {
				final var merged = new ArrayList<>(a);
				merged.addAll(b);
				return merged;
			});
		}
		area.setStyleSpans(0, spans);
	}

	private StyleSpans<Collection<String>> buildOverlay(int textLength, List<Reference> refs) {
		final var sorted = refs.stream().sorted(Comparator.comparingInt(Reference::offset)).toList();
		final var builder = new StyleSpansBuilder<Collection<String>>();
		int pos = 0;
		for (var ref : sorted) {
			if (ref.offset() > pos) {
				builder.add(List.of(), ref.offset() - pos);
				pos = ref.offset();
			}
			final int len = Math.max(1, ref.length());
			builder.add(List.of("ref-highlight"), len);
			pos += len;
		}
		if (pos < textLength) {
			builder.add(List.of(), textLength - pos);
		}
		return builder.create();
	}

	private StyleSpans<Collection<String>> buildParenOverlay(int textLength, int open, int close) {
		final var builder = new StyleSpansBuilder<Collection<String>>();
		int pos = 0;
		int first = Math.min(open, close);
		int second = Math.max(open, close);
		if (first > pos) {
			builder.add(List.of(), first - pos);
			pos = first;
		}
		builder.add(List.of("paren-highlight"), 1);
		pos++;
		if (second > pos) {
			builder.add(List.of(), second - pos);
			pos = second;
		}
		builder.add(List.of("paren-highlight"), 1);
		pos++;
		if (pos < textLength) {
			builder.add(List.of(), textLength - pos);
		}
		return builder.create();
	}

	private ParenMatch findParenMatch(String text, int caret) {
		final int leftIdx = caret > 0 ? caret - 1 : -1;
		if (leftIdx >= 0 && isParen(text.charAt(leftIdx))) {
			return text.charAt(leftIdx) == '('
				   ? matchForward(text, leftIdx)
				   : matchBackward(text, leftIdx);
		}
		if (caret < text.length() && isParen(text.charAt(caret))) {
			return text.charAt(caret) == '('
				   ? matchForward(text, caret)
				   : matchBackward(text, caret);
		}
		return null;
	}

	private ParenMatch matchForward(String text, int start) {
		int depth = 0;
		for (int i = start; i < text.length(); i++) {
			final char c = text.charAt(i);
			if (c == '(') depth++;
			else if (c == ')') {
				depth--;
				if (depth == 0) return new ParenMatch(start, i);
			}
		}
		return null;
	}

	private ParenMatch matchBackward(String text, int start) {
		int depth = 0;
		for (int i = start; i >= 0; i--) {
			final char c = text.charAt(i);
			if (c == ')') depth++;
			else if (c == '(') {
				depth--;
				if (depth == 0) return new ParenMatch(i, start);
			}
		}
		return null;
	}

	private static boolean isParen(char c) {
		return c == '(' || c == ')';
	}

	private Reference symbolToReference(Symbol symbol) {
		return new Reference(symbol.name(), symbol.path(), symbol.offset(), symbol.length(), symbol.line(), symbol.column());
	}

	private record ParenMatch(int open, int close) {}
}
