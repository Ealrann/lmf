package org.logoce.lmf.lsp.workspace;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.logoce.lmf.lsp.HeaderTextScanner;
import org.logoce.lmf.core.loader.api.loader.model.LmDocument;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;
import org.logoce.lmf.core.util.tree.Tree;
import org.logoce.lmf.lsp.state.LmDocumentState;
import org.logoce.lmf.lsp.state.SemanticSnapshot;
import org.logoce.lmf.lsp.state.SymbolTable;
import org.logoce.lmf.lsp.state.SyntaxSnapshot;
import org.logoce.lmf.lsp.state.WorkspaceIndex;
import org.logoce.lmf.lsp.features.completion.MetaModelResolver;
import org.logoce.lmf.core.lang.MetaModel;
import org.logoce.lmf.core.loader.api.loader.LmLoader;
import org.logoce.lmf.core.loader.api.loader.LmWorkspace;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.loader.api.loader.linking.LinkException;
import org.logoce.lmf.core.loader.api.loader.parsing.LmTreeReader;
import org.logoce.lmf.core.loader.api.loader.parsing.ModelHeaderUtil;
import org.logoce.lmf.core.api.model.ModelRegistry;
import org.logoce.lmf.core.loader.api.loader.util.TextPositions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.services.LanguageClient;

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
import java.util.function.Supplier;

public final class WorkspaceRebuilder
{
	private static final Logger LOG = LoggerFactory.getLogger(WorkspaceRebuilder.class);

	private final WorkspaceIndex workspaceIndex;
	private final SymbolIndexer symbolIndexer;
	private final Path projectRoot;
	private final Supplier<LanguageClient> clientSupplier;
	private final Map<Path, Long> indexedMetaModelFilesByMtime = new HashMap<>();
	private final DiskMetaModelHeaderIndex diskMetaModelHeaderIndex = new DiskMetaModelHeaderIndex();

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
		final long startNanos = System.nanoTime();
		final int docCount = workspaceIndex.documents().size();
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

