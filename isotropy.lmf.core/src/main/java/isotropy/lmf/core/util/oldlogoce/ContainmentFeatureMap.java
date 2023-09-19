package isotropy.lmf.core.util.oldlogoce;

import isotropy.lmf.core.api.feature.RawFeature;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.util.ModelUtils;

import java.util.ArrayList;
import java.util.List;

public final class ContainmentFeatureMap
{
	private final List<ClassFeatureEntry> map = new ArrayList<>();

	public List<RawFeature<?, ?>> features(final Group<?> group)
	{
		final var res = get(group);
		if (res == null)
		{
			return computeNew(group);
		}
		else
		{
			return res;
		}
	}

	private List<RawFeature<?, ?>> get(final Group<?> group)
	{
		for (final var entry : map)
		{
			if (entry.group == group)
			{
				return entry.relations;
			}
		}

		return null;
	}

	private List<RawFeature<?, ?>> computeNew(final Group<?> group)
	{
		final var containmentFeatures = ModelUtils.streamContainmentFeatures(group).toList();
		map.add(new ClassFeatureEntry(group, containmentFeatures));
		return containmentFeatures;
	}

	private record ClassFeatureEntry(Group<?> group, List<RawFeature<?, ?>> relations) {}
}
