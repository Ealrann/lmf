package isotropy.lmf.core.lang.builder;

import isotropy.lmf.core.lang.Attribute;
import isotropy.lmf.core.lang.JavaWrapper;
import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.lang.Relation;
import isotropy.lmf.core.lang.impl.JavaWrapperImpl;
import isotropy.lmf.core.model.FeatureInserter;
import isotropy.lmf.core.model.RelationLazyInserter;

import java.util.function.Supplier;

public final class JavaWrapperBuilder<T> implements JavaWrapper.Builder<T>
{
	private static final FeatureInserter<JavaWrapperBuilder<?>> FEATURE_INSERTER = new FeatureInserter

			.Builder<JavaWrapperBuilder<?>>().add(JavaWrapper.Features.name, JavaWrapperBuilder::name)
										  .add(JavaWrapper.Features.domain, JavaWrapperBuilder::domain)
										  .build();

	private static final RelationLazyInserter<JavaWrapperBuilder<?>> BUILDER_INSERTER = new RelationLazyInserter

			.Builder<JavaWrapperBuilder<?>>().build();

	private String name;
	private String domain;

	@Override
	public JavaWrapper<T> build()
	{

		return new JavaWrapperImpl<>(name, domain);
	}

	@Override
	public JavaWrapperBuilder<T> name(final String name)
	{
		this.name = name;
		return this;
	}

	@Override
	public JavaWrapperBuilder<T> domain(final String domain)
	{
		this.domain = domain;
		return this;
	}

	@Override
	public <Type> void push(final Attribute<Type, ?> feature, final Type value)
	{
		FEATURE_INSERTER.push(this, feature.rawFeature(), value);
	}

	@Override
	public <RelationType extends LMObject> void push(final Relation<RelationType, ?> relation,
													 final Supplier<RelationType> supplier)
	{
		BUILDER_INSERTER.push(this, relation.rawFeature(), supplier);
	}
}
