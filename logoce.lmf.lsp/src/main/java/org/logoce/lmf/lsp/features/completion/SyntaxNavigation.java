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

		int startOffset = first.offset();
		int endOffset = last.offset() + Math.max(1, last.length());

		// Extend the header range up to the closing ')' on the same line, so that
		// positions after the last header token (e.g. after '=') still count as
		// being inside the header for completion purposes.
		for (int i = endOffset; i < source.length(); i++)
		{
			final char c = source.charAt(i);
			if (c == ')')
			{
				endOffset = i + 1;
				break;
			}
			if (c == '\n')
			{
				break;
			}
		}

		final var nodeRange = rangeForOffsets(startOffset, endOffset, source);

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
		final List<PToken> tokens = node.tokens();
		if (tokens.isEmpty())
		{
			return null;
		}

		int anchorIndex = -1;

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
			anchorIndex = i;
		}

		if (anchorIndex == -1)
		{
			return null;
		}

		// Determine header span: first non-blank token is the keyword, second is the header name.
		int firstNonBlank = -1;
		int secondNonBlank = -1;
		for (int i = 0; i <= anchorIndex; i++)
		{
			final String v = tokens.get(i).value();
			if (v == null || v.isBlank() || "(".equals(v))
			{
				continue;
			}
			if (firstNonBlank == -1)
			{
				firstNonBlank = i;
			}
			else if (secondNonBlank == -1)
			{
				secondNonBlank = i;
				break;
			}
		}

		// Start scanning features after the header name if present,
		// otherwise after the keyword.
		final int featuresStart = secondNonBlank != -1
								  ? secondNonBlank + 1
								  : (firstNonBlank != -1 ? firstNonBlank + 1 : 0);

		String candidate = null;

		// Walk backwards from the token at or before the caret to find the
		// nearest 'name=' style feature assignment, falling back to the last
		// identifier-like token when '=' is absent (e.g. boolean flags like 'concrete').
		for (int i = anchorIndex; i >= featuresStart; i--)
		{
			final PToken tok = tokens.get(i);
			final String val = tok.value();

			if (val == null || val.isEmpty())
			{
				continue;
			}

			final int eq = val.indexOf('=');
			if (eq > 0)
			{
				return val.substring(0, eq);
			}

			if ("=".equals(val))
			{
				// Look for an identifier immediately before the '=' (skipping whitespace).
				for (int j = i - 1; j >= 0; j--)
				{
					final String prevVal = tokens.get(j).value();
					if (prevVal == null || prevVal.isBlank())
					{
						continue;
					}

					final int prevEq = prevVal.indexOf('=');
					if (prevEq > 0)
					{
						return prevVal.substring(0, prevEq);
					}

					if (Character.isJavaIdentifierStart(prevVal.charAt(0)))
					{
						return prevVal;
					}
				}
				continue;
			}

			// Fallback: remember the last identifier-like token before the caret.
			if (Character.isJavaIdentifierStart(val.charAt(0)) && val.indexOf('=') < 0)
			{
				candidate = val;
			}
		}

		return candidate;
	}

	private static int findNextNonWhitespaceTokenIndex(final List<PToken> tokens, final int startIndex)
	{
		for (int i = startIndex; i < tokens.size(); i++)
		{
			final String value = tokens.get(i).value();
			if (value == null || value.isBlank())
			{
				continue;
			}
			return i;
		}
		return -1;
	}

	private static boolean isHeaderKeyword(final String value)
	{
		return switch (value)
		{
			case "MetaModel", "Group", "Definition", "Enum", "Unit", "Generic", "Alias", "JavaWrapper", "includes" -> true;
			default -> false;
		};
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
