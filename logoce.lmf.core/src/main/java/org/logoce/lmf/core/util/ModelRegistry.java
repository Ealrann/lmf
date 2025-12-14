package org.logoce.lmf.core.util;

import org.logoce.lmf.core.lang.LMCoreModelPackage;
import org.logoce.lmf.core.lang.Model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ModelRegistry implements IModelRegistry
{
	private final Map<String, Model> modelMap;

	public static ModelRegistry empty()
	{
		return new Builder(List.of(LMCoreModelPackage.MODEL)).build();
	}

	public ModelRegistry(Map<String, Model> map)
	{
		this.modelMap = Map.copyOf(map);
	}

	public ModelRegistry(final List<Model> models)
	{
		modelMap =  models.stream().collect(Collectors.toUnmodifiableMap(ModelRegistry::domainName, Function.identity()));
	}

	@Override
	public Model getModel(final String qualifiedName)
	{
		return modelMap.get(qualifiedName);
	}

	private static String domainName(final Model model)
	{
		final var domain = model.domain();
		final var name = model.name();
		if (domain == null || domain.isBlank())
		{
			return name;
		}
		return domain + "." + name;
	}

	@Override
	public Model getModel(final String domain, String name)
	{
		return modelMap.get(domain + "." + name);
	}

	@Override
	public Stream<Model> models()
	{
		return modelMap.values().stream();
	}

	public static final class Builder implements IModelRegistry
	{
		private final Map<String, Model> modelMap = new HashMap<>();

		public Builder()
		{
		}

		public Builder(final ModelRegistry modelRegistry)
		{
			modelMap.putAll(modelRegistry.modelMap);
		}

		public Builder(final List<Model> models)
		{
			for (final var model : models)
			{
				register(model);
			}
		}

		public void register(final Model model)
		{
			final var qualifiedName = ModelRegistry.domainName(model);
			if (modelMap.containsKey(qualifiedName))
			{
				throw new IllegalStateException("Model already registered: " + qualifiedName);
			}
			modelMap.put(qualifiedName, model);
		}

		public void remove(final String qualifiedName)
		{
			if (qualifiedName == null) return;
			modelMap.remove(qualifiedName);
		}

		public void remove(final String domain, final String name)
		{
			if (domain == null || name == null) return;
			modelMap.remove(domain + "." + name);
		}

		@Override
		public Model getModel(final String qualifiedName)
		{
			return modelMap.get(qualifiedName);
		}

		@Override
		public Model getModel(final String domain, final String name)
		{
			return modelMap.get(domain + "." + name);
		}

		public ModelRegistry build()
		{
			return new ModelRegistry(modelMap);
		}

		@Override
		public Stream<Model> models()
		{
			return modelMap.values().stream();
		}
	}
}
