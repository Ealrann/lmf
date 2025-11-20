package org.logoce.lmf.editorfx.lang;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.logoce.lmf.editorfx.diagnostic.Diagnostic;
import org.logoce.lmf.editorfx.diagnostic.DiagnosticSeverity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class SyntaxHighlighter {
	private final LMCoreLexerAdapter lexer = new LMCoreLexerAdapter();

	public StyleSpans<Collection<String>> computeHighlighting(String text) {
		final List<Token> tokens = lexer.lex(text);
		final StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

		int lastIndex = 0;
		for (Token token : tokens) {
			if (token.start() > lastIndex) {
				spansBuilder.add(Collections.emptyList(), token.start() - lastIndex);
			}
			spansBuilder.add(styleFor(token.type()), token.end() - token.start());
			lastIndex = token.end();
		}
		if (lastIndex < text.length()) {
			spansBuilder.add(Collections.emptyList(), text.length() - lastIndex);
		}
		return spansBuilder.create();
	}

	public StyleSpans<Collection<String>> withDiagnostics(String text, StyleSpans<Collection<String>> base, List<Diagnostic> diagnostics) {
		if (diagnostics == null || diagnostics.isEmpty()) {
			return base;
		}
		final boolean[] errorMask = buildMask(text, diagnostics, true);
		final boolean[] warnMask = buildMask(text, diagnostics, false);
		final StyleSpansBuilder<Collection<String>> builder = new StyleSpansBuilder<>();
		int offset = 0;
		for (var span : base) {
			int remaining = span.getLength();
			while (remaining > 0) {
				final boolean err = offset < errorMask.length && errorMask[offset];
				final boolean warn = offset < warnMask.length && warnMask[offset] && !err;
				int run = 1;
				while (run < remaining && (offset + run) < text.length()
					&& (err == (offset + run < errorMask.length && errorMask[offset + run]))
					&& (warn == (offset + run < warnMask.length && warnMask[offset + run] && !err))) {
					run++;
				}
				final Collection<String> styles;
				if (err) {
					final var merged = new ArrayList<>(span.getStyle());
					merged.add("error-underline");
					styles = merged;
				} else if (warn) {
					final var merged = new ArrayList<>(span.getStyle());
					merged.add("warning-underline");
					styles = merged;
				} else {
					styles = span.getStyle();
				}
				builder.add(styles, run);
				offset += run;
				remaining -= run;
			}
		}
		if (offset < text.length()) {
			builder.add(Collections.emptyList(), text.length() - offset);
		}
		return builder.create();
	}

	private Collection<String> styleFor(TokenType type) {
		return switch (type) {
			case KEYWORD -> List.of("keyword");
			case IDENT -> List.of("identifier");
			case NUMBER -> List.of("number");
			case STRING -> List.of("string");
			case COMMENT -> List.of("comment");
			case LPAREN, RPAREN, ASSIGN, PLUS, MINUS, SYMBOL -> List.of("symbol");
			default -> Collections.emptyList();
		};
	}

	private boolean[] buildMask(String text, List<Diagnostic> diagnostics, boolean errors) {
		final boolean[] mask = new boolean[text.length()];
		for (Diagnostic d : diagnostics) {
			if (errors && d.severity() != DiagnosticSeverity.ERROR) continue;
			if (!errors && d.severity() != DiagnosticSeverity.WARNING) continue;
			final int start = offsetFor(text, d.line(), d.column());
			final int end = Math.min(text.length(), start + Math.max(1, d.length()));
			for (int i = start; i < end; i++) {
				mask[i] = true;
			}
		}
		return mask;
	}

	private int offsetFor(String text, int line, int column) {
		int currentLine = 1;
		int idx = 0;
		while (idx < text.length() && currentLine < line) {
			if (text.charAt(idx) == '\n') {
				currentLine++;
			}
			idx++;
		}
		return Math.min(text.length(), idx + Math.max(0, column - 1));
	}
}
