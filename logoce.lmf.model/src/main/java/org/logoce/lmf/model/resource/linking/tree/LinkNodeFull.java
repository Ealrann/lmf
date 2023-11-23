package org.logoce.lmf.model.resource.linking.tree;

import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.resource.interpretation.PFeature;
import org.logoce.lmf.model.resource.linking.FeatureResolution;
import org.logoce.lmf.model.resource.linking.linker.NodeLinker;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.resource.transform.ResolutionAttempt;
import org.logoce.lmf.model.util.tree.AbstractTree;

import java.util.List;
import java.util.function.Function;

public final class LinkNodeFull<T extends LMObject, I extends PNode> extends AbstractTree<LinkNodeFull<?, I>> implements
																											  LinkNodeInternal<T, I, LinkNodeFull<?,I>>
{
	private final LinkInfo<T, I> info;
	private final IFeaturedObject.Builder<T> builder;

	private final List<ResolutionAttempt<Attribute<?, ?>>> attributeResolutions;
	private List<ResolutionAttempt<Relation<?, ?>>> relationResolutions;
	private T builtObject = null;

	public LinkNodeFull(final LinkInfo<T, I> info,
						final LinkNodeFull<?, I> parent,
						final List<ResolutionAttempt<Attribute<?, ?>>> attributeResolutions,
						final Function<LinkNodeFull<?, I>, List<LinkNodeFull<?, I>>> childrenBuilder)
	{
		super(parent, childrenBuilder);

		this.info = info;
		this.builder = info.modelGroup().builder();
		this.attributeResolutions = List.copyOf(attributeResolutions);
	}

	@Override
	public void resolveReferences(final NodeLinker nodeLinker)
	{
		this.relationResolutions = nodeLinker.resolveRelations(this);
	}

	@Override
	public List<ResolutionAttempt<Attribute<?, ?>>> attributeResolutions()
	{
		return attributeResolutions;
	}

	@Override
	public List<ResolutionAttempt<Relation<?, ?>>> relationResolutions()
	{
		return relationResolutions;
	}

	@Override
	public I pNode()
	{
		return info.pNode();
	}

	@Override
	public LinkNodeFull<?, I> root()
	{
		return super.root();
	}

	@Override
	public T build()
	{
		if (builtObject == null)
		{
			streamChildren().forEach(this::injectContainment);

			attributeResolutions.forEach(this::install);
			relationResolutions.forEach(this::install);

			builtObject = builder.build();
		}
		return builtObject;
	}

	private void install(final ResolutionAttempt<?> tokenResolution)
	{
		if (tokenResolution.resolution() != null)
		{
			pushValue(tokenResolution.resolution());
		}
		if (tokenResolution.exception() != null)
		{
			tokenResolution.exception().printStackTrace();
		}
	}

	private void injectContainment(final LinkNodeFull<?, I> child)
	{
		child.injectContainmentInto(builder);
	}

	private void injectContainmentInto(IFeaturedObject.Builder<?> otherBuilder)
	{
		otherBuilder.push(containingRelation(), this::build);
	}

	private void pushValue(final FeatureResolution<?> resolution)
	{
		resolution.pushValue(builder);
	}

	@Override
	public Relation<T, ?> containingRelation()
	{
		return info.containingRelation();
	}

	@Override
	public List<PFeature> features()
	{
		return info.features();
	}

	@Override
	public Group<T> group()
	{
		return info.modelGroup().group();
	}
}
