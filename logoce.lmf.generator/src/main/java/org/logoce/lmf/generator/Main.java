package org.logoce.lmf.generator;

import org.logoce.lmf.generator.model.ModelGenerator;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.resource.ResourceUtil;
import org.logoce.lmf.model.util.ModelRegistry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Main
{
	public static void main(String[] args)
	{
		if (args.length == 0)
		{
			System.err.println("Usage: Main <modelPath> <targetPath>");
			System.err.println("   or: Main --targetDir <targetPath> --genModels <model1> [<model2> ...] " +
							   "[--imports <importModel1> ...]");
			System.exit(1);
		}

		if (containsFlag(args, "--targetDir") || containsFlag(args, "--genModels") ||
			containsFlag(args, "--imports"))
		{
			runWithFlags(args);
		}
		else if (args.length == 2)
		{
			final var modelFile = new File(args[0]);
			final var targetDir = new File(args[1]);
			generate(modelFile, targetDir);
		}
		else
		{
			System.err.println("Legacy mode expects exactly 2 arguments: <modelPath> <targetPath>");
			System.err.println("Flag mode expects: --targetDir <targetPath> --genModels <model1> [<model2> ...] " +
							   "[--imports <importModel1> ...]");
			System.exit(1);
		}
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

	public static void generate(final File targetDir,
								final List<File> modelsToGenerate,
								final List<File> availableModels)
	{
		if (modelsToGenerate.isEmpty())
		{
			throw new IllegalArgumentException("At least one model file must be provided to generate.");
		}

		final var start = System.currentTimeMillis();

		System.out.println("targetDir = " + targetDir.getAbsolutePath());
		System.out.println("modelsToGenerate = " + modelsToGenerate);
		if (availableModels.isEmpty() == false)
		{
			System.out.println("availableModels = " + availableModels);
		}

		if (targetDir.exists() == false && targetDir.mkdirs() == false)
		{
			throw new IllegalStateException("Cannot create output directory " + targetDir);
		}

		final var allFiles = new ArrayList<File>();
		final Set<File> seen = new HashSet<>();
		for (final var file : modelsToGenerate)
		{
			if (seen.add(file))
			{
				allFiles.add(file);
			}
		}
		for (final var file : availableModels)
		{
			if (seen.add(file))
			{
				allFiles.add(file);
			}
		}

		final List<InputStream> inputStreams = new ArrayList<>();
		try
		{
			for (final var file : allFiles)
			{
				if (file.isFile() == false)
				{
					throw new IllegalArgumentException("Model file does not exist: " + file.getAbsolutePath());
				}
				inputStreams.add(new FileInputStream(file));
			}

			final var models = ResourceUtil.loadModels(inputStreams, ModelRegistry.empty());
			if (models.size() != allFiles.size())
			{
				throw new IllegalStateException("Unexpected number of models loaded; expected " + allFiles.size() +
												" but got " + models.size());
			}

			final Map<File, MetaModel> metaModelsByFile = new HashMap<>();
			for (int i = 0; i < allFiles.size(); i++)
			{
				final var model = models.get(i);
				if (model instanceof MetaModel metaModel)
				{
					metaModelsByFile.put(allFiles.get(i), metaModel);
				}
				else
				{
					throw new IllegalArgumentException("File does not define a MetaModel: " +
													   allFiles.get(i).getAbsolutePath());
				}
			}

			for (final var file : modelsToGenerate)
			{
				final var model = metaModelsByFile.get(file);
				if (model == null)
				{
					throw new IllegalStateException("No MetaModel loaded for requested file: " +
													file.getAbsolutePath());
				}

				final var generator = new ModelGenerator(model);
				System.out.printf("Generating = %1$s...%n", model.name());
				generator.generateJava(targetDir);
			}

			final var end = System.currentTimeMillis();
			System.out.printf("Generation done in %1$d ms%n", end - start);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new RuntimeException("Failed to generate Java sources from models: " + allFiles, e);
		}
		finally
		{
			for (final var inputStream : inputStreams)
			{
				try
				{
					inputStream.close();
				}
				catch (IOException ignored)
				{
				}
			}
		}
	}

	private static boolean containsFlag(final String[] args, final String flag)
	{
		for (final var arg : args)
		{
			if (flag.equals(arg)) return true;
		}
		return false;
	}

	private static void runWithFlags(final String[] args)
	{
		File targetDir = null;
		final var modelsToGenerate = new ArrayList<File>();
		final var availableModels = new ArrayList<File>();

		for (int i = 0; i < args.length; i++)
		{
			final var arg = args[i];
			if ("--targetDir".equals(arg))
			{
				if (i + 1 >= args.length)
				{
					throw new IllegalArgumentException("--targetDir requires a path argument");
				}
				targetDir = new File(args[++i]);
			}
			else if ("--genModels".equals(arg))
			{
				i = collectFileArgs(args, i + 1, modelsToGenerate);
			}
			else if ("--imports".equals(arg))
			{
				i = collectFileArgs(args, i + 1, availableModels);
			}
			else
			{
				throw new IllegalArgumentException("Unknown argument: " + arg);
			}
		}

		if (targetDir == null)
		{
			throw new IllegalArgumentException("--targetDir must be specified");
		}

		generate(targetDir, modelsToGenerate, availableModels);
	}

	private static int collectFileArgs(final String[] args, int index, final List<File> out)
	{
		for (int i = index; i < args.length; i++)
		{
			final var value = args[i];
			if (value.startsWith("--"))
			{
				return i - 1;
			}

			for (final var part : value.split(","))
			{
				final var trimmed = part.trim();
				if (trimmed.isEmpty() == false)
				{
					out.add(new File(trimmed));
				}
			}
		}
		return args.length - 1;
	}
}
