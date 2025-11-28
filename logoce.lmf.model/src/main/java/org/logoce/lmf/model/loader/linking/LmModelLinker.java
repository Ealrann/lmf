package org.logoce.lmf.model.loader.linking;

import org.logoce.lmf.model.api.model.IModelPackage;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMCorePackage;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.model.loader.linking.tree.LinkNodeBuilder;
import org.logoce.lmf.model.loader.linking.tree.LinkNodeFull;
import org.logoce.lmf.model.resource.interpretation.LMInterpreter;
import org.logoce.lmf.model.resource.interpretation.PGroup;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.util.Functional;
import org.logoce.lmf.model.util.MetaModelRegistry;
import org.logoce.lmf.model.util.ModelRegistry;
import org.logoce.lmf.model.util.TextPositions;
import org.logoce.lmf.model.util.tree.BasicTree;
import org.logoce.lmf.model.util.tree.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class LmModelLinker<I extends PNode>
{
	private final LMInterpreter<I> interpreter;
	private final LinkNodeBuilder<I> linker;

	public LmModelLinker(final ModelRegistry modelRegistry)
	{
		final var metaModels = List.of(LMCorePackage.Instance);
		final var metaGroups = collectGroups(metaModels);
		final var metaResolvers = buildResolvers(metaGroups, modelRegistry);

		this.interpreter = new LMInterpreter<>(MetaModelRegistry.Instance.getAliasMap());
		this.linker = new LinkNodeBuilder<>(metaGroups, metaResolvers);
	}

	/**
	 * Link the given roots, collecting diagnostics and swallowing link errors.
	 * Intended for tooling-oriented flows where full diagnostics are preferred over exceptions.
	 */
	public LinkResult<I> linkModel(final List<? extends BasicTree<I, ?>> roots,
								   final List<LmDiagnostic> diagnostics,
								   final CharSequence source)
	{
		if (roots.isEmpty())
		{
			return new LinkResult<>(null, List.of(), List.of());
		}

		try
		{
			final var result = linkModelInternal(roots);

			if (result.model() == null && !result.roots().isEmpty())
			{
				final var span = TextPositions.spanOf(roots.getFirst().data(), source);
				diagnostics.add(new LmDiagnostic(span.line(),
												 span.column(),
												 span.length(),
												 span.offset(),
												 LmDiagnostic.Severity.ERROR,
												 "Root element is not a Model; use loadObject() for generic roots"));
			}

			return result;
		}
		catch (LinkException e)
		{
			final var span = TextPositions.spanOf(e.pNode(), source);
			diagnostics.add(new LmDiagnostic(span.line(),
											 span.column(),
											 span.length(),
											 span.offset(),
											 LmDiagnostic.Severity.ERROR,
											 e.getMessage() == null ? "Link error" : e.getMessage()));
			return new LinkResult<>(null, List.of(), List.of());
		}
		catch (Exception e)
		{
			diagnostics.add(new LmDiagnostic(1,
											 1,
											 1,
											 0,
											 LmDiagnostic.Severity.ERROR,
											 e.getMessage() == null ? "Link error" : e.getMessage()));
			return new LinkResult<>(null, List.of(), List.of());
		}
	}

	/**
	 * Link the given roots and build objects, propagating any {@link LinkException}.
	 * Useful for legacy flows that expect linking failures to be signalled via exceptions.
	 */
	public LinkResult<I> linkModelStrict(final List<? extends BasicTree<I, ?>> roots)
	{
		if (roots.isEmpty())
		{
			return new LinkResult<>(null, List.of(), List.of());
		}
		return linkModelInternal(roots);
	}

	private LinkResult<I> linkModelInternal(final List<? extends BasicTree<I, ?>> roots)
	{
		final var linkerTrees = roots.stream()
									 .map(this::interpretTree)
									 .map(linker::mapTree)
									 .toList();

		linkerTrees.stream().flatMap(LinkNodeFull::streamTree).forEach(linker::resolve);

		final var builtObjects = new ArrayList<LMObject>();
		for (final var node : linkerTrees)
		{
			builtObjects.add(node.build());
		}

		final var model = builtObjects.stream()
									  .filter(o -> o instanceof Model)
									  .map(o -> (Model) o)
									  .findFirst()
									  .orElse(null);

		return new LinkResult<>(model, List.copyOf(linkerTrees), List.copyOf(builtObjects));
	}

	private Tree<PGroup<I>> interpretTree(final BasicTree<I, ?> root)
	{
		return root.mapTree(interpreter::interpretTreeNode);
	}

	private static Map<Group<?>, TreeToFeatureLinker> buildResolvers(final Map<String, ModelGroup<?>> metaGroups,
																	 final ModelRegistry modelRegistry)
	{
		final var linkerBuilder = Functional.inject(modelRegistry, TreeToFeatureLinker::new);

		return metaGroups.values()
						 .stream()
						 .map(ModelGroup::group)
						 .map(linkerBuilder)
						 .collect(Collectors.toUnmodifiableMap(TreeToFeatureLinker::group, Function.identity()));
	}

	private static Stream<ModelGroup<?>> modelGroups(final IModelPackage model)
	{
		return model.model()
					.groups()
					.stream()
					.map(group -> new ModelGroup<>(model, group));
	}

	private static Map<String, ModelGroup<?>> collectGroups(final List<LMCorePackage> metaModels)
	{
		return metaModels.stream()
						 .flatMap(LmModelLinker::modelGroups)
						 .collect(Collectors.toUnmodifiableMap(ModelGroup::name, Function.identity()));
	}

	public record LinkResult<I extends PNode>(Model model,
											  List<? extends LinkNode<?, I>> trees,
											  List<? extends LMObject> roots)
	{
	}
}
