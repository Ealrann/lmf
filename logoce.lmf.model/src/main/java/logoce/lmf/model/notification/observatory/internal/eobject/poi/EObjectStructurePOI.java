package logoce.lmf.model.notification.observatory.internal.eobject.poi;

import logoce.lmf.model.api.notification.Notification;
import logoce.lmf.model.lang.LMObject;
import logoce.lmf.model.util.ModelUtils;

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
		final var containmentFeatures = ModelUtils.streamContainmentFeatures(group).toList();
		object.listen(structureChanged, containmentFeatures);
	}

	@Override
	public void sulk(final LMObject object)
	{
		final var group = object.lmGroup();
		final var containmentFeatures = ModelUtils.streamContainmentFeatures(group).toList();
		object.sulk(structureChanged, containmentFeatures);
	}
}
