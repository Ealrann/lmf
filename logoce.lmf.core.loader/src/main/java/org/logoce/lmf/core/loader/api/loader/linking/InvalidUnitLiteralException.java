package org.logoce.lmf.core.loader.api.loader.linking;

import org.logoce.lmf.core.lang.Primitive;
import org.logoce.lmf.core.lang.Unit;

import java.util.NoSuchElementException;
import java.util.Objects;

public final class InvalidUnitLiteralException extends NoSuchElementException
{
	private final String featureName;
	private final String literal;
	private final Primitive primitive;

	public InvalidUnitLiteralException(final String featureName,
									   final String literal,
									   final Unit<?> unit)
	{
		this(featureName,
			 literal,
			 unit == null ? null : unit.primitive(),
			 unit == null ? null : unit.defaultValue());
	}

	public InvalidUnitLiteralException(final String featureName,
									   final String literal,
									   final Primitive primitive,
									   final String example)
	{
		super(buildMessage(featureName, literal, primitive, example));
		this.featureName = Objects.requireNonNull(featureName, "featureName");
		this.literal = Objects.requireNonNull(literal, "literal");
		this.primitive = primitive;
	}

	public String featureName()
	{
		return featureName;
	}

	public String literal()
	{
		return literal;
	}

	public Primitive primitive()
	{
		return primitive;
	}

	private static String buildMessage(final String featureName,
									   final String literal,
									   final Primitive primitive,
									   final String example)
	{
		final var safeFeature = featureName == null ? "unknown" : featureName;
		final var safeLiteral = literal == null ? "" : literal;

		if (primitive == Primitive.Float)
		{
			if (safeLiteral.endsWith("f") || safeLiteral.endsWith("F"))
			{
				return "Invalid float literal '" + safeLiteral + "' for feature '" + safeFeature + "'";
			}
			return "Invalid float literal '" + safeLiteral + "' for feature '" + safeFeature + "'; use e.g. '" + safeLiteral + "f'";
		}

		if (primitive == Primitive.Long)
		{
			if (safeLiteral.endsWith("l") || safeLiteral.endsWith("L"))
			{
				return "Invalid long literal '" + safeLiteral + "' for feature '" + safeFeature + "'";
			}
			return "Invalid long literal '" + safeLiteral + "' for feature '" + safeFeature + "'; use e.g. '" + safeLiteral + "L'";
		}

		if (primitive == Primitive.Double)
		{
			return "Invalid double literal '" + safeLiteral + "' for feature '" + safeFeature + "'";
		}

		if (primitive == Primitive.Int)
		{
			return "Invalid int literal '" + safeLiteral + "' for feature '" + safeFeature + "'";
		}

		if (primitive == Primitive.Boolean)
		{
			return "Invalid boolean literal '" + safeLiteral + "' for feature '" + safeFeature + "'";
		}

		if (primitive == Primitive.String)
		{
			return "Invalid string literal '" + safeLiteral + "' for feature '" + safeFeature + "'";
		}

		if (example != null && !example.isBlank())
		{
			return "Invalid literal '" + safeLiteral + "' for feature '" + safeFeature + "'; expected a value like '" + example + "'";
		}

		return "Invalid literal '" + safeLiteral + "' for feature '" + safeFeature + "'";
	}
}

