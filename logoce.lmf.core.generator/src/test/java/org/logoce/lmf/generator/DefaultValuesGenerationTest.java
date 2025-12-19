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
		assertTrue(Pattern.compile("\\bboolean\\s+enabled\\s*=\\s*true\\s*;").matcher(fooContent).find(),
				   "FooBuilder.enabled should be initialized from defaultValue=true");
		assertTrue(Pattern.compile("\\bMode\\s+mode\\s*=\\s*Mode\\.A\\s*;").matcher(fooContent).find(),
				   "FooBuilder.mode should be initialized from defaultValue=A");
		assertTrue(Pattern.compile("\\b(?:java\\.util\\.function\\.)?Supplier<Bar>\\s+bar\\s*=\\s*\\(\\)\\s*->\\s*null\\s*;")
						  .matcher(fooContent)
						  .find(),
				   "FooBuilder.bar supplier should be initialized to avoid NPE when unset");
		assertTrue(Pattern.compile("\\.apply\\(\"hello\"\\)").matcher(fooContent).find(),
				   "FooBuilder.text should be initialized using JavaWrapper serializer from defaultValue=\"hello\"");

		final var barBuilder = new File(basePackageDir, "builder/BarBuilder.java");
		assertTrue(barBuilder.isFile(), "BarBuilder.java should be generated");
		final var barContent = Files.readString(barBuilder.toPath());
		assertTrue(Pattern.compile("\\bint\\s+id\\s*=\\s*3\\s*;").matcher(barContent).find(),
				   "BarBuilder.id should be initialized from defaultValue=3");
	}
}
