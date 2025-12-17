package org.logoce.lmf.core.loader.api.loader.linking;

import org.logoce.lmf.core.api.model.IModelPackage;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.LMCoreModelPackage;
import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.Model;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.loader.linking.TreeToFeatureLinker;
import org.logoce.lmf.core.loader.api.loader.linking.tree.LinkNodeBuilder;
import org.logoce.lmf.core.loader.api.loader.linking.tree.LinkNodeFull;
import org.logoce.lmf.core.loader.interpretation.LMInterpreter;
import org.logoce.lmf.core.loader.interpretation.PGroup;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;
import org.logoce.lmf.core.loader.api.loader.util.Functional;
import org.logoce.lmf.core.api.model.MetaModelRegistry;
import org.logoce.lmf.core.api.model.ModelRegistry;
import org.logoce.lmf.core.loader.api.loader.util.TextPositions;
import org.logoce.lmf.core.util.tree.BasicTree;
import org.logoce.lmf.core.util.tree.Tree;

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
		this(modelRegistry, List.of(LMCoreModelPackage.Instance));
	}

	public LmModelLinker(final ModelRegistry modelRegistry,
						 final List<? extends IModelPackage> metaModelPackages)
	{
		final var metaGroups = collectGroups(metaModelPackages);
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

		final var result = linkModelTolerant(roots, diagnostics, source);

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

	private LinkResult<I> linkModelTolerant(final List<? extends BasicTree<I, ?>> roots,
											final List<LmDiagnostic> diagnostics,
											final CharSequence source)
	{
		final var linkerTrees = roots.stream()
									 .map(this::interpretTree)
									 .map(linker::mapTree)
									 .toList();

		linkerTrees.stream().flatMap(LinkNodeFull::streamTree).forEach(linker::resolve);

		final var builtObjects = new ArrayList<LMObject>();
		for (final var node : linkerTrees)
		{
			try
			{
				builtObjects.add(node.build());
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
			}
			catch (Exception e)
			{
				diagnostics.add(new LmDiagnostic(1,
												 1,
												 1,
												 0,
												 LmDiagnostic.Severity.ERROR,
												 e.getMessage() == null ? "Link error" : e.getMessage()));
			}
		}

		final var model = builtObjects.stream()
									  .filter(o -> o instanceof Model)
									  .map(o -> (Model) o)
									  .findFirst()
									  .orElse(null);

		return new LinkResult<>(model, List.copyOf(linkerTrees), List.copyOf(builtObjects));
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

	private static Map<String, ModelGroup<?>> collectGroups(final List<? extends IModelPackage> metaModels)
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
