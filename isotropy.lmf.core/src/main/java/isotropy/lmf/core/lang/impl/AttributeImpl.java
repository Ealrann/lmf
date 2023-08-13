package isotropy.lmf.core.lang.impl;

import isotropy.lmf.core.lang.*;

import java.util.function.Function;

public final class AttributeImpl<UnaryType, EffectiveType> implements Attribute<UnaryType, EffectiveType>
{
	private final String name;
	private final boolean immutable;
	private final boolean many;
	private final boolean mandatory;
	private final Datatype<UnaryType> datatype;

	private LMObject container;

	public AttributeImpl(final String name,
						 final boolean immutable,
						 final boolean many,
						 final boolean mandatory,
						 final Datatype<UnaryType> datatype)
	{
		this.name = name;
		this.immutable = immutable;
		this.many = many;
		this.mandatory = mandatory;
		this.datatype = datatype;
	}

	@Override
	public String name()
	{
		return name;
	}

	@Override
	public boolean immutable()
	{
		return immutable;
	}

	@Override
	public boolean many()
	{
		return many;
	}

	@Override
	public boolean mandatory()
	{
		return mandatory;
	}

	@Override
	public Datatype<UnaryType> datatype()
	{
		return datatype;
	}

	@Override
	public <T> T get(final Feature<?, T> feature)
	{
		return featureGetter(feature).apply(this);
	}

	@SuppressWarnings("unchecked")
	private static <T> Function<Attribute<?, ?>, T> featureGetter(Feature<?, T> f)
	{
		if (f == LMCoreDefinition.Features.ATTRIBUTE.name)
		{
			return (Function<Attribute<?, ?>, T>) (Function<Attribute<?, ?>, ?>) Attribute::name;
		}
		else if (f == LMCoreDefinition.Features.ATTRIBUTE.immutable)
		{
			return (Function<Attribute<?, ?>, T>) (Function<Attribute<?, ?>, ?>) Attribute::immutable;
		}
		else if (f == LMCoreDefinition.Features.ATTRIBUTE.many)
		{
			return (Function<Attribute<?, ?>, T>) (Function<Attribute<?, ?>, ?>) Attribute::many;
		}
		else if (f == LMCoreDefinition.Features.ATTRIBUTE.mandatory)
		{
			return (Function<Attribute<?, ?>, T>) (Function<Attribute<?, ?>, ?>) Attribute::mandatory;
		}
		else if (f == LMCoreDefinition.Features.ATTRIBUTE.datatype)
		{
			return (Function<Attribute<?, ?>, T>) (Function<Attribute<?, ?>, ?>) Attribute::datatype;
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
		return LMCoreDefinition.Groups.ATTRIBUTE;
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
