package org.logoce.lmf.cli.features;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.ExitCodes;
import org.logoce.lmf.cli.diagnostics.DiagnosticReporter;
import org.logoce.lmf.cli.format.RootReferenceResolver;
import org.logoce.lmf.cli.json.JsonErrorWriter;
import org.logoce.lmf.cli.json.JsonWriter;
import org.logoce.lmf.cli.util.PathDisplay;
import org.logoce.lmf.cli.workspace.DocumentLoader;
import org.logoce.lmf.cli.workspace.ModelSpecResolver;
import org.logoce.lmf.cli.workspace.RegistryService;
import org.logoce.lmf.core.api.util.ModelUtil;
import org.logoce.lmf.core.lang.Attribute;
import org.logoce.lmf.core.lang.Datatype;
import org.logoce.lmf.core.lang.Feature;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class FeaturesRunner
{
	public record Options(boolean json)
	{
	}

	public int run(final CliContext context,
				   final String modelSpec,
				   final String objectReference,
				   final Options options)
	{
		Objects.requireNonNull(context, "context");
		Objects.requireNonNull(modelSpec, "modelSpec");
		Objects.requireNonNull(objectReference, "objectReference");
		Objects.requireNonNull(options, "options");

		final var resolved = ModelSpecResolver.resolve(context, modelSpec, "features", options.json());
		if (!resolved.ok())
		{
			return resolved.exitCode();
		}

		return listFeatures(modelSpec, resolved.path(), objectReference, context, options);
	}

	private int listFeatures(final String requestedModel,
							 final Path path,
							 final String objectReference,
							 final CliContext context,
							 final Options options)
	{
		final var displayPath = PathDisplay.display(context.projectRoot(), path);
		final var documentLoader = new DocumentLoader();
		final var source = documentLoader.readString(path, context.err());
		if (source == null)
		{
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "features", ExitCodes.INVALID, "Failed to read model file: " + displayPath);
			}
			return ExitCodes.INVALID;
		}

		final var parseDiagnostics = parseDiagnostics(source);
		if (DiagnosticReporter.hasErrors(parseDiagnostics))
		{
			DiagnosticReporter.printDiagnostics(context.err(), displayPath, parseDiagnostics);
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "features", ExitCodes.INVALID, "Model has syntax errors: " + displayPath);
			}
			return ExitCodes.INVALID;
		}

		final var registryService = new RegistryService(context.projectRoot(), documentLoader);
		final var prepareResult = registryService.prepareForModel(path, context.err());
		if (prepareResult instanceof RegistryService.PrepareResult.Failure)
		{
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "features", ExitCodes.INVALID, "Cannot prepare model registry for " + displayPath);
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
				JsonErrorWriter.writeError(context, "features", ExitCodes.INVALID, "Model has linking errors: " + displayPath);
			}
			return ExitCodes.INVALID;
		}

		final var linkRoots = RootReferenceResolver.collectLinkRoots(document.linkTrees());
		if (linkRoots.isEmpty())
		{
			context.err().println("No link trees available for " + displayPath);
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "features", ExitCodes.INVALID, "No link trees available for " + displayPath);
			}
			return ExitCodes.INVALID;
		}

		final var resolution = new RootReferenceResolver().resolve(linkRoots, objectReference);
		if (!(resolution instanceof RootReferenceResolver.Resolution.Found found))
		{
			final var message = referenceResolutionMessage(objectReference, resolution);
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "features", ExitCodes.USAGE, message);
			}
			else
			{
				context.err().println(message);
			}
			return ExitCodes.USAGE;
		}

		final var group = found.node().group();
		if (group == null)
		{
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "features", ExitCodes.INVALID, "Target object has no resolved group");
			}
			else
			{
				context.err().println("Target object has no resolved group");
			}
			return ExitCodes.INVALID;
		}

		final var features = describeFeatures(group);
		if (options.json())
		{
			writeJsonResult(context, requestedModel, displayPath, objectReference, group.name(), features);
			return ExitCodes.OK;
		}

		final var builder = new StringBuilder();
		for (final var feature : features)
		{
			builder.append(feature.format()).append('\n');
		}
		context.out().print(builder.toString());
		context.out().flush();
		return ExitCodes.OK;
	}

	private static String referenceResolutionMessage(final String reference,
													 final RootReferenceResolver.Resolution resolution)
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

	private static List<FeatureLine> describeFeatures(final Group<?> group)
	{
		final var result = new java.util.LinkedHashMap<FeatureKey, FeatureLine>();
		for (final var feature : ModelUtil.streamAllFeatures(group).toList())
		{
			final var line = FeatureLine.from(feature);
			result.putIfAbsent(FeatureKey.from(line, feature.id()), line);
		}
		return List.copyOf(result.values());
	}

	private static void writeJsonResult(final CliContext context,
										final String requestedModel,
										final String displayPath,
										final String objectReference,
										final String groupName,
										final List<FeatureLine> features)
	{
		final var json = new JsonWriter(context.out());
		json.beginObject()
			.name("command").value("features")
			.name("projectRoot").value(context.projectRoot().toString())
			.name("model").beginObject()
			.name("requested").value(requestedModel)
			.name("path").value(displayPath)
			.endObject()
			.name("object").beginObject()
			.name("ref").value(objectReference)
			.name("group").value(groupName)
			.endObject()
			.name("count").value(features.size())
			.name("features").beginArray();
		for (final var feature : features)
		{
			json.beginObject()
				.name("name").value(feature.name())
				.name("kind").value(feature.kind().name())
				.name("cardinality").value(feature.cardinality())
				.name("mandatory").value(feature.mandatory())
				.name("many").value(feature.many())
				.name("immutable").value(feature.immutable())
				.name("type").value(feature.type())
				.endObject();
		}
		json.endArray()
			.name("ok").value(true)
			.name("exitCode").value(ExitCodes.OK)
			.endObject()
			.flush();
		context.out().println();
	}

	private static List<LmDiagnostic> parseDiagnostics(final CharSequence source)
	{
		final var diagnostics = new ArrayList<LmDiagnostic>();
		new org.logoce.lmf.core.loader.api.loader.parsing.LmTreeReader().read(source, diagnostics);
		return List.copyOf(diagnostics);
	}

	private enum FeatureKind
	{
		ATTRIBUTE,
		CONTAINS,
		REFERS
	}

	private record FeatureLine(String name,
							   FeatureKind kind,
							   String cardinality,
							   boolean mandatory,
							   boolean many,
							   boolean immutable,
							   String type)
	{
		static FeatureLine from(final Feature<?, ?, ?, ?> feature)
		{
			final var name = feature.name();
			final var cardinality = cardinality(feature.mandatory(), feature.many());
			final var kind = kind(feature);
			final var type = type(feature);
			return new FeatureLine(name, kind, cardinality, feature.mandatory(), feature.many(), feature.immutable(), type);
		}

		String format()
		{
			return name + "\t" + kind.name().toLowerCase() + "\t" + cardinality + "\t" + type;
		}

		private static FeatureKind kind(final Feature<?, ?, ?, ?> feature)
		{
			if (feature instanceof Relation<?, ?, ?, ?> relation)
			{
				return relation.contains() ? FeatureKind.CONTAINS : FeatureKind.REFERS;
			}
			if (feature instanceof Attribute<?, ?, ?, ?>)
			{
				return FeatureKind.ATTRIBUTE;
			}
			return FeatureKind.ATTRIBUTE;
		}

		private static String type(final Feature<?, ?, ?, ?> feature)
		{
			if (feature instanceof Relation<?, ?, ?, ?> relation)
			{
				final var concept = relation.concept();
				return concept == null ? "?" : concept.name();
			}
			if (feature instanceof Attribute<?, ?, ?, ?> attribute)
			{
				final var datatype = attribute.datatype();
				return datatype == null ? "?" : datatypeName(datatype);
			}
			return "?";
		}

		private static String datatypeName(final Datatype<?> datatype)
		{
			return datatype.name();
		}

		private static String cardinality(final boolean mandatory, final boolean many)
		{
			final var lower = mandatory ? "1" : "0";
			final var upper = many ? "*" : "1";
			return "[" + lower + ".." + upper + "]";
		}
	}

	private record FeatureKey(int id,
							  String name,
							  FeatureKind kind,
							  boolean mandatory,
							  boolean many,
							  boolean immutable,
							  String type)
	{
		static FeatureKey from(final FeatureLine line, final int id)
		{
			return new FeatureKey(id,
								  line.name(),
								  line.kind(),
								  line.mandatory(),
								  line.many(),
								  line.immutable(),
								  line.type());
		}
	}
}
