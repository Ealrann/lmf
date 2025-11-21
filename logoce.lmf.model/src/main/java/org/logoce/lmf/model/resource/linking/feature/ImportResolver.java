package org.logoce.lmf.model.resource.linking.feature;

import org.logoce.lmf.model.lang.LMCorePackage;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.resource.interpretation.PFeature;
import org.logoce.lmf.model.resource.linking.tree.LinkNodeInternal;
import org.logoce.lmf.model.util.ModelRegistry;

import java.util.List;

/**
 * Resolves model references (#Model@...) using registry-qualified names and the owning model's imports.
 */
public final class ImportResolver
{
	private final ModelRegistry modelRegistry;

	public ImportResolver(final ModelRegistry modelRegistry)
	{
		this.modelRegistry = modelRegistry;
	}

	public Model resolve(final LinkNodeInternal<?, ?, ?> node, final String modelName, final String context)
	{
		final var qualifiedName = qualifiedModelName(node, modelName, context);
		final var model = modelRegistry.getModel(qualifiedName);
		if (model == null)
		{
			final var available = modelRegistry.models().map(ImportResolver::qualifiedName).toList();
			throw new AssertionError("Cannot resolve model '" + modelName + "' in registry. Available models: " +
									 available);
		}
		return model;
	}

	private String qualifiedModelName(final LinkNodeInternal<?, ?, ?> node,
									  final String modelName,
									  final String context)
	{
		if (modelName.contains("."))
		{
			return modelName;
		}
		if (LMCorePackage.MODEL.name().equals(modelName))
		{
			return qualifiedName(LMCorePackage.MODEL);
		}

		final var model = owningModel(node, context);
		for (final var imported : model.imports())
		{
			if (simpleName(imported).equals(modelName))
			{
				return imported;
			}
		}

		if (model.name().equals(modelName))
		{
			return model.qualifiedName();
		}

		throw new AssertionError("Cannot resolve imported model '" + modelName + "' in " + model.qualifiedName() +
								 ". Available imports: " + model.imports());
	}

	private ModelContext owningModel(final LinkNodeInternal<?, ?, ?> node, final String context)
	{
		final var rootFeatures = node.root().features();
		final var domain = findFirstValue(rootFeatures, "domain");
		final var name = findFirstValue(rootFeatures, "name");
		if (domain == null || name == null)
		{
			throw new IllegalStateException("Cannot find owning model information for " + context);
		}
		final var imports = findValues(rootFeatures, "imports");
		return new ModelContext(domain, name, imports);
	}

	private static String findFirstValue(final List<PFeature> features, final String targetName)
	{
		for (final var feature : features)
		{
			if (feature.isRelation()) continue;
			final var name = feature.name();
			if (name.isPresent() && targetName.equals(name.get()))
			{
				return feature.values().getFirst();
			}
		}
		return null;
	}

	private static List<String> findValues(final List<PFeature> features, final String targetName)
	{
		return features.stream()
					   .filter(pFeature -> pFeature.isRelation() == false)
					   .filter(pFeature -> pFeature.name().filter(targetName::equals).isPresent())
					   .flatMap(pFeature -> pFeature.values().stream())
					   .toList();
	}

	private static String simpleName(final String qualifiedName)
	{
		final int lastDot = qualifiedName.lastIndexOf('.');
		return lastDot == -1 ? qualifiedName : qualifiedName.substring(lastDot + 1);
	}

	public static String qualifiedName(final Model model)
	{
		return model.domain() + "." + model.name();
	}

	private record ModelContext(String domain, String name, List<String> imports)
	{
		public String qualifiedName()
		{
			return domain + "." + name;
		}
	}
}
