package org.logoce.lmf.model.notification.observatory.internal.eobject;

import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.notification.observatory.IEObjectObservatoryBuilder;
import org.logoce.lmf.model.notification.observatory.IObservatory;
import org.logoce.lmf.model.notification.observatory.internal.InternalObservatoryBuilder;
import org.logoce.lmf.model.notification.observatory.internal.eobject.listener.GatherBulkListener;
import org.logoce.lmf.model.notification.observatory.internal.eobject.listener.GatherListener;
import org.logoce.lmf.model.notification.observatory.internal.eobject.poi.IEObjectPOI;
import org.logoce.lmf.model.notification.util.ParentObserver;

import java.util.List;

public final class ParentObservatory<T extends LMObject> extends AbstractEObjectObservatory<T> implements IObservatory
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
	public void observe(final LMObject source)
	{
		final var parent = source.lmContainer();
		register(List.of(parent));
		parentObserver.startObserve(source);
	}

	@Override
	public void shut(final LMObject source)
	{
		parentObserver.stopObserve(source);
		final var parent = source.lmContainer();
		if (parent != null) unregister(List.of(parent));
	}

	private void parentChange(LMObject oldParent, LMObject newParent)
	{
		unregister(List.of(oldParent));
		if (newParent != null) register(List.of(newParent));
	}

	public static final class Builder<T extends LMObject> extends AbstractEObjectObservatory.Builder<T> implements
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
			final var builtChildren = children.stream().map(InternalObservatoryBuilder::build).toList();

			return new ParentObservatory<>(cast, builtChildren, pois, gatherListeners, gatherBulkListeners);
		}
	}
}
