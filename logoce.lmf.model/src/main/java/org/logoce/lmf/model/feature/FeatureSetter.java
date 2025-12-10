package org.logoce.lmf.model.feature;

import java.util.function.BiConsumer;

public final class FeatureSetter<T>
{
	private final FeatureMap<BiConsumer<T, ?>> featureMap;

	public FeatureSetter(final FeatureMap<BiConsumer<T, ?>> featureMap)
	{
		this.featureMap = featureMap;
	}

	@SuppressWarnings("unchecked")
	public <F> void set(final T object, final int featureId, final F value)
	{
		((BiConsumer<T, F>) featureMap.get(featureId)).accept(object, value);
	}

	public static final class Builder<T>
	{
		private final FeatureMap.Builder<BiConsumer<T, ?>> builder;

		public Builder(final int featureCount, final FeatureIndexFunction indexFunction)
		{
			builder = new FeatureMap.Builder<>(featureCount, indexFunction);
		}

		public <F> Builder<T> add(final int featureId, final BiConsumer<T, F> function)
		{
			builder.add(featureId, function);
			return this;
		}

		public FeatureSetter<T> build()
		{
			return new FeatureSetter<>(builder.build());
		}
	}
}
