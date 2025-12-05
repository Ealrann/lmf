package org.logoce.lmf.lsp;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.lsp.state.LmDocumentState;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MultiModelWorkspaceIntegrationTest
{
	@Test
	void graphMultiModelShouldLinkWithoutImportErrors() throws Exception
	{
		final Path corePath = Path.of("..", "logoce.lmf.generator", "src", "test", "model", "GraphCore.lm");
		final Path extensionsPath = Path.of("..", "logoce.lmf.generator", "src", "test", "model", "GraphExtensions.lm");
		final Path analysisPath = Path.of("..", "logoce.lmf.generator", "src", "test", "model", "GraphAnalysis.lm");

		final String coreText = Files.readString(corePath, StandardCharsets.UTF_8);
		final String extensionsText = Files.readString(extensionsPath, StandardCharsets.UTF_8);
		final String analysisText = Files.readString(analysisPath, StandardCharsets.UTF_8);

		final var server = new LmLanguageServer();
		server.connect(new NoopClient());

		final URI coreUri = corePath.toUri();
		final URI extensionsUri = extensionsPath.toUri();
		final URI analysisUri = analysisPath.toUri();

		final var coreState = new LmDocumentState(coreUri, 1, coreText);
		final var extensionsState = new LmDocumentState(extensionsUri, 1, extensionsText);
		final var analysisState = new LmDocumentState(analysisUri, 1, analysisText);

		server.workspaceIndex().putDocument(coreState);
		server.workspaceIndex().putDocument(extensionsState);
		server.workspaceIndex().putDocument(analysisState);

		server.worker().submit(server::rebuildWorkspace).get();

		final var state = server.workspaceIndex().documents().get(analysisUri);
		assertNotNull(state, "GraphAnalysis.lm document state should be present");
		assertNotNull(state.syntaxSnapshot(), "syntaxSnapshot should not be null for GraphAnalysis.lm");

		// Desired behaviour: when all related meta-models are open, the multi-model
		// import graph should resolve without link errors such as
		// "Cannot resolve model 'GraphExtensions' in registry".
		// This currently fails, reproducing the warnings seen in the IDE logs.
		assertTrue(state.syntaxSnapshot().diagnostics().stream().noneMatch(d ->
				d.message() != null && d.message().contains("Cannot resolve model 'GraphExtensions'")),
				   "GraphAnalysis.lm should not report unresolved 'GraphExtensions' model when all graph models are open");
	}
}

