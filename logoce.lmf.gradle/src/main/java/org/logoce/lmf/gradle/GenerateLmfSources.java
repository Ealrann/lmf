package org.logoce.lmf.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

public abstract class GenerateLmfSources extends DefaultTask
{
	@InputFiles
	@SkipWhenEmpty
	@PathSensitive(PathSensitivity.RELATIVE)
	public abstract ConfigurableFileCollection getModelFiles();

	@OutputDirectory
	public abstract DirectoryProperty getOutputDir();

	@TaskAction
	public void generate()
	{
		final var logger = getLogger();
		final var outputDir = getOutputDir().get().getAsFile();

		if (outputDir.exists() == false && outputDir.mkdirs() == false)
		{
			throw new IllegalStateException("Cannot create output directory " + outputDir);
		}

		for (final var modelFile : getModelFiles().getFiles())
		{
			logger.lifecycle("Generating LMF sources from {}", modelFile);
			runGenerator(modelFile, outputDir);
		}
	}

	private static void runGenerator(final File modelFile, final File outputDir)
	{
		org.logoce.lmf.generator.Main.generate(modelFile, outputDir);
	}
}

