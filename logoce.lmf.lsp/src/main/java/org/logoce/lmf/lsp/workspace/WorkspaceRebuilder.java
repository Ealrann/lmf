package org.logoce.lmf.lsp.workspace;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.logoce.lmf.lsp.state.LmDocumentState;
import org.logoce.lmf.lsp.state.SemanticSnapshot;
import org.logoce.lmf.lsp.state.SymbolTable;
import org.logoce.lmf.lsp.state.SyntaxSnapshot;
import org.logoce.lmf.lsp.state.WorkspaceIndex;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.loader.LmLoader;
import org.logoce.lmf.model.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.model.loader.linking.LmModelLinker;
import org.logoce.lmf.model.loader.parsing.LmTreeReader;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.util.ModelRegistry;
import org.logoce.lmf.model.util.TextPositions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.services.LanguageClient;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public final class WorkspaceRebuilder
{
	private static final Logger LOG = LoggerFactory.getLogger(WorkspaceRebuilder.class);

	private final WorkspaceIndex workspaceIndex;
	private final SymbolIndexer symbolIndexer;
	private final Path projectRoot;
	private final Supplier<LanguageClient> clientSupplier;

	public WorkspaceRebuilder(final WorkspaceIndex workspaceIndex,
							  final SymbolIndexer symbolIndexer,
							  final Path projectRoot,
							  final Supplier<LanguageClient> clientSupplier)
	{
		this.workspaceIndex = Objects.requireNonNull(workspaceIndex, "workspaceIndex");
		this.symbolIndexer = Objects.requireNonNull(symbolIndexer, "symbolIndexer");
		this.projectRoot = projectRoot;
		this.clientSupplier = Objects.requireNonNull(clientSupplier, "clientSupplier");
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
			final LanguageClient client = clientSupplier.get();
			if (client != null)
			{
				client.logMessage(new MessageParams(
					MessageType.Error,
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
				final List<Model> models = loadModelsFromProjectRoot(projectRoot);
				final var builder = new ModelRegistry.Builder(workspaceIndex.modelRegistry());
				for (final Model model : models)
				{
					builder.register(model);
				}
				workspaceIndex.setModelRegistry(builder.build());
				LOG.info("LMF LSP rebuildModelRegistry: projectRoot={}, models={}", projectRoot, models.size());
			}
			catch (Exception e)
			{
				LOG.warn("LMF LSP rebuildModelRegistry: error with projectRoot {}, keeping previous registry", projectRoot, e);
			}
			return;
		}

		final var docs = workspaceIndex.documents().values();
		if (docs.isEmpty())
		{
			workspaceIndex.setModelRegistry(ModelRegistry.empty());
			return;
		}

		final var inputs = new ArrayList<InputStream>();
		for (final LmDocumentState doc : docs)
		{
			final byte[] bytes = doc.text().getBytes(StandardCharsets.UTF_8);
			inputs.add(new ByteArrayInputStream(bytes));
		}

		try
		{
			final var loader = LmLoader.withEmptyRegistry();
			final List<Model> models = loader.loadModels(inputs);

			final var builder = new ModelRegistry.Builder(workspaceIndex.modelRegistry());
			for (final Model model : models)
			{
				builder.register(model);
			}
			workspaceIndex.setModelRegistry(builder.build());
			LOG.info("LMF LSP rebuildModelRegistry: from open documents, models={}", models.size());
		}
		catch (Exception e)
		{
			LOG.warn("Failed to rebuild model registry from open documents, keeping previous registry", e);
		}
	}

	private List<Model> loadModelsFromProjectRoot(final Path root) throws Exception
	{
		final var inputs = new ArrayList<InputStream>();
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

			final String text = state.text();

			final var syntaxDiagnostics = new ArrayList<LmDiagnostic>();
			final var treeReader = new LmTreeReader();
			final var readResult = treeReader.read(text, syntaxDiagnostics);
			final var roots = readResult.roots();
			final CharSequence source = readResult.source();

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

			final var previousGood = state.lastGoodSemanticSnapshot();
			final var newGood = model instanceof MetaModel ? semanticSnapshot : previousGood;
			state.setLastGoodSemanticSnapshot(newGood);

			symbolIndexer.rebuildIndicesForDocument(state);

			publishDiagnostics(state);

			final int syntaxCount = syntaxDiagnostics.size();
			final int semanticCount = semanticDiagnostics.size();
			final String modelKind = model == null ? "null" : model.getClass().getSimpleName();
			LOG.debug("LMF LSP analyzeDocument done: uri={}, model={}, syntaxDiag={}, semanticDiag={}",
					  state.uri(), modelKind, syntaxCount, semanticCount);
		}
		catch (Exception e)
		{
			LOG.error("Error while analyzing document {}", state.uri(), e);
			final LanguageClient client = clientSupplier.get();
			if (client != null)
			{
				client.logMessage(new MessageParams(
					MessageType.Error,
					"LMF LSP: error while analyzing " + state.uri() + ": " + e.getMessage()));
			}
		}
	}

	private void publishDiagnostics(final LmDocumentState state)
	{
		final LanguageClient client = clientSupplier.get();
		if (client == null) return;

		final var diagnostics = new ArrayList<Diagnostic>();

		final SyntaxSnapshot syntax = state.syntaxSnapshot();
		if (syntax != null)
		{
			for (final LmDiagnostic d : syntax.diagnostics())
			{
				diagnostics.add(toLspDiagnostic(d));
			}
		}

		final SemanticSnapshot semantic = state.semanticSnapshot();
		if (semantic != null)
		{
			for (final LmDiagnostic d : semantic.diagnostics())
			{
				diagnostics.add(toLspDiagnostic(d));
			}
		}

		final var params = new PublishDiagnosticsParams(state.uri().toString(), diagnostics);
		client.publishDiagnostics(params);
	}

	private static Diagnostic toLspDiagnostic(final LmDiagnostic d)
	{
		final int line = Math.max(0, d.line() - 1);
		final int startChar = Math.max(0, d.column() - 1);
		final int endChar = startChar + Math.max(1, d.length());

		final var start = new Position(line, startChar);
		final var end = new Position(line, endChar);
		final Range range = new Range(start, end);

		final var diag = new Diagnostic();
		diag.setRange(range);
		diag.setMessage(d.message());
		diag.setSeverity(switch (d.severity())
		{
			case ERROR -> DiagnosticSeverity.Error;
			case WARNING -> DiagnosticSeverity.Warning;
			case INFO -> DiagnosticSeverity.Information;
		});
		return diag;
	}
}
