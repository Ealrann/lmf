package org.logoce.lmf.lsp.workspace;

import org.eclipse.lsp4j.Range;
import org.logoce.lmf.lsp.LspRanges;
import org.logoce.lmf.lsp.state.LmDocumentState;
import org.logoce.lmf.lsp.state.LmSymbolKind;
import org.logoce.lmf.lsp.state.ModelKey;
import org.logoce.lmf.lsp.state.ReferenceOccurrence;
import org.logoce.lmf.lsp.state.SymbolEntry;
import org.logoce.lmf.lsp.state.SymbolId;
import org.logoce.lmf.lsp.state.WorkspaceIndex;
import org.logoce.lmf.lsp.features.completion.MetaModelResolver;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.loader.model.LmSemanticIndexBuilder;
import org.logoce.lmf.model.loader.model.LmSymbolIndex;
import org.logoce.lmf.model.util.TextPositions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

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

		final var registry = workspaceIndex.modelRegistry();
		final Model semanticModel = semantic.model();

		final MetaModel metaModel = MetaModelResolver.resolveForDocument(syntax, semanticModel, registry);
		if (metaModel == null)
		{
			workspaceIndex.clearIndicesForDocument(state.uri());
			return;
		}

		final LmSymbolIndex index = LmSemanticIndexBuilder.buildIndex(metaModel,
																	  semantic.linkTrees(),
																	  registry,
																	  syntax.source());

		final var symbolEntries = new ArrayList<SymbolEntry>();
		for (final var decl : index.declarations())
		{
			final var id = toSymbolId(decl.id());
			final var containerId = decl.container() != null ? toSymbolId(decl.container()) : null;
			final var range = toRange(decl.span(), syntax.source());
			symbolEntries.add(new SymbolEntry(id, containerId, state.uri(), range));
		}

		final var references = new ArrayList<ReferenceOccurrence>();
		for (final var ref : index.references())
		{
			final var id = toSymbolId(ref.target());
			final var range = toRange(ref.span(), syntax.source());
			references.add(new ReferenceOccurrence(id, state.uri(), range));
		}

		workspaceIndex.registerSymbols(state.uri(), symbolEntries);
		workspaceIndex.registerReferences(state.uri(), references);

		LOG.debug("LMF LSP SymbolIndexer: uri={}, symbols={}, references={}",
				  state.uri(), symbolEntries.size(), references.size());
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
		return new SymbolId(modelKey, kind, id.name(), id.containerPath());
	}

	private static Range toRange(final TextPositions.Span span, final CharSequence source)
	{
		return LspRanges.forSpan(source, span);
	}

}
