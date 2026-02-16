package org.logoce.lmf.core.loader.api.loader.linking;

import java.util.NoSuchElementException;
import java.util.Objects;

public final class AmbiguousReferenceException extends NoSuchElementException
{
	private final String featureName;
	private final String rawReference;
	private final String expectedConceptName;
	private final int matchCount;

	public AmbiguousReferenceException(final String featureName,
									   final String rawReference,
									   final String expectedConceptName,
									   final int matchCount)
	{
		super(buildMessage(featureName, rawReference, expectedConceptName, matchCount));
		this.featureName = Objects.requireNonNull(featureName, "featureName");
		this.rawReference = Objects.requireNonNull(rawReference, "rawReference");
		this.expectedConceptName = expectedConceptName;
		this.matchCount = matchCount;
	}

	public String featureName()
	{
		return featureName;
	}

	public String rawReference()
	{
		return rawReference;
	}

	public String expectedConceptName()
	{
		return expectedConceptName;
	}

	public int matchCount()
	{
		return matchCount;
	}

	private static String buildMessage(final String featureName,
									   final String rawReference,
									   final String expectedConceptName,
									   final int matchCount)
	{
		final var safeFeature = featureName == null || featureName.isBlank() ? "unknown" : featureName;
		final var safeRef = rawReference == null ? "" : rawReference;
		final var safeExpected = expectedConceptName == null || expectedConceptName.isBlank() ? null : expectedConceptName;
		final var count = Math.max(0, matchCount);

		if (safeExpected == null)
		{
			return "Ambiguous reference '" + safeRef + "' for relation '" + safeFeature + "' (" + count + " matches)";
		}
		return "Ambiguous reference '" + safeRef + "' for relation '" + safeFeature + "' (expected " + safeExpected + "; " + count + " matches)";
	}
}

