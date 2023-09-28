package org.logoce.lmf.model.resource.path;

import java.util.List;

public final class ResourcePath
{
	private final PathType type;
	private final List<String> nodes;

	public ResourcePath(final PathType type, final List<String> nodes)
	{
		this.type = type;
		this.nodes = nodes;
	}

	public PathType getType()
	{
		return type;
	}

	public List<String> getNodes()
	{
		return nodes;
	}
}
