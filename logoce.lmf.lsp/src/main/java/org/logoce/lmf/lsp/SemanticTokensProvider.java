package org.logoce.lmf.lsp;

import org.eclipse.lsp4j.SemanticTokens;
import org.logoce.lmf.core.lang.Attribute;
import org.logoce.lmf.core.loader.api.lexer.ELMTokenType;
import org.logoce.lmf.core.loader.api.loader.linking.LinkNode;
import org.logoce.lmf.core.loader.api.loader.linking.ResolutionAttempt;
import org.logoce.lmf.core.loader.api.loader.linking.tree.LinkNodeFull;
import org.logoce.lmf.core.loader.api.loader.util.TextPositions;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;
import org.logoce.lmf.core.loader.api.text.syntax.PToken;
import org.logoce.lmf.core.loader.feature.AttributeResolver;
import org.logoce.lmf.lsp.state.LmDocumentState;
import org.logoce.lmf.lsp.state.SemanticSnapshot;
import org.logoce.lmf.lsp.state.SyntaxSnapshot;
import org.logoce.lmf.lsp.state.WorkspaceIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

final class SemanticTokensProvider
{
	private static final Logger LOG = LoggerFactory.getLogger(SemanticTokensProvider.class);
	private static final int TOKEN_TYPE_KEYWORD = 0;
	private static final int TOKEN_TYPE_NAME = 1;

	private SemanticTokensProvider()
	{
	}

	static SemanticTokens computeSemanticTokens(final LmLanguageServer server, final URI uri)
	{
		final long startNanos = System.nanoTime();
		final WorkspaceIndex index = server.workspaceIndex();
		final LmDocumentState state = index.getDocument(uri);
		if (state == null)
		{
			LOG.info("LMF LSP semanticTokensFull: uri={} no document state, tokens=0, durationMs={}",
					 uri, (System.nanoTime() - startNanos) / 1_000_000);
			return new SemanticTokens(List.of());
		}

		SyntaxSnapshot syntax = state.syntaxSnapshot();
		if (syntax == null)
		{
			LOG.info("LMF LSP semanticTokensFull: uri={} has no syntax snapshot yet, analyzing document", uri);
			server.analyzeDocument(state);
			syntax = state.syntaxSnapshot();
			if (syntax == null)
			{
				final SyntaxSnapshot lastGood = state.lastGoodSyntaxSnapshot();
				if (lastGood != null)
				{
					LOG.info("LMF LSP semanticTokensFull: uri={} using lastGoodSyntaxSnapshot", uri);
					syntax = lastGood;
				}
			}

			if (syntax == null)
			{
				LOG.info("LMF LSP semanticTokensFull: uri={} no syntax available, tokens=0, durationMs={}",
						 uri, (System.nanoTime() - startNanos) / 1_000_000);
				return new SemanticTokens(List.of());
			}
		}

		final List<Integer> data = new ArrayList<>();
		final CharSequence source = syntax.source();
		final TextPositions.LineIndex lineIndex = syntax.lineIndex();

		final var tokens = new ArrayList<TokenSpan>();
		tokens.addAll(collectHeaderTokens(syntax, lineIndex));

		final SemanticSnapshot semantic = state.lastGoodSemanticSnapshot() != null
										  ? state.lastGoodSemanticSnapshot()
										  : state.semanticSnapshot();
		tokens.addAll(collectNameTokens(semantic, lineIndex));

		if (tokens.isEmpty())
		{
			LOG.info("LMF LSP semanticTokensFull: uri={} no tokens, tokens=0, durationMs={}",
					 uri, (System.nanoTime() - startNanos) / 1_000_000);
			return new SemanticTokens(List.of());
		}

		tokens.sort((a, b) -> {
			final int cmpLine = Integer.compare(a.line(), b.line());
			if (cmpLine != 0) return cmpLine;
			return Integer.compare(a.character(), b.character());
		});

		int prevLine = 0;
		int prevChar = 0;
		final int modifiers = 0;

		for (final TokenSpan token : tokens)
		{
			final int line = token.line();
			final int character = token.character();
			final int length = token.length();
			final int tokenTypeIndex = token.tokenType();

			final int deltaLine = line - prevLine;
			final int deltaStart = deltaLine == 0 ? character - prevChar : character;

			data.add(deltaLine);
			data.add(deltaStart);
			data.add(length);
			data.add(tokenTypeIndex);
			data.add(modifiers);

			prevLine = line;
			prevChar = character;
		}

		final var semanticTokens = new SemanticTokens(data);
		LOG.info("LMF LSP semanticTokensFull: uri={}, version={}, sourceLength={}, tokens={}, durationMs={}",
				 uri, state.version(), source.length(), data.size() / 5, (System.nanoTime() - startNanos) / 1_000_000);
		return semanticTokens;
	}

