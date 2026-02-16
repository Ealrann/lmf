package org.logoce.lmf.cli.command;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.replace.ReplaceRunner;
import org.logoce.lmf.cli.json.JsonErrorWriter;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public final class ReplaceCommand implements Command
{
	private final String modelSpec;
	private final String targetReference;
	private final SubtreeSource subtreeSource;
	private final boolean force;
	private final boolean json;

	public ReplaceCommand(final String modelSpec,
						  final String targetReference,
						  final SubtreeSource subtreeSource,
						  final boolean force,
						  final boolean json)
	{
		this.modelSpec = Objects.requireNonNull(modelSpec, "modelSpec");
		this.targetReference = Objects.requireNonNull(targetReference, "targetReference");
		this.subtreeSource = Objects.requireNonNull(subtreeSource, "subtreeSource");
		this.force = force;
		this.json = json;
	}

	@Override
	public String name()
	{
		return "replace";
	}

	public static ReplaceCommand parse(final List<String> args, final PrintWriter err)
	{
		if (args.isEmpty())
		{
			printUsage(err);
			return null;
		}

		boolean force = false;
		boolean json = false;
		boolean subtreeStdin = false;
		Path subtreeFile = null;

		String modelSpec = null;
		String targetReference = null;
		final var subtreeParts = new java.util.ArrayList<String>();

		for (int i = 0; i < args.size(); i++)
		{
			final var arg = args.get(i);
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
			if ("--subtree-stdin".equals(arg))
			{
				subtreeStdin = true;
				continue;
			}
			if ("--subtree-file".equals(arg))
			{
				if (i + 1 >= args.size())
				{
					err.println("Missing value for --subtree-file");
					printUsage(err);
					return null;
				}
				final var value = args.get(++i);
				if ("-".equals(value))
				{
					subtreeStdin = true;
					continue;
				}
				subtreeFile = Path.of(value);
				continue;
			}
			if (arg != null && arg.startsWith("--"))
			{
				err.println("Unknown option for replace: " + arg);
				printUsage(err);
				return null;
			}

			if (modelSpec == null)
			{
				modelSpec = arg;
				continue;
			}
			if (targetReference == null)
			{
				targetReference = arg;
				continue;
			}
			subtreeParts.add(arg);
		}

		if (modelSpec == null || targetReference == null)
		{
			printUsage(err);
			return null;
		}

		final SubtreeSource subtreeSource;
		if (subtreeFile != null && subtreeStdin)
		{
			err.println("Cannot use both --subtree-file and --subtree-stdin");
			printUsage(err);
			return null;
		}
		if (subtreeFile != null)
		{
			if (!subtreeParts.isEmpty())
			{
				err.println("Cannot use both <subtree> and --subtree-file");
				printUsage(err);
				return null;
			}
			subtreeSource = new SubtreeSource.File(subtreeFile);
		}
		else if (subtreeStdin)
		{
			if (!subtreeParts.isEmpty())
			{
				err.println("Cannot use both <subtree> and --subtree-stdin");
				printUsage(err);
				return null;
			}
			subtreeSource = new SubtreeSource.Stdin();
		}
		else
		{
			if (subtreeParts.isEmpty())
			{
				printUsage(err);
				return null;
			}
			if (subtreeParts.size() == 1 && "-".equals(subtreeParts.getFirst()))
			{
				subtreeSource = new SubtreeSource.Stdin();
			}
			else
			{
				subtreeSource = new SubtreeSource.Inline(String.join(" ", subtreeParts));
			}
		}

		return new ReplaceCommand(modelSpec, targetReference, subtreeSource, force, json);
	}

	@Override
	public int execute(final CliContext context)
	{
		if (subtreeSource instanceof SubtreeSource.Inline)
		{
			context.err().println("Warning: inline <subtree> is deprecated and shell-quoting fragile; prefer --subtree-stdin/--subtree-file (one-liner only).");
		}

		final var read = SubtreeSourceReader.read(context, subtreeSource);
		if (read instanceof SubtreeSourceReader.ReadResult.Failure failure)
		{
			if (json)
			{
				JsonErrorWriter.writeError(context, "replace", failure.exitCode(), failure.message());
			}
			else
			{
				context.err().println(failure.message());
			}
			return failure.exitCode();
		}

		final var replacementSubtree = ((SubtreeSourceReader.ReadResult.Success) read).subtree();
		final var runner = new ReplaceRunner();
		return runner.run(context,
						  modelSpec,
						  targetReference,
						  replacementSubtree,
						  new ReplaceRunner.Options(force, json));
	}

	private static void printUsage(final PrintWriter err)
	{
		err.println("Usage: lm [--project-root <path>] replace <model.lm> <ref> --subtree-file <path> [--force] [--json]");
		err.println("   or: lm [--project-root <path>] replace <model.lm> <ref> --subtree-stdin [--force] [--json]");
		err.println("   or: lm [--project-root <path>] replace <model.lm> <ref> - [--force] [--json]");
		err.println("   or: lm [--project-root <path>] replace <model.lm> <ref> <subtree> [--force] [--json]   (deprecated; one-liner only)");
	}
}
