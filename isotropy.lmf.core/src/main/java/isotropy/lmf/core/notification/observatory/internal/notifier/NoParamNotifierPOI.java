package isotropy.lmf.core.notification.observatory.internal.notifier;

import org.logoce.notification.api.Feature;
import org.logoce.notification.api.IFeatures;
import org.logoce.notification.api.INotifier;

public final class NoParamNotifierPOI<Type extends IFeatures<?>> implements INotifierPOI<Type>
{
	private final Runnable listener;
	private final Feature<?, ? super Type> feature;

	public NoParamNotifierPOI(final Runnable listener, Feature<?, ? super Type> feature)
	{
		this.listener = listener;
		this.feature = feature;
	}

	@Override
	public void listen(INotifier<? extends Type> notifier)
	{
		notifier.listenNoParam(listener, feature);
	}

	@Override
	public void sulk(INotifier<? extends Type> notifier)
	{
		notifier.sulkNoParam(listener, feature);
	}
}
