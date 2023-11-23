package org.logoce.lmf.model.resource.linking.tree;

import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.resource.interpretation.PFeature;
import org.logoce.lmf.model.resource.linking.linker.NodeLinker;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.resource.transform.ResolutionAttempt;
import org.logoce.lmf.model.util.tree.AbstractLazyMappedTree;
import org.logoce.lmf.model.util.tree.StructuredTree;

import java.util.List;
import java.util.function.Function;

public final class LinkNodePartial<T extends LMObject, I extends PNode> extends AbstractLazyMappedTree<LinkNodePartial<?, I>> implements
																															  LinkNodeInternal<T, I, LinkNodePartial<?, I>>
{
	private final LinkInfo<T, I> info;

	private final List<ResolutionAttempt<Attribute<?, ?>>> attributeResolutions;

	public <Y extends StructuredTree<Y>> LinkNodePartial(final LinkInfo<T, I> info,
														 final Y inputNode,
														 final List<ResolutionAttempt<Attribute<?, ?>>> attributeResolutions,
														 final Function<Y, LinkNodePartial<?, I>> mapper)
	{
		super(inputNode, mapper);

		this.info = info;
		this.attributeResolutions = attributeResolutions;
	}

	@Override
	public void resolveReferences(final NodeLinker nodeLinker)
	{
	}

	@Override
	public List<ResolutionAttempt<Attribute<?, ?>>> attributeResolutions()
	{
		return attributeResolutions;
	}

	@Override
	public List<ResolutionAttempt<Relation<?, ?>>> relationResolutions()
	{
		return List.of();
	}

	@Override
	public I pNode()
	{
		return info.pNode();
	}

	@Override
	public LinkNodePartial<?, I> root()
	{
		return super.root();
	}

	@Override
	public T build()
	{
		throw new IllegalStateException("Cannot build a partial Model");
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
