package org.logoce.lmf.cli.replace;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.ExitCodes;
import org.logoce.lmf.cli.diagnostics.DiagnosticReporter;
import org.logoce.lmf.cli.edit.EditOptions;
import org.logoce.lmf.cli.edit.EditValidationContext;
import org.logoce.lmf.cli.edit.SubtreeSpanLocator;
import org.logoce.lmf.cli.edit.TextEdits;
import org.logoce.lmf.cli.edit.WorkspaceEditPipeline;
import org.logoce.lmf.cli.format.LmFormatter;
import org.logoce.lmf.cli.format.RootReferenceResolver;
import org.logoce.lmf.cli.util.PathDisplay;
import org.logoce.lmf.cli.workspace.DocumentLoader;
import org.logoce.lmf.cli.workspace.ModelLocator;
import org.logoce.lmf.cli.workspace.ModelResolution;
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
	public record Options(boolean force)
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

		final var locator = new ModelLocator(context.projectRoot());
		final var resolution = locator.resolve(modelSpec);

		if (resolution instanceof ModelResolution.Found found)
		{
			return replaceInModel(found.path(), context, targetReference, replacementSubtree, options);
		}
		if (resolution instanceof ModelResolution.Ambiguous ambiguous)
		{
			final var err = context.err();
			err.println("Ambiguous model reference: " + modelSpec);
			for (final var path : ambiguous.matches())
			{
				err.println(" - " + PathDisplay.display(context.projectRoot(), path));
			}
			return ExitCodes.USAGE;
		}
		if (resolution instanceof ModelResolution.NotFound notFound)
		{
			final var err = context.err();
			err.println("Model not found: " + notFound.requested());
			err.println("Searched under: " + context.projectRoot());
			return ExitCodes.USAGE;
		}
		if (resolution instanceof ModelResolution.Failed failed)
		{
			final var err = context.err();
			err.println("Failed to search for model: " + failed.message());
			return ExitCodes.USAGE;
		}

		context.err().println("Unexpected model resolution state");
		return ExitCodes.USAGE;
	}

	private int replaceInModel(final Path modelPath,
							   final CliContext context,
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
			context.err().println("No changes written to " + displayPath);
			return failure.exitCode();
		}

		final var prepared = ((RegistryService.PrepareResult.Success) prepareResult).registry();
		final var registry = prepared.registry();
		final var targetQualifiedName = prepared.targetQualifiedName();

		final var originalSource = documentLoader.readString(modelPath, context.err());
		if (originalSource == null)
		{
			context.err().println("No changes written to " + displayPath);
			return ExitCodes.INVALID;
		}

		final var originalDocument = documentLoader.loadModelFromSource(registry,
																		targetQualifiedName,
																		originalSource,
																		context.err());
		final var linkRoots = RootReferenceResolver.collectLinkRoots(originalDocument.linkTrees());
		if (linkRoots.isEmpty())
		{
			context.err().println("No link trees available for " + displayPath);
			context.err().println("No changes written to " + displayPath);
			return ExitCodes.INVALID;
		}

		final var resolution = new RootReferenceResolver().resolve(linkRoots, targetReference);
		if (resolution instanceof RootReferenceResolver.Resolution.Found found)
		{
			return replaceSpan(context,
							   modelPath,
							   displayPath,
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
			context.err().println("No changes written to " + displayPath);
			return ExitCodes.USAGE;
		}
		if (resolution instanceof RootReferenceResolver.Resolution.NotFound notFound)
		{
			context.err().println("Cannot resolve reference: " + targetReference);
			context.err().println(notFound.message());
			context.err().println("No changes written to " + displayPath);
			return ExitCodes.USAGE;
		}
		if (resolution instanceof RootReferenceResolver.Resolution.Failure failure)
		{
			context.err().println("Cannot resolve reference: " + targetReference);
			context.err().println(failure.message());
			context.err().println("No changes written to " + displayPath);
			return ExitCodes.USAGE;
		}

		context.err().println("Unexpected reference resolution state");
		context.err().println("No changes written to " + displayPath);
		return ExitCodes.USAGE;
	}

	private int replaceSpan(final CliContext context,
							final Path modelPath,
							final String displayPath,
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
			context.err().println("Cannot locate subtree span for reference: " + targetReference);
			context.err().println("No changes written to " + displayPath);
			return ExitCodes.INVALID;
		}

		final var formattedReplacement = formatSingleRootSubtree(replacementSubtree, context.err());
		if (formattedReplacement == null)
		{
			context.err().println("No changes written to " + displayPath);
			return ExitCodes.USAGE;
		}

		final var baseIndent = trailingIndentBefore(originalSource, span.startOffset());
		final var indentedReplacement = baseIndent.isEmpty()
										? formattedReplacement
										: formattedReplacement.replace("\n", "\n" + baseIndent);

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
			context.err().println("No changes written to " + displayPath);
			return ExitCodes.INVALID;
		}

		if (!outcome.validationPassed())
		{
			context.err().println("FORCED: wrote changes to " + displayPath + " despite errors");
			return ExitCodes.INVALID;
		}

		context.out().println("OK: replaced " + targetReference + " in " + displayPath);
		return ExitCodes.OK;
	}

	private static String formatSingleRootSubtree(final String subtreeSource, final java.io.PrintWriter err)
	{
		final var diagnostics = new ArrayList<LmDiagnostic>();
		final var reader = new LmTreeReader();
		final var readResult = reader.read(subtreeSource, diagnostics);
		if (DiagnosticReporter.hasErrors(diagnostics))
		{
			DiagnosticReporter.printDiagnostics(err, "<subtree>", diagnostics);
			err.println("Replacement subtree cannot be parsed");
			return null;
		}

		if (readResult.roots().size() != 1)
		{
			err.println("Replacement subtree must contain exactly one root element; found: " + readResult.roots().size());
			return null;
		}

		final var formatter = new LmFormatter();
		return formatter.format(readResult.roots());
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
