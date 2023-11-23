package org.logoce.lmf.model.resource.linking.feature.reference;

import java.util.Iterator;

public final class PathParser implements Iterator<PathParser.Step>
{
	private final String path;

	public enum Type
	{
		ROOT,
		CURRENT,
		PARENT,
		CHILD,
		NAME,
		MODEL
	}

	private int location = 0;
	private int lastLocation = 0;

	public PathParser(final String path)
	{
		this.path = path;
	}

	/**
	 * Go back to the previous element, only once. Cannot be called multiple times.
	 */
	public void rewind()
	{
		location = lastLocation;
		lastLocation = 0;
	}

	@Override
	public Step next()
	{
		final var firstChar = path.charAt(location);
		lastLocation = location;

		if (firstChar == '@')
		{
			final var text = path.substring(location + 1);
			location = path.length();
			return new Step(Type.NAME, text);
		}
		else if (firstChar == '#')
		{
			final int nextStep = path.indexOf('/', location + 1);
			final int nextName = path.indexOf('@', location + 1);
			if (nextName == -1 && nextStep == -1)
			{
				final var text = path.substring(location + 1);
				location = path.length();
				return new Step(Type.MODEL, text);
			}
			else if (nextStep == -1)
			{
				final var text = path.substring(location + 1, nextName);
				location += nextName;
				return new Step(Type.MODEL, text);
			}
			else
			{
				final var text = path.substring(location + 1, nextStep);
				location = location + nextStep + 1;
				return new Step(Type.MODEL, text);
			}
		}
		else if (firstChar == '.')
		{
			final var secondChar = path.charAt(location + 1);
			if (secondChar == '.')
			{
				location += 3;
				return new Step(Type.PARENT, "");
			}
			else if (secondChar == '/')
			{
				location += 2;
				return new Step(Type.CURRENT, "");
			}
			else
			{
				throw new IllegalStateException();
			}
		}
		else if (firstChar == '/')
		{
			location++;
			return new Step(Type.ROOT, "");
		}
		else
		{
			final int nextStep = path.indexOf('/', location + 1);
			final int nextLocation = nextStep == -1 ? path.length() : nextStep;
			final var text = path.substring(location, nextLocation);
			location = nextLocation + 1;
			return new Step(Type.CHILD, text);
		}
	}

	@Override
	public boolean hasNext()
	{
		return location < path.length();
	}

	public record Step(Type type, String text) {}
}
