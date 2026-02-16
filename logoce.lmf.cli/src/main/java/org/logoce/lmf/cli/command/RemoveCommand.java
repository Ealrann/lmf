package org.logoce.lmf.cli.command;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.remove.RemoveRunner;

import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;

public final class RemoveCommand implements Command
{
	private final String modelSpec;
	private final String targetReference;
	private final boolean json;

	public RemoveCommand(final String modelSpec, final String targetReference, final boolean json)
	{
		this.modelSpec = Objects.requireNonNull(modelSpec, "modelSpec");
		this.targetReference = Objects.requireNonNull(targetReference, "targetReference");
		this.json = json;
	}

	@Override
	public String name()
	{
		return "remove";
	}

	public static RemoveCommand parse(final List<String> args, final PrintWriter err)
	{
		String modelSpec = null;
		String targetReference = null;
		boolean json = false;

		for (final var arg : args)
		{
			if ("--json".equals(arg))
			{
				json = true;
				continue;
			}
			if (arg != null && arg.startsWith("--"))
			{
				err.println("Unknown option for remove: " + arg);
				err.println("Usage: lm [--project-root <path>] remove <model.lm> <ref> [--json]");
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

			err.println("Usage: lm [--project-root <path>] remove <model.lm> <ref> [--json]");
			return null;
		}

		if (modelSpec == null || targetReference == null)
		{
			err.println("Usage: lm [--project-root <path>] remove <model.lm> <ref> [--json]");
			return null;
		}

		return new RemoveCommand(modelSpec, targetReference, json);
	}

	@Override
	public int execute(final CliContext context)
	{
		final var runner = new RemoveRunner();
		return runner.run(context, modelSpec, targetReference, new RemoveRunner.Options(json));
	}
}
