package isotropy.lmf.core.lang.impl;

import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.model.FeatureMap;
import isotropy.lmf.core.model.FeaturedObject;
import isotropy.lmf.core.model.RawFeature;

import java.util.List;
import java.util.function.Function;

public final class RelationImpl<UnaryType extends LMObject, EffectiveType> extends FeaturedObject implements Relation<UnaryType, EffectiveType>
{
	public static final FeatureMap<Function<Relation<?, ?>, Object>> GET_MAP = new FeatureMap<>(

			List.of(new FeatureMap.FeatureTuple<>(Features.name, Named::name),
					new FeatureMap.FeatureTuple<>(Features.immutable, Relation::immutable),
					new FeatureMap.FeatureTuple<>(Features.many, Relation::many),
					new FeatureMap.FeatureTuple<>(Features.mandatory, Relation::mandatory),
					new FeatureMap.FeatureTuple<>(Features.reference, Relation::reference),
					new FeatureMap.FeatureTuple<>(Features.contains, Relation::contains)));

	private final String name;
	private final boolean immutable;
	private final boolean many;
	private final boolean mandatory;
	private final Reference<UnaryType> reference;
	private final boolean contains;
	private final RawFeature<UnaryType, EffectiveType> rawFeature;

	public RelationImpl(final String name,
						final boolean immutable,
						final boolean many,
						final boolean mandatory,
						final Reference<UnaryType> reference,
						final boolean contains,
						final RawFeature<UnaryType, EffectiveType> rawFeature)
	{
		this.name = name;
		this.immutable = immutable;
		this.many = many;
		this.mandatory = mandatory;
		this.reference = reference;
		this.contains = contains;
		this.rawFeature = rawFeature;

		setContainer(reference, Features.reference);
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
	public Reference<UnaryType> reference()
	{
		return reference;
	}

	@Override
	public boolean contains()
	{
		return contains;
	}

	@Override
	public RawFeature<UnaryType, EffectiveType> rawFeature()
	{
		return rawFeature;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(final Feature<?, T> feature)
	{
		return (T) GET_MAP.get(feature.rawFeature())
						  .apply(this);
	}

	@Override
	public <T> void set(final Feature<?, T> feature, final T value)
	{
		throw new IllegalAccessError("Group " + Generic.class.getSimpleName() + " is immutable.");
	}

	@Override
	public Group<?> lmGroup()
	{
		return LMCoreDefinition.Groups.RELATION;
	}
}
