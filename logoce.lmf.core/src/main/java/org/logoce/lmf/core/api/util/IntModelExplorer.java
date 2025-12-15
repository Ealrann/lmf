package org.logoce.lmf.core.api.util;

import org.logoce.lmf.core.util.ReferenceExplorer;
import org.logoce.lmf.extender.api.IAdapter;
import org.logoce.lmf.core.lang.LMObject;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Model explorer based on integer FeatureIDs.
 * <p>
 * This mirrors the EMF-based IntModelExplorer: it climbs to the first ancestor
 * matching {@code parentClass}, then walks the provided relation FeatureIDs in order.
 */
public final class IntModelExplorer implements IModelExplorer
{
	private final Class<? extends LMObject> parentClass;
	private final List<ReferenceExplorer> references;

	public IntModelExplorer(final int[] references)
	{
		this(LMObject.class, references);
	}

	public IntModelExplorer(final Class<? extends LMObject> parentClass, final int[] references)
	{
		this.parentClass = parentClass;
		this.references = Arrays.stream(references)
								.mapToObj(ReferenceExplorer::new)
								.toList();
	}

	@Override
	public <T extends LMObject> List<T> explore(final LMObject root, final Class<T> targetClass)
	{
		return stream(root, targetClass).toList();
	}

	@Override
	public List<LMObject> explore(final LMObject root)
	{
		return stream(root).toList();
	}

	@Override
	public <T extends LMObject> Stream<T> stream(final LMObject root, final Class<T> targetClass)
	{
		return stream(root).map(targetClass::cast);
	}

	@Override
	public <T extends IAdapter> List<T> exploreAdapt(final LMObject root, final Class<T> adapterType)
	{
		return streamAdapt(root, adapterType).toList();
	}

	@Override
	public <T extends IAdapter> List<T> exploreAdaptGeneric(final LMObject root,
															final Class<? extends IAdapter> adapterType)
	{
		return this.<T>streamAdaptGeneric(root, adapterType).toList();
	}

	@Override
	public <T extends IAdapter> Stream<T> streamAdapt(final LMObject root, final Class<T> adapterType)
	{
		return stream(root).map(e -> e.adapt(adapterType)).filter(Objects::nonNull);
	}

	@Override
	public <T extends IAdapter> Stream<T> streamAdaptGeneric(final LMObject root,
															 final Class<? extends IAdapter> adapterType)
	{
		return stream(root).map(e -> e.<T>adaptGeneric(adapterType)).filter(Objects::nonNull);
	}

	@Override
	public <T extends IAdapter> List<T> exploreAdaptNotNull(final LMObject root, final Class<T> adapterType)
	{
		return streamAdaptNotNull(root, adapterType).toList();
	}

	@Override
	public <T extends IAdapter> Stream<T> streamAdaptNotNull(final LMObject root, final Class<T> adapterType)
	{
		return stream(root).map(e -> e.adaptNotNull(adapterType));
	}

	@Override
	public Stream<LMObject> stream(final LMObject source)
	{
		final var root = parent(source);
		var stream = Stream.of(root);
		for (final var reference : references)
		{
			stream = stream.flatMap(object -> reference.stream(object).map(LMObject.class::cast));
		}
		return stream;
	}

	private LMObject parent(LMObject source)
	{
		while (parentClass.isInstance(source) == false)
		{
			source = source.lmContainer();
		}
		return source;
	}
}

