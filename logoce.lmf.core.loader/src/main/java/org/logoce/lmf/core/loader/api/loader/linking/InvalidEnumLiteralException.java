package org.logoce.lmf.core.loader.api.loader.linking;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.StringJoiner;

public final class InvalidEnumLiteralException extends NoSuchElementException
{
	private final String featureName;
	private final String literal;
	private final List<String> expectedLiterals;

	public InvalidEnumLiteralException(final String featureName,
									   final String literal,
									   final List<String> expectedLiterals)
	{
		super(buildMessage(featureName, literal, expectedLiterals));
		this.featureName = Objects.requireNonNull(featureName, "featureName");
		this.literal = Objects.requireNonNull(literal, "literal");
		this.expectedLiterals = expectedLiterals == null ? List.of() : List.copyOf(expectedLiterals);
	}

	public String featureName()
	{
		return featureName;
	}

	public String literal()
	{
		return literal;
	}

	public List<String> expectedLiterals()
	{
		return expectedLiterals;
	}

	private static String buildMessage(final String featureName,
									   final String literal,
									   final List<String> expectedLiterals)
	{
		final var safeFeature = featureName == null ? "unknown" : featureName;
		final var safeLiteral = literal == null ? "" : literal;
		final var expected = expectedLiterals == null ? List.<String>of() : expectedLiterals;
		if (expected.isEmpty())
		{
			return "Invalid enum literal '" + safeLiteral + "' for feature '" + safeFeature + "'";
		}

		final var joiner = new StringJoiner(", ");
		for (final var candidate : expected)
		{
			if (candidate != null && !candidate.isBlank())
			{
				joiner.add(candidate);
			}
		}
		final var expectedText = joiner.toString();
		if (expectedText.isBlank())
		{
			return "Invalid enum literal '" + safeLiteral + "' for feature '" + safeFeature + "'";
		}
		return "Invalid enum literal '" + safeLiteral + "' for feature '" + safeFeature + "'; expected one of: " + expectedText;
	}
}

