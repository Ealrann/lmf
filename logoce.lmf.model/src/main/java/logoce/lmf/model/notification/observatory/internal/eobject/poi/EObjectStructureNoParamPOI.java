package logoce.lmf.model.notification.observatory.internal.eobject.poi;

import logoce.lmf.model.lang.LMObject;
import logoce.lmf.model.util.ModelUtils;

public final class EObjectStructureNoParamPOI implements IEObjectPOI
{
	private final Runnable listener;

	public EObjectStructureNoParamPOI(Runnable listener)
	{
		this.listener = listener;
	}

	@Override
	public void listen(final LMObject object)
	{
		final var group = object.lmGroup();
		final var containmentFeatures = ModelUtils.streamContainmentFeatures(group).toList();
		object.listenNoParam(listener, containmentFeatures);
	}

	@Override
	public void sulk(final LMObject object)
	{
		final var group = object.lmGroup();
		final var containmentFeatures = ModelUtils.streamContainmentFeatures(group).toList();
		object.sulkNoParam(listener, containmentFeatures);
	}
}
