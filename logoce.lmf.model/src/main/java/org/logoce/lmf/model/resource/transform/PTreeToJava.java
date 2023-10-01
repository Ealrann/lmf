package org.logoce.lmf.model.resource.transform;

import org.logoce.lmf.model.api.model.IModelPackage;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.lang.Named;
import org.logoce.lmf.model.resource.ptree.PToken;
import org.logoce.lmf.model.resource.transform.node.*;
import org.logoce.lmf.model.resource.transform.parsing.NodeParser;
import org.logoce.lmf.model.resource.transform.word.TreeToFeatureResolver;
import org.logoce.lmf.model.util.Tree;
import org.logoce.lmf.model.util.ModelRegistry;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PTreeToJava
{
	private final Map<Group<?>, TreeToFeatureResolver> resolvers;

	private final TreeBuilderNodeBuilder nodeMapper;

	public PTreeToJava()
	{
		final var aliases = ModelRegistry.Instance.models()
												  .map(IModelPackage::model)
												  .map(Model::aliases)
												  .flatMap(Collection::stream)
												  .collect(Collectors.toUnmodifiableMap(Named::name,
																						Function.identity()));

		final var nodeParser = new NodeParser(aliases);

		final var groups = ModelRegistry.Instance.models()
												 .flatMap(PTreeToJava::modelGroups)
												 .collect(Collectors.toUnmodifiableMap(ModelGroup::name,
																					   Function.identity()));

		resolvers = groups.values()
						  .stream()
						  .map(ModelGroup::group)
						  .map(TreeToFeatureResolver::new)
						  .collect(Collectors.toUnmodifiableMap(TreeToFeatureResolver::group, Function.identity()));

		nodeMapper = new TreeBuilderNodeBuilder(nodeParser, groups, resolvers);
	}

	public List<? extends LMObject> transform(final Tree<List<PToken>> tree)
	{
		final var builderTrees = tree.children()
									 .stream()
									 .map(nodeMapper::mapTree)
									 .toList();

		builderTrees.stream()
					.flatMap(TreeBuilderNode::stream)
					.forEach(this::resolve);

		return builderTrees.stream()
						   .map(TreeBuilderNode::build)
						   .toList();
	}

	private void resolve(TreeBuilderNode<?> node)
	{
		final var resolver = resolvers.get(node.data()
											   .modelGroup()
											   .group());
		resolver.resolve(node);
	}

	private static Stream<ModelGroup<?>> modelGroups(final IModelPackage model)
	{
		return model.model()
					.groups()
					.stream()
					.map(group -> new ModelGroup<>(model, group));
	}

}
