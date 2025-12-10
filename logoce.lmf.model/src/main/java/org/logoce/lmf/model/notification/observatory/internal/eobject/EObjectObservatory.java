package org.logoce.lmf.model.notification.observatory.internal.eobject;

import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.notification.observatory.IEObjectObservatoryBuilder;
import org.logoce.lmf.model.notification.observatory.IObservatory;
import org.logoce.lmf.model.notification.observatory.internal.InternalObservatoryBuilder;
import org.logoce.lmf.model.notification.observatory.internal.eobject.listener.GatherBulkListener;
import org.logoce.lmf.model.notification.observatory.internal.eobject.listener.GatherListener;
import org.logoce.lmf.model.notification.observatory.internal.eobject.poi.IEObjectPOI;
import org.logoce.lmf.model.notification.util.ModelStructureBulkObserver;

import java.util.List;

public final class EObjectObservatory<T extends LMObject> extends AbstractEObjectObservatory<T>
{
	private final ModelStructureBulkObserver structureObserver;
	private final int referenceId;

	public EObjectObservatory(final int referenceId,
							  final Class<T> cast,
							  final List<IObservatory> children,
							  final List<IEObjectPOI> pois,
							  final List<GatherListener<T>> gatherListeners,
							  final List<GatherBulkListener<T>> gatherBulkListeners)
	{
		super(cast, children, pois, gatherListeners, gatherBulkListeners);
		this.referenceId = referenceId;
		structureObserver = new ModelStructureBulkObserver(referenceId, this::startObserve, this::stopObserve);
	}

	@Override
	public void observe(final LMObject parent)
	{
		assert checkParent(parent);
		structureObserver.startObserve(parent);
	}

	@Override
	public void shut(final LMObject parent)
	{
		structureObserver.stopObserve(parent);
	}

	@SuppressWarnings("SameReturnValue")
	private boolean checkParent(final LMObject parent)
	{
		final var group = parent.lmGroup();
		for (final var feature : group.features())
		{
			if (feature.id() == referenceId)
			{
				if (feature instanceof org.logoce.lmf.model.lang.Relation<?, ?> relation && relation.contains())
				{
					return true;
				}
				throw new IllegalArgumentException("Observation failed, the explored feature " +
												   feature.name() +
												   " on " +
												   group.name() +
												   " is not a Relation.");
			}
		}
		throw new IllegalArgumentException("Unknown featureId " + referenceId + " for group " + group.name());
	}

	private void startObserve(List<? extends LMObject> objects)
	{
		register(objects);
	}

	private void stopObserve(List<? extends LMObject> objects)
	{
		unregister(objects);
	}

	public static final class Builder<T extends LMObject> extends AbstractEObjectObservatory.Builder<T> implements
																										IEObjectObservatoryBuilder<T>,
																										InternalObservatoryBuilder
	{
		private final int referenceId;

		public Builder(int referenceId, Class<T> cast)
		{
			super(cast);
			this.referenceId = referenceId;
		}

		@Override
		public IObservatory build()
		{
			final var builtChildren = children.stream().map(InternalObservatoryBuilder::build).toList();

			return new EObjectObservatory<>(referenceId,
											cast,
											builtChildren,
											pois,
											gatherListeners,
											gatherBulkListeners);
		}
	}
}
