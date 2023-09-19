package isotropy.lmf.core.notification.observatory.internal.eobject;

import isotropy.lmf.core.api.feature.RawFeature;
import isotropy.lmf.core.api.notification.Notification;
import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.notification.observatory.IAdapterObservatoryBuilder;
import isotropy.lmf.core.notification.observatory.IEObjectObservatoryBuilder;
import isotropy.lmf.core.notification.observatory.INotifierAdapterObservatoryBuilder;
import isotropy.lmf.core.notification.observatory.IObservatory;
import isotropy.lmf.core.notification.observatory.internal.InternalObservatoryBuilder;
import isotropy.lmf.core.notification.observatory.internal.allocation.AdapterObservatory;
import isotropy.lmf.core.notification.observatory.internal.eobject.listener.GatherBulkListener;
import isotropy.lmf.core.notification.observatory.internal.eobject.listener.GatherListener;
import isotropy.lmf.core.notification.observatory.internal.eobject.poi.*;
import isotropy.lmf.core.notification.observatory.internal.notifier.NotifierAdapterObservatory;
import org.logoce.extender.api.IAdapter;
import org.logoce.notification.api.IFeatures;
import org.logoce.notification.api.INotifier;

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
		public IEObjectObservatoryBuilder<LMObject> explore(final RawFeature<?, ?> relation)
		{
			final var child = new EObjectObservatory.Builder<>(relation, LMObject.class);
			children.add(child);
			return child;
		}

		@Override
		public <Y extends LMObject> IEObjectObservatoryBuilder<Y> explore(final RawFeature<?, ?> relation,
																		  final Class<Y> cast)
		{
			final var child = new EObjectObservatory.Builder<>(relation, cast);
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
		public IEObjectObservatoryBuilder<T> listen(final Consumer<Notification> listener,
													final List<RawFeature<?, ?>> features)
		{
			pois.add(new EObjectPOI(listener, features));
			return this;
		}

		@Override
		public IEObjectObservatoryBuilder<T> listenNoParam(final Runnable listener,
														   final List<RawFeature<?, ?>> features)
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
