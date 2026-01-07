package org.logoce.lmf.cli.command;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.ExitCodes;
import org.logoce.lmf.cli.diagnostics.DiagnosticReporter;
import org.logoce.lmf.cli.workspace.ModelLocator;
import org.logoce.lmf.cli.workspace.ModelResolution;
import org.logoce.lmf.cli.util.PathDisplay;
import org.logoce.lmf.cli.workspace.DocumentLoader;
import org.logoce.lmf.cli.workspace.RegistryService;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.loader.api.loader.parsing.LmTreeReader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class CheckCommand implements Command
{
	private final String modelSpec;
	private final boolean checkAll;

	private CheckCommand(final String modelSpec, final boolean checkAll)
	{
		this.modelSpec = modelSpec;
		this.checkAll = checkAll;
	}

	public static CheckCommand parse(final java.util.List<String> args, final java.io.PrintWriter err)
	{
		Objects.requireNonNull(args, "args");
		Objects.requireNonNull(err, "err");

		if (args.size() == 1 && "--all".equals(args.getFirst()))
		{
			return new CheckCommand(null, true);
		}
		if (args.size() == 1 && args.getFirst() != null && !args.getFirst().startsWith("--"))
		{
			return new CheckCommand(args.getFirst(), false);
		}

		for (final var arg : args)
		{
			if (arg != null && arg.startsWith("--"))
			{
				err.println("Unknown option for check: " + arg);
				err.println("Usage: lm [--project-root <path>] check <model.lm>");
				err.println("   or: lm [--project-root <path>] check --all");
				return null;
			}
		}

		err.println("Usage: lm [--project-root <path>] check <model.lm>");
		err.println("   or: lm [--project-root <path>] check --all");
		return null;
	}

	@Override
	public String name()
	{
		return "check";
	}

	@Override
	public int execute(final CliContext context)
	{
		if (checkAll)
		{
			return checkAll(context);
		}

		final var locator = new ModelLocator(context.projectRoot());
		final var resolution = locator.resolve(modelSpec);
		final int exitCode;

		if (resolution instanceof ModelResolution.Found found)
		{
			final var documentLoader = new DocumentLoader();
			final var registryService = new RegistryService(context.projectRoot(), documentLoader);
			exitCode = checkModel(registryService, documentLoader, found.path(), context);
		}
		else if (resolution instanceof ModelResolution.Ambiguous ambiguous)
		{
			final var err = context.err();
			err.println("Ambiguous model reference: " + modelSpec);
			for (final var path : ambiguous.matches())
			{
				err.println(" - " + PathDisplay.display(context.projectRoot(), path));
			}
			exitCode = ExitCodes.USAGE;
		}
		else if (resolution instanceof ModelResolution.NotFound notFound)
		{
			final var err = context.err();
			err.println("Model not found: " + notFound.requested());
			err.println("Searched under: " + context.projectRoot());
			exitCode = ExitCodes.USAGE;
		}
		else if (resolution instanceof ModelResolution.Failed failed)
		{
			final var err = context.err();
			err.println("Failed to search for model: " + failed.message());
			exitCode = ExitCodes.USAGE;
		}
		else
		{
			context.err().println("Unexpected model resolution state");
			exitCode = ExitCodes.USAGE;
		}

		return exitCode;
	}

	private int checkAll(final CliContext context)
	{
		final var root = context.projectRoot();
		final java.util.List<Path> paths;
		try (final var walk = java.nio.file.Files.walk(root))
		{
			paths = walk.filter(java.nio.file.Files::isRegularFile)
						.filter(p -> p.getFileName().toString().endsWith(".lm"))
						.map(p -> p.toAbsolutePath().normalize())
						.sorted()
						.toList();
		}
		catch (java.io.IOException e)
		{
			final var message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
			context.err().println("Failed to scan workspace: " + message);
			return ExitCodes.INVALID;
		}

		if (paths.isEmpty())
		{
			context.out().println("OK: no .lm files found under " + root);
			return ExitCodes.OK;
		}

		final var documentLoader = new DocumentLoader();
		final var registryService = new RegistryService(root, documentLoader);
		boolean anyInvalid = false;
		for (final var path : paths)
		{
			final int code = checkModel(registryService, documentLoader, path, context);
			if (code != ExitCodes.OK)
			{
				anyInvalid = true;
			}
		}

		return anyInvalid ? ExitCodes.INVALID : ExitCodes.OK;
	}

	private int checkModel(final RegistryService registryService,
						   final DocumentLoader documentLoader,
						   final Path path,
						   final CliContext context)
	{
		final var displayPath = PathDisplay.display(context.projectRoot(), path);

		final var source = documentLoader.readString(path, context.err());
		if (source == null)
		{
			context.err().println("INVALID: " + displayPath);
			return ExitCodes.INVALID;
		}

		final var parseDiagnostics = parseDiagnostics(source);
		if (DiagnosticReporter.hasErrors(parseDiagnostics))
		{
			DiagnosticReporter.printDiagnostics(context.err(), displayPath, parseDiagnostics);
			context.err().println("INVALID: " + displayPath);
			return ExitCodes.INVALID;
		}

		final var prepareResult = registryService.prepareForModel(path, context.err());
		if (prepareResult instanceof RegistryService.PrepareResult.Failure)
		{
			context.err().println("INVALID: " + displayPath);
			return ExitCodes.INVALID;
		}

		final var prepared = ((RegistryService.PrepareResult.Success) prepareResult).registry();
		final var document = documentLoader.loadModelFromSource(prepared.registry(),
																prepared.targetQualifiedName(),
																source,
																context.err());
		final var diagnostics = document.diagnostics();

		DiagnosticReporter.printDiagnostics(context.err(), displayPath, diagnostics);

		if (DiagnosticReporter.hasErrors(diagnostics))
		{
			context.err().println("INVALID: " + displayPath);
			return ExitCodes.INVALID;
		}

		context.out().println("OK: " + displayPath);
		return ExitCodes.OK;
	}

	private static List<LmDiagnostic> parseDiagnostics(final CharSequence source)
	{
		final var diagnostics = new ArrayList<LmDiagnostic>();
		new LmTreeReader().read(source, diagnostics);
		return List.copyOf(diagnostics);
	}
}
