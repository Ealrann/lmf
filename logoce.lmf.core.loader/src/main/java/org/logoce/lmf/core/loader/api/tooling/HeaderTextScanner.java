package org.logoce.lmf.core.loader.api.tooling;

import java.util.ArrayList;
import java.util.List;

/**
 * Best-effort header scanner for cases where parsing failed and we have no AST roots.
 * <p>
 * This is intentionally lightweight and only supports the small subset of header
 * attributes required by tooling to keep features usable during transient parse states.
 */
public final class HeaderTextScanner
{
	private HeaderTextScanner()
	{
	}

	public static boolean isMetaModelRoot(final CharSequence source)
	{
		if (source == null || source.length() == 0)
		{
			return false;
		}

		final String text = source.toString();
		final String trimmed = text.stripLeading();
		return trimmed.startsWith("(MetaModel");
	}

	public static List<String> parseMetamodelNames(final CharSequence source)
	{
		return parseCommaSeparatedHeaderValue(source, "metamodels=");
	}

	public static String parseMetaModelQualifiedName(final CharSequence source)
	{
		if (!isMetaModelRoot(source))
		{
			return null;
		}

		final String text = source.toString();
		final int limit = Math.min(text.length(), 512);
		final String head = text.substring(0, limit);

		final String name = parseScalarHeaderValue(head, "name=");
		if (name == null || name.isBlank())
		{
			return null;
		}

		final String domain = parseScalarHeaderValue(head, "domain=");
		if (domain == null || domain.isBlank())
		{
			return name;
		}
		return domain + "." + name;
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

	private static String parseScalarHeaderValue(final String source, final String key)
	{
		final int idx = source.indexOf(key);
		if (idx < 0)
		{
			return null;
		}

		int start = idx + key.length();
		final int endLine = source.indexOf('\n', start);
		final String tail = endLine < 0 ? source.substring(start) : source.substring(start, endLine);

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

		final String value = tail.substring(0, end).trim();
		return value.isBlank() ? null : value;
	}
}
