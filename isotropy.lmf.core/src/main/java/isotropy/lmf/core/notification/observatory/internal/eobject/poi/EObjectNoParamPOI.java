package isotropy.lmf.core.notification.observatory.internal.eobject.poi;

import isotropy.lmf.core.api.feature.RawFeature;
import isotropy.lmf.core.lang.LMObject;

import java.util.List;

public final class EObjectNoParamPOI implements IEObjectPOI
{
	private final Runnable listener;
	private final List<RawFeature<?, ?>> features;

	public EObjectNoParamPOI(Runnable listener, List<RawFeature<?, ?>> features)
	{
		this.listener = listener;
		this.features = List.copyOf(features);
	}

	@Override
	public void listen(final LMObject object)
	{
		object.listenNoParam(listener, features);
	}

	@Override
	public void sulk(final LMObject object)
	{
		object.sulkNoParam(listener, features);
	}
}
