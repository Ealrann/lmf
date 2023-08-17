package isotropy.lmf.core.resource.transform.word.resolver;

import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.model.IFeaturedObject;
import isotropy.lmf.core.resource.transform.node.TreeBuilderNode;
import isotropy.lmf.core.resource.transform.word.IFeatureResolution;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class AttributeResolver<T> extends AbstractResolver<T, Attribute<T, ?>> implements IWordResolver<T>
{
	private static final Pattern ROOT_MATCHER = Pattern.compile(LMCoreDefinition.Units.MATCHER.matcher());
	private final Pattern matcherPattern;
	private final Pattern extractorPattern;

	public AttributeResolver(final Attribute<T, ?> attribute)
	{
		super(attribute);
		final var datatype = attribute.datatype();
		matcherPattern = compilePattern(datatype, Unit::matcher);
		extractorPattern = compilePattern(datatype, Unit::extractor);
	}

	private Pattern compilePattern(final Datatype<T> datatype, Function<Unit<?>, String> expressionGetter)
	{
		if (datatype instanceof Unit<T> unit && expressionGetter.apply(unit) != null)
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
		final var datatype = feature.datatype();
		if (datatype instanceof final isotropy.lmf.core.lang.Enum<T> _enum)
		{
			return resolveEnum(value, _enum);
		}
		else if (datatype instanceof final Unit<T> unit)
		{
			return resolveUnit(value, unit);
		}

		return Optional.empty();
	}

	private Optional<IFeatureResolution> resolveUnit(final String value, final Unit<T> unit)
	{
		final var matcher = unit.matcher() != null ? LMCoreDefinition.Units.MATCHER.matcher() : null;
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

	private Optional<IFeatureResolution> resolveEnum(final String value, final isotropy.lmf.core.lang.Enum<T> _enum)
	{
		if (feature.many())
		{
			final var split = value.split(",");
			final var res = Stream.of(split)
								  .map(v -> extractEnumLiteral(v, _enum))
								  .filter(Optional::isPresent)
								  .map(Optional::get)
								  .toList();
			return Optional.of(new AttributeListResolution<>(feature, res));
		}
		else
		{
			final var resolvedEnum = extractEnumLiteral(value, _enum);
			return resolvedEnum.map(enumVal -> new AttributeResolution<>(feature, enumVal));
		}
	}

	private static <T> Optional<T> extractEnumLiteral(final String value, final isotropy.lmf.core.lang.Enum<T> _enum)
	{
		final var lPackage = ((Model) _enum.lContainer()).lPackage();
		final var resolvedEnum = lPackage.resolveEnum(_enum, capitalizeFirstLetter(value));
		return resolvedEnum;
	}

	public static String capitalizeFirstLetter(String str)
	{
		if (str == null || str.isEmpty())
		{
			return str;
		}
		return Character.toUpperCase(str.charAt(0)) + str.substring(1);
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

	@Override
	public boolean isBooleanAttribute()
	{
		return feature.datatype() == LMCoreDefinition.Units.BOOLEAN;
	}
}