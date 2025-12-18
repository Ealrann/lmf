package org.logoce.lmf.lsp.workspace;

import org.logoce.lmf.core.api.model.ModelRegistry;
import org.logoce.lmf.core.loader.api.loader.LmLoader;
import org.logoce.lmf.core.loader.api.loader.LmWorkspace;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.loader.api.loader.model.LmDocument;
import org.logoce.lmf.core.loader.api.loader.parsing.LmTreeReader;
import org.logoce.lmf.core.loader.api.loader.parsing.ModelHeaderUtil;
import org.logoce.lmf.lsp.state.LmDocumentState;
import org.logoce.lmf.lsp.state.SemanticSnapshot;
import org.logoce.lmf.lsp.state.SymbolTable;
import org.logoce.lmf.lsp.state.SyntaxSnapshot;
import org.logoce.lmf.lsp.state.WorkspaceIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

final class WorkspaceRegistryManager
{
	private static final Logger LOG = LoggerFactory.getLogger(WorkspaceRegistryManager.class);

	private final WorkspaceIndex workspaceIndex;
	private final SymbolIndexer symbolIndexer;
	private final Path projectRoot;

	private final DiskMetaModelHeaderIndex diskMetaModelHeaderIndex = new DiskMetaModelHeaderIndex();
	private final Map<Path, Long> indexedMetaModelFilesByMtime = new HashMap<>();

	private long generation;
	private Set<String> lastRequiredMetaModelNames = Set.of();
	private List<File> lastRequiredMetaModelFiles = List.of();
	private long lastRequiredMetaModelFilesSignature;
	private LmWorkspace.MetaModelWorkspace lastMetaModelWorkspace;

	WorkspaceRegistryManager(final WorkspaceIndex workspaceIndex,
							 final SymbolIndexer symbolIndexer,
							 final Path projectRoot)
	{
		this.workspaceIndex = Objects.requireNonNull(workspaceIndex, "workspaceIndex");
		this.symbolIndexer = Objects.requireNonNull(symbolIndexer, "symbolIndexer");
		this.projectRoot = projectRoot;
	}

	RegistryUpdate ensureRegistry(final Set<String> requiredMetaModelNames, final boolean forceRebuild)
	{
		if (projectRoot != null)
		{
			return ensureRegistryFromDisk(requiredMetaModelNames);
		}
		if (!forceRebuild)
		{
			return new RegistryUpdate(false, generation, List.of());
		}
		return rebuildRegistryFromOpenDocuments();
	}

	private RegistryUpdate ensureRegistryFromDisk(final Set<String> requiredMetaModelNames)
	{
		final var requiredNames = requiredMetaModelNames == null ? Set.<String>of() : Set.copyOf(requiredMetaModelNames);
		if (requiredNames.isEmpty())
		{
			return new RegistryUpdate(false, generation, List.of());
		}

		try
		{
			final boolean requiredChanged = !requiredNames.equals(lastRequiredMetaModelNames);
			if (requiredChanged || lastRequiredMetaModelFiles.isEmpty())
			{
				diskMetaModelHeaderIndex.refresh(projectRoot);
				lastRequiredMetaModelFiles = diskMetaModelHeaderIndex.resolveMetaModelFilesClosure(new HashSet<>(requiredNames));
				lastRequiredMetaModelNames = requiredNames;
				lastRequiredMetaModelFilesSignature = 0;
				lastMetaModelWorkspace = null;
			}

			if (lastRequiredMetaModelFiles.isEmpty())
			{
				return new RegistryUpdate(false, generation, List.of());
			}

			final long signature = signatureForFiles(lastRequiredMetaModelFiles);
			final boolean filesChanged = signature != lastRequiredMetaModelFilesSignature;
			final boolean needsRebuild = filesChanged || lastMetaModelWorkspace == null;
			boolean registryRebuilt = false;

			if (needsRebuild)
			{
				final var metaModelWorkspace = LmWorkspace.loadMetaModels(lastRequiredMetaModelFiles, workspaceIndex.modelRegistry());
				if (!metaModelWorkspace.files().isEmpty())
				{
					workspaceIndex.setModelRegistry(metaModelWorkspace.registry());
					lastMetaModelWorkspace = metaModelWorkspace;
					lastRequiredMetaModelFilesSignature = signature;
					generation++;
					registryRebuilt = true;
					LOG.debug("LMF LSP registry rebuild: projectRoot={}, metaModels={}, generation={}",
							  projectRoot, metaModelWorkspace.files().size(), generation);
				}
			}

			if (lastMetaModelWorkspace != null)
			{
				indexWorkspaceMetaModels(lastMetaModelWorkspace);
			}

			return new RegistryUpdate(registryRebuilt, generation, List.copyOf(lastRequiredMetaModelFiles));
		}
		catch (Exception e)
		{
			LOG.warn("LMF LSP registry rebuild: error with projectRoot {}, keeping previous registry", projectRoot, e);
			return new RegistryUpdate(false, generation, List.copyOf(lastRequiredMetaModelFiles));
		}
	}

