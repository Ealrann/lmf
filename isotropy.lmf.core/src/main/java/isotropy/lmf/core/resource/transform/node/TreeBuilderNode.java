package isotropy.lmf.core.resource.transform.node;

import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.lang.Relation;
import isotropy.lmf.core.api.model.IFeaturedObject;
import isotropy.lmf.core.resource.transform.word.IFeatureResolution;
import isotropy.lmf.core.resource.util.AbstractTree;

import java.util.List;

public final class TreeBuilderNode<T extends LMObject> extends AbstractTree<BuilderNodeInfo<T>, TreeBuilderNode<T>>
{
	private final IFeaturedObject.Builder<T> builder;
	private List<? extends IFeatureResolution> wordsResolutions;

	public T builtObject = null;

	public TreeBuilderNode(BuildInfo<BuilderNodeInfo<T>, TreeBuilderNode<T>> info)
	{
		super(info);

		builder = data().modelGroup()
						.builder();

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

	public List<String> words()
	{
		return data().words();
	}

	public ModelGroup<T> modelGroup()
	{
		return data().modelGroup();
	}

	public void setResolutions(List<? extends IFeatureResolution> wordsResolutions)
	{
		this.wordsResolutions = wordsResolutions;
	}

	public T build()
	{
		if (builtObject == null)
		{
			wordsResolutions.forEach(r -> r.pushValue(builder));
			builtObject = builder.build();
		}
		return builtObject;
	}
}
