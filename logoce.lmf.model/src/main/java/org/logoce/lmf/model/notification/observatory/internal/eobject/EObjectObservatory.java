package org.logoce.lmf.model.notification.observatory.internal.eobject;

import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
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
	private final RawFeature<?, ?> relation;

	public EObjectObservatory(final RawFeature<?, ?> relation,
							  final Class<T> cast,
							  final List<IObservatory> children,
							  final List<IEObjectPOI> pois,
							  final List<GatherListener<T>> gatherListeners,
							  final List<GatherBulkListener<T>> gatherBulkListeners)
	{
		super(cast, children, pois, gatherListeners, gatherBulkListeners);
		structureObserver = new ModelStructureBulkObserver(relation, this::startObserve, this::stopObserve);
		this.relation = relation;
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
		if (((Relation<?, ?>) relation.featureSupplier().get()).contains())
		{
			return true;
		}
		else
		{
			throw new IllegalArgumentException("Observation failed, the explored feature " +
											   relation.featureSupplier().get().name() +
											   " on " +
											   parent.lmGroup().name() +
											   " is not a Relation.");
		}
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
		private final RawFeature<?, ?> relation;

		public Builder(RawFeature<?, ?> relation, Class<T> cast)
		{
			super(cast);
			this.relation = relation;
		}

		@Override
		public IObservatory build()
		{
			final var builtChildren = children.stream().map(InternalObservatoryBuilder::build).toList();

			return new EObjectObservatory<>(relation,
											cast,
											builtChildren,
											pois,
											gatherListeners,
											gatherBulkListeners);
		}
	}
}
