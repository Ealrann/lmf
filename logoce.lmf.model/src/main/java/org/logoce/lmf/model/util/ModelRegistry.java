package org.logoce.lmf.model.util;

import org.logoce.lmf.model.lang.LMCorePackage;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.lang.Named;

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
		modelMap = models.stream().collect(Collectors.toUnmodifiableMap(Named::name, Function.identity()));
	}

	public Model getModel(final String name)
	{
		return modelMap.get(name);
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
				modelMap.put(model.name(), model);
			}
		}

		public void register(final Model model)
		{
			modelMap.put(model.name(), model);
		}

		@Override
		public Model getModel(final String name)
		{
			return modelMap.get(name);
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
