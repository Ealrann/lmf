package org.logoce.lmf.cli.tree;

import org.logoce.lmf.cli.format.NodeNameResolver;
import org.logoce.lmf.core.loader.api.loader.linking.tree.LinkNodeInternal;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class TreeLister
{
	public record Line(String path, String groupName, String name)
	{
		public String format()
		{
			final var safeGroup = groupName == null ? "" : groupName;
			final var safeName = name == null ? "" : name;
			return path + "\t" + safeGroup + "\t" + safeName;
		}
	}

	public List<Line> list(final LinkNodeInternal<?, PNode, ?> root, final int maxDepth)
	{
		if (root == null || maxDepth <= 0)
		{
			return List.of();
		}

		final var lines = new ArrayList<Line>();
		collectChildren(root, "", 1, maxDepth, lines);
		return List.copyOf(lines);
	}

	private void collectChildren(final LinkNodeInternal<?, PNode, ?> parent,
								 final String parentPath,
								 final int depth,
								 final int maxDepth,
								 final List<Line> out)
	{
		if (depth > maxDepth)
		{
			return;
		}

		final var children = parent.streamChildren()
								   .filter(child -> child.containingRelation() != null)
								   .toList();
		if (children.isEmpty())
		{
			return;
		}

		final var counts = new HashMap<String, Integer>();
		for (final var child : children)
		{
			final var relationName = child.containingRelation().name();
			counts.merge(relationName, 1, Integer::sum);
		}

		final var indices = new HashMap<String, Integer>();
		for (final var child : children)
		{
			final var relationName = child.containingRelation().name();
			final int index = indices.getOrDefault(relationName, 0);
			indices.put(relationName, index + 1);

			final boolean many = counts.getOrDefault(relationName, 0) > 1;
			final var segment = many ? relationName + "." + index : relationName;
			final var path = parentPath.isEmpty() ? "/" + segment : parentPath + "/" + segment;

			final var groupName = child.group() != null ? child.group().name() : "Unknown";
			final var name = NodeNameResolver.resolve(child);
			out.add(new Line(path, groupName, name));

			collectChildren(child, path, depth + 1, maxDepth, out);
		}
	}
}
