package isotropy.lmf.core.lang.impl;

import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.model.FeatureGetter;
import isotropy.lmf.core.model.FeatureSetter;
import isotropy.lmf.core.model.FeaturedObject;
import isotropy.lmf.core.model.RawFeature;

public final class RelationImpl<UnaryType extends LMObject, EffectiveType> extends FeaturedObject implements Relation<UnaryType, EffectiveType>
{
	public static final FeatureGetter<Relation<?, ?>> GET_MAP = new FeatureGetter.Builder<Relation<?, ?>>()

			.add(Features.name, Relation::name)
			.add(Features.immutable, Relation::immutable)
			.add(Features.many, Relation::many)
			.add(Features.mandatory, Relation::mandatory)
			.add(Features.reference, Relation::reference)
			.add(Features.contains, Relation::contains)
			.add(Features.lazy, Relation::lazy)
			.build();

	private final String name;
	private final boolean immutable;
	private final boolean many;
	private final boolean mandatory;
	private final Reference<UnaryType> reference;
	private final boolean contains;
	private final boolean lazy;
	private final RawFeature<UnaryType, EffectiveType> rawFeature;

	public RelationImpl(final String name,
						final boolean immutable,
						final boolean many,
						final boolean mandatory,
						final Reference<UnaryType> reference,
						final boolean contains,
						final boolean lazy,
						final RawFeature<UnaryType, EffectiveType> rawFeature)
	{
		this.name = name;
		this.immutable = immutable;
		this.many = many;
		this.mandatory = mandatory;
		this.reference = reference;
		this.contains = contains;
		this.lazy = lazy;
		this.rawFeature = rawFeature;

		setContainer(reference, Features.reference);
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
	public Reference<UnaryType> reference()
	{
		return reference;
	}

	@Override
	public boolean contains()
	{
		return contains;
	}

	@Override
	public boolean lazy()
	{
		return lazy;
	}

	@Override
	public RawFeature<UnaryType, EffectiveType> rawFeature()
	{
		return rawFeature;
	}

	@Override
	protected FeatureGetter<?> getterMap()
	{
		return GET_MAP;
	}

	@Override
	protected FeatureSetter<?> setterMap()
	{
		return null;
	}

	@Override
	public Group<?> lmGroup()
	{
		return LMCoreDefinition.Groups.RELATION;
	}
}
