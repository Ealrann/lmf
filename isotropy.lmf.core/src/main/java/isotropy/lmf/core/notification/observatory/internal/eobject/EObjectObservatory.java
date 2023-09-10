package isotropy.lmf.core.notification.observatory.internal.eobject;

import org.eclipse.emf.ecore.EReference;
import org.sheepy.lily.core.api.model.ILilyEObject;
import org.sheepy.lily.core.api.notification.observatory.IEObjectObservatoryBuilder;
import org.sheepy.lily.core.api.notification.observatory.IObservatory;
import org.sheepy.lily.core.api.notification.observatory.internal.InternalObservatoryBuilder;
import org.sheepy.lily.core.api.notification.observatory.internal.eobject.listener.GatherBulkListener;
import org.sheepy.lily.core.api.notification.observatory.internal.eobject.listener.GatherListener;
import org.sheepy.lily.core.api.notification.observatory.internal.eobject.poi.IEObjectPOI;
import org.sheepy.lily.core.api.notification.util.ModelStructureBulkObserver;

import java.util.List;

public final class EObjectObservatory<T extends ILilyEObject> extends AbstractEObjectObservatory<T>
{
	private final ModelStructureBulkObserver structureObserver;
	private final int referenceId;

	public EObjectObservatory(final int referenceId,
							  final Class<T> cast,
							  final List<IObservatory> children,
							  final List<IEObjectPOI> pois,
							  final List<GatherListener<T>> gatherListeners,
							  final List<GatherBulkListener<T>> gatherBulkListeners)
	{
		super(cast, children, pois, gatherListeners, gatherBulkListeners);
		structureObserver = new ModelStructureBulkObserver(referenceId, this::startObserve, this::stopObserve);
		this.referenceId = referenceId;
	}

	@Override
	public void observe(final ILilyEObject parent)
	{
		assert checkParent(parent);
		structureObserver.startObserve(parent);
	}

	@Override
	public void shut(final ILilyEObject parent)
	{
		structureObserver.stopObserve(parent);
	}

	@SuppressWarnings("SameReturnValue")
	private boolean checkParent(final ILilyEObject parent)
	{
		final var eStructuralFeature = parent.eClass().getEStructuralFeature(referenceId);
		if (eStructuralFeature instanceof EReference)
		{
			return true;
		}
		else
		{
			throw new IllegalArgumentException("Observation failed, the explored feature " + referenceId + " on " + parent
					.eClass()
					.getName() + " is not a EReference.");
		}
	}

	private void startObserve(List<? extends ILilyEObject> objects)
	{
		register(objects);
	}

	private void stopObserve(List<? extends ILilyEObject> objects)
	{
		unregister(objects);
	}

	public static final class Builder<T extends ILilyEObject> extends AbstractEObjectObservatory.Builder<T> implements
																											IEObjectObservatoryBuilder<T>,
																											InternalObservatoryBuilder
	{
		private final int referenceId;

		public Builder(int referenceId, Class<T> cast)
		{
			super(cast);
			assert referenceId >= 0;
			this.referenceId = referenceId;
		}

		@Override
		public IObservatory build()
		{
			final var builtChildren = children.stream()
											  .map(InternalObservatoryBuilder::build)
											  .toList();

			return new EObjectObservatory<>(referenceId,
											cast,
											builtChildren,
											pois,
											gatherListeners,
											gatherBulkListeners);
		}
	}
}
