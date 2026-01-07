package org.logoce.lmf.cli.move;

import java.util.OptionalInt;

record MoveSlotReference(String parentReference, String featureName, OptionalInt index)
{
	static MoveSlotReference parse(final String reference)
	{
		if (reference == null || reference.isBlank())
		{
			throw new MovePlanException("Target reference is missing");
		}

		final int lastSlash = reference.lastIndexOf('/');
		if (lastSlash < 0)
		{
			throw new MovePlanException("Target reference must include a parent path (for example '/parent/items.0')");
		}

		final var parentRef = lastSlash == 0 ? "/" : reference.substring(0, lastSlash);
		final var lastSegment = reference.substring(lastSlash + 1);

		final var index = parseIndex(lastSegment);
		final var featureName = index.isPresent()
							? lastSegment.substring(0, lastSegment.lastIndexOf('.'))
							: lastSegment;

		if (featureName.isBlank())
		{
			throw new MovePlanException("Invalid target reference: " + reference);
		}

		return new MoveSlotReference(parentRef, featureName, index);
	}

	private static OptionalInt parseIndex(final String segment)
	{
		if (segment == null)
		{
			return OptionalInt.empty();
		}

		final int dot = segment.lastIndexOf('.');
		if (dot < 0 || dot == segment.length() - 1)
		{
			return OptionalInt.empty();
		}

		for (int i = dot + 1; i < segment.length(); i++)
		{
			if (!Character.isDigit(segment.charAt(i)))
			{
				return OptionalInt.empty();
			}
		}

		final int index = Integer.parseInt(segment.substring(dot + 1));
		return OptionalInt.of(index);
	}
}
