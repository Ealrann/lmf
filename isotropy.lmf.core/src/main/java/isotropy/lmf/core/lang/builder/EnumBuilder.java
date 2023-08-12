package isotropy.lmf.core.lang.builder;

import isotropy.lmf.core.lang.Enum;
import isotropy.lmf.core.lang.Feature;
import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.lang.Relation;
import isotropy.lmf.core.lang.impl.EnumImpl;
import isotropy.lmf.core.model.FeatureInserter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class EnumBuilder<T> implements Enum.Builder<T>
{
	private static final FeatureInserter<EnumBuilder<?>> FEATURE_INSERTER = FeatureInserter.<EnumBuilder<?>>Builder()

			.add(Enum.Features.name, EnumBuilder::name).add(Enum.Features.literals, EnumBuilder::addLiteral).build();

	private String name = null;
	private final List<String> literals = new ArrayList<>();

	@Override
	public EnumImpl<T> build()
	{
		return new EnumImpl<>(name, literals);
	}

	@Override
	public EnumBuilder<T> name(String name)
	{
		this.name = name;
		return this;
	}

	@Override
	public Enum.Builder<T> addLiteral(String literal)
	{
		literals.add(literal);
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
	}
}
