package org.logoce.lmf.core.notification.list;

import org.logoce.lmf.core.api.notification.Notification.EventType;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public final class ObservableList<T> extends AbstractList<T>
{
	private final List<T> delegate = new ArrayList<>();
	private final BiConsumer<EventType, List<T>> listener;

	public ObservableList(final BiConsumer<EventType, List<T>> listener)
	{
		this.listener = Objects.requireNonNull(listener);
	}

	@Override
	public T get(final int index)
	{
		return delegate.get(index);
	}

	@Override
	public int size()
	{
		return delegate.size();
	}

	@Override
	public T set(final int index, final T element)
	{
		final var previous = delegate.set(index, element);
		if (!Objects.equals(previous, element))
		{
			notifyListener(EventType.REMOVE, List.of(previous));
			notifyListener(EventType.ADD, List.of(element));
		}
		return previous;
	}

	@Override
	public void add(final int index, final T element)
	{
		delegate.add(index, element);
		notifyListener(EventType.ADD, List.of(element));
	}

	@Override
	public T remove(final int index)
	{
		final var removed = delegate.remove(index);
		notifyListener(EventType.REMOVE, List.of(removed));
		return removed;
	}

	@Override
	public boolean add(final T element)
	{
		add(size(), element);
		return true;
	}

	@Override
	public boolean addAll(final Collection<? extends T> elements)
	{
		return addAll(size(), elements);
	}

	@Override
	public boolean addAll(final int index, final Collection<? extends T> elements)
	{
		if (elements.isEmpty())
		{
			return false;
		}

		final var added = new ArrayList<T>(elements.size());
		added.addAll(elements);

		final var changed = delegate.addAll(index, elements);
		if (changed)
		{
			final var eventType = added.size() == 1 ? EventType.ADD : EventType.ADD_MANY;
			notifyListener(eventType, added);
		}
		return changed;
	}

	@Override
	public boolean remove(final Object o)
	{
		final var index = delegate.indexOf(o);
		if (index < 0)
		{
			return false;
		}
		remove(index);
		return true;
	}

	@Override
	public boolean removeAll(final Collection<?> elements)
	{
		if (elements.isEmpty() || delegate.isEmpty())
		{
			return false;
		}

		final var removed = new ArrayList<T>();
		final Iterator<T> iterator = delegate.iterator();

		while (iterator.hasNext())
		{
			final var value = iterator.next();
			if (elements.contains(value))
			{
				iterator.remove();
				removed.add(value);
			}
		}

		if (removed.isEmpty())
		{
			return false;
		}

		final var eventType = removed.size() == 1 ? EventType.REMOVE : EventType.REMOVE_MANY;
		notifyListener(eventType, removed);
		return true;
	}

	@Override
	public boolean retainAll(final Collection<?> elements)
	{
		if (delegate.isEmpty())
		{
			return false;
		}

		final var removed = new ArrayList<T>();
		final Iterator<T> iterator = delegate.iterator();

		while (iterator.hasNext())
		{
			final var value = iterator.next();
			if (elements.contains(value) == false)
			{
				iterator.remove();
				removed.add(value);
			}
		}

		if (removed.isEmpty())
		{
			return false;
		}

		final var eventType = removed.size() == 1 ? EventType.REMOVE : EventType.REMOVE_MANY;
		notifyListener(eventType, removed);
		return true;
	}

	@Override
	public void clear()
	{
		if (delegate.isEmpty())
		{
			return;
		}

		final var removed = new ArrayList<T>(delegate);
		delegate.clear();

		final var eventType = removed.size() == 1 ? EventType.REMOVE : EventType.REMOVE_MANY;
		notifyListener(eventType, removed);
	}

	private void notifyListener(final EventType type, final List<T> elements)
	{
		if (elements.isEmpty())
		{
			return;
		}

		listener.accept(type, List.copyOf(elements));
	}
}

