package isotropy.lmf.core.lang.impl;

import isotropy.lmf.core.lang.Enum;
import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.model.FeaturedObject;

import java.util.List;
import java.util.function.Function;

public final class EnumImpl<T> extends FeaturedObject implements Enum<T>
{
	private final String name;
	private final List<String> literals;

	public EnumImpl(final String name, final List<String> literals)
	{
		this.name = name;
		this.literals = literals;
	}

	@Override
	public String name()
	{
		return name;
	}

	@Override
	public List<String> literals()
	{
		return literals;
	}

	@Override
	public <T> T get(final Feature<?, T> feature)
	{
		return featureGetter(feature).apply(this);
	}

	@SuppressWarnings("unchecked")
	private static <T> Function<Enum<?>, T> featureGetter(Feature<?, T> f)
	{
		if (f == LMCoreDefinition.Features.ENUM.name)
		{
			return (Function<Enum<?>, T>) (Function<Enum<?>, ?>) Enum::name;
		}
		else if (f == LMCoreDefinition.Features.ENUM.literals)
		{
			return (Function<Enum<?>, T>) (Function<Enum<?>, ?>) Enum::literals;
		}
		else
		{
			throw new IllegalArgumentException();
		}
	}

	@Override
	public <T> void set(final Feature<?, T> feature, final T value)
	{
		throw new IllegalAccessError("Group " + Alias.class.getSimpleName() + " is immutable.");
	}

	@Override
	public Group<?> lmGroup()
	{
		return LMCoreDefinition.Groups.ENUM;
	}
}
