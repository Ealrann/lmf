package org.logoce.lmf.core.loader.api.loader.linking;

import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.loader.feature.AbstractResolver;
import org.logoce.lmf.core.loader.feature.RelationResolver;
import org.logoce.lmf.core.loader.feature.reference.ModelReferenceResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * Tooling helper to extract resolved targets from relation reference resolutions.
 * <p>
 * This hides the internal resolver implementations (dynamic/local vs. static/model),
 * and exposes a stable view suitable for CLI/LSP consumers.
 */
public final class RelationReferences
{
	private RelationReferences()
	{
	}

	public record Resolved(String raw, LMObject target)
	{
	}

	public static List<Resolved> resolved(final ResolutionAttempt<Relation<?, ?, ?, ?>> attempt)
	{
		if (attempt == null || attempt.resolution() == null || attempt.exception() != null)
		{
			return List.of();
		}

		final var values = attempt.feature() != null ? attempt.feature().values() : List.<String>of();
		return resolve(attempt.resolution(), values);
	}

	private static List<Resolved> resolve(final FeatureResolution<?> resolution, final List<String> rawValues)
	{
		if (resolution == null)
		{
			return List.of();
		}

		if (resolution instanceof AbstractResolver.MultipleResolution<?> multiple)
		{
			final var nested = multiple.resolutions();
			final var out = new ArrayList<Resolved>(nested.size());
			for (int i = 0; i < nested.size(); i++)
			{
				final String raw = rawValues != null && i < rawValues.size() ? rawValues.get(i) : null;
				out.addAll(resolveSingle(nested.get(i), raw));
			}
			return List.copyOf(out);
		}

		final String raw = rawValues != null && !rawValues.isEmpty() ? rawValues.getFirst() : null;
		return List.copyOf(resolveSingle(resolution, raw));
	}

	private static List<Resolved> resolveSingle(final FeatureResolution<?> resolution, final String raw)
	{
		if (resolution == null)
		{
			return List.of();
		}

		final LMObject target = extractTarget(resolution);
		if (target == null)
		{
			return List.of();
		}
		return List.of(new Resolved(raw, target));
	}

	private static LMObject extractTarget(final FeatureResolution<?> resolution)
	{
		if (resolution instanceof RelationResolver.DynamicReferenceResolution<?> dyn)
		{
			try
			{
				return dyn.linkNode().build();
			}
			catch (Exception e)
			{
				return null;
			}
		}
		if (resolution instanceof ModelReferenceResolver.StaticResolution staticResolution)
		{
			return staticResolution.value();
		}
		return null;
	}
}
