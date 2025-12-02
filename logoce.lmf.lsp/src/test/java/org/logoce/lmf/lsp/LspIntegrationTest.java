package org.logoce.lmf.lsp;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.lsp.state.LmDocumentState;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;

final class LspIntegrationTest
{
	@Test
	void carCompanyModelProducesSymbolsAndReferences() throws Exception
	{
		// Resolve path relative to project root; tests are run from subproject dir,
		// so go up one level and reuse the generator test model.
		final Path path = Path.of("..", "logoce.lmf.generator", "src", "test", "model", "CarCompany.lm");
		final String text = Files.readString(path, StandardCharsets.UTF_8);

		final var server = new LmLanguageServer();
		server.connect(new NoopClient());

		final URI uri = path.toUri();
		final var state = new LmDocumentState(uri, 1, text);
		server.workspaceIndex().putDocument(state);

		// Rebuild workspace and wait for completion.
		server.worker().submit(server::rebuildWorkspace).get();

		assertFalse(server.workspaceIndex().symbolIndex().isEmpty(), "symbolIndex should not be empty");
		// CarCompany has at least one @Type reference (e.g. @Entity).
		assertFalse(server.workspaceIndex().referenceIndex().isEmpty(), "referenceIndex should not be empty");
	}
}
