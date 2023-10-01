package org.logoce.lmf.model.resource.transform.node;

import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.resource.transform.word.TreeToFeatureResolver;
import org.logoce.lmf.model.util.ModelUtils;
import org.logoce.lmf.model.util.Tree;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

public class TreeBuilderNodeBuilder
{
	private final Map<String, ModelGroup<?>> groups;

	private final Map<Group<?>, TreeToFeatureResolver> resolvers;

	public TreeBuilderNodeBuilder(final Map<String, ModelGroup<?>> groups,
								  final Map<Group<?>, TreeToFeatureResolver> resolvers)
	{
		this.groups = groups;
		this.resolvers = resolvers;
	}

	public <T extends LMObject> TreeBuilderNode<T> mapTree(final Tree<PNode> tree)
	{
		return tree.map(this::buildNodeInfo, TreeBuilderNode<T>::new);
	}

	private <T extends LMObject> BuilderNodeInfo<T> buildNodeInfo(final Tree<PNode> node)
	{
		final var pNode = node.data();
		final var modelGroup = this.<T>findModelGroup(pNode, false);
		final var parent = node.parent();
		if (parent != null)
		{
			final var parentModelGroup = findModelGroup(parent.data(), true);
			final var parentGroup = parentModelGroup.group();
			return buildNodeInfoWithParent(pNode, parentGroup, modelGroup);
		}
		else
		{
			return new BuilderNodeInfo<>(null, pNode.values(), modelGroup);
		}
	}

	private <T extends LMObject> BuilderNodeInfo<T> buildNodeInfoWithParent(final PNode node,
																			final Group<?> parentGroup,
																			final ModelGroup<T> modelGroup)
	{
		final var effectiveGroup = modelGroup != null
								   ? modelGroup
								   : this.<T>findModelGroupFromParent(node, parentGroup);
		final var resolvedRelation = resolveContainmentRelation(node, parentGroup, effectiveGroup);

		return new BuilderNodeInfo<>(resolvedRelation, node.values(), effectiveGroup);
	}

	@SuppressWarnings("unchecked")
	private <T extends LMObject> ModelGroup<T> findModelGroup(final PNode node, boolean mandatory)
	{
		final var groupName = node.type().values().get(0);
		final var modelGroup = groups.get(groupName);
		if (mandatory && modelGroup == null)
		{
			throw new NoSuchElementException("Cannot find Group: " + groupName);
		}
		return (ModelGroup<T>) modelGroup;
	}

	@SuppressWarnings("unchecked")
	private <T extends LMObject> ModelGroup<T> findModelGroupFromParent(final PNode node, final Group<?> parentGroup)
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
	private <T extends LMObject> Relation<T, ?> resolveContainmentRelation(final PNode node,
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
}
