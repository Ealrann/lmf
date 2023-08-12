package isotropy.lmf.core.resource.transform.util;

import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.model.IFeaturedObject;

import java.util.List;

public final class BuilderNode
{
	public final List<String> words;
	public final IFeaturedObject.Builder<?> builder;
	public final Group<?> group;

	public BuilderNode(final List<String> words, final IFeaturedObject.Builder<?> builder, final Group<?> group)
	{
		this.words = words;
		this.builder = builder;
		this.group = group;
	}
}