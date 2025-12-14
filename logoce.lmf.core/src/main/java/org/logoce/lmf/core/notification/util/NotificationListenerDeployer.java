package org.logoce.lmf.core.notification.util;

import org.logoce.lmf.core.api.notification.Notification;
import org.logoce.lmf.core.lang.LMObject;

import java.util.function.Consumer;

public final class NotificationListenerDeployer
{
	private final Consumer<Notification> listener;
	private final int[] featuresToListen;
	private final ModelStructureObserver structureObserver;

	public NotificationListenerDeployer(final int[] structuralFeatures,
										final Consumer<Notification> listener,
										final int... featuresToListen)
	{
		structureObserver = new ModelStructureObserver(structuralFeatures, this::add, this::remove);
		this.listener = listener;
		this.featuresToListen = featuresToListen.clone();
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
		newValue.notifier().listen(listener, featuresToListen);
	}

	private void remove(LMObject oldValue)
	{
		oldValue.notifier().sulk(listener, featuresToListen);
	}
}
