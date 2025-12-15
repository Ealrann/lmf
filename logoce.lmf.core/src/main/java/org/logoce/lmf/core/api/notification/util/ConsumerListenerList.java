package org.logoce.lmf.core.api.notification.util;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

public final class ConsumerListenerList<Type>
{
	private final Deque<Object> listeners = new ConcurrentLinkedDeque<>();

	@SuppressWarnings("unchecked")
	public void notify(final Type value)
	{
		for (final var listener : listeners)
		{
			if (listener instanceof Runnable runnable)
			{
				runnable.run();
			}
			else
			{
				((Consumer<? super Type>) listener).accept(value);
			}
		}
	}

	public void listen(Consumer<? super Type> listener)
	{
		listeners.add(listener);
	}

	public void listenNoParam(Runnable listener)
	{
		listeners.add(listener);
	}

	public void sulk(Consumer<? super Type> listener)
	{
		listeners.remove(listener);
	}

	public void sulkNoParam(Runnable listener)
	{
		listeners.remove(listener);
	}
}
