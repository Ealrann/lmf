package isotropy.lmf.core.notification.observatory.internal.eobject;

import org.eclipse.emf.common.notify.Notification;
import org.logoce.extender.api.IAdapter;
import org.logoce.notification.api.IFeatures;
import org.logoce.notification.api.INotifier;
import org.sheepy.lily.core.api.model.ILilyEObject;
import org.sheepy.lily.core.api.notification.observatory.*;
import org.sheepy.lily.core.api.notification.observatory.internal.InternalObservatoryBuilder;
import org.sheepy.lily.core.api.notification.observatory.internal.allocation.AdapterObservatory;
import org.sheepy.lily.core.api.notification.observatory.internal.eobject.listener.GatherBulkListener;
import org.sheepy.lily.core.api.notification.observatory.internal.eobject.listener.GatherListener;
import org.sheepy.lily.core.api.notification.observatory.internal.eobject.poi.*;
import org.sheepy.lily.core.api.notification.observatory.internal.notifier.NotifierAdapterObservatory;
import org.sheepy.lily.core.api.notification.observatory.internal.notifier.NotifierObservatory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractRootObservatory implements IObservatory
{
	private final List<IObservatory> children;
	private final List<IEObjectPOI> pois;
	private final List<GatherListener<ILilyEObject>> gatherListeners;
	private final List<GatherBulkListener<ILilyEObject>> gatherBulkListeners;

	protected AbstractRootObservatory(final List<IObservatory> children,
									  final List<IEObjectPOI> pois,
									  final List<GatherListener<ILilyEObject>> gatherListeners,
									  final List<GatherBulkListener<ILilyEObject>> gatherBulkListeners)
	{
		this.children = List.copyOf(children);
		this.pois = List.copyOf(pois);
		this.gatherListeners = List.copyOf(gatherListeners);
		this.gatherBulkListeners = List.copyOf(gatherBulkListeners);
	}

	protected void register(final ILilyEObject target)
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

	protected void unregister(final ILilyEObject target)
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
		protected final List<GatherListener<ILilyEObject>> gatherListeners = new ArrayList<>();
		protected final List<GatherBulkListener<ILilyEObject>> gatherBulkListeners = new ArrayList<>();

		@Override
		public IObservatoryBuilder focus(ILilyEObject object)
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
		public IEObjectObservatoryBuilder<ILilyEObject> explore(final int referenceId)
		{
			final var child = new EObjectObservatory.Builder<>(referenceId, ILilyEObject.class);
			children.add(child);
			return child;
		}

		@Override
		public <T extends ILilyEObject> IEObjectObservatoryBuilder<T> explore(final int referenceId,
																			  final Class<T> cast)
		{
			final var child = new EObjectObservatory.Builder<>(referenceId, cast);
			children.add(child);
			return child;
		}

		@Override
		public IEObjectObservatoryBuilder<ILilyEObject> exploreParent()
		{
			final var child = new ParentObservatory.Builder<>(ILilyEObject.class);
			children.add(child);
			return child;
		}

		@Override
		public <Y extends ILilyEObject> IEObjectObservatoryBuilder<Y> exploreParent(final Class<Y> cast)
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
		public IObservatoryBuilder listen(final Consumer<Notification> listener, final int... features)
		{
			pois.add(new EObjectPOI(listener, features));
			return this;
		}

		@Override
		public IObservatoryBuilder listenNoParam(final Runnable listener, final int... features)
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
		public IEObjectObservatoryBuilder<ILilyEObject> gather(final Consumer<ILilyEObject> discoveredObject,
															   final Consumer<ILilyEObject> removedObject)
		{
			gatherListeners.add(new GatherListener<>(discoveredObject, removedObject));
			return this;
		}

		@Override
		public IEObjectObservatoryBuilder<ILilyEObject> gatherBulk(final Consumer<List<ILilyEObject>> discoveredObjects,
																   final Consumer<List<ILilyEObject>> removedObjects)
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
