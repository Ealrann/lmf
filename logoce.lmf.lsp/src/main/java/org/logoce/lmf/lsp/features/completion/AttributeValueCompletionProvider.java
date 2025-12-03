package org.logoce.lmf.lsp.features.completion;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.logoce.lmf.lsp.state.SyntaxSnapshot;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Alias;
import org.logoce.lmf.model.lang.JavaWrapper;
import org.logoce.lmf.model.lang.LMCoreDefinition;
import org.logoce.lmf.model.lang.LMCorePackage;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.util.MetaModelRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Attribute value completion.
 * <p>
 * Completions are offered when the caret is positioned after an '=' inside a header, e.g.
 * {@code (Group X concrete=)}.
 * <ul>
 *   <li>We first require an '=' to exist before the caret on the current line.</li>
 *   <li>We use the link trees from {@code SemanticSnapshot} and the LMCore meta-model to resolve the
 *       {@link Attribute} being edited.</li>
 *   <li>If found, we propose value completions (boolean literals, enum literals, JavaWrapper default) with explicit
 *       {@link TextEdit}s anchored at the caret.</li>
 * </ul>
 */
final class AttributeValueCompletionProvider
{
	private static final Logger LOG = LoggerFactory.getLogger(AttributeValueCompletionProvider.class);

	private AttributeValueCompletionProvider()
	{
	}

	static List<CompletionItem> complete(final CompletionContext context)
	{
		final SyntaxSnapshot syntax = context.syntax();

		if (syntax == null)
		{
			LOG.info("LMF LSP completion: attribute value – missing syntax snapshot, uri={}, line={}, character={}",
					 context.uri(), context.position().getLine(), context.position().getCharacter());
			return List.of();
		}

		if (!SyntaxNavigation.hasEqualsBeforePosition(syntax, context.position()))
		{
			LOG.info("LMF LSP completion: attribute value – no '=' before caret, uri={}, line={}, character={}",
					 context.uri(), context.position().getLine(), context.position().getCharacter());
			return List.of();
		}

		final Attribute<?, ?> attribute = findAttributeAtValuePosition(syntax, context.position());
		if (attribute == null)
		{
			LOG.info("LMF LSP completion: attribute value – no attribute resolved from header text at uri={}, line={}, character={}",
					 context.uri(), context.position().getLine(), context.position().getCharacter());
			return List.of();
		}

		final List<CompletionItem> items = buildAttributeValueCompletions(attribute, context.position());
		LOG.info("LMF LSP completion: attribute value completions, attribute={}, items={}",
				 attribute.name(), items.size());
		return items;
	}

