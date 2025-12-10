package org.logoce.lmf.model.feature;

import java.util.function.Function;

public final class FeatureGetter<T>
{
	private final FeatureMap<Function<T, ?>> featureMap;

	public FeatureGetter(final FeatureMap<Function<T, ?>> featureMap)
	{
		this.featureMap = featureMap;
	}

	@SuppressWarnings("unchecked")
	public <F> F get(final T object, final int featureId)
	{
		return (F) featureMap.get(featureId).apply(object);
	}

	public static final class Builder<T>
	{
		private final FeatureMap.Builder<Function<T, ?>> builder;

		public Builder(int featureCount, final FeatureIndexFunction indexFunction)
		{
			builder = new FeatureMap.Builder<>(featureCount, indexFunction);
		}

		public <F> Builder<T> add(final int featureID, final Function<T, ? extends F> function)
		{
			builder.add(featureID, function);
			return this;
		}

		public FeatureGetter<T> build()
		{
			return new FeatureGetter<>(builder.build());
		}
	}
}
