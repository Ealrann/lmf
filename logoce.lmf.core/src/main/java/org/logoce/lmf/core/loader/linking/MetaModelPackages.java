package org.logoce.lmf.core.loader.linking;

import org.logoce.lmf.core.api.model.IModelPackage;
import org.logoce.lmf.core.lang.LMCoreModelPackage;
import org.logoce.lmf.core.lang.MetaModel;
import org.logoce.lmf.core.lang.Model;
import org.logoce.lmf.core.loader.parsing.ModelHeaderUtil;
import org.logoce.lmf.core.api.text.syntax.PNode;
import org.logoce.lmf.core.api.model.ModelRegistry;
import org.logoce.lmf.core.util.tree.Tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper for deriving the list of meta-model packages to use for linking,
 * based on the root header's {@code metamodels} property.
 * <p>
 * This logic was originally embedded in {@code LmLoader}; it is now
 * factored out so that tooling (e.g. an LSP) can reuse the same
 * meta-model selection behaviour.
 */
public final class MetaModelPackages
{
	private MetaModelPackages()
	{
	}

	/**
	 * Resolve meta-model packages for the given roots and registry.
	 * <p>
	 * Semantics:
	 * <ul>
	 *   <li>If there are no roots, returns LMCore only.</li>
	 *   <li>If the root has no {@code metamodels} header, returns LMCore only.</li>
	 *   <li>Otherwise, each entry is looked up as a model in the registry; if it is
	 *       a {@link MetaModel}, its generated {@link IModelPackage} is resolved and
	 *       added to the result.</li>
	 *   <li>If none of the configured metamodels can be resolved to packages,
	 *       falls back to LMCore only.</li>
	 * </ul>
	 */
	public static List<IModelPackage> resolve(final List<? extends Tree<PNode>> roots,
											  final ModelRegistry registry)
	{
		if (roots.isEmpty())
		{
			return List.of(LMCoreModelPackage.Instance);
		}

		final var rootNode = roots.getFirst().data();
		final var metamodelNames = ModelHeaderUtil.resolveMetamodelNames(rootNode);
		if (metamodelNames.isEmpty())
		{
			return List.of(LMCoreModelPackage.Instance);
		}

		final var packages = new ArrayList<IModelPackage>();
		for (final var name : metamodelNames)
		{
			final Model model = registry.getModel(name);
			if (model instanceof MetaModel metaModel)
			{
				packages.add(resolveModelPackage(metaModel));
			}
		}

		return packages.isEmpty() ? List.of(LMCoreModelPackage.Instance) : List.copyOf(packages);
	}

	public static IModelPackage resolveModelPackage(final MetaModel metaModel)
	{
		if (metaModel == null)
		{
			throw new IllegalArgumentException("metaModel is null");
		}

		final var pkg = metaModel.lmPackage();
		if (pkg != null)
		{
			return pkg;
		}

		throw new IllegalStateException("MetaModel " +
										metaModel.domain() +
										"." +
										metaModel.name() +
										" has no lmPackage; ensure it was loaded via LmLoader or registered " +
										"from a generated *ModelPackage");
	}
}
