package org.logoce.lmf.lsp.features.completion;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.logoce.lmf.lsp.state.SyntaxSnapshot;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.resource.parsing.PToken;
import org.logoce.lmf.model.util.TextPositions;
import org.logoce.lmf.model.util.tree.Tree;

final class SyntaxNavigation
{
	private SyntaxNavigation()
	{
	}

	static CompletionContextKind detectCompletionContext(final SyntaxSnapshot syntax, final Position pos)
	{
		final CharSequence source = syntax.source();
		for (final Tree<PNode> root : syntax.roots())
		{
			final CompletionContextKind kind = detectContextInNode(root, source, pos);
			if (kind != null && kind != CompletionContextKind.DEFAULT)
			{
				return kind;
			}
		}
		return CompletionContextKind.DEFAULT;
	}

	private static CompletionContextKind detectContextInNode(final Tree<PNode> node,
															 final CharSequence source,
															 final Position pos)
	{
		for (final PToken token : node.data().tokens())
		{
			final Range range = rangeForToken(token, source);
			if (rangeContains(range, pos))
			{
				final String value = token.value();
				if (value == null || value.isEmpty())
				{
					return CompletionContextKind.DEFAULT;
				}
				final char first = value.charAt(0);
				if (first == '@')
				{
					return CompletionContextKind.LOCAL_AT;
				}
				if (first == '#')
				{
					return CompletionContextKind.CROSS_MODEL_HASH;
				}
				return CompletionContextKind.DEFAULT;
			}
		}

		for (final Tree<PNode> child : node.children())
		{
			final CompletionContextKind kind = detectContextInNode(child, source, pos);
			if (kind != null && kind != CompletionContextKind.DEFAULT)
			{
				return kind;
			}
		}

		return CompletionContextKind.DEFAULT;
	}

	static Range rangeForToken(final PToken token, final CharSequence source)
	{
		final int start = token.offset();
		final int end = start + Math.max(1, token.length());
		final int startLine = Math.max(0, TextPositions.lineFor(source, start) - 1);
		final int startChar = Math.max(0, TextPositions.columnFor(source, start) - 1);
		final int endLine = Math.max(0, TextPositions.lineFor(source, end) - 1);
		final int endChar = Math.max(0, TextPositions.columnFor(source, end) - 1);
		final var startPos = new Position(startLine, startChar);
		final var endPos = new Position(endLine, endChar);
		return new Range(startPos, endPos);
	}

	static boolean rangeContains(final Range range, final Position pos)
	{
		final Position start = range.getStart();
		final Position end = range.getEnd();

		if (pos.getLine() < start.getLine() || pos.getLine() > end.getLine())
		{
			return false;
		}
		if (pos.getLine() == start.getLine() && pos.getCharacter() < start.getCharacter())
		{
			return false;
		}
		if (pos.getLine() == end.getLine() && pos.getCharacter() > end.getCharacter())
		{
			return false;
		}
		return true;
	}

	static PNode findPNodeAtPosition(final SyntaxSnapshot syntax, final Position pos)
	{
		final CharSequence source = syntax.source();
		PNode found = null;

		for (final Tree<PNode> root : syntax.roots())
		{
			final PNode candidate = findPNodeInTree(root, source, pos);
			if (candidate != null)
			{
				found = candidate;
			}
		}

		return found;
	}

	private static PNode findPNodeInTree(final Tree<PNode> node,
										 final CharSequence source,
										 final Position pos)
	{
		final var tokens = node.data().tokens();
		if (tokens.isEmpty())
		{
			return null;
		}

		final var first = tokens.getFirst();
		final var last = tokens.getLast();
		final var nodeRange = new Range(
			rangeForToken(first, source).getStart(),
			rangeForToken(last, source).getEnd());

		if (!rangeContains(nodeRange, pos))
		{
			return null;
		}

		for (final Tree<PNode> child : node.children())
		{
			final PNode childCandidate = findPNodeInTree(child, source, pos);
			if (childCandidate != null)
			{
				return childCandidate;
			}
		}

		return node.data();
	}

	static PNode findEnclosingGroupHeader(final SyntaxSnapshot syntax, final Position pos)
	{
		final CharSequence source = syntax.source();
		PNode found = null;

		for (final Tree<PNode> root : syntax.roots())
		{
			final PNode candidate = findEnclosingGroupHeaderInTree(root, source, pos);
			if (candidate != null)
			{
				found = candidate;
			}
		}

		return found;
	}

	private static PNode findEnclosingGroupHeaderInTree(final Tree<PNode> node,
														final CharSequence source,
														final Position pos)
	{
		final var tokens = node.data().tokens();
		if (tokens.isEmpty())
		{
			return null;
		}

		final var first = tokens.getFirst();
		final var last = tokens.getLast();
		final var nodeRange = new Range(
			rangeForToken(first, source).getStart(),
			rangeForToken(last, source).getEnd());

		if (!rangeContains(nodeRange, pos))
		{
			return null;
		}

		for (final Tree<PNode> child : node.children())
		{
			final PNode childCandidate = findEnclosingGroupHeaderInTree(child, source, pos);
			if (childCandidate != null)
			{
				return childCandidate;
			}
		}

		return isGroupHeaderNode(node.data()) ? node.data() : null;
	}

	private static boolean isGroupHeaderNode(final PNode node)
	{
		final var tokens = node.tokens();
		for (final PToken token : tokens)
		{
			final String value = token.value();
			if (value == null || value.isBlank() || "(".equals(value))
			{
				continue;
			}

			return switch (value)
			{
				case "MetaModel", "Group", "Definition", "Enum", "Unit", "JavaWrapper", "Alias" -> true;
				default -> false;
			};
		}
		return false;
	}

	static String headerKeyword(final PNode node)
	{
		for (final PToken token : node.tokens())
		{
			final String value = token.value();
			if (value == null || value.isBlank() || "(".equals(value))
			{
				continue;
			}
			return value;
		}
		return null;
	}

	static String headerName(final PNode node)
	{
		boolean seenKeyword = false;
		for (final PToken token : node.tokens())
		{
			final String value = token.value();
			if (value == null || value.isBlank() || "(".equals(value))
			{
				continue;
			}

			if (!seenKeyword)
			{
				seenKeyword = true;
				continue;
			}

			if (")".equals(value) || value.contains("="))
			{
				continue;
			}
			return value;
		}
		return null;
	}

	static String findFeatureNameAtValuePosition(final PNode node,
												 final CharSequence source,
												 final Position pos)
	{
		final var tokens = node.tokens();
		String currentName = null;

		for (int i = 0; i < tokens.size(); i++)
		{
			final PToken tok = tokens.get(i);
			final Range tokenRange = rangeForToken(tok, source);

			final Position start = tokenRange.getStart();
			if (pos.getLine() < start.getLine() ||
				(pos.getLine() == start.getLine() && pos.getCharacter() < start.getCharacter()))
			{
				break;
			}

			final String val = tok.value();
			if (val == null || val.isEmpty())
			{
				continue;
			}

			final int eq = val.indexOf('=');
			if (eq > 0)
			{
				currentName = val.substring(0, eq);
				continue;
			}

			if (Character.isJavaIdentifierStart(val.charAt(0)) && i + 1 < tokens.size())
			{
				final PToken eqTok = tokens.get(i + 1);
				if ("=".equals(eqTok.value()))
				{
					currentName = val;
				}
			}
		}

		return currentName;
	}
}
