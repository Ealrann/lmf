package org.logoce.lmf.cli.workspace;

import org.logoce.lmf.core.loader.api.loader.parsing.ModelHeaderUtil;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;
import org.logoce.lmf.core.loader.api.tooling.HeaderTextScanner;
import org.logoce.lmf.core.util.tree.Tree;

import java.util.List;

public record ModelHeader(boolean metaModelRoot,
						  String qualifiedName,
						  List<String> metamodels,
						  List<String> imports)
{
	public static ModelHeader from(final List<? extends Tree<PNode>> roots, final CharSequence source)
	{
		if (roots != null && !roots.isEmpty())
		{
			final var metaModelRoot = ModelHeaderUtil.isMetaModelRoot(roots);
			final var rootNode = roots.getFirst().data();
			final var qualifiedName = resolveQualifiedName(rootNode);
			final var metamodels = List.copyOf(ModelHeaderUtil.resolveMetamodelNames(rootNode));
			final var imports = metaModelRoot
								? List.copyOf(ModelHeaderUtil.resolveImports(rootNode))
								: List.<String>of();
			return new ModelHeader(metaModelRoot, qualifiedName, metamodels, imports);
		}

		final var metaModelRoot = HeaderTextScanner.isMetaModelRoot(source);
		final var qualifiedName = metaModelRoot ? HeaderTextScanner.parseMetaModelQualifiedName(source) : null;
		final var metamodels = List.copyOf(HeaderTextScanner.parseMetamodelNames(source));
		return new ModelHeader(metaModelRoot, qualifiedName, metamodels, List.of());
	}

	public List<String> requiredMetaModels()
	{
		return metaModelRoot ? imports : metamodels;
	}

	private static String resolveQualifiedName(final PNode rootNode)
	{
		final String domain = ModelHeaderUtil.resolveDomain(rootNode);
		final String name = resolveName(rootNode);
		if (name == null || name.isBlank())
		{
			return null;
		}
		if (domain == null || domain.isBlank())
		{
			return name;
		}
		return domain + "." + name;
	}

	private static String resolveName(final PNode rootNode)
	{
		try
		{
			return ModelHeaderUtil.resolveName(rootNode);
		}
		catch (IllegalStateException e)
		{
			return null;
		}
	}
}
