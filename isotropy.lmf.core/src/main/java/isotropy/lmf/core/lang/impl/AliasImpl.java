package isotropy.lmf.core.lang.impl;

import isotropy.lmf.core.lang.*;

import java.util.List;
import java.util.function.Function;

public final class AliasImpl implements Alias
{
	private final String name;
	private final List<String> words;

	private LMObject container;

	public AliasImpl(final String name, final List<String> words)
	{
		this.name = name;
		this.words = words;
	}

	@Override
	public String name()
	{
		return name;
	}

	@Override
	public List<String> words()
	{
		return words;
	}

	@Override
	public <T> T get(final Feature<?, T> feature)
	{
		return featureGetter(feature).apply(this);
	}

	@SuppressWarnings("unchecked")
	private static <T> Function<Alias, T> featureGetter(Feature<?, T> f)
	{
		if (f == LMCoreDefinition.Features.ALIAS.name)
		{
			return (Function<Alias, T>) (Function<Alias, ?>) Alias::name;
		}
		else if (f == LMCoreDefinition.Features.ALIAS.words)
		{
			return (Function<Alias, T>) (Function<Alias, ?>) Alias::words;
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
		return LMCoreDefinition.Groups.ALIAS;
	}

	@Override
	public LMObject lContainer()
	{
		return container;
	}

	@Override
	public void lContainer(final LMObject container)
	{
		this.container = container;
	}
}
