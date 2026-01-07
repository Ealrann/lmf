package org.logoce.lmf.cli.format;

import java.util.Iterator;

final class RootPathParser implements Iterator<RootPathParser.Step>
{
	enum Type
	{
		ROOT,
		CURRENT,
		PARENT,
		CHILD,
		NAME,
		CONTEXT_NAME,
		MODEL
	}

	private final String path;
	private int location = 0;
	private int lastLocation = 0;

	RootPathParser(final String path)
	{
		this.path = path;
	}

	void rewind()
	{
		location = lastLocation;
		lastLocation = 0;
	}

	@Override
	public Step next()
	{
		final int length = path.length();
		final var firstChar = path.charAt(location);
		lastLocation = location;

		if (firstChar == '@')
		{
			final int start = location + 1;
			final int nextSlash = path.indexOf('/', start);
			if (nextSlash == -1)
			{
				final var text = path.substring(start);
				location = length;
				return new Step(Type.NAME, text);
			}

			final var text = path.substring(start, nextSlash);
			location = nextSlash + 1;
			return new Step(Type.NAME, text);
		}
		if (firstChar == '^')
		{
			final int start = location + 1;
			final int nextSlash = path.indexOf('/', start);
			if (nextSlash == -1)
			{
				final var text = path.substring(start);
				location = length;
				return new Step(Type.CONTEXT_NAME, text);
			}

			final var text = path.substring(start, nextSlash);
			location = nextSlash + 1;
			return new Step(Type.CONTEXT_NAME, text);
		}
		if (firstChar == '#')
		{
			final int start = location + 1;
			final int nextStep = path.indexOf('/', start);
			final int nextName = path.indexOf('@', start);

			if (nextName == -1 && nextStep == -1)
			{
				final var text = path.substring(start);
				location = length;
				return new Step(Type.MODEL, text);
			}

			if (nextName != -1 && (nextStep == -1 || nextName < nextStep))
			{
				final var text = path.substring(start, nextName);
				location = nextName;
				return new Step(Type.MODEL, text);
			}

			final var text = path.substring(start, nextStep);
			location = nextStep + 1;
			return new Step(Type.MODEL, text);
		}
		if (firstChar == '.')
		{
			if (location + 1 >= length)
			{
				location = length;
				return new Step(Type.CURRENT, "");
			}

			final var secondChar = path.charAt(location + 1);
			if (secondChar == '.')
			{
				location += location + 2 < length && path.charAt(location + 2) == '/' ? 3 : 2;
				return new Step(Type.PARENT, "");
			}
			if (secondChar == '/')
			{
				location += 2;
				return new Step(Type.CURRENT, "");
			}
			throw new IllegalStateException();
		}
		if (firstChar == '/')
		{
			location++;
			return new Step(Type.ROOT, "");
		}

		final int nextStep = path.indexOf('/', location);
		final int nextLocation = nextStep == -1 ? length : nextStep;
		final var text = path.substring(location, nextLocation);
		location = nextStep == -1 ? length : nextLocation + 1;
		return new Step(Type.CHILD, text);
	}

	@Override
	public boolean hasNext()
	{
		return location < path.length();
	}

	record Step(Type type, String text)
	{
	}
}
