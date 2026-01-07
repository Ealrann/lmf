package org.logoce.lmf.lsp;

import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.junit.jupiter.api.Test;
import org.logoce.lmf.core.loader.api.tooling.state.LmDocumentState;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;

final class SemanticTokensTest
{
	@Test
	void semanticTokensTriggerAnalysisWhenSyntaxMissing() throws Exception
	{
		final var text = """
			(MetaModel domain=test.tokens name=Tokens
			    (Group LMObject)
			    (Group Entity
			        (includes group=@LMObject))
			)
			""";

		final var uri = URI.create("file:///test/SemanticTokens.lm");

		final var server = new LmLanguageServer();
		try
		{
			final var state = new LmDocumentState(uri, 1, text);
			server.workspaceIndex().putDocument(state);

			final var params = new SemanticTokensParams();
			params.setTextDocument(new TextDocumentIdentifier(uri.toString()));

			final var future = server.getTextDocumentService().semanticTokensFull(params);
			final SemanticTokens tokens = future.get(10, TimeUnit.SECONDS);

			assertFalse(tokens.getData().isEmpty(),
						"Expected semantic tokens to be produced after on-demand analysis");
		}
		finally
		{
			server.shutdown().join();
		}
	}
}
