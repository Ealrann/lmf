package org.logoce.lmf.model.resource.linking.feature;

import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.LMCoreDefinition;
import org.logoce.lmf.model.lang.Unit;
import org.logoce.lmf.model.resource.linking.FeatureLink;
import org.logoce.lmf.model.resource.linking.tree.ResolvedNode;

import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

public final class UnitResolver<T> extends AttributeResolver<T>
{
	private static final Pattern ROOT_MATCHER = Pattern.compile(LMCoreDefinition.Units.MATCHER.matcher());
	private final Pattern matcherPattern;
	private final Pattern extractorPattern;
	private final Unit<T> unit;

	public UnitResolver(final Attribute<T, ?> attribute)
	{
		super(attribute);
		unit = (Unit<T>) attribute.datatype();
		matcherPattern = compilePattern(Unit::matcher);
		extractorPattern = compilePattern(Unit::extractor);
	}

	private Pattern compilePattern(Function<Unit<?>, String> expressionGetter)
	{
		if (expressionGetter.apply(unit) != null)
		{
			final var matcher = expressionGetter.apply(unit);
			final var extractMatcher = ROOT_MATCHER.matcher(matcher);
			//noinspection ResultOfMethodCallIgnored
			extractMatcher.find();
			final var applicableMatcher = extractMatcher.group(1);
			return Pattern.compile(applicableMatcher);
		}
		return null;
	}

	@Override
	protected Optional<FeatureLink> internalResolve(final ResolvedNode<?, ?> node, final String value)
	{
		final var pmatcher = matcherPattern == null ? null : matcherPattern.matcher(value);
		if (pmatcher == null || pmatcher.matches())
		{
			final var extractedValue = extractValue(unit, value);
			return Optional.of(new AttributeLink<>(feature, extractedValue));
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
