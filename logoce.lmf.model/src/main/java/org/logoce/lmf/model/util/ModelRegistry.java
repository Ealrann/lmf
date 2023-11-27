package org.logoce.lmf.model.util;

import org.logoce.lmf.model.api.model.IModelPackage;
import org.logoce.lmf.model.lang.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ModelRegistry
{
	public static final ModelRegistry Instance = new ModelRegistry();

	private final Map<String, IModelPackage> modelMap = new HashMap<>();

	private ModelRegistry()
	{
		final var coreModelPackage = LMCorePackage.Instance;
		modelMap.put(coreModelPackage.model().name(), coreModelPackage);
	}

	public Map<String, Alias> getAliasMap()
	{
		return models().map(IModelPackage::model)
					   .map(MetaModel::aliases)
					   .flatMap(Collection::stream)
					   .collect(Collectors.toUnmodifiableMap(Named::name, Function.identity()));
	}

	public IModelPackage get(final String name)
	{
		return modelMap.get(name);
	}

	public Stream<IModelPackage> models()
	{
		return modelMap.values().stream();
	}

	public void register(final IModelPackage modelPackage)
	{
		modelMap.put(modelPackage.model().name(), modelPackage);
	}

	public Stream<Group<?>> streamChildGroups(Group<?> group)
	{
		if (group.concrete())
		{
			return Stream.of(group);
		}
		else
		{
			return modelMap.values()
						   .stream()
						   .map(IModelPackage::model)
						   .flatMap(m -> m.groups().stream())
						   .filter(p -> ModelRegistry.isChildOf(p, group))
						   .flatMap(this::streamChildGroups);
		}
	}

	private static boolean isChildOf(final Group<?> child, final Group<?> parent)
	{
		return child.includes().stream().map(Reference::group).anyMatch(parent::equals);
	}
}
