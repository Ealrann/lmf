package org.logoce.lmf.model.resource.linking.feature;

import org.logoce.lmf.model.resource.interpretation.PFeature;
import org.logoce.lmf.model.resource.interpretation.PNominalGroup;
import org.logoce.lmf.model.resource.linking.FeatureLink;
import org.logoce.lmf.model.resource.linking.tree.LinkNode;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class NodeLinker
{
	private final List<ITokenResolver<?>> availableResolvers;

	public NodeLinker(final List<? extends ITokenResolver<?>> tokenResolvers)
	{
		this.availableResolvers = List.copyOf(tokenResolvers);
	}

	public List<ResolutionAptempt> link(final LinkNode<?, ?> node)
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

		return List.copyOf(resolutions);
	}

	public record ResolutionAptempt(PFeature feature, FeatureLink resolution, NoSuchElementException exception) {}

	private static final class TokenResolverRunner
	{
		private final LinkedList<ITokenResolver<?>> availableResolvers;
		private final LinkNode<?, ?> node;

		public TokenResolverRunner(final List<ITokenResolver<?>> tokenResolvers, final LinkNode<?, ?> node)
		{
			this.availableResolvers = new LinkedList<>(tokenResolvers);
			this.node = node;
		}

		public Optional<FeatureLink> nameResolution(final PFeature token)
		{
			return resolveFromName(token).or(() -> resolveBoolean(token));
		}

		public FeatureLink valueResolution(final PFeature token)
		{
			return findMandatory(r -> true,
								 r -> r.resolve(node, token.values()),
								 () -> new NoSuchElementException("Cannot resolve value Token " + token.firstToken()));
		}

		private Optional<FeatureLink> resolveFromName(final PFeature token)
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

		private Optional<FeatureLink> resolveBoolean(final PNominalGroup token)
		{
			return findOptional(r -> r.match(token.firstToken()), r -> r.resolve(node, List.of("true")));
		}

		private FeatureLink findMandatory(final Predicate<ITokenResolver<?>> filter,
										  final Function<ITokenResolver<?>, Optional<? extends FeatureLink>> resolver,
										  final Supplier<NoSuchElementException> message)
		{
			return findOptional(filter, resolver).orElseThrow(message);
		}

		private Optional<FeatureLink> findOptional(final Predicate<ITokenResolver<?>> filter,
												   final Function<ITokenResolver<?>, Optional<? extends FeatureLink>> resolver)
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
