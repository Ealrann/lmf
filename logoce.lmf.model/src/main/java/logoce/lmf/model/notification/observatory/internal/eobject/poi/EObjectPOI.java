package logoce.lmf.model.notification.observatory.internal.eobject.poi;

import logoce.lmf.model.api.feature.RawFeature;
import logoce.lmf.model.api.notification.Notification;
import logoce.lmf.model.lang.LMObject;

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
