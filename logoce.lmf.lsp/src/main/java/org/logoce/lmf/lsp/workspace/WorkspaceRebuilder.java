package org.logoce.lmf.lsp.workspace;

import org.eclipse.lsp4j.services.LanguageClient;
import org.logoce.lmf.core.loader.api.tooling.state.LmDocumentState;
import org.logoce.lmf.core.loader.api.tooling.workspace.DocumentAnalyzer;
import org.logoce.lmf.core.loader.api.tooling.workspace.SymbolIndexer;
import org.logoce.lmf.core.loader.api.tooling.workspace.WorkspaceIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Supplier;

public final class WorkspaceRebuilder
{
	private static final Logger LOG = LoggerFactory.getLogger(WorkspaceRebuilder.class);

	private final WorkspaceIndex workspaceIndex;
	private final Path projectRoot;
	private final WorkspaceDocumentStore documentStore;
	private final WorkspaceRegistryManager registryManager;
	private final DocumentAnalyzer documentAnalyzer;
	private final DiagnosticsPublisher diagnosticsPublisher;

	public WorkspaceRebuilder(final WorkspaceIndex workspaceIndex,
							  final SymbolIndexer symbolIndexer,
							  final Path projectRoot,
							  final Supplier<LanguageClient> clientSupplier)
	{
		this.workspaceIndex = Objects.requireNonNull(workspaceIndex, "workspaceIndex");
		this.projectRoot = projectRoot;
		this.documentStore = new WorkspaceDocumentStore(workspaceIndex);
		this.registryManager = new WorkspaceRegistryManager(workspaceIndex, symbolIndexer, projectRoot);
		this.documentAnalyzer = new DocumentAnalyzer(workspaceIndex, symbolIndexer);
		this.diagnosticsPublisher = new DiagnosticsPublisher(Objects.requireNonNull(clientSupplier, "clientSupplier"));
	}

	/**
	 * Rebuild the workspace model registry and re-analyze all open documents.
	 * This should be called from the worker executor.
	 */
	public void rebuildWorkspace()
	{
		final long startNanos = System.nanoTime();
		final int docCount = workspaceIndex.documents().size();
		try
		{
			documentStore.syncFromWorkspaceIndex();
			registryManager.ensureRegistry(documentStore.requiredMetaModelNames(), true);
			for (final var state : workspaceIndex.documents().values())
			{
				analyzeDocument(state);
			}
		}
		catch (Exception e)
		{
			// Treat workspace rebuild issues as internal warnings; per-file
			// diagnostics are surfaced via analyzeDocument instead of global
			// client notifications.
			LOG.warn("Error while rebuilding workspace: {}", e.getMessage(), e);
		}
		finally
		{
			final long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
			if (durationMs > 500)
			{
				LOG.info("LMF LSP rebuildWorkspace: documents={}, durationMs={}", docCount, durationMs);
			}
		}
	}

	public void openDocument(final URI uri, final int version, final String text)
	{
		final var change = documentStore.openDocument(uri, version, text);
		final var update = registryManager.ensureRegistry(documentStore.requiredMetaModelNames(), change.metaModelDocumentTouched());

		if (update.registryRebuilt())
		{
			for (final var state : workspaceIndex.documents().values())
			{
				analyzeDocument(state);
			}
			return;
		}
		analyzeDocument(change.state());
	}

	public void updateDocument(final URI uri, final int version, final String text)
	{
		final var change = documentStore.updateDocument(uri, version, text);

		final boolean shouldEnsureRegistry = projectRoot == null
											 ? change.metaModelDocumentTouched()
											 : change.requiredMetaModelsChanged();
		if (shouldEnsureRegistry)
		{
			final var update = registryManager.ensureRegistry(documentStore.requiredMetaModelNames(), change.metaModelDocumentTouched());
			if (update.registryRebuilt())
			{
				for (final var state : workspaceIndex.documents().values())
				{
					analyzeDocument(state);
				}
				return;
			}
		}

		analyzeDocument(change.state());
	}

	public void closeDocument(final URI uri)
	{
		final var change = documentStore.closeDocument(uri);

		final boolean shouldEnsureRegistry = projectRoot != null || change.metaModelDocumentTouched();
		if (!shouldEnsureRegistry)
		{
			return;
		}

		final var update = registryManager.ensureRegistry(documentStore.requiredMetaModelNames(), change.metaModelDocumentTouched());
		if (update.registryRebuilt())
		{
			for (final var state : workspaceIndex.documents().values())
			{
				analyzeDocument(state);
			}
		}
	}

	/**
	 * Analyze the given document: run syntax and semantic pipelines and publish diagnostics.
	 * This method must be invoked from the worker executor.
	 */
	public void analyzeDocument(final LmDocumentState state)
	{
		documentAnalyzer.analyzeDocument(state);
		diagnosticsPublisher.publish(state);
	}
}
