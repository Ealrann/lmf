package org.logoce.lmf.model.resource.linking.tree;

import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.resource.interpretation.PFeature;
import org.logoce.lmf.model.resource.linking.FeatureLink;
import org.logoce.lmf.model.resource.linking.exception.LinkException;
import org.logoce.lmf.model.resource.linking.feature.NodeLinker;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.util.tree.AbstractTree;
import org.logoce.lmf.model.util.tree.NavigableTree;
import org.logoce.lmf.model.util.tree.StructuredTree;

import java.util.List;
import java.util.function.Function;

public final class LinkNode<I extends PNode> extends AbstractTree<LinkNode<I>> implements NavigableTree<LinkNode<I>>,
																						  StructuredTree<LinkNode<I>>
{
	private final I pNode;

	private final Structure<?> structure;
	private final LinkException exception;

	public LinkNode(final LinkNodeInfo.Resolved<?, I> info,
					final LinkNode<I> parent,
					final Function<LinkNode<I>, List<LinkNode<I>>> childrenBuilder)
	{
		super(parent, childrenBuilder);

		this.pNode = info.pNode();
		this.structure = new Structure<>(info, this);
		this.exception = null;
	}

	public LinkNode(final LinkException exception, final I pNode, final LinkNode<I> parent)
	{
		super(parent);

		this.pNode = pNode;
		this.structure = null;
		this.exception = exception;
	}

	public Structure<?> linkStructure()
	{
		return structure;
	}

	public LinkException exception()
	{
		return exception;
	}

	public List<PFeature> features()
	{
		return structure != null ? structure.features() : List.of();
	}

	public I pNode()
	{
		return pNode;
	}

	public LMObject build()
	{
		return structure != null ? structure.build() : null;
	}

	public static final class Structure<T extends LMObject>
	{
		private final LinkNodeInfo.Resolved<T, ?> info;
		private final IFeaturedObject.Builder<T> builder;

		private final LinkNode<?> linkNode;

		private T builtObject = null;
		private List<NodeLinker.ResolutionAptempt> tokenLinks;

		public Structure(LinkNodeInfo.Resolved<T, ?> info, LinkNode<?> linkNode)
		{
			this.info = info;
			this.linkNode = linkNode;
			this.builder = info.modelGroup().builder();
		}

		public void injectContainment(final LinkNode<?> child)
		{
			child.structure.injectContainmentInto(builder);
		}

		private void injectContainmentInto(IFeaturedObject.Builder<?> otherBuilder)
		{
			otherBuilder.push(containingRelation(), this::build);
		}

		public Relation<T, ?> containingRelation()
		{
			return info.containingRelation();
		}

		public List<PFeature> features()
		{
			return info.features();
		}

		public Group<?> group()
		{
			return info.modelGroup().group();
		}

		public LinkNode<?> linkNode()
		{
			return linkNode;
		}

		public void linkTokens(final NodeLinker nodeLinker)
		{
			this.tokenLinks = nodeLinker.link(this);
		}

		public T build()
		{
			if (builtObject == null)
			{
				linkNode.streamChildren().forEach(this::injectContainment);

				for (final var tokenResolution : tokenLinks)
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

				builtObject = builder.build();
			}
			return builtObject;
		}

		public void pushValue(final FeatureLink resolution)
		{
			resolution.pushValue(builder);
		}
	}
}
