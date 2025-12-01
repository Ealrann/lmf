package org.logoce.lmf.lsp.workspace;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.logoce.lmf.lsp.state.LmDocumentState;
import org.logoce.lmf.lsp.state.LmSymbolKind;
import org.logoce.lmf.lsp.state.ModelKey;
import org.logoce.lmf.lsp.state.ReferenceOccurrence;
import org.logoce.lmf.lsp.state.SymbolEntry;
import org.logoce.lmf.lsp.state.SymbolId;
import org.logoce.lmf.lsp.state.SyntaxSnapshot;
import org.logoce.lmf.lsp.state.WorkspaceIndex;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.loader.model.LmSymbolIndex;
import org.logoce.lmf.model.loader.model.LmSymbolIndexBuilder;
import org.logoce.lmf.model.util.ModelRegistry;
import org.logoce.lmf.model.util.TextPositions;
import org.logoce.lmf.model.util.tree.Tree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public final class SymbolIndexer
{
	private static final Logger LOG = LoggerFactory.getLogger(SymbolIndexer.class);

	private final WorkspaceIndex workspaceIndex;

	public SymbolIndexer(final WorkspaceIndex workspaceIndex)
	{
		this.workspaceIndex = workspaceIndex;
	}

	public void rebuildIndicesForDocument(final LmDocumentState state)
	{
		final var syntax = state.syntaxSnapshot();
		final var effectiveSemantic = state.lastGoodSemanticSnapshot() != null
									  ? state.lastGoodSemanticSnapshot()
									  : state.semanticSnapshot();
		final var semantic = effectiveSemantic;
		if (syntax == null || semantic == null)
		{
			workspaceIndex.clearIndicesForDocument(state.uri());
			return;
		}

		final Model model = semantic.model();
		if (model == null)
		{
			workspaceIndex.clearIndicesForDocument(state.uri());
			return;
		}

		final var modelKey = new ModelKey(model.domain(), model.name());

		final var registry = workspaceIndex.modelRegistry();
		final LmSymbolIndex index = LmSymbolIndexBuilder.buildIndex(model,
																	syntax.roots(),
																	syntax.source(),
																	registry);

		final var symbolEntries = new ArrayList<SymbolEntry>();
		for (final var decl : index.declarations())
		{
			final var id = toSymbolId(decl.id());
			final var range = toRange(decl.span(), syntax.source());
			symbolEntries.add(new SymbolEntry(id, state.uri(), range));
		}

		workspaceIndex.registerSymbols(state.uri(), symbolEntries);

		final var references = buildReferences(modelKey, model, syntax, state.uri());
		workspaceIndex.registerReferences(state.uri(), references);

		LOG.debug("LMF LSP SymbolIndexer: uri={}, symbols={}, references={}",
				  state.uri(), symbolEntries.size(), references.size());
	}

	private List<ReferenceOccurrence> buildReferences(final ModelKey modelKey,
													  final Model model,
													  final SyntaxSnapshot syntax,
													  final URI uri)
	{
		final var registry = workspaceIndex.modelRegistry();
		final LmSymbolIndex index = LmSymbolIndexBuilder.buildIndex(model,
																	syntax.roots(),
																	syntax.source(),
																	registry);

		final var out = new ArrayList<ReferenceOccurrence>();
		for (final var ref : index.references())
		{
			final var id = toSymbolId(ref.target());
			final var range = toRange(ref.span(), syntax.source());
			out.add(new ReferenceOccurrence(id, uri, range));
		}
		return List.copyOf(out);
	}

	private static SymbolId toSymbolId(final LmSymbolIndex.SymbolId id)
	{
		final var kind = switch (id.kind())
		{
			case META_MODEL -> LmSymbolKind.META_MODEL;
			case TYPE -> LmSymbolKind.TYPE;
			case FEATURE -> LmSymbolKind.FEATURE;
		};
		final var modelKey = new ModelKey(id.modelDomain(), id.modelName());
		return new SymbolId(modelKey, kind, id.name());
	}

	private static Range toRange(final TextPositions.Span span, final CharSequence source)
	{
		final int startOffset = span.offset();
		final int endOffset = startOffset + Math.max(1, span.length());
		final int startLine = Math.max(0, TextPositions.lineFor(source, startOffset) - 1);
		final int startChar = Math.max(0, TextPositions.columnFor(source, startOffset) - 1);
		final int endLine = Math.max(0, TextPositions.lineFor(source, endOffset) - 1);
		final int endChar = Math.max(0, TextPositions.columnFor(source, endOffset) - 1);
		final var startPos = new Position(startLine, startChar);
		final var endPos = new Position(endLine, endChar);
		return new Range(startPos, endPos);
	}
}
