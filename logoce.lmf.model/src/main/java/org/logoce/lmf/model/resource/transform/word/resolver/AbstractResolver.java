package org.logoce.lmf.model.resource.transform.word.resolver;

import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.resource.transform.node.TreeBuilderNode;
import org.logoce.lmf.model.resource.transform.word.IFeatureResolution;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractResolver<T, F extends Feature<T, ?>> implements ITokenResolver<T>
{
	protected final F feature;

	protected AbstractResolver(final F feature)
	{
		this.feature = feature;
	}

	@Override
	public final boolean match(final String featureName)
	{
		return feature.name().equals(featureName);
	}

	@Override
	public IFeatureResolution resolveOrThrow(TreeBuilderNode<?> node, List<String> values)
	{
		return resolve(node, values).orElseThrow();
	}

	@Override
	public Optional<? extends IFeatureResolution> resolve(TreeBuilderNode<?> node, List<String> values)
	{
		if (values.size() > 1 && !feature.many())
		{
			return Optional.empty();
		}

		final List<IFeatureResolution> resolutions = new ArrayList<>();
		for (var value : values)
		{
			final var resolution = internalResolve(node, value);
			if (resolution.isEmpty())
			{
				return Optional.empty();
			}
			else
			{
				resolutions.add(resolution.get());
			}
		}

		if (resolutions.size() > 1) return Optional.of(new MultipleResolution(resolutions));
		else return Optional.of(resolutions.get(0));
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
