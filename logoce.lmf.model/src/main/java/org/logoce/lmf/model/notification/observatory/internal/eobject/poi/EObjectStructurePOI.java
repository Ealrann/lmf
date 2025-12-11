package org.logoce.lmf.model.notification.observatory.internal.eobject.poi;

import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.util.ModelUtil;

import java.util.function.Consumer;

public final class EObjectStructurePOI implements IEObjectPOI
{
	private final Consumer<Notification> structureChanged;

	public EObjectStructurePOI(final Consumer<Notification> structureChanged)
	{
		this.structureChanged = structureChanged;
	}

	@Override
	public void listen(final LMObject object)
	{
		final var group = object.lmGroup();
		final var containmentFeatures = ModelUtil.streamContainmentFeatures(group)
												.mapToInt(Relation::id)
												.toArray();
		object.notifier().listen(structureChanged, containmentFeatures);
	}

	@Override
	public void sulk(final LMObject object)
	{
		final var group = object.lmGroup();
		final var containmentFeatures = ModelUtil.streamContainmentFeatures(group)
												.mapToInt(Relation::id)
												.toArray();
		object.notifier().sulk(structureChanged, containmentFeatures);
	}
}
