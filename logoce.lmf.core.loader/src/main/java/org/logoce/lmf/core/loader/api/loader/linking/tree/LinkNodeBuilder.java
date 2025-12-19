package org.logoce.lmf.core.loader.api.loader.linking.tree;

import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.loader.api.loader.linking.LinkException;
import org.logoce.lmf.core.loader.api.loader.linking.ModelGroup;
import org.logoce.lmf.core.loader.linking.TreeToFeatureLinker;
import org.logoce.lmf.core.loader.interpretation.PGroup;
import org.logoce.lmf.core.loader.interpretation.PType;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;
import org.logoce.lmf.core.api.util.ModelUtil;
import org.logoce.lmf.core.util.tree.BasicTree;
import org.logoce.lmf.core.util.tree.Tree;

import java.util.Map;
import java.util.Optional;

public final class LinkNodeBuilder<I extends PNode>
{
	private final Map<String, ModelGroup<?>> metaGroups;
	private final Map<Group<?>, TreeToFeatureLinker> metaResolvers;

	public LinkNodeBuilder(final Map<String, ModelGroup<?>> metaGroups,
						   final Map<Group<?>, TreeToFeatureLinker> metaResolvers)
	{
		this.metaGroups = metaGroups;
		this.metaResolvers = metaResolvers;
	}

	public LinkNodeFull<?, I> mapTree(final Tree<PGroup<I>> tree)
	{
		return tree.map(this::buildNodeInfo, this::buildNode);
	}

	public void resolve(final LinkNodeInternal<?, I, ?> node)
	{
		requireResolver(node.group(), node.pNode()).resolve(node);
	}

	private LinkNodeFull<?, I> buildNode(final BasicTree.BuildInfo<LinkInfo<?, I>, LinkNodeFull<?, I>> buildInfo)
	{
		@SuppressWarnings("unchecked")
		final var data = (LinkInfo<?, I>) buildInfo.data();
		final var resolver = requireResolver(data.modelGroup().group(), data.pNode());
		final var attributeResolutions = resolver.nodeLinker().resolveAttributes(data.features());

		return new LinkNodeFull<>(data, buildInfo.parent(), attributeResolutions, buildInfo.childrenBuilder());
	}

	private <T extends LMObject> LinkInfo<?, I> buildNodeInfo(final BasicTree<PGroup<I>, ?> node)
	{
		final var pGroup = node.data();
		final var parent = node.parent();

		final var modelGroup = resolveModelGroup(node);

		return parent != null
			   ? buildNodeInfoWithParent(pGroup, resolveModelGroup(parent).group(), modelGroup)
			   : new LinkInfo<>(pGroup.pnode(), null, pGroup.features(), modelGroup);
	}

	@SuppressWarnings("unchecked")
	private <T extends LMObject> ModelGroup<T> resolveModelGroup(final BasicTree<PGroup<I>, ?> node)
	{
		final var directResolution = this.<T>findModelGroupByValue(node.data().type());
		if (directResolution.isPresent()) return directResolution.get();

		final var parent = node.parent();
		if (parent == null) throw buildException(node.data(), node.data().type());

		final var parentGroup = resolveModelGroup(parent).group();
		return findModelGroupFromParent(node.data(), parentGroup);
	}

	private <T extends LMObject> LinkInfo<T, I> buildNodeInfoWithParent(final PGroup<I> pGroup,
																		final Group<?> parentGroup,
																		final ModelGroup<T> effectiveGroup)
	{
		final var resolvedRelation = resolveContainmentRelation(pGroup, parentGroup, effectiveGroup.group());
		return new LinkInfo<>(pGroup.pnode(), resolvedRelation, pGroup.features(), effectiveGroup);
	}

	@SuppressWarnings("unchecked")
	private <T extends LMObject> Optional<ModelGroup<T>> findModelGroupByValue(final PType type)
	{
		return type.value().map(s -> (ModelGroup<T>) metaGroups.get(s));
	}

	@SuppressWarnings("unchecked")
	private <T extends LMObject> ModelGroup<T> findModelGroupFromParent(final PGroup<I> node,
																		final Group<?> parentGroup)
	{
		final var containmentName = node.type().firstToken();
		final var groupFromParent = findContainmentRelationByName(parentGroup, containmentName, node.pnode())
			.map(Relation::concept)
			.orElseThrow(() -> buildException(node, containmentName, parentGroup));

		final var resolved = (ModelGroup<T>) metaGroups.get(groupFromParent.name());
		if (resolved == null)
		{
			throw new LinkException("Cannot find Group: " + groupFromParent.name(), node.pnode());
		}
		return resolved;
	}

	@SuppressWarnings("unchecked")
	private <T extends LMObject> Relation<T, ?, ?, ?> resolveContainmentRelation(final PGroup<I> node,
																					final Group<?> parentGroup,
																					final Group<T> childGroup)
	{
		final var containmentName = node.type().firstToken();
		final var fromName = resolveFromName(node, parentGroup, containmentName);
		return (Relation<T, ?, ?, ?>) fromName.or(() -> resolveFromGroup(node, parentGroup, childGroup))
												   .orElseThrow(() -> buildException(node,
																					 parentGroup,
																					 childGroup.name(),
																					 containmentName));
	}

	private <T extends LMObject> Optional<? extends Relation<?, ?, ?, ?>> resolveFromGroup(final PGroup<I> node,
																						   final Group<?> parentGroup,
																						   final Group<T> childGroup)
	{
		return requireResolver(parentGroup, node.pnode())
			.streamContainmentRelations()
			.filter(r -> ModelUtil.isSubGroup(r.concept(), childGroup))
			.findFirst();
	}

	private Optional<Relation<?, ?, ?, ?>> resolveFromName(final PGroup<I> node,
														   final Group<?> parentGroup,
														   final String containmentName)
	{
		return findContainmentRelationByName(parentGroup, containmentName, node.pnode());
	}

	private Optional<Relation<?, ?, ?, ?>> findContainmentRelationByName(final Group<?> parentGroup,
																		 final String containmentName,
																		 final PNode pNode)
	{
		return requireResolver(parentGroup, pNode)
			.streamContainmentRelations()
			.filter(r -> r.name().equals(containmentName))
			.findFirst();
	}

	private TreeToFeatureLinker requireResolver(final Group<?> group, final PNode pNode)
	{
		final var resolver = metaResolvers.get(group);
		if (resolver == null)
		{
			final var groupName = group != null ? group.name() : "<null>";
			throw new LinkException("No meta resolver registered for group " + groupName, pNode);
		}
		return resolver;
	}

	private static <I extends PNode> LinkException buildException(final PGroup<I> node,
																  final String containmentName,
																  final Group<?> parentGroup)
	{
		return new LinkException("Parent group " +
								 parentGroup.name() +
								 " doesn't contain a feature named " +
								 containmentName, node.pnode());
	}

	private static LinkException buildException(final PGroup<?> node,
												final Group<?> parentGroup,
												final String childGroupName,
												final String containmentName)
	{
		return new LinkException("Cannot find containment relation from parent " +
								 parentGroup.name() +
								 " (" +
								 containmentName +
								 ") to child " +
								 childGroupName, node.pnode());
	}

	private static LinkException buildException(final PGroup<?> node, final PType nodeType)
	{
		final var value = nodeType.name().orElse(nodeType.value().orElse("<unknown>"));
		return new LinkException("Cannot find Group: " + value, node.pnode());
	}
}
