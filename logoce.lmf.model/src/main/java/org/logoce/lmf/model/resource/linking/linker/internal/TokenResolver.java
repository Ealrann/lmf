package org.logoce.lmf.model.resource.linking.linker.internal;

import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.resource.linking.FeatureResolution;
import org.logoce.lmf.model.resource.linking.feature.AbstractResolver;
import org.logoce.lmf.model.resource.linking.feature.ITokenResolver;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class TokenResolver<T extends Feature<?, ?>, R extends AbstractResolver<T>>
{
	private final LinkedList<R> availableResolvers;
	private final BiFunction<R, List<String>, Optional<FeatureResolution<T>>> resolverUsage;

	public TokenResolver(final List<R> resolvers,
						 BiFunction<R, List<String>, Optional<FeatureResolution<T>>> resolverUsage)
	{
		this.availableResolvers = new LinkedList<>(resolvers);
		this.resolverUsage = resolverUsage;
	}

	public FeatureResolution<T> findMandatory(final Predicate<ITokenResolver> filter,
											  final List<String> values,
											  final Supplier<NoSuchElementException> message)
	{
		return findOptional(filter, values).orElseThrow(message);
	}

	public Optional<FeatureResolution<T>> findOptional(final Predicate<ITokenResolver> filter,
													   final List<String> values)
	{
		final var it = availableResolvers.iterator();

		while (it.hasNext())
		{
			final var currentResolver = it.next();
			if (filter.test(currentResolver))
			{

				final var resolution = resolverUsage.apply(currentResolver, values);
				if (resolution.isPresent())
				{
					it.remove();
					return resolution;
				}
			}
		}
		return Optional.empty();
	}
}
