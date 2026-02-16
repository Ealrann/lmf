package org.logoce.lmf.cli.batch;

import org.logoce.lmf.cli.ExitCodes;
import org.logoce.lmf.cli.diagnostics.DiagnosticItem;
import org.logoce.lmf.cli.diagnostics.DiagnosticReporter;
import org.logoce.lmf.cli.edit.SubtreeSpanLocator;
import org.logoce.lmf.cli.edit.TextEdits;
import org.logoce.lmf.cli.format.LmSourceFormatter;
import org.logoce.lmf.cli.format.RootReferenceResolver;
import org.logoce.lmf.cli.util.PathDisplay;
import org.logoce.lmf.cli.workspace.RegistryService;

import java.util.List;
import java.util.Objects;

final class BatchReplaceHandler implements BatchOperationHandler
{
	private final BatchOperationSupport support;
	private final LmSourceFormatter sourceFormatter = new LmSourceFormatter();

	BatchReplaceHandler(final BatchOperationSupport support)
	{
		this.support = Objects.requireNonNull(support, "support");
	}

	@Override
	public BatchOperationResult execute(final BatchOperation operation)
	{
		final var context = support.context();
		final var options = context.options();
		final var parsed = support.parseModelArgs(operation, options, 3, 2);
		if (parsed == null)
		{
			return support.invalidArgs(operation, options, "replace", "<model.lm> <ref> <subtree>");
		}

		final var modelSpec = parsed.modelSpec();
		final var targetReference = parsed.args().get(0);
		final var replacementSubtree = parsed.args().get(1);

		final var modelPath = support.resolveModelPath(operation, modelSpec);
		if (modelPath == null)
		{
			return support.result(operation,
							  ExitCodes.USAGE,
							  false,
							  "error",
							  "Cannot resolve model: " + modelSpec,
							  List.of(),
							  List.of());
		}

		final var prepareResult = context.registryService().prepareForModel(modelPath, context.err());
		if (prepareResult instanceof RegistryService.PrepareResult.Failure)
		{
			context.err().println(support.opPrefix(operation) + "Cannot prepare model registry for replace");
			return support.result(operation,
							  ExitCodes.INVALID,
							  false,
							  "error",
							  "Cannot prepare model registry for replace",
							  List.of(),
							  List.of());
		}

		final var prepared = ((RegistryService.PrepareResult.Success) prepareResult).registry();
		final var registry = prepared.registry();
		final var targetQualifiedName = prepared.targetQualifiedName();

		final var originalSource = context.documentLoader().readString(modelPath, context.err());
		if (originalSource == null)
		{
			return support.result(operation,
							  ExitCodes.INVALID,
							  false,
							  "error",
							  "Failed to read model file",
							  List.of(),
							  List.of());
		}

		final var originalDocument = context.documentLoader().loadModelFromSource(registry,
												  targetQualifiedName,
												  originalSource,
												  context.err());
		final var linkRoots = RootReferenceResolver.collectLinkRoots(originalDocument.linkTrees());
		if (linkRoots.isEmpty())
		{
			context.err().println(support.opPrefix(operation) + "No link trees available for " + PathDisplay.display(context.projectRoot(), modelPath));
			return support.result(operation,
							  ExitCodes.INVALID,
							  false,
							  "error",
							  "No link trees available for target document",
							  List.of(),
							  List.of());
		}

		final var resolution = new RootReferenceResolver().resolve(linkRoots, targetReference);
		if (resolution instanceof RootReferenceResolver.Resolution.Ambiguous ambiguous)
		{
			context.err().println(support.opPrefix(operation) + "Ambiguous reference: " + targetReference);
			for (final var candidate : ambiguous.candidates())
			{
				context.err().println(support.opPrefix(operation) + " - " + candidate);
			}
			return support.result(operation,
							  ExitCodes.USAGE,
							  false,
							  "error",
							  "Ambiguous reference: " + targetReference,
							  List.of(),
							  List.of());
		}
		if (resolution instanceof RootReferenceResolver.Resolution.NotFound notFound)
		{
			context.err().println(support.opPrefix(operation) + "Cannot resolve reference: " + targetReference);
			context.err().println(support.opPrefix(operation) + notFound.message());
			return support.result(operation,
							  ExitCodes.USAGE,
							  false,
							  "error",
							  "Cannot resolve reference: " + targetReference,
							  List.of(),
							  List.of());
		}
		if (resolution instanceof RootReferenceResolver.Resolution.Failure failure)
		{
			context.err().println(support.opPrefix(operation) + "Cannot resolve reference: " + targetReference);
			context.err().println(support.opPrefix(operation) + failure.message());
			return support.result(operation,
							  ExitCodes.USAGE,
							  false,
							  "error",
							  "Cannot resolve reference: " + targetReference,
							  List.of(),
							  List.of());
		}
		if (!(resolution instanceof RootReferenceResolver.Resolution.Found found))
		{
			context.err().println(support.opPrefix(operation) + "Unexpected reference resolution state");
			return support.result(operation,
							  ExitCodes.INVALID,
							  false,
							  "error",
							  "Unexpected reference resolution state",
							  List.of(),
							  List.of());
		}

		final var span = SubtreeSpanLocator.locate(originalSource, found.node());
		if (span == null)
		{
			context.err().println(support.opPrefix(operation) + "Cannot locate subtree span for reference: " + targetReference);
			return support.result(operation,
							  ExitCodes.INVALID,
							  false,
							  "error",
							  "Cannot locate subtree span for reference: " + targetReference,
							  List.of(),
							  List.of());
		}

		final var formattedReplacementResult = support.formatSingleRootSubtree(replacementSubtree, context.err(), operation);
		if (formattedReplacementResult instanceof BatchOperationSupport.SubtreeFormatResult.Failure failure)
		{
			return support.result(operation,
							  ExitCodes.USAGE,
							  false,
							  "error",
							  failure.message(),
							  List.of(),
							  failure.diagnostics(),
							  List.of(),
							  List.of());
		}
		final var formattedReplacement = ((BatchOperationSupport.SubtreeFormatResult.Success) formattedReplacementResult).formatted();

		final var baseIndent = trailingIndentBefore(originalSource, span.startOffset());
		final var indentedReplacement = baseIndent.isEmpty()
									? formattedReplacement
									: formattedReplacement.replace("\n", "\n" + baseIndent);

		final var updatedSource = TextEdits.apply(originalSource,
									  List.of(new TextEdits.TextEdit(span.startOffset(),
																   span.length(),
																   indentedReplacement)));
		final var formattedSource = sourceFormatter.formatOrOriginal(updatedSource);

		final var validatedDocument = context.documentLoader().loadModelFromSource(registry,
												  targetQualifiedName,
												  formattedSource,
												  context.err());
		final var diagnostics = validatedDocument.diagnostics();
		final var diagnosticItems = DiagnosticReporter.hasErrors(diagnostics)
										? diagnostics.stream()
													 .map(diagnostic -> new DiagnosticItem(PathDisplay.display(context.projectRoot(), modelPath), diagnostic))
													 .toList()
										: List.<DiagnosticItem>of();
		if (DiagnosticReporter.hasErrors(diagnostics))
		{
			DiagnosticReporter.printDiagnostics(context.err(),
									 PathDisplay.display(context.projectRoot(), modelPath),
									 diagnostics);
			if (!options.force())
			{
				context.err().println(support.opPrefix(operation) + "Validation failed; operation not staged");
				return support.result(operation,
									  ExitCodes.INVALID,
									  false,
									  "error",
									  "Validation failed; operation not staged",
									  List.of(),
									  diagnosticItems,
									  List.of(),
									  List.of());
			}
		}

		final var overlayUpdate = BatchOperationSupport.OverlayUpdate.apply(context.overlaySources(),
													  java.util.Map.of(modelPath.toAbsolutePath().normalize(), formattedSource));
		final boolean validated = !DiagnosticReporter.hasErrors(diagnostics);
		if (!validated && options.validateMode() == BatchOptions.ValidateMode.EACH && !options.force())
		{
			overlayUpdate.rollback(context.overlaySources());
			context.err().println(support.opPrefix(operation) + "Validation failed; operation rolled back");
			return support.result(operation,
							  ExitCodes.INVALID,
							  false,
							  "error",
							  "Validation failed; operation rolled back",
							  List.of(),
							  diagnosticItems,
							  List.of(),
							  List.of());
		}

		context.validationRoots().add(new BatchValidationRoot(modelPath, false));
		context.out().println(support.opPrefix(operation) + "STAGED: replaced " + targetReference);
		return support.result(operation,
						  ExitCodes.OK,
						  !validated,
						  "staged",
						  "Replaced " + targetReference,
						  List.of(modelPath.toAbsolutePath().normalize()),
						  diagnosticItems,
						  List.of(),
						  List.of());
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
