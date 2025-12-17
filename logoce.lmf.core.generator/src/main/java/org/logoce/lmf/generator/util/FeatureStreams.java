package org.logoce.lmf.generator.util;

import org.logoce.lmf.core.lang.Feature;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.api.util.ModelUtil;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.stream.Stream;

public final class FeatureStreams
{
	private FeatureStreams()
	{
	}

	public static Stream<Feature<?, ?, ?, ?>> distinctFeatures(final Group<?> group)
	{
		final Set<Feature<?, ?, ?, ?>> seen = Collections.newSetFromMap(new IdentityHashMap<>());
		return ModelUtil.streamAllFeatures(group)
						.filter(feature -> seen.add((Feature<?, ?, ?, ?>) feature));
	}
}
