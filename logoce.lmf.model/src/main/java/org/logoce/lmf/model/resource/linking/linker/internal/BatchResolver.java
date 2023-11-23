package org.logoce.lmf.model.resource.linking.linker.internal;

import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.resource.interpretation.PFeature;
import org.logoce.lmf.model.resource.interpretation.PNominalGroup;
import org.logoce.lmf.model.resource.linking.FeatureResolution;
import org.logoce.lmf.model.resource.transform.ResolutionAttempt;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

public final class BatchResolver<T extends Feature<?, ?>>
{
	private final TokenResolver<T, ?> runner;
	private final List<ResolutionAttempt<T>> resolutions = new ArrayList<>();
	private final List<PFeature> notFound = new ArrayList<>();

	public BatchResolver(final TokenResolver<T, ?> runner)
	{
		this.runner = runner;
	}

	public List<ResolutionAttempt<T>> resolve(final Stream<PFeature> featureStream)
	{
		final var it = featureStream.iterator();
		while (it.hasNext())
		{
			final var token = it.next();

			try
			{
				final var res = obviousResolveAttempt(token);
				if (res.isPresent())
				{
					resolutions.add(new ResolutionAttempt<>(token, res.get(), null));
				}
				else
				{
					notFound.add(token);
				}
			}
			catch (NoSuchElementException exception)
			{
				resolutions.add(new ResolutionAttempt<>(token, null, exception));
			}
		}
		for (final var token : notFound)
		{
			try
			{
				final var res = valueResolution(token);
				resolutions.add(new ResolutionAttempt<>(token, res, null));
			}
			catch (NoSuchElementException exception)
			{
				resolutions.add(new ResolutionAttempt<>(token, null, exception));
			}
		}

		return List.copyOf(resolutions);
	}

	private Optional<FeatureResolution<T>> obviousResolveAttempt(final PFeature token)
	{
		if (token.name().isPresent())
		{
			return Optional.of(nameResolution(token));
		}
		else
		{
			return resolveBoolean(token);
		}
	}



	private FeatureResolution<T> nameResolution(final PFeature token)
	{
		assert token.name().isPresent();
		final var tokenName = token.name().get();
		return runner.findMandatory(r -> r.match(tokenName),
									token.values(),
									() -> new NoSuchElementException("Cannot resolve named Token " + tokenName));
	}

	private FeatureResolution<T> valueResolution(final PFeature token)
	{
		return runner.findMandatory(r -> true,
									token.values(),
									() -> new NoSuchElementException("Cannot resolve value Token " +
																	 token.firstToken()));
	}

	private Optional<FeatureResolution<T>> resolveBoolean(final PNominalGroup token)
	{
		return runner.findOptional(r -> r.match(token.firstToken()), List.of("true"));
	}
}
