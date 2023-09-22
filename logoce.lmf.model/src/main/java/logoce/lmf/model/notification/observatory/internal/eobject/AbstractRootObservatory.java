package logoce.lmf.model.notification.observatory.internal.eobject;

import logoce.lmf.model.api.feature.RawFeature;
import logoce.lmf.model.api.notification.Notification;
import logoce.lmf.model.lang.LMObject;
import logoce.lmf.model.notification.observatory.*;
import logoce.lmf.model.notification.observatory.internal.InternalObservatoryBuilder;
import logoce.lmf.model.notification.observatory.internal.allocation.AdapterObservatory;
import logoce.lmf.model.notification.observatory.internal.eobject.listener.GatherBulkListener;
import logoce.lmf.model.notification.observatory.internal.eobject.listener.GatherListener;
import logoce.lmf.model.notification.observatory.internal.eobject.poi.*;
import logoce.lmf.model.notification.observatory.internal.notifier.NotifierAdapterObservatory;
import logoce.lmf.model.notification.observatory.internal.notifier.NotifierObservatory;
import org.logoce.lmf.extender.api.IAdapter;
import org.logoce.lmf.notification.api.IFeatures;
import org.logoce.lmf.notification.api.INotifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractRootObservatory implements IObservatory
{
	private final List<IObservatory> children;
	private final List<IEObjectPOI> pois;
	private final List<GatherListener<LMObject>> gatherListeners;
	private final List<GatherBulkListener<LMObject>> gatherBulkListeners;

	protected AbstractRootObservatory(final List<IObservatory> children,
									  final List<IEObjectPOI> pois,
									  final List<GatherListener<LMObject>> gatherListeners,
									  final List<GatherBulkListener<LMObject>> gatherBulkListeners)
	{
		this.children = List.copyOf(children);
		this.pois = List.copyOf(pois);
		this.gatherListeners = List.copyOf(gatherListeners);
		this.gatherBulkListeners = List.copyOf(gatherBulkListeners);
	}

	protected void register(final LMObject target)
	{
		for (var listener : gatherListeners)
		{
			listener.discoverObject().accept(target);
		}
		if (gatherBulkListeners.isEmpty() == false)
		{
			final var targetList = List.of(target);
			for (var listener : gatherBulkListeners)
			{
				listener.discoverObjects().accept(targetList);
			}
		}
		for (var poi : pois)
		{
			poi.listen(target);
		}
		for (var child : children)
		{
			child.observe(target);
		}
	}

	protected void unregister(final LMObject target)
	{
		for (var child : children)
		{
			child.shut(target);
		}
		for (var poi : pois)
		{
			poi.sulk(target);
		}
		if (gatherBulkListeners.isEmpty() == false)
		{
			final var targetList = List.of(target);
			for (var listener : gatherBulkListeners)
			{
				listener.removedObjects().accept(targetList);
			}
		}
		for (var listener : gatherListeners)
		{
			listener.removedObject().accept(target);
		}
	}

	public static abstract class Builder implements IObservatoryBuilder, InternalObservatoryBuilder
	{
		protected final List<InternalObservatoryBuilder> children = new ArrayList<>();
		protected final List<IEObjectPOI> pois = new ArrayList<>();
		protected final List<GatherListener<LMObject>> gatherListeners = new ArrayList<>();
		protected final List<GatherBulkListener<LMObject>> gatherBulkListeners = new ArrayList<>();

		@Override
		public IObservatoryBuilder focus(LMObject object)
		{
			final var child = new FocusedObservatory.Builder(object);
			children.add(child);
			return child;
		}

		@Override
		public <F extends IFeatures<F>> INotifierObservatoryBuilder<F> focus(INotifier<F> notifier)
		{
			final var child = new NotifierObservatory.Builder<>(notifier);
			children.add(child);
			return child;
		}

		@Override
		public IEObjectObservatoryBuilder<LMObject> explore(final RawFeature<?, ?> relation)
		{
			final var child = new EObjectObservatory.Builder<>(relation, LMObject.class);
			children.add(child);
			return child;
		}

		@Override
		public <T extends LMObject> IEObjectObservatoryBuilder<T> explore(final RawFeature<?, ?> relation,
																		  final Class<T> cast)
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
		public <T extends IAdapter> IAdapterObservatoryBuilder<T> adapt(final Class<T> classifier)
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
		public IObservatoryBuilder listen(final Consumer<Notification> listener, final List<RawFeature<?, ?>> features)
		{
			pois.add(new EObjectPOI(listener, features));
			return this;
		}

		@Override
		public IObservatoryBuilder listenNoParam(final Runnable listener, final List<RawFeature<?, ?>> features)
		{
			pois.add(new EObjectNoParamPOI(listener, features));
			return this;
		}

		@Override
		public IObservatoryBuilder listenStructure(final Consumer<Notification> structureChanged)
		{
			pois.add(new EObjectStructurePOI(structureChanged));
			return this;
		}

		@Override
		public IObservatoryBuilder listenStructureNoParam(final Runnable structureChanged)
		{
			pois.add(new EObjectStructureNoParamPOI(structureChanged));
			return this;
		}

		@Override
		public IEObjectObservatoryBuilder<LMObject> gather(final Consumer<LMObject> discoveredObject,
														   final Consumer<LMObject> removedObject)
		{
			gatherListeners.add(new GatherListener<>(discoveredObject, removedObject));
			return this;
		}

		@Override
		public IEObjectObservatoryBuilder<LMObject> gatherBulk(final Consumer<List<LMObject>> discoveredObjects,
															   final Consumer<List<LMObject>> removedObjects)
		{
			gatherBulkListeners.add(new GatherBulkListener<>(discoveredObjects, removedObjects));
			return this;
		}

		@Override
		public boolean isEmpty()
		{
			return pois.isEmpty() && children.isEmpty();
		}
	}
}
