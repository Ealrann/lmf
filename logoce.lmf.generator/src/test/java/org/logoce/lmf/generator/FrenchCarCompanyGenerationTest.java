package org.logoce.lmf.generator;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FrenchCarCompanyGenerationTest
{
	@Test
	public void generateFrenchCarCompanyModel()
	{
		final var basePackageDir = new File("src/test/generated/test/model2");

		assertTrue(basePackageDir.isDirectory(), "Base package directory should exist: " + basePackageDir);
		assertTrue(new File(basePackageDir, "French.java").isFile(), "French.java should be generated");
	}
}
