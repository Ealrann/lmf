package org.logoce.lmf.lsp.features.completion;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;
import org.logoce.lmf.lsp.state.SemanticSnapshot;
import org.logoce.lmf.lsp.state.SyntaxSnapshot;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Concept;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.loader.linking.FeatureResolution;
import org.logoce.lmf.model.loader.linking.LinkNode;
import org.logoce.lmf.model.loader.linking.ResolutionAttempt;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMCoreDefinition;
import org.logoce.lmf.model.lang.LMCorePackage;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.util.ModelRegistry;
import org.logoce.lmf.model.util.ModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

final class GroupFeatureCompletionProvider
{
	private static final Logger LOG = LoggerFactory.getLogger(GroupFeatureCompletionProvider.class);

	private GroupFeatureCompletionProvider()
	{
	}

	static List<CompletionItem> complete(final CompletionContext context)
	{
		final SyntaxSnapshot syntax = context.syntax();
		final Position pos = context.position();

		if (syntax == null)
		{
			return List.of();
		}

		final SemanticSnapshot semantic = context.semantic();
		final Group<?> semanticGroup = semantic != null
									   ? SemanticNavigation.findGroupAtPosition(semantic, syntax, pos)
									   : null;
		final Group<?> group = semanticGroup != null
							   ? semanticGroup
							   : fallbackGroupFromSyntax(syntax, pos);

		if (group == null)
		{
			LOG.info("LMF LSP completion: no group resolved at position line={}, character={}",
					 pos.getLine(), pos.getCharacter());
			return List.of();
		}

		final var usedFeatures = findUsedFeaturesAtPosition(semantic, syntax, pos);

		final List<CompletionItem> items = new ArrayList<>();
		items.addAll(buildGroupFeatureCompletions(group, usedFeatures));
		items.addAll(buildContainmentChildCompletions(context, group));

		if (!items.isEmpty() && LOG.isDebugEnabled())
		{
			final var labels = items.stream()
									.map(CompletionItem::getLabel)
									.toList();
			LOG.debug("LMF LSP completion: group feature completions, group={}, items={}, labels={}",
					  group.name(), items.size(), labels);
		}
		return items;
	}

	private static List<CompletionItem> buildGroupFeatureCompletions(final Group<?> group,
																	 final Set<Feature<?, ?>> usedFeatures)
	{
		final var items = new ArrayList<CompletionItem>();

		for (final Feature<?, ?> feature : group.features())
		{
			final String name = feature.name();
			if (name == null || name.isEmpty())
			{
				continue;
			}

			if (usedFeatures != null && usedFeatures.contains(feature))
			{
				continue;
			}

			final var item = new CompletionItem(name);

			final boolean isBooleanAttribute =
				feature instanceof Attribute<?, ?> attr && attr.datatype() == LMCoreDefinition.Units.BOOLEAN;

			if (!isBooleanAttribute)
			{
				item.setInsertText(name + "=");
			}

			item.setDetail("Feature of " + group.name());
			items.add(item);
		}

		return items;
	}

	private static Set<Feature<?, ?>> findUsedFeaturesAtPosition(final SemanticSnapshot semantic,
																 final SyntaxSnapshot syntax,
																 final Position pos)
	{
		if (semantic == null || syntax == null)
		{
			return Set.of();
		}

		final LinkNode<?, PNode> linkNode = LinkTreeNavigation.findLinkNodeAtOrBeforePosition(semantic, syntax, pos);
		if (linkNode == null)
		{
			return Set.of();
		}

		final Set<Feature<?, ?>> used = new HashSet<>();

		for (final ResolutionAttempt<Attribute<?, ?>> attempt : linkNode.attributeResolutions())
		{
			final FeatureResolution<Attribute<?, ?>> resolution = attempt.resolution();
			if (resolution != null)
			{
				final Feature<?, ?> feature = resolution.feature();
				if (feature != null)
				{
					used.add(feature);
				}
			}
		}

		for (final ResolutionAttempt<Relation<?, ?>> attempt : linkNode.relationResolutions())
		{
			final FeatureResolution<Relation<?, ?>> resolution = attempt.resolution();
			if (resolution != null)
			{
				final Feature<?, ?> feature = resolution.feature();
				if (feature != null)
				{
					used.add(feature);
				}
			}
		}

		return used;
	}

	private static List<CompletionItem> buildContainmentChildCompletions(final CompletionContext context,
																		 final Group<?> group)
	{
		final var conceptGroups = new ArrayList<Group<?>>();

		for (final Feature<?, ?> feature : group.features())
		{
			if (!(feature instanceof Relation<?, ?> relation))
			{
				continue;
			}

			if (!relation.contains())
			{
				continue;
			}

			final Concept<?> concept = relation.concept();
			if (concept == null)
			{
				continue;
			}

			final Group<?> conceptGroup =
				(concept instanceof Group<?> g) ? g : concept.lmGroup();
			if (conceptGroup == null)
			{
				continue;
			}

			conceptGroups.add(conceptGroup);
		}

		if (conceptGroups.isEmpty())
		{
			return List.of();
		}

		if (context.server() == null)
		{
			return List.of();
		}

		final var server = context.server();
		final ModelRegistry registry = server.workspaceIndex().modelRegistry();
		final MetaModel currentMetaModel = context.metaModel();

		final Set<String> seenCandidateNames = new HashSet<>();
		final var candidates = new ArrayList<Group<?>>();

		final java.util.function.Consumer<MetaModel> collectFromMetaModel = mm -> {
			if (mm == null)
			{
				return;
			}
			for (final Group<?> candidate : mm.groups())
			{
				if (!candidate.concrete())
				{
					continue;
				}

				for (final Group<?> conceptGroup : conceptGroups)
				{
					if (ModelUtils.isSubGroup(conceptGroup, candidate))
					{
						if (seenCandidateNames.add(candidate.name()))
						{
							candidates.add(candidate);
						}
						break;
					}
				}
			}
		};

		if (currentMetaModel != null)
		{
			collectFromMetaModel.accept(currentMetaModel);
			for (final String imp : currentMetaModel.imports())
			{
				final Model imported = registry.getModel(imp);
				if (imported instanceof MetaModel importedMm)
				{
					collectFromMetaModel.accept(importedMm);
				}
			}
		}

		// Always include LMCore groups as baseline candidates (Attribute, Relation, ...).
		collectFromMetaModel.accept(LMCorePackage.MODEL);

		if (candidates.isEmpty())
		{
			return List.of();
		}

		final var items = new ArrayList<CompletionItem>();
		for (final Group<?> candidate : candidates)
		{
			final String name = candidate.name();
			if (name == null || name.isEmpty())
			{
				continue;
			}

			final String snippet = "(" + name + " )";
			final var item = new CompletionItem(snippet);
			item.setInsertText(snippet);
			item.setDetail("Child of " + group.name());
			items.add(item);
		}

		return items;
	}

	private static Group<?> fallbackGroupFromSyntax(final SyntaxSnapshot syntax, final Position pos)
	{
		final var headerInfo = CompletionContextResolver.HeaderInfo.from(syntax, pos);
		if (headerInfo == null)
		{
			return null;
		}
		return headerInfo.lmCoreGroup();
	}
}
