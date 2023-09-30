package org.logoce.lmf.model.resource.transform.word.resolver;

import org.logoce.lmf.model.resource.transform.node.ParsedToken;
import org.logoce.lmf.model.resource.transform.node.TreeBuilderNode;
import org.logoce.lmf.model.resource.transform.word.IFeatureResolution;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class TokenResolver
{
	private final List<ITokenResolver<?>> availableResolvers;

	public TokenResolver(final List<? extends ITokenResolver<?>> tokenResolvers)
	{
		this.availableResolvers = List.copyOf(tokenResolvers);
	}

	public List<IFeatureResolution> resolve(final TreeBuilderNode<?> node)
	{
		final var runner = new TokenResolverRunner(availableResolvers, node);
		final var tokenResolutions = resolveTokens(node.tokens());
		final List<IFeatureResolution> resolutions = new ArrayList<>();

		resolutions.addAll(runner.filterNameValueResolvers(tokenResolutions.nameValueTokens));
		resolutions.addAll(runner.filterSimpleResolvers(tokenResolutions.simpleTokens));

		return resolutions;
	}

	private static TokenResolverRunner.TokensResolution resolveTokens(List<ParsedToken> tokens)
	{
		final List<ParsedToken> simpleTokens = new ArrayList<>();
		final List<TokenResolverRunner.NameValues> nameValueTokens = new ArrayList<>();

		for (final var token : tokens)
		{
			if (token.name().isPresent())
			{
				nameValueTokens.add(new TokenResolverRunner.NameValues(token.name().get(), token.values()));
			}
			else
			{
				simpleTokens.add(token);
			}
		}
		return new TokenResolverRunner.TokensResolution(simpleTokens, nameValueTokens);
	}

	private static class TokenResolverRunner
	{
		private final LinkedList<ITokenResolver<?>> availableResolvers;
		private final TreeBuilderNode<?> node;

		TokenResolverRunner(final List<ITokenResolver<?>> tokenResolvers, final TreeBuilderNode<?> node)
		{
			this.availableResolvers = new LinkedList<>(tokenResolvers);
			this.node = node;
		}

		public List<IFeatureResolution> filterNameValueResolvers(final List<NameValues> tokens)
		{
			final List<IFeatureResolution> resolutions = new ArrayList<>();
			for (final var token : tokens)
			{
				resolutions.add(findMandatory(r -> r.match(token.name()),
											  r -> r.resolve(node, token.values()),
											  token.name()));
			}
			return resolutions;
		}

		public List<IFeatureResolution> filterSimpleResolvers(final List<ParsedToken> simpleTokens)
		{
			final List<IFeatureResolution> resolutions = new ArrayList<>();
			final List<ParsedToken> notFound = new ArrayList<>();
			for (final var token : simpleTokens)
			{
				final var resolved = findOptional(r -> r.match(token.firstToken()),
												  r -> r.resolve(node, List.of("true")));
				resolved.ifPresentOrElse(resolutions::add, () -> notFound.add(token));
			}
			for (final var token : notFound)
			{
				resolutions.add(findMandatory(r -> true, r -> r.resolve(node, token.values()), token.firstToken()));
			}

			return resolutions;
		}

		private IFeatureResolution findMandatory(final Predicate<ITokenResolver<?>> filter,
												 final Function<ITokenResolver<?>, Optional<? extends IFeatureResolution>> resolver,
												 final String message)
		{
			return findOptional(filter, resolver).orElseThrow(() -> new NoSuchElementException(message));
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

		record NameValues(String name, List<String> values)
		{}

		record TokensResolution(List<ParsedToken> simpleTokens, List<NameValues> nameValueTokens) {}
	}
}
