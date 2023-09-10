package isotropy.lmf.core.util.oldlogoce;

import org.eclipse.emf.ecore.EReference;
import org.logoce.extender.api.IAdapter;
import org.sheepy.lily.core.api.model.ILilyEObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class CompositeModelExplorer
{
	private final List<ModelExplorer> explorers;

	public CompositeModelExplorer(List<List<EReference>> featureLists)
	{
		this.explorers = List.copyOf(buildExplorers(featureLists));
	}

	private static List<ModelExplorer> buildExplorers(List<List<EReference>> featureLists)
	{
		final List<ModelExplorer> explorers = new ArrayList<>();
		for (final var features : featureLists)
		{
			explorers.add(new ModelExplorer(features));
		}
		return explorers;
	}

	public <T extends ILilyEObject> List<T> explore(ILilyEObject root, Class<T> targetClass)
	{
		return stream(root, targetClass).toList();
	}

	public <T extends ILilyEObject> Stream<T> stream(ILilyEObject root, Class<T> targetClass)
	{
		return stream(root).map(targetClass::cast);
	}

	public <T extends IAdapter> List<T> exploreAdapt(ILilyEObject root, Class<T> adapterType)
	{
		return streamAdapt(root, adapterType).toList();
	}

	public <T extends IAdapter> Stream<T> streamAdapt(ILilyEObject root, Class<T> adapterType)
	{
		return stream(root).map(e -> e.adapt(adapterType)).filter(Objects::nonNull);
	}

	public <T extends IAdapter> List<T> exploreAdaptNotNull(ILilyEObject root, Class<T> adapterType)
	{
		return streamAdaptNotNull(root, adapterType).toList();
	}

	public <T extends IAdapter> Stream<T> streamAdaptNotNull(ILilyEObject root, Class<T> adapterType)
	{
		return stream(root).map(e -> e.adaptNotNull(adapterType));
	}

	private Stream<ILilyEObject> stream(ILilyEObject root)
	{
		return explorers.stream().flatMap(e -> e.stream(root));
	}
}
