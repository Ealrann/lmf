package org.logoce.lmf.cli.insert;

import org.logoce.lmf.cli.edit.ReferenceStringUtil;
import org.logoce.lmf.cli.ref.ObjectId;

import java.util.Objects;
import java.util.OptionalInt;

final class InsertShiftContext
{
	private final String modelQualifiedName;
	private final String containerPath;
	private final String featureName;
	private final int insertionIndex;

	InsertShiftContext(final String modelQualifiedName,
					   final String containerPath,
					   final String featureName,
					   final int insertionIndex)
	{
		this.modelQualifiedName = Objects.requireNonNull(modelQualifiedName, "modelQualifiedName");
		this.containerPath = Objects.requireNonNull(containerPath, "containerPath");
		this.featureName = Objects.requireNonNull(featureName, "featureName");
		this.insertionIndex = insertionIndex;
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
		return index.isPresent() && index.getAsInt() >= insertionIndex;
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
		final var newSegment = featureName + "." + (oldIndex + 1);
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
}

