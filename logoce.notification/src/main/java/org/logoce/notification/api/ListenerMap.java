package org.logoce.notification.api;

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
	private final List<Feature<?, ? super Type>> features;

	@SuppressWarnings("unchecked")
	public ListenerMap(final List<Feature<?, ? super Type>> features)
	{
		this.listenerMap = new Deque[features.size()];
		this.features = features;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <Callback> void notify(final Feature<? super Callback, ? super Type> feature,
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
	public <T> void notify(final Feature<Consumer<T>, ? super Type> feature, final T value)
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
	public void notify(final Feature<IntConsumer, ? super Type> feature, final int value)
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
	public void notify(final Feature<LongConsumer, ? super Type> feature, final long value)
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
	public void notify(final Feature<BooleanConsumer, ? super Type> feature, final boolean value)
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
	public void notify(final Feature<Runnable, ? super Type> feature)
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
	public <Callback> void listen(final Callback listener, final Feature<? super Callback, ? super Type> feature)
	{
		getOrCreateList(feature).add(listener);
	}

	@Override
	public void listenNoParam(final Runnable listener, final Feature<?, ? super Type> feature)
	{
		getOrCreateList(feature).add(listener);
	}

	@Override
	public <Callback> void listen(final Callback listener,
								  final Collection<? extends Feature<? super Callback, ? super Type>> features)
	{
		for (var feature : features)
		{
			listen(listener, feature);
		}
	}

	@Override
	public void listenNoParam(final Runnable listener, final Collection<? extends Feature<?, ? super Type>> features)
	{
		for (var feature : features)
		{
			listenNoParam(listener, feature);
		}
	}

	@Override
	public <Callback> void sulk(final Callback listener, final Feature<? super Callback, ? super Type> feature)
	{
		getList(feature).ifPresent(list -> list.remove(listener));
	}

	@Override
	public void sulkNoParam(final Runnable listener, final Feature<?, ? super Type> feature)
	{
		getList(feature).ifPresent(list -> list.remove(listener));
	}

	@Override
	public <Callback> void sulk(final Callback listener,
								final Collection<? extends Feature<? super Callback, ? super Type>> features)
	{
		for (var feature : features)
		{
			sulk(listener, feature);
		}
	}

	@Override
	public void sulkNoParam(final Runnable listener, final Collection<? extends Feature<?, ? super Type>> features)
	{
		for (var feature : features)
		{
			sulkNoParam(listener, feature);
		}
	}

	private Deque<Object> getOrCreateList(final Feature<?, ? super Type> feature)
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

	private Optional<Deque<Object>> getList(final Feature<?, ? super Type> feature)
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
