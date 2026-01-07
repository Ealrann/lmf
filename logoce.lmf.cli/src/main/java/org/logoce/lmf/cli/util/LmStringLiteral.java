package org.logoce.lmf.cli.util;

public final class LmStringLiteral
{
	private LmStringLiteral()
	{
	}

	public static String fromUserValue(final String raw)
	{
		final var value = unquote(raw == null ? "" : raw.strip());
		if (value.isEmpty())
		{
			return "\"\"";
		}

		return needsQuotes(value)
			   ? "\"" + escape(value) + "\""
			   : value;
	}

	public static String unquote(final String value)
	{
		if (value == null)
		{
			return "";
		}
		if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\""))
		{
			return value.substring(1, value.length() - 1);
		}
		return value;
	}

	public static boolean needsQuotes(final String value)
	{
		if (value == null || value.isEmpty())
		{
			return true;
		}

		for (int i = 0; i < value.length(); i++)
		{
			final char c = value.charAt(i);
			if (Character.isWhitespace(c))
			{
				return true;
			}
			if (c == '"' || c == '(' || c == ')' || c == ',' || c == '=')
			{
				return true;
			}
		}
		return false;
	}

	public static String escape(final String value)
	{
		if (value == null || value.isEmpty())
		{
			return "";
		}

		final var builder = new StringBuilder(value.length());
		for (int i = 0; i < value.length(); i++)
		{
			final char c = value.charAt(i);
			if (c == '\\' || c == '"')
			{
				builder.append('\\');
			}
			builder.append(c);
		}
		return builder.toString();
	}
}

