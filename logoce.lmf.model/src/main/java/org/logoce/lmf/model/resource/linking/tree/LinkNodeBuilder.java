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
import org.logoce.lmf.model.util.tree.BasicTree;
import org.logoce.lmf.model.util.tree.Tree;

import java.util.Map;
import java.util.Optional;

public final class LinkNodeBuilder<I extends PNode>
{
	private final Map<String, ModelGroup<?>> groups;
	private final Map<Group<?>, TreeToFeatureLinker> resolvers;

	public LinkNodeBuilder(final Map<String, ModelGroup<?>> groups, final Map<Group<?>, TreeToFeatureLinker> resolvers)
	{
		this.groups = groups;
		this.resolvers = resolvers;
	}

	public LinkNodeFull<?, I> mapTree(final Tree<PGroup<I>> tree)
	{
		return tree.map(this::buildNodeInfo, this::buildNode);
	}

	public <Y extends BasicTree<PGroup<I>, Y>> LinkNodePartial<?, I> mapPartial(final Y node)
	{
		final var linkInfo = buildNodeInfo(node);
		final var data = node.data();
		final var resolver = resolvers.get(linkInfo.modelGroup().group());
		final var attributeResolutions = resolver.nodeLinker.resolveAttributes(data.features());
		return new LinkNodePartial<>(linkInfo, node, attributeResolutions, this::mapPartial);
	}

	public void resolve(final LinkNodeInternal<?, I, ?> node)
	{
		final var resolver = resolvers.get(node.group());
		resolver.resolve(node);
	}

	private LinkNodeFull<?, I> buildNode(final BasicTree.BuildInfo<LinkInfo<?, I>, LinkNodeFull<?, I>> buildInfo)
	{
		final var data = (LinkInfo<?, I>) buildInfo.data();
		final var resolver = resolvers.get(data.modelGroup().group());
		final var attributeResolutions = resolver.nodeLinker.resolveAttributes(data.features());

		return new LinkNodeFull<>(data, buildInfo.parent(), attributeResolutions, buildInfo.childrenBuilder());
	}

	private <T extends LMObject> LinkInfo<?, I> buildNodeInfo(final BasicTree<PGroup<I>, ?> node)
	{
		final var pGroup = node.data();
		final var parent = node.parent();
		final var nodeType = pGroup.type();

		if (parent != null)
		{
			final var parentData = parent.data();
			final var parentType = parentData.type();
			final var parentModelGroup = findModelGroupByValue(parentType).orElseThrow(() -> buildException(parentData,
																											parentType));
			final var parentGroup = parentModelGroup.group();
			final var modelGroup = this.<T>findModelGroupByValue(nodeType)
									   .orElseGet(() -> findModelGroupFromParent(pGroup, parentGroup));
			return buildNodeInfoWithParent(pGroup, parentGroup, modelGroup);
		}
		else
		{
			final var modelGroup = this.<T>findModelGroupByValue(nodeType)
									   .orElseThrow(() -> buildException(pGroup, nodeType));
			return new LinkInfo<>(pGroup.pnode(), null, pGroup.features(), modelGroup);
		}
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
											   .orElseThrow(() -> buildException(node, containmentName, parentGroup));

		return (ModelGroup<T>) groups.get(groupFromParent.name());
	}

	@SuppressWarnings("unchecked")
	private <T extends LMObject> Relation<T, ?> resolveContainmentRelation(final PGroup<I> node,
																		   final Group<?> parentGroup,
																		   final Group<T> childGroup)
	{
		final var containmentName = node.type().firstToken();
		final var fromName = resolveFromName(parentGroup, containmentName);
		return (Relation<T, ?>) fromName.or(() -> resolveFromGroup(parentGroup, childGroup))
										.orElseThrow(() -> buildException(node,
																		  parentGroup,
																		  childGroup.name(),
																		  containmentName));
	}

	private <T extends LMObject> Optional<? extends Relation<?, ?>> resolveFromGroup(final Group<?> parentGroup,
																					 final Group<T> childGroup)
	{
		return resolvers.get(parentGroup)
						.streamContainmentRelations()
						.filter(r -> ModelUtils.isSubGroup(r.reference().group(), childGroup))
						.findAny();
	}

	private Optional<Relation<?, ?>> resolveFromName(final Group<?> parentGroup, final String containmentName)
	{
		return resolvers.get(parentGroup)
						.streamContainmentRelations()
						.filter(f -> f.name().equals(containmentName))
						.findAny();
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
		return new LinkException("Cannot find Group: " + nodeType.value(), node.pnode());
	}
}
