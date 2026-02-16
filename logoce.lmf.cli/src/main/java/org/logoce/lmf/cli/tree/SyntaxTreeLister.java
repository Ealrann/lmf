package org.logoce.lmf.cli.tree;

import org.logoce.lmf.cli.format.NodeNameResolver;
import org.logoce.lmf.core.loader.api.lexer.ELMTokenType;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;
import org.logoce.lmf.core.util.tree.Tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class SyntaxTreeLister
{
	public List<TreeLister.Line> list(final List<Tree<PNode>> roots, final int maxDepth)
	{
		if (roots == null || roots.isEmpty() || maxDepth <= 0)
		{
			return List.of();
		}

		final var lines = new ArrayList<TreeLister.Line>();
		for (final var root : roots)
		{
			collectChildren(root, "", 1, maxDepth, lines);
		}
		return List.copyOf(lines);
	}

	private void collectChildren(final Tree<PNode> parent,
								 final String parentPath,
								 final int depth,
								 final int maxDepth,
								 final List<TreeLister.Line> out)
	{
		if (depth > maxDepth)
		{
			return;
		}

		final var children = parent.children();
		if (children.isEmpty())
		{
			return;
		}

		final var counts = new HashMap<String, Integer>();
		for (final var child : children)
		{
			counts.merge(segmentName(child.data()), 1, Integer::sum);
		}

		final var indices = new HashMap<String, Integer>();
		for (final var child : children)
		{
			final var segment = segmentName(child.data());
			final int index = indices.getOrDefault(segment, 0);
			indices.put(segment, index + 1);

			final boolean multiple = counts.getOrDefault(segment, 0) > 1;
			final var pathSegment = multiple ? segment + "." + index : segment;
			final var path = parentPath.isEmpty() ? "/" + pathSegment : parentPath + "/" + pathSegment;

			final var groupName = segment;
			final var name = NodeNameResolver.resolve(child.data());
			out.add(new TreeLister.Line(path, groupName, name));

			collectChildren(child, path, depth + 1, maxDepth, out);
		}
	}

	private static String segmentName(final PNode node)
	{
		if (node == null)
		{
			return "Unknown";
		}

		final var tokens = node.tokens();
		if (tokens == null || tokens.isEmpty())
		{
			return "Unknown";
		}

		for (final var token : tokens)
		{
			if (token.type() == ELMTokenType.WHITE_SPACE)
			{
				continue;
			}
			return token.value() == null || token.value().isBlank() ? "Unknown" : token.value();
		}

		return "Unknown";
	}
}

