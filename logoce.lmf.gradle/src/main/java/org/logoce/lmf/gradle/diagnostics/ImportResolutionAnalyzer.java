package org.logoce.lmf.gradle.diagnostics;

import org.gradle.api.logging.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class ImportResolutionAnalyzer
{
	private ImportResolutionAnalyzer()
	{
	}

	static void logUnresolvedImports(final Logger logger, final List<ModelInspectionResult> inspections)
	{
		final var availableNames = inspections.stream()
											  .flatMap(info -> info.qualifiedName().stream())
											  .toList();
		if (availableNames.isEmpty())
		{
			logger.error("No MetaModels detected; cannot resolve imports.");
			return;
		}

		logger.error("Available models: {}", availableNames);

		final var unresolved = findUnresolved(inspections);
		if (unresolved.isEmpty())
		{
			logger.error("All imports are present; check for circular imports between models.");
			return;
		}

		logger.error("Unresolved LMF imports:");
		for (final var unresolvedImport : unresolved)
		{
			final var model = unresolvedImport.model();
			final var missing = unresolvedImport.missingImports();
			if (missing.isEmpty())
			{
				logger.error(" - {} (from {}): imports {} but resolution did not converge. Check for circular imports "
							 + "or typos.",
							 model.qualifiedName().orElse("[unknown]"),
							 model.file(),
							 describeImports(model.imports()));
			}
			else
			{
				logger.error(" - {} (from {}): missing imports {}. Available models: {}",
							 model.qualifiedName().orElse("[unknown]"),
							 model.file(),
							 missing,
							 availableNames);
			}
		}
	}

	private static List<UnresolvedImport> findUnresolved(final List<ModelInspectionResult> inspections)
	{
		final Map<String, ModelInspectionResult> byName = new HashMap<>();
		for (final var inspection : inspections)
		{
			inspection.qualifiedName().ifPresent(qn -> byName.put(qn, inspection));
		}

		final Set<String> availableNames = new HashSet<>(byName.keySet());
		final List<ModelInspectionResult> candidates = new ArrayList<>();
		final List<UnresolvedImport> unresolved = new ArrayList<>();

		for (final var inspection : inspections)
		{
			if (inspection.qualifiedName().isEmpty()) continue;

			final var missing = inspection.imports()
										  .stream()
										  .filter(importName -> availableNames.contains(importName) == false)
										  .toList();
			if (missing.isEmpty())
			{
				candidates.add(inspection);
			}
			else
			{
				unresolved.add(new UnresolvedImport(inspection, missing));
			}
		}

		final Set<String> resolved = new LinkedHashSet<>();
		boolean progressed;
		do
		{
			progressed = false;
			for (final var candidate : new ArrayList<>(candidates))
			{
				if (resolved.contains(candidate.qualifiedName().orElse(""))) continue;
				if (resolved.containsAll(candidate.imports()))
				{
					candidates.remove(candidate);
					candidate.qualifiedName().ifPresent(resolved::add);
					progressed = true;
				}
			}
		}
		while (progressed);

		for (final var candidate : candidates)
		{
			unresolved.add(new UnresolvedImport(candidate, List.of()));
		}

		return unresolved;
	}

	private static String describeImports(final List<String> imports)
	{
		return imports.isEmpty() ? "[none]" : imports.toString();
	}

	private record UnresolvedImport(ModelInspectionResult model, List<String> missingImports)
	{}
}
