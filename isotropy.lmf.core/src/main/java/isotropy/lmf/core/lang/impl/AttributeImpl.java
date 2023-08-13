package isotropy.lmf.core.lang.impl;

import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.model.FeatureMap;

import java.util.List;
import java.util.function.Function;

public final class AttributeImpl<UnaryType, EffectiveType> implements Attribute<UnaryType, EffectiveType>
{
	public static final FeatureMap<Function<Attribute<?, ?>, Object>> GET_MAP = new FeatureMap<>(
			List.of(new FeatureMap.FeatureTuple<>(LMCoreFeatures.Attribute_name, Named::name),
					new FeatureMap.FeatureTuple<>(LMCoreFeatures.Attribute_immutable, Attribute::immutable),
					new FeatureMap.FeatureTuple<>(LMCoreFeatures.Attribute_many, Attribute::many),
					new FeatureMap.FeatureTuple<>(LMCoreFeatures.Attribute_mandatory, Attribute::mandatory),
					new FeatureMap.FeatureTuple<>(LMCoreFeatures.Attribute_datatype, Attribute::datatype)));

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

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(final Feature<?, T> feature)
	{
		return (T) GET_MAP.get(feature).apply(this);
	}

	@Override
	public <T> void set(final Feature<?, T> feature, final T value)
	{
		throw new IllegalAccessError("Group " + Alias.class.getSimpleName() + " is immutable.");
	}

	@Override
	public Group<?> lmGroup()
	{
		return LMCorePackage.Groups.ATTRIBUTE_GROUP;
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
