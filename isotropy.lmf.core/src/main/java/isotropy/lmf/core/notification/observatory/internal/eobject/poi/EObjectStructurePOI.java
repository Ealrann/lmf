package isotropy.lmf.core.notification.observatory.internal.eobject.poi;

import org.eclipse.emf.common.notify.Notification;
import org.sheepy.lily.core.api.model.ILilyEObject;

import java.util.function.Consumer;

public final class EObjectStructurePOI implements IEObjectPOI
{
	private final Consumer<Notification> structureChanged;

	public EObjectStructurePOI(final Consumer<Notification> structureChanged)
	{
		this.structureChanged = structureChanged;
	}

	@Override
	public void listen(final ILilyEObject object)
	{
		final var eClass = object.eClass();
		final var containmentFeatures = eClass.getEAllContainments().stream().mapToInt(eClass::getFeatureID).toArray();
		object.listen(structureChanged, containmentFeatures);
	}

	@Override
	public void sulk(final ILilyEObject object)
	{
		final var eClass = object.eClass();
		final var containmentFeatures = eClass.getEAllContainments().stream().mapToInt(eClass::getFeatureID).toArray();
		object.sulk(structureChanged, containmentFeatures);
	}
}
