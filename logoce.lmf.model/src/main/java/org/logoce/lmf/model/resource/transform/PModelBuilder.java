package org.logoce.lmf.model.resource.transform;

import org.logoce.lmf.model.api.model.IModelPackage;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.resource.interpretation.LMInterpreter;
import org.logoce.lmf.model.resource.interpretation.PGroup;
import org.logoce.lmf.model.resource.linking.ModelGroup;
import org.logoce.lmf.model.resource.linking.TreeToFeatureLinker;
import org.logoce.lmf.model.resource.linking.exception.LinkException;
import org.logoce.lmf.model.resource.linking.tree.LinkNode;
import org.logoce.lmf.model.resource.linking.tree.LinkNodeBuilder;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.util.ModelRegistry;
import org.logoce.lmf.model.util.tree.NavigableDataTree;
import org.logoce.lmf.model.util.tree.Tree;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PModelBuilder<I extends PNode>
{
	private final Map<Group<?>, TreeToFeatureLinker> resolvers;

	private final LinkNodeBuilder<I> linker;
	private final LMInterpreter<I> interpreter;

	public PModelBuilder()
	{
		this((t, e) -> e.printStackTrace());
	}

	public PModelBuilder(final BiConsumer<I, LinkException> exceptionManager)
	{
		final var groups = ModelRegistry.Instance.models()
												 .flatMap(PModelBuilder::modelGroups)
												 .collect(Collectors.toUnmodifiableMap(ModelGroup::name,
																					   Function.identity()));

		resolvers = groups.values()
						  .stream()
						  .map(ModelGroup::group)
						  .map(TreeToFeatureLinker::new)
						  .collect(Collectors.toUnmodifiableMap(TreeToFeatureLinker::group, Function.identity()));

		interpreter = new LMInterpreter<>(ModelRegistry.Instance.getAliasMap());

		linker = new LinkNodeBuilder<>(groups, resolvers, exceptionManager);
	}

	public PModel<I> link(final List<? extends NavigableDataTree<I, ?>> roots)
	{
		final var linkerTrees = roots.stream().map(this::interpretTree).map(linker::mapTree).toList();
		linkerTrees.stream()
				   .flatMap(LinkNode::streamTree)
				   .map(LinkNode::linkStructure)
				   .filter(Objects::nonNull)
				   .forEach(this::linkNode);
		return new PModel<>(linkerTrees);
	}

	public List<? extends LMObject> build(final List<? extends NavigableDataTree<I, ?>> roots)
	{
		return link(roots).build();
	}

	private Tree<PGroup<I>> interpretTree(final NavigableDataTree<I, ?> root)
	{
		return root.map(interpreter::parseTreeNode, Tree::new);
	}

	private void linkNode(final LinkNode.Structure<?> node)
	{
		final var resolver = resolvers.get(node.group());
		resolver.resolve(node);
	}

	private static Stream<ModelGroup<?>> modelGroups(final IModelPackage model)
	{
		return model.model().groups().stream().map(group -> new ModelGroup<>(model, group));
	}
}
