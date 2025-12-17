package org.logoce.lmf.lsp;

import org.eclipse.lsp4j.SemanticTokens;
import org.logoce.lmf.lsp.state.LmDocumentState;
import org.logoce.lmf.lsp.state.SyntaxSnapshot;
import org.logoce.lmf.lsp.state.WorkspaceIndex;
import org.logoce.lmf.core.loader.api.text.syntax.PToken;
import org.logoce.lmf.core.loader.api.loader.util.TextPositions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

final class SemanticTokensProvider
{
	private static final Logger LOG = LoggerFactory.getLogger(SemanticTokensProvider.class);

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

		final List<int[]> headerTokens = collectHeaderTokens(syntax, source);
		if (headerTokens.isEmpty())
		{
			LOG.info("LMF LSP semanticTokensFull: uri={} no header tokens, tokens=0, durationMs={}",
					 uri, (System.nanoTime() - startNanos) / 1_000_000);
			return new SemanticTokens(List.of());
		}

		headerTokens.sort((a, b) -> {
			final int cmpLine = Integer.compare(a[0], b[0]);
			if (cmpLine != 0) return cmpLine;
			return Integer.compare(a[1], b[1]);
		});

		int prevLine = 0;
		int prevChar = 0;
		final int tokenTypeIndex = 0;
		final int modifiers = 0;

		for (final int[] token : headerTokens)
		{
			final int line = token[0];
			final int character = token[1];
			final int length = token[2];

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

		final var tokens = new SemanticTokens(data);
		LOG.info("LMF LSP semanticTokensFull: uri={}, version={}, sourceLength={}, tokens={}, durationMs={}",
				 uri, state.version(), source.length(), data.size() / 5, (System.nanoTime() - startNanos) / 1_000_000);
		return tokens;
	}

	private static List<int[]> collectHeaderTokens(final SyntaxSnapshot syntax, final CharSequence source)
	{
		final var headerTokens = new ArrayList<int[]>();
		for (final var root : syntax.roots())
		{
			for (final var node : root.streamTree().toList())
			{
				final var pnode = node.data();
				final var tokens = pnode.tokens();
				if (tokens.isEmpty())
				{
					continue;
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
					continue;
				}

				final int offset = headerToken.offset();
				final int line = Math.max(0, TextPositions.lineFor(source, offset) - 1);
				final int character = Math.max(0, TextPositions.columnFor(source, offset) - 1);
				final int length = Math.max(1, headerToken.length());

				headerTokens.add(new int[]{line, character, length});
			}
		}
		return headerTokens;
	}
}
