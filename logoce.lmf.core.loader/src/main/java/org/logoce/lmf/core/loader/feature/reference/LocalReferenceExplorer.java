package org.logoce.lmf.core.loader.feature.reference;

import org.logoce.lmf.core.loader.api.loader.linking.LinkNode;
import org.logoce.lmf.core.loader.api.loader.linking.AmbiguousReferenceException;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.Named;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.loader.api.loader.linking.FeatureResolution;
import org.logoce.lmf.core.loader.api.loader.linking.InvalidReferenceException;
import org.logoce.lmf.core.loader.api.loader.linking.ResolutionAttempt;
import org.logoce.lmf.core.loader.feature.AttributeResolver;
import org.logoce.lmf.core.loader.feature.RelationResolver;
import org.logoce.lmf.core.loader.api.loader.linking.tree.LinkNodeInternal;
import org.logoce.lmf.core.api.util.ModelUtil;

import java.util.NoSuchElementException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class LocalReferenceExplorer implements ReferenceResolver
{
	private final LinkNodeInternal<?, ?, ?> start;
	private final Relation<?, ?, ?, ?> relation;

	private LinkNodeInternal<?, ?, ?> current;

	public LocalReferenceExplorer(final LinkNodeInternal<?, ?, ?> node, final Relation<?, ?, ?, ?> relation)
	{
		this.start = node;
		this.relation = relation;
	}

	@Override
	public Optional<FeatureResolution<Relation<?, ?, ?, ?>>> resolve(final PathParser parser)
	{
		final var rawReference = parser.rawPath();
		current = start;
		while (parser.hasNext())
		{
			final var step = parser.next();

			switch (step.type())
			{
				case ROOT -> current = current.root();
				case NAME ->
				{
					if (parser.hasNext())
					{
						final var remaining = collectRemaining(parser);
						current = resolveNamedPath(current.root(), step.text(), remaining, rawReference);
					}
					else
					{
						current = searchNamedNode(current.root(), step.text(), rawReference);
					}
				}
				case CONTEXT_NAME ->
				{
					if (parser.hasNext())
					{
						final var remaining = collectRemaining(parser);
						current = resolveContextNamedPath(current, step.text(), remaining, rawReference);
					}
					else
					{
						current = searchContextNamedNode(current, step.text(), rawReference);
					}
				}
				case PARENT -> current = requireParent(current);
				case CHILD -> current = searchChild(current, step.text());
				case CURRENT ->
				{
				}
				default -> throw new IllegalStateException("Unsupported path step: " + step.type());
			}
		}

		if (ModelUtil.isSubGroup(relation.concept(), current.group()))
		{
			return Optional.of(buildResolution(relation, current));
		}
		else
		{
			throw new InvalidReferenceException(relation.name(),
												rawReference,
												expectedConceptName(),
												current.group().name());
		}
	}

	private static List<PathParser.Step> collectRemaining(final PathParser parser)
	{
		final var steps = new ArrayList<PathParser.Step>();
		while (parser.hasNext())
		{
			steps.add(parser.next());
		}
		return List.copyOf(steps);
	}

	private static LinkNodeInternal<?, ?, ?> requireParent(final LinkNodeInternal<?, ?, ?> node)
	{
		final var parent = node.parent();
		if (parent == null)
		{
			throw new NoSuchElementException("Cannot resolve parent of root node");
		}
		return parent;
	}

	private static LinkNodeInternal<?, ?, ?> searchChild(final LinkNodeInternal<?, ?, ?> current, final String text)
	{
		final var pointIndex = text.indexOf('.');
		final var featureName = pointIndex == -1 ? text : text.substring(0, pointIndex);
		final int index = pointIndex == -1 ? 0 : Integer.parseInt(text.substring(pointIndex + 1));
		final var children = current.streamChildren()
									.filter(c -> c.containingRelation().name().equals(featureName))
									.toList();
		if (children.size() < index + 1)
		{
			throw new NoSuchElementException("Cannot resolve step " + text);
		}
		return children.get(index);
	}

	@SuppressWarnings("unchecked")
	private static <T extends LMObject> RelationResolver.DynamicReferenceResolution<T> buildResolution(final Relation<T, ?, ?, ?> feature,
																									   final LinkNode<?, ?> current)
	{
		return new RelationResolver.DynamicReferenceResolution<>(feature,
																 (LinkNode<T, ?>) current);
	}

	private LinkNodeInternal<?, ?, ?> searchNamedNode(final LinkNodeInternal<?, ?, ?> root,
													 final String name,
													 final String rawReference)
	{
		final var candidates = root.streamTree()
								  .filter(node -> nameMatch(node, name))
								  .toList();
		final var matches = candidates.stream().filter(this::groupMatch).toList();
		if (!matches.isEmpty())
		{
			return matches.getFirst();
		}

		if (candidates.size() == 1)
		{
			throw new InvalidReferenceException(relation.name(),
												rawReference,
												expectedConceptName(),
												candidates.getFirst().group().name());
		}
		throw new InvalidReferenceException(relation.name(),
											rawReference,
											expectedConceptName());
	}

	private LinkNodeInternal<?, ?, ?> resolveNamedPath(final LinkNodeInternal<?, ?, ?> root,
													  final String name,
													  final List<PathParser.Step> remaining,
													  final String rawReference)
	{
		final var resolved = root.streamTree()
								 .filter(node -> nameMatch(node, name))
								 .map(candidate -> tryResolveFrom(candidate, remaining))
								 .filter(Optional::isPresent)
								 .map(Optional::get)
								 .toList();
		final var matches = resolved.stream().filter(this::groupMatch).toList();
		if (matches.size() == 1)
		{
			return matches.getFirst();
		}
		if (matches.size() > 1)
		{
			throw new AmbiguousReferenceException(relation.name(),
												  rawReference,
												  expectedConceptName(),
												  matches.size());
		}
		if (resolved.size() == 1)
		{
			throw new InvalidReferenceException(relation.name(),
												rawReference,
												expectedConceptName(),
												resolved.getFirst().group().name());
		}
		throw new InvalidReferenceException(relation.name(),
											rawReference,
											expectedConceptName());
	}

	private LinkNodeInternal<?, ?, ?> searchContextNamedNode(final LinkNodeInternal<?, ?, ?> startNode,
															 final String name,
															 final String rawReference)
	{
		LinkNodeInternal<?, ?, ?> cursor = startNode;

		while (cursor != null)
		{
			if (groupMatch(cursor) && nameMatch(cursor, name))
			{
				return cursor;
			}

			final var matchingChild = cursor.streamChildren()
										   .filter(this::groupMatch)
										   .filter(node -> nameMatch(node, name))
										   .findAny();
			if (matchingChild.isPresent())
			{
				return matchingChild.get();
			}

			cursor = cursor.parent();
		}

		final LinkNodeInternal<?, ?, ?> any;
		try
		{
			any = searchContextNamedNodeAny(startNode, name);
		}
		catch (NoSuchElementException ignored)
		{
			throw new InvalidReferenceException(relation.name(),
												rawReference,
												expectedConceptName());
		}
		throw new InvalidReferenceException(relation.name(),
											rawReference,
											expectedConceptName(),
											any.group().name());
	}

	private LinkNodeInternal<?, ?, ?> resolveContextNamedPath(final LinkNodeInternal<?, ?, ?> startNode,
															  final String name,
															  final List<PathParser.Step> remaining,
															  final String rawReference)
	{
		final var anchor = searchContextNamedNodeAny(startNode, name);
		final var resolved = tryResolveFrom(anchor, remaining);
		if (resolved.isEmpty())
		{
			throw new InvalidReferenceException(relation.name(),
												rawReference,
												expectedConceptName());
		}
		final var node = resolved.get();
		if (!groupMatch(node))
		{
			throw new InvalidReferenceException(relation.name(),
												rawReference,
												expectedConceptName(),
												node.group().name());
		}
		return node;
	}

	private static LinkNodeInternal<?, ?, ?> searchContextNamedNodeAny(final LinkNodeInternal<?, ?, ?> startNode,
																	  final String name)
	{
		LinkNodeInternal<?, ?, ?> cursor = startNode;

		while (cursor != null)
		{
			if (nameMatch(cursor, name))
			{
				return cursor;
			}

			final var matchingChild = cursor.streamChildren()
											.filter(node -> nameMatch(node, name))
											.findAny();
			if (matchingChild.isPresent())
			{
				return matchingChild.get();
			}

			cursor = cursor.parent();
		}

		throw new NoSuchElementException("Cannot find context-named Object : " + name);
	}

	private Optional<LinkNodeInternal<?, ?, ?>> tryResolveFrom(final LinkNodeInternal<?, ?, ?> start,
															  final List<PathParser.Step> remaining)
	{
		LinkNodeInternal<?, ?, ?> cursor = start;
		try
		{
			for (final var step : remaining)
			{
				cursor = switch (step.type())
				{
					case ROOT -> cursor.root();
					case NAME -> searchNamedNodeAny(cursor.root(), step.text());
					case CONTEXT_NAME -> searchContextNamedNodeAny(cursor, step.text());
					case PARENT -> requireParent(cursor);
					case CURRENT -> cursor;
					case CHILD -> searchChild(cursor, step.text());
					default -> throw new IllegalStateException("Unsupported path step: " + step.type());
				};
			}
			return Optional.of(cursor);
		}
		catch (final NoSuchElementException exception)
		{
			return Optional.empty();
		}
	}

	private static LinkNodeInternal<?, ?, ?> searchNamedNodeAny(final LinkNodeInternal<?, ?, ?> root, final String name)
	{
		return root.streamTree()
				   .filter(node -> nameMatch(node, name))
				   .findAny()
				   .orElseThrow(() -> new NoSuchElementException("Cannot find named Object : " + name));
	}

	private boolean groupMatch(final LinkNodeInternal<?, ?, ?> node)
	{
		final var concept = relation.concept();
		if (concept instanceof Group<?> group)
		{
			return ModelUtil.isSubGroup(group, node.group());
		}
		else
		{
			return false;
		}
	}

	private static boolean nameMatch(final LinkNodeInternal<?, ?, ?> node, final String name)
	{
		return node.attributeResolutions()
				   .stream()
				   .map(ResolutionAttempt::resolution)
				   .filter(AttributeResolver.AttributeResolution.class::isInstance)
				   .map(AttributeResolver.AttributeResolution.class::cast)
				   .filter(r -> r.feature().id() == Named.FeatureIDs.NAME)
				   .anyMatch(r -> name.equals(r.value()));
	}

	private String expectedConceptName()
	{
		final var concept = relation.concept();
		return concept == null ? null : concept.name();
	}
}
