package org.logoce.lmf.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.logoce.lmf.cli.edit.TextEdits;
import org.logoce.lmf.cli.remove.RemoveModelDocument;
import org.logoce.lmf.cli.remove.RemovePlanResult;
import org.logoce.lmf.cli.remove.RemovePlanner;
import org.logoce.lmf.cli.remove.RemoveWorkspace;
import org.logoce.lmf.cli.workspace.DocumentLoader;
import org.logoce.lmf.cli.workspace.RegistryService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RemovePlannerTest
{
	@Test
	void plannerBuildsEditsForUnsetsAndShifts(@TempDir final Path workspace) throws Exception
	{
		writeModels(workspace);
		final var modelAPath = workspace.resolve("ModelA.lm");
		final var modelBPath = workspace.resolve("ModelB.lm");

		final var errBuffer = new StringWriter();
		final var err = new PrintWriter(errBuffer);
		final var documentLoader = new DocumentLoader();
		final var registryService = new RegistryService(workspace, documentLoader);

		final var prepare = registryService.prepareForModelAndImporters(modelAPath, err, true);
		assertTrue(prepare instanceof RegistryService.PrepareWorkspaceResult.Success, errBuffer.toString());

		final var prepared = ((RegistryService.PrepareWorkspaceResult.Success) prepare).workspace();
		final var registry = prepared.registry();
		final var scanPaths = prepared.scanModelPaths();

		final var documents = new ArrayList<RemoveModelDocument>();
		RemoveModelDocument target = null;

		for (final var entry : scanPaths.entrySet())
		{
			final var qualifiedName = entry.getKey();
			final var path = entry.getValue();
			final var doc = documentLoader.loadModelFromPath(registry, qualifiedName, path, err);
			final var modelDoc = new RemoveModelDocument(path, doc);
			documents.add(modelDoc);
			if (qualifiedName.equals(prepared.targetQualifiedName()))
			{
				target = modelDoc;
			}
		}

		final var planner = new RemovePlanner();
		final var workspaceInfo = new RemoveWorkspace(target, documents);
		final var result = planner.plan(workspaceInfo, "/materials/materials.1");
		assertTrue(result instanceof RemovePlanResult.Success, "planning failed: " + result);

		final var planned = ((RemovePlanResult.Success) result).edit();
		final var edits = planned.editsByFile();

		final var originalA = Files.readString(modelAPath);
		final var originalB = Files.readString(modelBPath);

		final var updatedA = TextEdits.apply(originalA, edits.get(modelAPath));
		final var updatedB = TextEdits.apply(originalB, edits.get(modelBPath));

		assertFalse(updatedA.contains("Material name=Dirt"), "updatedA:\n" + updatedA);
		assertFalse(updatedA.contains("mainMaterial="), "updatedA:\n" + updatedA);
		assertTrue(updatedA.contains("altMaterial=/materials/materials.1"), "updatedA:\n" + updatedA);
		assertTrue(updatedB.contains("altMaterial=#ModelA/materials/materials.1"), "updatedB:\n" + updatedB);

		assertTrue(planned.unsets().size() == 1, "unsets: " + planned.unsets());
		assertTrue(planned.unsets().getFirst().raw().contains("/materials/materials.1"));
	}

	private static void writeModels(final Path workspace) throws Exception
	{
		final var meta = """
			(MetaModel domain=test.model name=RemoveMeta
				(Group Named (includes group=#LMCore@Named))
				(Definition Material (includes group=@Named))
				(Definition MaterialsEnv (includes group=#LMCore@LMObject)
					(+contains materials [0..*] @Material))
				(Definition Root (includes group=#LMCore@Model)
					(+contains materials [1..1] @MaterialsEnv)
					(+refers mainMaterial [0..1] @Material)
					(+refers altMaterial [0..1] @Material)))
			""";

		final var modelA = """
			(Root domain=test.model name=ModelA metamodels=test.model.RemoveMeta
				mainMaterial=/materials/materials.1
				altMaterial=/materials/materials.2
				(MaterialsEnv
					(Material name=Lava)
					(Material name=Dirt)
					(Material name=Stone)))
			""";

		final var modelB = """
			(Root domain=test.model name=ModelB metamodels=test.model.RemoveMeta imports=test.model.ModelA
				altMaterial=#ModelA/materials/materials.2
				(MaterialsEnv))
			""";

		Files.writeString(workspace.resolve("RemoveMeta.lm"), meta);
		Files.writeString(workspace.resolve("ModelA.lm"), modelA);
		Files.writeString(workspace.resolve("ModelB.lm"), modelB);
	}
}
