package isotropy.lmf.core.lang.impl;

import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.model.FeatureMap;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class GroupReferenceImpl<UnaryType extends LMObject> implements GroupReference<UnaryType>
{
	public static final FeatureMap<Function<GroupReference<?>, Object>> GET_MAP = new FeatureMap<>(

			List.of(new FeatureMap.FeatureTuple<>(LMCoreDefinition.Features.GROUP_REFERENCE.group,
												  GroupReference::group),
					new FeatureMap.FeatureTuple<>(LMCoreDefinition.Features.GROUP_REFERENCE.genericParameter,
												  GroupReference::genericParameter),
					new FeatureMap.FeatureTuple<>(LMCoreDefinition.Features.GROUP_REFERENCE.directParameter,
												  GroupReference::directParameter)));

	private final Supplier<Group<UnaryType>> group;
	private final Supplier<Generic> genericParameter;
	private final Supplier<Group<?>> directParameter;

	private LMObject container;

	public GroupReferenceImpl(final Supplier<Group<UnaryType>> group,
							  final Supplier<Generic> genericParameter,
							  final Supplier<Group<?>> directParameter)
	{
		this.group = group != null ? group : () -> null;
		this.genericParameter = directParameter != null ? genericParameter : () -> null;
		this.directParameter = directParameter != null ? directParameter : () -> null;
	}

	@Override
	public Group<UnaryType> group()
	{
		return group.get();
	}

	@Override
	public Generic genericParameter()
	{
		return genericParameter.get();
	}

	@Override
	public Group<?> directParameter()
	{
		return directParameter.get();
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
		return LMCoreDefinition.Groups.RELATION;
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
