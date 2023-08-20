package isotropy.lmf.core.lang.builder;

import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.lang.impl.GenericImpl;
import isotropy.lmf.core.model.FeatureInserter;
import isotropy.lmf.core.model.RelationLazyInserter;

import java.util.function.Supplier;

public class GenericBuilder<T> implements Generic.Builder<T>
{
	private static final FeatureInserter<GenericBuilder<?>> FEATURE_INSERTER = FeatureInserter

			.<GenericBuilder<?>>Builder()
			.add(LMCoreDefinition.Features.GENERIC.name, GenericBuilder::name)
			.add(LMCoreDefinition.Features.GENERIC.boundType, GenericBuilder::boundType)
			.build();

	private static final RelationLazyInserter<GenericBuilder<?>> RELATION_INSERTER = RelationLazyInserter

			.<GenericBuilder<?>>Builder()
			.add(LMCoreDefinition.Features.GENERIC.type, GenericBuilder::_type)
			.build();

	private String name;
	private BoundType boundType;
	private Supplier<Type<T>> type;

	@Override
	public Generic<T> build()
	{
		return new GenericImpl<>(name, boundType, type.get());
	}

	@Override
	public GenericBuilder<T> type(Supplier<Type<T>> type)
	{
		this.type = type;
		return this;
	}

	@SuppressWarnings("unchecked")
	public GenericBuilder<T> _type(Supplier<? extends Type<?>> type)
	{
		this.type = (Supplier<Type<T>>) type;
		return this;
	}

	@Override
	public GenericBuilder<T> name(final String name)
	{
		this.name = name;
		return this;
	}

	@Override
	public GenericBuilder<T> boundType(final BoundType boundType)
	{
		this.boundType = boundType;
		return this;
	}

	@Override
	public <Type> void push(final Attribute<Type, ?> feature, final Type value)
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
