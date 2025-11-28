package org.logoce.lmf.lsp;

import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.logoce.lmf.lsp.state.WorkspaceIndex;
import org.logoce.lmf.lsp.state.LmDocumentState;
import org.logoce.lmf.lsp.state.SemanticSnapshot;
import org.logoce.lmf.lsp.state.SymbolTable;
import org.logoce.lmf.lsp.state.SyntaxSnapshot;
import org.logoce.lmf.lsp.state.ModelKey;
import org.logoce.lmf.lsp.state.LmSymbolKind;
import org.logoce.lmf.lsp.state.SymbolId;
import org.logoce.lmf.lsp.state.SymbolEntry;
import org.logoce.lmf.lsp.state.ReferenceOccurrence;
import org.logoce.lmf.lsp.features.DocumentSymbols;
import org.logoce.lmf.model.lang.LMCorePackage;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.model.loader.LmLoader;
import org.logoce.lmf.model.loader.linking.LmModelLinker;
import org.logoce.lmf.model.loader.parsing.LmTreeReader;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.resource.parsing.PToken;
import org.logoce.lmf.model.util.ModelRegistry;
import org.logoce.lmf.model.util.TextPositions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
		try
		{
			rebuildModelRegistry();
			for (final var state : workspaceIndex.documents().values())
			{
				analyzeDocument(state);
			}
		}
		catch (Exception e)
		{
			LOG.error("Error while rebuilding workspace", e);
			final var client = client();
			if (client != null)
			{
				client.logMessage(new org.eclipse.lsp4j.MessageParams(
					org.eclipse.lsp4j.MessageType.Error,
					"LMF LSP: error while rebuilding workspace: " + e.getMessage()));
			}
		}
	}

	private void rebuildModelRegistry()
	{
		if (projectRoot != null)
		{
			try
			{
				final var models = loadModelsFromProjectRoot(projectRoot);
				final var builder = new ModelRegistry.Builder(ModelRegistry.empty());
				for (final var model : models)
				{
					builder.register(model);
				}
				workspaceIndex.setModelRegistry(builder.build());
				LOG.info("LMF LSP rebuildModelRegistry: projectRoot={}, models={}", projectRoot, models.size());
			}
			catch (Exception e)
			{
				LOG.warn("LMF LSP rebuildModelRegistry: error with projectRoot {}, falling back to empty", projectRoot, e);
				workspaceIndex.setModelRegistry(ModelRegistry.empty());
			}
			return;
		}

		final var docs = workspaceIndex.documents().values();
		if (docs.isEmpty())
		{
			workspaceIndex.setModelRegistry(ModelRegistry.empty());
			return;
		}

		final var inputs = new ArrayList<java.io.InputStream>();
		for (final var doc : docs)
		{
			final byte[] bytes = doc.text().getBytes(StandardCharsets.UTF_8);
			inputs.add(new ByteArrayInputStream(bytes));
		}

		try
		{
			final var loader = LmLoader.withEmptyRegistry();
			final var models = loader.loadModels(inputs);

			final var builder = new ModelRegistry.Builder(ModelRegistry.empty());
			for (final var model : models)
			{
				builder.register(model);
			}
			workspaceIndex.setModelRegistry(builder.build());
			LOG.info("LMF LSP rebuildModelRegistry: from open documents, models={}", models.size());
		}
		catch (Exception e)
		{
			LOG.warn("Failed to rebuild model registry from open documents, falling back to empty", e);
			workspaceIndex.setModelRegistry(ModelRegistry.empty());
		}
	}

	private List<Model> loadModelsFromProjectRoot(final Path root) throws Exception
	{
		final var inputs = new ArrayList<java.io.InputStream>();
		try (final var paths = Files.walk(root))
		{
			paths.filter(Files::isRegularFile)
				 .filter(p -> p.getFileName().toString().endsWith(".lm"))
				 .forEach(p -> {
					 try
					 {
						 final byte[] bytes = Files.readAllBytes(p);
						 inputs.add(new ByteArrayInputStream(bytes));
					 }
					 catch (Exception e)
					 {
						 LOG.warn("LMF LSP rebuildModelRegistry: cannot read model file {}", p, e);
					 }
				 });
		}

		if (inputs.isEmpty())
		{
			return List.of();
		}

		final var loader = LmLoader.withEmptyRegistry();
		return loader.loadModels(inputs);
	}

	/**
	 * Analyze the given document: run syntax and semantic pipelines and publish diagnostics.
	 * This method must be invoked from the worker executor.
	 */
	public void analyzeDocument(final LmDocumentState state)
	{
		try
		{
			LOG.info("LMF LSP analyzeDocument start: uri={}, textLength={}", state.uri(), state.text().length());

			final var text = state.text();

			final var syntaxDiagnostics = new ArrayList<LmDiagnostic>();
			final var treeReader = new LmTreeReader();
			final var readResult = treeReader.read(text, syntaxDiagnostics);
			final var roots = readResult.roots();
			final var source = readResult.source();

			// Tokens are optional for now; they can be populated later if needed.
			final var syntaxSnapshot = new SyntaxSnapshot(List.of(), roots, syntaxDiagnostics, source);
			state.setSyntaxSnapshot(syntaxSnapshot);

			final var semanticDiagnostics = new ArrayList<LmDiagnostic>();
			Model model = null;
			List<? extends org.logoce.lmf.model.loader.linking.LinkNode<?, PNode>> linkTrees = List.of();

			if (roots.isEmpty() == false)
			{
				final var linker = new LmModelLinker<PNode>(workspaceIndex.modelRegistry());
				final var linkResult = linker.linkModel(roots, semanticDiagnostics, source);
				model = linkResult.model();
				linkTrees = linkResult.trees();
			}

			final var semanticSnapshot = new SemanticSnapshot(model,
															  linkTrees,
															  semanticDiagnostics,
															  SymbolTable.EMPTY,
															  List.of());
			state.setSemanticSnapshot(semanticSnapshot);

			rebuildIndicesForDocument(state);

			publishDiagnostics(state);

			final int syntaxCount = syntaxDiagnostics.size();
			final int semanticCount = semanticDiagnostics.size();
			final String modelKind = model == null ? "null" : model.getClass().getSimpleName();
			LOG.info("LMF LSP analyzeDocument done: uri={}, model={}, syntaxDiag={}, semanticDiag={}",
					 state.uri(), modelKind, syntaxCount, semanticCount);
		}
		catch (Exception e)
		{
			LOG.error("Error while analyzing document {}", state.uri(), e);
			final var client = client();
			if (client != null)
			{
				client.logMessage(new org.eclipse.lsp4j.MessageParams(
					org.eclipse.lsp4j.MessageType.Error,
					"LMF LSP: error while analyzing " + state.uri() + ": " + e.getMessage()));
			}
		}
	}

	private void rebuildIndicesForDocument(final LmDocumentState state)
	{
		final var syntax = state.syntaxSnapshot();
		final var semantic = state.semanticSnapshot();
		if (syntax == null || semantic == null)
		{
			workspaceIndex.clearIndicesForDocument(state.uri());
			return;
		}

		final var model = semantic.model();
		if (!(model instanceof MetaModel mm))
		{
			workspaceIndex.clearIndicesForDocument(state.uri());
			return;
		}

		final var modelKey = new ModelKey(mm.domain(), mm.name());

		final var documentSymbols = DocumentSymbols.buildDocumentSymbols(syntax);
		final var symbolEntries = new ArrayList<SymbolEntry>();

		for (final var either : documentSymbols)
		{
			if (either.isRight())
			{
				collectSymbolEntries(modelKey, either.getRight(), symbolEntries, state.uri());
			}
		}

		workspaceIndex.registerSymbols(state.uri(), symbolEntries);

		final var references = buildReferences(modelKey, mm, syntax, state.uri());
		workspaceIndex.registerReferences(state.uri(), references);
	}

	private static void collectSymbolEntries(final ModelKey modelKey,
											 final DocumentSymbol symbol,
											 final List<SymbolEntry> out,
											 final java.net.URI uri)
	{
		final var kind = toSymbolKind(symbol);
		if (kind != null)
		{
			final var id = new SymbolId(modelKey, kind, symbol.getName());
			out.add(new SymbolEntry(id, uri, symbol.getRange()));
		}

		final var children = symbol.getChildren();
		if (children != null)
		{
			for (final var child : children)
			{
				collectSymbolEntries(modelKey, child, out, uri);
			}
		}
	}

	private List<ReferenceOccurrence> buildReferences(final ModelKey modelKey,
													  final MetaModel model,
													  final SyntaxSnapshot syntax,
													  final java.net.URI uri)
	{
		final var references = new ArrayList<ReferenceOccurrence>();
		final var source = syntax.source();
		final var registry = workspaceIndex.modelRegistry();

		for (final var root : syntax.roots())
		{
			collectReferencesInNode(root, modelKey, model, source, uri, registry, references);
		}

		return List.copyOf(references);
	}

	private void collectReferencesInNode(final org.logoce.lmf.model.util.tree.Tree<PNode> node,
										 final ModelKey modelKey,
										 final MetaModel currentModel,
										 final CharSequence source,
										 final java.net.URI uri,
										 final ModelRegistry registry,
										 final List<ReferenceOccurrence> out)
	{
		final var tokens = node.data().tokens();
		for (final var token : tokens)
		{
			final var value = token.value();
			if (value == null || value.isEmpty())
			{
				continue;
			}
			final char first = value.charAt(0);
			if (first == '@')
			{
				final var typeName = value.substring(1);
				if (!typeName.isEmpty())
				{
					final var id = new SymbolId(modelKey, LmSymbolKind.TYPE, typeName);
					if (workspaceIndex.symbolIndex().containsKey(id))
					{
						final var range = rangeForToken(token, source);
						out.add(new ReferenceOccurrence(id, uri, range));
					}
				}
			}
			else if (first == '#')
			{
				final var parsed = org.logoce.lmf.model.loader.linking.feature.reference.PathUtil.parse(value);
				String modelName = null;
				String targetType = null;

				for (final var segment : parsed.segments())
				{
					if (segment.type() == org.logoce.lmf.model.loader.linking.feature.reference.PathParser.Type.MODEL && modelName == null)
					{
						modelName = segment.text();
					}
					else if (segment.type() == org.logoce.lmf.model.loader.linking.feature.reference.PathParser.Type.NAME)
					{
						targetType = segment.text();
					}
				}

				if (modelName != null && targetType != null)
				{
					ModelKey targetKey = resolveModelAlias(currentModel, modelName, registry);
					if (targetKey == null)
					{
						targetKey = new ModelKey("", modelName);
					}

					final var id = new SymbolId(targetKey, LmSymbolKind.TYPE, targetType);
					if (workspaceIndex.symbolIndex().containsKey(id))
					{
						final var range = rangeForToken(token, source);
						out.add(new ReferenceOccurrence(id, uri, range));
					}
				}
			}
		}

		for (final var child : node.children())
		{
			collectReferencesInNode(child, modelKey, currentModel, source, uri, registry, out);
		}
	}

	private static ModelKey resolveModelAlias(final MetaModel currentModel,
											  final String alias,
											  final ModelRegistry registry)
	{
		if (alias == null || alias.isEmpty())
		{
			return null;
		}

		// LMCore is implicitly available for all M2 models.
		if (alias.equals(LMCorePackage.MODEL.name()))
		{
			return new ModelKey(LMCorePackage.MODEL.domain(), LMCorePackage.MODEL.name());
		}

		// First, resolve via imports on the current meta-model.
		for (final String imp : currentModel.imports())
		{
			final int lastDot = imp.lastIndexOf('.');
			final String simpleName = lastDot >= 0 ? imp.substring(lastDot + 1) : imp;
			if (simpleName.equals(alias))
			{
				final Model imported = registry.getModel(imp);
				if (imported instanceof MetaModel mm)
				{
					return new ModelKey(mm.domain(), mm.name());
				}
			}
		}

		// Fallback: scan the registry for the first meta-model with matching simple name.
		for (final var model : (Iterable<Model>) registry.models()::iterator)
		{
			if (model instanceof MetaModel mm && mm.name().equals(alias))
			{
				return new ModelKey(mm.domain(), mm.name());
			}
		}

		return null;
	}

	private static org.eclipse.lsp4j.Range rangeForToken(final PToken token, final CharSequence source)
	{
		final int start = token.offset();
		final int end = start + Math.max(1, token.length());
		final int startLine = Math.max(0, TextPositions.lineFor(source, start) - 1);
		final int startChar = Math.max(0, TextPositions.columnFor(source, start) - 1);
		final int endLine = Math.max(0, TextPositions.lineFor(source, end) - 1);
		final int endChar = Math.max(0, TextPositions.columnFor(source, end) - 1);
		final var startPos = new Position(startLine, startChar);
		final var endPos = new Position(endLine, endChar);
		return new org.eclipse.lsp4j.Range(startPos, endPos);
	}

	private static LmSymbolKind toSymbolKind(final DocumentSymbol symbol)
	{
		return switch (symbol.getKind())
		{
			case Namespace -> LmSymbolKind.META_MODEL;
			case Class, Enum, Struct -> LmSymbolKind.TYPE;
			case Field, Method, TypeParameter, Constant -> LmSymbolKind.FEATURE;
			default -> null;
		};
	}

	private void publishDiagnostics(final LmDocumentState state)
	{
		final var client = client();
		if (client == null) return;

		final var diagnostics = new ArrayList<org.eclipse.lsp4j.Diagnostic>();

		final var syntax = state.syntaxSnapshot();
		if (syntax != null)
		{
			for (final var d : syntax.diagnostics())
			{
				diagnostics.add(toLspDiagnostic(d));
			}
		}

		final var semantic = state.semanticSnapshot();
		if (semantic != null)
		{
			for (final var d : semantic.diagnostics())
			{
				diagnostics.add(toLspDiagnostic(d));
			}
		}

		final var params = new PublishDiagnosticsParams(state.uri().toString(), diagnostics);
		client.publishDiagnostics(params);
	}

	private static org.eclipse.lsp4j.Diagnostic toLspDiagnostic(final LmDiagnostic d)
	{
		final int line = Math.max(0, d.line() - 1);
		final int startChar = Math.max(0, d.column() - 1);
		final int endChar = startChar + Math.max(1, d.length());

		final var start = new org.eclipse.lsp4j.Position(line, startChar);
		final var end = new org.eclipse.lsp4j.Position(line, endChar);
		final var range = new org.eclipse.lsp4j.Range(start, end);

		final var diag = new org.eclipse.lsp4j.Diagnostic();
		diag.setRange(range);
		diag.setMessage(d.message());
		diag.setSeverity(switch (d.severity())
		{
			case ERROR -> org.eclipse.lsp4j.DiagnosticSeverity.Error;
			case WARNING -> org.eclipse.lsp4j.DiagnosticSeverity.Warning;
			case INFO -> org.eclipse.lsp4j.DiagnosticSeverity.Information;
		});
		return diag;
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
