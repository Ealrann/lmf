package org.logoce.lmf.cli.command;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.tree.TreeRunner;

import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;

public final class TreeCommand implements Command
{
	private static final int DEFAULT_DEPTH = Integer.MAX_VALUE;

	private final String modelSpec;
	private final int maxDepth;
	private final String rootReference;
	private final boolean alwaysIndex;
	private final boolean syntaxOnly;
	private final boolean json;

	public TreeCommand(final String modelSpec,
					   final int maxDepth,
					   final String rootReference,
					   final boolean alwaysIndex,
					   final boolean syntaxOnly,
					   final boolean json)
	{
		this.modelSpec = Objects.requireNonNull(modelSpec, "modelSpec");
		this.maxDepth = maxDepth;
		this.rootReference = rootReference;
		this.alwaysIndex = alwaysIndex;
		this.syntaxOnly = syntaxOnly;
		this.json = json;
	}

	@Override
	public String name()
	{
		return "tree";
	}

	public static TreeCommand parse(final List<String> args, final PrintWriter err)
	{
		String modelSpec = null;
		Integer depth = null;
		String rootReference = null;
		boolean alwaysIndex = false;
		boolean syntaxOnly = false;
		boolean json = false;

		for (int i = 0; i < args.size(); i++)
		{
			final String arg = args.get(i);
			if ("--depth".equals(arg) || "--max-depth".equals(arg))
			{
				if (i + 1 >= args.size())
				{
					err.println("Usage: lm [--project-root <path>] tree <model.lm> [--root <ref>] [--max-depth <n>] [--always-index] [--syntax-only]");
					return null;
				}
				final var rawDepth = args.get(i + 1);
				try
				{
					depth = Integer.parseInt(rawDepth);
				}
				catch (NumberFormatException e)
				{
					err.println("Invalid depth: " + rawDepth);
					err.println("Usage: lm [--project-root <path>] tree <model.lm> [--root <ref>] [--max-depth <n>] [--always-index] [--syntax-only]");
					return null;
				}
				if (depth < 0)
				{
					err.println("Depth must be >= 0");
					return null;
				}
				i++;
				continue;
			}
			if ("--root".equals(arg))
			{
				if (i + 1 >= args.size())
				{
					err.println("Usage: lm [--project-root <path>] tree <model.lm> [--root <ref>] [--max-depth <n>] [--always-index] [--syntax-only]");
					return null;
				}
				rootReference = args.get(i + 1);
				i++;
				continue;
			}
			if ("--always-index".equals(arg))
			{
				alwaysIndex = true;
				continue;
			}
			if ("--syntax-only".equals(arg))
			{
				syntaxOnly = true;
				continue;
			}
			if ("--json".equals(arg))
			{
				json = true;
				continue;
			}
			if (arg != null && arg.startsWith("--"))
			{
				err.println("Unknown option for tree: " + arg);
				err.println("Usage: lm [--project-root <path>] tree <model.lm> [--root <ref>] [--max-depth <n>] [--always-index] [--syntax-only] [--json]");
				return null;
			}
			if (modelSpec == null)
			{
				modelSpec = arg;
				continue;
			}
			err.println("Usage: lm [--project-root <path>] tree <model.lm> [--root <ref>] [--max-depth <n>] [--always-index] [--syntax-only] [--json]");
			return null;
		}

		if (modelSpec == null)
		{
			err.println("Usage: lm [--project-root <path>] tree <model.lm> [--root <ref>] [--max-depth <n>] [--always-index] [--syntax-only] [--json]");
			return null;
		}

		final int maxDepth = depth == null ? DEFAULT_DEPTH : depth;
		return new TreeCommand(modelSpec, maxDepth, rootReference, alwaysIndex, syntaxOnly, json);
	}

	@Override
	public int execute(final CliContext context)
	{
		final var runner = new TreeRunner();
		return runner.run(context, modelSpec, new TreeRunner.Options(maxDepth, rootReference, alwaysIndex, syntaxOnly, json));
	}
}
