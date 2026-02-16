package org.logoce.lmf.core.loader.linking;

import org.logoce.lmf.core.lang.Feature;
import org.logoce.lmf.core.loader.api.loader.linking.FeatureResolution;
import org.logoce.lmf.core.loader.api.loader.linking.InvalidEnumLiteralException;
import org.logoce.lmf.core.loader.api.loader.linking.InvalidUnitLiteralException;
import org.logoce.lmf.core.loader.feature.AbstractResolver;
import org.logoce.lmf.core.loader.feature.ITokenResolver;

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
											  final String tokenName,
											  final Supplier<NoSuchElementException> message)
	{
		final var optional = findOptional(filter, values);
		if (optional.isPresent())
		{
			return optional.get();
		}

		final var e = message.get();

		// If a resolver matching the filter used to exist but is no longer
		// available, interpret this as a duplicate usage of the same feature
		// (for example, an alias that already consumed the resolver plus an
		// explicit assignment such as 'immutable=true').
		final boolean existed = allResolvers.stream().anyMatch(filter::test);
		final boolean stillAvailable = availableResolvers.stream().anyMatch(filter::test);
		if (existed && !stillAvailable)
		{
			final var effectiveName = tokenName == null || tokenName.isBlank() ? "unknown" : tokenName;
			throw new NoSuchElementException("Feature \"" + effectiveName + "\" is already defined");
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

	public FeatureResolution<T> findMandatoryLenient(final Predicate<ITokenResolver> filter,
													 final List<String> values,
													 final String tokenName,
													 final Supplier<NoSuchElementException> message)
	{
		final var optional = findOptionalLenient(filter, values);
		if (optional.isPresent())
		{
			return optional.get();
		}

		final var e = message.get();

		final boolean existed = allResolvers.stream().anyMatch(filter::test);
		final boolean stillAvailable = availableResolvers.stream().anyMatch(filter::test);
		if (existed && !stillAvailable)
		{
			final var effectiveName = tokenName == null || tokenName.isBlank() ? "unknown" : tokenName;
			throw new NoSuchElementException("Feature \"" + effectiveName + "\" is already defined");
		}

		throw e;
	}

	public Optional<FeatureResolution<T>> findOptionalLenient(final Predicate<ITokenResolver> filter,
															  final List<String> values)
	{
		final var it = availableResolvers.iterator();

		while (it.hasNext())
		{
			final var currentResolver = it.next();
			if (!filter.test(currentResolver))
			{
				continue;
			}

			final Optional<FeatureResolution<T>> resolution;
			try
			{
				resolution = resolverUsage.apply(currentResolver, values);
			}
			catch (InvalidEnumLiteralException | InvalidUnitLiteralException e)
			{
				continue;
			}

			if (resolution.isPresent())
			{
				it.remove();
				return resolution;
			}
		}
		return Optional.empty();
	}
}
