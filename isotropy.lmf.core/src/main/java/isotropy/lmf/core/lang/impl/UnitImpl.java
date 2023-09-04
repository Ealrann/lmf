package isotropy.lmf.core.lang.impl;

import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.LMCoreDefinition;
import isotropy.lmf.core.lang.Primitive;
import isotropy.lmf.core.lang.Unit;
import isotropy.lmf.core.model.FeatureGetter;
import isotropy.lmf.core.model.FeatureSetter;
import isotropy.lmf.core.model.FeaturedObject;

public final class UnitImpl<T> extends FeaturedObject implements Unit<T>
{
	public static final FeatureGetter<Unit<?>> GET_MAP = new FeatureGetter.Builder<Unit<?>>()

			.add(Features.name, Unit::name)
			.add(Features.matcher, Unit::matcher)
			.add(Features.defaultValue, Unit::defaultValue)
			.add(Features.primitive, Unit::primitive)
			.add(Features.extractor, Unit::extractor)
			.build();

	private final String name;
	private final String matcher;
	private final String defaultValue;
	private final Primitive primitive;
	private final String extractor;

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
	protected FeatureGetter<?> getterMap()
	{
		return GET_MAP;
	}

	@Override
	protected FeatureSetter<?> setterMap()
	{
		return null;
	}

	@Override
	public Group<?> lmGroup()
	{
		return LMCoreDefinition.Groups.UNIT;
	}
}
