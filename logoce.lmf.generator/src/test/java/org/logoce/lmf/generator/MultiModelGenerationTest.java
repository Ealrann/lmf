package org.logoce.lmf.generator;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MultiModelGenerationTest
{
	@Test
	public void generateGraphModelsSingleStep()
	{
		final var targetDir = new File("build/test-generated/graph-single");
		if (targetDir.exists())
		{
			deleteRecursively(targetDir);
		}

		final var graphCoreModelFile = new File("src/test/model/GraphCore.lm");
		final var graphExtensionsModelFile = new File("src/test/model/GraphExtensions.lm");
		final var graphAnalysisModelFile = new File("src/test/model/GraphAnalysis.lm");

		Main.generate(targetDir,
					  List.of(graphCoreModelFile, graphExtensionsModelFile, graphAnalysisModelFile),
					  List.of());

		final var basePackageDir = new File(targetDir, "test/multi");

		assertTrue(basePackageDir.isDirectory(), "Base package directory should exist: " + basePackageDir);
		assertTrue(new File(basePackageDir, "Node.java").isFile(), "Node.java should be generated");
		assertTrue(new File(basePackageDir, "ColoredNode.java").isFile(), "ColoredNode.java should be generated");
		assertTrue(new File(basePackageDir, "Graph.java").isFile(), "Graph.java should be generated");
		assertTrue(new File(basePackageDir, "GraphView.java").isFile(), "GraphView.java should be generated");
	}

	@Test
	public void generateGraphModelsTwoStepWithImports()
	{
		final var targetDir = new File("build/test-generated/graph-two-step");
		if (targetDir.exists())
		{
			deleteRecursively(targetDir);
		}

		final var graphCoreModelFile = new File("src/test/model/GraphCore.lm");
		final var graphExtensionsModelFile = new File("src/test/model/GraphExtensions.lm");
		final var graphAnalysisModelFile = new File("src/test/model/GraphAnalysis.lm");

		Main.generate(targetDir,
					  List.of(graphCoreModelFile, graphExtensionsModelFile),
					  List.of());

		final var basePackageDir = new File(targetDir, "test/multi");
		assertTrue(new File(basePackageDir, "Node.java").isFile(), "Node.java should be generated");
		assertTrue(new File(basePackageDir, "Graph.java").isFile(), "Graph.java should be generated");

		Main.generate(targetDir,
					  List.of(graphAnalysisModelFile),
					  List.of(graphCoreModelFile, graphExtensionsModelFile));

		assertTrue(new File(basePackageDir, "GraphView.java").isFile(), "GraphView.java should be generated");
	}

	@Test
	public void generateGraphAnalysisWithCliImports()
	{
		final var targetDir = new File("build/test-generated/graph-cli");
		if (targetDir.exists())
		{
			deleteRecursively(targetDir);
		}

		final var args = new String[] {
				"--targetDir", targetDir.getPath(),
				"--genModels", "src/test/model/GraphAnalysis.lm",
				"--imports", "src/test/model/GraphCore.lm,src/test/model/GraphExtensions.lm"
		};

		Main.main(args);

		final var basePackageDir = new File(targetDir, "test/multi");
		assertTrue(new File(basePackageDir, "GraphView.java").isFile(), "GraphView.java should be generated");
	}

	private static void deleteRecursively(final File file)
	{
		if (file.isDirectory())
		{
			final var children = file.listFiles();
			if (children != null)
			{
				for (final var child : children)
				{
					deleteRecursively(child);
				}
			}
		}
		if (!file.delete() && file.exists())
		{
			throw new IllegalStateException("Cannot delete " + file);
		}
	}
}

