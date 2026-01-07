package org.logoce.lmf.cli.move;

import org.logoce.lmf.cli.edit.ReferenceStringUtil;
import org.logoce.lmf.cli.ref.ObjectId;

import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.IntUnaryOperator;

final class MoveIndexShift
{
	private final String modelQualifiedName;
	private final String containerPath;
	private final String featureName;
	private final IntUnaryOperator mapper;

	private MoveIndexShift(final String modelQualifiedName,
					   final String containerPath,
					   final String featureName,
					   final IntUnaryOperator mapper)
	{
		this.modelQualifiedName = Objects.requireNonNull(modelQualifiedName, "modelQualifiedName");
		this.containerPath = Objects.requireNonNull(containerPath, "containerPath");
		this.featureName = Objects.requireNonNull(featureName, "featureName");
		this.mapper = Objects.requireNonNull(mapper, "mapper");
	}

	static MoveIndexShift forRemoval(final String modelQualifiedName,
					   final String containerPath,
					   final String featureName,
					   final int removedIndex)
	{
		return new MoveIndexShift(modelQualifiedName,
						  containerPath,
						  featureName,
						  index -> index > removedIndex ? index - 1 : index);
	}

	static MoveIndexShift forInsertion(final String modelQualifiedName,
						 final String containerPath,
						 final String featureName,
						 final int insertionIndex)
	{
		return new MoveIndexShift(modelQualifiedName,
						  containerPath,
						  featureName,
						  index -> index >= insertionIndex ? index + 1 : index);
	}

	static MoveIndexShift forReorder(final String modelQualifiedName,
					  final String containerPath,
					  final String featureName,
					  final int sourceIndex,
					  final int destinationIndex)
	{
		final var mapper = buildReorderMapper(sourceIndex, destinationIndex);
		return new MoveIndexShift(modelQualifiedName, containerPath, featureName, mapper);
	}

	String rewriteRaw(final ObjectId targetId, final String raw)
	{
		if (targetId == null || raw == null)
		{
			return null;
		}
		if (!Objects.equals(modelQualifiedName, targetId.modelQualifiedName()))
		{
			return null;
		}
		if (!ReferenceStringUtil.isPathLikeReference(raw))
		{
			return null;
		}

		final var index = extractIndex(targetId.path());
		if (index.isEmpty())
		{
			return null;
		}

		final int oldIndex = index.getAsInt();
		final int newIndex = mapper.applyAsInt(oldIndex);
		if (newIndex == oldIndex)
		{
			return null;
		}
		if (newIndex < 0)
		{
			return null;
		}

		return shiftRaw(raw, oldIndex, newIndex);
	}

	private OptionalInt extractIndex(final String path)
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

	private String shiftRaw(final String raw, final int oldIndex, final int newIndex)
	{
		final var oldSegment = featureName + "." + oldIndex;
		final var newSegment = featureName + "." + newIndex;
		final int idx = ReferenceStringUtil.findPathSegment(raw, oldSegment);
		if (idx < 0)
		{
			return null;
		}
		return raw.substring(0, idx) + newSegment + raw.substring(idx + oldSegment.length());
	}

	private String buildPrefix()
	{
		final var root = "/".equals(containerPath) ? "" : containerPath;
		return root + "/" + featureName + ".";
	}

	private static IntUnaryOperator buildReorderMapper(final int sourceIndex, final int destinationIndex)
	{
		if (sourceIndex < destinationIndex)
		{
			return index -> {
				if (index == sourceIndex)
				{
					return destinationIndex - 1;
				}
				if (index > sourceIndex && index < destinationIndex)
				{
					return index - 1;
				}
				return index;
			};
		}

		if (sourceIndex > destinationIndex)
		{
			return index -> {
				if (index == sourceIndex)
				{
					return destinationIndex;
				}
				if (index >= destinationIndex && index < sourceIndex)
				{
					return index + 1;
				}
				return index;
			};
		}

		return index -> index;
	}
}
