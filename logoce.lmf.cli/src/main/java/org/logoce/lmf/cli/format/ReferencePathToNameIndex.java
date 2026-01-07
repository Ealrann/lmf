package org.logoce.lmf.cli.format;

import org.logoce.lmf.core.api.util.ModelUtil;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.loader.api.loader.linking.tree.LinkNodeInternal;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ReferencePathToNameIndex
{
	private final Map<String, Target> byPath;
	private final Map<String, List<Target>> byName;

	private ReferencePathToNameIndex(final Map<String, Target> byPath,
									 final Map<String, List<Target>> byName)
	{
		this.byPath = byPath;
		this.byName = byName;
	}

	public String replacementForAbsolutePath(final String absolutePath)
	{
		return replacementForAbsolutePath(absolutePath, null);
	}

	public String replacementForAbsolutePath(final String absolutePath, final Group<?> conceptGroup)
	{
		if (absolutePath == null || absolutePath.isBlank())
		{
			return null;
		}

		final var normalized = normalizeAbsolutePath(absolutePath);
		final var target = byPath.get(normalized);
		if (target == null)
		{
			return null;
		}

		final var name = target.name();
		if (!isReferenceSafeName(name))
		{
			return null;
		}

		final var candidates = byName.get(name);
		if (candidates == null || candidates.isEmpty())
		{
			return null;
		}

		if (conceptGroup == null)
		{
			return candidates.size() == 1 ? "@" + name : null;
		}

		int matches = 0;
		for (final var candidate : candidates)
		{
			if (candidate.group() != null && ModelUtil.isSubGroup(conceptGroup, candidate.group()))
			{
				matches++;
				if (matches > 1)
				{
					return null;
				}
			}
		}

		return matches == 1 ? "@" + name : null;
	}

	public static ReferencePathToNameIndex build(final LinkNodeInternal<?, PNode, ?> root)
	{
		Objects.requireNonNull(root, "root");

		final var byPath = new HashMap<String, Target>();
		final var byName = new HashMap<String, List<Target>>();

		collectTargets(root, "", byPath, byName);

		final var frozenByName = new HashMap<String, List<Target>>();
		for (final var entry : byName.entrySet())
		{
			frozenByName.put(entry.getKey(), List.copyOf(entry.getValue()));
		}

		return new ReferencePathToNameIndex(Map.copyOf(byPath), Map.copyOf(frozenByName));
	}

	private static void collectTargets(final LinkNodeInternal<?, PNode, ?> node,
									   final String nodePath,
									   final Map<String, Target> byPath,
									   final Map<String, List<Target>> byName)
	{
		final var resolvedName = NodeNameResolver.resolve(node);
		if (resolvedName != null && nodePath != null && !nodePath.isBlank() && node.group() != null)
		{
			final var target = new Target(nodePath, resolvedName, node.group());
			byPath.put(nodePath, target);
			byName.computeIfAbsent(resolvedName, ignored -> new ArrayList<>()).add(target);
		}

		final var children = node.streamChildren().toList();
		if (children.isEmpty())
		{
			return;
		}

		final var indices = new HashMap<String, Integer>();
		for (final var child : children)
		{
			if (child.containingRelation() == null)
			{
				continue;
			}

			final var relationName = child.containingRelation().name();
			final int index = indices.getOrDefault(relationName, 0);
			indices.put(relationName, index + 1);

			final var pathWithIndex = nodePath.isEmpty()
									  ? "/" + relationName + "." + index
									  : nodePath + "/" + relationName + "." + index;
			collectTargets(child, pathWithIndex, byPath, byName);
		}
	}

	private static boolean isReferenceSafeName(final String name)
	{
		if (name == null || name.isBlank())
		{
			return false;
		}
		if (name.indexOf('/') != -1)
		{
			return false;
		}
		for (int i = 0; i < name.length(); i++)
		{
			if (Character.isWhitespace(name.charAt(i)))
			{
				return false;
			}
		}
		return true;
	}

	static String normalizeAbsolutePath(final String absolutePath)
	{
		if (absolutePath == null || absolutePath.isBlank() || !absolutePath.startsWith("/"))
		{
			return absolutePath;
		}

		final var segments = absolutePath.split("/");
		final var builder = new StringBuilder(absolutePath.length() + 8);
		for (final var segment : segments)
		{
			if (segment == null || segment.isEmpty())
			{
				continue;
			}
			builder.append('/');

			if (segment.equals(".") || segment.equals("..") || segment.startsWith("."))
			{
				builder.append(segment);
				continue;
			}

			if (segment.indexOf('.') != -1)
			{
				builder.append(segment);
			}
			else
			{
				builder.append(segment).append(".0");
			}
		}

		return builder.length() == 0 ? "/" : builder.toString();
	}

	private record Target(String path, String name, Group<?> group)
	{
	}
}
