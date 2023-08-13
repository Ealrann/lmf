package isotropy.lmf.core.lang.impl;

import isotropy.lmf.core.lang.Enum;
import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.model.FeatureMap;

import java.util.List;
import java.util.function.Function;

public final class EnumImpl<T> implements Enum<T>
{
	public static final FeatureMap<Function<Enum<?>, Object>> GET_MAP = new FeatureMap<>(

			List.of(new FeatureMap.FeatureTuple<>(LMCoreFeatures.Enum_name, Named::name),
					new FeatureMap.FeatureTuple<>(LMCoreFeatures.Enum_literals, Enum::literals)));

	private final String name;
	private final List<String> literals;

	private LMObject container;

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

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(final Feature<?, T> feature)
	{
		return (T) GET_MAP.get(feature)
						  .apply(this);
	}

	@Override
	public <T> void set(final Feature<?, T> feature, final T value)
	{
		throw new IllegalAccessError("Group " + Alias.class.getSimpleName() + " is immutable.");
	}

	@Override
	public Group<?> lmGroup()
	{
		return LMCorePackage.Groups.ENUM_GROUP;
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
