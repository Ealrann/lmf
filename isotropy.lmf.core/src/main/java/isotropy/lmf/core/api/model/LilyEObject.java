package isotropy.lmf.core.api.model;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EReference;
import org.logoce.adapter.api.BasicAdapterManager;
import org.logoce.extender.api.IAdapter;
import org.sheepy.lily.core.api.notification.util.NotificationUnifier;
import org.sheepy.lily.core.api.util.TreeLazyIterator;
import org.sheepy.lily.core.model.types.LNamedElement;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class LilyEObject extends LilyBasicNotifier implements ILilyEObject
{
	private static final String CANNOT_FIND_ADAPTER = "Cannot find adapter [%s] for class [%s]";
	private static final String CANNOT_FIND_IDENTIFIED_ADAPTER = "Cannot find adapter [%s] (id = %s) for class [%s]";

	private BasicAdapterManager extenderManager = null;

	private boolean loaded = false;

	@Override
	public final void eNotify(final Notification notification)
	{
		final var feature = notification.getFeature();
		final boolean isContainment = feature instanceof EReference reference && reference.isContainment();
		if (isContainment) NotificationUnifier.unifyAdded(notification, this::setupChild);
		super.eNotify(notification);
		if (isContainment) NotificationUnifier.unifyRemoved(notification, this::disposeChild);
	}

	@Override
	public final boolean eNotificationRequired()
	{
		return true;
	}

	private void setupChild(ILilyEObject notifier)
	{
		if (loaded && notifier instanceof LilyEObject child)
		{
			child.loadExtenderManager();
		}
	}

	private void disposeChild(ILilyEObject notifier)
	{
		if (loaded && notifier instanceof LilyEObject child)
		{
			child.disposeExtenderManager();
		}
	}

	@Override
	public final <T extends IAdapter> T adaptGeneric(final Class<? extends IAdapter> type)
	{
		@SuppressWarnings("unchecked") final var adapt = (T) adapt(type);
		return adapt;
	}

	@Override
	public final <T extends IAdapter> T adapt(final Class<T> type)
	{
		return adapterManager().adapt(type);
	}

	@Override
	public final <T extends IAdapter> T adapt(final Class<T> type, final String identifier)
	{
		return adapterManager().adapt(type, identifier);
	}

	@Override
	public final <T extends IAdapter> T adaptNotNullGeneric(final Class<? extends IAdapter> type)
	{
		@SuppressWarnings("unchecked") final var adapt = (T) adaptNotNull(type);
		return adapt;
	}

	@Override
	public final <T extends IAdapter> T adaptNotNull(final Class<T> type)
	{
		final T adapt = adapt(type);
		if (adapt == null)
		{
			var message = String.format(CANNOT_FIND_ADAPTER, type.getSimpleName(), eClass().getName());
			if (this instanceof LNamedElement)
			{
				message += ": " + ((LNamedElement) this).getName();
			}
			throw new NullPointerException(message);
		}
		return adapt;
	}

	@Override
	public final <T extends IAdapter> T adaptNotNull(final Class<T> type, final String identifier)
	{
		final T adapt = adapt(type, identifier);
		if (adapt == null)
		{
			var message = String.format(CANNOT_FIND_IDENTIFIED_ADAPTER,
										type.getSimpleName(),
										identifier,
										eClass().getName());
			if (this instanceof LNamedElement)
			{
				message += ": " + ((LNamedElement) this).getName();
			}
			throw new NullPointerException(message);
		}
		return adapt;
	}

	@Override
	public final BasicAdapterManager adapterManager()
	{
		if (extenderManager == null)
		{
			extenderManager = new BasicAdapterManager(this);
		}
		return extenderManager;
	}

	public final void loadExtenderManager()
	{
		treeIterator().forEachRemaining(object -> ((LilyEObject) object).load());
	}

	public final void disposeExtenderManager()
	{
		treeIterator().forEachRemaining(object -> ((LilyEObject) object).dispose());
	}

	private void load()
	{
		if (!loaded)
		{
			loaded = true;
			adapterManager().load();
		}
	}

	private void dispose()
	{
		if (loaded)
		{
			adapterManager().dispose();
			loaded = false;
		}
	}

	@Override
	public final Stream<ILilyEObject> streamTree()
	{
		return StreamSupport.stream(treeIterator(), false);
	}

	private TreeLazyIterator treeIterator()
	{
		return new TreeLazyIterator(this);
	}

	@Override
	public final Stream<ILilyEObject> streamChildren()
	{
		return eClass().getEAllContainments()
					   .stream()
					   .flatMap(this::streamReference);
	}

	@SuppressWarnings("unchecked")
	private Stream<ILilyEObject> streamReference(EReference ref)
	{
		if (ref.isMany())
		{
			return ((List<ILilyEObject>) eGet(ref)).stream();
		}
		else
		{
			return Stream.ofNullable((ILilyEObject) eGet(ref));
		}
	}
}
