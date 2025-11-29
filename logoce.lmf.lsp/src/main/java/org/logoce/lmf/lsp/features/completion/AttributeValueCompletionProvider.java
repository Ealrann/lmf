package org.logoce.lmf.lsp.features.completion;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;
import org.logoce.lmf.lsp.state.SemanticSnapshot;
import org.logoce.lmf.lsp.state.SyntaxSnapshot;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.JavaWrapper;
import org.logoce.lmf.model.lang.LMCoreDefinition;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

final class AttributeValueCompletionProvider
{
	private static final Logger LOG = LoggerFactory.getLogger(AttributeValueCompletionProvider.class);

	private AttributeValueCompletionProvider()
	{
	}

	static List<CompletionItem> complete(final URI uri,
										 final SemanticSnapshot semantic,
										 final SyntaxSnapshot syntax,
										 final Position pos)
	{
		final Attribute<?, ?> attribute = findAttributeAtValuePosition(uri, semantic, syntax, pos);
		if (attribute == null)
		{
			return List.of();
		}

		final List<CompletionItem> items = buildAttributeValueCompletions(attribute);
		LOG.info("LMF LSP completion: attribute value completions, attribute={}, items={}",
				 attribute.name(), items.size());
		return items;
	}

	private static Attribute<?, ?> findAttributeAtValuePosition(final URI uri,
																final SemanticSnapshot semantic,
																final SyntaxSnapshot syntax,
																final Position pos)
	{
		final PNode targetNode = SyntaxNavigation.findPNodeAtPosition(syntax, pos);
		if (targetNode == null)
		{
			return null;
		}

		final Group<?> group = SemanticNavigation.findContainingGroupForNode(semantic, targetNode);
		if (group == null)
		{
			return null;
		}

		final String featureName = SyntaxNavigation.findFeatureNameAtValuePosition(targetNode, syntax.source(), pos);
		if (featureName == null || featureName.isEmpty())
		{
			return null;
		}

		for (final Feature<?, ?> feature : group.features())
		{
			if (feature instanceof Attribute<?, ?> attr && featureName.equals(feature.name()))
			{
				return attr;
			}
		}

		return null;
	}

	private static List<CompletionItem> buildAttributeValueCompletions(final Attribute<?, ?> attribute)
	{
		final var items = new ArrayList<CompletionItem>();

		final var datatype = attribute.datatype();
		if (datatype == null)
		{
			return items;
		}

		if (datatype == LMCoreDefinition.Units.BOOLEAN)
		{
			final var trueItem = new CompletionItem("true");
			trueItem.setDetail("boolean literal");
			items.add(trueItem);

			final var falseItem = new CompletionItem("false");
			falseItem.setDetail("boolean literal");
			items.add(falseItem);

			return items;
		}

		if (datatype instanceof org.logoce.lmf.model.lang.Enum<?> _enum)
		{
			for (final String literal : _enum.literals())
			{
				if (literal == null || literal.isEmpty())
				{
					continue;
				}
				final var item = new CompletionItem(literal);
				item.setDetail("enum literal");
				items.add(item);
			}
			return items;
		}

		if (datatype instanceof JavaWrapper<?> wrapper)
		{
			final var serializer = wrapper.serializer();
			if (serializer != null)
			{
				final String defaultValue = serializer.defaultValue();
				if (defaultValue != null && !defaultValue.isEmpty())
				{
					final var item = new CompletionItem(defaultValue);
					item.setDetail("default value");
					items.add(item);
				}
			}
		}

		return items;
	}
}
