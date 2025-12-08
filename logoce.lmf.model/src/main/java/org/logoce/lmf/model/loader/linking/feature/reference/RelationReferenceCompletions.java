package org.logoce.lmf.model.loader.linking.feature.reference;

import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMCoreModelDefinition;
import org.logoce.lmf.model.lang.LMCoreModelPackage;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.loader.linking.LinkNode;
import org.logoce.lmf.model.loader.linking.ResolutionAttempt;
import org.logoce.lmf.model.loader.linking.feature.AttributeResolver;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.loader.parsing.ModelHeaderUtil;
import org.logoce.lmf.model.util.ModelRegistry;
import org.logoce.lmf.model.util.ModelUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Helper for relation-valued reference completions built on top of the
 * linker/link-tree infrastructure.
 * <p>
 * This is intentionally LSP-agnostic: it exposes language-level candidates
 * (labels and details) that UIs can translate into protocol-specific
 * completion items.
 */
public final class RelationReferenceCompletions
{
	private RelationReferenceCompletions()
	{
	}

	public record RelationCandidate(String label, String detail, boolean local)
	{
	}

	/**
	 * Collect reference candidates for a relation-valued feature based on the
	 * owning model, its link trees (when available), and the workspace model
	 * registry.
	 * <p>
	 * Local candidates are derived from the link trees when possible so that
	 * partially linked models still contribute; imported models and LMCore are
	 * scanned from their built {@link Model} instances.
	 */
	public static List<RelationCandidate> collectRelationCandidates(final Model owningModel,
																	final List<? extends LinkNode<?, PNode>> linkTrees,
																	final Relation<?, ?> relation,
																	final ModelRegistry registry)
	{
		if (relation == null)
		{
			return List.of();
		}

		if (!(relation.concept() instanceof Group<?> conceptGroup))
		{
			return List.of();
		}

		final var result = new ArrayList<RelationCandidate>();
		final Set<String> seenLabels = new HashSet<>();

		// 1) Local objects from the current document: prefer link trees so we
		// can surface candidates even when the owning model failed to build.
		if (linkTrees != null && !linkTrees.isEmpty())
		{
			collectFromLinkTrees(owningModel, linkTrees, conceptGroup, seenLabels, result);
		}
		else if (owningModel != null)
		{
			collectFromModel(owningModel, owningModel, conceptGroup, seenLabels, result, true);

			// 2) Imported models from the owning model's header.
			for (final String imp : owningModel.imports())
			{
				final var imported = registry.getModel(imp);
				if (imported == null)
				{
					continue;
				}
				collectFromModel(imported, owningModel, conceptGroup, seenLabels, result, false);
			}
		}

		// 2) Meta-models declared in the header (metamodels=...), resolved via the registry.
		collectFromMetamodelsInHeader(linkTrees, registry, conceptGroup, seenLabels, result);

		// 3) LMCore is implicitly available as a cross-model source.
		collectFromModel(LMCoreModelPackage.MODEL, owningModel, conceptGroup, seenLabels, result, false);

		return List.copyOf(result);
	}

	private static void collectFromLinkTrees(final Model owningModel,
											 final List<? extends LinkNode<?, PNode>> linkTrees,
											 final Group<?> conceptGroup,
											 final Set<String> seenLabels,
											 final List<RelationCandidate> out)
	{
		final String qualified = owningModel != null
								 ? qualifiedName(owningModel)
								 : qualifiedNameFromLinkTrees(linkTrees);

		for (final LinkNode<?, PNode> root : linkTrees)
		{
			if (!(root instanceof org.logoce.lmf.model.loader.linking.tree.LinkNodeFull<?, PNode> fullRoot))
			{
				continue;
			}

			fullRoot.streamTree().forEach(node -> {
				final Group<?> group = node.group();
				if (!ModelUtil.isSubGroup(conceptGroup, group))
				{
					return;
				}

				final String name = resolveNameFromLinkNode(node);
				if (name == null || name.isBlank())
				{
					return;
				}

				final String label = "@" + name;
				if (!seenLabels.add(label))
				{
					return;
				}

				final String detail = group.name() + " in " + qualified;
				out.add(new RelationCandidate(label, detail, true));
			});
		}
	}

