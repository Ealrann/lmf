package isotropy.lmf.core.util.oldlogoce;

import org.eclipse.emf.ecore.EReference;
import org.logoce.extender.api.IAdapter;
import org.sheepy.lily.core.api.model.ILilyEObject;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class ModelExplorer implements IModelExplorer
{
	private final int parentHeight;
	private final List<EReference> references;

	public ModelExplorer(List<EReference> references)
	{
		this(0, references);
	}

	public ModelExplorer(int parentHeight, List<EReference> references)
	{
		this.parentHeight = parentHeight;
		this.references = List.copyOf(references);
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
		return stream(root).map(e -> e.adapt(adapterType)).filter(Objects::nonNull);
	}

	@Override
	public <T extends IAdapter> Stream<T> streamAdaptGeneric(ILilyEObject root, Class<? extends IAdapter> adapterType)
	{
		return stream(root).map(e -> e.<T>adaptGeneric(adapterType)).filter(Objects::nonNull);
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
		for (final var feature : references)
		{
			stream = stream.flatMap(e -> extractList(e, feature));
		}
		return stream;
	}

	private ILilyEObject parent(ILilyEObject source)
	{
		for (int i = 0; i < parentHeight; i++)
		{
			source = (ILilyEObject) source.eContainer();
		}
		return source;
	}

	@SuppressWarnings("unchecked")
	private static Stream<ILilyEObject> extractList(ILilyEObject object, EReference reference)
	{
		final var val = getValue(object, reference);
		if (val instanceof List)
		{
			return ((List<ILilyEObject>) val).stream();
		}
		else
		{
			return Stream.ofNullable((ILilyEObject) val);
		}
	}

	private static Object getValue(ILilyEObject target, final EReference reference)
	{
		if (target.eClass().getEAllStructuralFeatures().contains(reference))
		{
			return target.eGet(reference);
		}
		else
		{
			return null;
		}
	}
}
