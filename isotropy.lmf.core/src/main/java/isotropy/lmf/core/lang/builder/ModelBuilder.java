package isotropy.lmf.core.lang.builder;

import isotropy.lmf.core.lang.Enum;
import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.lang.impl.ModelImpl;
import isotropy.lmf.core.model.FeatureInserter;
import isotropy.lmf.core.model.IModelPackage;
import isotropy.lmf.core.model.RelationLazyInserter;
import isotropy.lmf.core.util.BuildUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class ModelBuilder implements Model.Builder
{
	private static final FeatureInserter<ModelBuilder> FEATURE_INSERTER = new FeatureInserter.Builder<ModelBuilder>()

			.add(Model.Features.name, ModelBuilder::name)
			.add(Model.Features.domain, ModelBuilder::domain)
			.build();

	private static final RelationLazyInserter<ModelBuilder> BUILDER_INSERTER = new RelationLazyInserter.Builder<ModelBuilder>()

			.add(Model.Features.groups, ModelBuilder::addGroup)
			.add(Model.Features.enums, ModelBuilder::addEnum)
			.add(Model.Features.units, ModelBuilder::addUnit)
			.add(Model.Features.aliases, ModelBuilder::addAlias)
			.build();

	private String name;
	private String domain;
	private IModelPackage modelPackage;

	private final List<Supplier<? extends Group<?>>> groups = new ArrayList<>();
	private final List<Supplier<? extends Enum<?>>> enums = new ArrayList<>();
	private final List<Supplier<? extends Unit<?>>> units = new ArrayList<>();
	private final List<Supplier<Alias>> aliases = new ArrayList<>();

	@Override
	public Model build()
	{
		final var builtGroups = BuildUtils.collectSuppliers(groups);
		final var builtEnums = BuildUtils.collectSuppliers(enums);
		final var builtUnits = BuildUtils.collectSuppliers(units);
		final var builtAliases = BuildUtils.collectSuppliers(aliases);

		return new ModelImpl(modelPackage, name, domain, builtGroups, builtEnums, builtUnits, builtAliases);
	}

	@Override
	public ModelBuilder name(final String name)
	{
		this.name = name;
		return this;
	}

	@Override
	public ModelBuilder domain(final String domain)
	{
		this.domain = domain;
		return this;
	}

	@Override
	public Model.Builder lPackage(final IModelPackage modelPackage)
	{
		this.modelPackage = modelPackage;
		return this;
	}

	@Override
	public ModelBuilder addGroup(final Supplier<? extends Group<?>> group)
	{
		groups.add(group);
		return this;
	}

	@Override
	public ModelBuilder addEnum(final Supplier<? extends Enum<?>> _enum)
	{
		enums.add(_enum);
		return this;
	}

	@Override
	public ModelBuilder addUnit(final Supplier<? extends Unit<?>> unit)
	{
		units.add(unit);
		return this;
	}

	@Override
	public ModelBuilder addAlias(final Supplier<Alias> alias)
	{
		aliases.add(alias);
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
