package org.logoce.lmf.cli;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.cli.tree.TreeLister;
import org.logoce.lmf.cli.format.RootReferenceResolver;
import org.logoce.lmf.core.api.model.ModelRegistry;
import org.logoce.lmf.core.loader.api.loader.LmLoader;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TreeListerTest
{
	@Test
	void treeListsIndexedAndSingularPaths() throws Exception
	{
		final var source = """
			(MetaModel domain=test.model name=TreeTest
				(Group A)
				(Group B)
				(Enum E A,B))
			""";

		final var loader = new LmLoader(ModelRegistry.empty());
		final var doc = loader.loadModel(source);

		assertTrue(doc.diagnostics()
					  .stream()
					  .noneMatch(d -> d.severity() == LmDiagnostic.Severity.ERROR));

		final var linkRoots = RootReferenceResolver.collectLinkRoots(doc.linkTrees());
		assertEquals(1, linkRoots.size());

		final var lines = new TreeLister().list(linkRoots.getFirst(), 1);
		assertEquals(3, lines.size());
		assertEquals("/groups.0\tGroup\tA", lines.get(0).format());
		assertEquals("/groups.1\tGroup\tB", lines.get(1).format());
		assertEquals("/enums\tEnum\tE", lines.get(2).format());
	}
}

