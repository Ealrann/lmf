package org.logoce.lmf.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;
import org.logoce.lmf.gradle.diagnostics.GenerationFailureReporter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class GenerateLmfSources extends DefaultTask
{
	@InputFiles
	@SkipWhenEmpty
	@PathSensitive(PathSensitivity.RELATIVE)
	public abstract ConfigurableFileCollection getModelFiles();

	@InputFiles
	@PathSensitive(PathSensitivity.RELATIVE)
	public abstract ConfigurableFileCollection getImportModelFiles();

	@OutputDirectory
	public abstract DirectoryProperty getOutputDir();

	@TaskAction
	public void generate()
	{
		final var logger = getLogger();
		final var outputDir = getOutputDir().get().getAsFile();

		final List<File> modelFiles = new ArrayList<>(getModelFiles().getFiles());
		final List<File> importModelFiles = new ArrayList<>(getImportModelFiles().getFiles());

		if (outputDir.exists() == false && outputDir.mkdirs() == false)
		{
			throw new IllegalStateException("Cannot create output directory " + outputDir);
		}

		if (modelFiles.isEmpty())
		{
			logger.lifecycle("No LMF model files to generate.");
			return;
		}

		logger.lifecycle("Generating LMF sources from {}", modelFiles);
		try
		{
			runGenerator(outputDir, modelFiles, importModelFiles, logger);
		}
		catch (Exception e)
		{
			GenerationFailureReporter.report(logger, modelFiles, e);
		}
	}

	private static void runGenerator(final File outputDir,
									 final List<File> modelsToGenerate,
									 final List<File> importModels,
									 final Logger logger)
	{
		if (importModels.isEmpty() == false)
		{
			logger.lifecycle("LMF import models = {}", importModels);
		}

		org.logoce.lmf.generator.Main.generate(outputDir, modelsToGenerate, importModels);
	}
}
