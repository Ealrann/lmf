package org.logoce.lmf.core.loader.api.tooling.workspace;

import org.logoce.lmf.core.lang.MetaModel;
import org.logoce.lmf.core.lang.Model;
import org.logoce.lmf.core.loader.api.loader.model.LmSemanticIndexBuilder;
import org.logoce.lmf.core.loader.api.loader.model.LmSymbolIndex;
import org.logoce.lmf.core.loader.api.tooling.MetaModelResolver;
import org.logoce.lmf.core.loader.api.tooling.state.LmDocumentState;
import org.logoce.lmf.core.loader.api.tooling.state.LmSymbolKind;
import org.logoce.lmf.core.loader.api.tooling.state.ModelKey;
import org.logoce.lmf.core.loader.api.tooling.state.ReferenceOccurrence;
import org.logoce.lmf.core.loader.api.tooling.state.SymbolEntry;
import org.logoce.lmf.core.loader.api.tooling.state.SymbolId;

import java.util.ArrayList;

public final class SymbolIndexer
{
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

		final MetaModel metaModel = MetaModelResolver.resolveForDocument(syntax.roots(), syntax.source(), semanticModel, registry);
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
			symbolEntries.add(new SymbolEntry(id, containerId, state.uri(), decl.span()));
		}

		final var references = new ArrayList<ReferenceOccurrence>();
		for (final var ref : index.references())
		{
			final var id = toSymbolId(ref.target());
			references.add(new ReferenceOccurrence(id, state.uri(), ref.span()));
		}

		workspaceIndex.registerSymbols(state.uri(), symbolEntries);
		workspaceIndex.registerReferences(state.uri(), references);
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
}
