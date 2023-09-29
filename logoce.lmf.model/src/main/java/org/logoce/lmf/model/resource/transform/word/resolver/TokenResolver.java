package org.logoce.lmf.model.resource.transform.word.resolver;

import org.logoce.lmf.model.resource.transform.node.ParsedToken;
import org.logoce.lmf.model.resource.transform.node.TreeBuilderNode;
import org.logoce.lmf.model.resource.transform.word.IFeatureResolution;

import java.util.*;
import java.util.function.Function;

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

		public List<IFeatureResolution> filterNameValueResolvers(final List<NameValues> nameValueTokens)
		{
			final List<IFeatureResolution> resolutions = new ArrayList<>();
			for (final var nameValueToken : nameValueTokens)
			{
				final var resolved = findMandatory(r -> r.match(nameValueToken.name()) ? Optional.of(r.resolveOrThrow(
						node,
						nameValueToken.values())) : Optional.empty(), nameValueToken.name());
				resolutions.add(resolved);
			}

			return resolutions;
		}

		public List<IFeatureResolution> filterSimpleResolvers(final List<ParsedToken> simpleTokens)
		{
			final List<IFeatureResolution> resolutions = new ArrayList<>();
			final List<ParsedToken> notFound = new ArrayList<>();
			for (final var token : simpleTokens)
			{
				final var resolved = findOptional(r -> r.match(token.firstToken())
													   ? Optional.of(r.resolveOrThrow(node,
																					  List.of("true")))
													   : Optional.empty());
				resolved.ifPresentOrElse(resolutions::add, () -> notFound.add(token));
			}
			for (final var token : notFound)
			{
				resolutions.add(findMandatory(r -> r.resolve(node, token.values()), token.firstToken()));
			}

			return resolutions;
		}

		private IFeatureResolution findMandatory(final Function<ITokenResolver<?>, Optional<? extends IFeatureResolution>> resolver,
												 final String message)
		{
			return findOptional(resolver).orElseThrow(() -> new NoSuchElementException(message));
		}

		private Optional<IFeatureResolution> findOptional(final Function<ITokenResolver<?>, Optional<? extends IFeatureResolution>> resolver)
		{
			final var it = availableResolvers.iterator();
			while (it.hasNext())
			{
				final var currentResolver = it.next();
				final var resolution = resolver.apply(currentResolver);
				if (resolution.isPresent())
				{
					it.remove();
					return Optional.of(resolution.get());
				}
			}
			return Optional.empty();
		}

		record NameValues(String name, List<String> values)
		{}

		record TokensResolution(List<ParsedToken> simpleTokens, List<NameValues> nameValueTokens) {}
	}
}
