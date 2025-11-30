package org.logoce.lmf.lsp.features.completion;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.logoce.lmf.lsp.state.SemanticSnapshot;
import org.logoce.lmf.lsp.state.SyntaxSnapshot;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.JavaWrapper;
import org.logoce.lmf.model.lang.LMCoreDefinition;
import org.logoce.lmf.model.lang.LMCorePackage;
import org.logoce.lmf.model.lang.MetaModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Attribute value completion.
 * <p>
 * This intentionally works off the raw line text plus the LMCore meta-model instead of PNode trees so that it remains
 * robust while the user is editing syntactically incomplete headers such as {@code (Group X concrete=)}.
 * <ul>
 *   <li>We first require an '=' to exist before the caret on the current line.</li>
 *   <li>We then parse the header keyword (e.g. {@code Group}) and the feature name immediately before '='
 *       directly from the line text.</li>
 *   <li>We resolve the LMCore meta-group for that keyword via {@link LMCorePackage#MODEL} and look up the matching
 *       {@link Attribute} by name.</li>
 *   <li>If found, we propose value completions (boolean, enum literals, JavaWrapper default) with explicit
 *       {@link TextEdit}s anchored at the caret.</li>
 * </ul>
 * This line-based strategy is the intended pattern for value completion in headers and should be preferred over
 * PNode-based navigation for similar cases.
 */
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
		if (!hasEqualsBeforePosition(syntax.source(), pos))
		{
			return List.of();
		}

		final Attribute<?, ?> attribute = findAttributeAtValuePosition(uri, semantic, syntax, pos);
		if (attribute == null)
		{
			return List.of();
		}

		final List<CompletionItem> items = buildAttributeValueCompletions(attribute, pos);
		LOG.info("LMF LSP completion: attribute value completions, attribute={}, items={}",
				 attribute.name(), items.size());
		return items;
	}

	private static Attribute<?, ?> findAttributeAtValuePosition(final URI uri,
																final SemanticSnapshot semantic,
																final SyntaxSnapshot syntax,
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

		final int eqIndex = lineText.lastIndexOf('=', targetChar);
		if (eqIndex < 0) return null;

		// Header keyword: first non-whitespace token after '('.
		final int parenIndex = lineText.indexOf('(');
		if (parenIndex < 0) return null;

		int kwStart = parenIndex + 1;
		while (kwStart < lineText.length() && Character.isWhitespace(lineText.charAt(kwStart)))
		{
			kwStart++;
		}
		int kwEnd = kwStart;
		while (kwEnd < lineText.length() && !Character.isWhitespace(lineText.charAt(kwEnd)))
		{
			kwEnd++;
		}

		if (kwStart >= kwEnd) return null;

		final String keyword = lineText.substring(kwStart, kwEnd);

		// Feature name: token immediately before '='.
		int featureEnd = eqIndex - 1;
		while (featureEnd >= 0 && Character.isWhitespace(lineText.charAt(featureEnd)))
		{
			featureEnd--;
		}
		if (featureEnd < 0) return null;

		int featureStart = featureEnd;
		while (featureStart >= 0)
		{
			final char c = lineText.charAt(featureStart);
			if (Character.isWhitespace(c) || c == '(')
			{
				featureStart++;
				break;
			}
			featureStart--;
		}
		if (featureStart < 0) featureStart = 0;

		final String featureName = lineText.substring(featureStart, featureEnd + 1);

		// Use LMCore meta-model as authoritative source for header concepts (MetaModel, Group, Definition, ...).
		final MetaModel lmCore = LMCorePackage.MODEL;
		Group<?> group = null;
		for (final Group<?> g : lmCore.groups())
		{
			if (keyword.equals(g.name()))
			{
				group = g;
				break;
			}
		}

		if (group == null) return null;

		for (final Feature<?, ?> feature : group.features())
		{
			if (feature instanceof Attribute<?, ?> attr && featureName.equals(feature.name()))
			{
				return attr;
			}
		}

		LOG.info("LMF LSP completion: attribute value, feature '{}' not found in group '{}', uri={}, line={}, character={}",
				 featureName, group.name(), uri, pos.getLine(), pos.getCharacter());
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

	private static boolean hasEqualsBeforePosition(final CharSequence source, final Position pos)
	{
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
			return false;
		}

		final int caretOffsetLimit = lineStartOffset + targetChar;
		int lineEndOffset = source.length();
		for (int i = lineStartOffset; i < source.length(); i++)
		{
			if (source.charAt(i) == '\n')
			{
				lineEndOffset = i;
				break;
			}
		}

		for (int i = lineStartOffset; i < lineEndOffset && i < caretOffsetLimit; i++)
		{
			if (source.charAt(i) == '=')
			{
				return true;
			}
		}

		return false;
	}

}
