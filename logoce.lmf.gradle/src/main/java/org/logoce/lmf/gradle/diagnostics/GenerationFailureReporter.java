package org.logoce.lmf.gradle.diagnostics;

import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GenerationFailureReporter
{
	private GenerationFailureReporter()
	{
	}

	public static void report(final Logger logger, final List<File> modelFiles, final Exception error)
	{
		final var message = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();

		final var inspections = ModelInspector.inspect(modelFiles);
		final boolean hasDiagnostics = inspections.stream().anyMatch(r -> r.diagnostics().isEmpty() == false);
		final var base = commonRoot(modelFiles);
		logInspectionSummary(logger, inspections, base);

		logger.error("");

		final var primaryError = PrimaryErrorSelector.selectPrimary(inspections);
		if (primaryError.isPresent())
		{
			final var entry = primaryError.get();
			final var diagnostic = entry.diagnostic();
			logger.error("LMF generation failed in {}:{}:{} [{}] {}",
						 toRelative(base, entry.file()),
						 diagnostic.line(),
						 diagnostic.column(),
						 diagnostic.severity(),
						 diagnostic.message());
		}
		else
		{
			logger.error("LMF generation failed: {}", message);
			if (hasDiagnostics == false)
			{
				extractTokenName(message).ifPresent(token -> logTokenLocations(logger, modelFiles, token, base));
			}
		}

		if (message != null && message.contains("Cannot resolve all imports"))
		{
			ImportResolutionAnalyzer.logUnresolvedImports(logger, inspections);
		}

		throw new GradleException("Failed to generate LMF sources. See error log above for details.", error);
	}

	private static Optional<String> extractTokenName(final String message)
	{
		if (message == null || message.isBlank()) return Optional.empty();

		final Pattern quotedToken = Pattern.compile("token\\s+'([^']+)'", Pattern.CASE_INSENSITIVE);
		final Matcher quoted = quotedToken.matcher(message);
		if (quoted.find()) return Optional.ofNullable(quoted.group(1));

		final Pattern namedToken = Pattern.compile("named\\s+Token\\s+([A-Za-z0-9_]+)", Pattern.CASE_INSENSITIVE);
		final Matcher named = namedToken.matcher(message);
		if (named.find()) return Optional.ofNullable(named.group(1));

		final Pattern tokenPattern = Pattern.compile("Token\\s+([A-Za-z0-9_]+)");
		final Matcher tokenMatcher = tokenPattern.matcher(message);
		if (tokenMatcher.find()) return Optional.ofNullable(tokenMatcher.group(1));

		return Optional.empty();
	}

	private static void logTokenLocations(final Logger logger,
										  final List<File> modelFiles,
										  final String token,
										  final Path base)
	{
		final var hit = TokenLocator.findFirst(modelFiles, token);
		if (hit.isEmpty())
		{
			logger.error("No occurrence of '{}' found while parsing model files.", token);
			return;
		}

		final var h = hit.get();
		logger.error("Problematic token '{}': {}:{}:{}", token, toRelative(base, h.file()), h.line(), h.column());
	}

	private static void logInspectionSummary(final Logger logger,
											 final List<ModelInspectionResult> inspections,
											 final Path base)
	{
		if (inspections.isEmpty())
		{
			logger.error("Unable to inspect imports because no models were read.");
			return;
		}

		logger.error("Detected models:");
		for (final var inspection : inspections)
		{
			if (inspection.hasMetaModel())
			{
				logger.error(" - {} -> {} (imports: {})",
							 toRelative(base, inspection.file()),
							 inspection.qualifiedName().orElse("[unknown]"),
							 describeImports(inspection.imports()));
			}
			else
			{
				logger.error(" - {} -> [no MetaModel] (imports: {})", toRelative(base, inspection.file()),
							 describeImports(inspection.imports()));
			}

			logDiagnostics(logger, inspection, base);
		}
	}

	private static void logDiagnostics(final Logger logger,
									   final ModelInspectionResult inspection,
									   final Path base)
	{
		for (final LmDiagnostic diagnostic : filterDiagnostics(inspection.diagnostics()))
		{
			logger.error("   {}:{}:{} [{}] {}",
						 toRelative(base, inspection.file()),
						 diagnostic.line(),
						 diagnostic.column(),
						 diagnostic.severity(),
						 diagnostic.message());
		}
	}

	private static String describeImports(final List<String> imports)
	{
		return imports.isEmpty() ? "[none]" : imports.toString();
	}

	private static List<LmDiagnostic> filterDiagnostics(final List<LmDiagnostic> diagnostics)
	{
		if (diagnostics.isEmpty()) return diagnostics;

		final boolean hasSpecificLocation = diagnostics.stream()
													   .anyMatch(d -> d.line() > 1 || d.column() > 1);
		if (hasSpecificLocation == false) return diagnostics;

		final List<LmDiagnostic> filtered = new ArrayList<>(diagnostics.size());
		for (final var diagnostic : diagnostics)
		{
			final boolean isGenericLinkError = diagnostic.line() == 1 &&
											   diagnostic.column() == 1 &&
											   "Link error".equals(diagnostic.message());
			if (isGenericLinkError == false)
			{
				filtered.add(diagnostic);
			}
		}

		return filtered.isEmpty() ? diagnostics : filtered;
	}

	private static Path commonRoot(final List<File> files)
	{
		if (files.isEmpty()) return Path.of("").toAbsolutePath().normalize();

		Path root = toPathSafe(files.getFirst()).getParent();
		for (int i = 1; i < files.size() && root != null; i++)
		{
			Path current = toPathSafe(files.get(i));
			while (current != null && !current.startsWith(root))
			{
				root = root.getParent();
			}
		}
		return root == null ? Path.of("").toAbsolutePath().normalize() : root;
	}

	private static Path toPathSafe(final File file)
	{
		try
		{
			return file.getCanonicalFile().toPath();
		}
		catch (Exception e)
		{
			return file.toPath().toAbsolutePath().normalize();
		}
	}

	private static String toRelative(final Path base, final File file)
	{
		try
		{
			final var target = toPathSafe(file);
			return base.relativize(target).toString();
		}
		catch (Exception e)
		{
			return file.toString();
		}
	}
}
