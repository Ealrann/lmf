package org.logoce.lmf.lsp;

import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SemanticTokensStabilityTest
{
	@Test
	void headerKeywordSemanticTokenShouldCoverFullWordAfterEdit() throws Exception
	{
		final String uriString = "file:///workspace/Test.lm";
		final URI uri = URI.create(uriString);

		final String initialText = """
			(MetaModel domain=test.model name=Test
				(Group Type
					(generics T))

				(Definition Group
					(includes group=@Type
						(parameters ^T))
					(Generic T))
			)
			""";

		final var server = new LmLanguageServer();
		server.connect(new NoopClient());
		final var textService = (LmTextDocumentService) server.getTextDocumentService();

		// Open document
		final var openParams = new DidOpenTextDocumentParams(
			new TextDocumentItem(uriString, "lmf", 1, initialText));
		textService.didOpen(openParams);
		server.worker().submit(server::rebuildWorkspace).get();

		// Helper to fetch semantic tokens for current state
		final var tokenParams = new SemanticTokensParams(new TextDocumentIdentifier(uriString));
		final SemanticTokens initialTokens = textService.semanticTokensFull(tokenParams).get();
		assertValidTokenStream(initialTokens, initialText);

		// Simulate removing the trailing 's' from 'parameters' to make it 'parameter'
		final int parametersOffset = initialText.indexOf("parameters");
		final int removeOffset = parametersOffset + "parameter".length();

		final Position removePos = positionForOffset(initialText, removeOffset);
		final var changeRange = new Range(removePos, new Position(removePos.getLine(), removePos.getCharacter() + 1));
		final var changeEvent = new TextDocumentContentChangeEvent();
		changeEvent.setRange(changeRange);
		changeEvent.setText("");

		final var changeId = new VersionedTextDocumentIdentifier(uriString, 2);
		final var changeParams = new DidChangeTextDocumentParams(changeId, List.of(changeEvent));

		textService.didChange(changeParams);
		server.worker().submit(server::rebuildWorkspace).get();

		// Now restore the 's' to get back to 'parameters'
		final var restoreEvent = new TextDocumentContentChangeEvent();
		restoreEvent.setRange(changeRange);
		restoreEvent.setText("s");

		final var restoreId = new VersionedTextDocumentIdentifier(uriString, 3);
		final var restoreParams = new DidChangeTextDocumentParams(restoreId, List.of(restoreEvent));

		textService.didChange(restoreParams);
		server.worker().submit(server::rebuildWorkspace).get();

		final SemanticTokens tokensAfterEdit = textService.semanticTokensFull(tokenParams).get();
		assertValidTokenStream(tokensAfterEdit, initialText);

		// Decode tokens and ensure at least one token fully covers 'parameters'
		final var positions = decodeSemanticTokens(tokensAfterEdit.getData());
		final Position parametersStart = positionForOffset(initialText, parametersOffset);

		final int line = parametersStart.getLine();
		final int startChar = parametersStart.getCharacter();
		final int endChar = startChar + "parameters".length();

		final boolean hasFullCoverage = positions.stream().anyMatch(t ->
			t.line == line &&
			t.startChar <= startChar &&
			t.startChar + t.length >= endChar
		);

		assertTrue(hasFullCoverage,
				   "Expected a semantic token fully covering 'parameters' after edit, but it was not found");

		server.shutdown().join();
	}

	private static Position positionForOffset(final String text, final int offset)
	{
		int line = 0;
		int col = 0;
		for (int i = 0; i < offset && i < text.length(); i++)
		{
			final char c = text.charAt(i);
			if (c == '\n')
			{
				line++;
				col = 0;
			}
			else
			{
				col++;
			}
		}
		return new Position(line, col);
	}

	private record TokenSpan(int line, int startChar, int length) {}

	private static void assertValidTokenStream(final SemanticTokens tokens, final String text)
	{
		final var data = tokens.getData();
		assertTrue(data.size() % 5 == 0, "Semantic tokens data length must be multiple of 5");

		final int totalLines = (int) text.chars().filter(c -> c == '\n').count() + 1;

		int line = 0;
		int character = 0;

		for (int i = 0; i + 4 < data.size(); i += 5)
		{
			final int deltaLine = data.get(i);
			final int deltaStart = data.get(i + 1);
			final int length = data.get(i + 2);

			assertTrue(deltaLine >= 0, "deltaLine must be non-negative");
			assertTrue(deltaStart >= 0, "deltaStart must be non-negative");
			assertTrue(length > 0, "token length must be positive");

			line += deltaLine;
			character = deltaLine == 0 ? character + deltaStart : deltaStart;

			assertTrue(line >= 0 && line < totalLines, "token line out of bounds");
			assertTrue(character >= 0, "token character must be non-negative");
		}

		assertFalse(data.isEmpty(), "Expected non-empty semantic tokens stream");
	}

	private static List<TokenSpan> decodeSemanticTokens(final List<Integer> data)
	{
		final java.util.ArrayList<TokenSpan> result = new java.util.ArrayList<>();
		int line = 0;
		int character = 0;

		for (int i = 0; i + 4 < data.size(); i += 5)
		{
			final int deltaLine = data.get(i);
			final int deltaStart = data.get(i + 1);
			final int length = data.get(i + 2);
			// int tokenType = data.get(i + 3);
			// int tokenModifiers = data.get(i + 4);

			line += deltaLine;
			character = deltaLine == 0 ? character + deltaStart : deltaStart;

			result.add(new TokenSpan(line, character, length));
		}

		return java.util.List.copyOf(result);
	}
}
