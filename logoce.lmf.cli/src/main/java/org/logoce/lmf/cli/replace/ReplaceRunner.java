package org.logoce.lmf.cli.replace;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.ExitCodes;
import org.logoce.lmf.cli.diagnostics.DiagnosticItem;
import org.logoce.lmf.cli.diagnostics.DiagnosticReporter;
import org.logoce.lmf.cli.diagnostics.ValidationReport;
import org.logoce.lmf.cli.edit.EditJsonReportWriter;
import org.logoce.lmf.cli.edit.EditOptions;
import org.logoce.lmf.cli.edit.EditValidationContext;
import org.logoce.lmf.cli.edit.SubtreeSpanLocator;
import org.logoce.lmf.cli.edit.TextEdits;
import org.logoce.lmf.cli.edit.WorkspaceEditPipeline;
import org.logoce.lmf.cli.format.LmFormatter;
import org.logoce.lmf.cli.format.RootReferenceResolver;
import org.logoce.lmf.cli.json.JsonErrorWriter;
import org.logoce.lmf.cli.json.JsonWriter;
import org.logoce.lmf.cli.util.PathDisplay;
import org.logoce.lmf.cli.workspace.DocumentLoader;
import org.logoce.lmf.cli.workspace.ModelSpecResolver;
import org.logoce.lmf.cli.workspace.RegistryService;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.loader.api.loader.parsing.LmTreeReader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ReplaceRunner
{
	public record Options(boolean force, boolean json)
	{
	}

	public int run(final CliContext context,
				   final String modelSpec,
				   final String targetReference,
				   final String replacementSubtree,
				   final Options options)
	{
		Objects.requireNonNull(context, "context");
		Objects.requireNonNull(modelSpec, "modelSpec");
		Objects.requireNonNull(targetReference, "targetReference");
		Objects.requireNonNull(replacementSubtree, "replacementSubtree");
		Objects.requireNonNull(options, "options");

		final var resolved = ModelSpecResolver.resolve(context, modelSpec, "replace", options.json());
		if (!resolved.ok())
		{
			return resolved.exitCode();
		}
		return replaceInModel(resolved.path(), context, modelSpec, targetReference, replacementSubtree, options);
	}

	private int replaceInModel(final Path modelPath,
							   final CliContext context,
							   final String requestedModel,
							   final String targetReference,
							   final String replacementSubtree,
							   final Options options)
	{
		final var projectRoot = context.projectRoot();
		final var displayPath = PathDisplay.display(projectRoot, modelPath);

		final var documentLoader = new DocumentLoader();
		final var registryService = new RegistryService(projectRoot, documentLoader);
		final var prepareResult = registryService.prepareForModel(modelPath, context.err());

		if (prepareResult instanceof RegistryService.PrepareResult.Failure failure)
		{
			if (options.json())
			{
				JsonErrorWriter.writeError(context,
										   "replace",
										   failure.exitCode(),
										   "Cannot prepare registry for " + displayPath);
			}
			else
			{
				context.err().println("No changes written to " + displayPath);
			}
			return failure.exitCode();
		}

		final var prepared = ((RegistryService.PrepareResult.Success) prepareResult).registry();
		final var registry = prepared.registry();
		final var targetQualifiedName = prepared.targetQualifiedName();

		final var originalSource = documentLoader.readString(modelPath, context.err());
		if (originalSource == null)
		{
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "replace", ExitCodes.INVALID, "Failed to read model file");
			}
			else
			{
				context.err().println("No changes written to " + displayPath);
			}
			return ExitCodes.INVALID;
		}

		final var originalDocument = documentLoader.loadModelFromSource(registry,
																		targetQualifiedName,
																		originalSource,
																		context.err());
		final var linkRoots = RootReferenceResolver.collectLinkRoots(originalDocument.linkTrees());
		if (linkRoots.isEmpty())
		{
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "replace", ExitCodes.INVALID, "No link trees available for " + displayPath);
			}
			else
			{
				context.err().println("No link trees available for " + displayPath);
				context.err().println("No changes written to " + displayPath);
			}
			return ExitCodes.INVALID;
		}

		final var resolution = new RootReferenceResolver().resolve(linkRoots, targetReference);
		if (resolution instanceof RootReferenceResolver.Resolution.Found found)
		{
			return replaceSpan(context,
							   modelPath,
							   displayPath,
							   requestedModel,
							   prepared,
							   originalSource,
							   found.node(),
							   targetReference,
							   replacementSubtree,
							   options);
		}
		if (resolution instanceof RootReferenceResolver.Resolution.Ambiguous ambiguous)
		{
			context.err().println("Ambiguous reference: " + targetReference);
			for (final var candidate : ambiguous.candidates())
			{
				context.err().println(" - " + candidate);
			}
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "replace", ExitCodes.USAGE, "Ambiguous reference: " + targetReference);
			}
			else
			{
				context.err().println("No changes written to " + displayPath);
			}
			return ExitCodes.USAGE;
		}
		if (resolution instanceof RootReferenceResolver.Resolution.NotFound notFound)
		{
			context.err().println("Cannot resolve reference: " + targetReference);
			context.err().println(notFound.message());
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "replace", ExitCodes.USAGE, "Cannot resolve reference: " + targetReference);
			}
			else
			{
				context.err().println("No changes written to " + displayPath);
			}
			return ExitCodes.USAGE;
		}
		if (resolution instanceof RootReferenceResolver.Resolution.Failure failure)
		{
			context.err().println("Cannot resolve reference: " + targetReference);
			context.err().println(failure.message());
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "replace", ExitCodes.USAGE, "Cannot resolve reference: " + targetReference);
			}
			else
			{
				context.err().println("No changes written to " + displayPath);
			}
			return ExitCodes.USAGE;
		}

		context.err().println("Unexpected reference resolution state");
		if (options.json())
		{
			JsonErrorWriter.writeError(context, "replace", ExitCodes.USAGE, "Unexpected reference resolution state");
		}
		else
		{
			context.err().println("No changes written to " + displayPath);
		}
		return ExitCodes.USAGE;
	}

	private int replaceSpan(final CliContext context,
							final Path modelPath,
							final String displayPath,
							final String requestedModel,
							final RegistryService.PreparedRegistry prepared,
							final String originalSource,
							final org.logoce.lmf.core.loader.api.loader.linking.tree.LinkNodeInternal<?, org.logoce.lmf.core.loader.api.text.syntax.PNode, ?> targetNode,
							final String targetReference,
							final String replacementSubtree,
							final Options options)
	{
		final var span = SubtreeSpanLocator.locate(originalSource, targetNode);
		if (span == null)
		{
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "replace", ExitCodes.INVALID, "Cannot locate subtree span for reference: " + targetReference);
			}
			else
			{
				context.err().println("Cannot locate subtree span for reference: " + targetReference);
				context.err().println("No changes written to " + displayPath);
			}
			return ExitCodes.INVALID;
		}

		final var formattedReplacement = formatSingleRootSubtree(replacementSubtree, context.err());
		if (formattedReplacement instanceof SubtreeFormatResult.Failure failure)
		{
			final var report = new ValidationReport(false, toSubtreeDiagnostics(failure.diagnostics()), List.of());
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "replace", ExitCodes.USAGE, failure.message(), report);
			}
			else
			{
				context.err().println("No changes written to " + displayPath);
			}
			return ExitCodes.USAGE;
		}
		final var formatted = ((SubtreeFormatResult.Success) formattedReplacement).formatted();

		final var baseIndent = trailingIndentBefore(originalSource, span.startOffset());
		final var indentedReplacement = baseIndent.isEmpty()
										? formatted
										: formatted.replace("\n", "\n" + baseIndent);

		final var documentLoader = new DocumentLoader();
		final var editsByFile = Map.of(modelPath,
									   List.of(new TextEdits.TextEdit(span.startOffset(),
																	 span.length(),
																	 indentedReplacement)));
		final var sourcesByPath = Map.of(modelPath, originalSource);
		final var pipeline = new WorkspaceEditPipeline();
		final var validationContext = new EditValidationContext.SingleModel(prepared,
																			context.projectRoot(),
																			documentLoader);
		final var outcome = pipeline.applyEdits(validationContext,
												editsByFile,
												sourcesByPath,
												new EditOptions(true, true, options.force(), true),
												context.err());

		if (!outcome.wrote())
		{
			if (options.json())
			{
				writeJsonResult(context,
								requestedModel,
								displayPath,
								targetReference,
								options.force(),
								outcome,
								"No changes written",
								ExitCodes.INVALID);
			}
			else
			{
				context.err().println("No changes written to " + displayPath);
			}
			return ExitCodes.INVALID;
		}

		if (!outcome.validationPassed())
		{
			if (options.json())
			{
				writeJsonResult(context,
								requestedModel,
								displayPath,
								targetReference,
								options.force(),
								outcome,
								"FORCED: wrote changes despite errors",
								ExitCodes.INVALID);
			}
			else
			{
				context.err().println("FORCED: wrote changes to " + displayPath + " despite errors");
			}
			return ExitCodes.INVALID;
		}

		if (options.json())
		{
			writeJsonResult(context,
							requestedModel,
							displayPath,
							targetReference,
							options.force(),
							outcome,
							"OK: replaced " + targetReference,
							ExitCodes.OK);
		}
		else
		{
			context.out().println("OK: replaced " + targetReference + " in " + displayPath);
		}
		return ExitCodes.OK;
	}

	private static void writeJsonResult(final CliContext context,
										final String requestedModel,
										final String displayPath,
										final String targetReference,
										final boolean force,
										final org.logoce.lmf.cli.edit.EditOutcome outcome,
										final String message,
										final int exitCode)
	{
		final var json = new JsonWriter(context.out());
		json.beginObject()
			.name("command").value("replace")
			.name("projectRoot").value(context.projectRoot().toString())
			.name("model").beginObject()
			.name("requested").value(requestedModel)
			.name("path").value(displayPath)
			.endObject()
			.name("target").beginObject()
			.name("reference").value(targetReference)
			.endObject()
			.name("options").beginObject()
			.name("force").value(force)
			.endObject();

		EditJsonReportWriter.writeOutcome(json, context, outcome);
		EditJsonReportWriter.writeDiagnostics(json, outcome.validationReport());

		json.name("message").value(message)
			.name("ok").value(exitCode == ExitCodes.OK)
			.name("exitCode").value(exitCode)
			.endObject()
			.flush();
		context.out().println();
	}

	private sealed interface SubtreeFormatResult permits SubtreeFormatResult.Success, SubtreeFormatResult.Failure
	{
		record Success(String formatted) implements SubtreeFormatResult
		{
		}

		record Failure(String message, List<LmDiagnostic> diagnostics) implements SubtreeFormatResult
		{
			public Failure
			{
				Objects.requireNonNull(message, "message");
				diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics);
			}
		}
	}

	private static List<DiagnosticItem> toSubtreeDiagnostics(final List<LmDiagnostic> diagnostics)
	{
		if (diagnostics == null || diagnostics.isEmpty())
		{
			return List.of();
		}
		return diagnostics.stream()
						  .map(diagnostic -> new DiagnosticItem("<subtree>", diagnostic))
						  .toList();
	}

	private static SubtreeFormatResult formatSingleRootSubtree(final String subtreeSource, final java.io.PrintWriter err)
	{
		final var diagnostics = new ArrayList<LmDiagnostic>();
		final var reader = new LmTreeReader();
		final var readResult = reader.read(subtreeSource, diagnostics);
		if (DiagnosticReporter.hasErrors(diagnostics))
		{
			DiagnosticReporter.printDiagnostics(err, "<subtree>", diagnostics);
			final var message = "Replacement subtree cannot be parsed";
			err.println(message);
			err.println("Hint: If you passed a subtree inline, prefer --subtree-stdin/--subtree-file to avoid shell quoting issues.");
			return new SubtreeFormatResult.Failure(message, List.copyOf(diagnostics));
		}

		if (readResult.roots().size() != 1)
		{
			final var message = "Replacement subtree must contain exactly one root element; found: " + readResult.roots().size();
			err.println(message);
			return new SubtreeFormatResult.Failure(message, List.of());
		}

		final var formatter = new LmFormatter();
		return new SubtreeFormatResult.Success(formatter.format(readResult.roots()));
	}

	private static String trailingIndentBefore(final CharSequence source, final int offset)
	{
		int start = offset;
		while (start > 0)
		{
			final char c = source.charAt(start - 1);
			if (c == ' ' || c == '\t')
			{
				start--;
				continue;
			}
			break;
		}
		return source.subSequence(start, offset).toString();
	}
}
