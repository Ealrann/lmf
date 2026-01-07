package org.logoce.lmf.cli.command;

import org.logoce.lmf.cli.CliContext;

public interface Command
{
	String name();

	int execute(CliContext context);
}
