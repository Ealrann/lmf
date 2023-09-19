package isotropy.lmf.core.notification.observatory.internal.eobject.poi;

import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.util.ModelUtils;

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
