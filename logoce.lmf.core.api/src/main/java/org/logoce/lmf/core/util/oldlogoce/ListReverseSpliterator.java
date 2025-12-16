package org.logoce.lmf.core.util.oldlogoce;

import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;

public final class ListReverseSpliterator<T> implements Spliterator<T>
{
	public static final int CHARACTERISTICS = Spliterator.ORDERED | Spliterator.SIZED | Spliterator.IMMUTABLE;

	private final ListIterator<T> iterator;
	private final int size;

	public ListReverseSpliterator(List<T> list)
	{
		size = list.size();
		iterator = list.listIterator(size);
	}

	@Override
	public boolean tryAdvance(final Consumer<? super T> action)
	{
		if (iterator.hasPrevious())
		{
			action.accept(iterator.previous());
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public Spliterator<T> trySplit()
	{
		return null;
	}

	@Override
	public long estimateSize()
	{
		return size;
	}

	@Override
	public int characteristics()
	{
		return CHARACTERISTICS;
	}
}
