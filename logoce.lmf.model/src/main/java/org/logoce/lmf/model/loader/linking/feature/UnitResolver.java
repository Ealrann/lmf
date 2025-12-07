package org.logoce.lmf.model.loader.linking.feature;

import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.LMCoreModelDefinition;
import org.logoce.lmf.model.lang.Unit;
import org.logoce.lmf.model.loader.linking.FeatureResolution;

import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

public final class UnitResolver<T> extends AttributeResolver
{
	private static final Pattern ROOT_MATCHER = Pattern.compile(LMCoreModelDefinition.Units.MATCHER.matcher());

	private final Pattern matcherPattern;
	private final Pattern extractorPattern;
	private final Unit<T> unit;

	@SuppressWarnings("unchecked")
	public UnitResolver(final Attribute<?, ?> attribute)
	{
		super(attribute);
		unit = (Unit<T>) attribute.datatype();
		matcherPattern = compilePattern(Unit::matcher);
		extractorPattern = compilePattern(Unit::extractor);
	}

	private Pattern compilePattern(final Function<Unit<?>, String> expressionGetter)
	{
		final var expression = expressionGetter.apply(unit);
		if (expression != null)
		{
			final var extractMatcher = ROOT_MATCHER.matcher(expression);
			//noinspection ResultOfMethodCallIgnored
			extractMatcher.find();
			final var applicableMatcher = extractMatcher.group(1);
			return Pattern.compile(applicableMatcher);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Optional<FeatureResolution<Attribute<?, ?>>> internalResolve(final String value)
	{
		final var pmatcher = matcherPattern == null ? null : matcherPattern.matcher(value);
		if (pmatcher == null || pmatcher.matches())
		{
			final var extractedValue = extractValue(unit, value);
			return Optional.of(new AttributeResolution<>((Attribute<T, ?>) feature, extractedValue));
		}
		else
		{
			return Optional.empty();
		}
	}

	@SuppressWarnings("unchecked")
	private T extractValue(final Unit<T> unit, final String word)
	{
		final var extractor = unit.extractor();
		final String extraction;
		if (extractor != null)
		{
			final var extractMatcher = extractorPattern.matcher(word);
			//noinspection ResultOfMethodCallIgnored
			extractMatcher.find();
			extraction = extractMatcher.group(1);
		}
		else
		{
			extraction = word;
		}

		return switch (unit.primitive())
		{
			case Boolean -> (T) Boolean.valueOf(extraction);
			case Int -> (T) Integer.valueOf(extraction);
			case Long -> (T) Long.valueOf(extraction);
			case Float -> (T) Float.valueOf(extraction);
			case Double -> (T) Double.valueOf(extraction);
			case String -> (T) extraction;
		};
	}
}

