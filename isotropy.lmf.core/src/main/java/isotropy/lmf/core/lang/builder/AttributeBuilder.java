package isotropy.lmf.core.lang.builder;

import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.lang.impl.AttributeImpl;
import isotropy.lmf.core.model.FeatureInserter;
import isotropy.lmf.core.model.RelationLazyInserter;

import java.util.function.Supplier;

public final class AttributeBuilder<UnaryType, EffectiveType> implements Attribute.Builder<UnaryType, EffectiveType>
{
	private static final FeatureInserter<AttributeBuilder<?, ?>> FEATURE_INSERTER
			= FeatureInserter.<AttributeBuilder<?, ?>>Builder()
							 .add(LMCoreDefinition.Features.ATTRIBUTE.name, AttributeBuilder::name)
							 .add(LMCoreDefinition.Features.ATTRIBUTE.immutable, AttributeBuilder::immutable)
							 .add(LMCoreDefinition.Features.ATTRIBUTE.many, AttributeBuilder::many)
							 .add(LMCoreDefinition.Features.ATTRIBUTE.mandatory, AttributeBuilder::mandatory)
							 .build();

	private static final RelationLazyInserter<AttributeBuilder<?, ?>> RELATION_INSERTER
			= RelationLazyInserter.<AttributeBuilder<?, ?>>Builder()
								  .add(LMCoreDefinition.Features.ATTRIBUTE.datatype, AttributeBuilder::_datatype)
								  .build();

	private String name = null;
	private boolean many;
	private boolean immutable;
	private boolean mandatory;
	private Supplier<? extends Datatype<UnaryType>> suppliedDatatype = () -> null;

	@Override
	public Attribute<UnaryType, EffectiveType> build()
	{
		return new AttributeImpl<>(name, immutable, many, mandatory, suppliedDatatype.get());
	}

	@Override
	public AttributeBuilder<UnaryType, EffectiveType> name(final String name)
	{
		this.name = name;
		return this;
	}

	@Override
	public AttributeBuilder<UnaryType, EffectiveType> many(final boolean many)
	{
		this.many = many;
		return this;
	}

	@Override
	public AttributeBuilder<UnaryType, EffectiveType> immutable(final boolean immutable)
	{
		this.immutable = immutable;
		return this;
	}

	@Override
	public AttributeBuilder<UnaryType, EffectiveType> mandatory(final boolean mandatory)
	{
		this.mandatory = mandatory;
		return this;
	}

	@Override
	public AttributeBuilder<UnaryType, EffectiveType> datatype(Supplier<Datatype<UnaryType>> suppliedDatatype)
	{
		this.suppliedDatatype = suppliedDatatype;
		return this;
	}

	@SuppressWarnings("unchecked")
	private AttributeBuilder<UnaryType, EffectiveType> _datatype(Supplier<? extends Datatype<?>> suppliedDatatype)
	{
		this.suppliedDatatype = (Supplier<? extends Datatype<UnaryType>>) suppliedDatatype;
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
		RELATION_INSERTER.push(this, relation, supplier);
	}
}
