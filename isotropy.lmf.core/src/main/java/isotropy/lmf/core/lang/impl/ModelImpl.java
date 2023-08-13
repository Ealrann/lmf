package isotropy.lmf.core.lang.impl;

import isotropy.lmf.core.lang.Enum;
import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.model.FeatureMap;
import isotropy.lmf.core.model.IModelPackage;

import java.util.List;
import java.util.function.Function;

public final class ModelImpl implements Model
{
	public static final FeatureMap<Function<Model, Object>> GET_MAP = new FeatureMap<>(

			List.of(new FeatureMap.FeatureTuple<>(LMCoreFeatures.Model_name, Named::name),
					new FeatureMap.FeatureTuple<>(LMCoreFeatures.Model_groups, Model::groups),
					new FeatureMap.FeatureTuple<>(LMCoreFeatures.Model_enums, Model::enums),
					new FeatureMap.FeatureTuple<>(LMCoreFeatures.Model_units, Model::units),
					new FeatureMap.FeatureTuple<>(LMCoreFeatures.Model_aliases, Model::aliases)));

	private final IModelPackage _package;
	private final String name;
	private final List<Group<?>> groups;
	private final List<Enum<?>> enums;
	private final List<Unit<?>> units;
	private final List<Alias> aliases;

	private LMObject container;

	public ModelImpl(final IModelPackage _package,
					 final String name,
					 final List<Group<?>> groups,
					 final List<Enum<?>> enums,
					 final List<Unit<?>> units,
					 final List<Alias> aliases)
	{
		this._package = _package;
		this.name = name;
		this.groups = groups;
		this.enums = enums;
		this.units = units;
		this.aliases = aliases;
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

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(final Feature<?, T> feature)
	{
		return (T) GET_MAP.get(feature).apply(this);
	}

	@Override
	public <T> void set(final Feature<?, T> feature, final T value)
	{
		throw new IllegalAccessError("Group " + Generic.class.getSimpleName() + " is immutable.");
	}

	@Override
	public Group<?> lmGroup()
	{
		return LMCorePackage.Groups.MODEL_GROUP;
	}

	@Override
	public LMObject lContainer()
	{
		return container;
	}

	@Override
	public void lContainer(final LMObject container)
	{
		this.container = container;
	}

	@Override
	public IModelPackage lPackage()
	{
		return _package;
	}
}
