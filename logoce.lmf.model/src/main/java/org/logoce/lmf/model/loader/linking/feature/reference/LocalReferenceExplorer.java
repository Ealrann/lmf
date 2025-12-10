package org.logoce.lmf.model.loader.linking.feature.reference;

import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Named;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.loader.linking.FeatureResolution;
import org.logoce.lmf.model.loader.linking.ResolutionAttempt;
import org.logoce.lmf.model.loader.linking.feature.AttributeResolver;
import org.logoce.lmf.model.loader.linking.feature.RelationResolver;
import org.logoce.lmf.model.loader.linking.tree.LinkNodeInternal;
import org.logoce.lmf.model.util.ModelUtil;

import java.util.NoSuchElementException;
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
		current = start;
		while (parser.hasNext())
		{
			final var step = parser.next();

			switch (step.type())
			{
				case ROOT -> current = current.root();
				case NAME -> current = searchNamedNode(current.root(), step.text());
				case CONTEXT_NAME -> current = searchContextNamedNode(current, step.text());
				case PARENT -> current = current.parent();
				case CHILD -> current = searchChild(step.text());
				default -> throw new IllegalStateException("Unsupported path step: " + step.type());
			}
		}

		if (ModelUtil.isSubGroup(relation.concept(), current.group()))
		{
			return Optional.of(buildResolution(relation, current));
		}
		else
		{
			return Optional.empty();
		}
	}

	private LinkNodeInternal<?, ?, ?> searchChild(final String text)
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
																									   final org.logoce.lmf.model.loader.linking.LinkNode<?, ?> current)
	{
		return new RelationResolver.DynamicReferenceResolution<>(feature,
																 (org.logoce.lmf.model.loader.linking.LinkNode<T, ?>) current);
	}

	private LinkNodeInternal<?, ?, ?> searchNamedNode(final LinkNodeInternal<?, ?, ?> root, final String name)
	{
		return root.streamTree()
				   .filter(this::groupMatch)
				   .filter(node -> nameMatch(node, name))
				   .findAny()
				   .orElseThrow(() -> new NoSuchElementException("Cannot find named Object : " + name));
	}

	private LinkNodeInternal<?, ?, ?> searchContextNamedNode(final LinkNodeInternal<?, ?, ?> startNode,
															 final String name)
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

		throw new NoSuchElementException("Cannot find context-named Object : " + name);
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
}
