package org.logoce.lmf.model.feature;

import org.logoce.lmf.model.api.feature.RawFeature;

import java.util.function.Function;

public final class FeatureGetter<T>
{
	private final FeatureMap<Function<T, ?>> featureMap;

	public FeatureGetter(final FeatureMap<Function<T, ?>> featureMap)
	{
		this.featureMap = featureMap;
	}

	@SuppressWarnings("unchecked")
	public <F> F get(final T object, final RawFeature<?, F> feature)
	{
		return (F) featureMap.getTuple(feature)
							 .value()
							 .apply(object);
	}

	public static final class Builder<T>
	{
		private final FeatureMap.Builder<Function<T, ?>> builder = new FeatureMap.Builder<>();

		public FeatureGetter<T> build()
		{
			return new FeatureGetter<>(builder.build());
		}

		public <F> Builder<T> add(final RawFeature<?, F> feature, final Function<T, ? extends F> function)
		{
			builder.add(feature, function);
			return this;
		}
	}
}
