package org.logoce.lmf.generator;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class EnumAttributesGenerationTest
{
	@Test
	public void generateEnumsWithAttributes() throws Exception
	{
		final var basePackageDir = new File("src/test/generated/test/model2/enumattrs/enumattributes");

		assertTrue(basePackageDir.isDirectory(), "Base package directory should exist: " + basePackageDir);

		final var nameAndId = new File(basePackageDir, "NameAndId.java");
		assertTrue(nameAndId.isFile(), "NameAndId.java should be generated");
		final var nameAndIdContent = Files.readString(nameAndId.toPath());
		assertTrue(nameAndIdContent.contains("private final int id"), "NameAndId should declare an id field");
		assertTrue(nameAndIdContent.contains("public int id()"), "NameAndId should expose an id accessor");
		assertTrue(nameAndIdContent.contains("A(1)"), "NameAndId.A should carry an id");
		assertTrue(nameAndIdContent.contains("B(2)"), "NameAndId.B should carry an id");
		assertFalse(Pattern.compile("A\\(1\\),\\R\\R\\s*B\\(2\\)").matcher(nameAndIdContent).find(),
					"NameAndId enum constants should not be separated by blank lines");

		final var labelled = new File(basePackageDir, "Labelled.java");
		assertTrue(labelled.isFile(), "Labelled.java should be generated");
		final var labelledContent = Files.readString(labelled.toPath());
		assertTrue(labelledContent.contains("private final String label"), "Labelled should declare a label field");
		assertTrue(labelledContent.contains("public String label()"), "Labelled should expose a label accessor");
		assertTrue(labelledContent.contains("A(\"Hello world\")"), "Labelled.A should carry a string label");
		assertTrue(labelledContent.contains("B(\"foo:bar\")"), "Labelled.B should preserve ':' in label");
		assertTrue(labelledContent.contains("C(\"hi,there\")"), "Labelled.C should preserve ',' in label");
		assertFalse(Pattern.compile("A\\(\"Hello world\"\\),\\R\\R\\s*B\\(\"foo:bar\"\\)").matcher(labelledContent).find(),
					"Labelled enum constants should not be separated by blank lines");

		final var codeAndLabel = new File(basePackageDir, "CodeAndLabel.java");
		assertTrue(codeAndLabel.isFile(), "CodeAndLabel.java should be generated");
		final var codeAndLabelContent = Files.readString(codeAndLabel.toPath());
		assertTrue(codeAndLabelContent.contains("private final int id"), "CodeAndLabel should declare an id field");
		assertTrue(codeAndLabelContent.contains("private final String label"), "CodeAndLabel should declare a label field");
		assertTrue(codeAndLabelContent.contains("A(1, \"Hello world\")"), "CodeAndLabel.A should carry both values");

		final var modelDefinition = new File(basePackageDir, "EnumAttributesModelDefinition.java");
		assertTrue(modelDefinition.isFile(), "EnumAttributesModelDefinition.java should be generated");
		final var modelDefinitionContent = Files.readString(modelDefinition.toPath());
		assertTrue(modelDefinitionContent.contains(".addAttribute(() ->"), "ModelDefinition should include EnumAttributes");
		assertTrue(modelDefinitionContent.contains("new EnumAttributeBuilder()"), "ModelDefinition should build EnumAttribute objects");
	}
}
