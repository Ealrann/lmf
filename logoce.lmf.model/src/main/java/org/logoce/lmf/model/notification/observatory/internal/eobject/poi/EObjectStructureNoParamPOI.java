package org.logoce.lmf.model.notification.observatory.internal.eobject.poi;

import org.logoce.lmf.model.lang.LMObject;
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
		@SuppressWarnings("unchecked")
		final var containmentFeatures = (java.util.List<org.logoce.lmf.model.lang.Feature<?, ?>>)
				(java.util.List<?>) ModelUtil.streamContainmentFeatures(group).toList();
		object.listenNoParam(listener, containmentFeatures);
	}

	@Override
	public void sulk(final LMObject object)
	{
		final var group = object.lmGroup();
		@SuppressWarnings("unchecked")
		final var containmentFeatures = (java.util.List<org.logoce.lmf.model.lang.Feature<?, ?>>)
				(java.util.List<?>) ModelUtil.streamContainmentFeatures(group).toList();
		object.sulkNoParam(listener, containmentFeatures);
	}
}
