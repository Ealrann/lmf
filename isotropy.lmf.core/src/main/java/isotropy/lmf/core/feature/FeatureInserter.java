package isotropy.lmf.core.feature;

import isotropy.lmf.core.api.feature.RawFeature;

import java.util.function.BiConsumer;

public final class FeatureInserter<T>
{
	private final FeatureMap<BiConsumer<T, ?>> featureMap;

	public FeatureInserter(final FeatureMap<BiConsumer<T, ?>> featureMap)
	{
		this.featureMap = featureMap;
	}

	@SuppressWarnings("unchecked")
	public <F> void push(final T object, final RawFeature<F, ?> feature, final F value)
	{
		((BiConsumer<T, F>) featureMap.getTuple(feature)
									  .value()).accept(object, value);
	}

	public static <T> Builder<T> Builder()
	{
		return new Builder<>();
	}

	public static final class Builder<T>
	{
		private final FeatureMap.Builder<BiConsumer<T, ?>> builder = new FeatureMap.Builder<>();

		public FeatureInserter<T> build()
		{
			return new FeatureInserter<>(builder.build());
		}

		public <F> Builder<T> add(final RawFeature<F, ?> feature, final BiConsumer<T, ? extends F> function)
		{
			builder.add(feature, function);
			return this;
		}
	}
}
