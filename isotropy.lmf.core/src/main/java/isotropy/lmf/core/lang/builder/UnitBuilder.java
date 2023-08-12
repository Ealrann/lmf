package isotropy.lmf.core.lang.builder;

import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.lang.impl.UnitImpl;
import isotropy.lmf.core.model.FeatureInserter;

import java.util.function.Supplier;

public final class UnitBuilder<T> implements Unit.Builder<T>
{
	public static final FeatureInserter<UnitBuilder<?>> FEATURE_SETTER = new FeatureInserter.Builder<UnitBuilder<?>>()

			.add(LMCoreFeatures.Unit_name, UnitBuilder::name)
			.add(LMCoreFeatures.Unit_matcher, UnitBuilder::matcher)
			.add(LMCoreFeatures.Unit_defaultValue, UnitBuilder::defaultValue)
			.add(LMCoreFeatures.Unit_primitive, UnitBuilder::primitive)
			.add(LMCoreFeatures.Unit_extractor, UnitBuilder::extractor)
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
	public <Type> void push(final Feature<Type, ?> feature, final Type value)
	{
		FEATURE_SETTER.push(this, feature, value);
	}

	@Override
	public <RelationType extends LMObject> void push(final Relation<RelationType, ?> relation,
													 final Supplier<RelationType> supplier)
	{
	}
}
