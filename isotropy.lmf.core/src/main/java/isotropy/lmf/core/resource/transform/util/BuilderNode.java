package isotropy.lmf.core.resource.transform.util;

import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.model.IFeaturedObject;
import isotropy.lmf.core.resource.transform.feature.IFeatureResolution;

import java.util.List;

public final class BuilderNode<T extends LMObject>
{
	public final String name;
	public final List<String> words;
	public final IFeaturedObject.Builder<T> builder;
	public final Group<?> group;

	public List<IFeatureResolution> featureResolutions;
	public T buildObject = null;

	public BuilderNode(final String name,
					   final List<String> words,
					   final IFeaturedObject.Builder<T> builder,
					   final Group<?> group)
	{
		this.name = name;
		this.words = words;
		this.builder = builder;
		this.group = group;
	}

	public void setFeatureResolutions(List<? extends IFeatureResolution> featureResolutions)
	{
		this.featureResolutions = List.copyOf(featureResolutions);
	}

	public T build()
	{
		if (buildObject == null)
		{
			for (final var reolution : featureResolutions)
			{
				reolution.pushValue(builder);
			}
			buildObject = builder.build();
		}
		return buildObject;
	}
}
