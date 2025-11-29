package org.logoce.lmf.lsp.features.completion;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.logoce.lmf.lsp.LmLanguageServer;
import org.logoce.lmf.lsp.state.LmDocumentState;
import org.logoce.lmf.lsp.state.SemanticSnapshot;
import org.logoce.lmf.lsp.state.SyntaxSnapshot;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;

public final class LmCompletionEngine
{
	private static final Logger LOG = LoggerFactory.getLogger(LmCompletionEngine.class);

	private LmCompletionEngine()
	{
	}

	public static Either<List<CompletionItem>, CompletionList> complete(final LmLanguageServer server,
																		final URI uri,
																		final Position pos)
	{
		final LmDocumentState state = server.workspaceIndex().getDocument(uri);
		if (state == null)
		{
			LOG.info("LMF LSP completion: no document state for uri={}", uri);
			return Either.forLeft(List.of());
		}

		final SyntaxSnapshot syntax = state.syntaxSnapshot();
		final SemanticSnapshot semantic = state.lastGoodSemanticSnapshot() != null
										  ? state.lastGoodSemanticSnapshot()
										  : state.semanticSnapshot();
		if (syntax == null || semantic == null)
		{
			LOG.info("LMF LSP completion: missing snapshots for uri={}, syntaxNull={}, semanticNull={}",
					 uri, syntax == null, semantic == null);
			return Either.forLeft(List.of());
		}

		// 1) Attribute value completions, based on datatype.
		final var valueItems = AttributeValueCompletionProvider.complete(uri, semantic, syntax, pos);
		if (!valueItems.isEmpty())
		{
			return Either.forLeft(List.copyOf(valueItems));
		}

		final CompletionContextKind contextKind = SyntaxNavigation.detectCompletionContext(syntax, pos);

		// 2) Group feature completions in default context.
		if (contextKind == CompletionContextKind.DEFAULT)
		{
			final var featureItems = GroupFeatureCompletionProvider.complete(semantic, syntax, pos);
			if (!featureItems.isEmpty())
			{
				LOG.info("LMF LSP completion: feature completion, uri={}, line={}, character={}, items={}",
						 uri, pos.getLine(), pos.getCharacter(), featureItems.size());
				return Either.forLeft(List.copyOf(featureItems));
			}

			// In default context, if we have no group feature completions,
			// do not fall back to type-oriented completions; keep expectations
			// focused on feature names here.
			LOG.info("LMF LSP completion: no completions in DEFAULT context for uri={}, line={}, character={}",
					 uri, pos.getLine(), pos.getCharacter());
			return Either.forLeft(List.of());
		}

		// 3) Type-oriented completions only for non-default contexts (@ / #).
		final Model model = semantic.model();
		if (!(model instanceof MetaModel mm))
		{
			LOG.info("LMF LSP completion: semantic model is not MetaModel for uri={}, modelClass={}, type completions skipped",
					 uri, model != null ? model.getClass().getName() : "null");
			return Either.forLeft(List.of());
		}

		final var completionContext = new CompletionContext(
			server, uri, pos, state, syntax, semantic, mm, contextKind);

		final List<CompletionItem> typeItems = TypeCompletionProvider.complete(completionContext);
		return Either.forLeft(List.copyOf(typeItems));
	}
}
