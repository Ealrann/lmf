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

	public RemoveCommand(final String modelSpec, final String targetReference)
	{
		this.modelSpec = Objects.requireNonNull(modelSpec, "modelSpec");
		this.targetReference = Objects.requireNonNull(targetReference, "targetReference");
	}

	@Override
	public String name()
	{
		return "remove";
	}

	public static RemoveCommand parse(final List<String> args, final PrintWriter err)
	{
		if (args.size() != 2)
		{
			err.println("Usage: lm [--project-root <path>] remove <model.lm> <ref>");
			return null;
		}

		final var modelSpec = args.getFirst();
		final var targetReference = args.get(1);
		return new RemoveCommand(modelSpec, targetReference);
	}

	@Override
	public int execute(final CliContext context)
	{
		final var runner = new RemoveRunner();
		return runner.run(context, modelSpec, targetReference);
	}
}
