package org.logoce.lmf.core.loader.api.tooling;

import org.logoce.lmf.core.lang.LMCoreModelPackage;
import org.logoce.lmf.core.lang.MetaModel;
import org.logoce.lmf.core.lang.Model;
import org.logoce.lmf.core.loader.api.loader.parsing.ModelHeaderUtil;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;
import org.logoce.lmf.core.loader.api.text.syntax.PToken;
import org.logoce.lmf.core.api.model.ModelRegistry;
import org.logoce.lmf.core.util.tree.Tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper for resolving the active meta-model for a document.
 * <p>
 * Resolution order:
 * <ul>
 *   <li>If the header declares {@code metamodels=...}, look up each name in the registry and
 *       return the first {@link MetaModel} found.</li>
 *   <li>If no {@code metamodels} are declared and the semantic root model is itself a
 *       {@link MetaModel}, return it.</li>
 *   <li>If nothing matches, fall back to {@link LMCoreModelPackage#MODEL} so that LMCore editing
 *       remains supported even with an empty registry.</li>
 * </ul>
 */
public final class MetaModelResolver
{
	private MetaModelResolver()
	{
	}

	/**
	 * Resolve all meta-models explicitly declared in the given roots via the
	 * {@code metamodels=} header. This is a narrow, header-only view used by
	 * components that need a list of active meta-models.
	 */
	public static List<MetaModel> resolveActiveMetaModelsFromRoots(
		final List<? extends Tree<PNode>> roots,
		final ModelRegistry registry)
	{
		if (roots == null || roots.isEmpty())
		{
			return List.of();
		}

		final var result = new ArrayList<MetaModel>();
		final var rootNode = roots.getFirst().data();
		final var metamodelNames = ModelHeaderUtil.resolveMetamodelNames(rootNode);

		for (final String name : metamodelNames)
		{
			final Model model = registry.getModel(name);
			if (model instanceof MetaModel mm)
			{
				result.add(mm);
			}
		}

		return List.copyOf(result);
	}

	public static MetaModel resolveForDocument(final List<? extends Tree<PNode>> roots,
											   final CharSequence source,
											   final Model semanticModel,
											   final ModelRegistry registry)
	{
		if (roots != null && !roots.isEmpty())
		{
			final var rootNode = roots.getFirst().data();
			final var metamodelNames = ModelHeaderUtil.resolveMetamodelNames(rootNode);

			for (final String name : metamodelNames)
			{
				final Model model = registry.getModel(name);
				if (model instanceof MetaModel mm)
				{
					return mm;
				}
			}

			if (!metamodelNames.isEmpty())
			{
				return LMCoreModelPackage.MODEL;
			}

			if (!ModelHeaderUtil.isMetaModelRoot(roots))
			{
				final String rootKeyword = headerKeyword(rootNode);
				if (rootKeyword != null && !rootKeyword.isBlank())
				{
					for (final Model model : (Iterable<Model>) registry.models()::iterator)
					{
						if (model instanceof MetaModel mm)
						{
							final boolean matches = mm.groups()
													 .stream()
													 .anyMatch(g -> rootKeyword.equals(g.name()));
							if (matches)
							{
								return mm;
							}
						}
					}
				}
			}

			if (ModelHeaderUtil.isMetaModelRoot(roots))
			{
				final String domain = ModelHeaderUtil.resolveDomain(rootNode);
				final String name = ModelHeaderUtil.resolveName(rootNode);
				if (domain != null && !domain.isBlank() && name != null && !name.isBlank())
				{
					final Model selfModel = registry.getModel(domain, name);
					if (selfModel instanceof MetaModel mm)
					{
						return mm;
					}
				}
			}
		}
		else if (source != null)
		{
			final var namesFromText = HeaderTextScanner.parseMetamodelNames(source);
			if (!namesFromText.isEmpty())
			{
				for (final String name : namesFromText)
				{
					final Model model = registry.getModel(name);
					if (model instanceof MetaModel mm)
					{
						return mm;
					}
				}
				return LMCoreModelPackage.MODEL;
			}
		}

		if (semanticModel instanceof MetaModel mm)
		{
			return mm;
		}

		return LMCoreModelPackage.MODEL;
	}

	private static String headerKeyword(final PNode node)
	{
		for (final PToken token : node.tokens())
		{
			final String value = token.value();
			if (value == null || value.isBlank() || "(".equals(value))
			{
				continue;
			}
			return value;
		}
		return null;
	}
}
