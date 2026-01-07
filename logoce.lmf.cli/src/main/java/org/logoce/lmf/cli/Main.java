package org.logoce.lmf.cli;

public final class Main
{
	private Main()
	{
	}

	public static void main(final String[] args)
	{
		final var cli = new LmCli(System.out, System.err);
		final int exitCode = cli.run(args);
		System.exit(exitCode);
	}
}
