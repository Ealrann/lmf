package org.logoce.lmf.core.util.oldlogoce;

import org.logoce.lmf.extender.api.IAdapter;
import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.api.util.ModelExplorer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class CompositeModelExplorer
{
	private final List<ModelExplorer> explorers;

	public CompositeModelExplorer(final List<List<Relation<?, ?, ?, ?>>> featureLists)
	{
		this.explorers = List.copyOf(buildExplorers(featureLists));
	}

	private static List<ModelExplorer> buildExplorers(final List<List<Relation<?, ?, ?, ?>>> featureLists)
	{
		final List<ModelExplorer> explorers = new ArrayList<>();
		for (final var features : featureLists)
		{
			explorers.add(new ModelExplorer(features));
		}
		return explorers;
	}

	public <T extends LMObject> List<T> explore(LMObject root, Class<T> targetClass)
	{
		return stream(root, targetClass).toList();
	}

	public <T extends LMObject> Stream<T> stream(LMObject root, Class<T> targetClass)
	{
		return stream(root).map(targetClass::cast);
	}

	public <T extends IAdapter> List<T> exploreAdapt(LMObject root, Class<T> adapterType)
	{
		return streamAdapt(root, adapterType).toList();
	}

	public <T extends IAdapter> Stream<T> streamAdapt(LMObject root, Class<T> adapterType)
	{
		return stream(root).map(e -> e.adapt(adapterType)).filter(Objects::nonNull);
	}

	public <T extends IAdapter> List<T> exploreAdaptNotNull(LMObject root, Class<T> adapterType)
	{
		return streamAdaptNotNull(root, adapterType).toList();
	}

	public <T extends IAdapter> Stream<T> streamAdaptNotNull(LMObject root, Class<T> adapterType)
	{
		return stream(root).map(e -> e.adaptNotNull(adapterType));
	}

	private Stream<LMObject> stream(LMObject root)
	{
		return explorers.stream().flatMap(e -> e.stream(root));
	}
}
