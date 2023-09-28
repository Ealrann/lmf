package org.logoce.lmf.model.util.oldlogoce;

import org.logoce.lmf.extender.api.IAdapter;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.util.ModelUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class ModelExplorer implements IModelExplorer
{
	private final int parentHeight;
	private final List<RawFeature<?, ?>> references;

	public ModelExplorer(List<RawFeature<?, ?>> references)
	{
		this(0, references);
	}

	public ModelExplorer(int parentHeight, List<RawFeature<?, ?>> references)
	{
		this.parentHeight = parentHeight;
		this.references = List.copyOf(references);
	}

	@Override
	public <T extends LMObject> List<T> explore(LMObject root, Class<T> targetClass)
	{
		return stream(root, targetClass).toList();
	}

	@Override
	public List<LMObject> explore(LMObject root)
	{
		return stream(root).toList();
	}

	@Override
	public <T extends LMObject> Stream<T> stream(LMObject root, Class<T> targetClass)
	{
		return stream(root).map(targetClass::cast);
	}

	@Override
	public <T extends IAdapter> List<T> exploreAdapt(LMObject root, Class<T> adapterType)
	{
		return streamAdapt(root, adapterType).toList();
	}

	@Override
	public <T extends IAdapter> List<T> exploreAdaptGeneric(LMObject root, Class<? extends IAdapter> adapterType)
	{
		return this.<T>streamAdaptGeneric(root, adapterType).toList();
	}

	@Override
	public <T extends IAdapter> Stream<T> streamAdapt(LMObject root, Class<T> adapterType)
	{
		return stream(root).map(e -> e.adapt(adapterType)).filter(Objects::nonNull);
	}

	@Override
	public <T extends IAdapter> Stream<T> streamAdaptGeneric(LMObject root, Class<? extends IAdapter> adapterType)
	{
		return stream(root).map(e -> e.<T>adaptGeneric(adapterType)).filter(Objects::nonNull);
	}

	@Override
	public <T extends IAdapter> List<T> exploreAdaptNotNull(LMObject root, Class<T> adapterType)
	{
		return streamAdaptNotNull(root, adapterType).toList();
	}

	@Override
	public <T extends IAdapter> Stream<T> streamAdaptNotNull(LMObject root, Class<T> adapterType)
	{
		return stream(root).map(e -> e.adaptNotNull(adapterType));
	}

	@Override
	public Stream<LMObject> stream(LMObject source)
	{
		final var root = parent(source);
		var stream = Stream.of(root);
		for (final var feature : references)
		{
			stream = stream.flatMap(e -> extractList(e, feature));
		}
		return stream;
	}

	private LMObject parent(LMObject source)
	{
		for (int i = 0; i < parentHeight; i++)
		{
			source = source.lmContainer();
		}
		return source;
	}

	@SuppressWarnings("unchecked")
	private static Stream<LMObject> extractList(LMObject object, final RawFeature<?, ?> reference)
	{
		final var val = getValue(object, reference);
		if (val instanceof List)
		{
			return ((List<LMObject>) val).stream();
		}
		else
		{
			return Stream.ofNullable((LMObject) val);
		}
	}

	private static Object getValue(LMObject target, final RawFeature<?, ?> reference)
	{
		final var found = ModelUtils.streamContainmentFeatures(target.lmGroup()).anyMatch(f -> f == reference);
		if (found)
		{
			return target.get(reference.featureSupplier().get());
		}
		else
		{
			return null;
		}
	}
}
