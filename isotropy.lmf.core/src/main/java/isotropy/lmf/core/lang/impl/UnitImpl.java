package isotropy.lmf.core.lang.impl;

import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.model.FeatureMap;

import java.util.List;
import java.util.function.Function;

public final class UnitImpl<T> implements Unit<T>
{
	public static final FeatureMap<Function<Unit<?>, Object>> FEATURE_GETTER = new FeatureMap<>(

			List.of(new FeatureMap.FeatureTuple<>(LMCoreDefinition.Features.Unit_name, Unit::name),
					new FeatureMap.FeatureTuple<>(LMCoreDefinition.Features.Unit_matcher, Unit::matcher),
					new FeatureMap.FeatureTuple<>(LMCoreDefinition.Features.Unit_defaultValue, Unit::defaultValue),
					new FeatureMap.FeatureTuple<>(LMCoreDefinition.Features.Unit_primitive, Unit::primitive),
					new FeatureMap.FeatureTuple<>(LMCoreDefinition.Features.Unit_extractor, Unit::extractor)));

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
		return (T) FEATURE_GETTER.get(feature).apply(this);
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
