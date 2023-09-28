package org.logoce.lmf.model.notification.util;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

public final class ListenerList<Type>
{
	private final Deque<Object> listeners = new ConcurrentLinkedDeque<>();

	@SuppressWarnings("unchecked")
	public void notify(final Consumer<Type> listenerExecution)
	{
		for (final var listener : listeners)
		{
			if (listener instanceof Runnable runnable)
			{
				runnable.run();
			}
			else
			{
				listenerExecution.accept((Type) listener);
			}
		}
	}

	public void listen(Type listener)
	{
		listeners.add(listener);
	}

	public void listenNoParam(Runnable listener)
	{
		listeners.add(listener);
	}

	public void sulk(Type listener)
	{
		listeners.remove(listener);
	}

	public void sulkNoParam(Runnable listener)
	{
		listeners.remove(listener);
	}
}
