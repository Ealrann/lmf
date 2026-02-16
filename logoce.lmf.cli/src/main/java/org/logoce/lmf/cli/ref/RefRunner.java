package org.logoce.lmf.cli.ref;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.ExitCodes;
import org.logoce.lmf.cli.diagnostics.DiagnosticReporter;
import org.logoce.lmf.cli.format.RootReferenceResolver;
import org.logoce.lmf.cli.json.JsonSerializers;
import org.logoce.lmf.cli.json.JsonWriter;
import org.logoce.lmf.cli.util.PathDisplay;
import org.logoce.lmf.cli.workspace.DocumentLoader;
import org.logoce.lmf.cli.workspace.ModelSpecResolver;
import org.logoce.lmf.cli.workspace.RegistryService;
import org.logoce.lmf.core.api.model.ModelRegistry;
import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.loader.api.loader.LmLoader;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.loader.api.loader.linking.RelationReferences;
import org.logoce.lmf.core.loader.api.loader.linking.tree.LinkNodeInternal;
import org.logoce.lmf.core.loader.api.loader.parsing.LmTreeReader;
import org.logoce.lmf.core.loader.api.loader.util.TextPositions;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;
import org.logoce.lmf.core.loader.api.tooling.workspace.DiskModelHeaderIndex;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class RefRunner
{
	public record Options(boolean includeDescendants, boolean json)
	{
	}

	public int run(final CliContext context,
				   final String modelSpec,
				   final String targetReference,
				   final Options options)
	{
		Objects.requireNonNull(context, "context");
		Objects.requireNonNull(modelSpec, "modelSpec");
		Objects.requireNonNull(targetReference, "targetReference");
		Objects.requireNonNull(options, "options");

		final var resolved = ModelSpecResolver.resolve(context, modelSpec, "ref", options.json());
		if (!resolved.ok())
		{
			return resolved.exitCode();
		}
		return findReferences(resolved.path(), context, targetReference, options);
	}

	private int findReferences(final Path targetModelPath,
							   final CliContext context,
							   final String targetReference,
							   final Options options)
	{
		final var projectRoot = context.projectRoot();
		final var displayTargetPath = PathDisplay.display(projectRoot, targetModelPath);

		final var documentLoader = new DocumentLoader();
		final var registryService = new RegistryService(projectRoot, documentLoader);
		final var prepareResult = registryService.prepareForModelAndImporters(targetModelPath, context.err(), false);
		if (prepareResult instanceof RegistryService.PrepareWorkspaceResult.Failure failure)
		{
			return failure.exitCode();
		}

		final var prepared = ((RegistryService.PrepareWorkspaceResult.Success) prepareResult).workspace();
		final var registry = prepared.registry();
		final var targetHeader = prepared.targetHeader();
		final var scanModelPaths = prepared.scanModelPaths();
		final var headersByQualifiedName = prepared.headersByQualifiedName();

		final var targetId = resolveTargetId(registry, targetHeader, targetModelPath, targetReference, context, displayTargetPath);
		if (targetId == null)
		{
			if (options.json())
			{
				writeJsonError(context, ExitCodes.INVALID, "Cannot resolve target reference: " + targetReference, List.of());
			}
			return ExitCodes.INVALID;
		}

		final var exact = new ArrayList<RefMatch>();
		final var descendants = new ArrayList<RefMatch>();

		for (final var entry : scanModelPaths.entrySet())
		{
			final var modelName = entry.getKey();
			final var path = entry.getValue();
			final var header = headersByQualifiedName.get(modelName);
			if (header == null)
			{
				continue;
			}

			final var doc = loadForAnalysis(registry, header, path, context.err());
			if (doc == null)
			{
				continue;
			}

			final var linkRoots = RootReferenceResolver.collectLinkRoots(doc.linkTrees());
			for (final var root : linkRoots)
			{
				root.streamTree().forEach(node -> scanNode(exact,
														   descendants,
														   path,
														   doc.source(),
														   node,
														   targetId,
														   options));
			}
		}

			final var output = outputMatches(exact, descendants, options);
			if (options.json())
			{
				writeJsonResult(context,
								displayTargetPath,
								targetReference,
								targetId,
								options,
							exact,
							descendants,
							output);
		}
		else
		{
			for (final var match : output)
			{
				context.out().println(formatMatch(projectRoot, match));
			}
			context.out().flush();
		}

		return ExitCodes.OK;
	}

	private static List<RefMatch> outputMatches(final List<RefMatch> exact,
												final List<RefMatch> descendants,
												final Options options)
	{
		if (exact.isEmpty())
		{
			return descendants;
		}
		if (!options.includeDescendants())
		{
			return exact;
		}
		final var out = new ArrayList<RefMatch>(exact.size() + descendants.size());
		out.addAll(exact);
		out.addAll(descendants);
		return List.copyOf(out);
	}

	private static void scanNode(final List<RefMatch> exactOut,
								 final List<RefMatch> descendantOut,
								 final Path documentPath,
								 final CharSequence source,
								 final LinkNodeInternal<?, PNode, ?> node,
								 final ObjectId targetId,
								 final Options options)
	{
		for (final var attempt : node.relationResolutions())
		{
			for (final var resolved : RelationReferences.resolved(attempt))
			{
				final var resolvedId = ObjectId.from(resolved.target());
				if (resolvedId == null)
				{
					continue;
				}

				final boolean exact = resolvedId.equals(targetId);
				final boolean descendant = !exact && isDescendant(targetId, resolvedId) && isPathLikeReference(resolved.raw());
				if (!exact && !descendant)
				{
					continue;
				}

				final var span = resolveValueSpan(resolved.raw(), node, source);

				if (exact)
				{
					exactOut.add(new RefMatch(documentPath, span, resolved.raw(), resolvedId, MatchKind.EXACT));
					continue;
				}

				if (options.includeDescendants() || exactOut.isEmpty())
				{
					descendantOut.add(new RefMatch(documentPath, span, resolved.raw(), resolvedId, MatchKind.DESCENDANT));
				}
			}
		}
	}

	private static ObjectId resolveTargetId(final ModelRegistry fullRegistry,
										   final DiskModelHeaderIndex.DiskModelHeader targetHeader,
										   final Path targetPath,
										   final String targetReference,
										   final CliContext context,
										   final String displayTargetPath)
	{
		final var doc = loadForAnalysis(fullRegistry, targetHeader, targetPath, context.err());
		if (doc == null)
		{
			return null;
		}

		if (DiagnosticReporter.hasErrors(doc.diagnostics()))
		{
			DiagnosticReporter.printDiagnostics(context.err(), displayTargetPath, doc.diagnostics());
			return null;
		}

		final var linkRoots = RootReferenceResolver.collectLinkRoots(doc.linkTrees());
		if (linkRoots.isEmpty())
		{
			context.err().println("No link trees available for " + displayTargetPath);
			return null;
		}

		final var resolution = new RootReferenceResolver().resolve(linkRoots, targetReference);
		if (resolution instanceof RootReferenceResolver.Resolution.Found found)
		{
			final LMObject built;
			try
			{
				built = found.node().build();
			}
			catch (RuntimeException e)
			{
				context.err().println("Failed to build target node for reference: " + targetReference);
				return null;
			}
			return ObjectId.from(built);
		}
		if (resolution instanceof RootReferenceResolver.Resolution.Ambiguous ambiguous)
		{
			context.err().println("Ambiguous reference: " + targetReference);
			for (final var candidate : ambiguous.candidates())
			{
				context.err().println(" - " + candidate);
			}
			return null;
		}
		if (resolution instanceof RootReferenceResolver.Resolution.NotFound notFound)
		{
			context.err().println("Cannot resolve reference: " + targetReference);
			context.err().println(notFound.message());
			return null;
		}
		if (resolution instanceof RootReferenceResolver.Resolution.Failure failure)
		{
			context.err().println("Cannot resolve reference: " + targetReference);
			context.err().println(failure.message());
			return null;
		}

		context.err().println("Unexpected reference resolution state");
		return null;
	}

	private static org.logoce.lmf.core.loader.api.loader.model.LmDocument loadForAnalysis(final ModelRegistry registry,
																						 final DiskModelHeaderIndex.DiskModelHeader header,
																						 final Path modelPath,
																						 final java.io.PrintWriter err)
	{
		final var source = readFile(modelPath, err);
		if (source == null)
		{
			return null;
		}

		final var diagnostics = new ArrayList<LmDiagnostic>();
		final var reader = new LmTreeReader();
		final var readResult = reader.read(source, diagnostics);

		final var builder = new ModelRegistry.Builder(registry);
		if (header != null && header.qualifiedName() != null)
		{
			builder.remove(header.qualifiedName());
		}

		final var loader = new LmLoader(builder.build());
		try
		{
			return loader.loadModel(readResult, diagnostics);
		}
		catch (RuntimeException e)
		{
			final var message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
			err.println("Failed to load model " + modelPath + ": " + message);
			return null;
		}
	}

	private static CharSequence readFile(final Path path, final java.io.PrintWriter err)
	{
		try
		{
			final byte[] bytes = Files.readAllBytes(path);
			return new String(bytes, StandardCharsets.UTF_8);
		}
		catch (IOException e)
		{
			final var message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
			err.println("Failed to read model file: " + path + " (" + message + ")");
			return null;
		}
	}

	private static String formatLocation(final Path projectRoot, final Path path, final TextPositions.Span span)
	{
		final var rel = PathDisplay.display(projectRoot, path);
		if (span == null)
		{
			return rel;
		}
		return rel + ":" + span.line() + ":" + span.column();
	}

	private static TextPositions.Span resolveValueSpan(final String raw, final LinkNodeInternal<?, PNode, ?> node, final CharSequence source)
	{
		if (raw == null || raw.isBlank())
		{
			return TextPositions.spanOf(node.pNode(), source);
		}

		final var tokens = node.pNode().tokens();
		if (tokens == null || tokens.isEmpty())
		{
			return TextPositions.spanOf(node.pNode(), source);
		}

		final int startOffset = tokens.getFirst().offset();
		final var lastToken = tokens.getLast();
		final int endOffset = lastToken.offset() + Math.max(1, lastToken.length());
		if (startOffset < 0 || endOffset > source.length())
		{
			return TextPositions.spanOf(node.pNode(), source);
		}

		final String slice = source.subSequence(startOffset, endOffset).toString();
		final int idx = slice.lastIndexOf(raw);
		if (idx < 0)
		{
			return TextPositions.spanOf(node.pNode(), source);
		}

		int valueOffset = startOffset + idx;
		int prefixOffset = valueOffset;
		while (prefixOffset > startOffset)
		{
			final char c = source.charAt(prefixOffset - 1);
			if (Character.isWhitespace(c) || c == '(' || c == ')' || c == '=')
			{
				break;
			}
			prefixOffset--;
		}

		valueOffset = prefixOffset;
		final int length = Math.max(1, (startOffset + idx + raw.length()) - valueOffset);
		final int line = TextPositions.lineFor(source, valueOffset);
		final int column = TextPositions.columnFor(source, valueOffset);
		return new TextPositions.Span(line, column, length, valueOffset);
	}

	private static boolean isDescendant(final ObjectId ancestor, final ObjectId candidate)
	{
		if (!Objects.equals(ancestor.modelQualifiedName(), candidate.modelQualifiedName()))
		{
			return false;
		}

		final var ancestorPath = ancestor.path();
		final var candidatePath = candidate.path();
		if (ancestorPath.equals("/"))
		{
			return !candidatePath.equals("/");
		}
		if (!candidatePath.startsWith(ancestorPath))
		{
			return false;
		}
		if (candidatePath.length() == ancestorPath.length())
		{
			return false;
		}
		return candidatePath.charAt(ancestorPath.length()) == '/';
	}

	private static boolean isPathLikeReference(final String raw)
	{
		if (raw == null || raw.isBlank())
		{
			return false;
		}

		if (raw.startsWith("@"))
		{
			return raw.contains("/");
		}

		if (raw.startsWith("#"))
		{
			final int at = raw.indexOf('@');
			if (at >= 0)
			{
				return raw.indexOf('/', at) >= 0;
			}
			return raw.contains("/");
		}

		return true;
	}

	private static String formatMatch(final Path projectRoot, final RefMatch match)
	{
		final var location = formatLocation(projectRoot, match.documentPath(), match.span());
		final var resolved = match.resolvedId().modelQualifiedName() + match.resolvedId().path();
		return location + "\t" + (match.rawReference() == null ? "" : match.rawReference()) + "\t" + resolved + "\t" + match.kind().label;
	}

	private static void writeJsonResult(final CliContext context,
										final String displayTargetPath,
										final String targetReference,
										final ObjectId targetId,
										final Options options,
										final List<RefMatch> exactMatches,
										final List<RefMatch> descendantMatches,
										final List<RefMatch> outputMatches)
	{
		final boolean fallbackToDescendants = exactMatches.isEmpty() && !descendantMatches.isEmpty();
		final var json = new JsonWriter(context.out());
		json.beginObject()
			.name("command").value("ref")
			.name("projectRoot").value(context.projectRoot().toString())
			.name("model").beginObject()
			.name("path").value(displayTargetPath)
			.endObject()
			.name("target").beginObject()
			.name("reference").value(targetReference)
			.name("resolved").beginObject()
			.name("modelQualifiedName").value(targetId.modelQualifiedName())
			.name("path").value(targetId.path())
			.endObject()
			.endObject()
			.name("options").beginObject()
			.name("includeDescendants").value(options.includeDescendants())
			.endObject()
			.name("exactCount").value(exactMatches.size())
			.name("descendantCount").value(descendantMatches.size())
			.name("fallbackToDescendants").value(fallbackToDescendants)
			.name("matchCount").value(outputMatches.size())
			.name("matches").beginArray();

		for (final var match : outputMatches)
		{
			json.beginObject()
				.name("file").value(PathDisplay.display(context.projectRoot(), match.documentPath()))
				.name("rawReference").value(match.rawReference())
				.name("kind").value(match.kind().label)
				.name("resolved").beginObject()
				.name("modelQualifiedName").value(match.resolvedId().modelQualifiedName())
				.name("path").value(match.resolvedId().path())
				.endObject();
			if (match.span() != null)
			{
				json.name("span");
				JsonSerializers.writeSpan(json, match.span());
			}
			json.endObject();
		}

		json.endArray()
			.name("ok").value(true)
			.name("exitCode").value(ExitCodes.OK)
			.endObject()
			.flush();
		context.out().println();
	}

	private static void writeJsonError(final CliContext context,
									   final int exitCode,
									   final String message,
									   final List<String> details)
	{
		final var json = new JsonWriter(context.out());
		json.beginObject()
			.name("command").value("ref")
			.name("ok").value(false)
			.name("exitCode").value(exitCode)
			.name("error").beginObject()
			.name("message").value(message)
			.name("details").beginArray();
		for (final var detail : details)
		{
			json.value(detail);
		}
		json.endArray()
			.endObject()
			.endObject()
			.flush();
		context.out().println();
	}

	private record RefMatch(Path documentPath,
							TextPositions.Span span,
							String rawReference,
							ObjectId resolvedId,
							MatchKind kind)
	{
	}

	private enum MatchKind
	{
		EXACT("exact"),
		DESCENDANT("descendant");

		private final String label;

		MatchKind(final String label)
		{
			this.label = label;
		}
	}
}
