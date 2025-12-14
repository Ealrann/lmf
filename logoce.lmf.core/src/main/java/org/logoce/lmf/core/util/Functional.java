package org.logoce.lmf.core.util;

import java.util.function.BiFunction;
import java.util.function.Function;

public final class Functional
{
	private Functional()
	{
	}

	public static <T, U, R> Function<T, R> inject(U injected, BiFunction<T, U, R> function)
	{
		return t -> function.apply(t, injected);
	}
}
