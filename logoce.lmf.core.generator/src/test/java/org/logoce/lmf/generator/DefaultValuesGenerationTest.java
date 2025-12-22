package org.logoce.lmf.generator;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultValuesGenerationTest
{
	@Test
	public void generateDefaultValuesModel() throws Exception
	{
		final var basePackageDir = new File("src/test/generated/test/model2/defaultvalues");
		assertTrue(basePackageDir.isDirectory(), "Base package directory should exist: " + basePackageDir);

		final var fooBuilder = new File(basePackageDir, "builder/FooBuilder.java");
		assertTrue(fooBuilder.isFile(), "FooBuilder.java should be generated");

		final var fooContent = Files.readString(fooBuilder.toPath());
		assertTrue(Pattern.compile("\\bMode\\s+mode\\s*=\\s*Mode\\.A\\s*;").matcher(fooContent).find(),
				   "FooBuilder.mode should be initialized from defaultValue=A");
	}
}
