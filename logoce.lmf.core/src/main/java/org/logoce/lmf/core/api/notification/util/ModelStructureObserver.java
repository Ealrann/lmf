package org.logoce.lmf.core.api.notification.util;

import org.logoce.lmf.core.api.notification.Notification;
import org.logoce.lmf.core.lang.LMObject;
import java.util.function.Consumer;

public final class ModelStructureObserver
{
	private final ModelObserver observer;
	private final Consumer<LMObject> onAddedObject;
	private final Consumer<LMObject> onRemovedObject;

	public ModelStructureObserver(final int feature,
								  final Consumer<LMObject> onAddedObject,
								  final Consumer<LMObject> onRemovedObject)
	{
		this(new int[]{feature}, onAddedObject, onRemovedObject);
	}

	public ModelStructureObserver(final int[] features,
								  final Consumer<LMObject> onAddedObject,
								  final Consumer<LMObject> onRemovedObject)
	{
		this.observer = new ModelObserver(this::notifyChanged, features);
		this.onAddedObject = onAddedObject;
		this.onRemovedObject = onRemovedObject;
	}

	public void startObserve(LMObject root)
	{
		observer.startObserve(root);
	}

	public void stopObserve(LMObject root)
	{
		observer.stopObserve(root);
	}

	private void notifyChanged(Notification notification)
	{
		NotificationUnifier.unify(notification, onAddedObject, onRemovedObject);
	}
}
