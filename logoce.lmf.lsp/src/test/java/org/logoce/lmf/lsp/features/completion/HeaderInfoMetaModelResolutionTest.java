package org.logoce.lmf.lsp.features.completion;

import org.eclipse.lsp4j.Position;
import org.junit.jupiter.api.Test;
import org.logoce.lmf.lsp.state.SyntaxSnapshot;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.MetaModel;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;

final class HeaderInfoMetaModelResolutionTest
{
	@Test
	void headerInfoUsesActiveMetaModelForM1Root() throws Exception
	{
		// 1) Build the CarCompany meta-model from its M2 definition.
		final var carCompanyPath = Path.of("..", "logoce.lmf.core.api", "src", "test", "model", "CarCompany.lm");
		final var carCompanyText = Files.readString(carCompanyPath, StandardCharsets.UTF_8);

		final var carDiagnostics = new ArrayList<LmDiagnostic>();
		final var treeReader = new LmTreeReader();
		final var carRead = treeReader.read(carCompanyText, carDiagnostics);

		final var linker = new LmModelLinker<PNode>(ModelRegistry.empty());
		final var carLinkResult = linker.linkModel(carRead.roots(), carDiagnostics, carRead.source());

		assertNotNull(carLinkResult.model(), "CarCompany.lm should produce a model");
		final MetaModel carCompanyMetaModel = (MetaModel) carLinkResult.model();

		// 2) Parse the Peugeot M1 model and build a SyntaxSnapshot for header inspection.
		final var peugeotPath = Path.of("..", "logoce.lmf.core.api", "src", "test", "model", "Peugeot.lm");
		final var peugeotText = Files.readString(peugeotPath, StandardCharsets.UTF_8);

		final var peugeotDiagnostics = new ArrayList<LmDiagnostic>();
		final var peugeotRead = treeReader.read(peugeotText, peugeotDiagnostics);

		final var syntaxSnapshot = new SyntaxSnapshot(
			List.of(),
			peugeotRead.roots(),
			peugeotDiagnostics,
			peugeotRead.source());

		// 3) Ask HeaderInfo to resolve the header group for the Peugeot root, using
		//    the CarCompany meta-model as the active meta-model.
		final var pos = new Position(0, 2); // inside "(CarCompany ..."
		final var headerInfo = CompletionContextResolver.HeaderInfo.from(syntaxSnapshot, pos, carCompanyMetaModel);

		assertNotNull(headerInfo, "HeaderInfo should not be null for Peugeot.lm root header");

		final Group<?> headerGroup = headerInfo.headerGroup();
		assertNotNull(headerGroup, "Header group should be resolved using the active meta-model");
		assertEquals("CarCompany", headerGroup.name(), "Header group name should be 'CarCompany'");
	}
}