	private RegistryUpdate rebuildRegistryFromOpenDocuments()
	{
		final var docs = workspaceIndex.documents().values();
		if (docs.isEmpty())
		{
			workspaceIndex.setModelRegistry(ModelRegistry.empty());
			generation++;
			return new RegistryUpdate(true, generation, List.of());
		}

		try
		{
			final var documents = new ArrayList<LmDocument>();
			final var reader = new LmTreeReader();

			for (final LmDocumentState docState : docs)
			{
				final String text = docState.text();
				final var diagnostics = new ArrayList<LmDiagnostic>();
				final var readResult = reader.read(text, diagnostics);
				final var roots = readResult.roots();

				if (roots.isEmpty())
				{
					continue;
				}
				if (!ModelHeaderUtil.isMetaModelRoot(roots))
				{
					continue;
				}

				final var doc = new LmDocument(
					null,
					List.copyOf(diagnostics),
					roots,
					readResult.source(),
					List.of());
				documents.add(doc);
			}

			if (documents.isEmpty())
			{
				workspaceIndex.setModelRegistry(ModelRegistry.empty());
				generation++;
				return new RegistryUpdate(true, generation, List.of());
			}

			final var newRegistry = LmLoader.buildRegistry(documents, workspaceIndex.modelRegistry());
			workspaceIndex.setModelRegistry(newRegistry);
			generation++;
			LOG.debug("LMF LSP registry rebuild from open documents: documents={}, generation={}", documents.size(), generation);
			return new RegistryUpdate(true, generation, List.of());
		}
		catch (Exception e)
		{
			LOG.debug("Failed to rebuild model registry from open documents, keeping previous registry: {}",
					  e.getMessage(), e);
			return new RegistryUpdate(false, generation, List.of());
		}
	}

	private void indexWorkspaceMetaModels(final LmWorkspace.MetaModelWorkspace metaModelWorkspace)
	{
		if (metaModelWorkspace == null || metaModelWorkspace.files().isEmpty())
		{
			return;
		}

		final var present = new HashSet<Path>();
		for (final var file : metaModelWorkspace.files())
		{
			if (file == null)
			{
				continue;
			}
			present.add(file.toPath().toAbsolutePath().normalize());
		}

		final var removed = new ArrayList<Path>();
		for (final var path : indexedMetaModelFilesByMtime.keySet())
		{
			if (!present.contains(path))
			{
				removed.add(path);
			}
		}
		for (final var path : removed)
		{
			indexedMetaModelFilesByMtime.remove(path);
			workspaceIndex.clearIndicesForDocument(path.toUri());
		}

		final int count = Math.min(metaModelWorkspace.files().size(), metaModelWorkspace.documents().size());
		for (int i = 0; i < count; i++)
		{
			final var file = metaModelWorkspace.files().get(i);
			final var parsedDoc = metaModelWorkspace.documents().get(i);
			if (file == null || parsedDoc == null)
			{
				continue;
			}

			final var path = file.toPath().toAbsolutePath().normalize();
			final var uri = path.toUri();

			if (workspaceIndex.getDocument(uri) != null)
			{
				continue;
			}

			final long mtime;
			try
			{
				mtime = Files.getLastModifiedTime(path).toMillis();
			}
			catch (Exception e)
			{
				continue;
			}

			final var previousMtime = indexedMetaModelFilesByMtime.get(path);
			final boolean hasIndex = !workspaceIndex.symbolsForUri(uri).isEmpty();
			if (previousMtime != null && previousMtime.longValue() == mtime && hasIndex)
			{
				continue;
			}

			try
			{
				final var diagnostics = new ArrayList<LmDiagnostic>(parsedDoc.diagnostics());
				final var readResult = new LmTreeReader.ReadResult(parsedDoc.roots(), parsedDoc.source());

				final var loader = new LmLoader(workspaceIndex.modelRegistry());
				final var doc = loader.loadModel(readResult, diagnostics);

				final var state = new LmDocumentState(uri, 0, doc.source().toString());
				final var syntaxSnapshot = new SyntaxSnapshot(
					List.of(),
					doc.roots(),
					doc.diagnostics(),
					doc.source());
				state.setSyntaxSnapshot(syntaxSnapshot);
				state.setLastGoodSyntaxSnapshot(syntaxSnapshot);

				final var semanticSnapshot = new SemanticSnapshot(
					doc.model(),
					doc.linkTrees(),
					List.of(),
					SymbolTable.EMPTY,
					List.of());
				state.setSemanticSnapshot(semanticSnapshot);

				symbolIndexer.rebuildIndicesForDocument(state);
				indexedMetaModelFilesByMtime.put(path, mtime);
			}
			catch (Exception e)
			{
				LOG.debug("LMF LSP: failed to index meta-model file {}", uri, e);
				workspaceIndex.clearIndicesForDocument(uri);
			}
		}
	}

	private static long signatureForFiles(final List<File> files) throws IOException
	{
		if (files == null || files.isEmpty())
		{
			return 0;
		}

		final var paths = new ArrayList<Path>(files.size());
		for (final var file : files)
		{
			if (file == null)
			{
				continue;
			}
			paths.add(file.toPath().toAbsolutePath().normalize());
		}
		paths.sort(Path::compareTo);

		long sig = 1;
		for (final var path : paths)
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

			sig = 31 * sig + path.toString().hashCode();
			sig = 31 * sig + mtime;
		}
		return sig;
	}

	record RegistryUpdate(boolean registryRebuilt, long generation, List<File> requiredMetaModelFiles)
	{
	}
}
