package org.logoce.lmf.generator;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CarCompanyGenerationTest
{
	@Test
	public void generateCarCompanyModel() {
		final var modelFile = new File("src/test/model/CarCompany.lm");
		final var targetDir = new File("src/test/generated");

		if (targetDir.exists()) {
			deleteRecursively(targetDir);
		}

		Main.generate(modelFile, targetDir);

		final var basePackageDir = new File(targetDir, "test/model");

		assertTrue(basePackageDir.isDirectory(), "Base package directory should exist: " + basePackageDir);
		assertTrue(new File(basePackageDir, "Entity.java").isFile(), "Entity.java should be generated");
		assertTrue(new File(basePackageDir, "Car.java").isFile(), "Car.java should be generated");
		assertTrue(new File(basePackageDir, "CarParc.java").isFile(), "CarParc.java should be generated");
	}

	private static void deleteRecursively(final File file) {
		if (file.isDirectory()) {
			final var children = file.listFiles();
			if (children != null) {
				for (final var child : children) {
					deleteRecursively(child);
				}
			}
		}
		if (!file.delete() && file.exists()) {
			throw new IllegalStateException("Cannot delete " + file);
		}
	}
}
