package isotropy.lmf.core.util.oldlogoce;

import org.logoce.extender.api.IAdapter;
import org.sheepy.lily.core.api.model.ILilyEObject;

import java.util.List;
import java.util.stream.Stream;

public interface IModelExplorer
{
	<T extends ILilyEObject> List<T> explore(ILilyEObject root, Class<T> targetClass);
	List<ILilyEObject> explore(ILilyEObject root);
	<T extends ILilyEObject> Stream<T> stream(ILilyEObject root, Class<T> targetClass);
	Stream<ILilyEObject> stream(ILilyEObject root);
	<T extends IAdapter> List<T> exploreAdapt(ILilyEObject root, Class<T> adapterType);
	<T extends IAdapter> List<T> exploreAdaptGeneric(ILilyEObject root, Class<? extends IAdapter> adapterType);
	<T extends IAdapter> Stream<T> streamAdapt(ILilyEObject root, Class<T> adapterType);
	<T extends IAdapter> Stream<T> streamAdaptGeneric(ILilyEObject root, Class<? extends IAdapter> adapterType);
	<T extends IAdapter> List<T> exploreAdaptNotNull(ILilyEObject root, Class<T> adapterType);
	<T extends IAdapter> Stream<T> streamAdaptNotNull(ILilyEObject root, Class<T> adapterType);
}
