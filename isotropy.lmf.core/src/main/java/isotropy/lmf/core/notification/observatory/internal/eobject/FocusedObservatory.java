package isotropy.lmf.core.notification.observatory.internal.eobject;

import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.notification.observatory.IObservatory;
import isotropy.lmf.core.notification.observatory.internal.InternalObservatoryBuilder;
import isotropy.lmf.core.notification.observatory.internal.eobject.listener.GatherBulkListener;
import isotropy.lmf.core.notification.observatory.internal.eobject.listener.GatherListener;
import isotropy.lmf.core.notification.observatory.internal.eobject.poi.IEObjectPOI;

import java.util.List;

public final class FocusedObservatory extends AbstractRootObservatory
{
	private final LMObject root;

	private FocusedObservatory(final LMObject root,
							   final List<IObservatory> children,
							   final List<IEObjectPOI> pois,
							   final List<GatherListener<LMObject>> gatherListeners,
							   final List<GatherBulkListener<LMObject>> gatherBulkListeners)
	{
		super(children, pois, gatherListeners, gatherBulkListeners);
		this.root = root;
	}

	@Override
	public void observe(final LMObject parent)
	{
		register(root);
	}

	@Override
	public void shut(final LMObject parent)
	{
		unregister(root);
	}

	public static final class Builder extends AbstractRootObservatory.Builder
	{
		private final LMObject root;

		public Builder(LMObject root)
		{
			super();
			this.root = root;
		}

		@Override
		public FocusedObservatory build()
		{
			final var builtChildren = children.stream().map(InternalObservatoryBuilder::build).toList();

			return new FocusedObservatory(root, builtChildren, pois, gatherListeners, gatherBulkListeners);
		}
	}
}
