package isotropy.lmf.core.resource.transform.feature.resolver;

import isotropy.lmf.core.lang.Attribute;
import isotropy.lmf.core.lang.Model;
import isotropy.lmf.core.lang.Unit;
import isotropy.lmf.core.model.IFeaturedObject;
import isotropy.lmf.core.resource.transform.feature.IFeatureResolution;
import isotropy.lmf.core.resource.transform.util.BuilderNode;
import isotropy.lmf.core.resource.util.Tree;

import java.util.Optional;
import java.util.regex.Pattern;

public final class AttributeResolver<T> extends AbstractResolver<T, Attribute<T, ?>> implements IWordResolver<T>
{
	public AttributeResolver(final Attribute<T, ?> attribute)
	{
		super(attribute);
	}

	@Override
	public Optional<AttributeResolution<T>> resolve(final Tree<BuilderNode> tree, final String word)
	{
		final var datatype = feature.datatype();
		if (datatype instanceof isotropy.lmf.core.lang.Enum)
		{
			final var _enum = (isotropy.lmf.core.lang.Enum<T>) datatype;
			final var lPackage = ((Model) _enum.lContainer()).lPackage();
			final var value = lPackage.resolveEnum(_enum, word);
			return value.map(enumVal -> new AttributeResolution<>(feature, enumVal));
		}
		else
		{
			final var unit = (Unit<T>) datatype;
			final var matcher = unit.matcher();
			if (matcher == null || word.matches(matcher))
			{
				final var value = extract(unit, word);
				return Optional.of(new AttributeResolution<T>(feature, value));
			}
		}
		return Optional.empty();
	}

	@SuppressWarnings("unchecked")
	private T extract(final Unit<T> unit, final String word)
	{
		final var extractor = unit.extractor();
		final String extraction;
		if (extractor != null)
		{
			// TODO compile in an Adapter ?
			final var pattern = Pattern.compile(extractor);
			final var extractMatcher = pattern.matcher(word);
			extractMatcher.find();
			extraction = extractMatcher.group();
		}
		else
		{
			extraction = word;
		}

		switch (unit.primitive())
		{
			case Boolean:
				return (T) Boolean.valueOf(extraction);
			case Int:
				return (T) Integer.valueOf(extraction);
			case Long:
				return (T) Long.valueOf(extraction);
			case Float:
				return (T) Float.valueOf(extraction);
			case Double:
				return (T) Double.valueOf(extraction);
			case String:
				return (T) extraction;
		}
		return null;
	}

	private static final class AttributeResolution<T> implements IFeatureResolution
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
}