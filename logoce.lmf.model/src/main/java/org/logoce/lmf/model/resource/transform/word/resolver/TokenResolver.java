package org.logoce.lmf.model.resource.transform.word.resolver;

import org.logoce.lmf.model.resource.interpretation.PFeature;
import org.logoce.lmf.model.resource.interpretation.PNominalGroup;
import org.logoce.lmf.model.resource.linking.LinkerNode;
import org.logoce.lmf.model.resource.transform.word.IFeatureResolution;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class TokenResolver
{
	private final List<ITokenResolver<?>> availableResolvers;

	public TokenResolver(final List<? extends ITokenResolver<?>> tokenResolvers)
	{
		this.availableResolvers = List.copyOf(tokenResolvers);
	}

	public List<ResolutionAptempt> resolve(final LinkerNode<?> node)
	{
		final var runner = new TokenResolverRunner(availableResolvers, node);
		final List<ResolutionAptempt> resolutions = new ArrayList<>();
		final List<PFeature> notFound = new ArrayList<>();

		for (final var token : node.features())
		{
			try
			{
				runner.nameResolution(token)
					  .or(() -> runner.resolveBoolean(token))
					  .ifPresentOrElse(r -> resolutions.add(new ResolutionAptempt(token, r, null)),
									   () -> notFound.add(token));
			}
			catch (NoSuchElementException exception)
			{
				resolutions.add(new ResolutionAptempt(token, null, exception));
			}
		}
		for (final var token : notFound)
		{
			try
			{
				final var res = runner.valueResolution(token);
				resolutions.add(new ResolutionAptempt(token, res, null));
			}
			catch (NoSuchElementException exception)
			{
				resolutions.add(new ResolutionAptempt(token, null, exception));
			}
		}

		return resolutions;
	}

	public record ResolutionAptempt(PFeature feature,
									IFeatureResolution resolution,
									NoSuchElementException exception) {}

	private static class TokenResolverRunner
	{
		private final LinkedList<ITokenResolver<?>> availableResolvers;
		private final LinkerNode<?> node;

		public TokenResolverRunner(final List<ITokenResolver<?>> tokenResolvers, final LinkerNode<?> node)
		{
			this.availableResolvers = new LinkedList<>(tokenResolvers);
			this.node = node;
		}

		public Optional<IFeatureResolution> nameResolution(final PFeature token)
		{
			return resolveFromName(token).or(() -> resolveBoolean(token));
		}

		public IFeatureResolution valueResolution(final PFeature token)
		{
			return findMandatory(r -> true,
								 r -> r.resolve(node, token.values()),
								 () -> new NoSuchElementException("Cannot resolve value Token " + token.firstToken()));
		}

		private Optional<IFeatureResolution> resolveFromName(final PFeature token)
		{
			if (token.name().isPresent())
			{
				final var tokenName = token.name().get();
				return Optional.of(findMandatory(r -> r.match(tokenName),
												 r -> r.resolve(node, token.values()),
												 () -> new NoSuchElementException("Cannot resolve named Token " +
																				  tokenName)));
			}
			else
			{
				return Optional.empty();
			}
		}

		private Optional<IFeatureResolution> resolveBoolean(final PNominalGroup token)
		{
			return findOptional(r -> r.match(token.firstToken()), r -> r.resolve(node, List.of("true")));
		}

		private IFeatureResolution findMandatory(final Predicate<ITokenResolver<?>> filter,
												 final Function<ITokenResolver<?>, Optional<? extends IFeatureResolution>> resolver,
												 final Supplier<NoSuchElementException> message)
		{
			return findOptional(filter, resolver).orElseThrow(message);
		}

		private Optional<IFeatureResolution> findOptional(final Predicate<ITokenResolver<?>> filter,
														  final Function<ITokenResolver<?>, Optional<? extends IFeatureResolution>> resolver)
		{
			final var it = availableResolvers.iterator();
			while (it.hasNext())
			{
				final var currentResolver = it.next();
				if (filter.test(currentResolver))
				{
					final var resolution = resolver.apply(currentResolver);
					if (resolution.isPresent())
					{
						it.remove();
						return Optional.of(resolution.get());
					}
				}
			}
			return Optional.empty();
		}
	}
}
