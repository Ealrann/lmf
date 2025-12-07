package org.logoce.lmf.lsp.workspace;

import org.logoce.lmf.model.api.model.IModelPackage;
import org.logoce.lmf.model.lang.LMCoreModelPackage;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.model.loader.linking.LmModelLinker;
import org.logoce.lmf.model.loader.linking.LinkNode;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.util.ModelRegistry;
import org.logoce.lmf.model.util.tree.Tree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Helper for semantic-only linking in the LSP.
 * <p>
 * This builds link trees using {@link LmModelLinker} and meta-model packages when
 * available, but degrades gracefully to an empty tree list when meta-model packages
 * cannot be resolved.
 */
final class SemanticLinking
{
	private static final Logger LOG = LoggerFactory.getLogger(SemanticLinking.class);

	private SemanticLinking()
	{
	}

	static List<? extends LinkNode<?, PNode>> link(final List<? extends Tree<PNode>> roots,
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
			if (pkg != null && metaModelPackages.stream().noneMatch(p -> p.getClass().equals(pkg.getClass())))
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
			LOG.debug("Semantic linking failed, returning empty link trees: {}", e.getMessage(), e);
			return List.of();
		}
	}

	private static IModelPackage tryResolveModelPackage(final MetaModel metaModel)
	{
		// Mirror MetaModelPackages.resolveModelPackage but swallow errors so that
		// semantic-only linking can fall back gracefully when generated packages
		// are not on the classpath.
		final StringBuilder pkg = new StringBuilder(metaModel.domain());

		final String extra = metaModel.extraPackage();
		if (extra != null && !extra.isBlank())
		{
			pkg.append('.').append(extra);
		}

		if (metaModel.genNamePackage())
		{
			pkg.append('.').append(metaModel.name().toLowerCase(Locale.ROOT));
		}

		final String className = pkg + "." + metaModel.name() + "Package";

		try
		{
			final Class<?> clazz = Class.forName(className);
			if (!IModelPackage.class.isAssignableFrom(clazz))
			{
				return null;
			}

			@SuppressWarnings("unchecked")
			final Class<? extends IModelPackage> typed = (Class<? extends IModelPackage>) clazz;

			try
			{
				final var instanceField = typed.getField("Instance");
				return (IModelPackage) instanceField.get(null);
			}
			catch (NoSuchFieldException e)
			{
				return typed.getDeclaredConstructor().newInstance();
			}
		}
		catch (Exception e)
		{
			LOG.debug("Cannot resolve model package for metamodel {}.{}: {}",
					  metaModel.domain(), metaModel.name(), e.getMessage());
			return null;
		}
	}
}

