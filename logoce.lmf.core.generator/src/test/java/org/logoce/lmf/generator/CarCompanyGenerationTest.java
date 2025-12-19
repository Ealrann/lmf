package org.logoce.lmf.generator;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class CarCompanyGenerationTest
{
	@Test
	public void generateCarCompanyModel() throws Exception
	{
		final var basePackageDir = new File("src/test/generated/test/model2/carcompany");

		assertTrue(basePackageDir.isDirectory(), "Base package directory should exist: " + basePackageDir);
		assertTrue(new File(basePackageDir, "Entity.java").isFile(), "Entity.java should be generated");
		assertTrue(new File(basePackageDir, "Car.java").isFile(), "Car.java should be generated");
		assertTrue(new File(basePackageDir, "CarParc.java").isFile(), "CarParc.java should be generated");

		final var brand = new File(basePackageDir, "Brand.java");
		assertTrue(brand.isFile(), "Brand.java should be generated");
		final var brandContent = Files.readString(brand.toPath());
		assertFalse(Pattern.compile("Renault,\\R\\R\\s*Peugeot").matcher(brandContent).find(),
					"Brand enum constants should not be separated by blank lines");
	}
}