	private static List<TokenSpan> collectHeaderTokens(final SyntaxSnapshot syntax,
													   final TextPositions.LineIndex lineIndex)
	{
		final var headerTokens = new ArrayList<TokenSpan>();
		for (final var root : syntax.roots())
		{
			root.streamTree().forEach(node -> {
				final var pnode = node.data();
				final var tokens = pnode.tokens();
				if (tokens.isEmpty())
				{
					return;
				}

				PToken headerToken = null;
				for (final var tok : tokens)
				{
					final String value = tok.value();
					if (value == null || value.isBlank() || "(".equals(value))
					{
						continue;
					}
					headerToken = tok;
					break;
				}

				if (headerToken == null)
				{
					return;
				}

				final int offset = headerToken.offset();
				final int line = Math.max(0, lineIndex.lineFor(offset) - 1);
				final int character = Math.max(0, lineIndex.columnFor(offset) - 1);
				final int length = Math.max(1, headerToken.length());

				headerTokens.add(new TokenSpan(line, character, length, TOKEN_TYPE_KEYWORD));
			});
		}
		return headerTokens;
	}

	private static List<TokenSpan> collectNameTokens(final SemanticSnapshot semantic,
													 final TextPositions.LineIndex lineIndex)
	{
		if (semantic == null || semantic.linkTrees().isEmpty())
		{
			return List.of();
		}

		final var nameTokens = new ArrayList<TokenSpan>();
		for (final LinkNode<?, PNode> root : semantic.linkTrees())
		{
			if (root instanceof LinkNodeFull<?, PNode> fullRoot)
			{
				fullRoot.streamTree().forEach(node -> collectNameTokens(node, lineIndex, nameTokens));
			}
		}
		return nameTokens;
	}

	private static void collectNameTokens(final LinkNode<?, PNode> node,
										  final TextPositions.LineIndex lineIndex,
										  final List<TokenSpan> tokens)
	{
		for (final ResolutionAttempt<Attribute<?, ?, ?, ?>> attempt : node.attributeResolutions())
		{
			final var resolution = attempt.resolution();
			if (!(resolution instanceof AttributeResolver.AttributeResolution<?> attributeResolution))
			{
				continue;
			}

			final var feature = attributeResolution.feature();
			if (feature == null || !"name".equals(feature.name()))
			{
				continue;
			}

			String rawName = null;
			final var featureToken = attempt.feature();
			if (featureToken != null && !featureToken.values().isEmpty())
			{
				rawName = featureToken.values().getFirst();
			}
			if (rawName == null || rawName.isBlank())
			{
				rawName = attributeResolution.value();
			}
			if (rawName == null || rawName.isBlank())
			{
				continue;
			}

			final var tokenList = node.pNode().tokens();
			PToken valueToken = null;
			final boolean explicit = featureToken != null && featureToken.name().isPresent();
			if (explicit)
			{
				valueToken = findAssignedValueToken(tokenList, featureToken.name().get());
			}
			if (valueToken == null)
			{
				valueToken = findMatchingValueToken(tokenList, rawName, explicit);
			}
			if (valueToken == null)
			{
				continue;
			}

			final int line = Math.max(0, lineIndex.lineFor(valueToken.offset()) - 1);
			final int character = Math.max(0, lineIndex.columnFor(valueToken.offset()) - 1);
			final int length = Math.max(1, valueToken.length());
			tokens.add(new TokenSpan(line, character, length, TOKEN_TYPE_NAME));
		}
	}

	private static PToken findAssignedValueToken(final List<PToken> tokens, final String key)
	{
		if (key == null || key.isBlank())
		{
			return null;
		}

		for (int i = 0; i < tokens.size(); i++)
		{
			final var token = tokens.get(i);
			if (token.type() == ELMTokenType.VALUE_NAME && key.equals(token.value()))
			{
				for (int j = i + 1; j < tokens.size(); j++)
				{
					final var next = tokens.get(j);
					switch (next.type())
					{
						case VALUE -> {
							return next;
						}
						case ASSIGN, WHITE_SPACE, LIST_SEPARATOR, QUOTE -> {
							continue;
						}
						default -> {
							return null;
						}
					}
				}
			}
		}

		return null;
	}

	private static PToken findMatchingValueToken(final List<PToken> tokens,
												 final String raw,
												 final boolean preferLast)
	{
		PToken match = null;
		for (final var token : tokens)
		{
			if (token == null) continue;
			if (raw.equals(token.value()) == false) continue;

			switch (token.type())
			{
				case ASSIGN, WHITE_SPACE, LIST_SEPARATOR, QUOTE, OPEN_NODE, CLOSE_NODE -> {
					continue;
				}
				default -> {
					if (preferLast)
					{
						match = token;
					}
					else
					{
						return token;
					}
				}
			}
		}
		return match;
	}

	private record TokenSpan(int line, int character, int length, int tokenType)
	{
	}
}
