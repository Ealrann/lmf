package isotropy.lmf.core.lang.builder;

import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.lang.impl.GroupReferenceImpl;
import isotropy.lmf.core.model.FeatureInserter;
import isotropy.lmf.core.model.RelationLazyInserter;

import java.util.function.Supplier;

public final class GroupReferenceBuilder<UnaryType extends LMObject> implements GroupReference.Builder<UnaryType>
{
	public static final FeatureInserter<GroupReferenceBuilder<?>> FEATURE_INSERTER =
			new FeatureInserter.Builder<GroupReferenceBuilder<?>>()

					.build();

	private static final RelationLazyInserter<GroupReferenceBuilder<?>> BUILDER_INSERTER =
			new RelationLazyInserter.Builder<GroupReferenceBuilder<?>>()

					.add(LMCoreDefinition.Features.GROUP_REFERENCE.group, GroupReferenceBuilder::_group)
					.add(LMCoreDefinition.Features.GROUP_REFERENCE.genericParameter, GroupReferenceBuilder::genericParameter)
					.add(LMCoreDefinition.Features.GROUP_REFERENCE.directParameter, GroupReferenceBuilder::directParameter)
					.build();

	private Supplier<Group<UnaryType>> group;

	private Supplier<Generic> genericParameter;

	private Supplier<Group<?>> directParameter;

	@Override
	public GroupReference<UnaryType> build()
	{
		return new GroupReferenceImpl<>(group, genericParameter, directParameter);
	}

	@Override
	public GroupReferenceBuilder<UnaryType> group(final Supplier<Group<UnaryType>> group)
	{
		this.group = group;
		return this;
	}

	@SuppressWarnings("unchecked")
	public GroupReferenceBuilder<UnaryType> _group(Supplier<? extends Group<?>> group)
	{
		this.group = (Supplier<Group<UnaryType>>) group;
		return this;
	}

	@Override
	public GroupReferenceBuilder<UnaryType> genericParameter(final Supplier<Generic> genericParameter)
	{
		this.genericParameter = genericParameter;
		return this;
	}

	@Override
	public GroupReferenceBuilder<UnaryType> directParameter(final Supplier<Group<?>> directParameter)
	{
		this.directParameter = directParameter;
		return this;
	}

	@Override
	public <AttributeType> void push(final Attribute<AttributeType, ?> feature, final AttributeType value)
	{
		FEATURE_INSERTER.push(this, feature, value);
	}

	@Override
	public <RelationType extends LMObject> void push(final Relation<RelationType, ?> relation,
													 final Supplier<RelationType> supplier)
	{
		BUILDER_INSERTER.push(this, relation, supplier);
	}
}
