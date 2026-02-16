package org.logoce.lmf.cli.format;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.ExitCodes;
import org.logoce.lmf.cli.diagnostics.DiagnosticReporter;
import org.logoce.lmf.cli.json.JsonErrorWriter;
import org.logoce.lmf.cli.json.JsonWriter;
import org.logoce.lmf.cli.util.PathDisplay;
import org.logoce.lmf.cli.workspace.ModelSpecResolver;
import org.logoce.lmf.cli.workspace.DocumentLoader;
import org.logoce.lmf.cli.workspace.RegistryService;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class FmtRunner
{
	public record Options(String rootReference, boolean refPathToName, boolean syntaxOnly, boolean inPlace, boolean json)
	{
		public Options
		{
			if (rootReference != null && rootReference.isBlank())
			{
				rootReference = null;
			}
		}
	}

	public int run(final CliContext context, final String modelSpec, final Options options)
	{
		Objects.requireNonNull(context, "context");
		Objects.requireNonNull(modelSpec, "modelSpec");
		Objects.requireNonNull(options, "options");

		final var resolved = ModelSpecResolver.resolve(context, modelSpec, "fmt", options.json());
		if (!resolved.ok())
		{
			return resolved.exitCode();
		}
		return formatModel(modelSpec, resolved.path(), context, options);
	}

	private int formatModel(final String requestedModel, final Path path, final CliContext context, final Options options)
	{
		final var displayPath = PathDisplay.display(context.projectRoot(), path);
		final var documentLoader = new DocumentLoader();
		final var source = documentLoader.readString(path, context.err());
		if (source == null)
		{
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "fmt", ExitCodes.INVALID, "Failed to read model file: " + displayPath);
			}
			return ExitCodes.INVALID;
		}

		final var parseDiagnostics = new ArrayList<org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic>();
		final var reader = new org.logoce.lmf.core.loader.api.loader.parsing.LmTreeReader();
		final var readResult = reader.read(source, parseDiagnostics);
		if (DiagnosticReporter.hasErrors(parseDiagnostics))
		{
			DiagnosticReporter.printDiagnostics(context.err(), displayPath, parseDiagnostics);
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "fmt", ExitCodes.INVALID, "Model has syntax errors: " + displayPath);
			}
			return ExitCodes.INVALID;
		}

		if (options.syntaxOnly())
		{
			if (options.rootReference() != null)
			{
				context.err().println("Cannot use --root with --syntax-only (requires semantic linking)");
				return ExitCodes.USAGE;
			}
			if (options.refPathToName())
			{
				context.err().println("Cannot use --ref-path-to-name with --syntax-only (requires semantic linking)");
				return ExitCodes.USAGE;
			}

			if (readResult.roots().isEmpty())
			{
				context.err().println("No syntax roots found in " + displayPath);
				if (options.json())
				{
					JsonErrorWriter.writeError(context, "fmt", ExitCodes.INVALID, "No syntax roots found in " + displayPath);
				}
				return ExitCodes.INVALID;
			}

			final var formatter = new LmFormatter();
			final var formatted = ensureTrailingNewline(formatter.format(readResult.roots()));
			return emitFormatted(context, requestedModel, path, displayPath, source, formatted, options);
		}

		final var registryService = new RegistryService(context.projectRoot(), documentLoader);
		final var prepareResult = registryService.prepareForModel(path, context.err());
		if (prepareResult instanceof RegistryService.PrepareResult.Failure)
		{
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "fmt", ExitCodes.INVALID, "Cannot prepare model registry for " + displayPath);
			}
			return ExitCodes.INVALID;
		}

		final var prepared = ((RegistryService.PrepareResult.Success) prepareResult).registry();
		final var document = documentLoader.loadModelFromSource(prepared.registry(),
																prepared.targetQualifiedName(),
																source,
																context.err());
		final var diagnostics = document.diagnostics();

		if (DiagnosticReporter.hasErrors(diagnostics))
		{
			DiagnosticReporter.printDiagnostics(context.err(), displayPath, diagnostics);
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "fmt", ExitCodes.INVALID, "Model has linking errors: " + displayPath);
			}
			return ExitCodes.INVALID;
		}

		final var roots = document.roots();
		if (roots.isEmpty())
		{
			context.err().println("No syntax roots found in " + displayPath);
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "fmt", ExitCodes.INVALID, "No syntax roots found in " + displayPath);
			}
			return ExitCodes.INVALID;
		}

		if (options.rootReference() == null)
		{
			final var formatted = formatRoots(document, roots, options);
			return emitFormatted(context, requestedModel, path, displayPath, source, formatted, options);
		}

		final var formatted = formatRootReference(document, displayPath, options, context);
		if (formatted == null)
		{
			return ExitCodes.USAGE;
		}
		return emitFormatted(context, requestedModel, path, displayPath, source, formatted, options);
	}

	private static String formatRoots(final org.logoce.lmf.core.loader.api.loader.model.LmDocument document,
									  final List<org.logoce.lmf.core.util.tree.Tree<PNode>> roots,
									  final Options options)
	{
		final var formatter = new LmFormatter();
		final String formattedBody;

		if (!options.refPathToName())
		{
			formattedBody = formatter.format(roots);
		}
		else
		{
			final var linkRoots = RootReferenceResolver.collectLinkRoots(document.linkTrees());
			if (!linkRoots.isEmpty())
			{
				final var builder = new StringBuilder();
				for (int i = 0; i < linkRoots.size(); i++)
				{
					final var linkRoot = linkRoots.get(i);
					final var index = ReferencePathToNameIndex.build(linkRoot);
					builder.append(formatter.format(linkRoot, index));
					if (i < linkRoots.size() - 1)
					{
						builder.append('\n').append('\n');
					}
				}
				formattedBody = builder.toString();
			}
			else
			{
				formattedBody = formatter.format(roots);
			}
		}

		return ensureTrailingNewline(formattedBody);
	}

	private static String formatRootReference(final org.logoce.lmf.core.loader.api.loader.model.LmDocument document,
											  final String displayPath,
											  final Options options,
											  final CliContext context)
	{
		final var linkRoots = RootReferenceResolver.collectLinkRoots(document.linkTrees());
		if (linkRoots.isEmpty())
		{
			context.err().println("Unable to resolve --root because no link trees are available for " + displayPath);
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "fmt", ExitCodes.INVALID, "Unable to resolve --root because no link trees are available for " + displayPath);
			}
			return null;
		}

		final var resolution = new RootReferenceResolver().resolve(linkRoots, options.rootReference());
		if (resolution instanceof RootReferenceResolver.Resolution.Found found)
		{
			final var formatter = new LmFormatter();
			final var index = options.refPathToName() ? ReferencePathToNameIndex.build(found.node().root()) : null;
			return ensureTrailingNewline(formatter.format(found.node(), index));
		}
		if (resolution instanceof RootReferenceResolver.Resolution.Ambiguous ambiguous)
		{
			context.err().println("Ambiguous --root reference: " + options.rootReference());
			for (final var candidate : ambiguous.candidates())
			{
				context.err().println(" - " + candidate);
			}
			if (options.json())
			{
				JsonErrorWriter.writeError(context,
										   "fmt",
										   ExitCodes.USAGE,
										   "Ambiguous --root reference: " + options.rootReference(),
										   ambiguous.candidates());
			}
			return null;
		}
		if (resolution instanceof RootReferenceResolver.Resolution.NotFound notFound)
		{
			context.err().println("Cannot resolve --root reference: " + options.rootReference());
			context.err().println(notFound.message());
			if (options.json())
			{
				JsonErrorWriter.writeError(context,
										   "fmt",
										   ExitCodes.USAGE,
										   "Cannot resolve --root reference: " + options.rootReference(),
										   List.of(notFound.message()));
			}
			return null;
		}
		if (resolution instanceof RootReferenceResolver.Resolution.Failure failure)
		{
			context.err().println("Cannot resolve --root reference: " + options.rootReference());
			context.err().println(failure.message());
			if (options.json())
			{
				JsonErrorWriter.writeError(context,
										   "fmt",
										   ExitCodes.USAGE,
										   "Cannot resolve --root reference: " + options.rootReference(),
										   List.of(failure.message()));
			}
			return null;
		}

		context.err().println("Unexpected root resolution state");
		if (options.json())
		{
			JsonErrorWriter.writeError(context, "fmt", ExitCodes.USAGE, "Unexpected root resolution state");
		}
		return null;
	}

	private int emitFormatted(final CliContext context,
							  final String requestedModel,
							  final Path modelPath,
							  final String displayPath,
							  final String originalSource,
							  final String formatted,
							  final Options options)
	{
		if (options.inPlace())
		{
			return writeInPlace(context, requestedModel, modelPath, displayPath, originalSource, formatted, options);
		}

		if (options.json())
		{
			writeJsonResult(context, requestedModel, displayPath, formatted, false, false, options);
			return ExitCodes.OK;
		}

		context.out().print(formatted);
		context.out().flush();
		return ExitCodes.OK;
	}

	private int writeInPlace(final CliContext context,
							 final String requestedModel,
							 final Path modelPath,
							 final String displayPath,
							 final String originalSource,
							 final String formatted,
							 final Options options)
	{
		final var normalizedOriginal = ensureTrailingNewline(originalSource);
		final boolean changed = !Objects.equals(normalizedOriginal, formatted);
		if (!changed)
		{
			if (options.json())
			{
				writeJsonResult(context, requestedModel, displayPath, null, false, false, options);
			}
			else
			{
				context.out().println("OK: already formatted " + displayPath);
			}
			return ExitCodes.OK;
		}

		try
		{
			Files.writeString(modelPath.toAbsolutePath().normalize(), formatted, StandardCharsets.UTF_8);
		}
		catch (Exception e)
		{
			final var message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "fmt", ExitCodes.INVALID, "Failed to write file: " + displayPath + " (" + message + ")");
			}
			else
			{
				context.err().println("Failed to write file: " + displayPath + " (" + message + ")");
			}
			return ExitCodes.INVALID;
		}

		if (options.json())
		{
			writeJsonResult(context, requestedModel, displayPath, null, true, true, options);
		}
		else
		{
			context.out().println("OK: formatted " + displayPath);
		}
		return ExitCodes.OK;
	}

	private static void writeJsonResult(final CliContext context,
										final String requestedModel,
										final String displayPath,
										final String formatted,
										final boolean changed,
										final boolean wrote,
										final Options options)
	{
		final var json = new JsonWriter(context.out());
		json.beginObject()
			.name("command").value("fmt")
			.name("projectRoot").value(context.projectRoot().toString())
			.name("model").beginObject()
			.name("requested").value(requestedModel)
			.name("path").value(displayPath)
			.endObject()
			.name("options").beginObject()
			.name("root").value(options.rootReference())
			.name("refPathToName").value(options.refPathToName())
			.name("syntaxOnly").value(options.syntaxOnly())
			.name("inPlace").value(options.inPlace())
			.endObject();

		if (!options.inPlace())
		{
			json.name("formatted").value(formatted);
		}
		else
		{
			json.name("changed").value(changed)
				.name("wrote").value(wrote);
		}

		json.name("ok").value(true)
			.name("exitCode").value(ExitCodes.OK)
			.endObject()
			.flush();
		context.out().println();
	}

	private static String ensureTrailingNewline(final String formatted)
	{
		if (formatted == null || formatted.isEmpty())
		{
			return "\n";
		}
		return formatted.endsWith("\n") ? formatted : formatted + "\n";
	}
}
