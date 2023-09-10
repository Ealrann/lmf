package isotropy.lmf.core.util.oldlogoce;

import org.logoce.extender.api.IAdapter;
import org.sheepy.lily.core.api.model.ILilyEObject;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class IntModelExplorer implements IModelExplorer
{
	private final Class<? extends ILilyEObject> parentClass;
	private final List<ReferenceExplorer> references;

	public IntModelExplorer(int[] references)
	{
		this(ILilyEObject.class, references);
	}

	public IntModelExplorer(Class<? extends ILilyEObject> parentClass, int[] references)
	{
		this.parentClass = parentClass;
		this.references = Arrays.stream(references)
								.mapToObj(ReferenceExplorer::new)
								.toList();
	}

	@Override
	public <T extends ILilyEObject> List<T> explore(ILilyEObject root, Class<T> targetClass)
	{
		return stream(root, targetClass).toList();
	}

	@Override
	public List<ILilyEObject> explore(ILilyEObject root)
	{
		return stream(root).toList();
	}

	@Override
	public <T extends ILilyEObject> Stream<T> stream(ILilyEObject root, Class<T> targetClass)
	{
		return stream(root).map(targetClass::cast);
	}

	@Override
	public <T extends IAdapter> List<T> exploreAdapt(ILilyEObject root, Class<T> adapterType)
	{
		return streamAdapt(root, adapterType).toList();
	}

	@Override
	public <T extends IAdapter> List<T> exploreAdaptGeneric(ILilyEObject root, Class<? extends IAdapter> adapterType)
	{
		return this.<T>streamAdaptGeneric(root, adapterType)
				   .toList();
	}

	@Override
	public <T extends IAdapter> Stream<T> streamAdapt(ILilyEObject root, Class<T> adapterType)
	{
		return stream(root).map(e -> e.adapt(adapterType))
						   .filter(Objects::nonNull);
	}

	@Override
	public <T extends IAdapter> Stream<T> streamAdaptGeneric(ILilyEObject root, Class<? extends IAdapter> adapterType)
	{
		return stream(root).map(e -> e.<T>adaptGeneric(adapterType))
						   .filter(Objects::nonNull);
	}

	@Override
	public <T extends IAdapter> List<T> exploreAdaptNotNull(ILilyEObject root, Class<T> adapterType)
	{
		return streamAdaptNotNull(root, adapterType).toList();
	}

	@Override
	public <T extends IAdapter> Stream<T> streamAdaptNotNull(ILilyEObject root, Class<T> adapterType)
	{
		return stream(root).map(e -> e.adaptNotNull(adapterType));
	}

	@Override
	public Stream<ILilyEObject> stream(ILilyEObject source)
	{
		final var root = parent(source);
		var stream = Stream.of(root);
		for (final var reference : references)
		{
			stream = stream.flatMap(reference::stream);
		}
		return stream;
	}

	private ILilyEObject parent(ILilyEObject source)
	{
		while (parentClass.isInstance(source) == false)
		{
			source = (ILilyEObject) source.eContainer();
		}
		return source;
	}
}
