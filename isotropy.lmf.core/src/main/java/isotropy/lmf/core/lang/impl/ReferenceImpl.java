package isotropy.lmf.core.lang.impl;

import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.model.FeatureMap;
import isotropy.lmf.core.model.FeaturedObject;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ReferenceImpl<UnaryType extends LMObject> extends FeaturedObject implements Reference<UnaryType>
{
	public static final FeatureMap<Function<Reference<?>, Object>> GET_MAP = new FeatureMap<>(

			List.of(new FeatureMap.FeatureTuple<>(Features.group, Reference::group),
					new FeatureMap.FeatureTuple<>(Features.parameters, Reference::parameters)));

	private final Supplier<Concept<UnaryType>> group;
	private final List<Supplier<? extends Concept<?>>> parameters;

	public ReferenceImpl(final Supplier<Concept<UnaryType>> group,
						 final List<Supplier<? extends Concept<?>>> parameters)
	{
		this.group = group != null ? group : () -> null;
		this.parameters = parameters;
	}

	@Override
	public Concept<UnaryType> group()
	{
		return group.get();
	}

	@Override
	public List<? extends Concept<?>> parameters()
	{
		return parameters.stream()
						 .map(Supplier::get)
						 .toList();
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
