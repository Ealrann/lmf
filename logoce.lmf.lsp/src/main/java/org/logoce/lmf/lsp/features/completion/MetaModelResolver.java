package org.logoce.lmf.lsp.features.completion;

import org.logoce.lmf.lsp.state.SyntaxSnapshot;
import org.logoce.lmf.model.lang.LMCorePackage;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.loader.parsing.ModelHeaderUtil;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.util.ModelRegistry;
import org.logoce.lmf.model.util.tree.Tree;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper for resolving the active meta-model for a document.
 * <p>
 * Resolution order:
 * <ul>
 *   <li>If the header declares {@code metamodels=...}, look up each name in the registry and
 *       return the first {@link MetaModel} found.</li>
 *   <li>If no {@code metamodels} are declared and the semantic root model is itself a
 *       {@link MetaModel}, return it.</li>
 *   <li>If nothing matches, fall back to {@link LMCorePackage#MODEL} so that LMCore editing
 *       remains supported even with an empty registry.</li>
 * </ul>
 */
public final class MetaModelResolver
{
	private static final Logger LOG = LoggerFactory.getLogger(MetaModelResolver.class);

	private MetaModelResolver()
	{
	}

	/**
	 * Resolve all meta-models explicitly declared in the given roots via the
	 * {@code metamodels=} header. This is a narrow, header-only view used by
	 * components that need a list of active meta-models (for example, semantic
	 * linking in the workspace).
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

	public static MetaModel resolveForDocument(final SyntaxSnapshot syntax,
											   final Model semanticModel,
											   final ModelRegistry registry)
	{
		if (syntax != null)
		{
			final List<? extends Tree<PNode>> roots = syntax.roots();
			if (!roots.isEmpty())
			{
				final var rootNode = roots.getFirst().data();
				final var metamodelNames = ModelHeaderUtil.resolveMetamodelNames(rootNode);

				for (final String name : metamodelNames)
				{
					final Model model = registry.getModel(name);
					if (model instanceof MetaModel mm)
					{
						LOG.debug("LMF LSP MetaModelResolver: resolved header metamodel '{}' to {}", name,
								 mm.domain() + "." + mm.name());
						return mm;
					}
				}

				if (!metamodelNames.isEmpty())
				{
					// Header explicitly declares metamodels but none are available in the
					// registry; fall back to LMCore so that basic tooling remains usable.
					LOG.debug("LMF LSP MetaModelResolver: header metamodels {} not found in registry, using LMCore",
							 metamodelNames);
					return LMCorePackage.MODEL;
				}

				// No explicit metamodels declared. For M1-style documents where the root
				// keyword is a concrete group (for example 'CarCompany'), try to resolve
				// the active meta-model by matching that group name against known meta-models.
				if (!ModelHeaderUtil.isMetaModelRoot(roots))
				{
					final String rootKeyword = SyntaxNavigation.headerKeyword(rootNode);
					if (rootKeyword != null && !rootKeyword.isBlank())
					{
						for (final Model model : (Iterable<Model>) registry.models()::iterator)
						{
							if (!(model instanceof MetaModel mm))
							{
								continue;
							}
							final boolean matches = mm.groups()
													 .stream()
													 .anyMatch(g -> rootKeyword.equals(g.name()));
							if (matches)
							{
								LOG.debug("LMF LSP MetaModelResolver: resolved root keyword '{}' to meta-model {}.{}",
										 rootKeyword,
										 mm.domain(),
										 mm.name());
								return mm;
							}
						}
					}
				}

				// No explicit metamodels declared. When the root itself is a MetaModel
				// header, prefer the corresponding MetaModel from the registry so that
				// M2 documents (like CarCompany.lm or TestTypes.lm) use their own types
				// for '@' / '#' completions instead of LMCore.
				if (ModelHeaderUtil.isMetaModelRoot(roots))
				{
					final String domain = ModelHeaderUtil.resolveDomain(rootNode);
					final String name = ModelHeaderUtil.resolveName(rootNode);
					if (domain != null && !domain.isBlank() && name != null && !name.isBlank())
					{
						final Model selfModel = registry.getModel(domain, name);
						if (selfModel instanceof MetaModel mm)
						{
								LOG.debug("LMF LSP MetaModelResolver: resolved self meta-model {}.{} from registry",
									 domain, name);
							return mm;
						}
					}
				}
			}
			else
			{
				// When parsing failed and we have no roots, fall back to a lightweight
				// textual scan of the source to recover `metamodels=` declarations from
				// the header line. This keeps M1 editing usable even for temporarily
				// invalid documents.
				final var source = syntax.source();
				final var namesFromText = parseMetamodelNamesFromSource(source);
				if (!namesFromText.isEmpty())
				{
					for (final String name : namesFromText)
					{
						final Model model = registry.getModel(name);
						if (model instanceof MetaModel mm)
						{
							LOG.debug("LMF LSP MetaModelResolver: resolved header metamodel '{}' (text scan) to {}",
									 name,
									 mm.domain() + "." + mm.name());
							return mm;
						}
					}

					LOG.debug("LMF LSP MetaModelResolver: header metamodels {} (text scan) not found in registry, using LMCore",
							 namesFromText);
					return LMCorePackage.MODEL;
				}
			}
		}

		if (semanticModel instanceof MetaModel mm)
		{
			LOG.debug("LMF LSP MetaModelResolver: using semantic model meta-model {}.{}",
					 mm.domain(), mm.name());
			return mm;
		}

		LOG.debug("LMF LSP MetaModelResolver: falling back to LMCore meta-model");
		return LMCorePackage.MODEL;
	}

	private static java.util.List<String> parseMetamodelNamesFromSource(final CharSequence source)
	{
		if (source == null || source.length() == 0)
		{
			return java.util.List.of();
		}

		final String text = source.toString();
		final String key = "metamodels=";
		final int idx = text.indexOf(key);
		if (idx < 0)
		{
			return java.util.List.of();
		}

		int start = idx + key.length();
		int endLine = text.indexOf('\n', start);
		if (endLine < 0)
		{
			endLine = text.length();
		}

		final String tail = text.substring(start, endLine);
		int end = tail.length();
		for (int i = 0; i < tail.length(); i++)
		{
			final char c = tail.charAt(i);
			if (Character.isWhitespace(c) || c == ')')
			{
				end = i;
				break;
			}
		}

		final String raw = tail.substring(0, end).trim();
		if (raw.isEmpty())
		{
			return java.util.List.of();
		}

		final var result = new java.util.ArrayList<String>();
		for (final String part : raw.split(","))
		{
			final String trimmed = part.trim();
			if (!trimmed.isEmpty())
			{
				result.add(trimmed);
			}
		}
		return java.util.List.copyOf(result);
	}
}
