package org.logoce.lmf.generator;

import org.logoce.lmf.generator.model.ModelGenerator;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.resource.ResourceUtil;
import org.logoce.lmf.model.util.ModelRegistry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public final class Main
{
	public static void main(String[] args)
	{
		if (args.length < 2)
		{
			System.err.println("Usage: Main <modelPath> <targetPath>");
			System.exit(1);
		}

		final var modelFile = new File(args[0]);
		final var targetDir = new File(args[1]);
		generate(modelFile, targetDir);
	}

	public static void generate(final File modelFile, final File targetDir)
	{
		final var start = System.currentTimeMillis();

		System.out.println("modelPath = " + modelFile.getAbsolutePath());
		System.out.println("targetDir = " + targetDir.getAbsolutePath());

		if (targetDir.exists() == false && targetDir.mkdirs() == false)
		{
			throw new IllegalStateException("Cannot create output directory " + targetDir);
		}

		try (final var modelInputStream = new FileInputStream(modelFile))
		{
			final var roots = ResourceUtil.loadObject(modelInputStream, ModelRegistry.empty());

			for (final var root : roots)
			{
				if (root instanceof MetaModel model)
				{
					final var generator = new ModelGenerator(model);
					System.out.printf("Generating = %1$s...%n", model.name());
					generator.generateJava(targetDir);
					final var end = System.currentTimeMillis();
					System.out.printf("Generation done in %1$d ms%n", end - start);
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new RuntimeException("Failed to generate Java sources from " + modelFile, e);
		}
	}
}
