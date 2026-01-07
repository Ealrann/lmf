package org.logoce.lmf.lsp.features.completion;

import org.eclipse.lsp4j.Position;
import org.junit.jupiter.api.Test;
import org.logoce.lmf.core.loader.api.tooling.state.SemanticSnapshot;
import org.logoce.lmf.core.loader.api.tooling.state.SymbolTable;
import org.logoce.lmf.core.loader.api.tooling.state.SyntaxSnapshot;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.loader.api.loader.linking.LmModelLinker;
import org.logoce.lmf.core.loader.api.loader.parsing.LmTreeReader;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;
import org.logoce.lmf.core.api.model.ModelRegistry;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

final class GroupFeatureCompletionProviderTest
{
	@Test
	void completesOperationAndParameterFeaturesInNativeGenerics() throws Exception
	{
		final var path = Path.of("..", "logoce.lmf.core.generator", "src", "test", "model", "NativeGenerics.lm");
		final var text = Files.readString(path, StandardCharsets.UTF_8);

		final var syntaxDiagnostics = new ArrayList<LmDiagnostic>();
		final var treeReader = new LmTreeReader();
		final var readResult = treeReader.read(text, syntaxDiagnostics);

		final var syntaxSnapshot = new SyntaxSnapshot(
			List.of(),
			readResult.roots(),
			syntaxDiagnostics,
			readResult.source());

		final var semanticDiagnostics = new ArrayList<LmDiagnostic>();
		final var linker = new LmModelLinker<PNode>(ModelRegistry.empty());
		final var linkResult = linker.linkModel(readResult.roots(), semanticDiagnostics, readResult.source());

		final var semanticSnapshot = new SemanticSnapshot(
			linkResult.model(),
			linkResult.trees(),
			semanticDiagnostics,
			SymbolTable.EMPTY,
			List.of());

		final var firstOperationPos = new Position(12, 13);
		final var secondOperationPos = new Position(13, 13);
		final var parameterPos = new Position(14, 23);

		final var firstContext = new CompletionContext(
			null,
			java.net.URI.create("file:///test"),
			firstOperationPos,
			null,
			syntaxSnapshot,
			semanticSnapshot,
			null,
			CompletionContextKind.DEFAULT,
			null,
			null);
		final var firstOpItems = GroupFeatureCompletionProvider.complete(firstContext);
		assertFalse(firstOpItems.isEmpty(), "First operation should produce feature completions");
		assertEquals("Feature of Operation", firstOpItems.getFirst().getDetail());

		final var secondContext = new CompletionContext(
			null,
			java.net.URI.create("file:///test"),
			secondOperationPos,
			null,
			syntaxSnapshot,
			semanticSnapshot,
			null,
			CompletionContextKind.DEFAULT,
			null,
			null);
		final var secondOpItems = GroupFeatureCompletionProvider.complete(secondContext);
		assertFalse(secondOpItems.isEmpty(), "Second operation should produce feature completions");
		assertEquals("Feature of Operation", secondOpItems.getFirst().getDetail());

		final var parameterContext = new CompletionContext(
			null,
			java.net.URI.create("file:///test"),
			parameterPos,
			null,
			syntaxSnapshot,
			semanticSnapshot,
			null,
			CompletionContextKind.DEFAULT,
			null,
			null);
		final var parameterItems = GroupFeatureCompletionProvider.complete(parameterContext);
		assertFalse(parameterItems.isEmpty(), "OperationParameter should produce feature completions");
		assertEquals("Feature of OperationParameter", parameterItems.getFirst().getDetail());
	}
}
