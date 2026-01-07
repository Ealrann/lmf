package org.logoce.lmf.cli.command;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.insert.InsertRunner;

import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;

public final class InsertCommand implements Command
{
	private final String modelSpec;
	private final String targetReference;
	private final String subtree;

	public InsertCommand(final String modelSpec,
						 final String targetReference,
						 final String subtree)
	{
		this.modelSpec = Objects.requireNonNull(modelSpec, "modelSpec");
		this.targetReference = Objects.requireNonNull(targetReference, "targetReference");
		this.subtree = Objects.requireNonNull(subtree, "subtree");
	}

	@Override
	public String name()
	{
		return "insert";
	}

	public static InsertCommand parse(final List<String> args, final PrintWriter err)
	{
		if (args.isEmpty())
		{
			err.println("Usage: lm [--project-root <path>] insert <model.lm> <ref> <subtree>");
			return null;
		}

		final var cleaned = new java.util.ArrayList<String>(args.size());
		for (final var arg : args)
		{
			if (arg != null && arg.startsWith("--"))
			{
				err.println("Unknown option for insert: " + arg);
				err.println("Usage: lm [--project-root <path>] insert <model.lm> <ref> <subtree>");
				return null;
			}
			cleaned.add(arg);
		}

		if (cleaned.size() < 3)
		{
			err.println("Usage: lm [--project-root <path>] insert <model.lm> <ref> <subtree>");
			return null;
		}

		final var modelSpec = cleaned.getFirst();
		final var targetRef = cleaned.get(1);
		final var subtree = String.join(" ", cleaned.subList(2, cleaned.size()));
		return new InsertCommand(modelSpec, targetRef, subtree);
	}

	@Override
	public int execute(final CliContext context)
	{
		final var runner = new InsertRunner();
		return runner.run(context, modelSpec, targetReference, subtree);
	}
}

