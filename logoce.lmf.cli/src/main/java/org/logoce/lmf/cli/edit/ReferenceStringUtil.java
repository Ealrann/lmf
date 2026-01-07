package org.logoce.lmf.cli.edit;

public final class ReferenceStringUtil
{
	private ReferenceStringUtil()
	{
	}

	public static boolean isPathLikeReference(final String raw)
	{
		if (raw == null || raw.isBlank())
		{
			return false;
		}

		if (raw.startsWith("@"))
		{
			return raw.contains("/");
		}

		if (raw.startsWith("#"))
		{
			final int at = raw.indexOf('@');
			if (at >= 0)
			{
				return raw.indexOf('/', at) >= 0;
			}
			return raw.contains("/");
		}

		return true;
	}

	public static int findPathSegment(final String raw, final String segment)
	{
		int idx = 0;
		while (idx >= 0 && idx < raw.length())
		{
			idx = raw.indexOf(segment, idx);
			if (idx < 0)
			{
				return -1;
			}

			final int before = idx - 1;
			final int after = idx + segment.length();

			final boolean startOk = before < 0
									|| raw.charAt(before) == '/'
									|| raw.charAt(before) == '@'
									|| raw.charAt(before) == '#';
			final boolean endOk = after >= raw.length()
								  || raw.charAt(after) == '/'
								  || raw.charAt(after) == ')';

			if (startOk && endOk)
			{
				return idx;
			}

			idx = after;
		}

		return -1;
	}
}
