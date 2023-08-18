package isotropy.lmf.core.resource.transform.word.resolver;

import isotropy.lmf.core.lang.Attribute;
import isotropy.lmf.core.lang.LMCoreDefinition;
import isotropy.lmf.core.lang.Unit;
import isotropy.lmf.core.model.IFeaturedObject;
import isotropy.lmf.core.resource.transform.node.TreeBuilderNode;
import isotropy.lmf.core.resource.transform.word.IFeatureResolution;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
	public Optional<IFeatureResolution> resolve(final TreeBuilderNode<?> node, final String value)
	{
		if (feature.many())
		{
			final var split = value.split(",");
			final var res = Stream.of(split)
								  .map(v -> extractValue(unit, v))
								  .toList();
			return Optional.of(new AttributeListResolution<>(feature, res));
		}
		else
		{
			final var pmatcher = matcherPattern == null ? null : matcherPattern.matcher(value);
			if (pmatcher == null || pmatcher.matches())
			{
				final var extractedValue = extractValue(unit, value);
				return Optional.of(new AttributeResolution<>(feature, extractedValue));
			}
		}
		return Optional.empty();
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

	public static final class AttributeResolution<T> implements IFeatureResolution
	{
		final Attribute<T, ?> attribute;
		final T value;

		private AttributeResolution(final Attribute<T, ?> attribute, final T value)
		{
			this.attribute = attribute;
			this.value = value;
		}

		@Override
		public void pushValue(final IFeaturedObject.Builder<?> builder)
		{
			builder.push(attribute, value);
		}
	}

	public static final class AttributeListResolution<T> implements IFeatureResolution
	{
		final Attribute<T, ?> attribute;
		final List<T> values;

		private AttributeListResolution(final Attribute<T, ?> attribute, final List<T> values)
		{
			this.attribute = attribute;
			this.values = values;
		}

		@Override
		public void pushValue(final IFeaturedObject.Builder<?> builder)
		{
			for (final var value : values)
			{
				builder.push(attribute, value);
			}
		}
	}
}
