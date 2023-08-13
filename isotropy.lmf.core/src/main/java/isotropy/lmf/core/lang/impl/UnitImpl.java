package isotropy.lmf.core.lang.impl;

import isotropy.lmf.core.lang.*;

import java.util.function.Function;

public final class UnitImpl<T> implements Unit<T>
{
	private final String name;
	private final String matcher;
	private final String defaultValue;
	private final Primitive primitive;
	private final String extractor;

	private LMObject container;

	public UnitImpl(final String name,
					final String matcher,
					final String defaultValue,
					final Primitive primitive,
					final String extractor)
	{
		this.name = name;
		this.matcher = matcher;
		this.defaultValue = defaultValue;
		this.primitive = primitive;
		this.extractor = extractor;
	}

	@Override
	public String name()
	{
		return name;
	}

	@Override
	public String matcher()
	{
		return matcher;
	}

	@Override
	public String defaultValue()
	{
		return defaultValue;
	}

	@Override
	public Primitive primitive()
	{
		return primitive;
	}

	@Override
	public String extractor()
	{
		return extractor;
	}

	@Override
	public <T> T get(final Feature<?, T> feature)
	{
		return featureGetter(feature).apply(this);
	}

	@SuppressWarnings("unchecked")
	private static <T> Function<Unit<?>, T> featureGetter(Feature<?, T> f)
	{
		if (f == LMCoreDefinition.Features.UNIT.name)
		{
			return (Function<Unit<?>, T>) (Function<Unit<?>, ?>) Unit::name;
		}
		else if (f == LMCoreDefinition.Features.UNIT.matcher)
		{
			return (Function<Unit<?>, T>) (Function<Unit<?>, ?>) Unit::matcher;
		}
		else if (f == LMCoreDefinition.Features.UNIT.defaultValue)
		{
			return (Function<Unit<?>, T>) (Function<Unit<?>, ?>) Unit::defaultValue;
		}
		else if (f == LMCoreDefinition.Features.UNIT.primitive)
		{
			return (Function<Unit<?>, T>) (Function<Unit<?>, ?>) Unit::primitive;
		}
		else if (f == LMCoreDefinition.Features.UNIT.extractor)
		{
			return (Function<Unit<?>, T>) (Function<Unit<?>, ?>) Unit::extractor;
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
		return LMCoreDefinition.Groups.UNIT;
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
