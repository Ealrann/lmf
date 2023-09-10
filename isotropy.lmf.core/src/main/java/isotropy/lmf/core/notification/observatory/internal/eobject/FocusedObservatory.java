package isotropy.lmf.core.notification.observatory.internal.eobject;

import org.sheepy.lily.core.api.model.ILilyEObject;
import org.sheepy.lily.core.api.notification.observatory.IObservatory;
import org.sheepy.lily.core.api.notification.observatory.internal.InternalObservatoryBuilder;
import org.sheepy.lily.core.api.notification.observatory.internal.eobject.listener.GatherBulkListener;
import org.sheepy.lily.core.api.notification.observatory.internal.eobject.listener.GatherListener;
import org.sheepy.lily.core.api.notification.observatory.internal.eobject.poi.IEObjectPOI;

import java.util.List;

public final class FocusedObservatory extends AbstractRootObservatory
{
	private final ILilyEObject root;

	private FocusedObservatory(final ILilyEObject root,
							   final List<IObservatory> children,
							   final List<IEObjectPOI> pois,
							   final List<GatherListener<ILilyEObject>> gatherListeners,
							   final List<GatherBulkListener<ILilyEObject>> gatherBulkListeners)
	{
		super(children, pois, gatherListeners, gatherBulkListeners);
		this.root = root;
	}

	@Override
	public void observe(final ILilyEObject parent)
	{
		register(root);
	}

	@Override
	public void shut(final ILilyEObject parent)
	{
		unregister(root);
	}

	public static final class Builder extends AbstractRootObservatory.Builder
	{
		private final ILilyEObject root;

		public Builder(ILilyEObject root)
		{
			super();
			this.root = root;
		}

		@Override
		public FocusedObservatory build()
		{
			final var builtChildren = children.stream()
											  .map(InternalObservatoryBuilder::build)
											  .toList();

			return new FocusedObservatory(root, builtChildren, pois, gatherListeners, gatherBulkListeners);
		}
	}
}
