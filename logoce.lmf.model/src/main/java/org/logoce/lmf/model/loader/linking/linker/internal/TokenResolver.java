package org.logoce.lmf.model.loader.linking.linker.internal;

import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.loader.linking.FeatureResolution;
import org.logoce.lmf.model.loader.linking.feature.AbstractResolver;
import org.logoce.lmf.model.loader.linking.feature.ITokenResolver;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class TokenResolver<T extends Feature<?, ?, ?, ?>, R extends AbstractResolver<T>>
{
	private final LinkedList<R> availableResolvers;
	private final List<R> allResolvers;
	private final BiFunction<R, List<String>, Optional<FeatureResolution<T>>> resolverUsage;

	public TokenResolver(final List<R> resolvers,
						 final BiFunction<R, List<String>, Optional<FeatureResolution<T>>> resolverUsage)
	{
		this.availableResolvers = new LinkedList<>(resolvers);
		this.allResolvers = List.copyOf(resolvers);
		this.resolverUsage = resolverUsage;
	}

	public FeatureResolution<T> findMandatory(final Predicate<ITokenResolver> filter,
											  final List<String> values,
											  final Supplier<NoSuchElementException> message)
	{
		final var optional = findOptional(filter, values);
		if (optional.isPresent())
		{
			return optional.get();
		}

		final var e = message.get();
		final var baseMessage = e.getMessage();

		// If a resolver matching the filter used to exist but is no longer
		// available, interpret this as a duplicate usage of the same feature
		// (for example, an alias that already consumed the resolver plus an
		// explicit assignment such as 'immutable=true').
		final boolean existed = allResolvers.stream().anyMatch(filter::test);
		if (existed && baseMessage != null && !baseMessage.isBlank())
		{
			final int lastSpace = baseMessage.lastIndexOf(' ');
			final String tokenName = lastSpace >= 0 && lastSpace + 1 < baseMessage.length()
									 ? baseMessage.substring(lastSpace + 1)
									 : baseMessage;
			throw new NoSuchElementException("Feature \"" + tokenName + "\" is already defined");
		}

		throw e;
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
