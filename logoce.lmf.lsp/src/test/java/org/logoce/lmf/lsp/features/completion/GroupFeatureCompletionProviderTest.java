package org.logoce.lmf.lsp.features.completion;

import org.eclipse.lsp4j.Position;
import org.junit.jupiter.api.Test;
import org.logoce.lmf.lsp.state.SemanticSnapshot;
import org.logoce.lmf.lsp.state.SymbolTable;
import org.logoce.lmf.lsp.state.SyntaxSnapshot;
import org.logoce.lmf.model.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.model.loader.linking.LmModelLinker;
import org.logoce.lmf.model.loader.parsing.LmTreeReader;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.util.ModelRegistry;

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
		final var path = Path.of("..", "logoce.lmf.generator", "src", "test", "model", "NativeGenerics.lm");
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

		final var firstOpItems = GroupFeatureCompletionProvider.complete(semanticSnapshot, syntaxSnapshot, firstOperationPos);
		assertFalse(firstOpItems.isEmpty(), "First operation should produce feature completions");
		assertEquals("Feature of Operation", firstOpItems.getFirst().getDetail());

		final var secondOpItems = GroupFeatureCompletionProvider.complete(semanticSnapshot, syntaxSnapshot, secondOperationPos);
		assertFalse(secondOpItems.isEmpty(), "Second operation should produce feature completions");
		assertEquals("Feature of Operation", secondOpItems.getFirst().getDetail());

		final var parameterItems = GroupFeatureCompletionProvider.complete(semanticSnapshot, syntaxSnapshot, parameterPos);
		assertFalse(parameterItems.isEmpty(), "OperationParameter should produce feature completions");
		assertEquals("Feature of OperationParameter", parameterItems.getFirst().getDetail());
	}
}
