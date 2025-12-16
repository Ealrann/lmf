package org.logoce.lmf.gradle.diagnostics;

import org.logoce.lmf.core.lang.Model;
import org.logoce.lmf.core.api.loader.LmLoader;
import org.logoce.lmf.core.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.api.loader.parsing.LmTreeReader;
import org.logoce.lmf.core.api.loader.parsing.ModelHeaderUtil;
import org.logoce.lmf.core.api.text.syntax.PNode;
import org.logoce.lmf.core.api.model.ModelRegistry;
import org.logoce.lmf.core.api.util.TextPositions;
import org.logoce.lmf.core.util.tree.Tree;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class ModelInspector
{
	private ModelInspector()
	{
	}

	static List<ModelInspectionResult> inspect(final List<File> modelFiles)
	{
		if (modelFiles.isEmpty()) return List.of();

		final var registryBuilder = new ModelRegistry.Builder(ModelRegistry.empty());
		final List<ModelInspectionResult> results = new ArrayList<>(modelFiles.size());

		for (final var file : modelFiles)
		{
			results.add(inspectSingle(file, registryBuilder));
		}
		return results;
	}

	private static ModelInspectionResult inspectSingle(final File file, final ModelRegistry.Builder registryBuilder)
	{
		final var registry = registryBuilder.build();

		try (final var inputStream = new FileInputStream(file))
		{
			final var loader = new LmLoader(registry);
			final var document = loader.loadModel(inputStream);

			final List<Tree<PNode>> roots = document.roots();
			final var qualifiedName = extractQualifiedName(roots);
			final var imports = extractImports(roots);

			final var filteredDiagnostics = filterDiagnostics(document.diagnostics());

			final var model = document.model();
			if (model instanceof Model m)
			{
				registryBuilder.register(m);
			}

			return new ModelInspectionResult(file,
											 qualifiedName,
											 imports,
											 filteredDiagnostics.isEmpty() ? List.of() : List.copyOf(filteredDiagnostics));
		}
		catch (IOException e)
		{
			return failureResult(file, e);
		}
		catch (Exception e)
		{
			return failureResult(file, e);
		}
	}

	private static Optional<String> extractQualifiedName(final List<Tree<PNode>> roots)
	{
		if (roots.isEmpty()) return Optional.empty();

		final var node = roots.getFirst().data();
		final var domain = ModelHeaderUtil.resolveDomain(node);
		final String name;
		try
		{
			name = ModelHeaderUtil.resolveName(node);
		}
		catch (Exception ignored)
		{
			return Optional.empty();
		}

		if (name == null || name.isBlank())
		{
			return Optional.empty();
		}

		if (domain == null || domain.isBlank())
		{
			return Optional.of(name);
		}
		return Optional.of(domain + "." + name);
	}

	private static List<String> extractImports(final List<Tree<PNode>> roots)
	{
		if (roots.isEmpty()) return List.of();

		final var node = roots.getFirst().data();
		return ModelHeaderUtil.resolveImports(node);
	}

	private static ModelInspectionResult failureResult(final File file, final Exception error)
	{
		final var message = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();

		final var diagnostics = fallbackDiagnostics(file, message);
		return new ModelInspectionResult(file,
										 Optional.empty(),
										 List.of(),
										 diagnostics.isEmpty() ? List.of() : List.copyOf(diagnostics));
	}

	private static List<LmDiagnostic> filterDiagnostics(final List<LmDiagnostic> diagnostics)
	{
		if (diagnostics.isEmpty()) return diagnostics;

		final List<LmDiagnostic> filtered = new ArrayList<>(diagnostics.size());
		for (final var diagnostic : diagnostics)
		{
			final var message = diagnostic.message() == null ? "" : diagnostic.message();
			final boolean isImportResolution = message.contains("Cannot resolve model '") ||
											   message.contains("Cannot resolve imported model '");
			if (isImportResolution == false)
			{
				filtered.add(diagnostic);
			}
		}
		return filtered;
	}

	private static List<LmDiagnostic> fallbackDiagnostics(final File file, final String message)
	{
		try
		{
			final var source = Files.readString(file.toPath(), StandardCharsets.UTF_8);
			final var diagnostics = new ArrayList<LmDiagnostic>();

			final var reader = new LmTreeReader();
			final var result = reader.read(source, diagnostics);
			if (result.roots().isEmpty() == false)
			{
				final var firstToken = result.roots().getFirst().data().tokens().getFirst();
				final int offset = firstToken.offset();
				final int line = TextPositions.lineFor(source, offset);
				final int col = TextPositions.columnFor(source, offset);

				final var diag = new LmDiagnostic(line,
												  col,
												  Math.max(1, firstToken.length()),
												  offset,
												  LmDiagnostic.Severity.ERROR,
												  "Failed near '" + firstToken.value() + "': " + message);
				return List.of(diag);
			}
			if (diagnostics.isEmpty() == false)
			{
				return List.of(diagnostics.getFirst());
			}
		}
		catch (Exception ignored)
		{
		}
		return List.of(new LmDiagnostic(1,
										1,
										1,
										0,
										LmDiagnostic.Severity.ERROR,
										message));
	}
}
