package isotropy.lmf.core.lang.impl;

import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.model.FeatureGetter;
import isotropy.lmf.core.model.FeatureSetter;
import isotropy.lmf.core.model.FeaturedObject;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class ReferenceImpl<UnaryType extends LMObject> extends FeaturedObject implements Reference<UnaryType>
{
	public static final FeatureGetter<Reference<?>> GET_MAP = new FeatureGetter.Builder<Reference<?>>()

			.add(Features.group, Reference::group)
			.add(Features.parameters, Reference::parameters)
			.build();

	private final Supplier<Concept<UnaryType>> group;
	private final List<Supplier<Concept<?>>> parameters;

	public ReferenceImpl(final Supplier<Concept<UnaryType>> group, final List<Supplier<Concept<?>>> parameters)
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
	public List<Concept<?>> parameters()
	{
		return parameters.stream()
						 .map(Supplier::get)
						 .collect(Collectors.toUnmodifiableList());
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
		return LMCoreDefinition.Groups.RELATION;
	}
}
