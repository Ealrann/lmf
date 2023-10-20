package org.logoce.lmf.model.resource.linking.tree;

import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.resource.interpretation.PFeature;
import org.logoce.lmf.model.resource.linking.FeatureLink;
import org.logoce.lmf.model.resource.linking.ModelGroup;
import org.logoce.lmf.model.resource.linking.feature.NodeLinker;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.util.tree.AbstractTree;

import java.util.List;
import java.util.function.Function;

public final class ResolvedNode<T extends LMObject, I extends PNode> extends AbstractTree<LinkNode<I>> implements
																									   LinkNode<I>
{
	private final LinkNodeInfo.Resolved<T, I> data;
	private List<NodeLinker.ResolutionAptempt> tokenResolutions;

	public T builtObject = null;

	public ResolvedNode(final LinkNode<I> parent,
						final LinkNodeInfo.Resolved<T, I> info,
						final Function<LinkNode<I>, List<LinkNode<I>>> childrenBuilder)
	{
		super(parent, childrenBuilder);
		this.data = info;
	}

	public Relation<T, ?> containingRelation()
	{
		return data.containingRelation();
	}

	public List<PFeature> features()
	{
		return data.features();
	}

	public Group<?> group()
	{
		return data.modelGroup().group();
	}

	@Override
	public I pNode()
	{
		return data.pNode();
	}

	public void resolve(final NodeLinker nodeLinker)
	{
		this.tokenResolutions = nodeLinker.link(this);
	}

	public List<NodeLinker.ResolutionAptempt> getTokenResolutions()
	{
		return tokenResolutions;
	}

	public T build()
	{
		if (builtObject == null)
		{
			final var runner = BuildRunner.from(data.modelGroup());
			streamChildren().filter(ResolvedNode.class::isInstance)
							.map(ResolvedNode.class::cast)
							.forEach(runner::injectContainment);

			for (final var tokenResolution : tokenResolutions)
			{
				if (tokenResolution.resolution() != null)
				{
					runner.pushValue(tokenResolution.resolution());
				}
				if (tokenResolution.exception() != null)
				{
					tokenResolution.exception().printStackTrace();
				}
			}

			builtObject = runner.build();
		}
		return builtObject;
	}

	private record BuildRunner<T extends LMObject>(IFeaturedObject.Builder<T> builder)
	{
		public static <T extends LMObject> BuildRunner<T> from(ModelGroup<T> modelGroup)
		{
			return new BuildRunner<>(modelGroup.builder());
		}

		public void injectContainment(final ResolvedNode<T, ?> child)
		{
			builder.push(child.containingRelation(), child::build);
		}

		public void pushValue(final FeatureLink resolution)
		{
			resolution.pushValue(builder);
		}

		public T build()
		{
			return builder.build();
		}
	}
}
