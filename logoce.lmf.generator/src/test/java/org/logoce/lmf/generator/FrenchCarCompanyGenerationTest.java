package org.logoce.lmf.generator;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FrenchCarCompanyGenerationTest
{
	@Test
	public void generateFrenchCarCompanyModel()
	{
		final var frenchCarCompanyModelFile = new File("src/test/model/FrenchCarCompany.lm");
		final var carCompanyModelFile = new File("src/test/model/CarCompany.lm");
		final var targetDir = new File("build/test-generated/french");

		if (targetDir.exists())
		{
			deleteRecursively(targetDir);
		}

		Main.generate(targetDir,
					  List.of(frenchCarCompanyModelFile),
					  List.of(carCompanyModelFile));

		final var basePackageDir = new File(targetDir, "test/model2");

		assertTrue(basePackageDir.isDirectory(), "Base package directory should exist: " + basePackageDir);
		assertTrue(new File(basePackageDir, "French.java").isFile(), "French.java should be generated");
	}

	private static void deleteRecursively(final File file)
	{
		if (file.isDirectory())
		{
			final var children = file.listFiles();
			if (children != null)
			{
				for (final var child : children)
				{
					deleteRecursively(child);
				}
			}
		}
		if (!file.delete() && file.exists())
		{
			throw new IllegalStateException("Cannot delete " + file);
		}
	}
}
