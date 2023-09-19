package isotropy.lmf.core.notification.observatory.internal.eobject.poi;

import isotropy.lmf.core.api.feature.RawFeature;
import isotropy.lmf.core.api.notification.Notification;
import isotropy.lmf.core.lang.LMObject;

import java.util.List;
import java.util.function.Consumer;

public final class EObjectPOI implements IEObjectPOI
{
	private final Consumer<Notification> listener;
	private final List<RawFeature<?, ?>> features;

	public EObjectPOI(Consumer<Notification> listener, List<RawFeature<?, ?>> features)
	{
		this.listener = listener;
		this.features = List.copyOf(features);
	}

	@Override
	public void listen(final LMObject object)
	{
		object.listen(listener, features);
	}

	@Override
	public void sulk(final LMObject object)
	{
		object.sulk(listener, features);
	}
}
