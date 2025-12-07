package org.logoce.lmf.model.util;

import org.logoce.lmf.model.api.model.IModelPackage;
import org.logoce.lmf.model.lang.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MetaModelRegistry
{
	public static final MetaModelRegistry Instance = new MetaModelRegistry();

	private final Map<String, IModelPackage> metamodelMap;

	private final Map<String, Alias> aliasMap;

	private MetaModelRegistry()
	{
		metamodelMap = Map.copyOf(buildMap());
		aliasMap = metamodels().map(IModelPackage::model)
							   .map(MetaModel::aliases)
							   .flatMap(Collection::stream)
							   .collect(Collectors.toUnmodifiableMap(Named::name, Function.identity()));
	}

	private static Map<String, IModelPackage> buildMap()
	{
		final var res = new HashMap<String, IModelPackage>();

		final var p = LMCoreModelPackage.Instance;
		res.put(p.model().name(), p);

		return res;
	}

	public Map<String, Alias> getAliasMap()
	{
		return aliasMap;
	}

	public IModelPackage getPackage(final String name)
	{
		return metamodelMap.get(name);
	}

	public MetaModel getMetaModel(final String name)
	{
		return getPackage(name).model();
	}

	public Stream<IModelPackage> metamodels()
	{
		return metamodelMap.values().stream();
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
							   .filter(p -> MetaModelRegistry.isChildOf(p, group))
							   .flatMap(this::streamChildGroups);
		}
	}

	private static boolean isChildOf(final Group<?> child, final Group<?> parent)
	{
		return child.includes().stream().map(Include::group).anyMatch(parent::equals);
	}
}
