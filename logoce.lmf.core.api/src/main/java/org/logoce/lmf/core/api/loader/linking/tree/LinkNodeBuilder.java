package org.logoce.lmf.core.api.loader.linking.tree;

import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.api.loader.linking.LinkException;
import org.logoce.lmf.core.api.loader.linking.ModelGroup;
import org.logoce.lmf.core.loader.internal.linking.TreeToFeatureLinker;
import org.logoce.lmf.core.loader.internal.interpretation.PGroup;
import org.logoce.lmf.core.loader.internal.interpretation.PType;
import org.logoce.lmf.core.api.text.syntax.PNode;
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
		final var resolver = metaResolvers.get(node.group());
		resolver.resolve(node);
	}

	private LinkNodeFull<?, I> buildNode(final BasicTree.BuildInfo<LinkInfo<?, I>, LinkNodeFull<?, I>> buildInfo)
	{
		@SuppressWarnings("unchecked")
		final var data = (LinkInfo<?, I>) buildInfo.data();
		final var resolver = metaResolvers.get(data.modelGroup().group());
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
		final var groupFromParent = parentGroup.features()
											   .stream()
											   .filter(feature -> feature instanceof Relation<?, ?, ?, ?> relation &&
																  relation.name().equals(containmentName))
											   .map(feature -> ((Relation<?, ?, ?, ?>) feature).concept())
											   .findAny()
											   .orElseThrow(() -> buildException(node, containmentName, parentGroup));

		return (ModelGroup<T>) metaGroups.get(groupFromParent.name());
	}

	@SuppressWarnings("unchecked")
	private <T extends LMObject> Relation<T, ?, ?, ?> resolveContainmentRelation(final PGroup<I> node,
																				final Group<?> parentGroup,
																				final Group<T> childGroup)
	{
		final var containmentName = node.type().firstToken();
		final var fromName = resolveFromName(parentGroup, containmentName);
		return (Relation<T, ?, ?, ?>) fromName.or(() -> resolveFromGroup(parentGroup, childGroup))
											   .orElseThrow(() -> buildException(node,
																				 parentGroup,
																				 childGroup.name(),
																				 containmentName));
	}

	private <T extends LMObject> Optional<? extends Relation<?, ?, ?, ?>> resolveFromGroup(final Group<?> parentGroup,
																						   final Group<T> childGroup)
	{
		return metaResolvers.get(parentGroup)
							.streamContainmentRelations()
							.filter(r -> ModelUtil.isSubGroup(r.concept(), childGroup))
							.findAny();
	}

	private Optional<Relation<?, ?, ?, ?>> resolveFromName(final Group<?> parentGroup, final String containmentName)
	{
		return metaResolvers.get(parentGroup)
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
