package org.logoce.lmf.model.notification.observatory.internal.eobject.poi;

import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.util.ModelUtil;

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
		object.listenNoParam(listener, containmentFeatures);
	}

	@Override
	public void sulk(final LMObject object)
	{
		final var group = object.lmGroup();
		final var containmentFeatures = ModelUtil.streamContainmentFeatures(group)
												.mapToInt(Relation::id)
												.toArray();
		object.sulkNoParam(listener, containmentFeatures);
	}
}
