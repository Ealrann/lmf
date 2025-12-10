package org.logoce.lmf.model.util;

import org.logoce.lmf.model.api.model.BuilderSupplier;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.*;

import java.util.*;

public final class ModelCopier
{
	private final Map<LMObject, LMObject> copies = new IdentityHashMap<>();

	public ModelCopier()
	{
	}

	public <T extends LMObject> T copyTree(final T root)
	{
		Objects.requireNonNull(root);
		final var copiedRoot = copySubtree(root);
		rewireNonContainmentRelations();
		@SuppressWarnings("unchecked") final var result = (T) copiedRoot;
		return result;
	}

	@SuppressWarnings("unchecked")
	private LMObject copySubtree(final LMObject source)
	{
		final var existing = copies.get(source);
		if (existing != null)
		{
			return existing;
		}

		final var group = source.lmGroup();
		final var builderSupplier = (BuilderSupplier<LMObject>) group.lmBuilder();
		if (builderSupplier == null)
		{
			throw new IllegalStateException("Group [" + group.name() + "] has no lmBuilder; cannot copy");
		}

		final var builder = builderSupplier.newBuilder();

		for (final var feature : group.features())
		{
			if (feature instanceof Attribute<?, ?, ?, ?> attribute)
			{
				copyAttribute(builder, source, attribute);
			}
			else if (feature instanceof Relation<?, ?, ?, ?> relation)
			{
				if (relation.contains())
				{
					copyContainmentRelation(builder, source, relation);
				}
				else
				{
					copyNonContainmentRelation(builder, source, relation);
				}
			}
		}

		final var copy = builder.build();
		copies.put(source, copy);
		return copy;
	}

	@SuppressWarnings("unchecked")
	private static <T extends LMObject> void copyAttribute(final IFeaturedObject.Builder<T> builder,
														   final LMObject source,
														   final Attribute<?, ?, ?, ?> attribute)
	{
		final var typedAttribute = (Attribute<Object, ?, ?, ?>) attribute;
		final var value = source.get(typedAttribute);
		if (value == null)
		{
			return;
		}

		if (attribute.many())
		{
			for (final var element : (Iterable<?>) value)
			{
				builder.push(typedAttribute, element);
			}
		}
		else
		{
			builder.push(typedAttribute, value);
		}
	}

	@SuppressWarnings("unchecked")
	private LMObject copyContainmentRelation(final IFeaturedObject.Builder<?> builder,
											 final LMObject source,
											 final Relation<?, ?, ?, ?> relation)
	{
		final var typedRelation = (Relation<LMObject, ?, ?, ?>) relation;
		final var value = source.get(typedRelation);

		if (value == null)
		{
			return null;
		}

		if (typedRelation.many())
		{
			for (final var child : (List<LMObject>) value)
			{
				final var childCopy = copySubtree(child);
				builder.push(typedRelation, () -> childCopy);
			}
			return null;
		}
		else
		{
			final var child = (LMObject) value;
			final var childCopy = copySubtree(child);
			builder.push(typedRelation, () -> childCopy);
			return childCopy;
		}
	}

	@SuppressWarnings("unchecked")
	private static void copyNonContainmentRelation(final IFeaturedObject.Builder<?> builder,
												   final LMObject source,
												   final Relation<?, ?, ?, ?> relation)
	{
		final var typedRelation = (Relation<LMObject, ?, ?, ?>) relation;
		final var value = source.get(typedRelation);

		if (typedRelation.many())
		{
			if (value == null)
			{
				return;
			}

			for (final var target : (List<LMObject>) value)
			{
				builder.push(typedRelation, () -> target);
			}
		}
		else
		{
			final var target = (LMObject) value;
			builder.push(typedRelation, () -> target);
		}
	}

	@SuppressWarnings("unchecked")
	private void rewireNonContainmentRelations()
	{
		final var originalNodes = new ArrayList<>(copies.keySet());

		for (final var source : originalNodes)
		{
			final var copy = copies.get(source);
			final Group<?> group = source.lmGroup();

			for (final var feature : group.features())
			{
				if (!(feature instanceof Relation<?, ?, ?, ?> relation) || relation.contains())
				{
					continue;
				}

				final var typedRelation = (Relation<LMObject, ?, ?, ?>) relation;
				final var originalValue = source.get(typedRelation);
				if (originalValue == null)
				{
					continue;
				}

				if (typedRelation.many())
				{
					final var originalList = (List<LMObject>) originalValue;
					final var targetList = (List<LMObject>) copy.get(typedRelation);
					targetList.clear();

					for (final var target : originalList)
					{
						final var mapped = copies.getOrDefault(target, target);
						targetList.add(mapped);
					}
				}
				else
				{
					final var originalTarget = (LMObject) originalValue;
					final var mapped = copies.getOrDefault(originalTarget, originalTarget);
					copy.set((Feature<?, LMObject, ?, ?>) typedRelation, mapped);
				}
			}
		}
	}
}
