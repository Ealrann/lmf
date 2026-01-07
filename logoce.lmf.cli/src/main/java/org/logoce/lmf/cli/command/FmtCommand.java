package org.logoce.lmf.cli.command;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.format.FmtRunner;

import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;

public final class FmtCommand implements Command
{
	private final String modelSpec;
	private final String rootReference;
	private final boolean refPathToName;

	public FmtCommand(final String modelSpec, final String rootReference, final boolean refPathToName)
	{
		this.modelSpec = Objects.requireNonNull(modelSpec, "modelSpec");
		this.rootReference = rootReference;
		this.refPathToName = refPathToName;
	}

	@Override
	public String name()
	{
		return "fmt";
	}

	public static FmtCommand parse(final List<String> args, final PrintWriter err)
	{
		String modelSpec = null;
		String rootRef = null;
		boolean refPathToName = false;

		for (int i = 0; i < args.size(); i++)
		{
			final String arg = args.get(i);
			if ("--root".equals(arg))
			{
				if (i + 1 >= args.size())
				{
					err.println("Usage: lm [--project-root <path>] fmt <model.lm> [--root <ref>] [--ref-path-to-name]");
					return null;
				}
				rootRef = args.get(i + 1);
				i++;
				continue;
			}
			if ("--ref-path-to-name".equals(arg))
			{
				refPathToName = true;
				continue;
			}
			if (arg != null && arg.startsWith("--"))
			{
				err.println("Unknown option for fmt: " + arg);
				err.println("Usage: lm [--project-root <path>] fmt <model.lm> [--root <ref>] [--ref-path-to-name]");
				return null;
			}
			if (modelSpec == null)
			{
				modelSpec = arg;
				continue;
			}
			err.println("Usage: lm [--project-root <path>] fmt <model.lm> [--root <ref>] [--ref-path-to-name]");
			return null;
		}

		if (modelSpec == null)
		{
			err.println("Usage: lm [--project-root <path>] fmt <model.lm> [--root <ref>] [--ref-path-to-name]");
			return null;
		}

		return new FmtCommand(modelSpec, rootRef, refPathToName);
	}

	@Override
	public int execute(final CliContext context)
	{
		final var runner = new FmtRunner();
		return runner.run(context, modelSpec, new FmtRunner.Options(rootReference, refPathToName));
	}
}
