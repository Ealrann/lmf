package org.logoce.notification.api;

import java.util.Collection;

public class DummyNotifier<Type extends IFeatures<?>> implements INotifier<Type>
{
	public DummyNotifier()
	{
	}

	@Override
	public <Callback> void listen(final Callback listener, final Feature<? super Callback, ? super Type> feature)
	{
	}

	@Override
	public <Callback> void listen(final Callback listener,
								  final Collection<? extends Feature<? super Callback, ? super Type>> features)
	{
	}

	@Override
	public void listenNoParam(final Runnable listener, final Feature<?, ? super Type> feature)
	{
	}

	@Override
	public void listenNoParam(final Runnable listener, final Collection<? extends Feature<?, ? super Type>> features)
	{
	}

	@Override
	public <Callback> void sulk(final Callback listener, final Feature<? super Callback, ? super Type> feature)
	{
	}

	@Override
	public <Callback> void sulk(final Callback listener,
								final Collection<? extends Feature<? super Callback, ? super Type>> features)
	{
	}

	@Override
	public void sulkNoParam(final Runnable listener, final Feature<?, ? super Type> feature)
	{
	}

	@Override
	public void sulkNoParam(final Runnable listener, final Collection<? extends Feature<?, ? super Type>> features)
	{
	}
}
