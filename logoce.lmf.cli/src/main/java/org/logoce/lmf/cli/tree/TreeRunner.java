package org.logoce.lmf.cli.tree;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.ExitCodes;
import org.logoce.lmf.cli.diagnostics.DiagnosticReporter;
import org.logoce.lmf.cli.format.RootReferenceResolver;
import org.logoce.lmf.cli.util.PathDisplay;
import org.logoce.lmf.cli.workspace.ModelLocator;
import org.logoce.lmf.cli.workspace.ModelResolution;
import org.logoce.lmf.cli.workspace.DocumentLoader;
import org.logoce.lmf.cli.workspace.RegistryService;
import org.logoce.lmf.core.loader.api.loader.linking.tree.LinkNodeInternal;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class TreeRunner
{
	public record Options(int maxDepth, String rootReference)
	{
		public Options
		{
			if (maxDepth < 0)
			{
				throw new IllegalArgumentException("maxDepth must be >= 0");
			}
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
			return listTree(found.path(), context, options);
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

	private int listTree(final Path path, final CliContext context, final Options options)
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

		final var linkRoots = RootReferenceResolver.collectLinkRoots(document.linkTrees());
		if (linkRoots.isEmpty())
		{
			context.err().println("No link trees available for " + displayPath);
			return ExitCodes.INVALID;
		}

		final var lister = new TreeLister();
		final var builder = new StringBuilder();

		if (options.rootReference() == null)
		{
			for (final var linkRoot : linkRoots)
			{
				appendTree(lister, builder, linkRoot, options.maxDepth());
			}
			context.out().print(builder.toString());
			context.out().flush();
			return ExitCodes.OK;
		}

		final var resolution = new RootReferenceResolver().resolve(linkRoots, options.rootReference());
		if (resolution instanceof RootReferenceResolver.Resolution.Found found)
		{
			final var basePath = absolutePath(found.node());
			appendTree(lister, builder, found.node(), options.maxDepth(), basePath);
			context.out().print(builder.toString());
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

	private static void appendTree(final TreeLister lister,
								   final StringBuilder builder,
								   final LinkNodeInternal<?, PNode, ?> root,
								   final int maxDepth)
	{
		final var lines = lister.list(root, maxDepth);
		for (final var line : lines)
		{
			builder.append(line.format()).append('\n');
		}
	}

	private static void appendTree(final TreeLister lister,
								   final StringBuilder builder,
								   final LinkNodeInternal<?, PNode, ?> root,
								   final int maxDepth,
								   final String basePath)
	{
		final var lines = lister.list(root, maxDepth);
		for (final var line : lines)
		{
			final var path = fullPath(basePath, line.path());
			final var safeGroup = line.groupName() == null ? "" : line.groupName();
			final var safeName = line.name() == null ? "" : line.name();
			builder.append(path).append('\t').append(safeGroup).append('\t').append(safeName).append('\n');
		}
	}

	private static String fullPath(final String basePath, final String relativePath)
	{
		if (relativePath == null || relativePath.isBlank())
		{
			return basePath == null ? "" : basePath;
		}
		if (basePath == null || basePath.isBlank() || "/".equals(basePath))
		{
			return relativePath;
		}
		return basePath + relativePath;
	}

	private static String absolutePath(final LinkNodeInternal<?, PNode, ?> node)
	{
		if (node == null)
		{
			return "/";
		}

		final var segments = new ArrayList<String>();
		LinkNodeInternal<?, PNode, ?> cursor = node;

		while (cursor != null && cursor.parent() != null && cursor.containingRelation() != null)
		{
			final var relationName = cursor.containingRelation().name();
			final var siblings = cursor.parent()
									   .streamChildren()
									   .filter(child -> child.containingRelation() != null)
									   .filter(child -> relationName.equals(child.containingRelation().name()))
									   .toList();

			if (siblings.size() > 1)
			{
				int index = -1;
				for (int i = 0; i < siblings.size(); i++)
				{
					if (siblings.get(i) == cursor)
					{
						index = i;
						break;
					}
				}
				segments.add(relationName + "." + Math.max(0, index));
			}
			else
			{
				segments.add(relationName);
			}

			cursor = cursor.parent();
		}

		if (segments.isEmpty())
		{
			return "/";
		}

		java.util.Collections.reverse(segments);
		return "/" + String.join("/", segments);
	}

	private static List<org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic> parseDiagnostics(final CharSequence source)
	{
		final var diagnostics = new ArrayList<org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic>();
		new org.logoce.lmf.core.loader.api.loader.parsing.LmTreeReader().read(source, diagnostics);
		return List.copyOf(diagnostics);
	}
}
