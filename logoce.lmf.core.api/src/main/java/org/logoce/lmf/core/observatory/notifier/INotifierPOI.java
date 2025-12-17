package org.logoce.lmf.core.observatory.notifier;

import org.logoce.lmf.core.api.notification.IFeatures;
import org.logoce.lmf.core.api.notification.INotifier;

public interface INotifierPOI<T extends IFeatures<?>>
{
	void listen(INotifier<? extends T> notifier);
	void sulk(INotifier<? extends T> notifier);
}
