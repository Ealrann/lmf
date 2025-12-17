package org.logoce.lmf.core.observatory.notifier;

import org.logoce.lmf.core.api.notification.IFeature;
import org.logoce.lmf.core.api.notification.IFeatures;
import org.logoce.lmf.core.api.notification.INotifier;

public final class NotifierPOI<Callback, Type extends IFeatures<?>> implements INotifierPOI<Type>
{
	private final Callback listener;
	private final IFeature<Callback, ? super Type> feature;

	public NotifierPOI(final Callback listener, IFeature<Callback, ? super Type> feature)
	{
		this.listener = listener;
		this.feature = feature;
	}

	@Override
	public void listen(INotifier<? extends Type> notifier)
	{
		notifier.listen(listener, feature);
	}

	@Override
	public void sulk(INotifier<? extends Type> notifier)
	{
		notifier.sulk(listener, feature);
	}
}
