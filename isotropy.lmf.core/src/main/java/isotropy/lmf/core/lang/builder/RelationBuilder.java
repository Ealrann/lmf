package isotropy.lmf.core.lang.builder;

import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.lang.impl.RelationImpl;
import isotropy.lmf.core.model.FeatureInserter;
import isotropy.lmf.core.model.RelationLazyInserter;

import java.util.function.Supplier;

public final class RelationBuilder<UnaryType extends LMObject, EffectiveType> implements Relation.Builder<UnaryType,
		EffectiveType>
{
	public static final FeatureInserter<RelationBuilder<?, ?>> FEATURE_INSERTER =
			new FeatureInserter.Builder<RelationBuilder<?, ?>>()

			.add(LMCoreDefinition.Features.RELATION.name, RelationBuilder::name)
			.add(LMCoreDefinition.Features.RELATION.immutable, RelationBuilder::immutable)
			.add(LMCoreDefinition.Features.RELATION.many, RelationBuilder::many)
			.add(LMCoreDefinition.Features.RELATION.mandatory, RelationBuilder::mandatory)
			.add(LMCoreDefinition.Features.RELATION.contains, RelationBuilder::contains)
			.build();

	private static final RelationLazyInserter<RelationBuilder<?, ?>> BUILDER_INSERTER =
			new RelationLazyInserter.Builder<RelationBuilder<?, ?>>()

			.add(LMCoreDefinition.Features.RELATION.group, RelationBuilder::_group)
			.add(LMCoreDefinition.Features.RELATION.parameter, RelationBuilder::parameter)
			.build();

	private String name = null;
	private boolean many;
	private boolean immutable;
	private boolean mandatory;
	private boolean contains;

	private Supplier<Group<UnaryType>> group = null;
	private Supplier<Generic> parameter;

	@Override
	public Relation<UnaryType, EffectiveType> build()
	{
		return new RelationImpl<>(name, many, immutable, mandatory, group, contains, parameter.get());
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
	public RelationBuilder<UnaryType, EffectiveType> group(Supplier<Group<UnaryType>> group)
	{
		this.group = group;
		return this;
	}

	@SuppressWarnings("unchecked")
	public RelationBuilder<UnaryType, EffectiveType> _group(Supplier<? extends Group<?>> group)
	{
		this.group = (Supplier<Group<UnaryType>>) group;
		return this;
	}

	@Override
	public RelationBuilder<UnaryType, EffectiveType> contains(final boolean contains)
	{
		this.contains = contains;
		return this;
	}

	@Override
	public RelationBuilder<UnaryType, EffectiveType> parameter(final Supplier<Generic> parameter)
	{
		this.parameter = parameter;
		return this;
	}

	@Override
	public <AttributeType> void push(final Attribute<AttributeType, ?> feature, final AttributeType value)
	{
		FEATURE_INSERTER.push(this, feature, value);
	}

	@Override
	public <RelationType extends LMObject> void push(final Relation<RelationType, ?> relation,
													 final Supplier<RelationType> supplier)
	{
		BUILDER_INSERTER.push(this, relation, supplier);
	}
}
