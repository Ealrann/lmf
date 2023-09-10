package isotropy.lmf.core.notification.observatory.internal.eobject.poi;

import org.eclipse.emf.common.notify.Notification;
import org.sheepy.lily.core.api.model.ILilyEObject;

import java.util.function.Consumer;

public final class EObjectPOI implements IEObjectPOI
{
	private final Consumer<Notification> listener;
	private final int[] features;

	public EObjectPOI(Consumer<Notification> listener, int... features)
	{
		this.listener = listener;
		this.features = features;
	}

	@Override
	public void listen(final ILilyEObject object)
	{
		object.listen(listener, features);
	}

	@Override
	public void sulk(final ILilyEObject object)
	{
		object.sulk(listener, features);
	}
}
