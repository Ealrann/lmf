package org.logoce.lmf.cli;

import org.logoce.lmf.cli.args.ArgumentParser;
import org.logoce.lmf.cli.args.ParseResult;
import org.logoce.lmf.cli.command.BatchCommand;
import org.logoce.lmf.cli.command.CheckCommand;
import org.logoce.lmf.cli.command.Command;
import org.logoce.lmf.cli.command.FmtCommand;
import org.logoce.lmf.cli.command.InsertCommand;
import org.logoce.lmf.cli.command.MoveCommand;
import org.logoce.lmf.cli.command.RemoveCommand;
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
import java.util.List;
import java.util.Set;

public final class LmCli
{
	private static final Set<String> KNOWN_COMMANDS = Set.of("batch",
															"check",
															"fmt",
															"tree",
															"ref",
															"replace",
															"remove",
															"insert",
															"move",
															"rename",
															"set",
															"unset");

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

		final Command command = buildCommand(parsed.commandName(), parsed.commandArgs());
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
		return null;
	}

	private static String usage()
	{
		return """
			Usage:
			  lm [--project-root <path>] batch [--file <path> | --stdin] [--dry-run] [--continue-on-error] [--force] [--validate each|final|none] [--default-model <model.lm>]
			  lm [--project-root <path>] check <model.lm>
			  lm [--project-root <path>] check --all
			  lm [--project-root <path>] fmt <model.lm> [--root <ref>] [--ref-path-to-name]
			  lm [--project-root <path>] tree <model.lm> [--root <ref>] [--max-depth <n>]
			  lm [--project-root <path>] ref <model.lm> <ref> [--include-descendants]
			  lm [--project-root <path>] replace <model.lm> <ref> <subtree> [--force]
			  lm [--project-root <path>] remove <model.lm> <ref>
			  lm [--project-root <path>] insert <model.lm> <ref> <subtree>
			  lm [--project-root <path>] move <model.lm> <fromRef> <toRef>
			  lm [--project-root <path>] set <model.lm> <objectRef> <featureName> <value>
			  lm [--project-root <path>] unset <model.lm> <objectRef> <featureName>
			  lm [--project-root <path>] rename <model.lm> <ref> <newName>

			Options:
			  --project-root <path>  set project root (default: current directory)
			  --root <ref>           limit fmt/tree to the subtree referenced by <ref>
			  --ref-path-to-name     replace absolute refs like /x.0 by @name when unique
			  --include-descendants  include descendant matches for ref (path-like refs only)
			  --max-depth <n>        limit tree output to <n> levels (default: unlimited)
			  --depth <n>            alias for --max-depth
			  --file <path>          for batch: read operations from file
			  --stdin                for batch: read operations from stdin (default)
			  --default-model <m>    for batch: default <model.lm> for operations
			  --validate <mode>      for batch: each|final|none
			  --dry-run              for batch: do not write changes
			  --continue-on-error    for batch: keep executing after errors
			  --force                for replace/batch: write even if validation has errors
			  -h, --help             show this help
			""".stripTrailing();
	}
}
