package org.logoce.lmf.cli.remove;

import org.logoce.lmf.cli.ref.ObjectId;
import org.logoce.lmf.cli.edit.ReferenceStringUtil;
import org.logoce.lmf.core.loader.api.loader.linking.tree.LinkNodeInternal;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;

import java.util.Objects;
import java.util.OptionalInt;

final class RemoveShiftContext
{
	private final String modelQualifiedName;
	private final String containerPath;
	private final String featureName;
	private final int removedIndex;

	private RemoveShiftContext(final String modelQualifiedName,
							   final String containerPath,
							   final String featureName,
							   final int removedIndex)
	{
		this.modelQualifiedName = modelQualifiedName;
		this.containerPath = containerPath;
		this.featureName = featureName;
		this.removedIndex = removedIndex;
	}

	static RemoveShiftContext from(final LinkNodeInternal<?, PNode, ?> targetNode, final ObjectId removedId)
	{
		if (targetNode == null || removedId == null)
		{
			return null;
		}

		final var relation = targetNode.containingRelation();
		if (relation == null || !relation.many())
		{
			return null;
		}

		final var parent = targetNode.parent();
		if (parent == null)
		{
			return null;
		}

		final var featureName = relation.name();
		final var siblings = parent.streamChildren()
								   .filter(child -> child.containingRelation() != null)
								   .filter(child -> featureName.equals(child.containingRelation().name()))
								   .toList();

		final int removedIndex = siblings.indexOf(targetNode);
		if (removedIndex < 0)
		{
			return null;
		}

		final var parentId = ObjectId.from(parent.build());
		final var containerPath = parentId != null ? parentId.path() : parentPath(removedId.path());
		return new RemoveShiftContext(removedId.modelQualifiedName(),
									  containerPath,
									  featureName,
									  removedIndex);
	}

	boolean shouldShift(final ObjectId targetId, final String raw)
	{
		if (targetId == null || raw == null)
		{
			return false;
		}
		if (!Objects.equals(modelQualifiedName, targetId.modelQualifiedName()))
		{
			return false;
		}
		if (!ReferenceStringUtil.isPathLikeReference(raw))
		{
			return false;
		}
		final var index = extractIndex(targetId.path());
		return index.isPresent() && index.getAsInt() > removedIndex;
	}

	OptionalInt extractIndex(final String path)
	{
		if (path == null)
		{
			return OptionalInt.empty();
		}

		final var prefix = buildPrefix();
		if (!path.startsWith(prefix))
		{
			return OptionalInt.empty();
		}

		int cursor = prefix.length();
		int start = cursor;
		while (cursor < path.length() && Character.isDigit(path.charAt(cursor)))
		{
			cursor++;
		}
		if (cursor == start)
		{
			return OptionalInt.empty();
		}

		final int index = Integer.parseInt(path.substring(start, cursor));
		return OptionalInt.of(index);
	}

	String shiftRaw(final String raw, final int oldIndex)
	{
		final var oldSegment = featureName + "." + oldIndex;
		final var newSegment = featureName + "." + (oldIndex - 1);
		final int idx = ReferenceStringUtil.findPathSegment(raw, oldSegment);
		if (idx < 0)
		{
			return raw;
		}
		return raw.substring(0, idx) + newSegment + raw.substring(idx + oldSegment.length());
	}

	private String buildPrefix()
	{
		final var root = "/".equals(containerPath) ? "" : containerPath;
		return root + "/" + featureName + ".";
	}

	private static String parentPath(final String path)
	{
		if (path == null || "/".equals(path))
		{
			return "/";
		}
		final int lastSlash = path.lastIndexOf('/');
		if (lastSlash <= 0)
		{
			return "/";
		}
		return path.substring(0, lastSlash);
	}
}
