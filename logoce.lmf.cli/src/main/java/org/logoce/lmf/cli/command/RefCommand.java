package org.logoce.lmf.cli.command;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.ref.RefRunner;

import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;

public final class RefCommand implements Command
{
	private final String modelSpec;
	private final String targetReference;
	private final boolean includeDescendants;
	private final boolean json;

	public RefCommand(final String modelSpec,
					  final String targetReference,
					  final boolean includeDescendants,
					  final boolean json)
	{
		this.modelSpec = Objects.requireNonNull(modelSpec, "modelSpec");
		this.targetReference = Objects.requireNonNull(targetReference, "targetReference");
		this.includeDescendants = includeDescendants;
		this.json = json;
	}

	@Override
	public String name()
	{
		return "ref";
	}

	public static RefCommand parse(final List<String> args, final PrintWriter err)
	{
		String modelSpec = null;
		String targetRef = null;
		boolean includeDescendants = false;
		boolean json = false;

		for (int i = 0; i < args.size(); i++)
		{
			final var arg = args.get(i);
			if ("--include-descendants".equals(arg) || "--descendants".equals(arg))
			{
				includeDescendants = true;
				continue;
			}
			if ("--json".equals(arg))
			{
				json = true;
				continue;
			}
			if (arg != null && arg.startsWith("--"))
			{
				err.println("Unknown option for ref: " + arg);
				err.println("Usage: lm [--project-root <path>] ref <model.lm> <ref> [--include-descendants] [--json]");
				return null;
			}
			if (modelSpec == null)
			{
				modelSpec = arg;
				continue;
			}
			if (targetRef == null)
			{
				targetRef = arg;
				continue;
			}

			err.println("Usage: lm [--project-root <path>] ref <model.lm> <ref> [--include-descendants] [--json]");
			return null;
		}

		if (modelSpec == null || targetRef == null)
		{
			err.println("Usage: lm [--project-root <path>] ref <model.lm> <ref> [--include-descendants] [--json]");
			return null;
		}

		return new RefCommand(modelSpec, targetRef, includeDescendants, json);
	}

	@Override
	public int execute(final CliContext context)
	{
		final var runner = new RefRunner();
		return runner.run(context, modelSpec, targetReference, new RefRunner.Options(includeDescendants, json));
	}
}
