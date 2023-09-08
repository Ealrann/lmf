package isotropy.lmf.core.lang.builder;

import isotropy.lmf.core.lang.Attribute;
import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.lang.Reference;
import isotropy.lmf.core.lang.Relation;
import isotropy.lmf.core.lang.impl.RelationImpl;
import isotropy.lmf.core.model.FeatureInserter;
import isotropy.lmf.core.model.RawFeature;
import isotropy.lmf.core.model.RelationLazyInserter;

import java.util.function.Supplier;

public final class RelationBuilder<UnaryType extends LMObject, EffectiveType> implements Relation.Builder<UnaryType, EffectiveType>
{
	public static final FeatureInserter<RelationBuilder<?, ?>> FEATURE_INSERTER = new FeatureInserter.Builder<RelationBuilder<?, ?>>()

			.add(Relation.Features.name, RelationBuilder::name)
			.add(Relation.Features.immutable, RelationBuilder::immutable)
			.add(Relation.Features.many, RelationBuilder::many)
			.add(Relation.Features.mandatory, RelationBuilder::mandatory)
			.add(Relation.Features.contains, RelationBuilder::contains)
			.add(Relation.Features.lazy, RelationBuilder::lazy)
			.build();

	private static final RelationLazyInserter<RelationBuilder<?, ?>> BUILDER_INSERTER = new RelationLazyInserter.Builder<RelationBuilder<?, ?>>()

			.add(Relation.Features.reference, RelationBuilder::_reference)
			.build();

	private String name = null;
	private boolean many;
	private boolean immutable;
	private boolean mandatory;
	private boolean contains;
	private boolean lazy;
	private RawFeature<UnaryType, EffectiveType> rawFeature;

	private Supplier<Reference<UnaryType>> reference = () -> null;

	@Override
	public Relation<UnaryType, EffectiveType> build()
	{
		return new RelationImpl<>(name, immutable, many, mandatory, reference.get(), contains, lazy, rawFeature);
	}

	@Override
	public RelationBuilder<UnaryType, EffectiveType> name(final String name)
	{
		this.name = name;
		return this;
	}

	@Override
	public RelationBuilder<UnaryType, EffectiveType> many(final boolean many)
	{
		this.many = many;
		return this;
	}

	@Override
	public RelationBuilder<UnaryType, EffectiveType> immutable(final boolean immutable)
	{
		this.immutable = immutable;
		return this;
	}

	@Override
	public RelationBuilder<UnaryType, EffectiveType> mandatory(final boolean mandatory)
	{
		this.mandatory = mandatory;
		return this;
	}

	@Override
	public RelationBuilder<UnaryType, EffectiveType> reference(Supplier<Reference<UnaryType>> reference)
	{
		this.reference = reference;
		return this;
	}

	@SuppressWarnings("unchecked")
	public RelationBuilder<UnaryType, EffectiveType> _reference(Supplier<? extends Reference<?>> reference)
	{
		this.reference = (Supplier<Reference<UnaryType>>) reference;
		return this;
	}

	@Override
	public RelationBuilder<UnaryType, EffectiveType> rawFeature(RawFeature<UnaryType, EffectiveType> rawFeature)
	{
		this.rawFeature = rawFeature;
		return this;
	}

	@Override
	public RelationBuilder<UnaryType, EffectiveType> contains(final boolean contains)
	{
		this.contains = contains;
		return this;
	}

	@Override
	public RelationBuilder<UnaryType, EffectiveType> lazy(final boolean lazy)
	{
		this.lazy = lazy;
		return this;
	}

	@Override
	public <AttributeType> void push(final Attribute<AttributeType, ?> feature, final AttributeType value)
	{
		FEATURE_INSERTER.push(this, feature.rawFeature(), value);
	}

	@Override
	public <RelationType extends LMObject> void push(final Relation<RelationType, ?> relation,
													 final Supplier<RelationType> supplier)
	{
		BUILDER_INSERTER.push(this, relation.rawFeature(), supplier);
	}
}
