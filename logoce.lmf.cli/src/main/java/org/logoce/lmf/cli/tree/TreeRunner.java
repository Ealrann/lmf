package org.logoce.lmf.cli.tree;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.ExitCodes;
import org.logoce.lmf.cli.diagnostics.DiagnosticReporter;
import org.logoce.lmf.cli.format.RootReferenceResolver;
import org.logoce.lmf.cli.json.JsonErrorWriter;
import org.logoce.lmf.cli.json.JsonWriter;
import org.logoce.lmf.cli.util.PathDisplay;
import org.logoce.lmf.cli.workspace.ModelSpecResolver;
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
	public record Options(int maxDepth, String rootReference, boolean alwaysIndex, boolean syntaxOnly, boolean json)
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

		final var resolved = ModelSpecResolver.resolve(context, modelSpec, "tree", options.json());
		if (!resolved.ok())
		{
			return resolved.exitCode();
		}
		return listTree(modelSpec, resolved.path(), context, options);
	}

	private int listTree(final String requestedModel, final Path path, final CliContext context, final Options options)
	{
		if (options.syntaxOnly())
		{
			return listSyntaxTree(requestedModel, path, context, options);
		}

		final var displayPath = PathDisplay.display(context.projectRoot(), path);
		final var documentLoader = new DocumentLoader();
		final var source = documentLoader.readString(path, context.err());
		if (source == null)
		{
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "tree", ExitCodes.INVALID, "Failed to read model file: " + displayPath);
			}
			return ExitCodes.INVALID;
		}

		final var parseDiagnostics = parseDiagnostics(source);
		if (DiagnosticReporter.hasErrors(parseDiagnostics))
		{
			DiagnosticReporter.printDiagnostics(context.err(), displayPath, parseDiagnostics);
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "tree", ExitCodes.INVALID, "Model has syntax errors: " + displayPath);
			}
			return ExitCodes.INVALID;
		}

		final var registryService = new RegistryService(context.projectRoot(), documentLoader);
		final var prepareResult = registryService.prepareForModel(path, context.err());
		if (prepareResult instanceof RegistryService.PrepareResult.Failure)
		{
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "tree", ExitCodes.INVALID, "Cannot prepare model registry for " + displayPath);
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
				JsonErrorWriter.writeError(context, "tree", ExitCodes.INVALID, "Model has linking errors: " + displayPath);
			}
			return ExitCodes.INVALID;
		}

		final var linkRoots = RootReferenceResolver.collectLinkRoots(document.linkTrees());
		if (linkRoots.isEmpty())
		{
			context.err().println("No link trees available for " + displayPath);
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "tree", ExitCodes.INVALID, "No link trees available for " + displayPath);
			}
			return ExitCodes.INVALID;
		}

		final var lister = new TreeLister();
		final var lines = new ArrayList<TreeLister.Line>();

		if (options.rootReference() == null)
		{
			for (final var linkRoot : linkRoots)
			{
				lines.addAll(lister.list(linkRoot, options.maxDepth(), options.alwaysIndex()));
			}
			return emitResult(context, requestedModel, displayPath, options, lines);
		}

		final var resolution = new RootReferenceResolver().resolve(linkRoots, options.rootReference());
		if (resolution instanceof RootReferenceResolver.Resolution.Found found)
		{
			final var basePath = absolutePath(found.node());
			final var relative = lister.list(found.node(), options.maxDepth(), options.alwaysIndex());
			for (final var line : relative)
			{
				lines.add(new TreeLister.Line(fullPath(basePath, line.path()), line.groupName(), line.name()));
			}
			return emitResult(context, requestedModel, displayPath, options, lines);
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
										   "tree",
										   ExitCodes.USAGE,
										   "Ambiguous --root reference: " + options.rootReference(),
										   ambiguous.candidates());
			}
			return ExitCodes.USAGE;
		}
		if (resolution instanceof RootReferenceResolver.Resolution.NotFound notFound)
		{
			context.err().println("Cannot resolve --root reference: " + options.rootReference());
			context.err().println(notFound.message());
			if (options.json())
			{
				JsonErrorWriter.writeError(context,
										   "tree",
										   ExitCodes.USAGE,
										   "Cannot resolve --root reference: " + options.rootReference(),
										   List.of(notFound.message()));
			}
			return ExitCodes.USAGE;
		}
		if (resolution instanceof RootReferenceResolver.Resolution.Failure failure)
		{
			context.err().println("Cannot resolve --root reference: " + options.rootReference());
			context.err().println(failure.message());
			if (options.json())
			{
				JsonErrorWriter.writeError(context,
										   "tree",
										   ExitCodes.USAGE,
										   "Cannot resolve --root reference: " + options.rootReference(),
										   List.of(failure.message()));
			}
			return ExitCodes.USAGE;
		}

		context.err().println("Unexpected root resolution state");
		if (options.json())
		{
			JsonErrorWriter.writeError(context, "tree", ExitCodes.USAGE, "Unexpected root resolution state");
		}
		return ExitCodes.USAGE;
	}

	private int emitResult(final CliContext context,
						   final String requestedModel,
						   final String displayPath,
						   final Options options,
						   final List<TreeLister.Line> lines)
	{
		if (options.json())
		{
			writeJsonResult(context, requestedModel, displayPath, options, lines);
			return ExitCodes.OK;
		}

		final var builder = new StringBuilder();
		for (final var line : lines)
		{
			builder.append(line.format()).append('\n');
		}
		context.out().print(builder.toString());
		context.out().flush();
		return ExitCodes.OK;
	}

	private static void writeJsonResult(final CliContext context,
										final String requestedModel,
										final String displayPath,
										final Options options,
										final List<TreeLister.Line> lines)
	{
		final var json = new JsonWriter(context.out());
		json.beginObject()
			.name("command").value("tree")
			.name("projectRoot").value(context.projectRoot().toString())
			.name("model").beginObject()
			.name("requested").value(requestedModel)
			.name("path").value(displayPath)
			.endObject()
			.name("options").beginObject()
			.name("root").value(options.rootReference())
			.name("maxDepth").value(options.maxDepth())
			.name("alwaysIndex").value(options.alwaysIndex())
			.name("syntaxOnly").value(options.syntaxOnly())
			.endObject()
			.name("count").value(lines.size())
			.name("items").beginArray();
		for (final var line : lines)
		{
			json.beginObject()
				.name("path").value(line.path())
				.name("group").value(line.groupName())
				.name("name").value(line.name())
				.endObject();
		}
		json.endArray()
			.name("ok").value(true)
			.name("exitCode").value(ExitCodes.OK)
			.endObject()
			.flush();
		context.out().println();
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

	private int listSyntaxTree(final String requestedModel, final Path path, final CliContext context, final Options options)
	{
		if (options.rootReference() != null)
		{
			context.err().println("Cannot use --root with --syntax-only (requires semantic linking)");
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "tree", ExitCodes.USAGE, "Cannot use --root with --syntax-only (requires semantic linking)");
			}
			return ExitCodes.USAGE;
		}

		final var displayPath = PathDisplay.display(context.projectRoot(), path);
		final var documentLoader = new DocumentLoader();
		final var source = documentLoader.readString(path, context.err());
		if (source == null)
		{
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "tree", ExitCodes.INVALID, "Failed to read model file: " + displayPath);
			}
			return ExitCodes.INVALID;
		}

		final var diagnostics = new ArrayList<org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic>();
		final var reader = new org.logoce.lmf.core.loader.api.loader.parsing.LmTreeReader();
		final var readResult = reader.read(source, diagnostics);
		if (DiagnosticReporter.hasErrors(diagnostics))
		{
			DiagnosticReporter.printDiagnostics(context.err(), displayPath, diagnostics);
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "tree", ExitCodes.INVALID, "Model has syntax errors: " + displayPath);
			}
			return ExitCodes.INVALID;
		}

		final var lister = new SyntaxTreeLister();
		final var lines = lister.list(readResult.roots(), options.maxDepth());
		return emitResult(context, requestedModel, displayPath, options, lines);
	}
}
