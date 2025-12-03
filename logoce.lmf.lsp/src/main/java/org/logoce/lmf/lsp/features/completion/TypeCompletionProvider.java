package org.logoce.lmf.lsp.features.completion;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;
import org.logoce.lmf.lsp.LmLanguageServer;
import org.logoce.lmf.lsp.state.SyntaxSnapshot;
import org.logoce.lmf.model.lang.Alias;
import org.logoce.lmf.model.lang.JavaWrapper;
import org.logoce.lmf.model.lang.LMCorePackage;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.lang.Unit;
import org.logoce.lmf.model.loader.model.LmSymbolIndex;
import org.logoce.lmf.model.loader.model.LmSymbolIndexBuilder;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.util.ModelRegistry;
import org.logoce.lmf.model.util.MetaModelRegistry;
import org.logoce.lmf.model.util.tree.Tree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class TypeCompletionProvider
{
	private static final Logger LOG = LoggerFactory.getLogger(TypeCompletionProvider.class);

	private enum TypeUsageKind
	{
		ANY,
		DATATYPE,
		CONCEPT
	}

	private TypeCompletionProvider()
	{
	}

	static List<CompletionItem> complete(final CompletionContext context)
	{
		final var items = buildTypeCompletions(context);
		final var shaped = shapeCompletionItems(items, context.contextKind());
		LOG.info("LMF LSP completion: type completions (semantic), uri={}, items={}, context={}",
				 context.uri(), shaped.size(), context.contextKind());
		return shaped;
	}

	static List<CompletionItem> completeFromSyntax(final ModelRegistry registry,
												   final SyntaxSnapshot syntax,
												   final Position pos,
												   final CompletionContextKind contextKind)
	{
		final List<CompletionItem> items = new ArrayList<>();
		final Set<String> seenLabels = new HashSet<>();

		for (final CompletionItem item : basicCompletions())
		{
			if (seenLabels.add(item.getLabel()))
			{
				items.add(item);
			}
		}
		final TypeUsageKind usageKind = resolveTypeUsageKind(syntax, pos);

		final var localTypes = extractLocalTypesFromSyntax(syntax, usageKind);
		for (final var typeName : localTypes)
		{
			addTypeCompletion(items,
							  seenLabels,
							  false,
							  null,
							  typeName,
							  "Type in local document");
		}

		for (final Model model : (Iterable<Model>) registry.models()::iterator)
		{
			if (model instanceof MetaModel mm)
			{
				addMetaModelTypes(mm, mm.name(), true, items, seenLabels, usageKind);
			}
		}

		final var shaped = shapeCompletionItems(items, contextKind);
		LOG.info("LMF LSP completion: type completions (fallback), usageKind={}, context={}, rawItems={}, shapedItems={}",
				 usageKind, contextKind, items.size(), shaped.size());
		return shaped;
	}

	private static List<CompletionItem> buildTypeCompletions(final CompletionContext context)
	{
		final List<CompletionItem> items = new ArrayList<>();
		final Set<String> seenLabels = new HashSet<>();

		for (final CompletionItem item : basicCompletions())
		{
			if (seenLabels.add(item.getLabel()))
			{
				items.add(item);
			}
		}

		final LmLanguageServer server = context.server();
		final ModelRegistry registry = server.workspaceIndex().modelRegistry();
		final MetaModel mm = context.metaModel();
		final SyntaxSnapshot syntax = context.syntax();
		final TypeUsageKind usageKind = resolveTypeUsageKind(syntax, context.position());
		LOG.info("LMF LSP completion: buildTypeCompletions, uri={}, usageKind={}, context={}, line={}, character={}",
				 context.uri(), usageKind, context.contextKind(),
				 context.position().getLine(), context.position().getCharacter());

		// Local types from the current model.
		addMetaModelTypes(mm, null, false, items, seenLabels, usageKind);

		for (final String imp : mm.imports())
		{
			final Model imported = registry.getModel(imp);
			if (imported instanceof MetaModel importedMm)
			{
				addMetaModelTypes(importedMm, importedMm.name(), true, items, seenLabels, usageKind);
			}
		}

		final MetaModel lmCore = findLmCoreMetaModel(registry);
		if (lmCore != null)
		{
			addMetaModelTypes(lmCore, lmCore.name(), true, items, seenLabels, usageKind);
		}

		return items;
	}

	private static List<CompletionItem> basicCompletions()
	{
		final List<CompletionItem> items = new ArrayList<>();
		for (final String label : List.of("MetaModel",
										  "Group",
										  "Definition",
										  "Enum",
										  "Unit",
										  "Alias",
										  "JavaWrapper",
										  "Generic",
										  "Operation",
										  "+att",
										  "-att",
										  "+contains",
										  "-contains",
										  "+refers",
										  "-refers"))
		{
			items.add(new CompletionItem(label));
		}
		return items;
	}

	private static List<CompletionItem> shapeCompletionItems(final List<CompletionItem> items,
															 final CompletionContextKind contextKind)
	{
		if (items.isEmpty())
		{
			return items;
		}

		final List<CompletionItem> result = new ArrayList<>();
		for (final CompletionItem item : items)
		{
			final String label = item.getLabel();
			if (label == null || label.isEmpty())
			{
				continue;
			}

			final String detail = item.getDetail();
			final boolean isTypeItem = detail != null;

			switch (contextKind)
			{
				case LOCAL_AT ->
				{
					if (label.startsWith("#") || !isTypeItem)
					{
						continue;
					}
					result.add(item);
				}
				case CROSS_MODEL_HASH ->
				{
					if (!label.startsWith("#") || !isTypeItem)
					{
						continue;
					}
					if (item.getInsertText() == null && label.length() > 1)
					{
						item.setInsertText(label.substring(1));
					}
					result.add(item);
				}
				default -> result.add(item);
			}
		}

		return result;
	}

	private static void addMetaModelTypes(final MetaModel mm,
										  final String modelAlias,
										  final boolean useModelAliasInLabel,
										  final List<CompletionItem> items,
										  final Set<String> seenLabels,
										  final TypeUsageKind usageKind)
	{
		final String modelQualifiedName = mm.domain() + "." + mm.name();
		final boolean crossModel = useModelAliasInLabel && modelAlias != null && !modelAlias.isEmpty();
		int addedGroups = 0;
		int addedEnums = 0;
		int addedUnits = 0;
		int addedWrappers = 0;

		for (final org.logoce.lmf.model.lang.Group<?> group : mm.groups())
		{
			if (isAllowedByUsage(usageKind, true, false))
			{
				addTypeCompletion(items,
								  seenLabels,
								  crossModel,
								  modelAlias,
								  group.name(),
								  "Group in " + modelQualifiedName);
				addedGroups++;
			}
		}
		for (final org.logoce.lmf.model.lang.Enum<?> _enum : mm.enums())
		{
			if (isAllowedByUsage(usageKind, false, true))
			{
				addTypeCompletion(items,
								  seenLabels,
								  crossModel,
								  modelAlias,
								  _enum.name(),
								  "Enum in " + modelQualifiedName);
				addedEnums++;
			}
		}
		for (final Unit<?> unit : mm.units())
		{
			if (isAllowedByUsage(usageKind, false, true))
			{
				addTypeCompletion(items,
								  seenLabels,
								  crossModel,
								  modelAlias,
								  unit.name(),
								  "Unit in " + modelQualifiedName);
				addedUnits++;
			}
		}
		for (final JavaWrapper<?> wrapper : mm.javaWrappers())
		{
			if (isAllowedByUsage(usageKind, false, true))
			{
				addTypeCompletion(items,
								  seenLabels,
								  crossModel,
								  modelAlias,
								  wrapper.name(),
								  "JavaWrapper in " + modelQualifiedName);
				addedWrappers++;
			}
		}

		if (addedGroups + addedEnums + addedUnits + addedWrappers > 0)
		{
			LOG.info("LMF LSP completion: addMetaModelTypes model={} alias={} usageKind={} groups={} enums={} units={} wrappers={}",
					 modelQualifiedName,
					 modelAlias,
					 usageKind,
					 addedGroups,
					 addedEnums,
					 addedUnits,
					 addedWrappers);
		}
	}

	private static void addTypeCompletion(final List<CompletionItem> items,
										  final Set<String> seenLabels,
										  final boolean crossModel,
										  final String modelAlias,
										  final String typeName,
										  final String detail)
	{
		if (typeName == null || typeName.isEmpty())
		{
			return;
		}

		final String label;
		if (crossModel && modelAlias != null && !modelAlias.isEmpty())
		{
			label = "#" + modelAlias + "@" + typeName;
		}
		else
		{
			label = typeName;
		}

		if (seenLabels.add(label))
		{
			final var item = new CompletionItem(label);
			item.setDetail(detail);
			items.add(item);
		}
	}

	private static void addLocalTypesFromIndex(final MetaModel mm,
											   final SyntaxSnapshot syntax,
											   final ModelRegistry registry,
											   final List<CompletionItem> items,
											   final Set<String> seenLabels)
	{
		final LmSymbolIndex index = LmSymbolIndexBuilder.buildIndex(
			mm,
			syntax.roots(),
			syntax.source(),
			registry);

		final String modelQualifiedName = mm.domain() + "." + mm.name();

		for (final LmSymbolIndex.SymbolSpan decl : index.declarations())
		{
			final var id = decl.id();
			if (id.kind() != LmSymbolIndex.SymbolKind.TYPE)
			{
				continue;
			}
			if (!mm.domain().equals(id.modelDomain()) || !mm.name().equals(id.modelName()))
			{
				continue;
			}

			final String typeName = id.name();
			if (typeName == null || typeName.isEmpty())
			{
				continue;
			}

			final String label = typeName;
			if (!seenLabels.add(label))
			{
				continue;
			}

			final var item = new CompletionItem(label);
			item.setDetail("Type in " + modelQualifiedName);
			items.add(item);
		}
	}

	private static TypeUsageKind resolveTypeUsageKind(final SyntaxSnapshot syntax, final Position pos)
	{
		final PNode node = SyntaxNavigation.findPNodeAtOrBeforePosition(syntax, pos);
		if (node == null)
		{
			LOG.info("LMF LSP completion: resolveTypeUsageKind -> ANY (no enclosing node) at line={}, character={}",
					 pos.getLine(), pos.getCharacter());
			return TypeUsageKind.ANY;
		}

		final String keyword = SyntaxNavigation.headerKeyword(node);
		if (keyword == null || keyword.isBlank())
		{
			LOG.info("LMF LSP completion: resolveTypeUsageKind -> ANY (no header keyword) at line={}, character={}",
					 pos.getLine(), pos.getCharacter());
			return TypeUsageKind.ANY;
		}

		final String featureName = SyntaxNavigation.findFeatureNameAtValuePosition(node,
																				   syntax.source(),
																				   pos);
		if (featureName == null || featureName.isBlank())
		{
			LOG.info("LMF LSP completion: resolveTypeUsageKind -> ANY (no feature name) keyword={} at line={}, character={}",
					 keyword, pos.getLine(), pos.getCharacter());
			return TypeUsageKind.ANY;
		}

		final String groupName = resolveGroupNameForHeaderKeyword(keyword);
		if (groupName == null || groupName.isBlank())
		{
			LOG.info("LMF LSP completion: resolveTypeUsageKind -> ANY (no groupName) keyword={} featureName={} at line={}, character={}",
					 keyword, featureName, pos.getLine(), pos.getCharacter());
			return TypeUsageKind.ANY;
		}

		final TypeUsageKind result;
		if ("Attribute".equals(groupName) && "datatype".equals(featureName))
		{
			result = TypeUsageKind.DATATYPE;
		}
		else if ("Relation".equals(groupName) && "concept".equals(featureName))
		{
			result = TypeUsageKind.CONCEPT;
		}
		else
		{
			result = TypeUsageKind.ANY;
		}

		LOG.info("LMF LSP completion: resolveTypeUsageKind result={} keyword={} featureName={} groupName={} line={} character={}",
				 result, keyword, featureName, groupName, pos.getLine(), pos.getCharacter());
		return result;
	}

	private static String resolveGroupNameForHeaderKeyword(final String keyword)
	{
		if (keyword == null || keyword.isBlank())
		{
			return null;
		}

		// Direct group name (MetaModel, Group, Definition, Enum, Unit, JavaWrapper, Alias, Attribute, Relation, ...)
		final MetaModel lmCore = LMCorePackage.MODEL;
		for (final org.logoce.lmf.model.lang.Group<?> g : lmCore.groups())
		{
			if (keyword.equals(g.name()))
			{
				return g.name();
			}
		}

		// Alias-based header, e.g. +att / -att / +contains / -contains.
		final Alias alias = MetaModelRegistry.Instance.getAliasMap().get(keyword);
		if (alias == null)
		{
			return null;
		}

		final String value = alias.value();
		if (value == null || value.isBlank())
		{
			return null;
		}

		int i = 0;
		final int len = value.length();
		while (i < len && Character.isWhitespace(value.charAt(i)))
		{
			i++;
		}
		final int start = i;
		while (i < len && !Character.isWhitespace(value.charAt(i)))
		{
			i++;
		}
		return start < i ? value.substring(start, i) : null;
	}

	private static boolean isAllowedByUsage(final TypeUsageKind usageKind,
											final boolean conceptLike,
											final boolean datatypeLike)
	{
		return switch (usageKind)
		{
			case DATATYPE -> datatypeLike;
			case CONCEPT -> conceptLike;
			case ANY -> true;
		};
	}

	private static java.util.Set<String> extractLocalTypesFromSyntax(final SyntaxSnapshot syntax,
																	 final TypeUsageKind usageKind)
	{
		final var result = new java.util.HashSet<String>();
		for (final Tree<PNode> root : syntax.roots())
		{
			collectLocalTypes(root, usageKind, result);
		}
		if (!result.isEmpty())
		{
			LOG.info("LMF LSP completion: local types from syntax, usageKind={}, count={}", usageKind, result.size());
		}
		return result;
	}

	private static void collectLocalTypes(final Tree<PNode> node,
										  final TypeUsageKind usageKind,
										  final java.util.Set<String> out)
	{
		final var pnode = node.data();
		final var tokens = pnode.tokens();
		if (!tokens.isEmpty())
		{
			final String head = tokens.getFirst().value();
			if (head != null)
			{
				final String trimmed = head.trim();
				final boolean isGroup = "Group".equals(trimmed) || "Definition".equals(trimmed);
				final boolean isEnum = "Enum".equals(trimmed);
				final boolean isUnit = "Unit".equals(trimmed);
				final boolean isJavaWrapper = "JavaWrapper".equals(trimmed);

				final boolean conceptLike = isGroup;
				final boolean datatypeLike = isEnum || isUnit || isJavaWrapper;

				if ((isGroup || isEnum || isUnit || isJavaWrapper) &&
					isAllowedByUsage(usageKind, conceptLike, datatypeLike))
				{
					final String name = SyntaxNavigation.headerName(pnode);
					if (name != null && !name.isEmpty())
					{
						out.add(name);
					}
				}
			}
		}

		for (final Tree<PNode> child : node.children())
		{
			collectLocalTypes(child, usageKind, out);
		}
	}

	private static MetaModel findLmCoreMetaModel(final ModelRegistry registry)
	{
		for (final Model model : (Iterable<Model>) registry.models()::iterator)
		{
			if (model instanceof MetaModel mm &&
				LMCorePackage.MODEL.domain().equals(mm.domain()) &&
				LMCorePackage.MODEL.name().equals(mm.name()))
			{
				return mm;
			}
		}
		return LMCorePackage.MODEL;
	}
}
