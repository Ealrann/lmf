package org.logoce.lmf.model.loader.linking.feature.reference;

import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMCoreDefinition;
import org.logoce.lmf.model.lang.LMCorePackage;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.loader.linking.LinkNode;
import org.logoce.lmf.model.loader.linking.ResolutionAttempt;
import org.logoce.lmf.model.loader.linking.feature.AttributeResolver;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.util.ModelRegistry;
import org.logoce.lmf.model.util.ModelUtils;

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

		if (owningModel != null)
		{
			// 1) Local objects from the current document: prefer link trees so we
			// can surface candidates even when the owning model failed to build.
			if (linkTrees != null && !linkTrees.isEmpty())
			{
				collectFromLinkTrees(owningModel, linkTrees, conceptGroup, seenLabels, result);
			}
			else
			{
				collectFromModel(owningModel, owningModel, conceptGroup, seenLabels, result, true);
			}

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

		// 3) LMCore is implicitly available as a cross-model source.
		collectFromModel(LMCorePackage.MODEL, owningModel, conceptGroup, seenLabels, result, false);

		return List.copyOf(result);
	}

	private static void collectFromLinkTrees(final Model owningModel,
											 final List<? extends LinkNode<?, PNode>> linkTrees,
											 final Group<?> conceptGroup,
											 final Set<String> seenLabels,
											 final List<RelationCandidate> out)
	{
		final String qualified = qualifiedName(owningModel);

		for (final LinkNode<?, PNode> root : linkTrees)
		{
			if (!(root instanceof org.logoce.lmf.model.loader.linking.tree.LinkNodeFull<?, PNode> fullRoot))
			{
				continue;
			}

			fullRoot.streamTree().forEach(node -> {
				final Group<?> group = node.group();
				if (!ModelUtils.isSubGroup(conceptGroup, group))
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

	private static String resolveNameFromLinkNode(
		final org.logoce.lmf.model.loader.linking.tree.LinkNodeFull<?, PNode> node)
	{
		for (final ResolutionAttempt<org.logoce.lmf.model.lang.Attribute<?, ?>> attempt : node.attributeResolutions())
		{
			final var resolution = attempt.resolution();
			if (resolution instanceof AttributeResolver.AttributeResolution<?> attrResolution &&
				attrResolution.feature() == LMCoreDefinition.Features.NAMED.NAME)
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

		for (final LMObject object : (Iterable<LMObject>) ModelUtils.streamTree(root)::iterator)
		{
			final Group<?> group = object.lmGroup();
			if (!ModelUtils.isSubGroup(conceptGroup, group))
			{
				continue;
			}

			final Object value;
			try
			{
				value = object.get(LMCoreDefinition.Features.NAMED.NAME);
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
}
