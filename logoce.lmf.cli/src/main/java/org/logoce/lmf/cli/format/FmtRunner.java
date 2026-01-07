package org.logoce.lmf.cli.format;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.ExitCodes;
import org.logoce.lmf.cli.diagnostics.DiagnosticReporter;
import org.logoce.lmf.cli.util.PathDisplay;
import org.logoce.lmf.cli.workspace.ModelLocator;
import org.logoce.lmf.cli.workspace.ModelResolution;
import org.logoce.lmf.cli.workspace.DocumentLoader;
import org.logoce.lmf.cli.workspace.RegistryService;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class FmtRunner
{
	public record Options(String rootReference, boolean refPathToName)
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

		final var locator = new ModelLocator(context.projectRoot());
		final var resolution = locator.resolve(modelSpec);

		if (resolution instanceof ModelResolution.Found found)
		{
			return formatModel(found.path(), context, options);
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

	private int formatModel(final Path path, final CliContext context, final Options options)
	{
		final var displayPath = PathDisplay.display(context.projectRoot(), path);
		final var documentLoader = new DocumentLoader();
		final var source = documentLoader.readString(path, context.err());
		if (source == null)
		{
			return ExitCodes.INVALID;
		}

		final var parseDiagnostics = parseDiagnostics(source);
		if (DiagnosticReporter.hasErrors(parseDiagnostics))
		{
			DiagnosticReporter.printDiagnostics(context.err(), displayPath, parseDiagnostics);
			return ExitCodes.INVALID;
		}

		final var registryService = new RegistryService(context.projectRoot(), documentLoader);
		final var prepareResult = registryService.prepareForModel(path, context.err());
		if (prepareResult instanceof RegistryService.PrepareResult.Failure)
		{
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
			return ExitCodes.INVALID;
		}

		final var roots = document.roots();
		if (roots.isEmpty())
		{
			context.err().println("No syntax roots found in " + displayPath);
			return ExitCodes.INVALID;
		}

		if (options.rootReference() == null)
		{
			return emitFormattedRoots(context, document, roots, options);
		}

		return emitFormattedRootReference(context, document, displayPath, options);
	}

	private int emitFormattedRoots(final CliContext context,
								   final org.logoce.lmf.core.loader.api.loader.model.LmDocument document,
								   final List<org.logoce.lmf.core.util.tree.Tree<PNode>> roots,
								   final Options options)
	{
		final var formatter = new LmFormatter();
		final String formatted;

		if (!options.refPathToName())
		{
			formatted = formatter.format(roots);
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
				formatted = builder.toString();
			}
			else
			{
				formatted = formatter.format(roots);
			}
		}

		context.out().print(formatted);
		if (!formatted.endsWith("\n"))
		{
			context.out().print("\n");
		}
		context.out().flush();
		return ExitCodes.OK;
	}

	private int emitFormattedRootReference(final CliContext context,
										   final org.logoce.lmf.core.loader.api.loader.model.LmDocument document,
										   final String displayPath,
										   final Options options)
	{
		final var linkRoots = RootReferenceResolver.collectLinkRoots(document.linkTrees());
		if (linkRoots.isEmpty())
		{
			context.err().println("Unable to resolve --root because no link trees are available for " + displayPath);
			return ExitCodes.INVALID;
		}

		final var resolution = new RootReferenceResolver().resolve(linkRoots, options.rootReference());
		if (resolution instanceof RootReferenceResolver.Resolution.Found found)
		{
			final var formatter = new LmFormatter();
			final var index = options.refPathToName() ? ReferencePathToNameIndex.build(found.node().root()) : null;
			final var formatted = formatter.format(found.node(), index);
			context.out().print(formatted);
			if (!formatted.endsWith("\n"))
			{
				context.out().print("\n");
			}
			context.out().flush();
			return ExitCodes.OK;
		}
		if (resolution instanceof RootReferenceResolver.Resolution.Ambiguous ambiguous)
		{
			context.err().println("Ambiguous --root reference: " + options.rootReference());
			for (final var candidate : ambiguous.candidates())
			{
				context.err().println(" - " + candidate);
			}
			return ExitCodes.USAGE;
		}
		if (resolution instanceof RootReferenceResolver.Resolution.NotFound notFound)
		{
			context.err().println("Cannot resolve --root reference: " + options.rootReference());
			context.err().println(notFound.message());
			return ExitCodes.USAGE;
		}
		if (resolution instanceof RootReferenceResolver.Resolution.Failure failure)
		{
			context.err().println("Cannot resolve --root reference: " + options.rootReference());
			context.err().println(failure.message());
			return ExitCodes.USAGE;
		}

		context.err().println("Unexpected root resolution state");
		return ExitCodes.USAGE;
	}

	private static List<org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic> parseDiagnostics(final CharSequence source)
	{
		final var diagnostics = new ArrayList<org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic>();
		new org.logoce.lmf.core.loader.api.loader.parsing.LmTreeReader().read(source, diagnostics);
		return List.copyOf(diagnostics);
	}
}
