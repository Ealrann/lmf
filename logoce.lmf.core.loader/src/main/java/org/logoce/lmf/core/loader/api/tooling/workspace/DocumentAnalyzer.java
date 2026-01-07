package org.logoce.lmf.core.loader.api.tooling.workspace;

import org.logoce.lmf.core.api.model.ModelRegistry;
import org.logoce.lmf.core.lang.MetaModel;
import org.logoce.lmf.core.loader.api.loader.LmLoader;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.loader.api.loader.linking.LinkException;
import org.logoce.lmf.core.loader.api.loader.parsing.LmTreeReader;
import org.logoce.lmf.core.loader.api.loader.parsing.ModelHeaderUtil;
import org.logoce.lmf.core.loader.api.loader.util.TextPositions;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;
import org.logoce.lmf.core.util.tree.Tree;
import org.logoce.lmf.core.loader.api.tooling.MetaModelResolver;
import org.logoce.lmf.core.loader.api.tooling.state.LmDocumentState;
import org.logoce.lmf.core.loader.api.tooling.state.SemanticSnapshot;
import org.logoce.lmf.core.loader.api.tooling.state.SymbolTable;
import org.logoce.lmf.core.loader.api.tooling.state.SyntaxSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DocumentAnalyzer
{
	private final WorkspaceIndex workspaceIndex;
	private final SymbolIndexer symbolIndexer;

	public DocumentAnalyzer(final WorkspaceIndex workspaceIndex,
							final SymbolIndexer symbolIndexer)
	{
		this.workspaceIndex = Objects.requireNonNull(workspaceIndex, "workspaceIndex");
		this.symbolIndexer = Objects.requireNonNull(symbolIndexer, "symbolIndexer");
	}

	public void analyzeDocument(final LmDocumentState state)
	{
		try
		{
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

			state.setLastGoodSyntaxSnapshot(syntaxSnapshot);

			symbolIndexer.rebuildIndicesForDocument(state);
		}
		catch (LinkException e)
		{
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
		}
		catch (IllegalStateException e)
		{
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
		}
		catch (Exception e)
		{
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

	private List<MetaModel> resolveActiveMetaModels(final List<Tree<PNode>> roots)
	{
		final var registry = workspaceIndex.modelRegistry();
		return MetaModelResolver.resolveActiveMetaModelsFromRoots(roots, registry);
	}
}
