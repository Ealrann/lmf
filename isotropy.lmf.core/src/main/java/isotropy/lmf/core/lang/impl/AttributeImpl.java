package isotropy.lmf.core.lang.impl;

import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.model.FeaturedObject;
import isotropy.lmf.core.model.RawFeature;

import java.util.List;
import java.util.function.Function;

public final class AttributeImpl<UnaryType, EffectiveType> extends FeaturedObject implements Attribute<UnaryType, EffectiveType>
{
	private final String name;
	private final boolean immutable;
	private final boolean many;
	private final boolean mandatory;
	private final Datatype<UnaryType> datatype;
	private final List<? extends Generic<?>> parameters;
	private final RawFeature<UnaryType, EffectiveType> rawFeature;

	public AttributeImpl(final String name,
						 final boolean immutable,
						 final boolean many,
						 final boolean mandatory,
						 final Datatype<UnaryType> datatype,
						 final List<? extends Generic<?>> parameters,
						 final RawFeature<UnaryType, EffectiveType> rawFeature)
	{
		this.name = name;
		this.immutable = immutable;
		this.many = many;
		this.mandatory = mandatory;
		this.datatype = datatype;
		this.parameters = parameters;
		this.rawFeature = rawFeature;

		setContainer(datatype, Features.datatype);
		setContainer(parameters, Features.parameters);
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
	public List<? extends Generic<?>> parameters()
	{
		return parameters;
	}

	@Override
	public RawFeature<UnaryType, EffectiveType> rawFeature()
	{
		return rawFeature;
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
		else if (f == LMCoreDefinition.Features.ATTRIBUTE.parameters)
		{
			return (Function<Attribute<?, ?>, T>) (Function<Attribute<?, ?>, ?>) Attribute::parameters;
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
}
