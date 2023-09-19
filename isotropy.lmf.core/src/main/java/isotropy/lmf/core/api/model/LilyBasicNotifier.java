package isotropy.lmf.core.api.model;

import isotropy.lmf.core.api.feature.RawFeature;
import isotropy.lmf.core.api.notification.Notification;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.notification.util.EMFListenerMap;

import java.util.List;
import java.util.function.Consumer;

public abstract class LilyBasicNotifier implements IEMFNotifier
{
	private final EMFListenerMap listenerMap;

	private boolean deliver;

	public LilyBasicNotifier()
	{
		final int featureCount = 100;
		listenerMap = new EMFListenerMap(featureCount);
	}

	public void eNotify(final Notification notification)
	{
		if (eDeliver()) listenerMap.notify(notification);
	}

	public boolean eDeliver()
	{
		return deliver;
	}

	public void eDeliver(boolean deliver)
	{
		this.deliver = deliver;
	}

	@Override
	public final void listen(Consumer<Notification> listener, List<RawFeature<?, ?>> features)
	{
		listenerMap.listen(listener, features);
	}

	@Override
	public final void sulk(Consumer<Notification> listener, List<RawFeature<?, ?>> features)
	{
		listenerMap.sulk(listener, features);
	}

	@Override
	public final void listenNoParam(Runnable listener, List<RawFeature<?, ?>> features)
	{
		listenerMap.listenNoParam(listener, features);
	}

	@Override
	public final void sulkNoParam(Runnable listener, List<RawFeature<?, ?>> features)
	{
		listenerMap.sulkNoParam(listener, features);
	}

	public abstract Group<?> lmGroup();
}
