package org.logoce.lmf.core.loader.linking.feature.reference;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility helpers around {@link PathParser} that expose per-step
 * ranges within the original path string. This is intended for
 * tooling and LSP integrations that need fine-grained ranges
 * for go-to-definition and rename inside complex paths.
 */
public final class PathUtil
{
	private PathUtil()
	{
	}

	public record Segment(PathParser.Type type, String text, int start, int end)
	{
	}

	public record ParsedPath(String raw, List<Segment> segments)
	{
	}

	public static ParsedPath parse(final String path)
	{
		final var parser = new PathParser(path);
		final var segments = new ArrayList<Segment>();

		int cursor = 0;
		while (parser.hasNext())
		{
			final var step = parser.next();
			final int start = cursor;
			final int length;

			if (step.type() == PathParser.Type.NAME || step.type() == PathParser.Type.CONTEXT_NAME)
			{
				// @Name / ^Name – include the leading sigil
				length = 1 + step.text().length();
			}
			else if (step.type() == PathParser.Type.MODEL)
			{
				// #Model – include the leading '#'
				length = 1 + step.text().length();
			}
			else if (step.type() == PathParser.Type.ROOT)
			{
				// Single '/'
				length = 1;
			}
			else if (step.type() == PathParser.Type.CURRENT)
			{
				// "./"
				length = 2;
			}
			else if (step.type() == PathParser.Type.PARENT)
			{
				// "../"
				length = 3;
			}
			else
			{
				// CHILD – already a plain segment without separators
				length = step.text().length();
			}

			final int end = start + length;
			segments.add(new Segment(step.type(), step.text(), start, end));
			cursor = end;

			// Skip a single '/' separator between segments when present.
			if (cursor < path.length()
				&& path.charAt(cursor) == '/'
				&& step.type() != PathParser.Type.ROOT
				&& step.type() != PathParser.Type.CURRENT
				&& step.type() != PathParser.Type.PARENT)
			{
				cursor++;
			}
		}

		return new ParsedPath(path, List.copyOf(segments));
	}
}
