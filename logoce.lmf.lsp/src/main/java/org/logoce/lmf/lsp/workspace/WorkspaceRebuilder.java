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
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.loader.LmLoader;
import org.logoce.lmf.model.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.model.loader.linking.LinkException;
import org.logoce.lmf.model.loader.parsing.LmTreeReader;
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
				final List<org.logoce.lmf.model.loader.model.LmDocument> documents = loadDocumentsFromProjectRoot(projectRoot);
				final var newRegistry = LmLoader.buildRegistry(documents, workspaceIndex.modelRegistry());
				workspaceIndex.setModelRegistry(newRegistry);
				LOG.info("LMF LSP rebuildModelRegistry: projectRoot={}, documents={}", projectRoot, documents.size());
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

		try
		{
			final var loader = LmLoader.withEmptyRegistry();
			final var documents = new ArrayList<org.logoce.lmf.model.loader.model.LmDocument>();
			for (final LmDocumentState docState : docs)
			{
				final var text = docState.text();
				final var doc = loader.loadModel(text);
				if (doc.roots().isEmpty())
				{
					final var error = doc.diagnostics().stream()
										 .filter(d -> d.severity() == LmDiagnostic.Severity.ERROR)
										 .findFirst();
					if (error.isPresent())
					{
						final var d = error.get();
						throw new IllegalArgumentException("Failed to parse model at " +
														   d.line() + ":" + d.column() +
														   " - " + d.message());
					}
					continue;
				}
				documents.add(doc);
			}

			final var newRegistry = LmLoader.buildRegistry(documents, workspaceIndex.modelRegistry());
			workspaceIndex.setModelRegistry(newRegistry);
			LOG.info("LMF LSP rebuildModelRegistry: from open documents, documents={}", documents.size());
		}
		catch (Exception e)
		{
			LOG.warn("Failed to rebuild model registry from open documents, keeping previous registry", e);
		}
	}

	private List<org.logoce.lmf.model.loader.model.LmDocument> loadDocumentsFromProjectRoot(final Path root) throws Exception
	{
		final var documents = new ArrayList<org.logoce.lmf.model.loader.model.LmDocument>();
		final var loader = LmLoader.withEmptyRegistry();
		try (final var paths = Files.walk(root))
		{
			paths.filter(Files::isRegularFile)
				 .filter(p -> p.getFileName().toString().endsWith(".lm"))
				 .forEach(p -> {
					 try
					 {
						 final byte[] bytes = Files.readAllBytes(p);
						 final var text = new String(bytes, StandardCharsets.UTF_8);
						 final var doc = loader.loadModel(text);
						 if (doc.roots().isEmpty())
						 {
							 final var error = doc.diagnostics().stream()
												  .filter(d -> d.severity() == LmDiagnostic.Severity.ERROR)
												  .findFirst();
							 if (error.isPresent())
							 {
								 final var d = error.get();
								 throw new IllegalArgumentException("Failed to parse model " + p + " at " +
																	d.line() + ":" + d.column() +
																	" - " + d.message());
							 }
							 return;
						 }
						 documents.add(doc);
					 }
					 catch (Exception e)
					 {
						 LOG.warn("LMF LSP rebuildModelRegistry: cannot read model file {}", p, e);
					 }
				 });
		}

		return List.copyOf(documents);
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

			final var loader = new LmLoader(workspaceIndex.modelRegistry());
			final var doc = loader.loadModel(text);

			final var syntaxSnapshot = new SyntaxSnapshot(
				List.of(),
				doc.roots(),
				doc.diagnostics(),
				doc.source());
			state.setSyntaxSnapshot(syntaxSnapshot);

			final var semanticSnapshot = new SemanticSnapshot(
				doc.model(),
				doc.linkTrees(),
				List.of(),
				SymbolTable.EMPTY,
				List.of());
			state.setSemanticSnapshot(semanticSnapshot);

			final var previousGood = state.lastGoodSemanticSnapshot();
			final var newGood = doc.model() != null ? semanticSnapshot : previousGood;
			state.setLastGoodSemanticSnapshot(newGood);

			symbolIndexer.rebuildIndicesForDocument(state);

			publishDiagnostics(state);

			final int syntaxCount = syntaxSnapshot.diagnostics().size();
			final int semanticCount = semanticSnapshot.diagnostics().size();
			final String modelKind = doc.model() == null ? "null" : doc.model().getClass().getSimpleName();
			LOG.debug("LMF LSP analyzeDocument done: uri={}, model={}, syntaxDiag={}, semanticDiag={}",
					  state.uri(), modelKind, syntaxCount, semanticCount);
		}
		catch (LinkException e)
		{
			// Link errors (for example unresolved imported models) are expected in
			// partially-loaded workspaces. Treat them as document diagnostics instead
			// of hard LSP errors to avoid flooding the client notification view.
			LOG.warn("LMF LSP analyzeDocument link error for uri={}: {}",
					 state.uri(), e.getMessage());

			final String text = state.text();
			final var diagnostics = new ArrayList<LmDiagnostic>();
			final var reader = new LmTreeReader();
			final var readResult = reader.read(text, diagnostics);

			if (e.pNode() != null)
			{
				final var span = TextPositions.spanOf(e.pNode(), readResult.source());
				diagnostics.add(new LmDiagnostic(
					span.line(),
					span.column(),
					span.length(),
					span.offset(),
					LmDiagnostic.Severity.ERROR,
					e.getMessage() == null ? "Link error" : e.getMessage()));
			}

			final var syntaxSnapshot = new SyntaxSnapshot(
				List.of(),
				readResult.roots(),
				List.copyOf(diagnostics),
				readResult.source());
			state.setSyntaxSnapshot(syntaxSnapshot);

			final var semanticSnapshot = new SemanticSnapshot(
				null,
				List.of(),
				List.of(),
				SymbolTable.EMPTY,
				List.of());
			state.setSemanticSnapshot(semanticSnapshot);

			// Keep lastGoodSemanticSnapshot unchanged: we don't have a new good model.

			publishDiagnostics(state);
		}
		catch (Exception e)
		{
			// Any other unexpected errors are also surfaced as document diagnostics
			// rather than global client errors, to avoid flooding the notification view.
			LOG.warn("LMF LSP analyzeDocument unexpected error for uri={}: {}",
					 state.uri(), e.getMessage(), e);

			final String text = state.text();
			final var diagnostics = new ArrayList<LmDiagnostic>();
			final var reader = new LmTreeReader();
			final var readResult = reader.read(text, diagnostics);

			diagnostics.add(new LmDiagnostic(
				1,
				1,
				1,
				0,
				LmDiagnostic.Severity.ERROR,
				e.getMessage() == null ? "Error while analyzing document" : e.getMessage()));

			final var syntaxSnapshot = new SyntaxSnapshot(
				List.of(),
				readResult.roots(),
				List.copyOf(diagnostics),
				readResult.source());
			state.setSyntaxSnapshot(syntaxSnapshot);

			final var semanticSnapshot = new SemanticSnapshot(
				null,
				List.of(),
				List.of(),
				SymbolTable.EMPTY,
				List.of());
			state.setSemanticSnapshot(semanticSnapshot);

			publishDiagnostics(state);
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