	private static Attribute<?, ?> findAttributeAtValuePosition(final SyntaxSnapshot syntax,
																final Position pos)
	{
		final CharSequence source = syntax.source();
		final int targetLine = pos.getLine();
		final int targetChar = pos.getCharacter();

		int line = 0;
		int lineStartOffset = -1;
		for (int i = 0; i < source.length(); i++)
		{
			final char c = source.charAt(i);
			if (line == targetLine)
			{
				lineStartOffset = i;
				break;
			}
			if (c == '\n')
			{
				line++;
			}
		}

		if (lineStartOffset == -1)
		{
			return null;
		}

		int lineEndOffset = source.length();
		for (int i = lineStartOffset; i < source.length(); i++)
		{
			if (source.charAt(i) == '\n')
			{
				lineEndOffset = i;
				break;
			}
		}

		final String lineText = source.subSequence(lineStartOffset, lineEndOffset).toString();

		final int openIdx = lineText.indexOf('(');
		if (openIdx < 0)
		{
			return null;
		}

		int closeIdx = lineText.indexOf(')', openIdx + 1);
		if (closeIdx < 0)
		{
			closeIdx = lineText.length();
		}

		final int headerStart = openIdx + 1;
		final int headerEnd = closeIdx;
		if (headerStart >= headerEnd)
		{
			return null;
		}

		final String header = lineText.substring(headerStart, headerEnd);
		final int headerCaret = Math.min(Math.max(0, targetChar - headerStart), header.length());

		final var tokens = tokenizeHeader(header);
		if (tokens.isEmpty())
		{
			return null;
		}

		// First token is the header keyword (MetaModel, Group, Definition, Enum, Unit, JavaWrapper, Alias, ...).
		final String keyword = tokens.getFirst().text();
		if (keyword == null || keyword.isBlank())
		{
			return null;
		}

		final String featureName = resolveFeatureNameAtCaret(tokens, headerCaret);
		if (featureName == null || featureName.isBlank())
		{
			return null;
		}

		final String groupName = resolveGroupNameForHeaderKeyword(keyword);
		if (groupName == null || groupName.isBlank())
		{
			return null;
		}

		final MetaModel lmCore = LMCorePackage.MODEL;
		Group<?> group = null;
		for (final Group<?> g : lmCore.groups())
		{
			if (groupName.equals(g.name()))
			{
				group = g;
				break;
			}
		}

		if (group == null)
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

	private static List<CompletionItem> buildAttributeValueCompletions(final Attribute<?, ?> attribute,
																	   final Position pos)
	{
		final var items = new ArrayList<CompletionItem>();

		final var datatype = attribute.datatype();
		if (datatype == null)
		{
			return items;
		}

		if (datatype == LMCoreDefinition.Units.BOOLEAN)
		{
			final var range = new Range(new Position(pos.getLine(), pos.getCharacter()),
										new Position(pos.getLine(), pos.getCharacter()));

			final var trueItem = new CompletionItem("true");
			trueItem.setDetail("boolean literal");
			trueItem.setTextEdit(Either.forLeft(new TextEdit(range, "true")));
			items.add(trueItem);

			final var falseItem = new CompletionItem("false");
			falseItem.setDetail("boolean literal");
			falseItem.setTextEdit(Either.forLeft(new TextEdit(range, "false")));
			items.add(falseItem);

			return items;
		}

		if (datatype instanceof org.logoce.lmf.model.lang.Enum<?> _enum)
		{
			final var range = new Range(new Position(pos.getLine(), pos.getCharacter()),
										new Position(pos.getLine(), pos.getCharacter()));

			for (final String literal : _enum.literals())
			{
				if (literal == null || literal.isEmpty())
				{
					continue;
				}
				final var item = new CompletionItem(literal);
				item.setDetail("enum literal");
				item.setTextEdit(Either.forLeft(new TextEdit(range, literal)));
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
					final var range = new Range(new Position(pos.getLine(), pos.getCharacter()),
												new Position(pos.getLine(), pos.getCharacter()));

					final var item = new CompletionItem(defaultValue);
					item.setDetail("default value");
					item.setTextEdit(Either.forLeft(new TextEdit(range, defaultValue)));
					items.add(item);
				}
			}
		}

		return items;
	}

	record HeaderToken(String text, int start, int end)
	{
	}

	static java.util.List<HeaderToken> tokenizeHeader(final String header)
	{
		final var out = new java.util.ArrayList<HeaderToken>();
		final int length = header.length();
		int i = 0;

		while (i < length)
		{
			final char c = header.charAt(i);
			if (Character.isWhitespace(c))
			{
				i++;
				continue;
			}

			if (c == '=')
			{
				out.add(new HeaderToken("=", i, i + 1));
				i++;
				continue;
			}

			int start = i;
			while (i < length)
			{
				final char ch = header.charAt(i);
				if (Character.isWhitespace(ch) || ch == '=')
				{
					break;
				}
				i++;
			}
			final int end = i;
			if (start < end)
			{
				out.add(new HeaderToken(header.substring(start, end), start, end));
			}
		}

		return java.util.List.copyOf(out);
	}

	static String resolveFeatureNameAtCaret(final java.util.List<HeaderToken> tokens,
											final int headerCaret)
	{
		if (tokens.isEmpty())
		{
			return null;
		}

		// Find the last token that starts at or before the caret.
		int caretTokenIndex = -1;
		for (int i = 0; i < tokens.size(); i++)
		{
			final HeaderToken tok = tokens.get(i);
			if (tok.start() <= headerCaret)
			{
				caretTokenIndex = i;
			}
			else
			{
				break;
			}
		}

		if (caretTokenIndex <= 0)
		{
			return null;
		}

		// Walk backwards from the caret to find the nearest '=',
		// then, if the caret is still within the value for that assignment,
		// pick the token immediately before '=' as the feature name.
		for (int i = caretTokenIndex; i >= 1; i--)
		{
			final HeaderToken tok = tokens.get(i);
			if ("=".equals(tok.text()))
			{
				final HeaderToken prev = tokens.get(i - 1);
				HeaderToken valueTok = (i + 1) < tokens.size() ? tokens.get(i + 1) : null;

				if (valueTok != null)
				{
					final int valueEnd = valueTok.end();
					if (headerCaret > valueEnd)
					{
						// Caret is past the end of the value; treat this as no longer being
						// in a value position for this feature.
						return null;
					}
				}

				return prev.text();
			}
		}

		return null;
	}

	static String resolveGroupNameForHeaderKeyword(final String keyword)
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
