package isotropy.lmf.core.lang.builder;

import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.lang.impl.AttributeImpl;
import isotropy.lmf.core.model.FeatureInserter;
import isotropy.lmf.core.model.RelationLazyInserter;

import java.util.function.Supplier;

public final class AttributeBuilder<UnaryType, EffectiveType> implements Attribute.Builder<UnaryType, EffectiveType>
{
	private static final FeatureInserter<AttributeBuilder<?, ?>> FEATURE_INSERTER = FeatureInserter.<AttributeBuilder<?, ?>>Builder()
			.add(Attribute.Features.name, AttributeBuilder::name)
			.add(Attribute.Features.immutable, AttributeBuilder::immutable)
			.add(Attribute.Features.many, AttributeBuilder::many)
			.add(Attribute.Features.mandatory, AttributeBuilder::mandatory)
			.build();

	private static final RelationLazyInserter<AttributeBuilder<?, ?>> RELATION_INSERTER = RelationLazyInserter.<AttributeBuilder<?, ?>>Builder()
			.add(Attribute.Features.datatype, AttributeBuilder::_datatype).build();

	private String name = null;
	private boolean many;
	private boolean immutable;
	private boolean mandatory;
	private Supplier<? extends Datatype<UnaryType>> suppliedDatatype;

	@Override
	public Attribute<UnaryType, EffectiveType> build()
	{
		return new AttributeImpl<>(name, many, immutable, mandatory, suppliedDatatype.get());
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
	public <Type> void push(final Feature<Type, ?> feature, final Type value)
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
