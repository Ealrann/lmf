package isotropy.lmf.core.lang.impl;

import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.model.FeatureGetter;
import isotropy.lmf.core.model.FeatureSetter;
import isotropy.lmf.core.model.FeaturedObject;
import isotropy.lmf.core.model.RawFeature;

public final class RelationImpl<UnaryType extends LMObject, EffectiveType> extends FeaturedObject implements Relation<UnaryType, EffectiveType>
{
	private static final FeatureGetter<Relation<?, ?>> GET_MAP = new FeatureGetter.Builder<Relation<?, ?>>().add(
																													Features.name,
																													Relation::name)
																											.add(Features.immutable,
																												 Relation::immutable)
																											.add(Features.many,
																												 Relation::many)
																											.add(Features.mandatory,
																												 Relation::mandatory)
																											.add(Features.rawFeature,
																												 Relation::rawFeature)
																											.add(Features.reference,
																												 Relation::reference)
																											.add(Features.lazy,
																												 Relation::lazy)
																											.add(Features.contains,
																												 Relation::contains)
																											.build();

	private static final FeatureSetter<Relation<?, ?>> SET_MAP = new FeatureSetter.Builder<Relation<?, ?>>().build();

	private final String name;

	private final boolean immutable;

	private final boolean many;

	private final boolean mandatory;

	private final RawFeature<UnaryType, EffectiveType> rawFeature;

	private final Reference<UnaryType> reference;

	private final boolean lazy;

	private final boolean contains;

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
		this.rawFeature = rawFeature;
		this.reference = reference;
		this.lazy = lazy;
		this.contains = contains;
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
	public RawFeature<UnaryType, EffectiveType> rawFeature()
	{
		return rawFeature;
	}

	@Override
	public Reference<UnaryType> reference()
	{
		return reference;
	}

	@Override
	public boolean lazy()
	{
		return lazy;
	}

	@Override
	public boolean contains()
	{
		return contains;
	}

	@Override
	public Group<Relation<?, ?>> lmGroup()
	{
		return LMCoreDefinition.Groups.RELATION;
	}

	@Override
	protected FeatureSetter<Relation<?, ?>> setterMap()
	{
		return SET_MAP;
	}

	@Override
	protected FeatureGetter<Relation<?, ?>> getterMap()
	{
		return GET_MAP;
	}
}
