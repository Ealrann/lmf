package org.logoce.lmf.model.loader.linking.feature;

import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.loader.linking.FeatureResolution;

import java.util.List;
import java.util.Optional;

public abstract class AttributeResolver extends AbstractResolver<Attribute<?, ?>>
{
	protected AttributeResolver(final Attribute<?, ?> attribute)
	{
		super(attribute);
	}

	public final Optional<FeatureResolution<Attribute<?, ?>>> resolve(final List<String> values)
	{
		return super.resolve(values, this::internalResolve);
	}

	protected abstract Optional<FeatureResolution<Attribute<?, ?>>> internalResolve(String value);

	public static final class AttributeResolution<Y> implements FeatureResolution<Attribute<?, ?>>
	{
		private final Attribute<Y, ?> attribute;
		private final Y value;

		public AttributeResolution(final Attribute<Y, ?> attribute, final Y value)
		{
			this.attribute = attribute;
			this.value = value;
		}

		@Override
		public void pushValue(final IFeaturedObject.Builder<?> builder)
		{
			builder.push(attribute, value);
		}

		@Override
		public Attribute<Y, ?> feature()
		{
			return attribute;
		}

		public String value()
		{
			return String.valueOf(value);
		}
	}
}

