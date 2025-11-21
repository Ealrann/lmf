package org.logoce.lmf.model.util;

import org.logoce.lmf.model.lang.LMCorePackage;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
		modelMap = models.stream()
						 .flatMap(model -> streamAliases(model).map(name -> Map.entry(name, model)))
						 .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
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
			streamAliases(model).forEach(name -> modelMap.put(name, model));
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

	private static Stream<String> streamAliases(final Model model)
	{
		if (model instanceof MetaModel metaModel)
		{
			return Stream.of(metaModel.name(), metaModel.domain() + "." + metaModel.name());
		}
		return Stream.of(model.name());
	}
}
