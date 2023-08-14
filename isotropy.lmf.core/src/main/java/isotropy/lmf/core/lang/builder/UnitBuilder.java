package isotropy.lmf.core.lang.builder;

import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.lang.impl.UnitImpl;
import isotropy.lmf.core.model.FeatureInserter;

import java.util.function.Supplier;

public final class UnitBuilder<T> implements Unit.Builder<T>
{
	public static final FeatureInserter<UnitBuilder<?>> FEATURE_SETTER = new FeatureInserter.Builder<UnitBuilder<?>>()

			.add(LMCoreDefinition.Features.UNIT.name, UnitBuilder::name)
			.add(LMCoreDefinition.Features.UNIT.matcher, UnitBuilder::matcher)
			.add(LMCoreDefinition.Features.UNIT.defaultValue, UnitBuilder::defaultValue)
			.add(LMCoreDefinition.Features.UNIT.primitive, UnitBuilder::primitive)
			.add(LMCoreDefinition.Features.UNIT.extractor, UnitBuilder::extractor)
			.build();

	private String name;
	private String matcher;
	private String defaultValue;
	private Primitive primitive;
	private String extractor;

	@Override
	public UnitImpl<T> build()
	{
		return new UnitImpl<T>(name, matcher, defaultValue, primitive, extractor);
	}

	@Override
	public UnitBuilder<T> name(final String name)
	{
		this.name = name;
		return this;
	}

	@Override
	public UnitBuilder<T> matcher(final String matcher)
	{
		this.matcher = matcher;
		return this;
	}

	@Override
	public UnitBuilder<T> defaultValue(final String defaultValue)
	{
		this.defaultValue = defaultValue;
		return this;
	}

	@Override
	public UnitBuilder<T> primitive(final Primitive primitive)
	{
		this.primitive = primitive;
		return this;
	}

	@Override
	public UnitBuilder<T> extractor(final String extractor)
	{
		this.extractor = extractor;
		return this;
	}

	@Override
	public <Type> void push(final Attribute<Type, ?> feature, final Type value)
	{
		FEATURE_SETTER.push(this, feature, value);
	}

	@Override
	public <RelationType extends LMObject> void push(final Relation<RelationType, ?> relation,
													 final Supplier<RelationType> supplier)
	{
	}
}
