package isotropy.lmf.core.lang.impl;

import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.model.FeatureMap;

import java.util.List;
import java.util.function.Function;

public final class RelationImpl<UnaryType extends LMObject, EffectiveType> implements Relation<UnaryType, EffectiveType>
{
	public static final FeatureMap<Function<Relation<?, ?>, Object>> GET_MAP = new FeatureMap<>(

			List.of(new FeatureMap.FeatureTuple<>(Relation.Features.name, Named::name),
					new FeatureMap.FeatureTuple<>(Relation.Features.immutable, Relation::immutable),
					new FeatureMap.FeatureTuple<>(Relation.Features.many, Relation::many),
					new FeatureMap.FeatureTuple<>(Relation.Features.mandatory, Relation::mandatory),
					new FeatureMap.FeatureTuple<>(Relation.Features.group, Relation::group),
					new FeatureMap.FeatureTuple<>(Relation.Features.contains, Relation::contains),
					new FeatureMap.FeatureTuple<>(Relation.Features.parameter, Relation::parameter)));

	private final String name;
	private final boolean immutable;
	private final boolean many;
	private final boolean mandatory;
	private final Group<UnaryType> group;
	private final boolean contains;
	private final Generic parameter;

	private LMObject container;

	public RelationImpl(final String name,
						final boolean immutable,
						final boolean many,
						final boolean mandatory,
						final Group<UnaryType> group,
						final boolean contains,
						final Generic parameter)
	{
		this.name = name;
		this.immutable = immutable;
		this.many = many;
		this.mandatory = mandatory;
		this.group = group;
		this.contains = contains;
		this.parameter = parameter;
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
	public Group<UnaryType> group()
	{
		return group;
	}

	@Override
	public boolean contains()
	{
		return contains;
	}

	@Override
	public Generic parameter()
	{
		return parameter;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(final Feature<?, T> feature)
	{
		return (T) GET_MAP.get(feature).apply(this);
	}

	@Override
	public <T> void set(final Feature<?, T> feature, final T value)
	{
		throw new IllegalAccessError("Group " + Generic.class.getSimpleName() + " is immutable.");
	}

	@Override
	public Group<?> lmGroup()
	{
		return Relation.GROUP;
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
