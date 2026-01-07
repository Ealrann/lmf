package org.logoce.lmf.cli.workspace;

import org.logoce.lmf.cli.ExitCodes;
import org.logoce.lmf.cli.diagnostics.DiagnosticReporter;
import org.logoce.lmf.cli.util.PathDisplay;
import org.logoce.lmf.core.api.model.ModelRegistry;
import org.logoce.lmf.core.loader.api.loader.LmWorkspace;
import org.logoce.lmf.core.loader.api.tooling.workspace.DiskMetaModelHeaderIndex;
import org.logoce.lmf.core.loader.api.tooling.workspace.DiskModelHeaderIndex;
import org.logoce.lmf.core.lang.Model;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
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

		record Failure(int exitCode) implements PrepareResult
		{
		}
	}

	public sealed interface PrepareWorkspaceResult permits PrepareWorkspaceResult.Success, PrepareWorkspaceResult.Failure
	{
		record Success(PreparedWorkspace workspace) implements PrepareWorkspaceResult
		{
		}

		record Failure(int exitCode) implements PrepareWorkspaceResult
		{
		}
	}

	private final Path projectRoot;
	private final DocumentLoader documentLoader;
	private final DiskModelHeaderIndex modelIndex = new DiskModelHeaderIndex();
	private final DiskMetaModelHeaderIndex metaModelIndex = new DiskMetaModelHeaderIndex();

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
			return new PrepareResult.Failure(ExitCodes.INVALID);
		}

		final var targetHeader = resolveHeaderForPath(modelIndex, targetModelPath);
		if (targetHeader == null || targetHeader.qualifiedName() == null)
		{
			final var display = PathDisplay.display(projectRoot, targetModelPath);
			err.println("Cannot resolve model header for " + display);
			return new PrepareResult.Failure(ExitCodes.INVALID);
		}

		final var targetQualifiedName = targetHeader.qualifiedName();
		final var registryModels = computeImportsClosure(modelIndex, Set.of(targetQualifiedName));
		final var registryModelPaths = resolveUniqueModelPaths(modelIndex,
															  targetQualifiedName,
															  targetModelPath,
															  registryModels,
															  err);
		if (registryModelPaths == null)
		{
			return new PrepareResult.Failure(ExitCodes.USAGE);
		}

		final var requiredMetaModels = collectRequiredMetaModels(modelIndex, registryModelPaths);
		final var metaModelFiles = metaModelIndex.resolveMetaModelFilesClosure(requiredMetaModels);

		final ModelRegistry baseRegistry;
		try
		{
			final var metaWorkspace = LmWorkspace.loadMetaModels(metaModelFiles, ModelRegistry.empty());
			baseRegistry = metaWorkspace.registry();
		}
		catch (IOException e)
		{
			final var message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
			err.println("Failed to load meta-models: " + message);
			return new PrepareResult.Failure(ExitCodes.INVALID);
		}

		final var dependencyRegistry = buildDependencyRegistry(baseRegistry,
															  registryModelPaths,
															  targetQualifiedName,
															  false,
															  true,
															  err);
		if (dependencyRegistry == null)
		{
			return new PrepareResult.Failure(ExitCodes.INVALID);
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
			return new PrepareWorkspaceResult.Failure(ExitCodes.INVALID);
		}

		final var targetHeader = resolveHeaderForPath(modelIndex, targetModelPath);
		if (targetHeader == null || targetHeader.qualifiedName() == null)
		{
			final var display = PathDisplay.display(projectRoot, targetModelPath);
			err.println("Cannot resolve model header for " + display);
			return new PrepareWorkspaceResult.Failure(ExitCodes.INVALID);
		}

		final var targetQualifiedName = targetHeader.qualifiedName();
		final var scanModels = computeImportersClosure(modelIndex, targetQualifiedName);
		final var registryModels = computeImportsClosure(modelIndex, scanModels);
		final var registryModelPaths = resolveUniqueModelPaths(modelIndex,
															  targetQualifiedName,
															  targetModelPath,
															  registryModels,
															  err);
		if (registryModelPaths == null)
		{
			return new PrepareWorkspaceResult.Failure(ExitCodes.USAGE);
		}

		final var scanModelPaths = subsetPaths(registryModelPaths, scanModels);
		final var headersByQualifiedName = resolveHeadersByQualifiedName(modelIndex, registryModelPaths);

		final var requiredMetaModels = collectRequiredMetaModels(modelIndex, registryModelPaths);
		final var metaModelFiles = metaModelIndex.resolveMetaModelFilesClosure(requiredMetaModels);

		final ModelRegistry baseRegistry;
		try
		{
			final var metaWorkspace = LmWorkspace.loadMetaModels(metaModelFiles, ModelRegistry.empty());
			baseRegistry = metaWorkspace.registry();
		}
		catch (IOException e)
		{
			final var message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
			err.println("Failed to load meta-models: " + message);
			return new PrepareWorkspaceResult.Failure(ExitCodes.INVALID);
		}

		final var dependencyRegistry = buildDependencyRegistry(baseRegistry,
															  registryModelPaths,
															  targetQualifiedName,
															  true,
															  requireValidImports,
															  err);
		if (dependencyRegistry == null)
		{
			return new PrepareWorkspaceResult.Failure(ExitCodes.INVALID);
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

	private ModelRegistry buildDependencyRegistry(final ModelRegistry baseRegistry,
												 final Map<String, Path> models,
												 final String targetQualifiedName,
												 final boolean includeTarget,
												 final boolean requireValidImports,
												 final PrintWriter err)
	{
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

			final var header = resolveHeaderForQualifiedName(modelIndex, qualifiedName, path);
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
				final var header = resolveHeaderForQualifiedName(modelIndex, candidate, pathsByName.get(candidate));
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
			metaModelIndex.refresh(projectRoot);
			return true;
		}
		catch (IOException e)
		{
			final var message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
			err.println("Failed to scan workspace: " + message);
			return false;
		}
	}

	private static DiskModelHeaderIndex.DiskModelHeader resolveHeaderForPath(final DiskModelHeaderIndex index, final Path path)
	{
		final var normalized = path.toAbsolutePath().normalize();
		final var header = index.headerForPath(normalized);
		if (header != null)
		{
			return header;
		}
		return index.headerForPath(path);
	}

	private static DiskModelHeaderIndex.DiskModelHeader resolveHeaderForQualifiedName(final DiskModelHeaderIndex index,
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

	private static Set<String> computeImportersClosure(final DiskModelHeaderIndex index, final String targetQualifiedName)
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

	private static Set<String> computeImportsClosure(final DiskModelHeaderIndex index, final Set<String> roots)
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

	private static Map<String, Path> resolveUniqueModelPaths(final DiskModelHeaderIndex index,
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

	private static Map<String, DiskModelHeaderIndex.DiskModelHeader> resolveHeadersByQualifiedName(final DiskModelHeaderIndex index,
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

	private static Set<String> collectRequiredMetaModels(final DiskModelHeaderIndex modelIndex, final Map<String, Path> models)
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
				required.addAll(header.imports());
			}
			else
			{
				required.addAll(header.metamodels());
			}
		}
		return Set.copyOf(required);
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
