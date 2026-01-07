package org.logoce.lmf.cli.remove;

import org.logoce.lmf.cli.edit.FeatureValueSpanIndex;
import org.logoce.lmf.cli.edit.TextEdits;
import org.logoce.lmf.core.loader.api.loader.util.TextPositions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class RemoveMutationApplier
{
	TextEdits.TextEdit apply(final CharSequence source,
							 final FeatureValueSpanIndex.FeatureSpan featureSpan,
							 final List<RemoveValueMutation> mutations)
	{
		if (mutations.isEmpty())
		{
			return null;
		}

		final var assignmentSpan = featureSpan.assignmentSpan();
		if (assignmentSpan == null)
		{
			throw new RemovePlanException("Missing assignment span for feature");
		}

		final var values = featureSpan.values();
		final var removalIndices = new HashSet<Integer>();
		final var replacementEdits = new ArrayList<TextEdits.TextEdit>();

		for (final var mutation : mutations)
		{
			if (mutation.kind() == RemoveMutationKind.REMOVE)
			{
				final int index = values.indexOf(mutation.valueSpan());
				if (index >= 0)
				{
					removalIndices.add(index);
				}
				continue;
			}

			if (mutation.kind() == RemoveMutationKind.SHIFT)
			{
				final var span = mutation.valueSpan().span();
				final int offset = span.offset() - assignmentSpan.offset();
				replacementEdits.add(new TextEdits.TextEdit(offset, span.length(), mutation.newRaw()));
			}
		}

		if (!removalIndices.isEmpty() && removalIndices.size() == values.size())
		{
			return new TextEdits.TextEdit(assignmentSpan.offset(), assignmentSpan.length(), "");
		}

		final var removalEdits = buildRemovalEdits(source, assignmentSpan, values, removalIndices);
		final var innerEdits = new ArrayList<TextEdits.TextEdit>();
		innerEdits.addAll(replacementEdits);
		innerEdits.addAll(removalEdits);

		if (innerEdits.isEmpty())
		{
			return null;
		}

		final var assignmentText = source.subSequence(assignmentSpan.offset(),
													 assignmentSpan.offset() + assignmentSpan.length())
										 .toString();
		final var updatedAssignment = TextEdits.apply(assignmentText, innerEdits);

		if (updatedAssignment.equals(assignmentText))
		{
			return null;
		}

		return new TextEdits.TextEdit(assignmentSpan.offset(), assignmentSpan.length(), updatedAssignment);
	}

	private static List<TextEdits.TextEdit> buildRemovalEdits(final CharSequence source,
															  final TextPositions.Span assignmentSpan,
															  final List<FeatureValueSpanIndex.ValueSpan> values,
															  final Set<Integer> removalIndices)
	{
		if (removalIndices.isEmpty())
		{
			return List.of();
		}

		final var fullSpans = new ArrayList<TextPositions.Span>(values.size());
		for (final var value : values)
		{
			fullSpans.add(fullSpan(value, source));
		}

		final var edits = new ArrayList<TextEdits.TextEdit>();

		for (final int index : removalIndices)
		{
			final int prevKept = findPreviousKept(removalIndices, index);
			final int nextKept = findNextKept(removalIndices, values.size(), index);

			final var currentSpan = fullSpans.get(index);
			int removeStart = currentSpan.offset();
			int removeEnd = currentSpan.offset() + currentSpan.length();

			if (prevKept >= 0)
			{
				final var prevSpan = fullSpans.get(prevKept);
				final int searchStart = prevSpan.offset() + prevSpan.length();
				final int searchEnd = currentSpan.offset();
				final int comma = findCommaBackward(source, searchStart, searchEnd);
				if (comma >= 0)
				{
					removeStart = comma;
				}
			}
			else if (nextKept >= 0)
			{
				final var nextSpan = fullSpans.get(nextKept);
				final int searchStart = currentSpan.offset() + currentSpan.length();
				final int searchEnd = nextSpan.offset();
				final int comma = findCommaForward(source, searchStart, searchEnd);
				if (comma >= 0)
				{
					removeEnd = skipWhitespace(source, comma + 1, searchEnd);
				}
			}

			final int relativeStart = removeStart - assignmentSpan.offset();
			final int relativeLength = removeEnd - removeStart;
			if (relativeLength > 0)
			{
				edits.add(new TextEdits.TextEdit(relativeStart, relativeLength, ""));
			}
		}

		return List.copyOf(edits);
	}

	private static int findPreviousKept(final Set<Integer> removals, final int index)
	{
		for (int i = index - 1; i >= 0; i--)
		{
			if (!removals.contains(i))
			{
				return i;
			}
		}
		return -1;
	}

	private static int findNextKept(final Set<Integer> removals, final int total, final int index)
	{
		for (int i = index + 1; i < total; i++)
		{
			if (!removals.contains(i))
			{
				return i;
			}
		}
		return -1;
	}

	private static TextPositions.Span fullSpan(final FeatureValueSpanIndex.ValueSpan valueSpan, final CharSequence source)
	{
		if (!valueSpan.quoted())
		{
			return valueSpan.span();
		}

		int start = valueSpan.span().offset();
		int length = valueSpan.span().length();

		if (start > 0 && source.charAt(start - 1) == '"')
		{
			start -= 1;
			length += 1;
		}

		final int end = start + length;
		if (end < source.length() && source.charAt(end) == '"')
		{
			length += 1;
		}

		return new TextPositions.Span(valueSpan.span().line(),
									  valueSpan.span().column(),
									  length,
									  start);
	}

	private static int findCommaBackward(final CharSequence source, final int start, final int end)
	{
		for (int i = Math.min(end - 1, source.length() - 1); i >= start; i--)
		{
			if (source.charAt(i) == ',')
			{
				return i;
			}
		}
		return -1;
	}

	private static int findCommaForward(final CharSequence source, final int start, final int end)
	{
		for (int i = Math.max(0, start); i < end && i < source.length(); i++)
		{
			if (source.charAt(i) == ',')
			{
				return i;
			}
		}
		return -1;
	}

	private static int skipWhitespace(final CharSequence source, final int start, final int end)
	{
		int cursor = start;
		while (cursor < end && cursor < source.length() && Character.isWhitespace(source.charAt(cursor)))
		{
			cursor++;
		}
		return cursor;
	}
}
