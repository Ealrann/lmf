package org.logoce.lmf.model.resource.linking.feature;

import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.resource.linking.FeatureLink;

public abstract class AttributeResolver<T> extends AbstractResolver<T, Attribute<T, ?>> implements ITokenResolver<T>
{
	public AttributeResolver(final Attribute<T, ?> attribute)
	{
		super(attribute);
	}

	public static final class AttributeLink<T> implements FeatureLink
	{
		final Attribute<T, ?> attribute;
		final T value;

		AttributeLink(final Attribute<T, ?> attribute, final T value)
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
