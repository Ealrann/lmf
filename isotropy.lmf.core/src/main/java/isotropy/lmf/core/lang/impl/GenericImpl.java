package isotropy.lmf.core.lang.impl;

import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.model.FeatureMap;

import java.util.List;
import java.util.function.Function;

public final class GenericImpl implements Generic
{
	public static final FeatureMap<Function<Generic, Object>> GET_MAP = new FeatureMap<>(

			List.of(new FeatureMap.FeatureTuple<>(LMCoreDefinition.Features.GENERIC.name, Named::name),
					new FeatureMap.FeatureTuple<>(LMCoreDefinition.Features.GENERIC.boundType, Generic::boundType),
					new FeatureMap.FeatureTuple<>(LMCoreDefinition.Features.GENERIC.type, Generic::type)));

	private final String name;
	private final BoundType boundType;
	private final Type type;

	private LMObject container;

	public GenericImpl(final String name, final BoundType boundType, final Type type)
	{
		this.name = name;
		this.boundType = boundType;
		this.type = type;
	}

	@Override
	public String name()
	{
		return name;
	}

	@Override
	public BoundType boundType()
	{
		return boundType;
	}

	@Override
	public Type type()
	{
		return type;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(final Feature<?, T> feature)
	{
		return (T) GET_MAP.get(feature)
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
		return LMCoreDefinition.Groups.GENERIC;
	}

	@Override
	public LMObject lContainer()
	{
		return container;
	}

	@Override
	public void lContainer(final LMObject container)
	{
		this.container = container;
	}
}
