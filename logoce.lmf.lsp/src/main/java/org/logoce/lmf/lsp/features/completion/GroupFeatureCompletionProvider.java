package org.logoce.lmf.lsp.features.completion;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;
import org.logoce.lmf.lsp.state.SemanticSnapshot;
import org.logoce.lmf.lsp.state.SyntaxSnapshot;
import org.logoce.lmf.core.lang.Attribute;
import org.logoce.lmf.core.lang.Concept;
import org.logoce.lmf.core.lang.Feature;
import org.logoce.lmf.core.loader.linking.FeatureResolution;
import org.logoce.lmf.core.loader.linking.LinkNode;
import org.logoce.lmf.core.loader.linking.ResolutionAttempt;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.LMCoreModelDefinition;
import org.logoce.lmf.core.lang.LMCoreModelPackage;
import org.logoce.lmf.core.lang.MetaModel;
import org.logoce.lmf.core.lang.Model;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.resource.parsing.PNode;
import org.logoce.lmf.core.util.ModelRegistry;
import org.logoce.lmf.core.util.ModelUtil;
import org.logoce.lmf.core.util.MetaModelRegistry;
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
							   : fallbackGroupFromSyntax(context);

		if (group == null)
		{
			LOG.debug("LMF LSP completion: no group resolved at position line={}, character={}, metaModel={}",
					  pos.getLine(),
					  pos.getCharacter(),
					  context.metaModel() != null
					  ? context.metaModel().domain() + "." + context.metaModel().name()
					  : "null");
			return List.of();
		}

		final var usedFeatures = findUsedFeaturesAtPosition(semantic, syntax, pos);

		final List<CompletionItem> items = new ArrayList<>();
		items.addAll(buildGroupFeatureCompletions(group, usedFeatures));
		items.addAll(buildContainmentChildCompletions(context, group));

		if (!items.isEmpty())
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
																	 final Set<Feature<?, ?, ?, ?>> usedFeatures)
	{
		final var items = new ArrayList<CompletionItem>();

		LOG.debug("LMF LSP completion: building feature completions for group={} features={} used={}",
				  group.name(),
				  group.features().size(),
				  usedFeatures != null ? usedFeatures.size() : 0);

		final boolean isOperationGroup = "Operation".equals(group.name()) || "OperationParameter".equals(group.name());

		for (final Feature<?, ?, ?, ?> feature : group.features())
		{
			LOG.debug("LMF LSP completion: inspecting feature '{}' (class={}) used={} contains={} isOperationGroup={}",
					  feature.name(),
					  feature.getClass().getSimpleName(),
					  usedFeatures != null && usedFeatures.contains(feature),
					  feature instanceof Relation<?, ?, ?, ?> r && r.contains(),
					  isOperationGroup);

			if (!isOperationGroup && feature instanceof Relation<?, ?, ?, ?> relation && relation.contains())
			{
				// Containment relations are handled separately via dedicated
				// child completion logic.
				continue;
			}

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
				feature instanceof Attribute<?, ?, ?, ?> attr && attr.datatype() == LMCoreModelDefinition.Units.BOOLEAN;

			if (!isBooleanAttribute)
			{
				item.setInsertText(name + "=");
			}

			item.setDetail("Feature of " + group.name());
			items.add(item);
		}

		return items;
	}

	private static Set<Feature<?, ?, ?, ?>> findUsedFeaturesAtPosition(final SemanticSnapshot semantic,
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

		final Set<Feature<?, ?, ?, ?>> used = new HashSet<>();

		for (final ResolutionAttempt<Attribute<?, ?, ?, ?>> attempt : linkNode.attributeResolutions())
		{
			final FeatureResolution<Attribute<?, ?, ?, ?>> resolution = attempt.resolution();
			if (resolution != null)
			{
				final Feature<?, ?, ?, ?> feature = resolution.feature();
				if (feature != null)
				{
					used.add(feature);
				}
			}
		}

		for (final ResolutionAttempt<Relation<?, ?, ?, ?>> attempt : linkNode.relationResolutions())
		{
			final FeatureResolution<Relation<?, ?, ?, ?>> resolution = attempt.resolution();
			if (resolution != null)
			{
				final Feature<?, ?, ?, ?> feature = resolution.feature();
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
		final var items = new ArrayList<CompletionItem>();
		final Set<String> seenLabels = new HashSet<>();

		// 1) Resolve containment relations and their concrete target groups.
		final var containmentRelations = new ArrayList<Relation<?, ?, ?, ?>>();
		for (final Feature<?, ?, ?, ?> feature : group.features())
		{
			if (feature instanceof Relation<?, ?, ?, ?> relation && relation.contains())
			{
				containmentRelations.add(relation);
			}
		}

		if (containmentRelations.isEmpty())
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

		final var searchMetaModels = new ArrayList<MetaModel>();
		if (currentMetaModel != null)
		{
			searchMetaModels.add(currentMetaModel);
			for (final String imp : currentMetaModel.imports())
			{
				final Model imported = registry.getModel(imp);
				if (imported instanceof MetaModel importedMm)
				{
					searchMetaModels.add(importedMm);
				}
			}
		}

		// Always include LMCore groups as baseline candidates (Attribute, Relation, ...).
		searchMetaModels.add(LMCoreModelPackage.MODEL);

		// Map each containment relation to the list of concrete groups that can
		// appear as children for that relation.
		final var relationToConcreteGroups = new java.util.LinkedHashMap<Relation<?, ?, ?, ?>, List<Group<?>>>();

		for (final Relation<?, ?, ?, ?> relation : containmentRelations)
		{
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

			final var concreteGroups = new ArrayList<Group<?>>();
			final Set<String> seenNames = new HashSet<>();

			for (final MetaModel mm : searchMetaModels)
			{
				if (mm == null)
				{
					continue;
				}
				for (final Group<?> candidate : mm.groups())
				{
					final String candidateName = candidate.name();
					if (!candidate.concrete() || candidateName == null || candidateName.isEmpty())
					{
						continue;
					}

					if (!ModelUtil.isSubGroup(conceptGroup, candidate))
					{
						continue;
					}

					if (seenNames.add(candidateName))
					{
						concreteGroups.add(candidate);
					}
				}
			}

			if (!concreteGroups.isEmpty())
			{
				relationToConcreteGroups.put(relation, List.copyOf(concreteGroups));
			}
		}

		if (relationToConcreteGroups.isEmpty())
		{
			return List.of();
		}

		final String detail = "Child of " + group.name();

		// 2) Aliases as before: propose alias-based snippets such as (-att ),
		//    (+att ), etc. for suitable target groups, but enrich the insertion
		//    text with mandatory attributes and non-containment relations of the
		//    target group.
		for (final List<Group<?>> concreteGroups : relationToConcreteGroups.values())
		{
			for (final Group<?> concrete : concreteGroups)
			{
				final String candidateName = concrete.name();
				for (final String aliasName : MetaModelRegistry.Instance.getAliasMap().keySet())
				{
					final String targetGroupName =
						AttributeValueCompletionProvider.resolveGroupNameForHeaderKeyword(aliasName);
					if (candidateName.equals(targetGroupName))
					{
						final String snippet = "(" + aliasName + " )";
						final String insertText = buildGroupHeaderInsertText(aliasName, concrete);
						addSnippetCompletion(snippet, insertText, detail, items, seenLabels);
					}
				}
			}
		}

		// 3) New relation-based candidates: (<feature_name>:<concrete_group_name> )
		//    with insertion depending on the number of concrete groups and the
		//    mandatory features of the target group.
		for (final var entry : relationToConcreteGroups.entrySet())
		{
			final Relation<?, ?, ?, ?> relation = entry.getKey();
			final List<Group<?>> concreteGroups = entry.getValue();

			final String featureName = relation.name();
			if (featureName == null || featureName.isBlank())
			{
				continue;
			}

			for (final Group<?> concrete : concreteGroups)
			{
				final String groupName = concrete.name();
				if (groupName == null || groupName.isBlank())
				{
					continue;
				}

				final boolean multiple = concreteGroups.size() > 1;
				final String headToken = multiple ? groupName : featureName;
				final String label = "(" + featureName + ":" + groupName + " )";
				final String insertText = buildGroupHeaderInsertText(headToken, concrete);

				addSnippetCompletion(label, insertText, detail, items, seenLabels);
			}
		}

		return List.copyOf(items);
	}

	private static Group<?> fallbackGroupFromSyntax(final CompletionContext context)
	{
		final var syntax = context.syntax();
		if (syntax == null)
		{
			return null;
		}

		final var headerInfo = CompletionContextResolver.HeaderInfo.from(syntax, context.position(), context.metaModel());
		if (headerInfo == null)
		{
			return null;
		}
		return headerInfo.headerGroup();
	}

	private static String buildGroupHeaderInsertText(final String headToken,
													 final Group<?> targetGroup)
	{
		final var mandatoryFeatureNames = new ArrayList<String>();

		ModelUtil.streamAllFeatures(targetGroup).forEach(feature -> {
			final String name = feature.name();
			if (name == null || name.isBlank())
			{
				return;
			}

			if (feature instanceof Attribute<?, ?, ?, ?> attribute)
			{
				if (attribute.mandatory())
				{
					mandatoryFeatureNames.add(name + "=");
				}
			}
			else if (feature instanceof Relation<?, ?, ?, ?> relation)
			{
				if (relation.mandatory() && !relation.contains())
				{
					mandatoryFeatureNames.add(name + "=");
				}
			}
		});

		final var sb = new StringBuilder();
		sb.append('(').append(headToken);
		for (final String featureName : mandatoryFeatureNames)
		{
			sb.append(' ').append(featureName);
		}
		sb.append(" )");
		return sb.toString();
	}

	private static void addSnippetCompletion(final String label,
											 final String insertText,
											 final String detail,
											 final List<CompletionItem> items,
											 final Set<String> seenLabels)
	{
		if (!seenLabels.add(label))
		{
			return;
		}

		final var item = new CompletionItem(label);
		item.setInsertText(insertText);
		item.setDetail(detail);
		items.add(item);
	}
}
