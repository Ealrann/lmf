package org.logoce.lmf.lsp.features.completion;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;
import org.logoce.lmf.lsp.state.SemanticSnapshot;
import org.logoce.lmf.lsp.state.SyntaxSnapshot;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMCoreDefinition;
import org.logoce.lmf.model.lang.LMCorePackage;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Alias;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.util.MetaModelRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

final class GroupFeatureCompletionProvider
{
	private static final Logger LOG = LoggerFactory.getLogger(GroupFeatureCompletionProvider.class);

	private GroupFeatureCompletionProvider()
	{
	}

	static List<CompletionItem> complete(final CompletionContext context)
	{
		final SemanticSnapshot semantic = context.semantic();
		final SyntaxSnapshot syntax = context.syntax();
		final Position pos = context.position();

		if (syntax == null)
		{
			return List.of();
		}

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

		final List<CompletionItem> items = buildGroupFeatureCompletions(group);
		if (!items.isEmpty())
		{
			LOG.info("LMF LSP completion: group feature completions, group={}, items={}",
					 group.name(), items.size());
		}
		return items;
	}

	private static List<CompletionItem> buildGroupFeatureCompletions(final Group<?> group)
	{
		final var items = new ArrayList<CompletionItem>();

		for (final Feature<?, ?> feature : group.features())
		{
			final String name = feature.name();
			if (name == null || name.isEmpty())
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

	private static Group<?> fallbackGroupFromSyntax(final SyntaxSnapshot syntax, final Position pos)
	{
		final PNode node = SyntaxNavigation.findPNodeAtPosition(syntax, pos);
		if (node == null)
		{
			return null;
		}

		final String keyword = SyntaxNavigation.headerKeyword(node);
		if (keyword == null || keyword.isBlank())
		{
			return null;
		}

		final String groupName = resolveGroupNameForHeaderKeyword(keyword);
		if (groupName == null || groupName.isBlank())
		{
			return null;
		}

		final MetaModel lmCore = LMCorePackage.MODEL;
		for (final Group<?> g : lmCore.groups())
		{
			if (groupName.equals(g.name()))
			{
				return g;
			}
		}
		return null;
	}

	private static String resolveGroupNameForHeaderKeyword(final String keyword)
	{
		if (keyword == null || keyword.isBlank())
		{
			return null;
		}

		// Direct group name (MetaModel, Group, Definition, Enum, Unit, JavaWrapper, Alias, Attribute, Relation, ...)
		final MetaModel lmCore = LMCorePackage.MODEL;
		for (final Group<?> g : lmCore.groups())
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
}
