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
import org.logoce.lmf.model.lang.Model;
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

	static List<CompletionItem> complete(final SemanticSnapshot semantic,
										 final SyntaxSnapshot syntax,
										 final Position pos)
	{
		Group<?> group = SemanticNavigation.findGroupAtPosition(semantic, syntax, pos);
		if (group == null)
		{
			group = resolveGroupFromModelAndSyntax(semantic, syntax, pos);
		}
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

	private static Group<?> resolveGroupFromModelAndSyntax(final SemanticSnapshot semantic,
														   final SyntaxSnapshot syntax,
														   final Position pos)
	{
		// For now, use LMCore as the authoritative meta-model for header keywords.
		final MetaModel lmCore = LMCorePackage.MODEL;

		final var headerNode = SyntaxNavigation.findEnclosingGroupHeader(syntax, pos);
		if (headerNode == null)
		{
			return null;
		}

		final String keyword = SyntaxNavigation.headerKeyword(headerNode);
		if (keyword == null || keyword.isEmpty())
		{
			return null;
		}

		for (final Group<?> g : lmCore.groups())
		{
			if (keyword.equals(g.name()))
			{
				return g;
			}
		}

		return null;
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
}
