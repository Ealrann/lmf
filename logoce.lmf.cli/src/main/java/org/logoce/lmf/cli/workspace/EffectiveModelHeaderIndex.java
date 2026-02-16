package org.logoce.lmf.cli.workspace;

import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.loader.api.loader.parsing.LmTreeReader;
import org.logoce.lmf.core.loader.api.loader.parsing.ModelHeaderUtil;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;
import org.logoce.lmf.core.loader.api.tooling.HeaderTextScanner;
import org.logoce.lmf.core.loader.api.tooling.workspace.DiskModelHeaderIndex;
import org.logoce.lmf.core.util.tree.Tree;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class EffectiveModelHeaderIndex
{
	private final Map<Path, DiskModelHeaderIndex.DiskModelHeader> headersByPath;
	private final Map<String, List<DiskModelHeaderIndex.DiskModelHeader>> headersByQualifiedName;
	private final Map<String, List<DiskModelHeaderIndex.DiskModelHeader>> importersByImportedModel;

	private EffectiveModelHeaderIndex(final Map<Path, DiskModelHeaderIndex.DiskModelHeader> headersByPath,
									  final Map<String, List<DiskModelHeaderIndex.DiskModelHeader>> headersByQualifiedName,
									  final Map<String, List<DiskModelHeaderIndex.DiskModelHeader>> importersByImportedModel)
	{
		this.headersByPath = headersByPath;
		this.headersByQualifiedName = headersByQualifiedName;
		this.importersByImportedModel = importersByImportedModel;
	}

	static EffectiveModelHeaderIndex build(final DiskModelHeaderIndex diskIndex,
										  final DocumentLoader documentLoader,
										  final PrintWriter err)
	{
		Objects.requireNonNull(diskIndex, "diskIndex");
		Objects.requireNonNull(documentLoader, "documentLoader");
		Objects.requireNonNull(err, "err");

		final var headersByPath = new HashMap<Path, DiskModelHeaderIndex.DiskModelHeader>();
		for (final var header : diskIndex.headers())
		{
			if (header.path() == null)
			{
				continue;
			}
			headersByPath.put(header.path().toAbsolutePath().normalize(), header);
		}

		for (final var overlayPath : documentLoader.overlayPaths())
		{
			if (overlayPath == null)
			{
				continue;
			}
			final var normalized = overlayPath.toAbsolutePath().normalize();
			final var source = documentLoader.readString(normalized, err);
			if (source == null)
			{
				headersByPath.remove(normalized);
				continue;
			}

			final var header = parseHeader(normalized, source);
			if (header == null)
			{
				headersByPath.remove(normalized);
			}
			else
			{
				headersByPath.put(normalized, header);
			}
		}

		final var headersByQualifiedName = new HashMap<String, List<DiskModelHeaderIndex.DiskModelHeader>>();
		for (final var header : headersByPath.values())
		{
			if (header.qualifiedName() == null)
			{
				continue;
			}
			headersByQualifiedName.computeIfAbsent(header.qualifiedName(), ignored -> new ArrayList<>()).add(header);
		}

		final var importersByImportedModel = new HashMap<String, List<DiskModelHeaderIndex.DiskModelHeader>>();
		for (final var header : headersByPath.values())
		{
			if (header.imports() == null || header.imports().isEmpty())
			{
				continue;
			}
			for (final var imp : header.imports())
			{
				if (imp == null || imp.isBlank())
				{
					continue;
				}
				importersByImportedModel.computeIfAbsent(imp, ignored -> new ArrayList<>()).add(header);
			}
		}

		headersByQualifiedName.replaceAll((ignored, v) -> List.copyOf(v));
		importersByImportedModel.replaceAll((ignored, v) -> List.copyOf(v));
		return new EffectiveModelHeaderIndex(Map.copyOf(headersByPath),
											 Map.copyOf(headersByQualifiedName),
											 Map.copyOf(importersByImportedModel));
	}

	DiskModelHeaderIndex.DiskModelHeader headerForPath(final Path path)
	{
		if (path == null)
		{
			return null;
		}

		final var normalized = path.toAbsolutePath().normalize();
		final var header = headersByPath.get(normalized);
		if (header != null)
		{
			return header;
		}
		return headersByPath.get(path);
	}

	List<DiskModelHeaderIndex.DiskModelHeader> headersByQualifiedName(final String qualifiedName)
	{
		if (qualifiedName == null)
		{
			return List.of();
		}
		final var headers = headersByQualifiedName.get(qualifiedName);
		return headers == null ? List.of() : headers;
	}

	List<DiskModelHeaderIndex.DiskModelHeader> importersOf(final String qualifiedName)
	{
		if (qualifiedName == null)
		{
			return List.of();
		}
		final var importers = importersByImportedModel.get(qualifiedName);
		return importers == null ? List.of() : importers;
	}

	private static DiskModelHeaderIndex.DiskModelHeader parseHeader(final Path path, final CharSequence source)
	{
		final var diagnostics = new ArrayList<LmDiagnostic>();
		final var readResult = new LmTreeReader().read(source, diagnostics);

		final List<Tree<PNode>> roots = readResult.roots();
		if (roots != null && !roots.isEmpty())
		{
			final boolean metaModelRoot = ModelHeaderUtil.isMetaModelRoot(roots);
			final var rootNode = roots.getFirst().data();

			final var domain = ModelHeaderUtil.resolveDomain(rootNode);
			final String name;
			try
			{
				name = ModelHeaderUtil.resolveName(rootNode);
			}
			catch (IllegalStateException e)
			{
				return null;
			}

			final var qualifiedName = domain == null || domain.isBlank() ? name : domain + "." + name;
			final var imports = List.copyOf(ModelHeaderUtil.resolveImports(rootNode));
			final var metamodels = List.copyOf(ModelHeaderUtil.resolveMetamodelNames(rootNode));
			return new DiskModelHeaderIndex.DiskModelHeader(path, metaModelRoot, domain, name, qualifiedName, imports, metamodels);
		}

		final boolean metaModelRoot = HeaderTextScanner.isMetaModelRoot(source);
		final String qualifiedName = metaModelRoot ? HeaderTextScanner.parseMetaModelQualifiedName(source) : null;
		final var metamodels = List.copyOf(HeaderTextScanner.parseMetamodelNames(source));
		return new DiskModelHeaderIndex.DiskModelHeader(path, metaModelRoot, null, null, qualifiedName, List.of(), metamodels);
	}
}

