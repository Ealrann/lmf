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
			LOG.debug("LMF LSP completion: missing snapshots for uri={}, syntaxNull={}, semanticNull={}",
					  uri, syntax == null, semantic == null);
			return Either.forLeft(List.of());
		}

		final Model model = semantic.model();
		final MetaModel metaModel = model instanceof MetaModel mm ? mm : null;
		final CompletionContextKind contextKind = SyntaxNavigation.detectCompletionContext(syntax, pos);
		final var context = new CompletionContext(server,
												 uri,
												 pos,
												 state,
												 syntax,
												 semantic,
												 metaModel,
												 contextKind);

		// 1) Attribute value completions, based on datatype.
		final var valueItems = AttributeValueCompletionProvider.complete(context);
		if (!valueItems.isEmpty())
		{
			LOG.info("LMF LSP completion: attribute completion, uri={}, line={}, character={}, items={}",
					 uri, pos.getLine(), pos.getCharacter(), valueItems.size());
			return Either.forLeft(List.copyOf(valueItems));
		}
		else
		{
			LOG.debug("LMF LSP completion: attribute completion produced no items at uri={}, line={}, character={}",
					  uri, pos.getLine(), pos.getCharacter());
		}

		// 1b) Relation value completions, based on relation concept.
		final var relationItems = RelationValueCompletionProvider.complete(context);
		if (!relationItems.isEmpty())
		{
			LOG.info("LMF LSP completion: relation completion, uri={}, line={}, character={}, items={}",
					 uri, pos.getLine(), pos.getCharacter(), relationItems.size());
			return Either.forLeft(List.copyOf(relationItems));
		}
		else
		{
			LOG.debug("LMF LSP completion: relation completion produced no items at uri={}, line={}, character={}",
					  uri, pos.getLine(), pos.getCharacter());
		}

		// 1c) Type completions (Attribute.datatype, Relation.concept, etc.) are only
		// relevant when the user is explicitly typing a local ('@') or cross-model
		// ('#') reference. In all other contexts (header feature names, plain values),
		// attribute/relation-specific completions or group feature completions should
		// take precedence.
		if (contextKind == CompletionContextKind.LOCAL_AT || contextKind == CompletionContextKind.CROSS_MODEL_HASH)
		{
			final List<CompletionItem> typeItems;
			if (metaModel != null)
			{
				typeItems = TypeCompletionProvider.complete(context);
			}
			else
			{
				typeItems = TypeCompletionProvider.completeFromSyntax(server.workspaceIndex().modelRegistry(),
																	  syntax,
																	  pos,
																	  contextKind);
			}

			if (!typeItems.isEmpty())
			{
				LOG.info("LMF LSP completion: type completion, uri={}, line={}, character={}, items={}",
						 uri, pos.getLine(), pos.getCharacter(), typeItems.size());
				return Either.forLeft(List.copyOf(typeItems));
			}
			else
			{
				LOG.debug("LMF LSP completion: type completion produced no items at uri={}, line={}, character={}",
						  uri, pos.getLine(), pos.getCharacter());
			}
		}

		// 2) Group feature completions (attributes of headers, etc.).
		final var featureItems = GroupFeatureCompletionProvider.complete(context);
		if (!featureItems.isEmpty())
		{
			LOG.info("LMF LSP completion: feature completion, uri={}, line={}, character={}, items={}",
					 uri, pos.getLine(), pos.getCharacter(), featureItems.size());
			return Either.forLeft(List.copyOf(featureItems));
		}

		LOG.debug("LMF LSP completion: no completions for uri={}, line={}, character={}",
				  uri, pos.getLine(), pos.getCharacter());
		return Either.forLeft(List.of());
	}

}
