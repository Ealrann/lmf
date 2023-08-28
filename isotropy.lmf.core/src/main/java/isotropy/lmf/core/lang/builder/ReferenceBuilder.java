package isotropy.lmf.core.lang.builder;

import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.lang.impl.ReferenceImpl;
import isotropy.lmf.core.model.FeatureInserter;
import isotropy.lmf.core.model.RelationLazyInserter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class ReferenceBuilder<UnaryType extends LMObject> implements Reference.Builder<UnaryType>
{
	private static final FeatureInserter<ReferenceBuilder<?>> FEATURE_INSERTER = FeatureInserter

			.<ReferenceBuilder<?>>Builder()
			.build();

	private static final RelationLazyInserter<ReferenceBuilder<?>> RELATION_INSERTER = RelationLazyInserter

			.<ReferenceBuilder<?>>Builder()
			.add(Reference.Features.group, ReferenceBuilder::_group)
			.add(Reference.Features.parameters, ReferenceBuilder::addParameter)
			.build();

	private Supplier<Concept<UnaryType>> group = null;
	private final List<Supplier<? extends Concept<?>>> parameters = new ArrayList<>();

	@Override
	public Reference<UnaryType> build()
	{
		return new ReferenceImpl<UnaryType>(group, parameters);
	}

	@Override
	public ReferenceBuilder<UnaryType> group(final Supplier<Concept<UnaryType>> group)
	{
		this.group = group;
		return this;
	}

	@SuppressWarnings("unchecked")
	private ReferenceBuilder<UnaryType> _group(final Supplier<? extends Concept<?>> group)
	{
		this.group = (Supplier<Concept<UnaryType>>) group;
		return this;
	}

	@Override
	public ReferenceBuilder<UnaryType> addParameter(final Supplier<Concept<?>> parameter)
	{
		this.parameters.add(parameter);
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
