package isotropy.lmf.core.notification.observatory.internal.notifier;

import org.logoce.notification.api.IFeatures;
import org.logoce.notification.api.INotifier;

public interface INotifierPOI<T extends IFeatures<?>>
{
	void listen(INotifier<? extends T> notifier);
	void sulk(INotifier<? extends T> notifier);
}
