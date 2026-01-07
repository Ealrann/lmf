package org.logoce.lmf.cli;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.cli.diagnostics.DiagnosticReporter;
import org.logoce.lmf.cli.format.LmFormatter;
import org.logoce.lmf.cli.format.ReferencePathToNameIndex;
import org.logoce.lmf.cli.format.RootReferenceResolver;
import org.logoce.lmf.cli.workspace.ProjectModelLoader;
import org.logoce.lmf.core.api.model.ModelRegistry;
import org.logoce.lmf.core.lang.LMCoreModelPackage;
import org.logoce.lmf.core.lang.MetaModel;
import org.logoce.lmf.core.loader.api.loader.LmLoader;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FmtCommandTest
{
	@Test
	void fmtResolvesAtReference() throws Exception
	{
		final var repoRoot = findRepoRoot();
		final var modelPath = repoRoot.resolve("logoce.lmf.core.generator/src/test/model/GraphAnalysis.lm");
		final var loader = new ProjectModelLoader(repoRoot);
		final var result = loader.load(modelPath);

		assertFalse(DiagnosticReporter.hasErrors(result.document().diagnostics()));

		final var linkRoots = RootReferenceResolver.collectLinkRoots(result.document().linkTrees());
		final var resolver = new RootReferenceResolver();
		final var resolution = resolver.resolve(linkRoots, "@GraphView");

		assertTrue(resolution instanceof RootReferenceResolver.Resolution.Found);
		final var found = (RootReferenceResolver.Resolution.Found) resolution;
		final var formatted = new LmFormatter().format(found.node());

		assertTrue(formatted.startsWith("(Definition GraphView"));
	}

	@Test
	void fmtResolvesIndexedPath() throws Exception
	{
		final var repoRoot = findRepoRoot();
		final var modelPath = repoRoot.resolve("logoce.lmf.core.api/src/main/model/asset/LMCore.lm");
		final var loader = new ProjectModelLoader(repoRoot);
		final var result = loader.load(modelPath);

		assertFalse(DiagnosticReporter.hasErrors(result.document().diagnostics()));

		final var linkRoots = RootReferenceResolver.collectLinkRoots(result.document().linkTrees());
		final var resolver = new RootReferenceResolver();
		final var resolution = resolver.resolve(linkRoots, "/units.1");

		assertTrue(resolution instanceof RootReferenceResolver.Resolution.Found);
		final var found = (RootReferenceResolver.Resolution.Found) resolution;
		final var formatted = new LmFormatter().format(found.node());

		assertTrue(formatted.contains("name=extractor"));
	}

	@Test
	void fmtRefPathToName_rewritesUniqueAbsolutePaths() throws Exception
	{
		final var repoRoot = findRepoRoot();
		final var modelPath = repoRoot.resolve("logoce.lmf.core.api/src/main/model/asset/LMCore.lm");
		final var loader = new ProjectModelLoader(repoRoot);
		final var result = loader.load(modelPath);

		assertFalse(DiagnosticReporter.hasErrors(result.document().diagnostics()));

		final var linkRoots = RootReferenceResolver.collectLinkRoots(result.document().linkTrees());
		assertTrue(linkRoots.size() == 1);
		final var index = ReferencePathToNameIndex.build(linkRoots.getFirst());

		final var formatted = new LmFormatter().format(result.document().roots(), root -> index);
		assertTrue(formatted.contains("(-att name=defaultValue @string)"));
		assertFalse(formatted.contains("/units.7"));
	}

	@Test
	void fmtRefPathToName_normalizesMissingIndexZeroSegments() throws Exception
	{
		final var repoRoot = findRepoRoot();
		final var modelPath = repoRoot.resolve("logoce.lmf.core.api/src/main/model/asset/LMCore.lm");
		final var loader = new ProjectModelLoader(repoRoot);
		final var result = loader.load(modelPath);

		assertFalse(DiagnosticReporter.hasErrors(result.document().diagnostics()));

		final var linkRoots = RootReferenceResolver.collectLinkRoots(result.document().linkTrees());
		assertTrue(linkRoots.size() == 1);
		final var index = ReferencePathToNameIndex.build(linkRoots.getFirst());

		assertTrue("@LMObject".equals(index.replacementForAbsolutePath("/groups")));
		assertTrue("@BoundType".equals(index.replacementForAbsolutePath("/enums")));
	}

	@Test
	void fmtRefPathToName_keepsPathWhenNameIsNotUnique() throws Exception
	{
		final var source = """
			(MetaModel domain=test.model name=DupNames
				(Unit name=u)
				(Unit name=u)
				(Definition A
					(-att x /units.0 [0..1])))
			""";

		final var loader = new LmLoader(ModelRegistry.empty());
		final var doc = loader.loadModel(source);

		assertTrue(doc.diagnostics()
					  .stream()
					  .noneMatch(d -> d.severity() == LmDiagnostic.Severity.ERROR));

		final var linkRoots = RootReferenceResolver.collectLinkRoots(doc.linkTrees());
		assertTrue(linkRoots.size() == 1);
		final var index = ReferencePathToNameIndex.build(linkRoots.getFirst());

		final var formatted = new LmFormatter().format(doc.roots(), root -> index);
		assertTrue(formatted.contains("(-att x /units.0 [0..1])"));
	}

	@Test
	void fmtRefPathToName_rewritesWhenUniqueInConceptGroup() throws Exception
	{
		final var metaSource = """
			(MetaModel domain=test.model name=RewriteMeta
				(Group Named (includes group=#LMCore@Named))
				(Definition Material (includes group=@Named))
				(Definition Sound (includes group=@Named))
				(Definition MaterialEnv (includes group=#LMCore@LMObject)
					(+contains materials [0..*] @Material))
				(Definition SoundEnv (includes group=#LMCore@LMObject)
					(+contains sounds [0..*] @Sound))
				(Definition Transformation (includes group=#LMCore@LMObject)
					(+refers target [1..1] @Material))
				(Definition Root (includes group=#LMCore@Model)
					(+contains materials @MaterialEnv [1..1])
					(+contains sounds @SoundEnv [1..1])
					(+contains transformations @Transformation [0..*])))
			""";

		final var metaLoader = LmLoader.withEmptyRegistry();
		final var metaDoc = metaLoader.loadModel(metaSource);
		assertTrue(metaDoc.diagnostics()
						  .stream()
						  .noneMatch(d -> d.severity() == LmDiagnostic.Severity.ERROR));

		final var metaModel = (MetaModel) metaDoc.model();
		final var registryBuilder = new ModelRegistry.Builder();
		registryBuilder.register(LMCoreModelPackage.MODEL);
		registryBuilder.register(metaModel);
		final var registry = registryBuilder.build();

		final var m1Source = """
			(Root domain=test.model name=App metamodels=test.model.RewriteMeta
				(MaterialEnv
					(Material name=Lava))
				(SoundEnv
					(Sound name=Lava))
				(Transformation target=/materials/materials.0))
			""";

		final var loader = new LmLoader(registry);
		final var doc = loader.loadModel(m1Source);
		assertTrue(doc.diagnostics()
					  .stream()
					  .noneMatch(d -> d.severity() == LmDiagnostic.Severity.ERROR));

		final var linkRoots = RootReferenceResolver.collectLinkRoots(doc.linkTrees());
		assertTrue(linkRoots.size() == 1);
		final var linkRoot = linkRoots.getFirst();

		final var index = ReferencePathToNameIndex.build(linkRoot);
		final var formatted = new LmFormatter().format(linkRoot, index);

		assertTrue(formatted.contains("target=@Lava"));
	}

	private static Path findRepoRoot() throws Exception
	{
		Path cursor = Path.of("").toAbsolutePath().normalize();
		while (cursor != null)
		{
			if (Files.exists(cursor.resolve("settings.gradle")))
			{
				return cursor;
			}
			cursor = cursor.getParent();
		}
		throw new IllegalStateException("Cannot locate repo root (settings.gradle not found)");
	}
}
