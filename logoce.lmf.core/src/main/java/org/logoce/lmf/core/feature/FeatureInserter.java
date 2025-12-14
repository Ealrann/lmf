package org.logoce.lmf.core.feature;

import java.util.function.BiConsumer;

public final class FeatureInserter<T>
{
	private final FeatureMap<BiConsumer<T, ?>> featureMap;

	public FeatureInserter(final FeatureMap<BiConsumer<T, ?>> featureMap)
	{
		this.featureMap = featureMap;
	}

	@SuppressWarnings("unchecked")
	public <F> void push(final T object, final int featureId, final F value)
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

		public Builder<T> add(final int featureId, final BiConsumer<T, ?> function)
		{
			builder.add(featureId, function);
			return this;
		}

		public FeatureInserter<T> build()
		{
			return new FeatureInserter<>(builder.build());
		}
	}
}
