package org.logoce.lmf.cli.command;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.ExitCodes;
import org.logoce.lmf.cli.diagnostics.DiagnosticReporter;
import org.logoce.lmf.cli.json.JsonSerializers;
import org.logoce.lmf.cli.json.JsonErrorWriter;
import org.logoce.lmf.cli.json.JsonWriter;
import org.logoce.lmf.core.loader.api.tooling.workspace.WorkspaceScanDefaults;
import org.logoce.lmf.core.loader.api.tooling.validation.LmConstraints;
import org.logoce.lmf.cli.workspace.ModelSpecResolver;
import org.logoce.lmf.cli.util.PathDisplay;
import org.logoce.lmf.cli.workspace.DocumentLoader;
import org.logoce.lmf.cli.workspace.RegistryService;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.loader.api.loader.parsing.LmTreeReader;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class CheckCommand implements Command
{
	private final String modelSpec;
	private final boolean checkAll;
	private final boolean constraints;
	private final boolean verbose;
	private final boolean json;
	private final List<String> excludes;

	private CheckCommand(final String modelSpec,
						 final boolean checkAll,
						 final boolean constraints,
						 final boolean verbose,
						 final boolean json,
						 final List<String> excludes)
	{
		this.modelSpec = modelSpec;
		this.checkAll = checkAll;
		this.constraints = constraints;
		this.verbose = verbose;
		this.json = json;
		this.excludes = excludes == null ? List.of() : List.copyOf(excludes);
	}

	public static CheckCommand parse(final java.util.List<String> args, final java.io.PrintWriter err)
	{
		Objects.requireNonNull(args, "args");
		Objects.requireNonNull(err, "err");

		boolean checkAll = false;
		boolean constraints = false;
		boolean verbose = false;
		boolean json = false;
		String modelSpec = null;
		final var excludes = new ArrayList<String>();

		for (int i = 0; i < args.size(); i++)
		{
			final var arg = args.get(i);
			if ("--all".equals(arg))
			{
				checkAll = true;
				continue;
			}
			if ("--constraints".equals(arg))
			{
				constraints = true;
				continue;
			}
			if ("--json".equals(arg))
			{
				json = true;
				continue;
			}
			if ("--verbose".equals(arg))
			{
				verbose = true;
				continue;
			}
			if ("--exclude".equals(arg))
			{
				if (i + 1 >= args.size())
				{
					err.println("Missing value for --exclude");
					printUsage(err);
					return null;
				}
				excludes.add(args.get(++i));
				continue;
			}
			if (arg != null && arg.startsWith("--"))
			{
				err.println("Unknown option for check: " + arg);
				printUsage(err);
				return null;
			}
			if (modelSpec == null)
			{
				modelSpec = arg;
				continue;
			}

			printUsage(err);
			return null;
		}

		if (checkAll && modelSpec != null)
		{
			err.println("Cannot use both --all and <model.lm>");
			printUsage(err);
			return null;
		}

		if (checkAll)
		{
			return new CheckCommand(null, true, constraints, verbose, json, excludes);
		}

		if (!excludes.isEmpty())
		{
			err.println("Cannot use --exclude without --all");
			printUsage(err);
			return null;
		}

		if (verbose)
		{
			err.println("Cannot use --verbose without --all");
			printUsage(err);
			return null;
		}

		if (modelSpec != null && !modelSpec.isBlank())
		{
			return new CheckCommand(modelSpec, false, constraints, false, json, List.of());
		}

		printUsage(err);
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
			return json ? checkAllJson(context) : checkAll(context, verbose);
		}

		final var resolved = ModelSpecResolver.resolve(context, modelSpec, "check", json);
		if (!resolved.ok())
		{
			return resolved.exitCode();
		}

		final var documentLoader = new DocumentLoader();
		final var registryService = new RegistryService(context.projectRoot(), documentLoader);
		return json
			   ? checkModelJson(registryService, documentLoader, resolved.path(), context, modelSpec)
			   : checkModel(registryService, documentLoader, resolved.path(), context);
	}

	private int checkAllJson(final CliContext context)
	{
		final var root = context.projectRoot();
		final java.util.List<Path> paths;
		try
		{
			paths = discoverWorkspaceModels(root, excludes);
		}
		catch (java.io.IOException e)
		{
			final var message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
			JsonErrorWriter.writeError(context, "check", ExitCodes.INVALID, "Failed to scan workspace: " + message, List.of());
			return ExitCodes.INVALID;
		}

		final var json = new JsonWriter(context.out());
		json.beginObject()
			.name("command").value("check")
			.name("mode").value("all")
			.name("projectRoot").value(root.toString())
			.name("results").beginArray();

		if (paths.isEmpty())
		{
			json.endArray()
				.name("total").value(0)
				.name("invalidCount").value(0)
				.name("ok").value(true)
				.name("exitCode").value(ExitCodes.OK)
				.endObject()
				.flush();
			context.out().println();
			return ExitCodes.OK;
		}

		final var documentLoader = new DocumentLoader();
		final var registryService = new RegistryService(root, documentLoader);

		boolean anyInvalid = false;
		int invalidCount = 0;
		for (final var path : paths)
		{
			final var result = checkModelJsonResult(registryService, documentLoader, path, context);
			if (!result.ok())
			{
				anyInvalid = true;
				invalidCount++;
			}
			writeJsonModelResultObject(json, result);
		}

		final int exitCode = anyInvalid ? ExitCodes.INVALID : ExitCodes.OK;
		json.endArray()
			.name("total").value(paths.size())
			.name("invalidCount").value(invalidCount)
			.name("ok").value(!anyInvalid)
			.name("exitCode").value(exitCode)
			.endObject()
			.flush();
		context.out().println();
		return exitCode;
	}

	private int checkAll(final CliContext context, final boolean verbose)
	{
		final var root = context.projectRoot();
		final java.util.List<Path> paths;
		try
		{
			paths = discoverWorkspaceModels(root, excludes);
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
		int invalidCount = 0;
		for (final var path : paths)
		{
			final int code = checkModelAll(registryService, documentLoader, path, context, verbose);
			if (code != ExitCodes.OK)
			{
				invalidCount++;
			}
		}

		final int exitCode = invalidCount == 0 ? ExitCodes.OK : ExitCodes.INVALID;
		final var out = context.out();
		if (exitCode == ExitCodes.OK)
		{
			out.println("OK: checked " + paths.size() + " .lm file(s)");
		}
		else
		{
			out.println("INVALID: " + invalidCount + "/" + paths.size() + " .lm file(s) have errors");
		}
		return exitCode;
	}

	private int checkModelJson(final RegistryService registryService,
							   final DocumentLoader documentLoader,
							   final Path path,
							   final CliContext context,
							   final String requestedModel)
	{
		final var result = checkModelJsonResult(registryService, documentLoader, path, context, requestedModel);
		final var json = new JsonWriter(context.out());
		json.beginObject()
			.name("command").value("check")
			.name("mode").value("single")
			.name("projectRoot").value(context.projectRoot().toString());
		writeJsonModelResultFields(json, result);
		json.endObject().flush();
		context.out().println();
		return result.exitCode();
	}

	private CheckModelJsonResult checkModelJsonResult(final RegistryService registryService,
													  final DocumentLoader documentLoader,
													  final Path path,
													  final CliContext context)
	{
		return checkModelJsonResult(registryService, documentLoader, path, context, null);
	}

	private CheckModelJsonResult checkModelJsonResult(final RegistryService registryService,
													  final DocumentLoader documentLoader,
													  final Path path,
													  final CliContext context,
													  final String requestedModel)
	{
		final var displayPath = PathDisplay.display(context.projectRoot(), path);

		final var source = documentLoader.readString(path, context.err());
		if (source == null)
		{
			return new CheckModelJsonResult(requestedModel,
											displayPath,
											false,
											ExitCodes.INVALID,
											List.of(),
											List.of("Failed to read model file"));
		}

		final var parseDiagnostics = parseDiagnostics(source);
		if (DiagnosticReporter.hasErrors(parseDiagnostics))
		{
			return new CheckModelJsonResult(requestedModel,
											displayPath,
											false,
											ExitCodes.INVALID,
											List.copyOf(parseDiagnostics),
											List.of());
		}

		final var prepareResult = registryService.prepareForModel(path, context.err());
		if (prepareResult instanceof RegistryService.PrepareResult.Failure failure)
		{
			final var messages = new ArrayList<String>();
			if (failure.message() != null && !failure.message().isBlank())
			{
				messages.add(failure.message());
			}
			if (failure.details() != null && !failure.details().isEmpty())
			{
				messages.addAll(failure.details());
			}

			return new CheckModelJsonResult(requestedModel,
											displayPath,
											false,
											failure.exitCode(),
											List.of(),
											List.copyOf(messages));
		}

		final var prepared = ((RegistryService.PrepareResult.Success) prepareResult).registry();
		final var document = documentLoader.loadModelFromSource(prepared.registry(),
																prepared.targetQualifiedName(),
																source,
																context.err());
		final var diagnostics = new ArrayList<LmDiagnostic>(document.diagnostics());

		final boolean baseOk = !DiagnosticReporter.hasErrors(diagnostics);
		if (constraints && baseOk)
		{
			diagnostics.addAll(LmConstraints.mandatoryFeatureWarnings(document.linkTrees(), document.source()));
		}

		final boolean ok = !DiagnosticReporter.hasErrors(diagnostics);
		final int exitCode = ok ? ExitCodes.OK : ExitCodes.INVALID;
		return new CheckModelJsonResult(requestedModel,
										displayPath,
										ok,
										exitCode,
										List.copyOf(diagnostics),
										List.of());
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
		final var diagnostics = new ArrayList<LmDiagnostic>(document.diagnostics());

		if (constraints && !DiagnosticReporter.hasErrors(diagnostics))
		{
			diagnostics.addAll(LmConstraints.mandatoryFeatureWarnings(document.linkTrees(), document.source()));
		}

		DiagnosticReporter.printDiagnostics(context.err(), displayPath, List.copyOf(diagnostics));

		if (DiagnosticReporter.hasErrors(diagnostics))
		{
			context.err().println("INVALID: " + displayPath);
			return ExitCodes.INVALID;
		}

		context.out().println("OK: " + displayPath);
		return ExitCodes.OK;
	}

	private int checkModelAll(final RegistryService registryService,
							  final DocumentLoader documentLoader,
							  final Path path,
							  final CliContext context,
							  final boolean verbose)
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
		final var diagnostics = new ArrayList<LmDiagnostic>(document.diagnostics());

		if (constraints && !DiagnosticReporter.hasErrors(diagnostics))
		{
			diagnostics.addAll(LmConstraints.mandatoryFeatureWarnings(document.linkTrees(), document.source()));
		}

		if (verbose || constraints || DiagnosticReporter.hasErrors(diagnostics))
		{
			DiagnosticReporter.printDiagnostics(context.err(), displayPath, List.copyOf(diagnostics));
		}

		if (DiagnosticReporter.hasErrors(diagnostics))
		{
			context.err().println("INVALID: " + displayPath);
			return ExitCodes.INVALID;
		}

		if (verbose)
		{
			context.out().println("OK: " + displayPath);
		}
		return ExitCodes.OK;
	}

	private static List<LmDiagnostic> parseDiagnostics(final CharSequence source)
	{
		final var diagnostics = new ArrayList<LmDiagnostic>();
		new LmTreeReader().read(source, diagnostics);
		return List.copyOf(diagnostics);
	}

	private static void printUsage(final java.io.PrintWriter err)
	{
		err.println("Usage: lm [--project-root <path>] check <model.lm> [--constraints] [--json]");
		err.println("   or: lm [--project-root <path>] check --all [--constraints] [--verbose] [--exclude <path|glob>]... [--json]");
	}


	private static void writeJsonModelResultObject(final JsonWriter json, final CheckModelJsonResult result)
	{
		json.beginObject();
		writeJsonModelResultFields(json, result);
		json.endObject();
	}

	private static void writeJsonModelResultFields(final JsonWriter json, final CheckModelJsonResult result)
	{
		json.name("model").beginObject();
		if (result.requestedModel() != null)
		{
			json.name("requested").value(result.requestedModel());
		}
		json.name("path").value(result.path())
			.endObject()
			.name("diagnostics").beginArray();
		for (final var diagnostic : result.diagnostics())
		{
			JsonSerializers.writeDiagnostic(json, diagnostic);
		}
		json.endArray();
		if (result.messages() != null && !result.messages().isEmpty())
		{
			json.name("messages").beginArray();
			for (final var message : result.messages())
			{
				json.value(message);
			}
			json.endArray();
		}
		json.name("ok").value(result.ok())
			.name("exitCode").value(result.exitCode());
	}

	private record CheckModelJsonResult(String requestedModel,
										String path,
										boolean ok,
										int exitCode,
										List<LmDiagnostic> diagnostics,
										List<String> messages)
	{
	}

	private static List<Path> discoverWorkspaceModels(final Path root, final List<String> excludes) throws java.io.IOException
	{
		final var normalizedRoot = root.toAbsolutePath().normalize();
		final var excludeFilter = ExcludeFilter.compile(normalizedRoot, excludes);

		final var paths = new ArrayList<Path>();
		java.nio.file.Files.walkFileTree(normalizedRoot, new java.nio.file.SimpleFileVisitor<>()
		{
			@Override
			public java.nio.file.FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
			{
				if (!dir.equals(normalizedRoot))
				{
					final var name = dir.getFileName();
					if (name != null && WorkspaceScanDefaults.isIgnoredDirectoryName(name.toString()))
					{
						return java.nio.file.FileVisitResult.SKIP_SUBTREE;
					}
					if (excludeFilter.excludes(dir))
					{
						return java.nio.file.FileVisitResult.SKIP_SUBTREE;
					}
				}
				return java.nio.file.FileVisitResult.CONTINUE;
			}

			@Override
			public java.nio.file.FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
			{
				if (attrs != null && !attrs.isRegularFile())
				{
					return java.nio.file.FileVisitResult.CONTINUE;
				}
				if (!file.getFileName().toString().endsWith(".lm"))
				{
					return java.nio.file.FileVisitResult.CONTINUE;
				}
				if (!excludeFilter.excludes(file))
				{
					paths.add(file.toAbsolutePath().normalize());
				}
				return java.nio.file.FileVisitResult.CONTINUE;
			}
		});

		paths.sort(Comparator.naturalOrder());
		return List.copyOf(paths);
	}

	private record ExcludeFilter(Path root, List<Path> prefixExcludes, List<java.nio.file.PathMatcher> globExcludes)
	{
		static ExcludeFilter compile(final Path root, final List<String> specs)
		{
			final var normalizedRoot = root.toAbsolutePath().normalize();
			if (specs == null || specs.isEmpty())
			{
				return new ExcludeFilter(normalizedRoot, List.of(), List.of());
			}

			final var prefixes = new ArrayList<Path>();
			final var globs = new ArrayList<java.nio.file.PathMatcher>();

			for (final var spec : specs)
			{
				if (spec == null || spec.isBlank())
				{
					continue;
				}

				if (containsGlobMeta(spec))
				{
					globs.add(normalizedRoot.getFileSystem().getPathMatcher("glob:" + spec));
					continue;
				}

				Path path;
				try
				{
					path = Path.of(spec);
				}
				catch (RuntimeException ignored)
				{
					globs.add(normalizedRoot.getFileSystem().getPathMatcher("glob:" + spec));
					continue;
				}

				final var absolute = path.isAbsolute() ? path.toAbsolutePath().normalize() : normalizedRoot.resolve(path).normalize();
				prefixes.add(absolute);
			}

			return new ExcludeFilter(normalizedRoot, List.copyOf(prefixes), List.copyOf(globs));
		}

		boolean excludes(final Path path)
		{
			if (path == null)
			{
				return false;
			}

			final var normalized = path.toAbsolutePath().normalize();
			for (final var prefix : prefixExcludes)
			{
				if (normalized.startsWith(prefix))
				{
					return true;
				}
			}

			if (globExcludes.isEmpty())
			{
				return false;
			}

			final Path relative;
			try
			{
				relative = root.relativize(normalized);
			}
			catch (IllegalArgumentException ignored)
			{
				return false;
			}

			for (final var matcher : globExcludes)
			{
				if (matcher.matches(relative))
				{
					return true;
				}
			}
			return false;
		}

		private static boolean containsGlobMeta(final String spec)
		{
			for (int i = 0; i < spec.length(); i++)
			{
				switch (spec.charAt(i))
				{
					case '*', '?', '[', ']', '{', '}' -> {
						return true;
					}
					default -> {
					}
				}
			}
			return false;
		}
	}
}
