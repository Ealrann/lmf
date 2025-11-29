package org.logoce.lmf.lsp;

import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.logoce.lmf.lsp.workspace.SymbolIndexer;
import org.logoce.lmf.lsp.workspace.WorkspaceRebuilder;
import org.logoce.lmf.lsp.state.WorkspaceIndex;
import org.logoce.lmf.lsp.state.LmSymbolKind;
import org.logoce.lmf.lsp.state.ModelKey;
import org.logoce.lmf.lsp.state.SymbolId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.List;

public final class LmLanguageServer implements LanguageServer, LanguageClientAware
{
	private static final Logger LOG = LoggerFactory.getLogger(LmLanguageServer.class);

	private final ExecutorService worker;
	private final WorkspaceIndex workspaceIndex;
	private final LmTextDocumentService textDocumentService;
	private final LmWorkspaceService workspaceService;
	private final Path projectRoot;
	private final WorkspaceRebuilder workspaceRebuilder;

	private volatile LanguageClient client;
	private volatile Settings settings = Settings.defaults();

	public LmLanguageServer()
	{
		this(null);
	}

	public LmLanguageServer(final Path projectRoot)
	{
		this.worker = Executors.newSingleThreadExecutor(r -> {
			final Thread t = new Thread(r, "lm-lsp-worker");
			t.setDaemon(true);
			return t;
		});
		this.workspaceIndex = new WorkspaceIndex();
		this.textDocumentService = new LmTextDocumentService(this);
		this.workspaceService = new LmWorkspaceService(this);
		this.projectRoot = projectRoot;
		final var symbolIndexer = new SymbolIndexer(this.workspaceIndex);
		this.workspaceRebuilder = new WorkspaceRebuilder(this.workspaceIndex, symbolIndexer, this.projectRoot, this::client);
	}

	@Override
	public void connect(final LanguageClient client)
	{
		this.client = Objects.requireNonNull(client, "client");
	}

	public LanguageClient client()
	{
		return client;
	}

	public ExecutorService worker()
	{
		return worker;
	}

	public WorkspaceIndex workspaceIndex()
	{
		return workspaceIndex;
	}

	public Settings settings()
	{
		return settings;
	}

	public void updateSettings(final Settings newSettings)
	{
		if (newSettings != null)
		{
			this.settings = newSettings;
			LOG.info("Updated settings: {}", newSettings);
		}
	}

	/**
	 * Find the symbol id at the given URI and position, first using references and
	 * then falling back to declarations in the same document.
	 */
	public SymbolId findTargetSymbol(final java.net.URI uri, final Position position)
	{
		for (final var ref : workspaceIndex.referencesForUri(uri))
		{
			if (rangeContains(ref.range(), position))
			{
				return ref.target();
			}
		}

		for (final var entry : workspaceIndex.symbolsForUri(uri))
		{
			if (rangeContains(entry.range(), position))
			{
				return entry.id();
			}
		}

		return null;
	}

	private static boolean rangeContains(final org.eclipse.lsp4j.Range range, final Position pos)
	{
		final Position start = range.getStart();
		final Position end = range.getEnd();

		if (pos.getLine() < start.getLine() || pos.getLine() > end.getLine())
		{
			return false;
		}
		if (pos.getLine() == start.getLine() && pos.getCharacter() < start.getCharacter())
		{
			return false;
		}
		if (pos.getLine() == end.getLine() && pos.getCharacter() > end.getCharacter())
		{
			return false;
		}
		return true;
	}

	/**
	 * Rebuild the workspace model registry and re-analyze all open documents.
	 * This should be called from the worker executor.
	 */
	public void rebuildWorkspace()
	{
		workspaceRebuilder.rebuildWorkspace();
	}

	@Override
	public CompletableFuture<InitializeResult> initialize(final InitializeParams params)
	{
		LOG.info("LMF LSP initialize: clientId={}, rootUri={}, projectRoot={}",
				 params.getClientInfo() != null ? params.getClientInfo().getName() : "unknown",
				 params.getRootUri(),
				 projectRoot);

		final var capabilities = new ServerCapabilities();
		capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
		capabilities.setCompletionProvider(new CompletionOptions());
		capabilities.setDefinitionProvider(true);
		capabilities.setReferencesProvider(true);
		capabilities.setHoverProvider(true);
		capabilities.setDocumentSymbolProvider(true);
		capabilities.setRenameProvider(true);

		LOG.info("LMF LSP capabilities: textSync=Full, completion=true, definition=true, "
				 + "references=true, hover=true, documentSymbol=true, rename=true");

		final var result = new InitializeResult(capabilities);
		return CompletableFuture.completedFuture(result);
	}

	@Override
	public CompletableFuture<Object> shutdown()
	{
		LOG.info("Shutting down LMF LSP server");
		worker.shutdown();
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public void exit()
	{
		LOG.info("Exiting LMF LSP server");
	}

	@Override
	public TextDocumentService getTextDocumentService()
	{
		return textDocumentService;
	}

	@Override
	public WorkspaceService getWorkspaceService()
	{
		return workspaceService;
	}
}
