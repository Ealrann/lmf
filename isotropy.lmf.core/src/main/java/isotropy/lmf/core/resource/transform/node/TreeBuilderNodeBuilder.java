package isotropy.lmf.core.resource.transform.node;

import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.lang.Relation;
import isotropy.lmf.core.resource.transform.word.TreeToFeatureResolver;
import isotropy.lmf.core.resource.util.Tree;
import isotropy.lmf.core.util.ModelUtils;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class TreeBuilderNodeBuilder
{
	private final NamedNode.Builder namedNodeBuilder;

	private final Map<String, ModelGroup<?>> groups;

	private final Map<Group<?>, TreeToFeatureResolver> resolvers;

	public TreeBuilderNodeBuilder(final NamedNode.Builder namedNodeBuilder,
								  final Map<String, ModelGroup<?>> groups,
								  final Map<Group<?>, TreeToFeatureResolver> resolvers)
	{
		this.namedNodeBuilder = namedNodeBuilder;
		this.groups = groups;
		this.resolvers = resolvers;
	}

	public <T extends LMObject> TreeBuilderNode<T> mapTree(final Tree<List<String>> node)
	{
		return node.map(this::buildNodeInfo, TreeBuilderNode<T>::new);
	}

	private <T extends LMObject> BuilderNodeInfo<T> buildNodeInfo(final Tree<List<String>> node)
	{
		final var namedNode = namedNodeBuilder.build(node.data());
		final var modelGroup = this.<T>findModelGroup(namedNode);
		final var parent = node.parent();
		if (parent != null && parent.parent() != null)
		{
			final var parentNamedNode = namedNodeBuilder.build(parent.data());
			final var parentModelGroup = findModelGroup(parentNamedNode);
			final var parentGroup = parentModelGroup.group();

			return buildNodeInfoWithParent(namedNode, parentGroup, modelGroup);
		}
		else
		{
			return new BuilderNodeInfo<>(null, namedNode.words(), modelGroup);
		}
	}

	private <T extends LMObject> BuilderNodeInfo<T> buildNodeInfoWithParent(NamedNode namedNode,
																			Group<?> parentGroup,
																			ModelGroup<T> modelGroup)
	{
		final var effectiveGroup = modelGroup != null
								   ? modelGroup
								   : this.<T>findModelGroupFromParent(namedNode, parentGroup);
		final var name = namedNode.name();
		final var equalIndex = name.indexOf('=');
		final var containmentName = equalIndex == -1 ? null : name.substring(0, equalIndex);
		final var resolvedRelation = resolveContainmentRelation(containmentName, parentGroup, effectiveGroup.group());
		return new BuilderNodeInfo<>(resolvedRelation, namedNode.words(), effectiveGroup);
	}

	@SuppressWarnings("unchecked")
	private <T extends LMObject> ModelGroup<T> findModelGroup(final NamedNode namedNode)
	{
		final var name = namedNode.name();
		final var equalIndex = name.indexOf('=');
		final var groupName = equalIndex == -1 ? name : name.substring(equalIndex + 1);
		final var modelGroup = groups.get(groupName);
		return (ModelGroup<T>) modelGroup;
	}

	@SuppressWarnings("unchecked")
	private <T extends LMObject> ModelGroup<T> findModelGroupFromParent(final NamedNode namedNode,
																		final Group<?> parentGroup)
	{
		final var groupFromParent = parentGroup.features()
											   .stream()
											   .filter(Relation.class::isInstance)
											   .map(Relation.class::cast)
											   .filter(r -> r.name()
															 .equals(namedNode.name()))
											   .map(r -> r.groupReference()
														  .group())
											   .findAny()
											   .orElseThrow(() -> new NoSuchElementException("Cannot resolve " +
																							 "containance of " +
																							 namedNode.name()));

		return (ModelGroup<T>) groups.get(groupFromParent.name());
	}

	@SuppressWarnings("unchecked")
	private <T extends LMObject> Relation<T, ?> resolveContainmentRelation(String containmentName,
																		   Group<?> parentGroup,
																		   Group<T> childGroup)
	{
		final var preciseChildren = resolvers.get(parentGroup)
											 .streamContainmentRelations()
											 .filter(f -> f.name()
														   .equals(containmentName))
											 .findAny();

		if (preciseChildren.isPresent())
		{
			return (Relation<T, ?>) preciseChildren.get();
		}
		else
		{
			return (Relation<T, ?>) resolvers.get(parentGroup)
											 .streamContainmentRelations()
											 .filter(r -> ModelUtils.isSubGroup(r.groupReference()
																				 .group(), childGroup))
											 .findAny()
											 .orElseThrow(() -> new NoSuchElementException("Cannot find containment " +
																						   "relation from parent " +
																						   parentGroup.name() +
																						   " to child " +
																						   childGroup.name()));
		}
	}
}
