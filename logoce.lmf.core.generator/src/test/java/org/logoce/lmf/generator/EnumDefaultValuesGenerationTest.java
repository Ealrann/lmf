package org.logoce.lmf.generator;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnumDefaultValuesGenerationTest
{
	@Test
	public void enumDefaultsToFirstLiteral() throws Exception
	{
		final var basePackageDir = new File("src/test/generated/test/multi/graphcore/builder");
		assertTrue(basePackageDir.isDirectory(), "Base package directory should exist: " + basePackageDir);

		final var builderFile = new File(basePackageDir, "ColoredNodeBuilder.java");
		assertTrue(builderFile.isFile(), "ColoredNodeBuilder.java should be generated");

		final var content = Files.readString(builderFile.toPath());
		assertTrue(Pattern.compile("\\bColor\\s+color\\s*=\\s*Color\\.Red\\s*;").matcher(content).find(),
				   "ColoredNodeBuilder.color should default to the first enum literal");
	}
}
