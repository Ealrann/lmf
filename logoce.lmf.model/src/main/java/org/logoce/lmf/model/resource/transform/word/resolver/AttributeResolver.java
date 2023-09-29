package org.logoce.lmf.model.resource.transform.word.resolver;

import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.resource.transform.word.IFeatureResolution;

public abstract class AttributeResolver<T> extends AbstractResolver<T, Attribute<T, ?>> implements ITokenResolver<T>
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
