package isotropy.lmf.core.notification.observatory;

import isotropy.lmf.core.api.feature.RawFeature;
import isotropy.lmf.core.api.notification.Notification;
import isotropy.lmf.core.lang.LMObject;
import org.logoce.extender.api.IAdapter;
import org.logoce.notification.api.IFeatures;
import org.logoce.notification.api.INotifier;

import java.util.List;
import java.util.function.Consumer;

public interface IStructuralObservatoryBuilder<C extends IStructuralObservatoryBuilder<C>>
{
	IEObjectObservatoryBuilder<LMObject> explore(RawFeature<?, ?> relation);
	<Target extends LMObject> IEObjectObservatoryBuilder<Target> explore(RawFeature<?, ?> relation, Class<Target> cast);
	IEObjectObservatoryBuilder<LMObject> exploreParent();
	<Target extends LMObject> IEObjectObservatoryBuilder<Target> exploreParent(Class<Target> cast);

	<T extends IAdapter> IAdapterObservatoryBuilder<T> adapt(Class<T> classifier);
	<F extends IFeatures<?>, T extends IAdapter & INotifier<? extends F>> INotifierAdapterObservatoryBuilder<F, T> adaptNotifier(
			Class<T> classifier);

	C listen(Consumer<Notification> listener, List<RawFeature<?, ?>> features);
	C listenNoParam(Runnable listener, List<RawFeature<?, ?>> features);
}
