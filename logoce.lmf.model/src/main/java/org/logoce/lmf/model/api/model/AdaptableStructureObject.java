package org.logoce.lmf.model.api.model;

import org.logoce.lmf.adapter.api.BasicAdapterManager;
import org.logoce.lmf.extender.api.IAdapter;
import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Named;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.notification.util.NotificationUnifier;
import org.logoce.lmf.model.util.ModelUtil;
import org.logoce.lmf.model.util.oldlogoce.TreeLazyIterator;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class AdaptableStructureObject extends LilyBasicNotifier implements ILilyEObject
{
	private static final String CANNOT_FIND_ADAPTER = "Cannot find adapter [%s] for class [%s]";
	private static final String CANNOT_FIND_IDENTIFIED_ADAPTER = "Cannot find adapter [%s] (id = %s) for class [%s]";

	private BasicAdapterManager extenderManager = null;
	private boolean loaded = false;

	protected AdaptableStructureObject()
	{
	}

	@Override
	protected final void eNotify(final Notification notification)
	{
		final var feature = notification.feature();
		final boolean isContainment = feature instanceof Relation<?, ?, ?, ?> relation && relation.contains();
		if (isContainment) NotificationUnifier.unifyAdded(notification, this::setupChild);
		super.eNotify(notification);
		if (isContainment) NotificationUnifier.unifyRemoved(notification, this::disposeChild);
	}

	private void setupChild(ILilyEObject notifier)
	{
		if (loaded && notifier instanceof AdaptableStructureObject child)
		{
			child.loadExtenderManager();
		}
	}

	private void disposeChild(ILilyEObject notifier)
	{
		if (loaded && notifier instanceof AdaptableStructureObject child)
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
			var message = String.format(CANNOT_FIND_ADAPTER, type.getSimpleName(), lmGroup().name());
			if (this instanceof Named)
			{
				message += ": " + ((Named) this).name();
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
										lmGroup().name());
			if (this instanceof Named)
			{
				message += ": " + ((Named) this).name();
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
		treeIterator().forEachRemaining(object -> ((AdaptableStructureObject) object).load());
	}

	public final void disposeExtenderManager()
	{
		treeIterator().forEachRemaining(object -> ((AdaptableStructureObject) object).dispose());
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
	public final Stream<LMObject> streamTree()
	{
		return StreamSupport.stream(treeIterator(), false);
	}

	private TreeLazyIterator treeIterator()
	{
		return new TreeLazyIterator((LMObject) this);
	}

	@Override
	public final Stream<LMObject> streamChildren()
	{
		return ModelUtil.streamContainmentFeatures(lmGroup())
						.map(Relation.class::cast)
						.flatMap(this::streamReference);
	}

	@SuppressWarnings("unchecked")
	private Stream<LMObject> streamReference(Relation<?, ?, ?, ?> ref)
	{
		if (ref.many())
		{
			return ((List<LMObject>) ((LMObject) this).get(ref)).stream();
		}
		else
		{
			return Stream.ofNullable((LMObject) ((LMObject) this).get(ref));
		}
	}
}
