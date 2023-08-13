package isotropy.lmf.core.lang.builder;

import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.lang.impl.GroupImpl;
import isotropy.lmf.core.model.FeatureInserter;
import isotropy.lmf.core.model.RelationLazyInserter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class GroupBuilder<T extends LMObject> implements Group.Builder<T>
{
	private static final FeatureInserter<GroupBuilder<?>> FEATURE_INSERTER
			= new FeatureInserter.Builder<GroupBuilder<?>>()

			.add(LMCoreDefinition.Features.GROUP.name, GroupBuilder::name)
			.add(LMCoreDefinition.Features.GROUP.concrete, GroupBuilder::concrete)
			.build();

	private static final RelationLazyInserter<GroupBuilder<?>> BUILDER_INSERTER
			= new RelationLazyInserter.Builder<GroupBuilder<?>>()

			.add(LMCoreDefinition.Features.GROUP.includes, GroupBuilder::addInclude)
			.add(LMCoreDefinition.Features.GROUP.features, GroupBuilder::addFeature)
			.add(LMCoreDefinition.Features.GROUP.generics, GroupBuilder::addGeneric)
			.build();

	private String name = null;
	private boolean concrete;

	private final List<Supplier<Group<?>>> includes = new ArrayList<>();
	private final List<Supplier<Feature<?, ?>>> features = new ArrayList<>();
	private final List<Supplier<Generic>> generics = new ArrayList<>();

	@Override
	public Group<T> build()
	{
		final var builtIncludes = includes.stream()
										  .map(Supplier::get)
										  .toList();
		final var builtFeatures = features.stream()
										  .map(Supplier::get)
										  .toList();
		final var builtGenerics = generics.stream()
										  .map(Supplier::get)
										  .toList();

		return new GroupImpl<>(name, concrete, builtIncludes, builtFeatures, builtGenerics);
	}

	@Override
	public GroupBuilder<T> name(final String name)
	{
		this.name = name;
		return this;
	}

	@Override
	public GroupBuilder<T> concrete(final boolean concrete)
	{
		this.concrete = concrete;
		return this;
	}

	@Override
	public GroupBuilder<T> addInclude(Supplier<Group<?>> include)
	{
		includes.add(include);
		return this;
	}

	@Override
	public GroupBuilder<T> addFeature(Supplier<Feature<?, ?>> feature)
	{
		features.add(feature);
		return this;
	}

	@Override
	public GroupBuilder<T> addGeneric(Supplier<Generic> generic)
	{
		generics.add(generic);
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
		BUILDER_INSERTER.push(this, relation, supplier);
	}
}
