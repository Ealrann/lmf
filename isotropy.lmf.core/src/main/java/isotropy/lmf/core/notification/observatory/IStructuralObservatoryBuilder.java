package isotropy.lmf.core.notification.observatory;

import org.eclipse.emf.common.notify.Notification;
import org.logoce.extender.api.IAdapter;
import org.logoce.notification.api.IFeatures;
import org.logoce.notification.api.INotifier;
import org.sheepy.lily.core.api.model.ILilyEObject;

import java.util.function.Consumer;

public interface IStructuralObservatoryBuilder<C extends IStructuralObservatoryBuilder<C>>
{
	IEObjectObservatoryBuilder<ILilyEObject> explore(int referenceId);
	<Target extends ILilyEObject> IEObjectObservatoryBuilder<Target> explore(int referenceId, Class<Target> cast);
	IEObjectObservatoryBuilder<ILilyEObject> exploreParent();
	<Target extends ILilyEObject> IEObjectObservatoryBuilder<Target> exploreParent(Class<Target> cast);

	<T extends IAdapter> IAdapterObservatoryBuilder<T> adapt(Class<T> classifier);
	<F extends IFeatures<?>, T extends IAdapter & INotifier<? extends F>> INotifierAdapterObservatoryBuilder<F, T> adaptNotifier(
			Class<T> classifier);

	C listen(Consumer<Notification> listener, int... features);
	C listenNoParam(Runnable listener, int... features);
}
