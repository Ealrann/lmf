package org.logoce.lmf.cli.workspace;

import org.logoce.lmf.cli.ExitCodes;
import org.logoce.lmf.cli.diagnostics.DiagnosticReporter;
import org.logoce.lmf.cli.util.PathDisplay;
import org.logoce.lmf.core.api.model.ModelRegistry;
import org.logoce.lmf.core.lang.Model;
import org.logoce.lmf.core.loader.api.loader.LmLoader;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.loader.api.loader.model.LmDocument;
import org.logoce.lmf.core.loader.api.loader.parsing.LmTreeReader;
import org.logoce.lmf.core.loader.api.loader.parsing.ModelHeaderUtil;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;
import org.logoce.lmf.core.loader.api.tooling.workspace.DiskModelHeaderIndex;
import org.logoce.lmf.core.util.tree.Tree;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class RegistryService
{
	public sealed interface PrepareResult permits PrepareResult.Success, PrepareResult.Failure
	{
		record Success(PreparedRegistry registry) implements PrepareResult
		{
		}

		record Failure(int exitCode, String message, List<String> details) implements PrepareResult
		{
			public Failure
			{
				Objects.requireNonNull(message, "message");
				details = details == null ? List.of() : List.copyOf(details);
			}
		}
	}

	public sealed interface PrepareWorkspaceResult permits PrepareWorkspaceResult.Success, PrepareWorkspaceResult.Failure
	{
		record Success(PreparedWorkspace workspace) implements PrepareWorkspaceResult
		{
		}

		record Failure(int exitCode, String message, List<String> details) implements PrepareWorkspaceResult
		{
			public Failure
			{
				Objects.requireNonNull(message, "message");
				details = details == null ? List.of() : List.copyOf(details);
			}
		}
	}

	private final Path projectRoot;
	private final DocumentLoader documentLoader;
	private final DiskModelHeaderIndex modelIndex = new DiskModelHeaderIndex();

	private static final String IMPORT_RESOLUTION_PREFIX = "Cannot resolve all imports between provided models: ";

	public RegistryService(final Path projectRoot, final DocumentLoader documentLoader)
	{
		this.projectRoot = Objects.requireNonNull(projectRoot, "projectRoot").toAbsolutePath().normalize();
		this.documentLoader = Objects.requireNonNull(documentLoader, "documentLoader");
	}

	public PrepareResult prepareForModel(final Path targetModelPath, final PrintWriter err)
	{
		Objects.requireNonNull(targetModelPath, "targetModelPath");
		Objects.requireNonNull(err, "err");

		if (!refreshIndices(err))
		{
			return new PrepareResult.Failure(ExitCodes.INVALID, "Failed to scan workspace", List.of());
		}

		final var index = EffectiveModelHeaderIndex.build(modelIndex, documentLoader, err);
		final var targetHeader = index.headerForPath(targetModelPath);
		if (targetHeader == null || targetHeader.qualifiedName() == null)
		{
			final var display = PathDisplay.display(projectRoot, targetModelPath);
			err.println("Cannot resolve model header for " + display);
			return new PrepareResult.Failure(ExitCodes.INVALID, "Cannot resolve model header for " + display, List.of());
		}

		final var targetQualifiedName = targetHeader.qualifiedName();
		final var registryModels = computeImportsClosure(index, Set.of(targetQualifiedName));
		final var registryModelPaths = resolveUniqueModelPaths(index,
													  targetQualifiedName,
													  targetModelPath,
													  registryModels,
													  err);
		if (registryModelPaths == null)
		{
			return new PrepareResult.Failure(ExitCodes.INVALID,
										 "Ambiguous model qualified name in imports closure",
											 List.of());
		}

		final var requiredMetaModels = collectRequiredMetaModels(index, registryModelPaths);
		final var metaRegistryResult = loadMetaModelRegistry(index, requiredMetaModels, err);
		if (metaRegistryResult instanceof MetaModelRegistryResult.Failure failure)
		{
			reportFailure(err, failure.failure());
			return new PrepareResult.Failure(failure.failure().exitCode(),
										 failure.failure().message(),
										 failure.failure().details());
		}

		final var baseRegistry = ((MetaModelRegistryResult.Success) metaRegistryResult).registry();
		final var dependencyRegistry = buildDependencyRegistry(index,
													   baseRegistry,
													   registryModelPaths,
													   targetQualifiedName,
													   false,
													   true,
												   err);
		if (dependencyRegistry == null)
		{
			return new PrepareResult.Failure(ExitCodes.INVALID, "Failed to build dependency registry", List.of());
		}

		final var prepared = new PreparedRegistry(targetModelPath.toAbsolutePath().normalize(),
										  targetHeader,
										  targetQualifiedName,
										  dependencyRegistry);
		return new PrepareResult.Success(prepared);
	}

	public PrepareWorkspaceResult prepareForModelAndImporters(final Path targetModelPath, final PrintWriter err)
	{
		return prepareForModelAndImporters(targetModelPath, err, true);
	}

	public PrepareWorkspaceResult prepareForModelAndImporters(final Path targetModelPath,
														 final PrintWriter err,
															 final boolean requireValidImports)
	{
		Objects.requireNonNull(targetModelPath, "targetModelPath");
		Objects.requireNonNull(err, "err");

		if (!refreshIndices(err))
		{
			return new PrepareWorkspaceResult.Failure(ExitCodes.INVALID, "Failed to scan workspace", List.of());
		}

		final var index = EffectiveModelHeaderIndex.build(modelIndex, documentLoader, err);
		final var targetHeader = index.headerForPath(targetModelPath);
		if (targetHeader == null || targetHeader.qualifiedName() == null)
		{
			final var display = PathDisplay.display(projectRoot, targetModelPath);
			err.println("Cannot resolve model header for " + display);
			return new PrepareWorkspaceResult.Failure(ExitCodes.INVALID,
												  "Cannot resolve model header for " + display,
												  List.of());
		}

		final var targetQualifiedName = targetHeader.qualifiedName();
		final var scanModels = computeImportersClosure(index, targetQualifiedName);
		final var registryModels = computeImportsClosure(index, scanModels);
		final var registryModelPaths = resolveUniqueModelPaths(index,
													  targetQualifiedName,
													  targetModelPath,
													  registryModels,
													  err);
		if (registryModelPaths == null)
		{
			return new PrepareWorkspaceResult.Failure(ExitCodes.INVALID,
												  "Ambiguous model qualified name in imports closure",
												  List.of());
			}

			final var scanModelPaths = subsetPaths(registryModelPaths, scanModels);
			final var headersByQualifiedName = resolveHeadersByQualifiedName(index, registryModelPaths);

			final var requiredMetaModels = collectRequiredMetaModels(index, registryModelPaths);
			final var metaRegistryResult = loadMetaModelRegistry(index, requiredMetaModels, err);
			if (metaRegistryResult instanceof MetaModelRegistryResult.Failure failure)
			{
				reportFailure(err, failure.failure());
				return new PrepareWorkspaceResult.Failure(failure.failure().exitCode(),
												  failure.failure().message(),
												  failure.failure().details());
			}

			final var baseRegistry = ((MetaModelRegistryResult.Success) metaRegistryResult).registry();
			final var dependencyRegistry = buildDependencyRegistry(index,
													   baseRegistry,
													   registryModelPaths,
													   targetQualifiedName,
													   true,
													   requireValidImports,
												   err);
		if (dependencyRegistry == null)
		{
			return new PrepareWorkspaceResult.Failure(ExitCodes.INVALID, "Failed to build dependency registry", List.of());
		}

		final var prepared = new PreparedWorkspace(targetModelPath.toAbsolutePath().normalize(),
										   targetHeader,
										   targetQualifiedName,
										   dependencyRegistry,
										   scanModelPaths,
										   registryModelPaths,
										   headersByQualifiedName);
		return new PrepareWorkspaceResult.Success(prepared);
	}

	private PrepareFailure prepareFailureForMetaModelLoad(final Exception exception, final EffectiveModelHeaderIndex index)
	{
		Objects.requireNonNull(index, "index");
		final var message = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
		if (exception instanceof IllegalStateException && message.startsWith(IMPORT_RESOLUTION_PREFIX))
		{
			final var unresolved = parseUnresolvedQualifiedNames(message);
			final var cycle = findImportCycle(unresolved, index);
			if (cycle != null && !cycle.isEmpty())
			{
				final var details = describeCycleDetails(cycle, index);
				return new PrepareFailure(ExitCodes.INVALID,
									  "Import cycle detected between meta-models: " + String.join(" -> ", cycle),
									  details);
			}
			return new PrepareFailure(ExitCodes.INVALID,
									  "Cannot resolve meta-model imports (cycle or missing import): " + String.join(", ", unresolved),
									  List.of());
		}

		return new PrepareFailure(ExitCodes.INVALID, "Failed to load meta-models: " + message, List.of());
	}

	private List<String> describeCycleDetails(final List<String> cycle, final EffectiveModelHeaderIndex index)
	{
		Objects.requireNonNull(index, "index");
		final var details = new ArrayList<String>();
		final var seen = new HashSet<String>();
		for (final var name : cycle)
		{
			if (name == null || !seen.add(name))
			{
				continue;
			}
			final var header = resolveHeaderForQualifiedName(index, name, null);
			if (header != null && header.path() != null)
			{
				details.add(name + " -> " + PathDisplay.display(projectRoot, header.path()));
			}
		}
		return List.copyOf(details);
	}

	private static List<String> parseUnresolvedQualifiedNames(final String message)
	{
		if (message == null || !message.startsWith(IMPORT_RESOLUTION_PREFIX))
		{
			return List.of();
		}

		final var tail = message.substring(IMPORT_RESOLUTION_PREFIX.length());
		final var parts = tail.split(",");
		final var result = new ArrayList<String>(parts.length);
		for (final var raw : parts)
		{
			if (raw == null)
			{
				continue;
			}
			final var trimmed = raw.trim();
			if (!trimmed.isEmpty())
			{
				result.add(trimmed);
			}
		}
		return List.copyOf(result);
	}

	private List<String> findImportCycle(final List<String> unresolved, final EffectiveModelHeaderIndex index)
	{
		Objects.requireNonNull(index, "index");
		if (unresolved == null || unresolved.isEmpty())
		{
			return null;
		}

		final var nodes = new LinkedHashSet<>(unresolved);
		final var graph = new HashMap<String, List<String>>();
		for (final var name : nodes)
		{
			final var header = resolveHeaderForQualifiedName(index, name, null);
			if (header == null || header.imports() == null || header.imports().isEmpty())
			{
				graph.put(name, List.of());
				continue;
			}

			final var deps = new ArrayList<String>();
			for (final var imp : header.imports())
			{
				if (imp != null && nodes.contains(imp))
				{
					deps.add(imp);
				}
			}
			graph.put(name, List.copyOf(deps));
		}

		final var state = new HashMap<String, VisitState>();
		final var parent = new HashMap<String, String>();

		for (final var node : nodes)
		{
			if (state.get(node) == null)
			{
				final var cycle = dfsCycle(node, graph, state, parent);
				if (cycle != null)
				{
					return cycle;
				}
			}
		}

		return null;
	}

	private static List<String> dfsCycle(final String node,
									final Map<String, List<String>> graph,
									final Map<String, VisitState> state,
									final Map<String, String> parent)
	{
		state.put(node, VisitState.VISITING);

		for (final var next : graph.getOrDefault(node, List.of()))
		{
			final var nextState = state.get(next);
			if (nextState == null)
			{
				parent.put(next, node);
				final var cycle = dfsCycle(next, graph, state, parent);
				if (cycle != null)
				{
					return cycle;
				}
			}
			else if (nextState == VisitState.VISITING)
			{
				return buildCyclePath(node, next, parent);
			}
		}

		state.put(node, VisitState.VISITED);
		return null;
	}

	private static List<String> buildCyclePath(final String current,
										final String target,
										final Map<String, String> parent)
	{
		final var cycle = new ArrayList<String>();
		cycle.add(target);
		String node = current;
		while (node != null && !node.equals(target))
		{
			cycle.add(node);
			node = parent.get(node);
		}
		cycle.add(target);
		java.util.Collections.reverse(cycle);
		return List.copyOf(cycle);
	}

	private static void reportFailure(final PrintWriter err, final PrepareFailure failure)
	{
		if (failure == null)
		{
			return;
		}

		err.println(failure.message());
		if (failure.details() != null && !failure.details().isEmpty())
		{
			for (final var detail : failure.details())
			{
				err.println(" - " + detail);
			}
		}
	}

	private enum VisitState
	{
		VISITING,
		VISITED
	}

	private record PrepareFailure(int exitCode, String message, List<String> details)
	{
		PrepareFailure
		{
			Objects.requireNonNull(message, "message");
			details = details == null ? List.of() : List.copyOf(details);
		}
	}

	private ModelRegistry buildDependencyRegistry(final EffectiveModelHeaderIndex index,
											 final ModelRegistry baseRegistry,
											 final Map<String, Path> models,
											 final String targetQualifiedName,
											 final boolean includeTarget,
											 final boolean requireValidImports,
											 final PrintWriter err)
	{
		Objects.requireNonNull(index, "index");
		final var builder = new ModelRegistry.Builder(baseRegistry);
		final var toLoad = new LinkedHashSet<String>();
		final var pathsByName = new HashMap<String, Path>();

		for (final var entry : models.entrySet())
		{
			final var qualifiedName = entry.getKey();
			final var path = entry.getValue();
			if (qualifiedName == null || (!includeTarget && qualifiedName.equals(targetQualifiedName)))
			{
				continue;
			}

				final var header = resolveHeaderForQualifiedName(index, qualifiedName, path);
				if (header == null || header.metaModelRoot())
				{
					continue;
				}

			toLoad.add(qualifiedName);
			pathsByName.put(qualifiedName, path);
		}

		final var remaining = new LinkedHashSet<String>(toLoad);
		final var loaded = new HashSet<String>();

		while (!remaining.isEmpty())
		{
			String next = null;
				for (final var candidate : remaining)
				{
					final var header = resolveHeaderForQualifiedName(index, candidate, pathsByName.get(candidate));
					if (header == null)
					{
						continue;
					}

				final boolean depsLoaded = header.imports()
											  .stream()
											  .filter(remaining::contains)
											  .allMatch(loaded::contains);
				if (depsLoaded)
				{
					next = candidate;
					break;
				}
			}

			if (next == null)
			{
				next = remaining.iterator().next();
			}

			remaining.remove(next);
			final var path = pathsByName.get(next);
			final var source = documentLoader.readString(path, err);
			if (source == null)
			{
				return null;
			}

			final var display = PathDisplay.display(projectRoot, path);
			final var doc = documentLoader.loadModelFromSource(builder.build(), next, source, err);
			if (doc == null)
			{
				if (requireValidImports)
				{
					return null;
				}
				continue;
			}
			if (DiagnosticReporter.hasErrors(doc.diagnostics()))
			{
				if (requireValidImports)
				{
					DiagnosticReporter.printDiagnostics(err, display, doc.diagnostics());
					err.println("Cannot load imported model required for validation: " + next);
					return null;
				}
			}
			if (doc.model() instanceof Model model)
			{
				builder.register(model);
			}
			else if (requireValidImports)
			{
				err.println("Input doesn't define a valid model: " + display);
				return null;
			}

			loaded.add(next);
		}

		return builder.build();
	}

	private boolean refreshIndices(final PrintWriter err)
	{
		try
		{
			modelIndex.refresh(projectRoot);
			return true;
		}
		catch (IOException e)
		{
			final var message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
			err.println("Failed to scan workspace: " + message);
			return false;
		}
	}

	private static DiskModelHeaderIndex.DiskModelHeader resolveHeaderForQualifiedName(final EffectiveModelHeaderIndex index,
																		 final String qualifiedName,
																		 final Path expectedPath)
	{
		if (qualifiedName == null)
		{
			return null;
		}

		final var candidates = index.headersByQualifiedName(qualifiedName);
		if (candidates.isEmpty())
		{
			return null;
		}
		if (candidates.size() == 1)
		{
			return candidates.getFirst();
		}
		if (expectedPath != null)
		{
			final var normalizedExpected = expectedPath.toAbsolutePath().normalize();
			for (final var candidate : candidates)
			{
				if (candidate.path().toAbsolutePath().normalize().equals(normalizedExpected))
				{
					return candidate;
				}
			}
		}
		return null;
	}

	private static Set<String> computeImportersClosure(final EffectiveModelHeaderIndex index, final String targetQualifiedName)
	{
		final var closure = new LinkedHashSet<String>();
		final var queue = new ArrayDeque<String>();
		closure.add(targetQualifiedName);
		queue.add(targetQualifiedName);

		while (!queue.isEmpty())
		{
			final var current = queue.removeFirst();
			for (final var importer : index.importersOf(current))
			{
				final var qualifiedName = importer.qualifiedName();
				if (qualifiedName != null && closure.add(qualifiedName))
				{
					queue.add(qualifiedName);
				}
			}
		}

		return Set.copyOf(closure);
	}

	private static Set<String> computeImportsClosure(final EffectiveModelHeaderIndex index, final Set<String> roots)
	{
		final var closure = new LinkedHashSet<String>(roots);
		final var queue = new ArrayDeque<String>(roots);

		while (!queue.isEmpty())
		{
			final var current = queue.removeFirst();
			final var candidates = index.headersByQualifiedName(current);
			if (candidates.isEmpty())
			{
				continue;
			}
			final var header = candidates.getFirst();
			for (final var imp : header.imports())
			{
				if (imp == null || imp.isBlank())
				{
					continue;
				}
				if (!index.headersByQualifiedName(imp).isEmpty() && closure.add(imp))
				{
					queue.add(imp);
				}
			}
		}

		return Set.copyOf(closure);
	}

	private static Map<String, Path> resolveUniqueModelPaths(final EffectiveModelHeaderIndex index,
																 final String targetQualifiedName,
																 final Path targetPath,
																 final Set<String> models,
																 final PrintWriter err)
	{
		final var result = new HashMap<String, Path>();

		for (final var name : models)
		{
			if (name == null)
			{
				continue;
			}

			final var candidates = index.headersByQualifiedName(name);
			if (candidates.isEmpty())
			{
				continue;
			}

			if (candidates.size() == 1)
			{
				result.put(name, candidates.getFirst().path());
				continue;
			}

			if (name.equals(targetQualifiedName))
			{
				final var normalizedTarget = targetPath.toAbsolutePath().normalize();
				final var match = candidates.stream()
												  .filter(h -> h.path().toAbsolutePath().normalize().equals(normalizedTarget))
												  .findFirst();
				if (match.isPresent())
				{
					result.put(name, match.get().path());
					continue;
				}
			}

			err.println("Ambiguous model qualified name: " + name);
			for (final var header : candidates)
			{
				err.println(" - " + header.path());
			}
			return null;
		}

		return Map.copyOf(result);
	}

	private static Map<String, Path> subsetPaths(final Map<String, Path> registryModelPaths,
											 final Set<String> names)
	{
		final var result = new HashMap<String, Path>();
		for (final var name : names)
		{
			final var path = registryModelPaths.get(name);
			if (path != null)
			{
				result.put(name, path);
			}
		}
		return Map.copyOf(result);
	}

	private static Map<String, DiskModelHeaderIndex.DiskModelHeader> resolveHeadersByQualifiedName(final EffectiveModelHeaderIndex index,
																										  final Map<String, Path> models)
	{
		final var result = new HashMap<String, DiskModelHeaderIndex.DiskModelHeader>();
		for (final var entry : models.entrySet())
		{
			final var name = entry.getKey();
			final var path = entry.getValue();
			final var header = resolveHeaderForQualifiedName(index, name, path);
			if (header != null)
			{
				result.put(name, header);
			}
		}
		return Map.copyOf(result);
	}

	private static Set<String> collectRequiredMetaModels(final EffectiveModelHeaderIndex modelIndex, final Map<String, Path> models)
	{
		final var required = new HashSet<String>();
		for (final var entry : models.entrySet())
		{
			final var header = resolveHeaderForQualifiedName(modelIndex, entry.getKey(), entry.getValue());
			if (header == null)
			{
				continue;
			}

			if (header.metaModelRoot())
			{
				if (header.qualifiedName() != null)
				{
					required.add(header.qualifiedName());
				}
			}
			else
			{
				required.addAll(header.metamodels());
			}
		}
		return Set.copyOf(required);
	}

	private sealed interface MetaModelRegistryResult permits MetaModelRegistryResult.Success, MetaModelRegistryResult.Failure
	{
		record Success(ModelRegistry registry) implements MetaModelRegistryResult
		{
		}

		record Failure(PrepareFailure failure) implements MetaModelRegistryResult
		{
		}
	}

	private record ParsedMetaModel(Path path, List<String> imports, LmDocument document)
	{
	}

	private MetaModelRegistryResult loadMetaModelRegistry(final EffectiveModelHeaderIndex index,
														 final Set<String> requiredNames,
														 final PrintWriter err)
	{
		Objects.requireNonNull(index, "index");
		if (requiredNames == null || requiredNames.isEmpty())
		{
			return new MetaModelRegistryResult.Success(ModelRegistry.empty());
		}

		final var parsedByPath = new LinkedHashMap<Path, ParsedMetaModel>();
		final var queue = new ArrayDeque<String>(requiredNames);
		final var visited = new LinkedHashSet<String>();

		while (!queue.isEmpty())
		{
			final var name = queue.removeFirst();
			if (name == null || name.isBlank() || !visited.add(name))
			{
				continue;
			}

				final var candidates = index.headersByQualifiedName(name)
											 .stream()
											 .filter(DiskModelHeaderIndex.DiskModelHeader::metaModelRoot)
											 .toList();
			if (candidates.isEmpty())
			{
				return new MetaModelRegistryResult.Failure(new PrepareFailure(ExitCodes.INVALID,
													  "Meta-model not found: " + name,
													  List.of()));
			}
			if (candidates.size() > 1)
			{
				final var details = candidates.stream()
										  .map(DiskModelHeaderIndex.DiskModelHeader::path)
										  .filter(Objects::nonNull)
										  .map(Path::toString)
										  .toList();
				return new MetaModelRegistryResult.Failure(new PrepareFailure(ExitCodes.INVALID,
													  "Duplicate meta-model qualified name: " + name,
													  details));
			}

			final var path = candidates.getFirst().path().toAbsolutePath().normalize();
			final var existing = parsedByPath.get(path);
			final var parsed = existing != null ? existing : parseMetaModel(path, err);
			if (parsed == null)
			{
				return new MetaModelRegistryResult.Failure(new PrepareFailure(ExitCodes.INVALID,
													  "Failed to load meta-model: " + PathDisplay.display(projectRoot, path),
													  List.of()));
			}

			parsedByPath.putIfAbsent(path, parsed);
			for (final var imp : parsed.imports())
			{
				if (imp != null && !imp.isBlank())
				{
					queue.add(imp);
				}
			}
		}

		final var documents = parsedByPath.values().stream().map(ParsedMetaModel::document).toList();
		try
		{
			return new MetaModelRegistryResult.Success(LmLoader.buildRegistry(documents, ModelRegistry.empty()));
			}
			catch (RuntimeException e)
			{
				return new MetaModelRegistryResult.Failure(prepareFailureForMetaModelLoad(e, index));
			}
		}

	private ParsedMetaModel parseMetaModel(final Path path, final PrintWriter err)
	{
		final var displayPath = PathDisplay.display(projectRoot, path);
		final var source = documentLoader.readString(path, err);
		if (source == null)
		{
			return null;
		}

		final var diagnostics = new ArrayList<LmDiagnostic>();
		final var readResult = new LmTreeReader().read(source, diagnostics);
		if (DiagnosticReporter.hasErrors(diagnostics))
		{
			DiagnosticReporter.printDiagnostics(err, displayPath, diagnostics);
			return null;
		}

		final List<Tree<PNode>> roots = readResult.roots();
		if (roots.isEmpty() || !ModelHeaderUtil.isMetaModelRoot(roots))
		{
			err.println("Input doesn't define a valid meta-model: " + displayPath);
			return null;
		}

		final var rootNode = roots.getFirst().data();
		final var imports = List.copyOf(ModelHeaderUtil.resolveImports(rootNode));
		final var document = new LmDocument(null,
									List.copyOf(diagnostics),
									List.copyOf(roots),
									readResult.source(),
									List.of());
		return new ParsedMetaModel(path.toAbsolutePath().normalize(), imports, document);
	}

	public record PreparedRegistry(Path targetPath,
							   DiskModelHeaderIndex.DiskModelHeader targetHeader,
							   String targetQualifiedName,
							   ModelRegistry registry)
	{
	}

	public record PreparedWorkspace(Path targetPath,
							DiskModelHeaderIndex.DiskModelHeader targetHeader,
							String targetQualifiedName,
							ModelRegistry registry,
							Map<String, Path> scanModelPaths,
							Map<String, Path> registryModelPaths,
							Map<String, DiskModelHeaderIndex.DiskModelHeader> headersByQualifiedName)
	{
	}
}
