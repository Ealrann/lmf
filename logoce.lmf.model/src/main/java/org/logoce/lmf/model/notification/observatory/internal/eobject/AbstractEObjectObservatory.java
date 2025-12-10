package org.logoce.lmf.model.notification.observatory.internal.eobject;

import org.logoce.lmf.extender.api.IAdapter;
import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.notification.observatory.IAdapterObservatoryBuilder;
import org.logoce.lmf.model.notification.observatory.IEObjectObservatoryBuilder;
import org.logoce.lmf.model.notification.observatory.INotifierAdapterObservatoryBuilder;
import org.logoce.lmf.model.notification.observatory.IObservatory;
import org.logoce.lmf.model.notification.observatory.internal.InternalObservatoryBuilder;
import org.logoce.lmf.model.notification.observatory.internal.allocation.AdapterObservatory;
import org.logoce.lmf.model.notification.observatory.internal.eobject.listener.GatherBulkListener;
import org.logoce.lmf.model.notification.observatory.internal.eobject.listener.GatherListener;
import org.logoce.lmf.model.notification.observatory.internal.eobject.poi.*;
import org.logoce.lmf.model.notification.observatory.internal.notifier.NotifierAdapterObservatory;
import org.logoce.lmf.notification.api.IFeatures;
import org.logoce.lmf.notification.api.INotifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractEObjectObservatory<T extends LMObject> implements IObservatory
{
	private final Class<T> cast;
	private final List<IObservatory> children;
	private final List<IEObjectPOI> pois;
	private final List<GatherListener<T>> gatherListeners;
	private final List<GatherBulkListener<T>> gatherBulkListeners;

	public AbstractEObjectObservatory(final Class<T> cast,
									  final List<IObservatory> children,
									  final List<IEObjectPOI> pois,
									  final List<GatherListener<T>> gatherListeners,
									  final List<GatherBulkListener<T>> gatherBulkListeners)
	{
		this.cast = cast;
		this.children = List.copyOf(children);
		this.pois = List.copyOf(pois);
		this.gatherListeners = List.copyOf(gatherListeners);
		this.gatherBulkListeners = List.copyOf(gatherBulkListeners);
	}

	@SuppressWarnings("unchecked")
	protected void register(List<? extends LMObject> objects)
	{
		for (var listener : gatherBulkListeners)
		{
			listener.discoverObjects().accept((List<T>) objects);
		}

		for (var object : objects)
		{
			for (var listener : gatherListeners)
			{
				if (cast.isInstance(object))
				{
					listener.discoverObject().accept(cast.cast(object));
				}
			}

			for (var poi : pois)
			{
				poi.listen(object);
			}

			for (var childObservatory : children)
			{
				childObservatory.observe(object);
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected void unregister(List<? extends LMObject> objects)
	{
		for (var object : objects)
		{
			for (var childObservatory : children)
			{
				childObservatory.shut(object);
			}

			for (var poi : pois)
			{
				poi.sulk(object);
			}

			for (var listener : gatherListeners)
			{
				if (cast.isInstance(object))
				{
					listener.removedObject().accept(cast.cast(object));
				}
			}
		}

		for (var listener : gatherBulkListeners)
		{
			listener.removedObjects().accept((List<T>) objects);
		}
	}

	protected static abstract class Builder<T extends LMObject> implements IEObjectObservatoryBuilder<T>
	{
		protected final Class<T> cast;
		protected final List<InternalObservatoryBuilder> children = new ArrayList<>();
		protected final List<IEObjectPOI> pois = new ArrayList<>();
		protected final List<GatherListener<T>> gatherListeners = new ArrayList<>();
		protected final List<GatherBulkListener<T>> gatherBulkListeners = new ArrayList<>();

		public Builder(Class<T> cast)
		{
			this.cast = cast;
		}

		@Override
		public IEObjectObservatoryBuilder<LMObject> explore(final int referenceId)
		{
			final var child = new EObjectObservatory.Builder<>(referenceId, LMObject.class);
			children.add(child);
			return child;
		}

		@Override
		public <Y extends LMObject> IEObjectObservatoryBuilder<Y> explore(final int referenceId,
																		  final Class<Y> cast)
		{
			final var child = new EObjectObservatory.Builder<>(referenceId, cast);
			children.add(child);
			return child;
		}

		@Override
		public IEObjectObservatoryBuilder<LMObject> exploreParent()
		{
			final var child = new ParentObservatory.Builder<>(LMObject.class);
			children.add(child);
			return child;
		}

		@Override
		public <Y extends LMObject> IEObjectObservatoryBuilder<Y> exploreParent(final Class<Y> cast)
		{
			final var child = new ParentObservatory.Builder<>(cast);
			children.add(child);
			return child;
		}

		@Override
		public <L extends IAdapter> IAdapterObservatoryBuilder<L> adapt(final Class<L> classifier)
		{
			final var child = new AdapterObservatory.Builder<>(classifier);
			children.add(child);
			return child;
		}

		@Override
		public <F extends IFeatures<?>, N extends IAdapter & INotifier<? extends F>> INotifierAdapterObservatoryBuilder<F, N> adaptNotifier(
				final Class<N> classifier)
		{
			final var child = new NotifierAdapterObservatory.Builder<>(classifier);
			children.add(child);
			return child;
		}

		@Override
		public IEObjectObservatoryBuilder<T> listen(final Consumer<Notification> listener, final int... features)
		{
			pois.add(new EObjectPOI(listener, features));
			return this;
		}

		@Override
		public IEObjectObservatoryBuilder<T> listenNoParam(final Runnable listener, final int... features)
		{
			pois.add(new EObjectNoParamPOI(listener, features));
			return this;
		}

		@Override
		public IEObjectObservatoryBuilder<T> listenStructure(final Consumer<Notification> structureChanged)
		{
			pois.add(new EObjectStructurePOI(structureChanged));
			return this;
		}

		@Override
		public IEObjectObservatoryBuilder<T> listenStructureNoParam(final Runnable structureChanged)
		{
			pois.add(new EObjectStructureNoParamPOI(structureChanged));
			return this;
		}

		@Override
		public IEObjectObservatoryBuilder<T> gather(Consumer<T> discoveredObject, Consumer<T> removedObject)
		{
			gatherListeners.add(new GatherListener<>(discoveredObject, removedObject));
			return this;
		}

		@Override
		public IEObjectObservatoryBuilder<T> gatherBulk(Consumer<List<T>> discoveredObjects,
														Consumer<List<T>> removedObjects)
		{
			gatherBulkListeners.add(new GatherBulkListener<>(discoveredObjects, removedObjects));
			return this;
		}
	}
}
