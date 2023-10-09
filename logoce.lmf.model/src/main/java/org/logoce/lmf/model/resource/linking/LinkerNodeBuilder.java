package org.logoce.lmf.model.resource.linking;

import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.resource.interpretation.PGroup;
import org.logoce.lmf.model.resource.interpretation.PType;
import org.logoce.lmf.model.resource.transform.word.TreeToFeatureResolver;
import org.logoce.lmf.model.util.ModelUtils;
import org.logoce.lmf.model.util.Tree;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

public class LinkerNodeBuilder
{
	private final Map<String, ModelGroup<?>> groups;

	private final Map<Group<?>, TreeToFeatureResolver> resolvers;

	public LinkerNodeBuilder(final Map<String, ModelGroup<?>> groups,
							 final Map<Group<?>, TreeToFeatureResolver> resolvers)
	{
		this.groups = groups;
		this.resolvers = resolvers;
	}

	public <T extends LMObject> LinkerNode<T> mapTree(final Tree<PGroup> tree)
	{
		return tree.map(this::buildNodeInfo, LinkerNode::new);
	}

	private <T extends LMObject> LinkNodeInfo<T> buildNodeInfo(final Tree<PGroup> node)
	{
		final var pNode = node.data();
		final var parent = node.parent();
		final var nodeType = pNode.type();
		if (parent != null)
		{
			final var parentType = parent.data().type();
			final var parentModelGroup = findModelGroupByValue(parentType).orElseThrow(() -> cannotFindGroup(parentType));
			final var parentGroup = parentModelGroup.group();
			final var modelGroup = this.<T>findModelGroupByValue(nodeType)
									   .orElseGet(() -> findModelGroupFromParent(pNode, parentGroup));
			return buildNodeInfoWithParent(pNode, parentGroup, modelGroup);
		}
		else
		{
			final var modelGroup = this.<T>findModelGroupByValue(nodeType).orElseThrow(() -> cannotFindGroup(nodeType));
			return new LinkNodeInfo<>(null, pNode.features(), modelGroup);
		}
	}

	private <T extends LMObject> LinkNodeInfo<T> buildNodeInfoWithParent(final PGroup node,
																		 final Group<?> parentGroup,
																		 final ModelGroup<T> effectiveGroup)
	{
		final var resolvedRelation = resolveContainmentRelation(node, parentGroup, effectiveGroup);
		return new LinkNodeInfo<>(resolvedRelation, node.features(), effectiveGroup);
	}

	@SuppressWarnings("unchecked")
	private <T extends LMObject> Optional<ModelGroup<T>> findModelGroupByValue(final PType type)
	{
		return type.value().map(s -> (ModelGroup<T>) groups.get(s));
	}

	@SuppressWarnings("unchecked")
	private <T extends LMObject> ModelGroup<T> findModelGroupFromParent(final PGroup node, final Group<?> parentGroup)
	{
		final var containmentName = node.type().firstToken();
		final var groupFromParent = parentGroup.features()
											   .stream()
											   .filter(Relation.class::isInstance)
											   .map(Relation.class::cast)
											   .filter(r -> r.name().equals(containmentName))
											   .map(r -> r.reference().group())
											   .findAny()
											   .orElseThrow(() -> new NoSuchElementException("Cannot resolve " +
																							 "containance of " +
																							 containmentName));

		return (ModelGroup<T>) groups.get(groupFromParent.name());
	}

	@SuppressWarnings("unchecked")
	private <T extends LMObject> Relation<T, ?> resolveContainmentRelation(final PGroup node,
																		   final Group<?> parentGroup,
																		   final ModelGroup<T> modelGroup)
	{
		final var containmentName = node.type().firstToken();
		final var fromName = resolveFromName(parentGroup, containmentName);
		return (Relation<T, ?>) fromName.or(() -> resolveFromGroup(parentGroup, modelGroup))
										.orElseThrow(() -> buildNoSuchElementException(parentGroup,
																					   modelGroup,
																					   containmentName));
	}

	private <T extends LMObject> Optional<? extends Relation<?, ?>> resolveFromGroup(final Group<?> parentGroup,
																					 final ModelGroup<T> modelGroup)
	{
		return resolvers.get(parentGroup)
						.streamContainmentRelations()
						.filter(r -> ModelUtils.isSubGroup(r.reference().group(), modelGroup.group()))
						.findAny();
	}

	private Optional<Relation<?, ?>> resolveFromName(final Group<?> parentGroup, final String containmentName)
	{
		return resolvers.get(parentGroup)
						.streamContainmentRelations()
						.filter(f -> f.name().equals(containmentName))
						.findAny();
	}

	private static <T extends LMObject> NoSuchElementException buildNoSuchElementException(final Group<?> parentGroup,
																						   final ModelGroup<T> modelGroup,
																						   final String containmentName)
	{
		return new NoSuchElementException("Cannot find containment " +
										  "relation from parent " +
										  parentGroup.name() +
										  " (" +
										  containmentName +
										  ") to child " +
										  modelGroup.group().name());
	}

	private static NoSuchElementException cannotFindGroup(final PType nodeType)
	{
		return new NoSuchElementException("Cannot find Group: " + nodeType.value());
	}
}
