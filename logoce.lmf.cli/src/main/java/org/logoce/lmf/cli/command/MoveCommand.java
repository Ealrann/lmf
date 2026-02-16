package org.logoce.lmf.cli.command;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.move.MoveRunner;

import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;

public final class MoveCommand implements Command
{
	private final String modelSpec;
	private final String fromReference;
	private final String toReference;
	private final boolean json;

	public MoveCommand(final String modelSpec,
					   final String fromReference,
					   final String toReference,
					   final boolean json)
	{
		this.modelSpec = Objects.requireNonNull(modelSpec, "modelSpec");
		this.fromReference = Objects.requireNonNull(fromReference, "fromReference");
		this.toReference = Objects.requireNonNull(toReference, "toReference");
		this.json = json;
	}

	@Override
	public String name()
	{
		return "move";
	}

	public static MoveCommand parse(final List<String> args, final PrintWriter err)
	{
		if (args.isEmpty())
		{
			err.println("Usage: lm [--project-root <path>] move <model.lm> <fromRef> <toRef> [--json]");
			return null;
		}

		boolean json = false;
		final var cleaned = new java.util.ArrayList<String>(args.size());
		for (final var arg : args)
		{
			if ("--json".equals(arg))
			{
				json = true;
				continue;
			}
			if (arg != null && arg.startsWith("--"))
			{
				err.println("Unknown option for move: " + arg);
				err.println("Usage: lm [--project-root <path>] move <model.lm> <fromRef> <toRef> [--json]");
				return null;
			}
			cleaned.add(arg);
		}

		if (cleaned.size() != 3)
		{
			err.println("Usage: lm [--project-root <path>] move <model.lm> <fromRef> <toRef> [--json]");
			return null;
		}

		return new MoveCommand(cleaned.getFirst(), cleaned.get(1), cleaned.get(2), json);
	}

	@Override
	public int execute(final CliContext context)
	{
		return new MoveRunner().run(context, modelSpec, fromReference, toReference, new MoveRunner.Options(json));
	}
}
