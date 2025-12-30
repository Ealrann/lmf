package org.logoce.lmf.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MultiModelGenerationTest
{
	@Test
	public void generateGraphModelsSingleStep()
	{
		final var basePackageDir = new File("src/test/generated/test/multi");

		assertTrue(basePackageDir.isDirectory(), "Base package directory should exist: " + basePackageDir);
		assertTrue(new File(basePackageDir, "graphcore/Node.java").isFile(), "Node.java should be generated");
		assertTrue(new File(basePackageDir, "graphcore/ColoredNode.java").isFile(), "ColoredNode.java should be generated");
		assertTrue(new File(basePackageDir, "graphextensions/Graph.java").isFile(), "Graph.java should be generated");
		assertTrue(new File(basePackageDir, "graphanalysis/GraphView.java").isFile(), "GraphView.java should be generated");
	}

	@Test
	public void generateGraphModelsTwoStepWithImports()
	{
		final var basePackageDir = new File("src/test/generated/test/multi");
		assertTrue(new File(basePackageDir, "graphcore/Node.java").isFile(), "Node.java should be generated");
		assertTrue(new File(basePackageDir, "graphextensions/Graph.java").isFile(), "Graph.java should be generated");
		assertTrue(new File(basePackageDir, "graphanalysis/GraphView.java").isFile(), "GraphView.java should be generated");
	}

	@Test
	public void generateGraphAnalysisWithCliImports(@TempDir final Path tempDir)
	{
		final var targetDir = tempDir.resolve("graph-cli").toFile();
		final var graphCore = new File("src/test/model/GraphCore.lm");
		final var graphExtensions = new File("src/test/model/GraphExtensions.lm");
		final var graphAnalysis = new File("src/test/model/GraphAnalysis.lm");

		Main.generate(targetDir,
					  List.of(graphAnalysis),
					  List.of(graphCore, graphExtensions));

		final var basePackageDir = new File(targetDir, "test/multi/graphanalysis");
		assertTrue(new File(basePackageDir, "GraphView.java").isFile(), "GraphView.java should be generated");
	}
}
