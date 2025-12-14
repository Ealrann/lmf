package org.logoce.lmf.core.notification.observatory.internal.eobject.poi;

import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.util.ModelUtil;

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
		final var containmentFeatures = ModelUtil.streamContainmentFeatures(group)
												.mapToInt(Relation::id)
												.toArray();
		object.notifier().listenNoParam(listener, containmentFeatures);
	}

	@Override
	public void sulk(final LMObject object)
	{
		final var group = object.lmGroup();
		final var containmentFeatures = ModelUtil.streamContainmentFeatures(group)
												.mapToInt(Relation::id)
												.toArray();
		object.notifier().sulkNoParam(listener, containmentFeatures);
	}
}
