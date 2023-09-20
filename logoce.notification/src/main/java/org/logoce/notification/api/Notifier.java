package org.logoce.notification.api;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

public class Notifier<Type extends IFeatures<?>> implements INotifier.Internal<Type>
{
	private final ListenerMap<Type> listenerMap;

	public Notifier(final List<IFeature<?, ? super Type>> features)
	{
		listenerMap = new ListenerMap<>(features);
	}

	@Override
	public <Callback> void listen(final Callback listener, final IFeature<? super Callback, ? super Type> feature)
	{
		listenerMap.listen(listener, feature);
	}

	@Override
	public <Callback> void listen(final Callback listener,
								  final Collection<? extends IFeature<? super Callback, ? super Type>> features)
	{
		listenerMap.listen(listener, features);
	}

	@Override
	public void listenNoParam(final Runnable listener, final IFeature<?, ? super Type> feature)
	{
		listenerMap.listenNoParam(listener, feature);
	}

	@Override
	public void listenNoParam(final Runnable listener, final Collection<? extends IFeature<?, ? super Type>> features)
	{
		listenerMap.listenNoParam(listener, features);
	}

	@Override
	public <Callback> void sulk(final Callback listener, final IFeature<? super Callback, ? super Type> feature)
	{
		listenerMap.sulk(listener, feature);
	}

	@Override
	public <Callback> void sulk(final Callback listener,
								final Collection<? extends IFeature<? super Callback, ? super Type>> features)
	{
		listenerMap.sulk(listener, features);
	}

	@Override
	public void sulkNoParam(final Runnable listener, final IFeature<?, ? super Type> feature)
	{
		listenerMap.sulkNoParam(listener, feature);
	}

	@Override
	public void sulkNoParam(final Runnable listener, final Collection<? extends IFeature<?, ? super Type>> features)
	{
		listenerMap.sulkNoParam(listener, features);
	}

	@Override
	public <Callback> void notify(final IFeature<? super Callback, ? super Type> feature,
								  final Consumer<Callback> caller)
	{
		listenerMap.notify(feature, caller);
	}

	@Override
	public <T> void notify(final IFeature<Consumer<T>, ? super Type> feature, final T value)
	{
		listenerMap.notify(feature, value);
	}

	@Override
	public void notify(final IFeature<IntConsumer, ? super Type> feature, final int value)
	{
		listenerMap.notify(feature, value);
	}

	@Override
	public void notify(final IFeature<LongConsumer, ? super Type> feature, final long value)
	{
		listenerMap.notify(feature, value);
	}

	@Override
	public void notify(final IFeature<BooleanConsumer, ? super Type> feature, final boolean value)
	{
		listenerMap.notify(feature, value);
	}

	@Override
	public void notify(final IFeature<Runnable, ? super Type> feature)
	{
		listenerMap.notify(feature);
	}
}
