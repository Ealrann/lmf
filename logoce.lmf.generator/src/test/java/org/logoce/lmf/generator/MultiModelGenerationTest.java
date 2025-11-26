package org.logoce.lmf.generator;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MultiModelGenerationTest
{
	@Test
	public void generateGraphModelsSingleStep()
	{
		final var basePackageDir = new File("src/test/generated/test/multi");

		assertTrue(basePackageDir.isDirectory(), "Base package directory should exist: " + basePackageDir);
		assertTrue(new File(basePackageDir, "Node.java").isFile(), "Node.java should be generated");
		assertTrue(new File(basePackageDir, "ColoredNode.java").isFile(), "ColoredNode.java should be generated");
		assertTrue(new File(basePackageDir, "Graph.java").isFile(), "Graph.java should be generated");
		assertTrue(new File(basePackageDir, "GraphView.java").isFile(), "GraphView.java should be generated");
	}

	@Test
	public void generateGraphModelsTwoStepWithImports()
	{
		final var basePackageDir = new File("src/test/generated/test/multi");
		assertTrue(new File(basePackageDir, "Node.java").isFile(), "Node.java should be generated");
		assertTrue(new File(basePackageDir, "Graph.java").isFile(), "Graph.java should be generated");
		assertTrue(new File(basePackageDir, "GraphView.java").isFile(), "GraphView.java should be generated");
	}

	@Test
	public void generateGraphAnalysisWithCliImports()
	{
		final var basePackageDir = new File("src/test/generated/test/multi");
		assertTrue(new File(basePackageDir, "GraphView.java").isFile(), "GraphView.java should be generated");
	}
}
