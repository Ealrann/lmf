package org.logoce.lmf.model.util;

import org.logoce.lmf.model.lang.LMCorePackage;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Model;

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
		return new Builder(List.of(LMCorePackage.MODEL)).build();
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
		return model instanceof MetaModel mm ? mm.domain() + "." + mm.name() : model.name();
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
				modelMap.put(ModelRegistry.domainName(model), model);
			}
		}

		public void register(final Model model)
		{
			modelMap.put(ModelRegistry.domainName(model), model);
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
