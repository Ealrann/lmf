package org.logoce.lmf.lsp.features.completion;

import org.eclipse.lsp4j.CompletionItem;
import org.logoce.lmf.lsp.LmLanguageServer;
import org.logoce.lmf.model.lang.JavaWrapper;
import org.logoce.lmf.model.lang.LMCorePackage;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.lang.Unit;
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

		addMetaModelTypes(mm, null, false, items, seenLabels);

		for (final String imp : mm.imports())
		{
			final Model imported = registry.getModel(imp);
			if (imported instanceof MetaModel importedMm)
			{
				addMetaModelTypes(importedMm, importedMm.name(), true, items, seenLabels);
			}
		}

		final MetaModel lmCore = findLmCoreMetaModel(registry);
		if (lmCore != null)
		{
			addMetaModelTypes(lmCore, lmCore.name(), true, items, seenLabels);
		}

		final List<CompletionItem> shaped = shapeCompletionItems(items, context.contextKind());
		LOG.info("LMF LSP completion: type completions, uri={}, items={}, context={}",
				 context.uri(), shaped.size(), context.contextKind());

		return shaped;
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
										  final Set<String> seenLabels)
	{
		final String modelQualifiedName = mm.domain() + "." + mm.name();
		final boolean crossModel = useModelAliasInLabel && modelAlias != null && !modelAlias.isEmpty();

		for (final org.logoce.lmf.model.lang.Group<?> group : mm.groups())
		{
			addTypeCompletion(items, seenLabels, crossModel, modelAlias, group.name(), "Group in " + modelQualifiedName);
		}
		for (final org.logoce.lmf.model.lang.Enum<?> _enum : mm.enums())
		{
			addTypeCompletion(items, seenLabels, crossModel, modelAlias, _enum.name(), "Enum in " + modelQualifiedName);
		}
		for (final Unit<?> unit : mm.units())
		{
			addTypeCompletion(items, seenLabels, crossModel, modelAlias, unit.name(), "Unit in " + modelQualifiedName);
		}
		for (final JavaWrapper<?> wrapper : mm.javaWrappers())
		{
			addTypeCompletion(items, seenLabels, crossModel, modelAlias, wrapper.name(), "JavaWrapper in " + modelQualifiedName);
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

