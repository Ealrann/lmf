package org.logoce.lmf.cli.command;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.features.FeaturesRunner;

import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;

public final class FeaturesCommand implements Command
{
	private final String modelSpec;
	private final String objectReference;
	private final boolean json;

	public FeaturesCommand(final String modelSpec,
						   final String objectReference,
						   final boolean json)
	{
		this.modelSpec = Objects.requireNonNull(modelSpec, "modelSpec");
		this.objectReference = Objects.requireNonNull(objectReference, "objectReference");
		this.json = json;
	}

	@Override
	public String name()
	{
		return "features";
	}

	public static FeaturesCommand parse(final List<String> args, final PrintWriter err)
	{
		String modelSpec = null;
		String objectReference = null;
		boolean json = false;

		for (int i = 0; i < args.size(); i++)
		{
			final var arg = args.get(i);
			if ("--json".equals(arg))
			{
				json = true;
				continue;
			}
			if (arg != null && arg.startsWith("--"))
			{
				err.println("Unknown option for features: " + arg);
				err.println("Usage: lm [--project-root <path>] features <model.lm> <objectRef> [--json]");
				return null;
			}
			if (modelSpec == null)
			{
				modelSpec = arg;
				continue;
			}
			if (objectReference == null)
			{
				objectReference = arg;
				continue;
			}
			err.println("Usage: lm [--project-root <path>] features <model.lm> <objectRef> [--json]");
			return null;
		}

		if (modelSpec == null || objectReference == null)
		{
			err.println("Usage: lm [--project-root <path>] features <model.lm> <objectRef> [--json]");
			return null;
		}

		return new FeaturesCommand(modelSpec, objectReference, json);
	}

	@Override
	public int execute(final CliContext context)
	{
		return new FeaturesRunner().run(context, modelSpec, objectReference, new FeaturesRunner.Options(json));
	}
}

