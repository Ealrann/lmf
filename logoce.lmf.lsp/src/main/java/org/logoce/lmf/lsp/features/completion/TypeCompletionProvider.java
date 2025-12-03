package org.logoce.lmf.lsp.features.completion;

import org.eclipse.lsp4j.CompletionItem;
import org.logoce.lmf.lsp.LmLanguageServer;
import org.logoce.lmf.lsp.state.LmSymbolKind;
import org.logoce.lmf.lsp.state.ModelKey;
import org.logoce.lmf.lsp.state.SymbolEntry;
import org.logoce.lmf.model.lang.Enum;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.JavaWrapper;
import org.logoce.lmf.model.lang.LMCorePackage;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.lang.Unit;
import org.logoce.lmf.model.loader.model.LmSymbolIndex;
import org.logoce.lmf.model.loader.model.LmSymbolIndexBuilder;
import org.logoce.lmf.model.util.ModelRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class TypeCompletionProvider
{
	private static final Logger LOG = LoggerFactory.getLogger(TypeCompletionProvider.class);

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
		final TypeUsageKind usageKind = context.value() != null ? context.value().typeUsageKind() : TypeUsageKind.ANY;
		LOG.info("LMF LSP completion: buildTypeCompletions, uri={}, usageKind={}, context={}, line={}, character={}",
				 context.uri(), usageKind, context.contextKind(),
				 context.position().getLine(), context.position().getCharacter());

		// Local types from the current document via the symbol index.
		addLocalTypesFromSymbolIndex(context, mm, usageKind, items, seenLabels);

		// Types from imported meta-models – prefer workspace symbol index, fall back to the model registry.
		for (final String imp : mm.imports())
		{
			final Model imported = registry.getModel(imp);
			if (imported instanceof MetaModel importedMm)
			{
				addCrossModelTypes(context, importedMm, importedMm.name(), usageKind, items, seenLabels);
			}
		}

		// LMCore types.
		final MetaModel lmCore = findLmCoreMetaModel(registry);
		if (lmCore != null)
		{
			addCrossModelTypes(context, lmCore, lmCore.name(), usageKind, items, seenLabels);
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

	private static void addLocalTypesFromSymbolIndex(final CompletionContext context,
													 final MetaModel mm,
													 final TypeUsageKind usageKind,
													 final List<CompletionItem> items,
													 final Set<String> seenLabels)
	{
		final var semantic = context.semantic();
		final var syntax = context.syntax();
		if (semantic == null || syntax == null || semantic.model() == null)
		{
			return;
		}

		final Model model = semantic.model();
		final LmSymbolIndex index = LmSymbolIndexBuilder.buildIndex(
			model,
			syntax.roots(),
			syntax.source(),
			ModelRegistry.empty());

		int added = 0;
		for (final LmSymbolIndex.SymbolSpan decl : index.declarations())
		{
			if (decl.id().kind() != LmSymbolIndex.SymbolKind.TYPE)
			{
				continue;
			}

			final String typeName = decl.id().name();
			final boolean conceptLike = isConceptLikeType(mm, typeName);
			final boolean datatypeLike = isDatatypeLikeType(mm, typeName);

			if (!isAllowedByUsage(usageKind, conceptLike, datatypeLike))
			{
				continue;
			}

			addTypeCompletion(items,
							  seenLabels,
							  false,
							  null,
							  typeName,
							  "Type in " + mm.domain() + "." + mm.name());
			added++;
		}

		if (added > 0)
		{
			LOG.info("LMF LSP completion: addLocalTypesFromSymbolIndex model={} types={}",
					 mm.domain() + "." + mm.name(),
					 added);
		}
	}

	private static void addCrossModelTypes(final CompletionContext context,
										   final MetaModel importedMm,
										   final String modelAlias,
										   final TypeUsageKind usageKind,
										   final List<CompletionItem> items,
										   final Set<String> seenLabels)
	{
		final var workspaceIndex = context.server().workspaceIndex();
		final var targetKey = new ModelKey(importedMm.domain(), importedMm.name());

		int added = 0;
		for (final SymbolEntry entry : workspaceIndex.symbolIndex().values())
		{
			final var id = entry.id();
			if (id.kind() != LmSymbolKind.TYPE)
			{
				continue;
			}
			if (!targetKey.equals(id.modelKey()))
			{
				continue;
			}

			final String typeName = id.name();
			final boolean conceptLike = isConceptLikeType(importedMm, typeName);
			final boolean datatypeLike = isDatatypeLikeType(importedMm, typeName);
			if (!isAllowedByUsage(usageKind, conceptLike, datatypeLike))
			{
				continue;
			}

			addTypeCompletion(items,
							  seenLabels,
							  true,
							  modelAlias,
							  typeName,
							  "Type in " + importedMm.domain() + "." + importedMm.name());
			added++;
		}

		if (added > 0)
		{
			LOG.info("LMF LSP completion: addCrossModelTypes model={} alias={} types={}",
					 importedMm.domain() + "." + importedMm.name(),
					 modelAlias,
					 added);
			return;
		}

		// Fallback when no symbol index entries exist for the imported model.
		addMetaModelTypes(importedMm, modelAlias, true, items, seenLabels, usageKind);
	}

	private static boolean isConceptLikeType(final MetaModel mm, final String typeName)
	{
		if (typeName == null || typeName.isEmpty())
		{
			return false;
		}

		for (final Group<?> g : mm.groups())
		{
			if (typeName.equals(g.name()))
			{
				return true;
			}
		}
		return false;
	}

	private static boolean isDatatypeLikeType(final MetaModel mm, final String typeName)
	{
		if (typeName == null || typeName.isEmpty())
		{
			return false;
		}

		for (final Enum<?> _enum : mm.enums())
		{
			if (typeName.equals(_enum.name()))
			{
				return true;
			}
		}
		for (final Unit<?> unit : mm.units())
		{
			if (typeName.equals(unit.name()))
			{
				return true;
			}
		}
		for (final JavaWrapper<?> wrapper : mm.javaWrappers())
		{
			if (typeName.equals(wrapper.name()))
			{
				return true;
			}
		}
		return false;
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
