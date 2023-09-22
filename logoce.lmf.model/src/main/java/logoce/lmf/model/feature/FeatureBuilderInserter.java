package logoce.lmf.model.feature;

import logoce.lmf.model.api.feature.RawFeature;
import logoce.lmf.model.lang.LMObject;
import logoce.lmf.model.api.model.IFeaturedObject;

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
													   final RawFeature<RelationType, ?> feature,
													   final IFeaturedObject.Builder<RelationType> builder)
	{
		final var value = (BiConsumer<T, IFeaturedObject.Builder<RelationType>>) featureMap.getTuple(feature).value();
		value.accept(owner, builder);
	}

	public static <T extends LMObject> Builder<T> Builder()
	{
		return new Builder<>();
	}

	public static final class Builder<T>
	{
		private final FeatureMap.Builder<BiConsumer<T, ? extends IFeaturedObject.Builder<?>>> builder = new FeatureMap.Builder<>();

		public FeatureBuilderInserter<T> build()
		{
			return new FeatureBuilderInserter<>(builder.build());
		}

		public <F extends LMObject> Builder<T> add(final RawFeature<F, ?> feature,
												   final BiConsumer<T, IFeaturedObject.Builder<F>> function)
		{
			builder.add(feature, function);
			return this;
		}
	}
}
