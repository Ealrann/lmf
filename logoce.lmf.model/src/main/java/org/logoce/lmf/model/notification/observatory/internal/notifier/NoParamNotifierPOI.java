package org.logoce.lmf.model.notification.observatory.internal.notifier;

import org.logoce.lmf.notification.api.IFeature;
import org.logoce.lmf.notification.api.IFeatures;
import org.logoce.lmf.notification.api.INotifier;

public final class NoParamNotifierPOI<Type extends IFeatures<?>> implements INotifierPOI<Type>
{
	private final Runnable listener;
	private final IFeature<?, ? super Type> feature;

	public NoParamNotifierPOI(final Runnable listener, IFeature<?, ? super Type> feature)
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
