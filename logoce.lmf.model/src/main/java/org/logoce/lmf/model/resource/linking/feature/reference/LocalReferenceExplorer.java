package org.logoce.lmf.model.resource.linking.feature.reference;

import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMCoreDefinition;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.resource.linking.FeatureResolution;
import org.logoce.lmf.model.resource.linking.feature.AttributeResolver;
import org.logoce.lmf.model.resource.linking.feature.RelationResolver;
import org.logoce.lmf.model.resource.linking.tree.LinkNodeInternal;
import org.logoce.lmf.model.resource.transform.LinkNode;
import org.logoce.lmf.model.resource.transform.ResolutionAttempt;
import org.logoce.lmf.model.util.ModelUtils;

import java.util.NoSuchElementException;
import java.util.Optional;

public class LocalReferenceExplorer implements ReferenceResolver
{
	private final LinkNodeInternal<?, ?, ?> start;
	private final Relation<?, ?> relation;

	private LinkNodeInternal<?, ?, ?> current;

	public LocalReferenceExplorer(final LinkNodeInternal<?, ?, ?> node, final Relation<?, ?> relation)
	{
		this.start = node;
		this.relation = relation;
	}

	@Override
	public Optional<FeatureResolution<Relation<?, ?>>> resolve(final PathParser parser)
	{
		current = start;
		while (parser.hasNext())
		{
			final var step = parser.next();

			switch (step.type())
			{
				case ROOT -> current = current.root();
				case NAME -> current = searchNamedNode(current.root(), step.text());
				case PARENT -> current = current.parent();
				case CHILD -> current = searchChild(step.text());
				default -> throw new IllegalStateException();
			}
		}

		if (ModelUtils.isSubGroup(relation.concept(), current.group()))
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
	private static <T extends LMObject> RelationResolver.DynamicReferenceResolution<T> buildResolution(final Relation<T, ?> feature,
																									   final LinkNode<?, ?> current)
	{
		return new RelationResolver.DynamicReferenceResolution<>(feature, (LinkNode<T, ?>) current);
	}

	private LinkNodeInternal<?, ?, ?> searchNamedNode(final LinkNodeInternal<?, ?, ?> root, final String name)
	{
		return root.streamTree()
				   .filter(this::groupMatch)
				   .filter(node -> nameMatch(node, name))
				   .findAny()
				   .orElseThrow(() -> new NoSuchElementException("Cannot find named Object : " + name));
	}

	private boolean groupMatch(LinkNodeInternal<?, ?, ?> node)
	{
		final var concept = relation.concept();
		if (concept instanceof Group<?> group)
		{
			return ModelUtils.isSubGroup(group, node.group());
		}
		else
		{
			return false;
		}
	}

	private static boolean nameMatch(LinkNodeInternal<?, ?, ?> node, String name)
	{
		return node.attributeResolutions()
				   .stream()
				   .map(ResolutionAttempt::resolution)
				   .filter(AttributeResolver.AttributeResolution.class::isInstance)
				   .map(AttributeResolver.AttributeResolution.class::cast)
				   .filter(r -> r.feature() == LMCoreDefinition.Features.NAMED.NAME)
				   .anyMatch(r -> name.equals(r.value()));
	}
}
