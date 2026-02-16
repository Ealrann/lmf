package org.logoce.lmf.cli.workspace;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.ExitCodes;
import org.logoce.lmf.cli.json.JsonErrorWriter;
import org.logoce.lmf.cli.util.PathDisplay;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public final class ModelSpecResolver
{
	private ModelSpecResolver()
	{
	}

	public record Result(Path path, int exitCode)
	{
		public boolean ok()
		{
			return path != null;
		}
	}

	public static Result resolve(final CliContext context,
							 final String modelSpec,
							 final String command,
							 final boolean json)
	{
		Objects.requireNonNull(context, "context");
		Objects.requireNonNull(modelSpec, "modelSpec");
		Objects.requireNonNull(command, "command");

		final var locator = new ModelLocator(context.projectRoot());
		final var resolution = locator.resolve(modelSpec);

		if (resolution instanceof ModelResolution.Found found)
		{
			return new Result(found.path(), ExitCodes.OK);
		}
		if (resolution instanceof ModelResolution.Ambiguous ambiguous)
		{
			if (json)
			{
				JsonErrorWriter.writeError(context,
									 command,
									 ExitCodes.USAGE,
									 "Ambiguous model reference: " + modelSpec,
									 ambiguous.matches().stream().map(p -> PathDisplay.display(context.projectRoot(), p)).toList());
			}
			else
			{
				final var err = context.err();
				err.println("Ambiguous model reference: " + modelSpec);
				for (final var path : ambiguous.matches())
				{
					err.println(" - " + PathDisplay.display(context.projectRoot(), path));
				}
			}
			return new Result(null, ExitCodes.USAGE);
		}
		if (resolution instanceof ModelResolution.NotFound notFound)
		{
			if (json)
			{
				JsonErrorWriter.writeError(context,
									 command,
									 ExitCodes.USAGE,
									 "Model not found: " + notFound.requested(),
									 List.of("Searched under: " + context.projectRoot()));
			}
			else
			{
				final var err = context.err();
				err.println("Model not found: " + notFound.requested());
				err.println("Searched under: " + context.projectRoot());
			}
			return new Result(null, ExitCodes.USAGE);
		}
		if (resolution instanceof ModelResolution.Failed failed)
		{
			if (json)
			{
				JsonErrorWriter.writeError(context,
									 command,
									 ExitCodes.USAGE,
									 "Failed to search for model: " + failed.message());
			}
			else
			{
				context.err().println("Failed to search for model: " + failed.message());
			}
			return new Result(null, ExitCodes.USAGE);
		}

		if (json)
		{
			JsonErrorWriter.writeError(context, command, ExitCodes.USAGE, "Unexpected model resolution state");
		}
		else
		{
			context.err().println("Unexpected model resolution state");
		}
		return new Result(null, ExitCodes.USAGE);
	}
}
