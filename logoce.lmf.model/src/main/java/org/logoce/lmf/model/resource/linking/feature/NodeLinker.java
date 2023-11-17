package org.logoce.lmf.model.resource.linking.feature;

import org.logoce.lmf.model.resource.interpretation.PFeature;
import org.logoce.lmf.model.resource.interpretation.PNominalGroup;
import org.logoce.lmf.model.resource.linking.FeatureResolution;
import org.logoce.lmf.model.resource.linking.tree.LinkNodeInternal;
import org.logoce.lmf.model.resource.transform.ResolutionAttempt;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class NodeLinker
{
	private final List<AttributeResolver<?>> attributeResolvers;
	private final List<ReferenceResolver<?>> referenceResolvers;

	public NodeLinker(final List<? extends ITokenResolver<?>> tokenResolvers)
	{
		attributeResolvers = tokenResolvers.stream()
										   .filter(AttributeResolver.class::isInstance)
										   .map(r -> (AttributeResolver<?>) r)
										   .collect(Collectors.toUnmodifiableList());

		referenceResolvers = tokenResolvers.stream()
										   .filter(ReferenceResolver.class::isInstance)
										   .map(r -> (ReferenceResolver<?>) r)
										   .collect(Collectors.toUnmodifiableList());
	}

	public List<ResolutionAttempt> resolve(final LinkNodeInternal<?, ?> node, final boolean resolveRelations)
	{
		final var runner = new TokenResolverRunner(attributeResolvers, referenceResolvers, node);
		final List<ResolutionAttempt> resolutions = new ArrayList<>();
		final List<PFeature> notFound = new ArrayList<>();

		for (final var token : node.features())
		{
			if (token.isRelation() && !resolveRelations)
			{
				continue;
			}

			try
			{
				runner.nameResolution(token)
					  .or(() -> runner.resolveBoolean(token))
					  .ifPresentOrElse(r -> resolutions.add(new ResolutionAttempt(token, r, null)),
									   () -> notFound.add(token));
			}
			catch (NoSuchElementException exception)
			{
				resolutions.add(new ResolutionAttempt(token, null, exception));
			}
		}
		for (final var token : notFound)
		{
			try
			{
				final var res = runner.valueResolution(token);
				resolutions.add(new ResolutionAttempt(token, res, null));
			}
			catch (NoSuchElementException exception)
			{
				resolutions.add(new ResolutionAttempt(token, null, exception));
			}
		}

		return List.copyOf(resolutions);
	}

	private static final class TokenResolverRunner
	{
		private final LinkedList<AttributeResolver<?>> availableAttributesResolvers;
		private final LinkedList<ReferenceResolver<?>> availableReferenceResolvers;
		private final LinkNodeInternal<?, ?> node;

		public TokenResolverRunner(final List<AttributeResolver<?>> attributeResolvers,
								   final List<ReferenceResolver<?>> referenceResolvers,
								   final LinkNodeInternal<?, ?> node)
		{
			this.availableAttributesResolvers = new LinkedList<>(attributeResolvers);
			this.availableReferenceResolvers = new LinkedList<>(referenceResolvers);
			this.node = node;
		}

		public Optional<FeatureResolution> nameResolution(final PFeature token)
		{
			return resolveFromName(token).or(() -> resolveBoolean(token));
		}

		public FeatureResolution valueResolution(final PFeature token)
		{
			return findMandatory(r -> true,
								 r -> r.resolve(node, token.values()),
								 () -> new NoSuchElementException("Cannot resolve value Token " + token.firstToken()),
								 token.isRelation());
		}

		private Optional<FeatureResolution> resolveFromName(final PFeature token)
		{
			if (token.name().isPresent())
			{
				final var tokenName = token.name().get();
				return Optional.of(findMandatory(r -> r.match(tokenName),
												 r -> r.resolve(node, token.values()),
												 () -> new NoSuchElementException("Cannot resolve named Token " +
																				  tokenName),
												 token.isRelation()));
			}
			else
			{
				return Optional.empty();
			}
		}

		private Optional<FeatureResolution> resolveBoolean(final PNominalGroup token)
		{
			return findOptional(r -> r.match(token.firstToken()), r -> r.resolve(node, List.of("true")), false);
		}

		private FeatureResolution findMandatory(final Predicate<ITokenResolver<?>> filter,
												final Function<ITokenResolver<?>, Optional<? extends FeatureResolution>> resolver,
												final Supplier<NoSuchElementException> message,
												final boolean isRelation)
		{
			return findOptional(filter, resolver, isRelation).orElseThrow(message);
		}

		private Optional<FeatureResolution> findOptional(final Predicate<ITokenResolver<?>> filter,
														 final Function<ITokenResolver<?>, Optional<? extends FeatureResolution>> resolver,
														 final boolean isRelation)
		{
			final var it = isRelation
						   ? availableReferenceResolvers.iterator()
						   : availableAttributesResolvers.iterator();

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
