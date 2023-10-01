package org.logoce.lmf.model.resource.transform.node;

import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.resource.parsing.ParsedToken;
import org.logoce.lmf.model.resource.transform.word.IFeatureResolution;
import org.logoce.lmf.model.util.AbstractTree;

import java.util.List;

public final class TreeBuilderNode<T extends LMObject> extends AbstractTree<BuilderNodeInfo<T>, TreeBuilderNode<T>>
{
	private final IFeaturedObject.Builder<T> builder;
	private List<? extends IFeatureResolution> tokenResolutions;

	public T builtObject = null;

	public TreeBuilderNode(final BuildInfo<BuilderNodeInfo<T>, TreeBuilderNode<T>> info)
	{
		super(info);

		builder = data().modelGroup().builder();

		children().forEach(this::injectContainment);
	}

	private void injectContainment(final TreeBuilderNode<T> child)
	{
		builder.push(child.containingRelation(), child::build);
	}

	public Relation<T, ?> containingRelation()
	{
		return data().containingRelation();
	}

	public List<ParsedToken> tokens()
	{
		return data().tokens();
	}

	public ModelGroup<T> modelGroup()
	{
		return data().modelGroup();
	}

	public void setResolutions(List<? extends IFeatureResolution> tokenResolutions)
	{
		this.tokenResolutions = tokenResolutions;
	}

	public T build()
	{
		if (builtObject == null)
		{
			tokenResolutions.forEach(r -> r.pushValue(builder));
			builtObject = builder.build();
		}
		return builtObject;
	}
}
