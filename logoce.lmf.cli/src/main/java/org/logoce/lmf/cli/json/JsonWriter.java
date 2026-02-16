package org.logoce.lmf.cli.json;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

public final class JsonWriter
{
	private final Appendable out;
	private final Deque<Scope> scopes = new ArrayDeque<>();

	public JsonWriter(final Appendable out)
	{
		this.out = Objects.requireNonNull(out, "out");
	}

	public JsonWriter beginObject()
	{
		beforeValue();
		append('{');
		scopes.push(new Scope(Container.OBJECT));
		return this;
	}

	public JsonWriter endObject()
	{
		final var scope = scopes.peek();
		if (scope == null || scope.container != Container.OBJECT || scope.expectingValue)
		{
			throw new IllegalStateException("Not in an object");
		}
		scopes.pop();
		append('}');
		return this;
	}

	public JsonWriter beginArray()
	{
		beforeValue();
		append('[');
		scopes.push(new Scope(Container.ARRAY));
		return this;
	}

	public JsonWriter endArray()
	{
		final var scope = scopes.peek();
		if (scope == null || scope.container != Container.ARRAY)
		{
			throw new IllegalStateException("Not in an array");
		}
		scopes.pop();
		append(']');
		return this;
	}

	public JsonWriter name(final String name)
	{
		Objects.requireNonNull(name, "name");

		final var scope = scopes.peek();
		if (scope == null || scope.container != Container.OBJECT)
		{
			throw new IllegalStateException("Not in an object");
		}
		if (scope.expectingValue)
		{
			throw new IllegalStateException("Expected a value after a name()");
		}

		if (!scope.first)
		{
			append(',');
		}
		scope.first = false;

		writeString(name);
		append(':');
		scope.expectingValue = true;
		return this;
	}

	public JsonWriter value(final String value)
	{
		if (value == null)
		{
			return nullValue();
		}
		beforeValue();
		writeString(value);
		return this;
	}

	public JsonWriter value(final boolean value)
	{
		beforeValue();
		append(value ? "true" : "false");
		return this;
	}

	public JsonWriter value(final int value)
	{
		beforeValue();
		append(Integer.toString(value));
		return this;
	}

	public JsonWriter value(final long value)
	{
		beforeValue();
		append(Long.toString(value));
		return this;
	}

	public JsonWriter nullValue()
	{
		beforeValue();
		append("null");
		return this;
	}

	public JsonWriter flush()
	{
		if (out instanceof java.io.Flushable flushable)
		{
			try
			{
				flushable.flush();
			}
			catch (IOException e)
			{
				throw new UncheckedIOException(e);
			}
		}
		return this;
	}

	private void beforeValue()
	{
		if (scopes.isEmpty())
		{
			return;
		}

		final var scope = scopes.peek();
		if (scope.container == Container.ARRAY)
		{
			if (!scope.first)
			{
				append(',');
			}
			scope.first = false;
			return;
		}

		if (scope.container == Container.OBJECT)
		{
			if (!scope.expectingValue)
			{
				throw new IllegalStateException("Expected name() before value()");
			}
			scope.expectingValue = false;
		}
	}

	private void writeString(final String value)
	{
		append('"');
		for (int i = 0; i < value.length(); i++)
		{
			final char c = value.charAt(i);
			switch (c)
			{
				case '"' -> append("\\\"");
				case '\\' -> append("\\\\");
				case '\b' -> append("\\b");
				case '\f' -> append("\\f");
				case '\n' -> append("\\n");
				case '\r' -> append("\\r");
				case '\t' -> append("\\t");
				default ->
				{
					if (c < 0x20)
					{
						append("\\u");
						append(hex4(c));
					}
					else
					{
						append(c);
					}
				}
			}
		}
		append('"');
	}

	private static String hex4(final char c)
	{
		final int v = c;
		return "" + hexDigit((v >>> 12) & 0xF)
			+ hexDigit((v >>> 8) & 0xF)
			+ hexDigit((v >>> 4) & 0xF)
			+ hexDigit(v & 0xF);
	}

	private static char hexDigit(final int value)
	{
		return (char) (value < 10 ? ('0' + value) : ('a' + (value - 10)));
	}

	private void append(final char c)
	{
		try
		{
			out.append(c);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	private void append(final String s)
	{
		try
		{
			out.append(s);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	private enum Container
	{
		OBJECT,
		ARRAY
	}

	private static final class Scope
	{
		private final Container container;
		private boolean first = true;
		private boolean expectingValue = false;

		private Scope(final Container container)
		{
			this.container = Objects.requireNonNull(container, "container");
		}
	}
}

