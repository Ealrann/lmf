package org.logoce.lmf.generator;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CarCompanyGenerationTest
{
	@Test
	public void generateCarCompanyModel()
	{
		final var basePackageDir = new File("src/test/generated/test/model/carcompany");

		assertTrue(basePackageDir.isDirectory(), "Base package directory should exist: " + basePackageDir);
		assertTrue(new File(basePackageDir, "Entity.java").isFile(), "Entity.java should be generated");
		assertTrue(new File(basePackageDir, "Car.java").isFile(), "Car.java should be generated");
		assertTrue(new File(basePackageDir, "CarParc.java").isFile(), "CarParc.java should be generated");
	}
}
