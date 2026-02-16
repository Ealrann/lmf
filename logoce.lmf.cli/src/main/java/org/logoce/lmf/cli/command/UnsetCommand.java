package org.logoce.lmf.cli.command;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.assign.UnsetRunner;

import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;

public final class UnsetCommand implements Command
{
	private final String modelSpec;
	private final String objectReference;
	private final String featureName;
	private final boolean json;

	public UnsetCommand(final String modelSpec,
						final String objectReference,
						final String featureName,
						final boolean json)
	{
		this.modelSpec = Objects.requireNonNull(modelSpec, "modelSpec");
		this.objectReference = Objects.requireNonNull(objectReference, "objectReference");
		this.featureName = Objects.requireNonNull(featureName, "featureName");
		this.json = json;
	}

	@Override
	public String name()
	{
		return "unset";
	}

	public static UnsetCommand parse(final List<String> args, final PrintWriter err)
	{
		if (args.isEmpty())
		{
			err.println("Usage: lm [--project-root <path>] unset <model.lm> <objectRef> <featureName> [--json]");
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
				err.println("Unknown option for unset: " + arg);
				err.println("Usage: lm [--project-root <path>] unset <model.lm> <objectRef> <featureName> [--json]");
				return null;
			}
			cleaned.add(arg);
		}

		if (cleaned.size() != 3)
		{
			err.println("Usage: lm [--project-root <path>] unset <model.lm> <objectRef> <featureName> [--json]");
			return null;
		}

		final var modelSpec = cleaned.getFirst();
		final var objectRef = cleaned.get(1);
		final var featureName = cleaned.get(2);
		return new UnsetCommand(modelSpec, objectRef, featureName, json);
	}

	@Override
	public int execute(final CliContext context)
	{
		return new UnsetRunner().run(context, modelSpec, objectReference, featureName, new UnsetRunner.Options(json));
	}
}
