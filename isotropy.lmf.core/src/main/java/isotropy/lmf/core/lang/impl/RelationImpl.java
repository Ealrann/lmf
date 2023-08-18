package isotropy.lmf.core.lang.impl;

import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.model.FeatureMap;

import java.util.List;
import java.util.function.Function;

public final class RelationImpl<UnaryType extends LMObject, EffectiveType> implements Relation<UnaryType, EffectiveType>
{
	public static final FeatureMap<Function<Relation<?, ?>, Object>> GET_MAP = new FeatureMap<>(

			List.of(new FeatureMap.FeatureTuple<>(LMCoreDefinition.Features.RELATION.name, Named::name),
					new FeatureMap.FeatureTuple<>(LMCoreDefinition.Features.RELATION.immutable, Relation::immutable),
					new FeatureMap.FeatureTuple<>(LMCoreDefinition.Features.RELATION.many, Relation::many),
					new FeatureMap.FeatureTuple<>(LMCoreDefinition.Features.RELATION.mandatory, Relation::mandatory),
					new FeatureMap.FeatureTuple<>(LMCoreDefinition.Features.RELATION.groupReference, Relation::groupReference),
					new FeatureMap.FeatureTuple<>(LMCoreDefinition.Features.RELATION.contains, Relation::contains)));

	private final String name;
	private final boolean immutable;
	private final boolean many;
	private final boolean mandatory;
	private final GroupReference<UnaryType> groupReference;
	private final boolean contains;

	private LMObject container;

	public RelationImpl(final String name,
						final boolean immutable,
						final boolean many,
						final boolean mandatory,
						final GroupReference<UnaryType> groupReference,
						final boolean contains)
	{
		this.name = name;
		this.immutable = immutable;
		this.many = many;
		this.mandatory = mandatory;
		this.groupReference = groupReference;
		this.contains = contains;
	}

	@Override
	public String name()
	{
		return name;
	}

	@Override
	public boolean immutable()
	{
		return immutable;
	}

	@Override
	public boolean many()
	{
		return many;
	}

	@Override
	public boolean mandatory()
	{
		return mandatory;
	}

	@Override
	public GroupReference<UnaryType> groupReference()
	{
		return groupReference;
	}

	@Override
	public boolean contains()
	{
		return contains;
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
