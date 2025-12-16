package org.logoce.lmf.core.util.oldlogoce;

import java.util.Deque;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class StreamUtil
{
	public static final int CHARACTERISTICS = Spliterator.ORDERED | Spliterator.SIZED | Spliterator.IMMUTABLE;

	public static <T> Stream<T> reverseStream(List<T> list)
	{
		final var spliterator = new ListReverseSpliterator<>(list);
		return StreamSupport.stream(spliterator, false);
	}

	public static <T> Stream<T> reverseStreamFromDeque(Deque<T> deque)
	{
		final var it = deque.descendingIterator();
		final var spliterator = Spliterators.spliterator(it, deque.size(), CHARACTERISTICS);
		return StreamSupport.stream(spliterator, false);
	}
}
