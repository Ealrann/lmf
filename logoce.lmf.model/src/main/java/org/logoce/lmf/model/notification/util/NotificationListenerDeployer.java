package org.logoce.lmf.model.notification.util;

import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.LMObject;

import java.util.List;
import java.util.function.Consumer;

public final class NotificationListenerDeployer
{
	private final Consumer<Notification> listener;
	private final List<RawFeature<?, ?>> featuresToListen;
	private final ModelStructureObserver structureObserver;

	public NotificationListenerDeployer(List<RawFeature<?, ?>> structuralFeatures,
										Consumer<Notification> listener,
										List<RawFeature<?, ?>> featuresToListen)
	{
		structureObserver = new ModelStructureObserver(structuralFeatures, this::add, this::remove);
		this.listener = listener;
		this.featuresToListen = featuresToListen;
	}

	public void startDeploy(LMObject root)
	{
		structureObserver.startObserve(root);
	}

	public void stopDeploy(LMObject root)
	{
		structureObserver.stopObserve(root);
	}

	private void add(LMObject newValue)
	{
		newValue.listen(listener, featuresToListen);
	}

	private void remove(LMObject oldValue)
	{
		oldValue.sulk(listener, featuresToListen);
	}
}
