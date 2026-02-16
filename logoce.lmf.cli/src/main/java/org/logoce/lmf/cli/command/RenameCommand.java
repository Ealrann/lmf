package org.logoce.lmf.cli.command;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.rename.RenameRunner;

import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;

public final class RenameCommand implements Command
{
	private final String modelSpec;
	private final String objectReference;
	private final String newName;
	private final boolean json;

	public RenameCommand(final String modelSpec,
					final String objectReference,
					final String newName,
					final boolean json)
	{
		this.modelSpec = Objects.requireNonNull(modelSpec, "modelSpec");
		this.objectReference = Objects.requireNonNull(objectReference, "objectReference");
		this.newName = Objects.requireNonNull(newName, "newName");
		this.json = json;
	}

	@Override
	public String name()
	{
		return "rename";
	}

	public static RenameCommand parse(final List<String> args, final PrintWriter err)
	{
		if (args.isEmpty())
		{
			err.println("Usage: lm [--project-root <path>] rename <model.lm> <ref> <newName> [--json]");
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
				err.println("Unknown option for rename: " + arg);
				err.println("Usage: lm [--project-root <path>] rename <model.lm> <ref> <newName> [--json]");
				return null;
			}
			cleaned.add(arg);
		}

		if (cleaned.size() < 3)
		{
			err.println("Usage: lm [--project-root <path>] rename <model.lm> <ref> <newName> [--json]");
			return null;
		}

		final var modelSpec = cleaned.getFirst();
		final var ref = cleaned.get(1);
		final var newName = String.join(" ", cleaned.subList(2, cleaned.size()));
		return new RenameCommand(modelSpec, ref, newName, json);
	}

	@Override
	public int execute(final CliContext context)
	{
		return new RenameRunner().run(context, modelSpec, objectReference, newName, new RenameRunner.Options(json));
	}
}
