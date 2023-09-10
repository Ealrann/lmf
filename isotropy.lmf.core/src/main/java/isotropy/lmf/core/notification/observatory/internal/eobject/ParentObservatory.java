package isotropy.lmf.core.notification.observatory.internal.eobject;

import org.sheepy.lily.core.api.model.ILilyEObject;
import org.sheepy.lily.core.api.notification.observatory.IEObjectObservatoryBuilder;
import org.sheepy.lily.core.api.notification.observatory.IObservatory;
import org.sheepy.lily.core.api.notification.observatory.internal.InternalObservatoryBuilder;
import org.sheepy.lily.core.api.notification.observatory.internal.eobject.listener.GatherBulkListener;
import org.sheepy.lily.core.api.notification.observatory.internal.eobject.listener.GatherListener;
import org.sheepy.lily.core.api.notification.observatory.internal.eobject.poi.IEObjectPOI;
import org.sheepy.lily.core.api.notification.util.ParentObserver;

import java.util.List;

public final class ParentObservatory<T extends ILilyEObject> extends AbstractEObjectObservatory<T> implements
																								   IObservatory
{
	private final ParentObserver parentObserver = new ParentObserver(this::parentChange);

	private ParentObservatory(final Class<T> cast,
							  final List<IObservatory> children,
							  final List<IEObjectPOI> pois,
							  final List<GatherListener<T>> gatherListeners,
							  final List<GatherBulkListener<T>> gatherBulkListeners)
	{
		super(cast, children, pois, gatherListeners, gatherBulkListeners);
	}

	@Override
	public void observe(final ILilyEObject source)
	{
		final var parent = (ILilyEObject) source.eContainer();
		register(List.of(parent));
		parentObserver.startObserve(source);
	}

	@Override
	public void shut(final ILilyEObject source)
	{
		parentObserver.stopObserve(source);
		final var parent = (ILilyEObject) source.eContainer();
		if (parent != null) unregister(List.of(parent));
	}

	private void parentChange(ILilyEObject oldParent, ILilyEObject newParent)
	{
		unregister(List.of(oldParent));
		if (newParent != null) register(List.of(newParent));
	}

	public static final class Builder<T extends ILilyEObject> extends AbstractEObjectObservatory.Builder<T> implements
																											IEObjectObservatoryBuilder<T>,
																											InternalObservatoryBuilder
	{
		public Builder(Class<T> cast)
		{
			super(cast);
		}

		@Override
		public ParentObservatory<T> build()
		{
			final var builtChildren = children.stream()
											  .map(InternalObservatoryBuilder::build)
											  .toList();

			return new ParentObservatory<>(cast, builtChildren, pois, gatherListeners, gatherBulkListeners);
		}
	}
}
