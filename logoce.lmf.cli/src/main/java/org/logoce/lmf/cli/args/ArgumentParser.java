package org.logoce.lmf.cli.args;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ArgumentParser
{
	public ParseResult parse(final String[] args, final Path defaultProjectRoot)
	{
		final var tokens = args == null ? new String[0] : args;
		int index = 0;
		Path projectRoot = defaultProjectRoot;

		while (index < tokens.length)
		{
			final String token = tokens[index];
			if ("-h".equals(token) || "--help".equals(token))
			{
				return new ParseResult.Help();
			}
			if ("--project-root".equals(token))
			{
				if (index + 1 >= tokens.length)
				{
					return new ParseResult.Error("Missing value for --project-root");
				}
				final var rootArg = Path.of(tokens[index + 1]);
				projectRoot = rootArg.isAbsolute()
							  ? rootArg.toAbsolutePath().normalize()
							  : defaultProjectRoot.resolve(rootArg).toAbsolutePath().normalize();
				index += 2;
				continue;
			}
			if (token != null && token.startsWith("--"))
			{
				return new ParseResult.Error("Unknown option: " + token);
			}
			break;
		}

		if (index >= tokens.length)
		{
			return new ParseResult.Error("Missing command");
		}

		final String commandName = tokens[index++];
		final var commandArgs = new ArrayList<String>();
		while (index < tokens.length)
		{
			final String arg = tokens[index++];
			if (arg != null)
			{
				commandArgs.add(arg);
			}
		}

		return new ParseResult.Parsed(projectRoot,
									  commandName,
									  List.copyOf(commandArgs));
	}
}
