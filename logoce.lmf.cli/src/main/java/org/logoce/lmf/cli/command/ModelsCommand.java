package org.logoce.lmf.cli.command;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.workspace.ModelsRunner;

import java.io.PrintWriter;
import java.util.List;

public final class ModelsCommand implements Command
{
	private final boolean duplicates;
	private final boolean json;

	private ModelsCommand(final boolean duplicates, final boolean json)
	{
		this.duplicates = duplicates;
		this.json = json;
	}

	public static ModelsCommand parse(final List<String> args, final PrintWriter err)
	{
		boolean duplicates = false;
		boolean json = false;

		for (int i = 0; i < args.size(); i++)
		{
			final var arg = args.get(i);
			if ("--duplicates".equals(arg))
			{
				duplicates = true;
				continue;
			}
			if ("--json".equals(arg))
			{
				json = true;
				continue;
			}
			if (arg != null && arg.startsWith("--"))
			{
				err.println("Unknown option for models: " + arg);
				printUsage(err);
				return null;
			}

			printUsage(err);
			return null;
		}

		return new ModelsCommand(duplicates, json);
	}

	@Override
	public String name()
	{
		return "models";
	}

	@Override
	public int execute(final CliContext context)
	{
		final var runner = new ModelsRunner();
		return runner.run(context, new ModelsRunner.Options(duplicates, json));
	}

	private static void printUsage(final PrintWriter err)
	{
		err.println("Usage: lm [--project-root <path>] models [--duplicates] [--json]");
	}
}

