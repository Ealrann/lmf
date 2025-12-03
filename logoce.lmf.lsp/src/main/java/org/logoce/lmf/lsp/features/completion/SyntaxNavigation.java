package org.logoce.lmf.lsp.features.completion;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.logoce.lmf.lsp.state.SyntaxSnapshot;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.resource.parsing.PToken;
import org.logoce.lmf.model.util.TextPositions;
import org.logoce.lmf.model.util.tree.Tree;

import java.util.List;

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

	private static Range rangeForOffsets(final int startOffset, final int endOffset, final CharSequence source)
	{
		final int startLine = Math.max(0, TextPositions.lineFor(source, startOffset) - 1);
		final int startChar = Math.max(0, TextPositions.columnFor(source, startOffset) - 1);
		final int endLine = Math.max(0, TextPositions.lineFor(source, endOffset) - 1);
		final int endChar = Math.max(0, TextPositions.columnFor(source, endOffset) - 1);
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
		final var best = new BestPNode();

		for (final Tree<PNode> root : syntax.roots())
		{
			findPNodeInTree(root, source, pos, best);
		}

		return best.node;
	}

	static PNode findPNodeAtOrBeforePosition(final SyntaxSnapshot syntax, final Position pos)
	{
		PNode node = findPNodeAtPosition(syntax, pos);
		if (node == null && pos.getCharacter() > 0)
		{
			final var prev = new Position(pos.getLine(), pos.getCharacter() - 1);
			node = findPNodeAtPosition(syntax, prev);
		}
		return node;
	}

	private static void findPNodeInTree(final Tree<PNode> node,
										final CharSequence source,
										final Position pos,
										final BestPNode best)
	{
		final var tokens = node.data().tokens();
		if (!tokens.isEmpty())
		{
			final var first = tokens.getFirst();
			final var last = tokens.getLast();
			final var nodeRange = new Range(
				rangeForToken(first, source).getStart(),
				rangeForToken(last, source).getEnd());

			if (rangeContains(nodeRange, pos))
			{
				final int span = spanLength(nodeRange);
				if (span < best.span)
				{
					best.node = node.data();
					best.span = span;
				}
			}
		}

		for (final Tree<PNode> child : node.children())
		{
			findPNodeInTree(child, source, pos, best);
		}
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

	private static int spanLength(final Range range)
	{
		final var start = range.getStart();
		final var end = range.getEnd();

		if (start.getLine() == end.getLine())
		{
			return Math.max(1, end.getCharacter() - start.getCharacter());
		}

		final int lineSpan = end.getLine() - start.getLine();
		final int charSpan = Math.max(0, end.getCharacter() - start.getCharacter());
		return lineSpan * 1_000_000 + charSpan;
	}

	private static final class BestPNode
	{
		private PNode node;
		private int span = Integer.MAX_VALUE;
	}
}
