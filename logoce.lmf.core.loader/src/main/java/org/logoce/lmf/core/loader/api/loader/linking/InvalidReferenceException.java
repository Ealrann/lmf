package org.logoce.lmf.core.loader.api.loader.linking;

import java.util.NoSuchElementException;
import java.util.Objects;

public final class InvalidReferenceException extends NoSuchElementException
{
	private final String featureName;
	private final String rawReference;
	private final String expectedConceptName;
	private final String actualConceptName;

	public InvalidReferenceException(final String featureName,
									 final String rawReference,
									 final String expectedConceptName)
	{
		this(featureName, rawReference, expectedConceptName, null);
	}

	public InvalidReferenceException(final String featureName,
									 final String rawReference,
									 final String expectedConceptName,
									 final String actualConceptName)
	{
		super(buildMessage(featureName, rawReference, expectedConceptName, actualConceptName));
		this.featureName = Objects.requireNonNull(featureName, "featureName");
		this.rawReference = Objects.requireNonNull(rawReference, "rawReference");
		this.expectedConceptName = expectedConceptName;
		this.actualConceptName = actualConceptName;
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

	public String actualConceptName()
	{
		return actualConceptName;
	}

	private static String buildMessage(final String featureName,
									   final String rawReference,
									   final String expectedConceptName,
									   final String actualConceptName)
	{
		final var safeFeature = featureName == null || featureName.isBlank() ? "unknown" : featureName;
		final var safeRef = rawReference == null ? "" : rawReference;
		final var safeExpected = expectedConceptName == null || expectedConceptName.isBlank() ? null : expectedConceptName;
		final var safeActual = actualConceptName == null || actualConceptName.isBlank() ? null : actualConceptName;

		if (safeExpected == null)
		{
			return "Cannot resolve reference '" + safeRef + "' for relation '" + safeFeature + "'";
		}
		if (safeActual == null)
		{
			return "Cannot resolve reference '" + safeRef + "' for relation '" + safeFeature + "' (expected " + safeExpected + ")";
		}
		return "Cannot resolve reference '" + safeRef + "' for relation '" + safeFeature + "' (expected " + safeExpected + " but found " + safeActual + ")";
	}
}

