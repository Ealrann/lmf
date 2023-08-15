package isotropy.lmf.core.resource.transform.node;

import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.model.IFeaturedObject;
import isotropy.lmf.core.resource.transform.feature.IFeatureResolution;

import java.util.List;
import java.util.stream.Stream;

public final class BuilderNode<T extends LMObject>
{
	public final String containmentName;
	public final List<String> words;
	public final IFeaturedObject.Builder<T> builder;
	public final Group<?> group;

	private Stream<? extends IFeatureResolution> wordsResolutions;
	public List<? extends IFeatureResolution> childrenResolutions;
	public T buildObject = null;

	public BuilderNode(final String containmentName,
					   final List<String> words,
					   final IFeaturedObject.Builder<T> builder,
					   final Group<?> group)
	{
		this.containmentName = containmentName;
		this.words = words;
		this.builder = builder;
		this.group = group;
	}

	public void setResolutions(Stream<? extends IFeatureResolution> wordsResolutions,
							   Stream<? extends IFeatureResolution> childrenResolutions)
	{
		this.wordsResolutions = wordsResolutions;
		this.childrenResolutions = childrenResolutions.toList();
	}

	public T build()
	{
		if (buildObject == null)
		{
			childrenResolutions.forEach(r -> r.pushValue(builder));
			wordsResolutions.forEach(r -> r.pushValue(builder));
			buildObject = builder.build();
		}
		return buildObject;
	}
}
