package org.logoce.lmf.cli;

import org.logoce.lmf.cli.args.ArgumentParser;
import org.logoce.lmf.cli.args.ParseResult;
import org.logoce.lmf.cli.command.BatchCommand;
import org.logoce.lmf.cli.command.CheckCommand;
import org.logoce.lmf.cli.command.Command;
import org.logoce.lmf.cli.command.FeaturesCommand;
import org.logoce.lmf.cli.command.FmtCommand;
import org.logoce.lmf.cli.command.AddCommand;
import org.logoce.lmf.cli.command.ClearCommand;
import org.logoce.lmf.cli.command.InsertCommand;
import org.logoce.lmf.cli.command.MoveCommand;
import org.logoce.lmf.cli.command.RemoveCommand;
import org.logoce.lmf.cli.command.RemoveValueCommand;
import org.logoce.lmf.cli.command.RenameCommand;
import org.logoce.lmf.cli.command.ReplaceCommand;
import org.logoce.lmf.cli.command.RefCommand;
import org.logoce.lmf.cli.command.SetCommand;
import org.logoce.lmf.cli.command.TreeCommand;
import org.logoce.lmf.cli.command.UnsetCommand;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class LmCli
{
	private static final Set<String> KNOWN_COMMANDS = Set.of("batch",
															"check",
															"fmt",
															"models",
															"features",
															"tree",
															"ref",
															"replace",
															"remove",
															"insert",
															"move",
															"rename",
															"set",
															"unset",
															"add",
															"remove-value",
															"clear");

	private final PrintWriter out;
	private final PrintWriter err;
	private final Path defaultProjectRoot;
	private final ArgumentParser argumentParser = new ArgumentParser();

	public LmCli(final PrintStream outStream, final PrintStream errStream)
	{
		this(outStream, errStream, Path.of("").toAbsolutePath().normalize());
	}

	LmCli(final PrintStream outStream, final PrintStream errStream, final Path defaultProjectRoot)
	{
		this.out = new PrintWriter(outStream, true);
		this.err = new PrintWriter(errStream, true);
		this.defaultProjectRoot = defaultProjectRoot;
	}

	public int run(final String[] args)
	{
		final var result = argumentParser.parse(args, defaultProjectRoot);
		final int exitCode;

		if (result instanceof ParseResult.Parsed parsed)
		{
			exitCode = runParsed(parsed);
		}
		else if (result instanceof ParseResult.Help)
		{
			out.println(usage());
			exitCode = ExitCodes.OK;
		}
		else if (result instanceof ParseResult.Error error)
		{
			err.println("Error: " + error.message());
			err.println(usage());
			exitCode = ExitCodes.USAGE;
		}
		else
		{
			err.println("Error: unexpected parse result");
			err.println(usage());
			exitCode = ExitCodes.USAGE;
		}

		return exitCode;
	}

	private int runParsed(final ParseResult.Parsed parsed)
	{
		final var projectRoot = parsed.projectRoot();
		if (Files.isDirectory(projectRoot) == false)
		{
			err.println("Project root is not a directory: " + projectRoot);
			return ExitCodes.USAGE;
		}

		final var normalizedArgs = normalizeModelOption(parsed.commandName(), parsed.commandArgs());
		if (normalizedArgs == null)
		{
			err.println(usage());
			return ExitCodes.USAGE;
		}

		final Command command = buildCommand(parsed.commandName(), normalizedArgs);
		if (command == null)
		{
			if (KNOWN_COMMANDS.contains(parsed.commandName()))
			{
				return wantsHelp(parsed.commandArgs()) ? ExitCodes.OK : ExitCodes.USAGE;
			}

			err.println("Unknown or invalid command: " + parsed.commandName());
			err.println(usage());
			return ExitCodes.USAGE;
		}

		final var context = new CliContext(projectRoot, out, err);
		return command.execute(context);
	}

	private static boolean wantsHelp(final List<String> args)
	{
		if (args == null || args.isEmpty())
		{
			return false;
		}

		for (final var arg : args)
		{
			if ("-h".equals(arg) || "--help".equals(arg))
			{
				return true;
			}
		}
		return false;
	}

	private List<String> normalizeModelOption(final String commandName, final List<String> args)
	{
		if (args == null || args.isEmpty())
		{
			return args == null ? List.of() : args;
		}

		final var supportsModelOption = Set.of("check",
											  "fmt",
											  "features",
											  "tree",
											  "ref",
											  "replace",
											  "remove",
											  "insert",
											  "move",
											  "rename",
											  "set",
											  "unset",
											  "add",
											  "remove-value",
											  "clear").contains(commandName);
		if (!supportsModelOption)
		{
			return args;
		}

		int modelIndex = -1;
		for (int i = 0; i < args.size(); i++)
		{
			if ("--model".equals(args.get(i)))
			{
				if (modelIndex >= 0)
				{
					err.println("Error: --model cannot be specified more than once");
					return null;
				}
				modelIndex = i;
			}
		}

		if (modelIndex < 0)
		{
			return args;
		}

		if (modelIndex + 1 >= args.size())
		{
			err.println("Error: Missing value for --model");
			return null;
		}

		final var qualifiedName = args.get(modelIndex + 1);
		if (qualifiedName == null || qualifiedName.isBlank())
		{
			err.println("Error: Empty value for --model");
			return null;
		}

		final var normalized = new ArrayList<String>(args.size() - 1);
		for (int i = 0; i < args.size(); i++)
		{
			if (i == modelIndex)
			{
				normalized.add("qn:" + qualifiedName.strip());
				i++; // skip value
				continue;
			}
			normalized.add(args.get(i));
		}

		return List.copyOf(normalized);
	}

	private Command buildCommand(final String name, final List<String> args)
	{
		if ("batch".equals(name))
		{
			final var command = BatchCommand.parse(args, err);
			if (command == null)
			{
				return null;
			}
			return command;
		}
		if ("models".equals(name))
		{
			final var command = org.logoce.lmf.cli.command.ModelsCommand.parse(args, err);
			if (command == null)
			{
				return null;
			}
			return command;
		}
		if ("check".equals(name))
		{
			final var command = CheckCommand.parse(args, err);
			if (command == null)
			{
				return null;
			}
			return command;
		}
		if ("fmt".equals(name))
		{
			final var command = FmtCommand.parse(args, err);
			if (command == null)
			{
				return null;
			}
			return command;
		}
		if ("features".equals(name))
		{
			final var command = FeaturesCommand.parse(args, err);
			if (command == null)
			{
				return null;
			}
			return command;
		}
		if ("tree".equals(name))
		{
			final var command = TreeCommand.parse(args, err);
			if (command == null)
			{
				return null;
			}
			return command;
		}
		if ("ref".equals(name))
		{
			final var command = RefCommand.parse(args, err);
			if (command == null)
			{
				return null;
			}
			return command;
		}
		if ("replace".equals(name))
		{
			final var command = ReplaceCommand.parse(args, err);
			if (command == null)
			{
				return null;
			}
			return command;
		}
		if ("remove".equals(name))
		{
			final var command = RemoveCommand.parse(args, err);
			if (command == null)
			{
				return null;
			}
			return command;
		}
		if ("insert".equals(name))
		{
			final var command = InsertCommand.parse(args, err);
			if (command == null)
			{
				return null;
			}
			return command;
		}
		if ("move".equals(name))
		{
			final var command = MoveCommand.parse(args, err);
			if (command == null)
			{
				return null;
			}
			return command;
		}
		if ("rename".equals(name))
		{
			final var command = RenameCommand.parse(args, err);
			if (command == null)
			{
				return null;
			}
			return command;
		}
		if ("set".equals(name))
		{
			final var command = SetCommand.parse(args, err);
			if (command == null)
			{
				return null;
			}
			return command;
		}
		if ("unset".equals(name))
		{
			final var command = UnsetCommand.parse(args, err);
			if (command == null)
			{
				return null;
			}
			return command;
		}
		if ("add".equals(name))
		{
			final var command = AddCommand.parse(args, err);
			if (command == null)
			{
				return null;
			}
			return command;
		}
		if ("remove-value".equals(name))
		{
			final var command = RemoveValueCommand.parse(args, err);
			if (command == null)
			{
				return null;
			}
			return command;
		}
		if ("clear".equals(name))
		{
			final var command = ClearCommand.parse(args, err);
			if (command == null)
			{
				return null;
			}
			return command;
		}
		return null;
	}

	private static String usage()
	{
		return """
			Usage:
			  lm [--project-root <path>] batch [--file <path> | --stdin] [--dry-run] [--continue-on-error] [--force] [--validate each|final|none] [--default-model <model.lm>] [--json]
			  lm [--project-root <path>] models [--duplicates] [--json]
			  lm [--project-root <path>] check <model.lm|qn:<domain.name>> [--model <domain.name>] [--constraints] [--json]
			  lm [--project-root <path>] check --all [--constraints] [--verbose] [--exclude <path|glob>]... [--json]
			  lm [--project-root <path>] features <model.lm|qn:<domain.name>> [--model <domain.name>] <objectRef> [--json]
				  lm [--project-root <path>] fmt <model.lm|qn:<domain.name>> [--model <domain.name>] [--root <ref>] [--ref-path-to-name] [--syntax-only] [--in-place] [--json]
				  lm [--project-root <path>] tree <model.lm|qn:<domain.name>> [--model <domain.name>] [--root <ref>] [--max-depth <n>] [--always-index] [--syntax-only] [--json]
				  lm [--project-root <path>] ref <model.lm|qn:<domain.name>> [--model <domain.name>] <ref> [--include-descendants] [--json]
				  lm [--project-root <path>] replace <model.lm|qn:<domain.name>> [--model <domain.name>] <ref> --subtree-file <path> [--force] [--json]
				  lm [--project-root <path>] replace <model.lm|qn:<domain.name>> [--model <domain.name>] <ref> --subtree-stdin [--force] [--json]
				  lm [--project-root <path>] replace <model.lm|qn:<domain.name>> [--model <domain.name>] <ref> - [--force] [--json]
				  lm [--project-root <path>] replace <model.lm|qn:<domain.name>> [--model <domain.name>] <ref> <subtree> [--force] [--json]   (deprecated; one-liner only)
				  lm [--project-root <path>] remove <model.lm|qn:<domain.name>> [--model <domain.name>] <ref> [--json]
				  lm [--project-root <path>] insert <model.lm|qn:<domain.name>> [--model <domain.name>] <ref> --subtree-file <path> [--json]
				  lm [--project-root <path>] insert <model.lm|qn:<domain.name>> [--model <domain.name>] <ref> --subtree-stdin [--json]
				  lm [--project-root <path>] insert <model.lm|qn:<domain.name>> [--model <domain.name>] <ref> - [--json]
				  lm [--project-root <path>] insert <model.lm|qn:<domain.name>> [--model <domain.name>] <ref> <subtree> [--json]   (deprecated; one-liner only)
				  lm [--project-root <path>] move <model.lm|qn:<domain.name>> [--model <domain.name>] <fromRef> <toRef> [--json]
				  lm [--project-root <path>] set <model.lm|qn:<domain.name>> [--model <domain.name>] <objectRef> <featureName> <value> [--json]
				  lm [--project-root <path>] unset <model.lm|qn:<domain.name>> [--model <domain.name>] <objectRef> <featureName> [--json]
				  lm [--project-root <path>] add <model.lm|qn:<domain.name>> [--model <domain.name>] <objectRef> <featureName> <value> [--json]
			  lm [--project-root <path>] remove-value <model.lm|qn:<domain.name>> [--model <domain.name>] <objectRef> <featureName> <value> [--json]
			  lm [--project-root <path>] clear <model.lm|qn:<domain.name>> [--model <domain.name>] <objectRef> <featureName> [--json]
			  lm [--project-root <path>] rename <model.lm|qn:<domain.name>> [--model <domain.name>] <ref> <newName> [--json]

			Options:
			  --project-root <path>  set project root (default: current directory)
			  --model <domain.name>  target a model by its qualified name (shorthand for <model>=qn:<domain.name>)
			  --root <ref>           limit fmt/tree to the subtree referenced by <ref>
			  --ref-path-to-name     replace absolute refs like /x.0 by @name when unique
			  --include-descendants  include descendant matches for ref (path-like refs only)
			  --max-depth <n>        limit tree output to <n> levels (default: unlimited)
			  --depth <n>            alias for --max-depth
			  --always-index         for tree: always show .0 for list paths
			  --syntax-only          for fmt/tree: parse-only (no linking; disables --root/--ref-path-to-name)
			  --in-place             for fmt: rewrite file in-place (full file only; incompatible with --root)
			  --file <path>          for batch: read operations from file
			  --stdin                for batch: read operations from stdin (default)
			  --default-model <m>    for batch: default <model.lm> for operations
			  --validate <mode>      for batch: each|final|none
			  --dry-run              for batch: do not write changes
			  --constraints          for check: emit mandatory-feature warnings (does not affect exit code)
				  --continue-on-error    for batch: keep executing after errors
				  --force                for replace/batch: write even if validation has errors
				  --verbose              for check --all: print OK per file
				  --exclude <path|glob>  for check --all: skip files/dirs (repeatable)
				  --subtree-file <path>  for replace/insert: read subtree from file (recommended for non-trivial subtrees)
				  --subtree-stdin        for replace/insert: read subtree from stdin (recommended; alias: pass '-' as <subtree>)
				  --json                 machine-readable JSON to stdout (human messages/diagnostics go to stderr)
				  --duplicates           for models: show duplicates only
				  -h, --help             show this help
				""".stripTrailing();
		}
	}
