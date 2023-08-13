package isotropy.lmf.core.lang.impl;

import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.model.FeatureGetter;

import java.util.List;

public final class GroupImpl<T extends LMObject> implements Group<T>
{
	public static final FeatureGetter<Group<?>> GET_MAP = new FeatureGetter.Builder<Group<?>>()

			.add(LMCoreDefinition.Features.Group_name, Group::name)
			.add(LMCoreDefinition.Features.Group_concrete, Group::concrete)
			.add(LMCoreDefinition.Features.Group_includes, Group::includes)
			.add(LMCoreDefinition.Features.Group_features, Group::features)
			.add(LMCoreDefinition.Features.Group_generics, Group::generics)
			.build();

	private final String name;
	private final boolean concrete;
	private final List<? extends Group<?>> includes;
	private final List<? extends Feature<?, ?>> features;
	private final List<Generic> generics;

	private LMObject container;

	public GroupImpl(final String name,
					 final boolean concrete,
					 final List<? extends Group<?>> includes,
					 final List<? extends Feature<?, ?>> features,
					 final List<Generic> generics)
	{
		this.name = name;
		this.concrete = concrete;
		this.includes = includes;
		this.features = features;
		this.generics = generics;
	}

	@Override
	public String name()
	{
		return name;
	}

	@Override
	public boolean concrete()
	{
		return concrete;
	}

	@Override
	public List<? extends Group<?>> includes()
	{
		return includes;
	}

	@Override
	public List<? extends Feature<?, ?>> features()
	{
		return features;
	}

	@Override
	public List<Generic> generics()
	{
		return generics;
	}

	@Override
	public <T> T get(final Feature<?, T> feature)
	{
		return GET_MAP.get(this, feature);
	}

	@Override
	public <T> void set(final Feature<?, T> feature, final T value)
	{
		throw new IllegalAccessError("Group " + Generic.class.getSimpleName() + " is immutable.");
	}

	@Override
	public Group<?> lmGroup()
	{
		return LMCoreDefinition.Groups.GROUP;
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