	private static void collectFromMetamodelsInHeader(final List<? extends LinkNode<?, PNode>> linkTrees,
													  final ModelRegistry registry,
													  final Group<?> conceptGroup,
													  final Set<String> seenLabels,
													  final List<RelationCandidate> out)
	{
		if (linkTrees == null || linkTrees.isEmpty())
		{
			return;
		}

		final LinkNode<?, PNode> root = linkTrees.getFirst();
		final PNode pNode = root.pNode();
		final var metamodelNames = ModelHeaderUtil.resolveMetamodelNames(pNode);

		for (final String name : metamodelNames)
		{
			final Model model = registry.getModel(name);
			if (model instanceof MetaModel mm)
			{
				collectFromMetaModel(mm, conceptGroup, seenLabels, out);
			}
		}
	}

	private static void collectFromMetaModel(final MetaModel metaModel,
											 final Group<?> conceptGroup,
											 final Set<String> seenLabels,
											 final List<RelationCandidate> out)
	{
		final String qualified = qualifiedName(metaModel);
		final String alias = Objects.toString(metaModel.name(), "");

		for (final Group<?> group : metaModel.groups())
		{
			if (!ModelUtil.isSubGroup(conceptGroup, group))
			{
				continue;
			}

			final String name = group.name();
			if (name == null || name.isBlank())
			{
				continue;
			}

			final String label = "#" + alias + "@" + name;
			if (!seenLabels.add(label))
			{
				continue;
			}

			final String detail = group.name() + " in " + qualified;
			out.add(new RelationCandidate(label, detail, false));
		}
	}

	private static String resolveNameFromLinkNode(
		final org.logoce.lmf.model.loader.linking.tree.LinkNodeFull<?, PNode> node)
	{
		for (final ResolutionAttempt<org.logoce.lmf.model.lang.Attribute<?, ?>> attempt : node.attributeResolutions())
		{
			final var resolution = attempt.resolution();
			if (resolution instanceof AttributeResolver.AttributeResolution<?> attrResolution &&
				attrResolution.feature() == LMCoreModelDefinition.Features.NAMED.NAME)
			{
				return attrResolution.value();
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static void collectFromModel(final Model model,
										 final Model owningModel,
										 final Group<?> conceptGroup,
										 final Set<String> seenLabels,
										 final List<RelationCandidate> out,
										 final boolean localFlag)
	{
		if (!(model instanceof LMObject root))
		{
			return;
		}

		final String qualified = qualifiedName(model);
		final String alias = model.name();

		for (final LMObject object : (Iterable<LMObject>) ModelUtil.streamTree(root)::iterator)
		{
			final Group<?> group = object.lmGroup();
			if (!ModelUtil.isSubGroup(conceptGroup, group))
			{
				continue;
			}

			final Object value;
			try
			{
				value = object.get(LMCoreModelDefinition.Features.NAMED.NAME);
			}
			catch (Exception e)
			{
				continue;
			}

			if (value == null)
			{
				continue;
			}

			final String name = value.toString();
			if (name == null || name.isBlank())
			{
				continue;
			}

			final boolean isLocal = localFlag && model == owningModel;
			final String label = isLocal ? "@" + name : "#" + alias + "@" + name;

			if (!seenLabels.add(label))
			{
				continue;
			}

			final String detail = group.name() + " in " + qualified;
			out.add(new RelationCandidate(label, detail, isLocal));
		}
	}

	private static String qualifiedName(final Model model)
	{
		if (model instanceof MetaModel mm)
		{
			final String domain = Objects.toString(mm.domain(), "");
			final String name = Objects.toString(mm.name(), "");
			return domain.isBlank() ? name : domain + "." + name;
		}

		return Objects.toString(model.name(), "");
	}

	private static String qualifiedNameFromLinkTrees(final List<? extends LinkNode<?, PNode>> linkTrees)
	{
		if (linkTrees == null || linkTrees.isEmpty())
		{
			return "";
		}

		final LinkNode<?, PNode> root = linkTrees.getFirst();
		final PNode pNode = root.pNode();

		final String domain = ModelHeaderUtil.resolveDomain(pNode);
		final String name = safeModelName(pNode);

		if (name == null || name.isBlank())
		{
			return "";
		}

		return (domain == null || domain.isBlank()) ? name : domain + "." + name;
	}

	private static String safeModelName(final PNode pNode)
	{
		try
		{
			return ModelHeaderUtil.resolveName(pNode);
		}
		catch (Exception e)
		{
			return null;
		}
	}
}
