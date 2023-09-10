package isotropy.lmf.core.notification.observatory.internal.eobject;

import org.sheepy.lily.core.api.model.ILilyEObject;
import org.sheepy.lily.core.api.notification.observatory.IObservatory;
import org.sheepy.lily.core.api.notification.observatory.internal.InternalObservatoryBuilder;
import org.sheepy.lily.core.api.notification.observatory.internal.eobject.listener.GatherBulkListener;
import org.sheepy.lily.core.api.notification.observatory.internal.eobject.listener.GatherListener;
import org.sheepy.lily.core.api.notification.observatory.internal.eobject.poi.IEObjectPOI;

import java.util.List;

public final class RootObservatory extends AbstractRootObservatory
{
	private RootObservatory(final List<IObservatory> children,
							final List<IEObjectPOI> pois,
							final List<GatherListener<ILilyEObject>> gatherListeners,
							final List<GatherBulkListener<ILilyEObject>> gatherBulkListeners)
	{
		super(children, pois, gatherListeners, gatherBulkListeners);
	}

	@Override
	public void observe(final ILilyEObject parent)
	{
		register(parent);
	}

	@Override
	public void shut(final ILilyEObject parent)
	{
		unregister(parent);
	}

	public static final class Builder extends AbstractRootObservatory.Builder
	{
		@Override
		public IObservatory build()
		{
			final var builtChildren = children.stream()
											  .map(InternalObservatoryBuilder::build)
											  .toList();

			return new RootObservatory(builtChildren, pois, gatherListeners, gatherBulkListeners);
		}
	}
}
