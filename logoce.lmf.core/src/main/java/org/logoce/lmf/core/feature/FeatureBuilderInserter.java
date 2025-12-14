package org.logoce.lmf.core.feature;

import org.logoce.lmf.core.api.model.IFeaturedObject;
import org.logoce.lmf.core.lang.LMObject;

import java.util.function.BiConsumer;

public final class FeatureBuilderInserter<T>
{
	private final FeatureMap<BiConsumer<T, ? extends IFeaturedObject.Builder<?>>> featureMap;

	public FeatureBuilderInserter(final FeatureMap<BiConsumer<T, ? extends IFeaturedObject.Builder<?>>> featureMap)
	{
		this.featureMap = featureMap;
	}

	@SuppressWarnings("unchecked")
	public <RelationType extends LMObject> void accept(final T owner,
													   final int featureId,
													   final IFeaturedObject.Builder<RelationType> builder)
	{
		final var value = (BiConsumer<T, IFeaturedObject.Builder<RelationType>>) featureMap.get(featureId);
		value.accept(owner, builder);
	}

	public static final class Builder<T>
	{
		private final FeatureMap.Builder<BiConsumer<T, ? extends IFeaturedObject.Builder<?>>> builder;

		public Builder(final int featureCount, final FeatureIndexFunction indexFunction)
		{
			builder = new FeatureMap.Builder<>(featureCount, indexFunction);
		}

		public <F extends LMObject> Builder<T> add(final int featureId,
												   final BiConsumer<T, IFeaturedObject.Builder<F>> function)
		{
			builder.add(featureId, function);
			return this;
		}

		public FeatureBuilderInserter<T> build()
		{
			return new FeatureBuilderInserter<>(builder.build());
		}
	}
}
