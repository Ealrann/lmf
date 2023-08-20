package isotropy.lmf.core.resource.transform.word.resolver;

import isotropy.lmf.core.lang.Attribute;
import isotropy.lmf.core.lang.LMCoreDefinition;
import isotropy.lmf.core.model.IFeaturedObject;
import isotropy.lmf.core.resource.transform.word.IFeatureResolution;

import java.util.List;

public abstract class AttributeResolver<T> extends AbstractResolver<T, Attribute<T, ?>> implements IWordResolver<T>
{
	public AttributeResolver(final Attribute<T, ?> attribute)
	{
		super(attribute);
	}

	public static final class AttributeResolution<T> implements IFeatureResolution
	{
		final Attribute<T, ?> attribute;
		final T value;

		AttributeResolution(final Attribute<T, ?> attribute, final T value)
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

		AttributeListResolution(final Attribute<T, ?> attribute, final List<T> values)
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