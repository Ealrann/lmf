package org.logoce.lmf.editorfx.symbol;

import org.logoce.lmf.editorfx.core.Document;

import java.util.ArrayList;
import java.util.List;

public final class ReferenceFinder {
	private ReferenceFinder() {
	}

	public static List<Reference> findReferences(Document document, String symbolName) {
		final List<Reference> refs = new ArrayList<>();
		final String text = document.getText();
		int idx = 0;
		while (idx < text.length()) {
			final int next = text.indexOf(symbolName, idx);
			if (next < 0) break;
			// Ensure we are on word boundaries
			final boolean beforeOk = next == 0 || !Character.isJavaIdentifierPart(text.charAt(next - 1));
			final int end = next + symbolName.length();
			final boolean afterOk = end >= text.length() || !Character.isJavaIdentifierPart(text.charAt(end));
			if (beforeOk && afterOk) {
				final LineCol lc = lineCol(text, next);
				refs.add(new Reference(symbolName, document.path(), next, symbolName.length(), lc.line, lc.col));
			}
			idx = next + symbolName.length();
		}
		return refs;
	}

	public static List<Reference> findReferences(Document document, String symbolName, int startOffset, int endOffset) {
		final List<Reference> refs = new ArrayList<>();
		final String text = document.getText();
		int idx = Math.max(0, startOffset);
		final int endLimit = endOffset <= 0 ? text.length() : Math.min(text.length(), endOffset);
		while (idx < endLimit) {
			final int next = text.indexOf(symbolName, idx);
			if (next < 0 || next >= endLimit) break;
			final boolean beforeOk = next == 0 || !Character.isJavaIdentifierPart(text.charAt(next - 1));
			final int end = next + symbolName.length();
			final boolean afterOk = end >= text.length() || !Character.isJavaIdentifierPart(text.charAt(end));
			if (beforeOk && afterOk) {
				final LineCol lc = lineCol(text, next);
				refs.add(new Reference(symbolName, document.path(), next, symbolName.length(), lc.line, lc.col));
			}
			idx = next + symbolName.length();
		}
		return refs;
	}

	public static List<Reference> findReferences(Iterable<Document> documents, String symbolName) {
		final List<Reference> all = new ArrayList<>();
		for (Document doc : documents) {
			all.addAll(findReferences(doc, symbolName));
		}
		return all;
	}

	private static LineCol lineCol(String src, int offset) {
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
