package isotropy.lmf.core.resource.transform.word.resolver;

import isotropy.lmf.core.lang.Attribute;
import isotropy.lmf.core.model.IFeaturedObject;
import isotropy.lmf.core.resource.transform.word.IFeatureResolution;

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
}
