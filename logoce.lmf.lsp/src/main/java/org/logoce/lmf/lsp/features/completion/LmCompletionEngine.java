package org.logoce.lmf.lsp.features.completion;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.logoce.lmf.lsp.LmLanguageServer;
import org.logoce.lmf.model.lang.MetaModel;
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
		final var resolvedContext = CompletionContextResolver.resolve(server, uri, pos);
		if (resolvedContext.isEmpty())
		{
			return Either.forLeft(List.of());
		}

		final CompletionContext context = resolvedContext.get();
		final MetaModel metaModel = context.metaModel();
		final CompletionContextKind contextKind = context.contextKind();
		final CompletionContext.HeaderContext header = context.header();
		final CompletionContext.ValueContext value = context.value();
		final CompletionContext.HeaderPositionKind headerPosKind = header != null
																   ? header.positionKind()
																   : CompletionContext.HeaderPositionKind.OTHER;

		// 1) Value position inside a header: prefer attribute/relation-specific completions.
		if (headerPosKind == CompletionContext.HeaderPositionKind.FEATURE_VALUE && value != null)
		{
			if (value.attribute() != null)
			{
				final var items = AttributeValueCompletionProvider.complete(context);
				if (!items.isEmpty())
				{
					LOG.debug("LMF LSP completion: attribute completion, uri={}, line={}, character={}, items={}",
							  uri, pos.getLine(), pos.getCharacter(), items.size());
					return Either.forLeft(List.copyOf(items));
				}
				LOG.debug("LMF LSP completion: attribute completion produced no items at uri={}, line={}, character={}",
						  uri, pos.getLine(), pos.getCharacter());
			}

			if (value.relation() != null)
			{
				final var items = RelationValueCompletionProvider.complete(context);
				if (!items.isEmpty())
				{
					LOG.debug("LMF LSP completion: relation completion, uri={}, line={}, character={}, items={}",
							  uri, pos.getLine(), pos.getCharacter(), items.size());
					return Either.forLeft(List.copyOf(items));
				}
				LOG.debug("LMF LSP completion: relation completion produced no items at uri={}, line={}, character={}",
						  uri, pos.getLine(), pos.getCharacter());
			}
		}

		// 2) Explicit local ('@') or cross-model ('#') reference: type completions only.
		if (contextKind == CompletionContextKind.LOCAL_AT || contextKind == CompletionContextKind.CROSS_MODEL_HASH)
		{
			if (metaModel == null)
			{
				LOG.debug("LMF LSP completion: type completion skipped (no metaModel) at uri={}, line={}, character={}",
						  uri, pos.getLine(), pos.getCharacter());
				return Either.forLeft(List.of());
			}

			final List<CompletionItem> typeItems = TypeCompletionProvider.complete(context);

			if (!typeItems.isEmpty())
			{
				LOG.debug("LMF LSP completion: type completion, uri={}, line={}, character={}, items={}",
						  uri, pos.getLine(), pos.getCharacter(), typeItems.size());
				return Either.forLeft(List.copyOf(typeItems));
			}
			LOG.debug("LMF LSP completion: type completion produced no items at uri={}, line={}, character={}",
					  uri, pos.getLine(), pos.getCharacter());
		}

		// 3) Header keyword, name or feature name position: propose header features only.
		if (headerPosKind == CompletionContext.HeaderPositionKind.HEADER_KEYWORD ||
			headerPosKind == CompletionContext.HeaderPositionKind.HEADER_NAME ||
			headerPosKind == CompletionContext.HeaderPositionKind.FEATURE_NAME)
		{
			final var featureItems = GroupFeatureCompletionProvider.complete(context);
			if (!featureItems.isEmpty())
			{
				LOG.debug("LMF LSP completion: feature completion, uri={}, line={}, character={}, items={}",
						  uri, pos.getLine(), pos.getCharacter(), featureItems.size());
				return Either.forLeft(List.copyOf(featureItems));
			}
			LOG.debug("LMF LSP completion: feature completion produced no items at uri={}, line={}, character={}",
					  uri, pos.getLine(), pos.getCharacter());
			return Either.forLeft(List.of());
		}

		// 4) Fallback inside a group: header features and containment children.
		final var groupItems = GroupFeatureCompletionProvider.complete(context);
		if (!groupItems.isEmpty())
		{
			LOG.debug("LMF LSP completion: group context completion, uri={}, line={}, character={}, items={}",
					  uri, pos.getLine(), pos.getCharacter(), groupItems.size());
			return Either.forLeft(List.copyOf(groupItems));
		}

		LOG.debug("LMF LSP completion: no completions for uri={}, line={}, character={}",
				  uri, pos.getLine(), pos.getCharacter());
		return Either.forLeft(List.of());
	}

}
