package isotropy.lmf.core.api.model;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.sheepy.lily.core.api.notification.util.EMFListenerMap;

import java.util.function.Consumer;

public abstract class LilyBasicNotifier extends EObjectImpl implements IEMFNotifier
{
	private final EMFListenerMap listenerMap;

	public LilyBasicNotifier()
	{
		final int featureCount = eStaticClass().getEAllStructuralFeatures().size();
		listenerMap = new EMFListenerMap(featureCount);
	}

	@Override
	public void eNotify(final Notification notification)
	{
		if (eDeliver()) listenerMap.notify(notification);
		super.eNotify(notification);
	}

	@Override
	public boolean eNotificationRequired()
	{
		return eDeliver() || super.eNotificationRequired();
	}

	@Override
	public final void listen(Consumer<Notification> listener, int... features)
	{
		listenerMap.listen(listener, features);
	}

	@Override
	public final void sulk(Consumer<Notification> listener, int... features)
	{
		listenerMap.sulk(listener, features);
	}

	@Override
	public final void listenNoParam(Runnable listener, int... features)
	{
		listenerMap.listenNoParam(listener, features);
	}

	@Override
	public final void sulkNoParam(Runnable listener, int... features)
	{
		listenerMap.sulkNoParam(listener, features);
	}
}
