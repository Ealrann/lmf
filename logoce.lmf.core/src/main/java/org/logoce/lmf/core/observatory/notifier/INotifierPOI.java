package org.logoce.lmf.core.observatory.notifier;

import org.logoce.lmf.notification.api.IFeatures;
import org.logoce.lmf.notification.api.INotifier;

public interface INotifierPOI<T extends IFeatures<?>>
{
	void listen(INotifier<? extends T> notifier);
	void sulk(INotifier<? extends T> notifier);
}
