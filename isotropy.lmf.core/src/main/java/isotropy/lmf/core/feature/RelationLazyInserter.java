package isotropy.lmf.core.feature;

import isotropy.lmf.core.api.feature.RawFeature;
import isotropy.lmf.core.lang.LMObject;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class RelationLazyInserter<T>
{
	private final FeatureMap<BiConsumer<T, ? extends Supplier<? extends LMObject>>> featureMap;

	public RelationLazyInserter(final FeatureMap<BiConsumer<T, ? extends Supplier<? extends LMObject>>> featureMap)
	{
		this.featureMap = featureMap;
	}

	@SuppressWarnings("unchecked")
	public <F extends LMObject> void push(final T object, final RawFeature<F, ?> relation, final Supplier<F> value)
	{
		((BiConsumer<T, Supplier<F>>) featureMap.getTuple(relation).value()).accept(object, value);
	}

	public static <T> Builder<T> Builder()
	{
		return new Builder<>();
	}

	public static final class Builder<T>
	{
		private final FeatureMap.Builder<BiConsumer<T, ? extends Supplier<? extends LMObject>>> builder = new FeatureMap.Builder<>();

		public RelationLazyInserter<T> build()
		{
			return new RelationLazyInserter<>(builder.build());
		}

		public <F extends LMObject> Builder<T> add(final RawFeature<F, ?> relation,
												   final BiConsumer<T, Supplier<F>> function)
		{
			builder.add(relation, function);
			return this;
		}
	}
}
