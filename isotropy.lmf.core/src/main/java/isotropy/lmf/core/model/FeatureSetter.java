package isotropy.lmf.core.model;

import isotropy.lmf.core.lang.Feature;

import java.util.function.BiConsumer;

public final class FeatureSetter<T>
{
	private final FeatureMap<BiConsumer<T, ?>> featureMap;

	public FeatureSetter(final FeatureMap<BiConsumer<T, ?>> featureMap)
	{
		this.featureMap = featureMap;
	}

	@SuppressWarnings("unchecked")
	public <F> void set(final T object, final Feature<?, F> feature, final F value)
	{
		((BiConsumer<T, F>) featureMap.getTuple(feature).value).accept(object, value);
	}

	public static <T> Builder<T> Builder()
	{
		return new Builder<>();
	}

	public static final class Builder<T>
	{
		private final FeatureMap.Builder<BiConsumer<T, ?>> builder = new FeatureMap.Builder<>();

		public FeatureSetter<T> build()
		{
			return new FeatureSetter<>(builder.build());
		}

		public <F> Builder<T> add(final Feature<?, F> feature, final BiConsumer<T, F> function)
		{
			builder.add(feature, function);
			return this;
		}
	}
}
