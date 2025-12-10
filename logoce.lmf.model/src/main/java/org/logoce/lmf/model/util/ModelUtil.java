package org.logoce.lmf.model.util;

import org.logoce.lmf.model.lang.*;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class ModelUtil
{
	public ModelUtil()
	{
	}

	public static LMObject root(LMObject child)
	{
		if (child == null) return null;
		if (child.lmContainer() != null) return root(child.lmContainer());
		else return child;
	}

	public static boolean isSubGroup(final Concept<?> parent, final Group<?> check)
	{
		if (parent instanceof Group<?> parentGroup)
		{
			return isSubGroup(parentGroup, check);
		}
		else
		{
			return false;
		}
	}

	public static boolean isSubGroup(final Group<?> parent, final Group<?> check)
	{
		if (check == parent)
		{
			return true;
		}
		else if (check.includes().isEmpty() == false)
		{
			for (final var include : check.includes())
			{
				if (isSubGroup(parent, (Group<?>) include.group()))
				{
					return true;
				}
			}
		}
		return false;
	}

	public static Stream<Feature<?, ?>> streamAllFeatures(final Group<?> group)
	{
		return streamHierarchy(group).map(Group::features).flatMap(Collection::stream);
	}

	public static Stream<Relation<?, ?>> streamContainmentFeatures(final Group<?> group)
	{
		return group.features()
					.stream()
					.filter(feature -> feature instanceof Relation<?, ?> relation && relation.contains())
					.map(feature -> (Relation<?, ?>) feature);
	}

	public static Stream<Group<?>> streamHierarchy(Group<?> group)
	{
		final var groupSet = new LinkedHashSet<Group<?>>();
		addHierarchy(group, groupSet);
		groupSet.add(group);

		final var deque = new LinkedList<>(groupSet);
		final var reverseStream = StreamSupport.stream(Spliterators.spliterator(deque.descendingIterator(),
																				deque.size(),
																				Spliterator.SIZED |
																				Spliterator.ORDERED |
																				Spliterator.IMMUTABLE |
																				Spliterator.DISTINCT), false);

		return reverseStream;
	}

	private static void addHierarchy(Group<?> group, LinkedHashSet<Group<?>> res)
	{
		res.add(group);
		for (final var include : group.includes())
		{
			addHierarchy((Group<?>) include.group(), res);
		}
	}

	@SuppressWarnings("unchecked")
	public static final Stream<LMObject> streamTree(final LMObject root)
	{
		final var childStream = root.lmGroup()
									.features()
									.stream()
									.filter(feature -> feature instanceof Relation<?, ?> relation && relation.contains())
									.flatMap(feature -> streamChildren(root, (Relation<LMObject, ?>) feature));

		return Stream.concat(Stream.of(root), childStream);
	}

	@SuppressWarnings("unchecked")
	public static final <T extends LMObject> Stream<T> streamChildren(final LMObject element,
																	  final Relation<T, ?> relation)
	{
		if (relation.many())
		{
			final var value = element.get(relation);
			if (!(value instanceof List<?> list))
			{
				throw new IllegalStateException(
						"Expected List value for relation '" + relation.name() + "' on group '" +
						element.lmGroup().name() + "', but got: " +
						(value == null ? "null" : value.getClass().getName()));
			}
			@SuppressWarnings("unchecked")
			final var typedList = (List<T>) list;
			return typedList.stream();
		}
		else
		{
			return Stream.of((T) element.get(relation));
		}
	}

	public static Optional<ContainmentPath> containmentPath(final LMObject parent, LMObject child)
	{
		final Deque<ChildReference> res = new ArrayDeque<>();
		while (child != null && child != parent && child.lmContainer() != null)
		{
			res.addFirst(ChildReference.referenceFromParent(child));
			child = child.lmContainer();
		}
		if (child == parent) return Optional.of(new ContainmentPath(res));
		else return Optional.empty();
	}

	public record ChildReference(Feature<?, ?> reference, int index)
	{
		public static ChildReference referenceFromParent(LMObject child)
		{
			final var containmentFeature = child.lmContainingFeature();
			final var container = child.lmContainer();
			final var many = containmentFeature.many();
			final int index = many ? ((List<?>) container.get(containmentFeature)).indexOf(child) : 0;
			return new ChildReference(containmentFeature, index);
		}

		@SuppressWarnings("unchecked")
		public LMObject eGet(LMObject source)
		{
			if (source == null)
			{
				return null;
			}
			else if (reference.many())
			{
				return ((List<LMObject>) source.get(reference)).get(index);
			}
			else
			{
				return (LMObject) source.get(reference);
			}
		}
	}

	public record ContainmentPath(Collection<ChildReference> path)
	{
		public LMObject eGet(LMObject source)
		{
			for (final var childReference : path)
			{
				source = childReference.eGet(source);
			}
			return source;
		}
	}

	public static Type<?> resolveGenericType(LMObject object, Group<?> genericHolder)
	{
		return resolveGenericType(object.lmGroup(), genericHolder);
	}

	private static Type<?> resolveGenericType(Group<?> group, Group<?> genericHolder)
	{
		Type<?> res = null;
		final var includes = group.includes();

		for (int i = 0; i < includes.size(); i++)
		{
			final var include = includes.get(i);
			if (genericHolder == include.group())
			{
				res = include.parameters().getFirst().type();
				break;
			}
		}

		if (res == null)
		{
			for (int i = 0; i < includes.size(); i++)
			{
				final var include = includes.get(i);
				if (include.group() instanceof Group<?> includedGroup)
				{
					res = resolveGenericType(includedGroup, genericHolder);
					if (res != null)
					{
						break;
					}
				}
			}
		}

		return res;
	}
}
