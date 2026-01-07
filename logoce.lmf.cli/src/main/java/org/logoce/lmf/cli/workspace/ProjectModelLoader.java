package org.logoce.lmf.cli.workspace;

import org.logoce.lmf.core.api.model.ModelRegistry;
import org.logoce.lmf.core.loader.api.loader.LmLoader;
import org.logoce.lmf.core.loader.api.loader.LmWorkspace;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.loader.api.loader.model.LmDocument;
import org.logoce.lmf.core.loader.api.loader.parsing.LmTreeReader;
import org.logoce.lmf.core.loader.api.tooling.workspace.DiskMetaModelHeaderIndex;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public final class ProjectModelLoader
{
	private final Path projectRoot;
	private final DiskMetaModelHeaderIndex headerIndex = new DiskMetaModelHeaderIndex();

	public ProjectModelLoader(final Path projectRoot)
	{
		this.projectRoot = Objects.requireNonNull(projectRoot, "projectRoot")
								  .toAbsolutePath()
								  .normalize();
	}

	public ModelLoadResult load(final Path modelPath)
	{
		final var normalizedPath = modelPath.toAbsolutePath().normalize();
		final String source;
		try
		{
			source = Files.readString(normalizedPath, StandardCharsets.UTF_8);
		}
		catch (IOException e)
		{
			final var message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
			final var diag = new LmDiagnostic(1,
											  1,
											  1,
											  0,
											  LmDiagnostic.Severity.ERROR,
											  "Failed to read model file: " + message);
			final var doc = new LmDocument(null, List.of(diag), List.of(), "", List.of());
			return new ModelLoadResult(normalizedPath, doc, new ModelHeader(false, null, List.of(), List.of()), List.of());
		}

		final var diagnostics = new ArrayList<LmDiagnostic>();
		final var reader = new LmTreeReader();
		final var readResult = reader.read(source, diagnostics);
		final var header = ModelHeader.from(readResult.roots(), readResult.source());

		final var registryResolution = resolveRegistry(header, normalizedPath);
		if (registryResolution.errorMessage() != null)
		{
			diagnostics.add(new LmDiagnostic(1,
											1,
											1,
											0,
											LmDiagnostic.Severity.ERROR,
											registryResolution.errorMessage()));
		}

		var registry = registryResolution.registry();
		if (header.metaModelRoot() && header.qualifiedName() != null)
		{
			final var builder = new ModelRegistry.Builder(registry);
			builder.remove(header.qualifiedName());
			registry = builder.build();
		}

		final var loader = new LmLoader(registry);
		LmDocument document;
		try
		{
			document = loader.loadModel(readResult, diagnostics);
		}
		catch (RuntimeException e)
		{
			final var message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
			diagnostics.add(new LmDiagnostic(1,
											1,
											1,
											0,
											LmDiagnostic.Severity.ERROR,
											"Failed to load model: " + message));
			document = new LmDocument(null,
									  List.copyOf(diagnostics),
									  readResult.roots(),
									  readResult.source(),
									  List.of());
		}

		return new ModelLoadResult(normalizedPath,
								   document,
								   header,
								   registryResolution.metaModelFiles());
	}

	private RegistryResolution resolveRegistry(final ModelHeader header, final Path modelPath)
	{
		final var required = new HashSet<>(header.requiredMetaModels());
		if (required.isEmpty())
		{
			return new RegistryResolution(ModelRegistry.empty(), List.of(), null);
		}

		try
		{
			headerIndex.refresh(projectRoot);
			final var metaModelFiles = filterTargetFile(
				headerIndex.resolveMetaModelFilesClosure(required),
				modelPath);

			if (metaModelFiles.isEmpty())
			{
				return new RegistryResolution(ModelRegistry.empty(),
											  List.of(),
											  "No meta-model files found for: " + String.join(", ", required));
			}

			final var workspace = LmWorkspace.loadMetaModels(metaModelFiles, ModelRegistry.empty());
			return new RegistryResolution(workspace.registry(), metaModelFiles, null);
		}
		catch (IOException e)
		{
			final var message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
			return new RegistryResolution(ModelRegistry.empty(),
										  List.of(),
										  "Failed to load meta-models: " + message);
		}
	}

	private static List<File> filterTargetFile(final List<File> files, final Path modelPath)
	{
		if (files == null || files.isEmpty())
		{
			return List.of();
		}

		final var normalizedTarget = modelPath.toAbsolutePath().normalize();
		final var filtered = new ArrayList<File>(files.size());
		for (final var file : files)
		{
			if (file == null)
			{
				continue;
			}
			final var normalized = file.toPath().toAbsolutePath().normalize();
			if (!normalized.equals(normalizedTarget))
			{
				filtered.add(file);
			}
		}

		return List.copyOf(filtered);
	}

	private record RegistryResolution(ModelRegistry registry, List<File> metaModelFiles, String errorMessage)
	{
	}
}
