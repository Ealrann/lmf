package org.logoce.lmf.editorfx.ui;

import org.fxmisc.richtext.CodeArea;

public final class TextOps {
	private TextOps() {
	}

	public static String wordAt(String text, int caret) {
		if (text == null || text.isEmpty()) return "";
		int start = caret;
		while (start > 0 && isWordChar(text.charAt(start - 1))) start--;
		int end = caret;
		while (end < text.length() && isWordChar(text.charAt(end))) end++;
		if (start == end) return "";
		return text.substring(start, end);
	}

	public static void replaceWordAtCaret(CodeArea area, String replacement) {
		final String text = area.getText();
		final int caret = area.getCaretPosition();
		int start = caret;
		while (start > 0 && isWordChar(text.charAt(start - 1))) start--;
		int end = caret;
		while (end < text.length() && isWordChar(text.charAt(end))) end++;
		area.replaceText(start, end, replacement);
		area.moveTo(start + replacement.length());
	}

	private static boolean isWordChar(char c) {
		return Character.isJavaIdentifierPart(c);
	}
}
