package isotropy.lmf.core.resource.transform.word.resolver;

import isotropy.lmf.core.lang.Feature;
import isotropy.lmf.core.api.model.IFeaturedObject;
import isotropy.lmf.core.resource.transform.node.TreeBuilderNode;
import isotropy.lmf.core.resource.transform.word.IFeatureResolution;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

	@Override
	public IFeatureResolution resolveOrThrow(TreeBuilderNode<?> node, String value)
	{
		return resolve(node, value).orElseThrow();
	}

	@Override
	public Optional<? extends IFeatureResolution> resolve(TreeBuilderNode<?> node, String value)
	{
		final int indexEqual = value.indexOf(',');
		if (indexEqual > -1)
		{
			if (!feature.many())
			{
				return Optional.empty();
			}

			final List<IFeatureResolution> resolutions = new ArrayList<>();
			final var split = value.split(",");
			for (final var val : split)
			{
				final var res = resolve(node, val);
				if (res.isEmpty())
				{
					return Optional.empty();
				}
				else
				{
					resolutions.add(res.get());
				}
			}

			return Optional.of(new MultipleResolution(resolutions));
		}

		return internalResolve(node, value);
	}

	protected abstract Optional<? extends IFeatureResolution> internalResolve(TreeBuilderNode<?> node, String value);

	public static final class MultipleResolution implements IFeatureResolution
	{
		private final List<IFeatureResolution> resolutions;

		public MultipleResolution(final List<IFeatureResolution> resolutions)
		{
			this.resolutions = resolutions;
		}

		@Override
		public void pushValue(final IFeaturedObject.Builder<?> builder)
		{
			resolutions.forEach(r -> r.pushValue(builder));
		}
	}
}
