package org.logoce.lmf.lsp;

import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.SemanticTokenTypes;
import org.eclipse.lsp4j.SemanticTokensLegend;
import org.eclipse.lsp4j.SemanticTokensServerFull;
import org.eclipse.lsp4j.SemanticTokensWithRegistrationOptions;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.logoce.lmf.core.loader.api.loader.util.TextPositions;
import org.logoce.lmf.core.loader.api.tooling.state.LmDocumentState;
import org.logoce.lmf.core.loader.api.tooling.state.SymbolId;
import org.logoce.lmf.core.loader.api.tooling.workspace.SymbolIndexer;
import org.logoce.lmf.core.loader.api.tooling.workspace.WorkspaceIndex;
import org.logoce.lmf.lsp.workspace.WorkspaceRebuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

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
	private volatile Boolean semanticTokensRefreshSupport;
	private final AtomicLong semanticTokensRefreshId = new AtomicLong();
	private volatile long lastSemanticTokensRefreshNanos;

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

	public void analyzeDocument(final LmDocumentState state)
	{
		workspaceRebuilder.analyzeDocument(state);
	}

	public void openDocument(final java.net.URI uri, final int version, final String text)
	{
		workspaceRebuilder.openDocument(uri, version, text);
	}

	public void updateDocument(final java.net.URI uri, final int version, final String text)
	{
		workspaceRebuilder.updateDocument(uri, version, text);
	}

	public void closeDocument(final java.net.URI uri)
	{
		workspaceRebuilder.closeDocument(uri);
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
			if (spanContains(ref.span(), position))
			{
				return ref.target();
			}
		}

		for (final var entry : workspaceIndex.symbolsForUri(uri))
		{
			if (spanContains(entry.span(), position))
			{
				return entry.id();
			}
		}

		return null;
	}

	private static boolean spanContains(final TextPositions.Span span, final Position pos)
	{
		if (span == null || pos == null)
		{
			return false;
		}

		final int line = Math.max(0, span.line() - 1);
		final int startChar = Math.max(0, span.column() - 1);
		final int endChar = startChar + Math.max(1, span.length());

		if (pos.getLine() != line)
		{
			return false;
		}
		return pos.getCharacter() >= startChar && pos.getCharacter() <= endChar;
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
	public void initialized(final InitializedParams params)
	{
		LOG.info("LMF LSP initialized");
	}

	@Override
	public CompletableFuture<InitializeResult> initialize(final InitializeParams params)
	{
		final var refreshSupport = readSemanticTokensRefreshSupport(params);
		semanticTokensRefreshSupport = refreshSupport;

		LOG.info("LMF LSP initialize: clientId={}, projectRoot={}",
				 params.getClientInfo() != null ? params.getClientInfo().getName() : "unknown",
				 projectRoot);
		LOG.info("LMF LSP client capabilities: semanticTokensRefreshSupport={}", refreshSupport);

		final var capabilities = new ServerCapabilities();
		capabilities.setTextDocumentSync(TextDocumentSyncKind.Incremental);
		capabilities.setCompletionProvider(new CompletionOptions());
		capabilities.setDefinitionProvider(true);
		capabilities.setReferencesProvider(true);
		capabilities.setDocumentHighlightProvider(true);
		capabilities.setHoverProvider(true);
		capabilities.setDocumentSymbolProvider(true);
		capabilities.setRenameProvider(true);
		capabilities.setFoldingRangeProvider(true);

		// Semantic tokens: keep a minimal legend where type-like names are reported
		// as 'keyword', and Named values are reported as 'string'.
		final var legend = new SemanticTokensLegend(
			java.util.List.of(SemanticTokenTypes.Keyword, SemanticTokenTypes.String),
			java.util.List.of()
		);
		final var full = new SemanticTokensServerFull();
		full.setDelta(false);
		final var semanticOptions = new SemanticTokensWithRegistrationOptions(legend, full, false);
		capabilities.setSemanticTokensProvider(semanticOptions);

		LOG.info("LMF LSP capabilities: textSync=Incremental, completion=true, definition=true, "
				 + "references=true, hover=true, documentSymbol=true, rename=true, semanticTokens=true");

		final var result = new InitializeResult(capabilities);
		return CompletableFuture.completedFuture(result);
	}

	public void refreshSemanticTokensIfSupported(final String reason)
	{
		final LanguageClient currentClient = client;
		if (currentClient == null)
		{
			return;
		}

		final Boolean refreshSupport = semanticTokensRefreshSupport;
		if (Boolean.FALSE.equals(refreshSupport))
		{
			return;
		}

		final long now = System.nanoTime();
		final long last = lastSemanticTokensRefreshNanos;
		if (last != 0 && (now - last) < TimeUnit.MILLISECONDS.toNanos(250))
		{
			return;
		}
		lastSemanticTokensRefreshNanos = now;

		final long id = semanticTokensRefreshId.incrementAndGet();
		LOG.info("LMF LSP semanticTokensRefresh: id={}, reason={}", id, reason);

		currentClient.refreshSemanticTokens().whenComplete((ignored, error) -> {
			if (error != null)
			{
				if (refreshSupport == null)
				{
					semanticTokensRefreshSupport = Boolean.FALSE;
				}
				LOG.warn("LMF LSP semanticTokensRefresh failed: id={}, reason={}, error={}",
						 id, reason, error.getClass().getName() + ": " + error.getMessage());
				return;
			}
			if (refreshSupport == null)
			{
				semanticTokensRefreshSupport = Boolean.TRUE;
			}
		});
	}

	private static Boolean readSemanticTokensRefreshSupport(final InitializeParams params)
	{
		if (params == null || params.getCapabilities() == null)
		{
			return null;
		}
		final var workspace = params.getCapabilities().getWorkspace();
		if (workspace == null)
		{
			return null;
		}
		final var semanticTokens = workspace.getSemanticTokens();
		if (semanticTokens == null)
		{
			return null;
		}
		return semanticTokens.getRefreshSupport();
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
