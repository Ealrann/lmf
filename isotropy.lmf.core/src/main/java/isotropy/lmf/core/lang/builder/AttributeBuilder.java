package isotropy.lmf.core.lang.builder;

import isotropy.lmf.core.lang.Attribute;
import isotropy.lmf.core.lang.Datatype;
import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.lang.Relation;
import isotropy.lmf.core.lang.impl.AttributeImpl;
import isotropy.lmf.core.model.FeatureInserter;
import isotropy.lmf.core.model.RawFeature;
import isotropy.lmf.core.model.RelationLazyInserter;

import java.util.function.Supplier;

public final class AttributeBuilder<UnaryType, EffectiveType> implements Attribute.Builder<UnaryType, EffectiveType>
{
	private static final FeatureInserter<AttributeBuilder<?, ?>> FEATURE_INSERTER = FeatureInserter

			.<AttributeBuilder<?, ?>>Builder()
			.add(Attribute.Features.name, AttributeBuilder::name)
			.add(Attribute.Features.immutable, AttributeBuilder::immutable)
			.add(Attribute.Features.many, AttributeBuilder::many)
			.add(Attribute.Features.mandatory, AttributeBuilder::mandatory)
			.build();

	private static final RelationLazyInserter<AttributeBuilder<?, ?>> RELATION_INSERTER = RelationLazyInserter

			.<AttributeBuilder<?, ?>>Builder()
			.add(Attribute.Features.datatype, AttributeBuilder::_datatype)
			.build();

	private String name = null;
	private boolean many;
	private boolean immutable;
	private boolean mandatory;
	private Supplier<? extends Datatype<UnaryType>> suppliedDatatype = () -> null;
	private RawFeature<UnaryType, EffectiveType> rawFeature;

	@Override
	public Attribute<UnaryType, EffectiveType> build()
	{
		return new AttributeImpl<>(name, immutable, many, mandatory, suppliedDatatype.get(), rawFeature);
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
	public AttributeBuilder<UnaryType, EffectiveType> rawFeature(RawFeature<UnaryType, EffectiveType> rawFeature)
	{
		this.rawFeature = rawFeature;
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
		FEATURE_INSERTER.push(this, feature.rawFeature(), value);
	}

	@Override
	public <RelationType extends LMObject> void push(final Relation<RelationType, ?> relation,
													 final Supplier<RelationType> supplier)
	{
		RELATION_INSERTER.push(this, relation.rawFeature(), supplier);
	}
}
