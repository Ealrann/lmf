package org.logoce.lmf.core.api.notification;

import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

public final class ListenerMap<Type extends IFeatures<?>> implements INotifier.Internal<Type>
{
	private final Deque<Object>[] listenerMap;
	private final List<IFeature<?, ? super Type>> features;

	@SuppressWarnings({"unchecked", "rawtypes"})
	public ListenerMap(final List<IFeature<?, ? super Type>> features)
	{
		this.listenerMap = new Deque[features.size()];
		this.features = features;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <Callback> void notify(final IFeature<? super Callback, ? super Type> feature,
								  final Consumer<Callback> caller)
	{
		final var listeners = listenerMap[features.indexOf(feature)];
		if (listeners != null)
		{
			for (final var listener : listeners)
			{
				if (listener instanceof Runnable runnable)
				{
					runnable.run();
				}
				else
				{
					caller.accept((Callback) listener);
				}
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> void notify(final IFeature<Consumer<T>, ? super Type> feature, final T value)
	{
		final var listeners = listenerMap[features.indexOf(feature)];
		if (listeners != null)
		{
			for (final var listener : listeners)
			{
				if (listener instanceof Runnable runnable)
				{
					runnable.run();
				}
				else
				{
					((Consumer<? super T>) listener).accept(value);
				}
			}
		}
	}

	@Override
	public void notify(final IFeature<IntConsumer, ? super Type> feature, final int value)
	{
		final var listeners = listenerMap[features.indexOf(feature)];
		if (listeners != null)
		{
			for (final var listener : listeners)
			{
				if (listener instanceof Runnable runnable)
				{
					runnable.run();
				}
				else
				{
					((IntConsumer) listener).accept(value);
				}
			}
		}
	}

	@Override
	public void notify(final IFeature<LongConsumer, ? super Type> feature, final long value)
	{
		final var listeners = listenerMap[features.indexOf(feature)];
		if (listeners != null)
		{
			for (final var listener : listeners)
			{
				if (listener instanceof Runnable runnable)
				{
					runnable.run();
				}
				else
				{
					((LongConsumer) listener).accept(value);
				}
			}
		}
	}

	@Override
	public void notify(final IFeature<BooleanConsumer, ? super Type> feature, final boolean value)
	{
		final var listeners = listenerMap[features.indexOf(feature)];
		if (listeners != null)
		{
			for (final var listener : listeners)
			{
				if (listener instanceof Runnable runnable)
				{
					runnable.run();
				}
				else
				{
					((BooleanConsumer) listener).accept(value);
				}
			}
		}
	}

	@Override
	public void notify(final IFeature<Runnable, ? super Type> feature)
	{
		final var listeners = listenerMap[features.indexOf(feature)];
		if (listeners != null)
		{
			for (final var listener : listeners)
			{
				((Runnable) listener).run();
			}
		}
	}

	@Override
	public <Callback> void listen(final Callback listener, final IFeature<? super Callback, ? super Type> feature)
	{
		getOrCreateList(feature).add(listener);
	}

	@Override
	public void listenNoParam(final Runnable listener, final IFeature<?, ? super Type> feature)
	{
		getOrCreateList(feature).add(listener);
	}

	@Override
	public <Callback> void listen(final Callback listener,
								  final Collection<? extends IFeature<? super Callback, ? super Type>> features)
	{
		for (var feature : features)
		{
			listen(listener, feature);
		}
	}

	@Override
	public void listenNoParam(final Runnable listener, final Collection<? extends IFeature<?, ? super Type>> features)
	{
		for (var feature : features)
		{
			listenNoParam(listener, feature);
		}
	}

	@Override
	public <Callback> void sulk(final Callback listener, final IFeature<? super Callback, ? super Type> feature)
	{
		getList(feature).ifPresent(list -> list.remove(listener));
	}

	@Override
	public void sulkNoParam(final Runnable listener, final IFeature<?, ? super Type> feature)
	{
		getList(feature).ifPresent(list -> list.remove(listener));
	}

	@Override
	public <Callback> void sulk(final Callback listener,
								final Collection<? extends IFeature<? super Callback, ? super Type>> features)
	{
		for (var feature : features)
		{
			sulk(listener, feature);
		}
	}

	@Override
	public void sulkNoParam(final Runnable listener, final Collection<? extends IFeature<?, ? super Type>> features)
	{
		for (var feature : features)
		{
			sulkNoParam(listener, feature);
		}
	}

	private Deque<Object> getOrCreateList(final IFeature<?, ? super Type> feature)
	{
		final int ordinal = features.indexOf(feature);
		final var res = listenerMap[ordinal];
		if (res != null)
		{
			return res;
		}
		else
		{
			final var newList = new ConcurrentLinkedDeque<>();
			listenerMap[ordinal] = newList;
			return newList;
		}
	}

	private Optional<Deque<Object>> getList(final IFeature<?, ? super Type> feature)
	{
		final int ordinal = features.indexOf(feature);
		final var res = listenerMap[ordinal];
		if (res != null)
		{
			return Optional.of(res);
		}
		else
		{
			return Optional.empty();
		}
	}
}