	private void rebuildModelRegistry()
	{
		if (projectRoot != null)
		{
			try
			{
				final var requiredMetaModelFiles = collectRequiredMetaModelFilesFromProjectRoot(projectRoot);
				if (requiredMetaModelFiles.isEmpty())
				{
					return;
				}

				final var metaModelWorkspace = LmWorkspace.loadMetaModels(requiredMetaModelFiles, workspaceIndex.modelRegistry());
				workspaceIndex.setModelRegistry(metaModelWorkspace.registry());
				indexWorkspaceMetaModels(metaModelWorkspace);
				LOG.debug("LMF LSP rebuildModelRegistry: projectRoot={}, metaModels={}", projectRoot,
						  metaModelWorkspace.files().size());
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
					final var error = diagnostics.stream()
												.filter(d -> d.severity() == LmDiagnostic.Severity.ERROR)
												.findFirst();
					if (error.isPresent())
					{
						final var d = error.get();
						LOG.warn("LMF LSP rebuildModelRegistry: skipping document {} due to parse error at {}:{} - {}",
								 docState.uri(), d.line(), d.column(), d.message());
					}
					continue;
				}

				// Only MetaModel roots participate in the model registry; M1 instance
				// documents rely on their 'metamodels' header and the registry should
				// contain only the referenced meta-models, not the instances themselves.
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
				return;
			}

			final var newRegistry = LmLoader.buildRegistry(documents, workspaceIndex.modelRegistry());
			workspaceIndex.setModelRegistry(newRegistry);
			LOG.debug("LMF LSP rebuildModelRegistry: from open documents, documents={}", documents.size());
		}
		catch (Exception e)
		{
			// If multi-model registry rebuild fails (for example due to incomplete
			// import graphs in the currently open documents), keep the previous
			// registry so that already-built models (LMCore, compiled meta-models)
			// remain available for analysis.
			LOG.debug("Failed to rebuild model registry from open documents, keeping previous registry: {}",
					  e.getMessage(), e);
		}
	}

	private List<File> collectRequiredMetaModelFilesFromProjectRoot(final Path root) throws IOException
	{
		final var requiredNames = collectDeclaredMetaModelNamesFromOpenDocuments();
		if (requiredNames.isEmpty())
		{
			return List.of();
		}

		diskMetaModelHeaderIndex.refresh(root);
		return diskMetaModelHeaderIndex.resolveMetaModelFilesClosure(requiredNames);
	}

	private HashSet<String> collectDeclaredMetaModelNamesFromOpenDocuments()
	{
		final var result = new HashSet<String>();
		final var reader = new LmTreeReader();

		for (final var state : workspaceIndex.documents().values())
		{
			final var text = state.text();
			final var diagnostics = new ArrayList<LmDiagnostic>();
			final var readResult = reader.read(text, diagnostics);
			final var roots = readResult.roots();

			if (roots.isEmpty())
			{
				result.addAll(HeaderTextScanner.parseMetamodelNames(text));
				continue;
			}

			final var rootNode = roots.getFirst().data();

			if (ModelHeaderUtil.isMetaModelRoot(roots))
			{
				final var qualifiedName = qualifiedNameFromHeader(rootNode);
				if (qualifiedName != null)
				{
					result.add(qualifiedName);
				}
				continue;
			}

			result.addAll(ModelHeaderUtil.resolveMetamodelNames(rootNode));
		}

		return result;
	}

	private static String qualifiedNameFromHeader(final PNode rootNode)
	{
		final String domain = ModelHeaderUtil.resolveDomain(rootNode);
		final String name = ModelHeaderUtil.resolveName(rootNode);
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

	/**
	 * Index meta-model files from disk (not only opened documents) so that features like
	 * go-to-definition can navigate to declarations in unopened files.
	 * <p>
	 * Open documents always take precedence: if a meta-model file is open, its in-memory
	 * version is indexed via {@link #analyzeDocument(LmDocumentState)} instead.
	 */
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

		// Evict removed files and their indices.
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

			// If the file is open, the editor content (not the on-disk file) is indexed elsewhere.
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

	/**
	 * Analyze the given document: run syntax and semantic pipelines and publish diagnostics.
	 * This method must be invoked from the worker executor.
	 */
	public void analyzeDocument(final LmDocumentState state)
	{
		try
		{
			LOG.debug("LMF LSP analyzeDocument start: uri={}, textLength={}", state.uri(), state.text().length());
			final String text = state.text();

			final var reader = new LmTreeReader();
			final var diagnostics = new ArrayList<LmDiagnostic>();
			final var readResult = reader.read(text, diagnostics);
			final var roots = readResult.roots();

			final var registryBuilder = new ModelRegistry.Builder(workspaceIndex.modelRegistry());
			if (!roots.isEmpty())
			{
				final var node = roots.getFirst().data();
				final var domain = ModelHeaderUtil.resolveDomain(node);
				final var name = ModelHeaderUtil.resolveName(node);
				if (domain != null && !domain.isBlank() && name != null && !name.isBlank())
				{
					registryBuilder.remove(domain + "." + name);
				}
			}

			final var loader = new LmLoader(registryBuilder.build());
			final var doc = loader.loadModel(readResult, diagnostics);

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

			// Syntax is considered "good" whenever parsing succeeds, even if
			// linking fails later. This snapshot can be reused for semantic
			// tokens when the current syntax is temporarily invalid.
			state.setLastGoodSyntaxSnapshot(syntaxSnapshot);

			symbolIndexer.rebuildIndicesForDocument(state);

			publishDiagnostics(state);

			final int syntaxCount = syntaxSnapshot.diagnostics().size();
			final int semanticCount = semanticSnapshot.diagnostics().size();
			final String modelKind = doc.model() == null ? "null" : doc.model().getClass().getSimpleName();
			LOG.debug("LMF LSP analyzeDocument done: uri={}, model={}, syntaxDiag={}, semanticDiag={}",
					  state.uri(), modelKind, syntaxCount, semanticCount);

			// Let the client decide when to refresh semantic tokens.
		}
		catch (LinkException e)
		{
			// Link errors (for example unresolved imported models) are expected in
			// partially-loaded workspaces. Treat them as document diagnostics instead
			// of hard LSP errors to avoid flooding the client notification view or logs.
			LOG.debug("LMF LSP analyzeDocument link error for uri={}: {}",
					  state.uri(), e.getMessage());

			final String text = state.text();
			final var diagnostics = new ArrayList<LmDiagnostic>();
			final var parsed = parseForDiagnostics(text, diagnostics);

			if (e.pNode() != null)
			{
				final var span = TextPositions.spanOf(e.pNode(), parsed.source());
				diagnostics.add(new LmDiagnostic(
					span.line(),
					span.column(),
					span.length(),
					span.offset(),
					LmDiagnostic.Severity.ERROR,
					e.getMessage() == null ? "Link error" : e.getMessage()));
			}

			final var syntaxSnapshot = buildSyntaxSnapshot(parsed.roots(), diagnostics, parsed.source());
			setSyntaxSnapshot(state, syntaxSnapshot, true);

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
		catch (IllegalStateException e)
		{
			// Meta-model package resolution and similar issues are expected in dynamic
			// workspaces (for example, when generated Java for a meta-model is not on
			// the LSP classpath). Treat them as document diagnostics instead of hard
			// LSP errors so that M1/M2 documents remain editable, and attempt a
			// semantic-only link to populate link trees when possible.
			LOG.warn("LMF LSP analyzeDocument meta-model error for uri={}: {}",
					 state.uri(), e.getMessage());

			final String text = state.text();
			final var diagnostics = new ArrayList<LmDiagnostic>();
			final var parsed = parseForDiagnostics(text, diagnostics);

			diagnostics.add(new LmDiagnostic(
				1,
				1,
				Math.max(1, text.length()),
				0,
				LmDiagnostic.Severity.ERROR,
				e.getMessage() == null ? "Error while analyzing document" : e.getMessage()));

			final var activeMetaModels = resolveActiveMetaModels(parsed.roots());
			final var linkTrees = SemanticLinking.link(parsed.roots(),
													  activeMetaModels,
													  workspaceIndex.modelRegistry(),
													  parsed.source());

			final var syntaxSnapshot = buildSyntaxSnapshot(parsed.roots(), diagnostics, parsed.source());
			setSyntaxSnapshot(state, syntaxSnapshot, true);

			final var semanticSnapshot = new SemanticSnapshot(
				null,
				linkTrees,
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
			final var parsed = parseForDiagnostics(text, diagnostics);

			diagnostics.add(new LmDiagnostic(
				1,
				1,
				1,
				0,
				LmDiagnostic.Severity.ERROR,
				e.getMessage() == null ? "Error while analyzing document" : e.getMessage()));

			final var syntaxSnapshot = buildSyntaxSnapshot(parsed.roots(), diagnostics, parsed.source());
			setSyntaxSnapshot(state, syntaxSnapshot, false);

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

	private record ParsedText(List<Tree<PNode>> roots, CharSequence source)
	{
	}

	private static ParsedText parseForDiagnostics(final String text, final List<LmDiagnostic> diagnostics)
	{
		final var reader = new LmTreeReader();
		try
		{
			final var readResult = reader.read(text, diagnostics);
			return new ParsedText(readResult.roots(), readResult.source());
		}
		catch (Exception parseEx)
		{
			diagnostics.add(new LmDiagnostic(
				1,
				1,
				Math.max(1, text.length()),
				0,
				LmDiagnostic.Severity.ERROR,
				parseEx.getMessage() == null ? "Error while parsing document" : parseEx.getMessage()));
			return new ParsedText(List.of(), text);
		}
	}

	private static SyntaxSnapshot buildSyntaxSnapshot(final List<Tree<PNode>> roots,
													 final List<LmDiagnostic> diagnostics,
													 final CharSequence source)
	{
		return new SyntaxSnapshot(
			List.of(),
			roots,
			List.copyOf(diagnostics),
			source);
	}

	private static void setSyntaxSnapshot(final LmDocumentState state,
										  final SyntaxSnapshot syntaxSnapshot,
										  final boolean updateLastGood)
	{
		state.setSyntaxSnapshot(syntaxSnapshot);
		if (updateLastGood)
		{
			state.setLastGoodSyntaxSnapshot(syntaxSnapshot);
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

	private List<MetaModel> resolveActiveMetaModels(final List<Tree<PNode>> roots)
	{
		final var registry = workspaceIndex.modelRegistry();
		return MetaModelResolver.resolveActiveMetaModelsFromRoots(roots, registry);
	}
}
