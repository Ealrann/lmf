package org.logoce.lmf.model.util;

import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.lang.*;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ModelUtils
{
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
		else if (parent instanceof Generic<?> genericParent && genericParent.type() instanceof Group<?> parentGroup)
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

	public static Stream<Feature<?, ?>> streamAllFeatures(Group<?> group)
	{
		return streamHierarchy(group).map(Group::features).flatMap(Collection::stream);
	}

	public static Stream<RawFeature<?, ?>> streamContainmentFeatures(Group<?> group)
	{
		return group.features()
					.stream()
					.filter(Relation.class::isInstance)
					.map(Relation.class::cast)
					.filter(Relation::contains)
					.map(Feature::rawFeature)
					.map(r -> (RawFeature<?, ?>) r);
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
									.filter(Relation.class::isInstance)
									.map(Relation.class::cast)
									.filter(Relation::contains)
									.flatMap(r -> streamChildren(root, r));

		return Stream.concat(Stream.of(root), childStream);
	}

	@SuppressWarnings("unchecked")
	public static final <T extends LMObject> Stream<T> streamChildren(final LMObject element,
																	  final Relation<T, ?> relation)
	{
		if (relation.many())
		{
			final var list = (List<T>) element.get(relation);
			return list.stream();
		}
		else
		{
			return Stream.of((T) element.get(relation));
		}
	}
}
