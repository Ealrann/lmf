package org.logoce.lmf.core.util.oldlogoce;

import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.util.ModelUtil;

import java.util.ArrayList;
import java.util.List;

public final class ContainmentFeatureMap
{
	private final List<ClassFeatureEntry> map = new ArrayList<>();

	public List<Relation<?, ?, ?, ?>> features(final Group<?> group)
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

	private List<Relation<?, ?, ?, ?>> get(final Group<?> group)
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

	private List<Relation<?, ?, ?, ?>> computeNew(final Group<?> group)
	{
		final var containmentFeatures = ModelUtil.streamContainmentFeatures(group).toList();
		map.add(new ClassFeatureEntry(group, containmentFeatures));
		return containmentFeatures;
	}

	private record ClassFeatureEntry(Group<?> group, List<Relation<?, ?, ?, ?>> relations) {}
}
