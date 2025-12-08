package org.logoce.lmf.generator.util;

import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.util.ModelUtil;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.stream.Stream;

public final class FeatureStreams
{
	private FeatureStreams()
	{
	}

	public static Stream<Feature<?, ?>> distinctFeatures(final Group<?> group)
	{
		final Set<Feature<?, ?>> seen = Collections.newSetFromMap(new IdentityHashMap<>());
		return ModelUtil.streamAllFeatures(group)
						.filter(feature -> seen.add(feature));
	}
}

