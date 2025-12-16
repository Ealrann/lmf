package org.logoce.lmf.lsp.features.completion;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.logoce.lmf.core.api.loader.linking.LinkNode;
import org.logoce.lmf.core.api.text.syntax.PNode;
import org.logoce.lmf.lsp.state.SemanticSnapshot;
import org.logoce.lmf.lsp.state.SyntaxSnapshot;
import org.logoce.lmf.core.lang.Model;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.api.loader.RelationReferenceCompletions;
import org.logoce.lmf.core.api.model.ModelRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Relation value completion.
 * <p>
 * This follows the same overall pattern as {@link AttributeValueCompletionProvider} but targets
 * relation-valued features: it uses the LMCore header and feature name to resolve the
 * {@link Relation} being edited and its target concept, then delegates candidate discovery to
 * {@link RelationReferenceCompletions}, which operates on the link trees and model registry.
 */
final class RelationValueCompletionProvider
{
	private static final Logger LOG = LoggerFactory.getLogger(RelationValueCompletionProvider.class);

	private RelationValueCompletionProvider()
	{
	}

	static List<CompletionItem> complete(final CompletionContext context)
	{
		final SyntaxSnapshot syntax = context.syntax();
		final Position pos = context.position();

		if (syntax == null)
		{
			return List.of();
		}

		final var valueContext = context.value();
		final Relation<?, ?, ?, ?> relationFeature = valueContext != null ? valueContext.relation() : null;
		if (relationFeature == null)
		{
			return List.of();
		}

		final SemanticSnapshot semantic = context.semantic();
		final Model owningModel = semantic != null ? semantic.model() : null;
		final List<? extends LinkNode<?, PNode>> linkTrees =
			semantic != null ? semantic.linkTrees() : List.of();
		final ModelRegistry registry = context.server().workspaceIndex().modelRegistry();
		final var candidates = RelationReferenceCompletions.collectRelationCandidates(
			owningModel,
			linkTrees,
			relationFeature,
			registry);

		if (candidates.isEmpty())
		{
			return List.of();
		}

		final var range = new Range(new Position(pos.getLine(), pos.getCharacter()),
									new Position(pos.getLine(), pos.getCharacter()));

		final var items = new ArrayList<CompletionItem>(candidates.size());
		for (final RelationReferenceCompletions.RelationCandidate candidate : candidates)
		{
			final var item = new CompletionItem(candidate.label());
			item.setDetail(candidate.detail());
			final String sortPrefix = candidate.local() ? "1_" : "2_";
			item.setSortText(sortPrefix + candidate.label());
			item.setTextEdit(Either.forLeft(new TextEdit(range, candidate.label())));
			items.add(item);
		}

		LOG.debug("LMF LSP completion: relation value completions, feature={}, items={}",
				  relationFeature.name(), items.size());

		return List.copyOf(items);
	}
}
