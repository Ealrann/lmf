package org.logoce.lmf.cli.batch;

import org.logoce.lmf.cli.ExitCodes;
import org.logoce.lmf.cli.diagnostics.DiagnosticItem;
import org.logoce.lmf.cli.diagnostics.DiagnosticReporter;
import org.logoce.lmf.cli.diagnostics.ValidationReport;
import org.logoce.lmf.cli.edit.ReferenceRewrite;
import org.logoce.lmf.cli.format.LmFormatter;
import org.logoce.lmf.cli.format.LmSourceFormatter;
import org.logoce.lmf.cli.format.RootReferenceResolver;
import org.logoce.lmf.cli.remove.RemoveUnsetReference;
import org.logoce.lmf.cli.util.PathDisplay;
import org.logoce.lmf.cli.workspace.ModelLocator;
import org.logoce.lmf.cli.workspace.ModelResolution;
import org.logoce.lmf.cli.workspace.RegistryService;
import org.logoce.lmf.cli.workspace.WorkspaceDocuments;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.loader.api.loader.parsing.LmTreeReader;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class BatchOperationSupport
{
	private final BatchExecutionContext context;
	private final LmSourceFormatter sourceFormatter = new LmSourceFormatter();

	BatchOperationSupport(final BatchExecutionContext context)
	{
		this.context = Objects.requireNonNull(context, "context");
	}

	BatchExecutionContext context()
	{
		return context;
	}

	String opPrefix(final BatchOperation operation)
	{
		return "[batch:" + operation.index() + " line " + operation.lineNumber() + "] ";
	}

	String messageOf(final Exception e)
	{
		return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
	}

	String referenceResolutionMessage(final String reference, final RootReferenceResolver.Resolution resolution)
	{
		if (resolution instanceof RootReferenceResolver.Resolution.Ambiguous ambiguous)
		{
			return "Ambiguous reference: " + reference + " (" + ambiguous.candidates().size() + " matches)";
		}
		if (resolution instanceof RootReferenceResolver.Resolution.NotFound notFound)
		{
			return "Cannot resolve reference: " + notFound.message();
		}
		if (resolution instanceof RootReferenceResolver.Resolution.Failure failure)
		{
			return "Cannot resolve reference: " + failure.message();
		}
		return "Cannot resolve reference: " + reference;
	}

	ParsedModelArgs parseModelArgs(final BatchOperation operation,
							 final BatchOptions options,
							 final int argsWithModel,
							 final int argsWithDefaultModel)
	{
		final var provided = operation.args();
		if (provided.size() == argsWithModel)
		{
			return new ParsedModelArgs(provided.getFirst(), provided.subList(1, provided.size()));
		}

		final var defaultModel = options.defaultModel();
		if (defaultModel != null && !defaultModel.isBlank() && provided.size() == argsWithDefaultModel)
		{
			return new ParsedModelArgs(defaultModel, provided);
		}

		return null;
	}

	BatchOperationResult invalidArgs(final BatchOperation operation,
						 final BatchOptions options,
						 final String command,
						 final String usageTail)
	{
		final var err = context.err();
		err.println(opPrefix(operation) + "Invalid args for '" + command + "'");
		err.println(opPrefix(operation) + "Expected: " + usageTail);
		if (options.defaultModel() != null && !options.defaultModel().isBlank())
		{
			err.println(opPrefix(operation) + "Tip: args may omit <model.lm> because --default-model is set to " + options.defaultModel());
		}
		err.println(opPrefix(operation) + "Raw: " + operation.rawLine());
		return result(operation,
					  ExitCodes.USAGE,
					  false,
					  "error",
					  "Invalid args for '" + command + "' (expected: " + usageTail + ")",
					  List.of(),
					  List.of());
	}

	BatchOperationResult result(final BatchOperation operation,
						   final int exitCode,
						   final boolean validationFailed,
						   final String status,
						   final String message,
						   final List<Path> stagedFiles,
						   final List<RemoveUnsetReference> unsets)
	{
		return result(operation,
					  exitCode,
					  validationFailed,
					  status,
					  message,
					  stagedFiles,
					  unsets,
					  List.of());
	}

	BatchOperationResult result(final BatchOperation operation,
						   final int exitCode,
						   final boolean validationFailed,
						   final String status,
						   final String message,
						   final List<Path> stagedFiles,
						   final List<RemoveUnsetReference> unsets,
						   final List<ReferenceRewrite> rewrites)
	{
		return result(operation,
					  exitCode,
					  validationFailed,
					  status,
					  message,
					  stagedFiles,
					  List.of(),
					  unsets,
					  rewrites);
	}

	BatchOperationResult result(final BatchOperation operation,
						   final int exitCode,
						   final boolean validationFailed,
						   final String status,
						   final String message,
						   final List<Path> stagedFiles,
						   final List<DiagnosticItem> diagnostics,
						   final List<RemoveUnsetReference> unsets,
						   final List<ReferenceRewrite> rewrites)
	{
		final var report = new BatchOperationReport(operation.index(),
											  operation.lineNumber(),
											  operation.command(),
											  List.copyOf(operation.args()),
											  status,
											  message == null ? "" : message,
											  exitCode,
											  validationFailed,
											  stagedFiles,
											  diagnostics,
											  unsets,
											  rewrites);
		return new BatchOperationResult(exitCode, validationFailed, report);
	}

	Path resolveModelPath(final BatchOperation operation, final String modelSpec)
	{
		final var cliContext = context.cliContext();
		final var locator = new ModelLocator(cliContext.projectRoot());
		final var resolution = locator.resolve(modelSpec);

		if (resolution instanceof ModelResolution.Found found)
		{
			return found.path();
		}
		if (resolution instanceof ModelResolution.Ambiguous ambiguous)
		{
			final var err = cliContext.err();
			err.println(opPrefix(operation) + "Ambiguous model reference: " + modelSpec);
			for (final var path : ambiguous.matches())
			{
				err.println(opPrefix(operation) + " - " + PathDisplay.display(cliContext.projectRoot(), path));
			}
			return null;
		}
		if (resolution instanceof ModelResolution.NotFound notFound)
		{
			final var err = cliContext.err();
			err.println(opPrefix(operation) + "Model not found: " + notFound.requested());
			err.println(opPrefix(operation) + "Searched under: " + cliContext.projectRoot());
			return null;
		}
		if (resolution instanceof ModelResolution.Failed failed)
		{
			final var err = cliContext.err();
			err.println(opPrefix(operation) + "Failed to search for model: " + failed.message());
			return null;
		}

		cliContext.err().println(opPrefix(operation) + "Unexpected model resolution state");
		return null;
	}

	WorkspaceLoad loadWorkspace(final BatchOperation operation,
					  final Path modelPath,
					  final boolean requireValidImports)
	{
		final var projectRoot = context.projectRoot();
		final var displayPath = PathDisplay.display(projectRoot, modelPath);
		final var err = context.err();

		final var prepareResult = context.registryService().prepareForModelAndImporters(modelPath, err, requireValidImports);
		if (prepareResult instanceof RegistryService.PrepareWorkspaceResult.Failure failure)
		{
			err.println(opPrefix(operation) + "Cannot prepare workspace (exit " + failure.exitCode() + ")");
			return null;
		}

		final var prepared = ((RegistryService.PrepareWorkspaceResult.Success) prepareResult).workspace();
		final var registry = prepared.registry();
		final var documents = context.documentsLoader().load(context.documentLoader(),
									 registry,
									 prepared,
									 projectRoot,
									 err,
									 displayPath);
		if (documents == null || documents.targetDocument() == null)
		{
			err.println(opPrefix(operation) + "Cannot load workspace documents for " + displayPath);
			return null;
		}

		return new WorkspaceLoad(prepared, documents);
	}

	StageValidation validateWorkspaceAfterStage(final BatchOperation operation,
									final RegistryService.PreparedWorkspace prepared,
									final Map<Path, String> formattedSources)
	{
		final var options = context.options();
		if (options.validateMode() == BatchOptions.ValidateMode.FINAL)
		{
			final var parseability = validateParseable(formattedSources);
			if (!parseability.ok())
			{
				return new StageValidation(false, false, parseability);
			}
		}

		if (options.validateMode() != BatchOptions.ValidateMode.EACH)
		{
			return new StageValidation(true, true, ValidationReport.success());
		}

		final var report = context.validator().validateWithReport(prepared,
											  context.projectRoot(),
											  context.overlaySources(),
											  context.documentLoader(),
											  context.err());
		final boolean ok = report.ok();
		if (!ok && options.force())
		{
			context.err().println(opPrefix(operation) + "FORCED: staged changes despite validation errors");
		}
		return new StageValidation(true, ok, report);
	}

	ValidationReport validateParseable(final Map<Path, String> sourcesByPath)
	{
		if (sourcesByPath == null || sourcesByPath.isEmpty())
		{
			return ValidationReport.success();
		}

		for (final var entry : sourcesByPath.entrySet())
		{
			final var diagnostics = new ArrayList<LmDiagnostic>();
			new LmTreeReader().read(entry.getValue() == null ? "" : entry.getValue(), diagnostics);
			if (DiagnosticReporter.hasErrors(diagnostics))
			{
				final var displayPath = PathDisplay.display(context.projectRoot(), entry.getKey());
				DiagnosticReporter.printDiagnostics(context.err(), displayPath, diagnostics);
				final var items = diagnostics.stream()
										 .map(diagnostic -> new DiagnosticItem(displayPath, diagnostic))
										 .toList();
				return new ValidationReport(false, items, List.of());
			}
		}

		return ValidationReport.success();
	}

	Map<Path, String> formatUpdatedSources(final Map<Path, String> updatedSources)
	{
		if (updatedSources.isEmpty())
		{
			return Map.of();
		}
		return sourceFormatter.formatAll(updatedSources);
	}

	SubtreeFormatResult formatSingleRootSubtree(final String subtreeSource,
									final PrintWriter err,
									final BatchOperation operation)
	{
		final var diagnostics = new ArrayList<LmDiagnostic>();
		final var reader = new LmTreeReader();
		final var readResult = reader.read(subtreeSource, diagnostics);

		if (DiagnosticReporter.hasErrors(diagnostics))
		{
			final var message = "Subtree cannot be parsed";
			err.println(opPrefix(operation) + message);
			DiagnosticReporter.printDiagnostics(err, "<subtree>", diagnostics);
			err.println(opPrefix(operation) + "Hint: If you passed a subtree inline, prefer --subtree-stdin/--subtree-file to avoid shell quoting issues.");
			final var items = diagnostics.stream()
									 .map(diagnostic -> new DiagnosticItem("<subtree>", diagnostic))
									 .toList();
			return new SubtreeFormatResult.Failure(message, items);
		}

		if (readResult.roots().size() != 1)
		{
			final var message = "Subtree must contain exactly one root element; found: " + readResult.roots().size();
			err.println(opPrefix(operation) + message);
			return new SubtreeFormatResult.Failure(message, List.of());
		}

		return new SubtreeFormatResult.Success(new LmFormatter().format(readResult.roots()));
	}

	record ParsedModelArgs(String modelSpec, List<String> args)
	{
	}

	record WorkspaceLoad(RegistryService.PreparedWorkspace prepared,
						WorkspaceDocuments documents)
	{
	}

	record StageValidation(boolean parseable, boolean validationPassed, ValidationReport report)
	{
	}

	sealed interface SubtreeFormatResult permits SubtreeFormatResult.Success, SubtreeFormatResult.Failure
	{
		record Success(String formatted) implements SubtreeFormatResult
		{
		}

		record Failure(String message, List<DiagnosticItem> diagnostics) implements SubtreeFormatResult
		{
			public Failure
			{
				Objects.requireNonNull(message, "message");
				diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics);
			}
		}
	}

	record OverlayUpdate(Map<Path, String> previousByPath)
	{
		static OverlayUpdate apply(final Map<Path, String> overlaySources, final Map<Path, String> updatedSources)
		{
			final var previous = new LinkedHashMap<Path, String>();
			for (final var entry : updatedSources.entrySet())
			{
				final var normalized = entry.getKey().toAbsolutePath().normalize();
				previous.put(normalized, overlaySources.get(normalized));
				overlaySources.put(normalized, entry.getValue() == null ? "" : entry.getValue());
			}
			return new OverlayUpdate(java.util.Collections.unmodifiableMap(new LinkedHashMap<>(previous)));
		}

		void rollback(final Map<Path, String> overlaySources)
		{
			for (final var entry : previousByPath.entrySet())
			{
				if (entry.getValue() == null)
				{
					overlaySources.remove(entry.getKey());
				}
				else
				{
					overlaySources.put(entry.getKey(), entry.getValue());
				}
			}
		}
	}
}
