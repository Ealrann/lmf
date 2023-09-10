package isotropy.lmf.core.notification.observatory.internal.eobject.poi;

import org.sheepy.lily.core.api.model.ILilyEObject;

public final class EObjectStructureNoParamPOI implements IEObjectPOI
{
	private final Runnable listener;

	public EObjectStructureNoParamPOI(Runnable listener)
	{
		this.listener = listener;
	}

	@Override
	public void listen(final ILilyEObject object)
	{
		final var eClass = object.eClass();
		final var containmentFeatures = eClass.getEAllContainments().stream().mapToInt(eClass::getFeatureID).toArray();
		object.listenNoParam(listener, containmentFeatures);
	}

	@Override
	public void sulk(final ILilyEObject object)
	{
		final var eClass = object.eClass();
		final var containmentFeatures = eClass.getEAllContainments().stream().mapToInt(eClass::getFeatureID).toArray();
		object.sulkNoParam(listener, containmentFeatures);
	}
}
