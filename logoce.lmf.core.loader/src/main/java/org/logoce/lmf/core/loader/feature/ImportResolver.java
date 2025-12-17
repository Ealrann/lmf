package org.logoce.lmf.core.loader.feature;

import org.logoce.lmf.core.lang.LMCoreModelPackage;
import org.logoce.lmf.core.lang.Model;
import org.logoce.lmf.core.loader.api.loader.linking.LinkException;
import org.logoce.lmf.core.loader.api.loader.linking.tree.LinkNodeInternal;
import org.logoce.lmf.core.loader.interpretation.PFeature;
import org.logoce.lmf.core.loader.api.loader.util.ModelImports;
import org.logoce.lmf.core.api.model.ModelRegistry;

import java.util.List;

/**
 * Resolves model references (#Model@...) using registry-qualified names and the owning model's imports.
 * This mirrors the behavior of the legacy resource linker ImportResolver.
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
			final var available = modelRegistry.models()
											  .map(ImportResolver::qualifiedName)
											  .toList();
			throw new LinkException("Cannot resolve model '" + modelName + "' in registry. Available models: " +
									available, node.pNode());
		}
		return model;
	}

	private String qualifiedModelName(final LinkNodeInternal<?, ?, ?> node,
									  final String modelName,
									  final String context)
	{
		// Preserve legacy resolution order: fully-qualified first, then LMCore,
		// then imports/self using header information.
		if (modelName.contains("."))
		{
			return modelName;
		}

		if (LMCoreModelPackage.MODEL.name().equals(modelName))
		{
			final var lmCore = LMCoreModelPackage.MODEL;
			return lmCore.domain() + "." + lmCore.name();
		}

		final var model = owningModel(node, context);
		return ModelImports.resolveQualifiedName(model.domain(), model.name(), model.imports(), modelName)
						   .orElseThrow(() -> new LinkException("Cannot resolve imported model '" + modelName +
																"' in " + model.qualifiedName() +
																". Available imports: " + model.imports(),
																node.pNode()));
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

	private static String qualifiedName(final Model model)
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
