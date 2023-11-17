package org.logoce.lmf.model.resource.transform;

import org.logoce.lmf.model.api.model.IModelPackage;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.resource.interpretation.LMInterpreter;
import org.logoce.lmf.model.resource.interpretation.PGroup;
import org.logoce.lmf.model.resource.linking.ModelGroup;
import org.logoce.lmf.model.resource.linking.TreeToFeatureLinker;
import org.logoce.lmf.model.resource.linking.exception.LinkException;
import org.logoce.lmf.model.resource.linking.tree.LinkNodeBuilder;
import org.logoce.lmf.model.resource.linking.tree.LinkNodeFull;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.util.ModelRegistry;
import org.logoce.lmf.model.util.tree.BasicTree;
import org.logoce.lmf.model.util.tree.Tree;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PModelBuilder<I extends PNode>
{
	private final LMInterpreter<I> interpreter;
	private final LinkNodeBuilder<I> linker;

	public PModelBuilder()
	{
		final var groups = ModelRegistry.Instance.models()
												 .flatMap(PModelBuilder::modelGroups)
												 .collect(Collectors.toUnmodifiableMap(ModelGroup::name,
																					   Function.identity()));

		final Map<Group<?>, TreeToFeatureLinker> resolvers = groups.values()
																   .stream()
																   .map(ModelGroup::group)
																   .map(TreeToFeatureLinker::new)
																   .collect(Collectors.toUnmodifiableMap(
																		   TreeToFeatureLinker::group,
																		   Function.identity()));

		this.interpreter = new LMInterpreter<>(ModelRegistry.Instance.getAliasMap());
		this.linker = new LinkNodeBuilder<>(groups, resolvers);
	}

	public PModel<I> link(final List<? extends BasicTree<I, ?>> roots)
	{
		return link(roots, (t, e) -> e.printStackTrace());
	}

	@SuppressWarnings("unchecked")
	public PModel<I> link(final List<? extends BasicTree<I, ?>> roots,
						  final BiConsumer<I, LinkException> exceptionConsumer)
	{
		try
		{
			final var linkerTrees = roots.stream().map(this::interpretTree).map(linker::mapTree).toList();
			linkerTrees.stream().flatMap(LinkNodeFull::streamTree).forEach(linker::resolve);
			return new PModel<>(linkerTrees);
		}
		catch (LinkException e)
		{
			exceptionConsumer.accept((I) e.pNode, e);
			return new PModel<>(List.of());
		}
	}

	@SuppressWarnings("unchecked")
	public <NodeType extends BasicTree<I, NodeType>> LinkNode<?, I> linkPartial(final NodeType node,
																				final BiConsumer<LinkException, I> exceptionConsumer)
	{
		try
		{
			final var interpretedNode = node.mapLazy(interpreter::interpret);
			final var linkedNode = linker.mapPartial(interpretedNode);
			linker.resolve(linkedNode);
			return linkedNode;
		}
		catch (LinkException e)
		{
			exceptionConsumer.accept(e, (I) e.pNode);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public <NodeType extends BasicTree<I, NodeType>> LinkNode<?, I> linkPartialUnresolved(final NodeType node,
																						  final BiConsumer<LinkException, I> exceptionConsumer)
	{
		try
		{
			final var interpretedNode = node.mapLazy(interpreter::interpret);
			final var linkedNode = linker.mapPartial(interpretedNode);
			return linkedNode;
		}
		catch (LinkException e)
		{
			exceptionConsumer.accept(e, (I) e.pNode);
			return null;
		}
	}

	public List<? extends LMObject> build(final List<? extends BasicTree<I, ?>> roots)
	{
		return link(roots).build();
	}

	private Tree<PGroup<I>> interpretTree(final BasicTree<I, ?> root)
	{
		return root.mapTree(interpreter::interpretTreeNode);
	}

	private static Stream<ModelGroup<?>> modelGroups(final IModelPackage model)
	{
		return model.model().groups().stream().map(group -> new ModelGroup<>(model, group));
	}
}
