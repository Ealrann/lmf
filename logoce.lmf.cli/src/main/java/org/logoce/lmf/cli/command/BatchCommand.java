package org.logoce.lmf.cli.command;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.ExitCodes;
import org.logoce.lmf.cli.batch.BatchOptions;
import org.logoce.lmf.cli.batch.BatchRunner;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public final class BatchCommand implements Command
{
	private final BatchOptions options;

	private BatchCommand(final BatchOptions options)
	{
		this.options = Objects.requireNonNull(options, "options");
	}

	public static BatchCommand parse(final List<String> args, final PrintWriter err)
	{
		Path file = null;
		boolean readFromStdin = false;
		boolean dryRun = false;
		boolean continueOnError = false;
		boolean force = false;
		boolean json = false;
		BatchOptions.ValidateMode validateMode = BatchOptions.ValidateMode.FINAL;
		String defaultModel = null;

		for (int i = 0; i < args.size(); i++)
		{
			final var arg = args.get(i);
			if ("-h".equals(arg) || "--help".equals(arg))
			{
				err.println(usage());
				return null;
			}
			if ("--file".equals(arg))
			{
				if (i + 1 >= args.size())
				{
					err.println("Missing value for --file");
					err.println(usage());
					return null;
				}
				final var value = args.get(++i);
				if ("-".equals(value))
				{
					readFromStdin = true;
					continue;
				}
				file = Path.of(value);
				continue;
			}
			if ("--stdin".equals(arg))
			{
				readFromStdin = true;
				continue;
			}
			if ("--dry-run".equals(arg))
			{
				dryRun = true;
				continue;
			}
			if ("--continue-on-error".equals(arg))
			{
				continueOnError = true;
				continue;
			}
			if ("--force".equals(arg))
			{
				force = true;
				continue;
			}
			if ("--json".equals(arg))
			{
				json = true;
				continue;
			}
			if ("--validate".equals(arg))
			{
				if (i + 1 >= args.size())
				{
					err.println("Missing value for --validate");
					err.println(usage());
					return null;
				}
				final var value = args.get(++i);
				validateMode = switch (value)
				{
					case "each" -> BatchOptions.ValidateMode.EACH;
					case "final" -> BatchOptions.ValidateMode.FINAL;
					case "none" -> BatchOptions.ValidateMode.NONE;
					default ->
					{
						err.println("Invalid --validate value: " + value + " (expected: each|final|none)");
						err.println(usage());
						yield null;
					}
				};
				if (validateMode == null)
				{
					return null;
				}
				continue;
			}
			if ("--default-model".equals(arg))
			{
				if (i + 1 >= args.size())
				{
					err.println("Missing value for --default-model");
					err.println(usage());
					return null;
				}
				defaultModel = args.get(++i);
				continue;
			}
			if (arg != null && arg.startsWith("--"))
			{
				err.println("Unknown option for batch: " + arg);
				err.println(usage());
				return null;
			}

			if (file != null)
			{
				err.println("Unexpected argument: " + arg);
				err.println(usage());
				return null;
			}
			if ("-".equals(arg))
			{
				readFromStdin = true;
				continue;
			}
			file = Path.of(arg);
		}

		if (file != null && readFromStdin)
		{
			err.println("Cannot use both a file and --stdin");
			err.println(usage());
			return null;
		}

		if (file == null && !readFromStdin)
		{
			readFromStdin = true;
		}

		return new BatchCommand(new BatchOptions(file,
												readFromStdin,
												dryRun,
												continueOnError,
												force,
												validateMode,
												defaultModel,
												json));
	}

	@Override
	public String name()
	{
		return "batch";
	}

	@Override
	public int execute(final CliContext context)
	{
		final var runner = new BatchRunner();
		final var file = options.file();

		if (file != null)
		{
			final var resolved = file.isAbsolute() ? file.toAbsolutePath().normalize() : context.projectRoot().resolve(file).toAbsolutePath().normalize();
			if (!Files.isRegularFile(resolved))
			{
				context.err().println("Batch file not found: " + resolved);
				return ExitCodes.USAGE;
			}

			try (final var reader = Files.newBufferedReader(resolved, StandardCharsets.UTF_8))
			{
				final var effective = new BatchOptions(resolved,
													   false,
													   options.dryRun(),
													   options.continueOnError(),
													   options.force(),
													   options.validateMode(),
													   options.defaultModel(),
													   options.json());
				return runner.run(context, effective, reader);
			}
			catch (Exception e)
			{
				final var message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
				context.err().println("Failed to read batch file: " + message);
				return ExitCodes.INVALID;
			}
		}

		try (final var reader = new InputStreamReader(System.in, StandardCharsets.UTF_8))
		{
			return runner.run(context, options, reader);
		}
		catch (Exception e)
		{
			final var message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
			context.err().println("Failed to read batch input: " + message);
			return ExitCodes.INVALID;
		}
	}

	private static String usage()
	{
		return """
			Usage: lm [--project-root <path>] batch [--file <path> | --stdin] [--dry-run] [--continue-on-error] [--force] [--validate each|final|none] [--default-model <model.lm>] [--json]

			Batch format: JSON Lines (one JSON object per line), for example:
			  {"cmd":"remove","args":["Application.vsand.lm","/materials/materials.1"]}
			  {"cmd":"rename","args":["Application.vsand.lm","@Lava","Lava Boiling"]}
			""".stripTrailing();
	}
}
