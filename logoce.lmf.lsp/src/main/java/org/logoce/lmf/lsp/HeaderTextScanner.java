package org.logoce.lmf.lsp;

import java.util.ArrayList;
import java.util.List;

/**
 * Best-effort header scanner for cases where parsing failed and we have no AST roots.
 * <p>
 * This is intentionally lightweight and only supports the small subset of header
 * attributes required by the LSP to keep features usable during transient parse states.
 */
public final class HeaderTextScanner
{
	private HeaderTextScanner()
	{
	}

	public static List<String> parseMetamodelNames(final CharSequence source)
	{
		return parseCommaSeparatedHeaderValue(source, "metamodels=");
	}

	private static List<String> parseCommaSeparatedHeaderValue(final CharSequence source, final String key)
	{
		if (source == null || source.length() == 0)
		{
			return List.of();
		}

		final String text = source.toString();
		final int idx = text.indexOf(key);
		if (idx < 0)
		{
			return List.of();
		}

		int start = idx + key.length();
		int endLine = text.indexOf('\n', start);
		if (endLine < 0)
		{
			endLine = text.length();
		}

		final String tail = text.substring(start, endLine);
		int end = tail.length();
		for (int i = 0; i < tail.length(); i++)
		{
			final char c = tail.charAt(i);
			if (Character.isWhitespace(c) || c == ')')
			{
				end = i;
				break;
			}
		}

		final String names = tail.substring(0, end).trim();
		if (names.isBlank())
		{
			return List.of();
		}

		final var result = new ArrayList<String>();
		for (final var part : names.split(","))
		{
			final var trimmed = part.trim();
			if (!trimmed.isBlank())
			{
				result.add(trimmed);
			}
		}
		return List.copyOf(result);
	}
}

