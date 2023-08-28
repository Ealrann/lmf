package isotropy.lmf.core.lang.impl;

import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.model.FeatureGetter;
import isotropy.lmf.core.model.FeaturedObject;

public final class GenericImpl<T> extends FeaturedObject implements Generic<T>
{
	public static final FeatureGetter<Generic<?>> GET_MAP = new FeatureGetter.Builder<Generic<?>>()

			.add(Features.name, Named::name)
			.add(Features.boundType, Generic::boundType)
			.add(Features.type, Generic::type)
			.build();

	private final String name;
	private final BoundType boundType;
	private final Type<T> type;

	public GenericImpl(final String name, final BoundType boundType, final Type<T> type)
	{
		this.name = name;
		this.boundType = boundType;
		this.type = type;

		ContainmentUtils.setContainer(this, type, Features.type);
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
	public Type<T> type()
	{
		return type;
	}

	@Override
	public <T> T get(final Feature<?, T> feature)
	{
		return GET_MAP.get(this, feature.rawFeature());
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
}
