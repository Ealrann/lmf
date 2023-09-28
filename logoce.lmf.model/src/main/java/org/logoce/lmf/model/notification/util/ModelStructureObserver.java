package org.logoce.lmf.model.notification.util;

import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.LMObject;

import java.util.List;
import java.util.function.Consumer;

public final class ModelStructureObserver
{
	private final ModelObserver observer;
	private final Consumer<LMObject> onAddedObject;
	private final Consumer<LMObject> onRemovedObject;

	public ModelStructureObserver(RawFeature<?, ?> feature,
								  Consumer<LMObject> onAddedObject,
								  Consumer<LMObject> onRemovedObject)
	{
		this(List.of(feature), onAddedObject, onRemovedObject);
	}

	public ModelStructureObserver(List<RawFeature<?, ?>> features,
								  Consumer<LMObject> onAddedObject,
								  Consumer<LMObject> onRemovedObject)
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
