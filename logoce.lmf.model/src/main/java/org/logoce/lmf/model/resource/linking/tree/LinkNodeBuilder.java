package org.logoce.lmf.model.resource.linking.tree;

import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.resource.interpretation.PGroup;
import org.logoce.lmf.model.resource.interpretation.PType;
import org.logoce.lmf.model.resource.linking.ModelGroup;
import org.logoce.lmf.model.resource.linking.TreeToFeatureLinker;
import org.logoce.lmf.model.resource.linking.exception.LinkException;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.util.ModelUtils;
import org.logoce.lmf.model.util.tree.NavigableDataTree;
import org.logoce.lmf.model.util.tree.Tree;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class LinkNodeBuilder<I extends PNode>
{
	private final Map<String, ModelGroup<?>> groups;

	private final Map<Group<?>, TreeToFeatureLinker> resolvers;
	private final BiConsumer<I, LinkException> exceptionManager;

	public LinkNodeBuilder(final Map<String, ModelGroup<?>> groups,
						   final Map<Group<?>, TreeToFeatureLinker> resolvers,
						   final BiConsumer<I, LinkException> exceptionManager)
	{
		this.groups = groups;
		this.resolvers = resolvers;
		this.exceptionManager = exceptionManager;
	}

	public LinkNode<I> mapTree(final Tree<PGroup<I>> tree)
	{
		return tree.map(this::buildNodeInfo, this::buildNode);
	}

	private LinkNode<I> buildNode(final NavigableDataTree.BuildInfo<LinkNodeInfo<I>, LinkNode<I>> buildInfo)
	{
		final var info = buildInfo.data();
		return info.buildNode(buildInfo);
	}

	private <T extends LMObject> LinkNodeInfo<I> buildNodeInfo(final Tree<PGroup<I>> node)
	{
		final var pGroup = node.data();
		final var parent = node.parent();
		final var nodeType = pGroup.type();

		try
		{
			if (parent != null)
			{
				final var parentType = parent.data().type();
				final var parentModelGroup = findModelGroupByValue(parentType).orElseThrow(() -> cannotFindGroup(
						parentType));
				final var parentGroup = parentModelGroup.group();
				final var modelGroup = this.<T>findModelGroupByValue(nodeType)
										   .orElseGet(() -> findModelGroupFromParent(pGroup, parentGroup));
				return buildNodeInfoWithParent(pGroup, parentGroup, modelGroup);
			}
			else
			{
				final var modelGroup = this.<T>findModelGroupByValue(nodeType)
										   .orElseThrow(() -> cannotFindGroup(nodeType));
				return new LinkNodeInfo.Resolved<>(pGroup.pnode(), null, pGroup.features(), modelGroup);
			}
		}
		catch (LinkException e)
		{
			exceptionManager.accept(pGroup.pnode(), e);
			return new LinkNodeInfo.Failed<>(pGroup.pnode(), e);
		}
	}

	private <T extends LMObject> LinkNodeInfo<I> buildNodeInfoWithParent(final PGroup<I> pGroup,
																		 final Group<?> parentGroup,
																		 final ModelGroup<T> effectiveGroup)
	{
		final var resolvedRelation = resolveContainmentRelation(pGroup, parentGroup, effectiveGroup);
		return new LinkNodeInfo.Resolved<>(pGroup.pnode(), resolvedRelation, pGroup.features(), effectiveGroup);
	}

	@SuppressWarnings("unchecked")
	private <T extends LMObject> Optional<ModelGroup<T>> findModelGroupByValue(final PType type)
	{
		return type.value().map(s -> (ModelGroup<T>) groups.get(s));
	}

	@SuppressWarnings("unchecked")
	private <T extends LMObject> ModelGroup<T> findModelGroupFromParent(final PGroup<I> node,
																		final Group<?> parentGroup)
	{
		final var containmentName = node.type().firstToken();
		final var groupFromParent = parentGroup.features()
											   .stream()
											   .filter(Relation.class::isInstance)
											   .map(Relation.class::cast)
											   .filter(r -> r.name().equals(containmentName))
											   .map(r -> r.reference().group())
											   .findAny()
											   .orElseThrow(() -> new LinkException("Cannot resolve " +
																					"containance of " +
																					containmentName));

		return (ModelGroup<T>) groups.get(groupFromParent.name());
	}

	@SuppressWarnings("unchecked")
	private <T extends LMObject> Relation<T, ?> resolveContainmentRelation(final PGroup<I> node,
																		   final Group<?> parentGroup,
																		   final ModelGroup<T> modelGroup)
	{
		final var containmentName = node.type().firstToken();
		final var fromName = resolveFromName(parentGroup, containmentName);
		return (Relation<T, ?>) fromName.or(() -> resolveFromGroup(parentGroup, modelGroup))
										.orElseThrow(() -> buildLinkException(parentGroup,
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

	private static <T extends LMObject> LinkException buildLinkException(final Group<?> parentGroup,
																		 final ModelGroup<T> modelGroup,
																		 final String containmentName)
	{
		return new LinkException("Cannot find containment " +
								 "relation from parent " +
								 parentGroup.name() +
								 " (" +
								 containmentName +
								 ") to child " +
								 modelGroup.group().name());
	}

	private static LinkException cannotFindGroup(final PType nodeType)
	{
		return new LinkException("Cannot find Group: " + nodeType.value());
	}
}
