package isotropy.lmf.core.resource.transform.word.resolver;

import isotropy.lmf.core.lang.Feature;

public abstract class AbstractResolver<T, F extends Feature<T, ?>> implements IWordResolver<T>
{
	protected final F feature;

	protected AbstractResolver(final F feature)
	{
		this.feature = feature;
	}

	@Override
	public final boolean match(final String featureName)
	{
		return feature.name()
					  .equals(featureName);
	}
}
