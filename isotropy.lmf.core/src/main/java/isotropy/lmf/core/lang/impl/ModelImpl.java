package isotropy.lmf.core.lang.impl;

import isotropy.lmf.core.lang.Enum;
import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.model.FeatureMap;
import isotropy.lmf.core.model.FeaturedObject;
import isotropy.lmf.core.model.IModelPackage;

import java.util.List;
import java.util.function.Function;

public final class ModelImpl extends FeaturedObject implements Model
{
	public static final FeatureMap<Function<Model, Object>> GET_MAP = new FeatureMap<>(

			List.of(new FeatureMap.FeatureTuple<>(Features.name, Named::name),
					new FeatureMap.FeatureTuple<>(Features.domain, Model::domain),
					new FeatureMap.FeatureTuple<>(Features.groups, Model::groups),
					new FeatureMap.FeatureTuple<>(Features.enums, Model::enums),
					new FeatureMap.FeatureTuple<>(Features.units, Model::units),
					new FeatureMap.FeatureTuple<>(Features.aliases, Model::aliases),
					new FeatureMap.FeatureTuple<>(Features.javaWrappers, Model::javaWrappers)));

	private final IModelPackage _package;
	private final String name;
	private final String domain;
	private final List<Group<?>> groups;
	private final List<Enum<?>> enums;
	private final List<Unit<?>> units;
	private final List<Alias> aliases;
	private final List<JavaWrapper<?>> javaWrappers;

	public ModelImpl(final IModelPackage _package,
					 final String name,
					 final String domain,
					 final List<Group<?>> groups,
					 final List<Enum<?>> enums,
					 final List<Unit<?>> units,
					 final List<Alias> aliases,
					 final List<JavaWrapper<?>> javaWrappers)
	{
		this._package = _package;
		this.name = name;
		this.domain = domain;
		this.groups = groups;
		this.enums = enums;
		this.units = units;
		this.aliases = aliases;
		this.javaWrappers = javaWrappers;

		ContainmentUtils.setContainer(this, groups, Features.groups);
		ContainmentUtils.setContainer(this, enums, Features.enums);
		ContainmentUtils.setContainer(this, units, Features.units);
		ContainmentUtils.setContainer(this, aliases, Features.aliases);
		ContainmentUtils.setContainer(this, javaWrappers, Features.javaWrappers);
	}

	@Override
	public String name()
	{
		return name;
	}

	@Override
	public String domain()
	{
		return domain;
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

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(final Feature<?, T> feature)
	{
		return (T) GET_MAP.get(feature.rawFeature())
						  .apply(this);
	}

	@Override
	public <T> void set(final Feature<?, T> feature, final T value)
	{
		throw new IllegalAccessError("Group " + Generic.class.getSimpleName() + " is immutable.");
	}

	@Override
	public Group<?> lmGroup()
	{
		return LMCoreDefinition.Groups.MODEL;
	}

	@Override
	public IModelPackage lPackage()
	{
		return _package;
	}
}
