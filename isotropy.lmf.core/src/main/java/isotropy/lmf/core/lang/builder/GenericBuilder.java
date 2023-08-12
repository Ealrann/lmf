package isotropy.lmf.core.lang.builder;

import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.lang.impl.GenericImpl;
import isotropy.lmf.core.model.FeatureInserter;
import isotropy.lmf.core.model.RelationLazyInserter;

import java.util.function.Supplier;

public class GenericBuilder implements Generic.Builder
{
	private static final FeatureInserter<GenericBuilder> FEATURE_INSERTER = FeatureInserter.<GenericBuilder>Builder()

			.add(Generic.Features.name, GenericBuilder::name)
			.add(Generic.Features.boundType, GenericBuilder::boundType)
			.build();

	private static final RelationLazyInserter<GenericBuilder> RELATION_INSERTER = RelationLazyInserter.<GenericBuilder>Builder()

			.add(Generic.Features.type, GenericBuilder::type).build();

	private String name;
	private BoundType boundType;
	private Supplier<Type> type;

	@Override
	public Generic build()
	{
		return new GenericImpl(name, boundType, type.get());
	}

	@Override
	public GenericBuilder type(Supplier<Type> type)
	{
		this.type = type;
		return this;
	}

	@Override
	public GenericBuilder name(final String name)
	{
		this.name = name;
		return this;
	}

	@Override
	public GenericBuilder boundType(final BoundType boundType)
	{
		this.boundType = boundType;
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
