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

	private final Map<String, IModelPackage> metamodelMap = new HashMap<>();

	private final Map<String, Model> modelMap = new HashMap<>();

	private ModelRegistry()
	{
		register(LMCorePackage.Instance);
	}

	public Map<String, Alias> getAliasMap()
	{
		return metamodels().map(IModelPackage::model)
						   .map(MetaModel::aliases)
						   .flatMap(Collection::stream)
						   .collect(Collectors.toUnmodifiableMap(Named::name, Function.identity()));
	}

	public IModelPackage getMetaModel(final String name)
	{
		return metamodelMap.get(name);
	}

	public Model getModel(final String name)
	{
		return modelMap.get(name);
	}

	public Stream<IModelPackage> metamodels()
	{
		return metamodelMap.values().stream();
	}

	public Stream<Model> models()
	{
		return modelMap.values().stream();
	}

	public void register(final IModelPackage modelPackage)
	{
		metamodelMap.put(modelPackage.model().name(), modelPackage);
		register(modelPackage.model());
	}

	public void register(final Model model)
	{
		modelMap.put(model.name(), model);
	}

	public Stream<Group<?>> streamChildGroups(Group<?> group)
	{
		if (group.concrete())
		{
			return Stream.of(group);
		}
		else
		{
			return metamodelMap.values()
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
