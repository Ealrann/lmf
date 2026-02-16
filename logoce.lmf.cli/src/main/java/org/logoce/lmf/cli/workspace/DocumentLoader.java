package org.logoce.lmf.cli.workspace;

import org.logoce.lmf.core.api.model.ModelRegistry;
import org.logoce.lmf.core.loader.api.loader.LmLoader;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.loader.api.loader.model.LmDocument;
import org.logoce.lmf.core.loader.api.loader.parsing.LmTreeReader;
import org.logoce.lmf.core.loader.api.loader.util.TextPositions;
import org.logoce.lmf.core.loader.api.loader.linking.LinkException;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class DocumentLoader
{
	private final Map<Path, String> overlaySources;

	public DocumentLoader()
	{
		this(Map.of());
	}

	public DocumentLoader(final Map<Path, String> overlaySources)
	{
		this.overlaySources = Objects.requireNonNull(overlaySources, "overlaySources");
	}

	public Set<Path> overlayPaths()
	{
		return Collections.unmodifiableSet(overlaySources.keySet());
	}

	public String readString(final Path path, final PrintWriter err)
	{
		Objects.requireNonNull(path, "path");
		Objects.requireNonNull(err, "err");

		final var normalizedPath = path.toAbsolutePath().normalize();
		final var overlay = overlaySources.get(normalizedPath);
		if (overlay != null)
		{
			return overlay;
		}
		try
		{
			return Files.readString(normalizedPath, StandardCharsets.UTF_8);
		}
		catch (Exception e)
		{
			final var message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
			err.println("Failed to read model file: " + normalizedPath + " (" + message + ")");
			return null;
		}
	}

	public LmDocument loadModelFromPath(final ModelRegistry registry,
										final String modelQualifiedName,
										final Path path,
										final PrintWriter err)
	{
		Objects.requireNonNull(path, "path");
		Objects.requireNonNull(err, "err");

		final var source = readString(path, err);
		if (source == null)
		{
			return null;
		}
		return loadModelFromSource(registry, modelQualifiedName, source, err);
	}

	public LmDocument loadModelFromSource(final ModelRegistry registry,
										  final String modelQualifiedName,
										  final CharSequence source,
										  final PrintWriter err)
	{
		Objects.requireNonNull(registry, "registry");
		Objects.requireNonNull(source, "source");
		Objects.requireNonNull(err, "err");

		final var diagnostics = new ArrayList<LmDiagnostic>();
		final var reader = new LmTreeReader();
		final var readResult = reader.read(source, diagnostics);

		final var builder = new ModelRegistry.Builder(registry);
		if (modelQualifiedName != null && !modelQualifiedName.isBlank())
		{
			builder.remove(modelQualifiedName);
		}

		final var loader = new LmLoader(builder.build());
		try
		{
			return loader.loadModel(readResult, diagnostics);
		}
		catch (RuntimeException e)
		{
			final var message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
			addErrorDiagnostic(readResult.source(), diagnostics, e, message);

			return new LmDocument(null,
								  List.copyOf(diagnostics),
								  readResult.roots(),
								  readResult.source(),
								  List.of());
		}
	}

	private static void addErrorDiagnostic(final CharSequence source,
										   final List<LmDiagnostic> diagnostics,
										   final RuntimeException exception,
										   final String message)
	{
		if (exception instanceof LinkException linkException && linkException.pNode() != null)
		{
			final var span = TextPositions.spanOf(linkException.pNode(), source);
			diagnostics.add(new LmDiagnostic(span.line(),
											span.column(),
											span.length(),
											span.offset(),
											LmDiagnostic.Severity.ERROR,
											"Failed to load model: " + message));
			return;
		}

		diagnostics.add(new LmDiagnostic(1,
										1,
										1,
										0,
										LmDiagnostic.Severity.ERROR,
										"Failed to load model: " + message));
	}
}
