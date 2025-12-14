package org.logoce.lmf.core.notification.observatory.internal.eobject;

import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.notification.observatory.IObservatory;
import org.logoce.lmf.core.notification.observatory.internal.InternalObservatoryBuilder;
import org.logoce.lmf.core.notification.observatory.internal.eobject.listener.GatherBulkListener;
import org.logoce.lmf.core.notification.observatory.internal.eobject.listener.GatherListener;
import org.logoce.lmf.core.notification.observatory.internal.eobject.poi.IEObjectPOI;

import java.util.List;

public final class RootObservatory extends AbstractRootObservatory
{
	private RootObservatory(final List<IObservatory> children,
							final List<IEObjectPOI> pois,
							final List<GatherListener<LMObject>> gatherListeners,
							final List<GatherBulkListener<LMObject>> gatherBulkListeners)
	{
		super(children, pois, gatherListeners, gatherBulkListeners);
	}

	@Override
	public void observe(final LMObject parent)
	{
		register(parent);
	}

	@Override
	public void shut(final LMObject parent)
	{
		unregister(parent);
	}

	public static final class Builder extends AbstractRootObservatory.Builder
	{
		@Override
		public IObservatory build()
		{
			final var builtChildren = children.stream().map(InternalObservatoryBuilder::build).toList();

			return new RootObservatory(builtChildren, pois, gatherListeners, gatherBulkListeners);
		}
	}
}
