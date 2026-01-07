package org.logoce.lmf.core.loader.api.tooling.workspace;

import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.loader.api.loader.parsing.LmTreeReader;
import org.logoce.lmf.core.loader.api.loader.parsing.ModelHeaderUtil;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;
import org.logoce.lmf.core.loader.api.tooling.HeaderTextScanner;
import org.logoce.lmf.core.util.tree.Tree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Indexes basic model headers from disk (qualified name, imports, metamodels) for workspace tooling.
 * <p>
 * Unlike {@link DiskMetaModelHeaderIndex}, this index targets all {@code .lm} model files (M1 and M2),
 * and is intended for workspace-level graph queries such as "who imports what".
 */
public final class DiskModelHeaderIndex
{
	private final Map<Path, Long> modelFilesByMtime = new HashMap<>();
	private final Map<Path, DiskModelHeader> headersByPath = new HashMap<>();

	private final Map<String, List<DiskModelHeader>> headersByQualifiedName = new HashMap<>();
	private final Map<String, List<DiskModelHeader>> importersByImportedModel = new HashMap<>();

	public void refresh(final Path root) throws IOException
	{
		final var present = new HashSet<Path>();
		try (final var paths = Files.walk(root))
		{
			paths.filter(Files::isRegularFile)
				 .filter(p -> p.getFileName().toString().endsWith(".lm"))
				 .forEach(present::add);
		}

		final var removed = new ArrayList<Path>();
		for (final var path : modelFilesByMtime.keySet())
		{
			if (!present.contains(path))
			{
				removed.add(path);
			}
		}
		for (final var path : removed)
		{
			modelFilesByMtime.remove(path);
			headersByPath.remove(path);
		}

		final var reader = new LmTreeReader();
		for (final var path : present)
		{
			final long mtime;
			try
			{
				mtime = Files.getLastModifiedTime(path).toMillis();
			}
			catch (Exception e)
			{
				continue;
			}

			final var previous = modelFilesByMtime.get(path);
			if (previous != null && previous.longValue() == mtime)
			{
				continue;
			}

			try
			{
				final var text = Files.readString(path);
				final var diagnostics = new ArrayList<LmDiagnostic>();
				final var readResult = reader.read(text, diagnostics);
				final var roots = readResult.roots();

				final DiskModelHeader header = buildHeader(path, roots, readResult.source());
				if (header == null)
				{
					headersByPath.remove(path);
				}
				else
				{
					headersByPath.put(path, header);
				}
			}
			catch (Exception e)
			{
				headersByPath.remove(path);
			}
			finally
			{
				modelFilesByMtime.put(path, mtime);
			}
		}

		rebuildIndices();
	}

	public DiskModelHeader headerForPath(final Path path)
	{
		return headersByPath.get(path);
	}

	public List<DiskModelHeader> headersByQualifiedName(final String qualifiedName)
	{
		if (qualifiedName == null)
		{
			return List.of();
		}
		final var headers = headersByQualifiedName.get(qualifiedName);
		return headers == null ? List.of() : headers;
	}

	public List<DiskModelHeader> importersOf(final String qualifiedName)
	{
		if (qualifiedName == null)
		{
			return List.of();
		}
		final var importers = importersByImportedModel.get(qualifiedName);
		return importers == null ? List.of() : importers;
	}

	public List<DiskModelHeader> headers()
	{
		return List.copyOf(headersByPath.values());
	}

	private void rebuildIndices()
	{
		headersByQualifiedName.clear();
		importersByImportedModel.clear();

		for (final var header : headersByPath.values())
		{
			if (header.qualifiedName() != null)
			{
				headersByQualifiedName.computeIfAbsent(header.qualifiedName(), k -> new ArrayList<>()).add(header);
			}
		}

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
				importersByImportedModel.computeIfAbsent(imp, k -> new ArrayList<>()).add(header);
			}
		}

		headersByQualifiedName.replaceAll((k, v) -> List.copyOf(v));
		importersByImportedModel.replaceAll((k, v) -> List.copyOf(v));
	}

	private static DiskModelHeader buildHeader(final Path path, final List<Tree<PNode>> roots, final CharSequence source)
	{
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
			return new DiskModelHeader(path, metaModelRoot, domain, name, qualifiedName, imports, metamodels);
		}

		final boolean metaModelRoot = HeaderTextScanner.isMetaModelRoot(source);
		final String qualifiedName = metaModelRoot ? HeaderTextScanner.parseMetaModelQualifiedName(source) : null;
		final var metamodels = List.copyOf(HeaderTextScanner.parseMetamodelNames(source));
		return new DiskModelHeader(path, metaModelRoot, null, null, qualifiedName, List.of(), metamodels);
	}

	public record DiskModelHeader(Path path,
								  boolean metaModelRoot,
								  String domain,
								  String name,
								  String qualifiedName,
								  List<String> imports,
								  List<String> metamodels)
	{
	}
}

