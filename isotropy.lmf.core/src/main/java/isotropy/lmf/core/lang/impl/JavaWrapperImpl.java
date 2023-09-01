package isotropy.lmf.core.lang.impl;

import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.model.FeatureMap;
import isotropy.lmf.core.model.FeaturedObject;

import java.util.List;
import java.util.function.Function;

public final class JavaWrapperImpl<T> extends FeaturedObject implements JavaWrapper<T>
{
	public static final FeatureMap<Function<JavaWrapper<?>, Object>> GET_MAP = new FeatureMap<>(

			List.of(new FeatureMap.FeatureTuple<>(Features.name, JavaWrapper::name),
					new FeatureMap.FeatureTuple<>(Features.domain, JavaWrapper::domain)));

	private final String name;
	private final String domain;

	public JavaWrapperImpl(final String name, final String domain)
	{
		this.name = name;
		this.domain = domain;
	}

	@Override
	public String name()
	{
		return name;
	}

	@Override
	public String domain()
	{
		return domain;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(final Feature<?, T> feature)
	{
		return (T) GET_MAP.get(feature.rawFeature())
						  .apply(this);
	}

	@Override
	public <T> void set(final Feature<?, T> feature, final T value)
	{
		throw new IllegalAccessError("Group " + Generic.class.getSimpleName() + " is immutable.");
	}

	@Override
	public Group<?> lmGroup()
	{
		return LMCoreDefinition.Groups.JAVA_WRAPPER;
	}
}
