package org.logoce.lmf.cli.command;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.assign.RemoveValueRunner;

import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;

public final class RemoveValueCommand implements Command
{
	private final String modelSpec;
	private final String objectReference;
	private final String featureName;
	private final String value;
	private final boolean json;

	public RemoveValueCommand(final String modelSpec,
							  final String objectReference,
							  final String featureName,
							  final String value,
							  final boolean json)
	{
		this.modelSpec = Objects.requireNonNull(modelSpec, "modelSpec");
		this.objectReference = Objects.requireNonNull(objectReference, "objectReference");
		this.featureName = Objects.requireNonNull(featureName, "featureName");
		this.value = Objects.requireNonNull(value, "value");
		this.json = json;
	}

	@Override
	public String name()
	{
		return "remove-value";
	}

	public static RemoveValueCommand parse(final List<String> args, final PrintWriter err)
	{
		if (args.isEmpty())
		{
			err.println("Usage: lm [--project-root <path>] remove-value <model.lm> <objectRef> <featureName> <value> [--json]");
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
				err.println("Unknown option for remove-value: " + arg);
				err.println("Usage: lm [--project-root <path>] remove-value <model.lm> <objectRef> <featureName> <value> [--json]");
				return null;
			}
			cleaned.add(arg);
		}

		if (cleaned.size() < 4)
		{
			err.println("Usage: lm [--project-root <path>] remove-value <model.lm> <objectRef> <featureName> <value> [--json]");
			return null;
		}

		final var modelSpec = cleaned.getFirst();
		final var objectRef = cleaned.get(1);
		final var featureName = cleaned.get(2);
		final var value = String.join(" ", cleaned.subList(3, cleaned.size()));
		return new RemoveValueCommand(modelSpec, objectRef, featureName, value, json);
	}

	@Override
	public int execute(final CliContext context)
	{
		return new RemoveValueRunner().run(context, modelSpec, objectReference, featureName, value, new RemoveValueRunner.Options(json));
	}
}

