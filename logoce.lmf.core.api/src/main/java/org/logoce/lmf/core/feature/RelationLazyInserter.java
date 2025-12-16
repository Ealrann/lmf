package org.logoce.lmf.core.feature;

import org.logoce.lmf.core.lang.LMObject;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class RelationLazyInserter<T>
{
	private final FeatureMap<BiConsumer<T, Supplier>> featureMap;

	public RelationLazyInserter(final FeatureMap<BiConsumer<T, Supplier>> featureMap)
	{
		this.featureMap = featureMap;
	}

	@SuppressWarnings("unchecked")
	public void push(final T object, final int featureId, final Supplier<? extends LMObject> value)
	{
		((BiConsumer<T, Supplier>) featureMap.get(featureId)).accept(object, (Supplier) value);
	}

	public static final class Builder<T>
	{
		private final FeatureMap.Builder<BiConsumer<T, Supplier>> builder;

		public Builder(final int featureCount, final FeatureIndexFunction indexFunction)
		{
			builder = new FeatureMap.Builder<>(featureCount, indexFunction);
		}

		public Builder<T> add(final int featureId, final BiConsumer<T, Supplier> function)
		{
			builder.add(featureId, function);
			return this;
		}
		
		public RelationLazyInserter<T> build()
		{
			return new RelationLazyInserter<>(builder.build());
		}
	}
}
