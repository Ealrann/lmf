package org.logoce.lmf.model.resource.linking.tree;

import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.resource.interpretation.PFeature;
import org.logoce.lmf.model.resource.linking.FeatureResolution;
import org.logoce.lmf.model.resource.linking.feature.NodeLinker;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.resource.transform.ResolutionAttempt;
import org.logoce.lmf.model.util.tree.AbstractTree;

import java.util.List;
import java.util.function.Function;

public final class LinkNodeFull<T extends LMObject, I extends PNode> extends AbstractTree<LinkNodeFull<?, I>> implements
																											  LinkNodeInternal<T, I>
{
	private final LinkInfo<T, I> info;
	private final IFeaturedObject.Builder<T> builder;

	private List<ResolutionAttempt> tokenResolutions;
	private T builtObject = null;

	public LinkNodeFull(final LinkInfo<T, I> info,
						final LinkNodeFull<?, I> parent,
						final Function<LinkNodeFull<?, I>, List<LinkNodeFull<?, I>>> childrenBuilder)
	{
		super(parent, childrenBuilder);

		this.info = info;
		this.builder = info.modelGroup().builder();
	}

	@Override
	public void resolveTokens(final NodeLinker nodeLinker)
	{
		this.tokenResolutions = nodeLinker.resolve(this, true);
	}

	@Override
	public List<ResolutionAttempt> tokenResolutions()
	{
		return tokenResolutions;
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

			for (final var tokenResolution : tokenResolutions)
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

	private void injectContainment(final LinkNodeFull<?, I> child)
	{
		child.injectContainmentInto(builder);
	}

	private void injectContainmentInto(IFeaturedObject.Builder<?> otherBuilder)
	{
		otherBuilder.push(containingRelation(), this::build);
	}

	private void pushValue(final FeatureResolution resolution)
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
