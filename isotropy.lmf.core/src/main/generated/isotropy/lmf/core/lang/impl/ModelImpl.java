package isotropy.lmf.core.lang.impl;

import isotropy.lmf.core.lang.Enum;
import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.model.FeatureGetter;
import isotropy.lmf.core.model.FeatureSetter;
import isotropy.lmf.core.model.FeaturedObject;
import isotropy.lmf.core.model.IModelPackage;

import java.util.List;

public final class ModelImpl extends FeaturedObject implements Model
{
	private static final FeatureGetter<Model> GET_MAP = new FeatureGetter.Builder<Model>().add(Features.name,
																							   Model::name)
																						  .add(Features.groups,
																							   Model::groups)
																						  .add(Features.enums,
																							   Model::enums)
																						  .add(Features.units,
																							   Model::units)
																						  .add(Features.aliases,
																							   Model::aliases)
																						  .add(Features.javaWrappers,
																							   Model::javaWrappers)
																						  .add(Features.lPackage,
																							   Model::lPackage)
																						  .build();

	private static final FeatureSetter<Model> SET_MAP = new FeatureSetter.Builder<Model>().build();

	private final String name;
	private final String domain;

	private final List<Group<?>> groups;

	private final List<Enum<?>> enums;

	private final List<Unit<?>> units;

	private final List<Alias> aliases;

	private final List<JavaWrapper<?>> javaWrappers;

	private final IModelPackage lPackage;

	public ModelImpl(final String name,
					 final String domain,
					 final List<Group<?>> groups,
					 final List<Enum<?>> enums,
					 final List<Unit<?>> units,
					 final List<Alias> aliases,
					 final List<JavaWrapper<?>> javaWrappers,
					 final IModelPackage lPackage)
	{
		this.name = name;
		this.domain = domain;
		this.groups = List.copyOf(groups);
		this.enums = List.copyOf(enums);
		this.units = List.copyOf(units);
		this.aliases = List.copyOf(aliases);
		this.javaWrappers = List.copyOf(javaWrappers);
		this.lPackage = lPackage;
		setContainer(groups, Features.groups);
		setContainer(enums, Features.enums);
		setContainer(units, Features.units);
		setContainer(aliases, Features.aliases);
		setContainer(javaWrappers, Features.javaWrappers);
	}

	@Override
	public String domain()
	{
		return domain;
	}

	@Override
	public String name()
	{
		return name;
	}

	@Override
	public List<Group<?>> groups()
	{
		return groups;
	}

	@Override
	public List<Enum<?>> enums()
	{
		return enums;
	}

	@Override
	public List<Unit<?>> units()
	{
		return units;
	}

	@Override
	public List<Alias> aliases()
	{
		return aliases;
	}

	@Override
	public List<JavaWrapper<?>> javaWrappers()
	{
		return javaWrappers;
	}

	@Override
	public IModelPackage lPackage()
	{
		return lPackage;
	}

	@Override
	public isotropy.lmf.core.lang.Group<Model> lmGroup()
	{
		return LMCoreDefinition.Groups.MODEL;
	}

	@Override
	protected FeatureSetter<Model> setterMap()
	{
		return SET_MAP;
	}

	@Override
	protected FeatureGetter<Model> getterMap()
	{
		return GET_MAP;
	}
}
