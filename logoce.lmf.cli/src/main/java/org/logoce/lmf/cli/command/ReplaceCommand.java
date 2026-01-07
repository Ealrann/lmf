package org.logoce.lmf.cli.command;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.replace.ReplaceRunner;

import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;

public final class ReplaceCommand implements Command
{
	private final String modelSpec;
	private final String targetReference;
	private final String replacementSubtree;
	private final boolean force;

	public ReplaceCommand(final String modelSpec,
						  final String targetReference,
						  final String replacementSubtree,
						  final boolean force)
	{
		this.modelSpec = Objects.requireNonNull(modelSpec, "modelSpec");
		this.targetReference = Objects.requireNonNull(targetReference, "targetReference");
		this.replacementSubtree = Objects.requireNonNull(replacementSubtree, "replacementSubtree");
		this.force = force;
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
			err.println("Usage: lm [--project-root <path>] replace <model.lm> <ref> <subtree> [--force]");
			return null;
		}

		boolean force = false;
		final var cleaned = new java.util.ArrayList<String>(args.size());

		for (final var arg : args)
		{
			if ("--force".equals(arg))
			{
				force = true;
				continue;
			}
			if (arg != null && arg.startsWith("--"))
			{
				err.println("Unknown option for replace: " + arg);
				err.println("Usage: lm [--project-root <path>] replace <model.lm> <ref> <subtree> [--force]");
				return null;
			}
			cleaned.add(arg);
		}

		if (cleaned.size() < 3)
		{
			err.println("Usage: lm [--project-root <path>] replace <model.lm> <ref> <subtree> [--force]");
			return null;
		}

		final var modelSpec = cleaned.getFirst();
		final var targetRef = cleaned.get(1);
		final var subtree = String.join(" ", cleaned.subList(2, cleaned.size()));
		return new ReplaceCommand(modelSpec, targetRef, subtree, force);
	}

	@Override
	public int execute(final CliContext context)
	{
		final var runner = new ReplaceRunner();
		return runner.run(context, modelSpec, targetReference, replacementSubtree, new ReplaceRunner.Options(force));
	}
}
