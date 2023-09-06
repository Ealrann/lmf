package isotropy.lmf.core.util;

import isotropy.lmf.core.lang.*;

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
}
