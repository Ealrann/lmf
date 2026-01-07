package org.logoce.lmf.core.loader.api.tooling.workspace;

import org.logoce.lmf.core.api.model.IModelPackage;
import org.logoce.lmf.core.lang.LMCoreModelPackage;
import org.logoce.lmf.core.lang.MetaModel;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.loader.api.loader.linking.LmModelLinker;
import org.logoce.lmf.core.loader.api.loader.linking.LinkNode;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;
import org.logoce.lmf.core.api.model.ModelRegistry;
import org.logoce.lmf.core.util.tree.Tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper for semantic-only linking in tooling workflows.
 * <p>
 * This builds link trees using {@link LmModelLinker} and meta-model packages when
 * available, but degrades gracefully to an empty tree list when meta-model packages
 * cannot be resolved.
 */
public final class SemanticLinking
{
	private SemanticLinking()
	{
	}

	public static List<? extends LinkNode<?, PNode>> link(final List<? extends Tree<PNode>> roots,
														  final List<MetaModel> activeMetaModels,
														  final ModelRegistry registry,
														  final CharSequence source)
	{
		if (roots == null || roots.isEmpty())
		{
			return List.of();
		}

		final var metaModelPackages = new ArrayList<IModelPackage>();
		metaModelPackages.add(LMCoreModelPackage.Instance);

		for (final MetaModel mm : activeMetaModels)
		{
			final IModelPackage pkg = tryResolveModelPackage(mm);
			if (pkg != null && metaModelPackages.stream().map(IModelPackage::model).noneMatch(m -> sameModel(m, mm)))
			{
				metaModelPackages.add(pkg);
			}
		}

		try
		{
			final var linker = new LmModelLinker<PNode>(registry, metaModelPackages);
			final var diagnostics = new ArrayList<LmDiagnostic>();
			final var result = linker.linkModel(roots, diagnostics, source);
			return result.trees();
		}
		catch (Exception e)
		{
			return List.of();
		}
	}

	private static IModelPackage tryResolveModelPackage(final MetaModel metaModel)
	{
		if (metaModel == null)
		{
			return null;
		}

		return metaModel.lmPackage();
	}

	private static boolean sameModel(final MetaModel left, final MetaModel right)
	{
		if (left == right) return true;
		if (left == null || right == null) return false;
		return left.name().equals(right.name()) && left.domain().equals(right.domain());
	}
}
