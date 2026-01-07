package org.logoce.lmf.cli.format;

import org.logoce.lmf.core.loader.api.loader.linking.tree.LinkNodeInternal;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public final class RootReferenceResolver
{
	public sealed interface Resolution permits Resolution.Found, Resolution.NotFound,
												 Resolution.Ambiguous, Resolution.Failure
	{
		record Found(LinkNodeInternal<?, PNode, ?> node) implements Resolution
		{
		}

		record NotFound(String message) implements Resolution
		{
		}

		record Ambiguous(List<String> candidates) implements Resolution
		{
		}

		record Failure(String message) implements Resolution
		{
		}
	}

	public Resolution resolve(final List<LinkNodeInternal<?, PNode, ?>> roots,
							  final String reference)
	{
		if (reference == null || reference.isBlank())
		{
			if (roots.size() == 1)
			{
				return new Resolution.Found(roots.getFirst());
			}
			return new Resolution.Ambiguous(describeCandidates(roots));
		}

		final var parser = new RootPathParser(reference);
		final var candidates = new ArrayList<LinkNodeInternal<?, PNode, ?>>(roots);

		try
		{
			while (parser.hasNext())
			{
				final var step = parser.next();
				switch (step.type())
				{
					case ROOT -> resetToRoots(candidates, roots);
					case CURRENT -> {}
					case PARENT -> moveToParents(candidates);
					case CHILD -> selectChild(candidates, step.text());
					case NAME -> selectNamed(candidates, step.text());
					case CONTEXT_NAME -> selectContextNamed(candidates, step.text());
					case MODEL -> {
						return new Resolution.Failure("Model-qualified references are not supported for --root");
					}
				}

				if (candidates.isEmpty())
				{
					return new Resolution.NotFound("Reference step could not be resolved: " + step.text());
				}
			}
		}
		catch (Exception e)
		{
			final var message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
			return new Resolution.Failure(message);
		}

		if (candidates.size() == 1)
		{
			return new Resolution.Found(candidates.getFirst());
		}
		return new Resolution.Ambiguous(describeCandidates(candidates));
	}

	public static List<LinkNodeInternal<?, PNode, ?>> collectLinkRoots(final List<? extends org.logoce.lmf.core.loader.api.loader.linking.LinkNode<?, PNode>> linkTrees)
	{
		if (linkTrees == null || linkTrees.isEmpty())
		{
			return List.of();
		}

		final var roots = new ArrayList<LinkNodeInternal<?, PNode, ?>>();
		for (final var node : linkTrees)
		{
			if (node instanceof LinkNodeInternal<?, PNode, ?> internal)
			{
				roots.add(internal);
			}
		}
		return List.copyOf(roots);
	}

	private static void resetToRoots(final List<LinkNodeInternal<?, PNode, ?>> candidates,
									 final List<LinkNodeInternal<?, PNode, ?>> roots)
	{
		candidates.clear();
		candidates.addAll(roots);
	}

	private static void moveToParents(final List<LinkNodeInternal<?, PNode, ?>> candidates)
	{
		for (int i = candidates.size() - 1; i >= 0; i--)
		{
			final var parent = candidates.get(i).parent();
			if (parent == null)
			{
				candidates.remove(i);
			}
			else
			{
				candidates.set(i, parent);
			}
		}
	}

	private static void selectChild(final List<LinkNodeInternal<?, PNode, ?>> candidates,
									final String text)
	{
		final var pointIndex = text.indexOf('.');
		final var featureName = pointIndex == -1 ? text : text.substring(0, pointIndex);
		final int index = pointIndex == -1 ? 0 : Integer.parseInt(text.substring(pointIndex + 1));

		final var selected = new ArrayList<LinkNodeInternal<?, PNode, ?>>();
		for (final var candidate : candidates)
		{
			final var children = candidate.streamChildren()
										  .filter(child -> child.containingRelation() != null)
										  .filter(child -> featureName.equals(child.containingRelation().name()))
										  .toList();
			if (index >= 0 && index < children.size())
			{
				selected.add(children.get(index));
			}
		}
		candidates.clear();
		candidates.addAll(selected);
	}

	private static void selectNamed(final List<LinkNodeInternal<?, PNode, ?>> candidates,
									final String name)
	{
		final var matches = new ArrayList<LinkNodeInternal<?, PNode, ?>>();
		final var visited = new HashSet<LinkNodeInternal<?, PNode, ?>>();

		for (final var candidate : candidates)
		{
			final var root = candidate.root();
			if (!visited.add(root))
			{
				continue;
			}
			root.streamTree()
				.filter(node -> nameMatch(node, name))
				.findAny()
				.ifPresent(matches::add);
		}

		candidates.clear();
		candidates.addAll(matches);
	}

	private static void selectContextNamed(final List<LinkNodeInternal<?, PNode, ?>> candidates,
										   final String name)
	{
		final var matches = new ArrayList<LinkNodeInternal<?, PNode, ?>>();
		for (final var candidate : candidates)
		{
			final var found = searchContextNamed(candidate, name);
			if (found != null)
			{
				matches.add(found);
			}
		}
		candidates.clear();
		candidates.addAll(matches);
	}

	private static LinkNodeInternal<?, PNode, ?> searchContextNamed(final LinkNodeInternal<?, PNode, ?> start,
																	final String name)
	{
		LinkNodeInternal<?, PNode, ?> cursor = start;

		while (cursor != null)
		{
			if (nameMatch(cursor, name))
			{
				return cursor;
			}

			final var childMatch = cursor.streamChildren()
										 .filter(node -> nameMatch(node, name))
										 .findAny();
			if (childMatch.isPresent())
			{
				return childMatch.get();
			}

			cursor = cursor.parent();
		}

		return null;
	}

	private static boolean nameMatch(final LinkNodeInternal<?, PNode, ?> node, final String name)
	{
		final var resolved = NodeNameResolver.resolve(node);
		return resolved != null && resolved.equals(name);
	}

	private static List<String> describeCandidates(final List<LinkNodeInternal<?, PNode, ?>> candidates)
	{
		final var descriptions = new ArrayList<String>();
		for (final var candidate : candidates)
		{
			final var name = NodeNameResolver.resolve(candidate);
			final var groupName = candidate.group() != null ? candidate.group().name() : "Unknown";
			descriptions.add(groupName + (name != null ? " " + name : ""));
		}
		return List.copyOf(descriptions);
	}
}
