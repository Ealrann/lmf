package org.logoce.lmf.core.api.util;

import org.logoce.lmf.core.api.model.DynamicFeaturedObject;
import org.logoce.lmf.core.api.model.FeaturedObject;
import org.logoce.lmf.core.lang.*;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class ModelUtil
{
	public ModelUtil()
	{
	}

	/**
	 * Deletes an object from the model rooted at {@code target}'s root container:
	 * <ul>
	 *     <li>Removes all relations (containment and non-containment) that reference {@code target} within the model.</li>
	 *     <li>Fails if {@code target} is contained by an immutable containment relation.</li>
	 * </ul>
	 * <p>
	 * Unlike EMF, LMF models have a single containment root and no {@code Resource}/{@code ResourceSet} scope.
	 */
	public static void delete(final LMObject target)
	{
		Objects.requireNonNull(target, "target");

		final var directContainer = target.lmContainer();
		final var directContainment = target.lmContainingFeature();
		if (directContainer != null && directContainment != null && directContainment.immutable())
		{
			throw cannotDeleteFromImmutableContainment(directContainer, directContainment, target);
		}

		final var root = root(target);
		if (root == null)
		{
			return;
		}

		final var treeSnapshot = collectContainmentTree(root);
		for (final var element : treeSnapshot)
		{
			removeRelationUsages(element, target);
		}

		unsetContainer(target);
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

	public static Stream<Feature<?, ?, ?, ?>> streamAllFeatures(final Group<?> group)
	{
		return streamHierarchy(group).map(Group::features).flatMap(Collection::stream);
	}

	public static Stream<Relation<?, ?, ?, ?>> streamContainmentFeatures(final Group<?> group)
	{
		return group.features()
					.stream()
					.filter(feature -> feature instanceof Relation<?, ?, ?, ?> relation && relation.contains())
					.map(feature -> (Relation<?, ?, ?, ?>) feature);
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

	private static List<LMObject> collectContainmentTree(final LMObject root)
	{
		final var result = new ArrayList<LMObject>();
		final var visited = Collections.newSetFromMap(new IdentityHashMap<LMObject, Boolean>());
		final Deque<LMObject> stack = new ArrayDeque<>();
		stack.push(root);

		while (!stack.isEmpty())
		{
			final var current = stack.pop();
			if (!visited.add(current))
			{
				continue;
			}

			result.add(current);

			final var seenFeatures = Collections.newSetFromMap(new IdentityHashMap<Feature<?, ?, ?, ?>, Boolean>());
			streamAllFeatures(current.lmGroup())
					.filter(seenFeatures::add)
					.filter(Relation.class::isInstance)
					.map(Relation.class::cast)
					.filter(Relation::contains)
					.forEach(relation -> pushChildren(stack, current, relation));
		}

		return result;
	}

	private static void pushChildren(final Deque<LMObject> stack,
									 final LMObject parent,
									 final Relation<?, ?, ?, ?> relation)
	{
		if (relation.many())
		{
			final var value = parent.get(relation);
			if (value == null)
			{
				return;
			}
			if (!(value instanceof List<?> list))
			{
				throw new IllegalStateException("Expected List value for relation '" +
												relation.name() +
												"' on group '" +
												parent.lmGroup().name() +
												"', but got: " +
												value.getClass().getName());
			}

			for (final var element : list)
			{
				if (element instanceof LMObject child)
				{
					stack.push(child);
				}
			}
		}
		else
		{
			final var value = parent.get(relation);
			if (value instanceof LMObject child)
			{
				stack.push(child);
			}
		}
	}

	private static void removeRelationUsages(final LMObject owner, final LMObject target)
	{
		final var seenFeatures = Collections.newSetFromMap(new IdentityHashMap<Feature<?, ?, ?, ?>, Boolean>());
		streamAllFeatures(owner.lmGroup())
				.filter(seenFeatures::add)
				.filter(Relation.class::isInstance)
				.map(Relation.class::cast)
				.forEach(relation -> removeFromRelation(owner, relation, target));
	}

	private static void removeFromRelation(final LMObject owner,
										  final Relation<?, ?, ?, ?> relation,
										  final LMObject target)
	{
		if (relation.many())
		{
			final var value = owner.get(relation);
			if (value == null)
			{
				return;
			}
			if (!(value instanceof List<?> list))
			{
				throw new IllegalStateException("Expected List value for relation '" +
												relation.name() +
												"' on group '" +
												owner.lmGroup().name() +
												"', but got: " +
												value.getClass().getName());
			}

			final var referencesTarget = containsIdentity(list, target);
			if (!referencesTarget)
			{
				return;
			}

			if (relation.immutable())
			{
				if (relation.contains())
				{
					throw cannotDeleteFromImmutableContainment(owner, relation, target);
				}
				return;
			}

			removeIdentity(list, target);
		}
		else
		{
			final var value = owner.get(relation);
			if (value != target)
			{
				return;
			}

			if (relation.immutable())
			{
				if (relation.contains())
				{
					throw cannotDeleteFromImmutableContainment(owner, relation, target);
				}
				return;
			}

			owner.set(relation.id(), null);
		}
	}

	private static boolean containsIdentity(final List<?> list, final Object target)
	{
		for (final var element : list)
		{
			if (element == target)
			{
				return true;
			}
		}
		return false;
	}

	private static void removeIdentity(final List<?> list, final Object target)
	{
		for (final var iterator = list.listIterator(); iterator.hasNext();)
		{
			final var element = iterator.next();
			if (element == target)
			{
				iterator.remove();
			}
		}
	}

	private static IllegalStateException cannotDeleteFromImmutableContainment(final LMObject container,
																			 final Relation<?, ?, ?, ?> relation,
																			 final LMObject target)
	{
		return new IllegalStateException("Cannot delete object [" +
										 target.lmGroup().name() +
										 "] because it is contained by immutable relation [" +
										 container.lmGroup().name() +
										 "." +
										 relation.name() +
										 "]");
	}

	private static void unsetContainer(final LMObject target)
	{
		if (target instanceof FeaturedObject<?> featuredObject)
		{
			featuredObject.lmUnsetContainer();
		}
		else if (target instanceof DynamicFeaturedObject dynamicFeaturedObject)
		{
			dynamicFeaturedObject.lmUnsetContainer();
		}
	}

	@SuppressWarnings("unchecked")
	public static final Stream<LMObject> streamTree(final LMObject root)
	{
		final var childStream = root.lmGroup()
									.features()
									.stream()
									.filter(feature -> feature instanceof Relation<?, ?, ?, ?> relation &&
													   relation.contains())
									.flatMap(feature -> streamChildren(root, (Relation<LMObject, ?, ?, ?>) feature));

		return Stream.concat(Stream.of(root), childStream);
	}

	@SuppressWarnings("unchecked")
	public static final <T extends LMObject> Stream<T> streamChildren(final LMObject element,
																	  final Relation<T, ?, ?, ?> relation)
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

	public record ChildReference(Feature<?, ?, ?, ?> reference, int index)
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
