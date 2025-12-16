package org.logoce.lmf.lsp;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.lsp.state.LmDocumentState;
import org.logoce.lmf.lsp.state.SemanticSnapshot;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class M1PeugeotIntegrationTest
{
	@Test
	void peugeotM1ModelIsAnalyzedWithoutCrashing() throws Exception
	{
		final Path path = Path.of("..", "logoce.lmf.core.api", "src", "test", "model", "Peugeot.lm");
		final String text = Files.readString(path, StandardCharsets.UTF_8);

		final var server = new LmLanguageServer();
		server.connect(new NoopClient());

		final URI uri = path.toUri();
		final var state = new LmDocumentState(uri, 1, text);
		server.workspaceIndex().putDocument(state);

		server.worker().submit(server::rebuildWorkspace).get();

		final var syntax = state.syntaxSnapshot();
		assertNotNull(syntax, "syntaxSnapshot should not be null for Peugeot.lm");
		assertTrue(syntax.diagnostics().stream().anyMatch(d -> d.message() != null),
				   "Peugeot.lm should produce diagnostics instead of crashing analysis");

		final SemanticSnapshot semantic = state.semanticSnapshot();
		assertNotNull(semantic, "semanticSnapshot should not be null for Peugeot.lm");
		assertNotNull(semantic.linkTrees(), "semanticSnapshot.linkTrees should not be null for Peugeot.lm");
	}
}
