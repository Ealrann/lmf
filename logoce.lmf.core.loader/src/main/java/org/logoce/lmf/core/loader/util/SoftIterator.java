package org.logoce.lmf.core.loader.util;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Supplier;

public final class SoftIterator<T> implements Iterator<T>
{
	private final Supplier<Optional<T>> next;
	private Optional<T> current;

	private boolean progressed = false;

	public SoftIterator(Supplier<Optional<T>> next)
	{
		this.next = next;
	}

	@Override
	public boolean hasNext()
	{
		if (!progressed) progress();
		progressed = true;
		return current.isPresent();
	}

	@Override
	public T next()
	{
		if (!progressed) progress();
		progressed = false;
		return current.orElse(null);
	}

	private void progress()
	{
		current = next.get();
	}
}
