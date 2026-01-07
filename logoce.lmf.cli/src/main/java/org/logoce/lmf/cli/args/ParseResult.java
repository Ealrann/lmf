package org.logoce.lmf.cli.args;

import java.nio.file.Path;
import java.util.List;

public sealed interface ParseResult permits ParseResult.Parsed, ParseResult.Error, ParseResult.Help
{
	record Parsed(Path projectRoot, String commandName, List<String> commandArgs) implements ParseResult
	{
	}

	record Error(String message) implements ParseResult
	{
	}

	record Help() implements ParseResult
	{
	}
}
