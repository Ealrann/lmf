package org.logoce.lmf.cli.format;

import org.logoce.lmf.core.loader.api.lexer.ELMTokenType;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;
import org.logoce.lmf.core.loader.api.text.syntax.PToken;
import org.logoce.lmf.core.loader.api.loader.linking.tree.LinkNodeInternal;
import org.logoce.lmf.core.util.tree.Tree;
import org.logoce.lmf.core.api.util.ModelUtil;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.Relation;

import java.util.List;

public final class LmFormatter
{
	private static final String INDENT = "\t";

	public String format(final List<Tree<PNode>> roots)
	{
		return format(roots, node -> null);
	}

	public String format(final List<Tree<PNode>> roots,
						 final java.util.function.Function<PNode, ReferencePathToNameIndex> indexProvider)
	{
		if (roots == null || roots.isEmpty())
		{
			return "";
		}

		final var builder = new StringBuilder();
		for (int i = 0; i < roots.size(); i++)
		{
			final var root = roots.get(i);
			final var index = indexProvider == null ? null : indexProvider.apply(root.data());
			formatNode(root, 0, builder, index);
			if (i < roots.size() - 1)
			{
				builder.append('\n').append('\n');
			}
		}
		return builder.toString();
	}

	public String format(final LinkNodeInternal<?, PNode, ?> root)
	{
		return format(root, null);
	}

	public String format(final LinkNodeInternal<?, PNode, ?> root,
						 final ReferencePathToNameIndex index)
	{
		if (root == null)
		{
			return "";
		}

		final var builder = new StringBuilder();
		formatLinkNode(root, 0, builder, index);
		return builder.toString();
	}

	private void formatNode(final Tree<PNode> node,
							final int depth,
							final StringBuilder out,
							final ReferencePathToNameIndex index)
	{
		indent(out, depth);
		out.append('(');
		appendTokens(out, node.data().tokens(), index, null);

		final var children = node.children();
		if (children.isEmpty())
		{
			out.append(')');
			return;
		}

		for (final var child : children)
		{
			out.append('\n');
			formatNode(child, depth + 1, out, index);
		}
		if (depth == 0)
		{
			out.append('\n');
			indent(out, depth);
		}
		out.append(')');
	}

	private void formatLinkNode(final LinkNodeInternal<?, PNode, ?> node,
								final int depth,
								final StringBuilder out,
								final ReferencePathToNameIndex index)
	{
		indent(out, depth);
		out.append('(');
		appendTokens(out, node.pNode().tokens(), index, node.group());

		final var children = node.streamChildren().toList();
		if (children.isEmpty())
		{
			out.append(')');
			return;
		}

		for (final var child : children)
		{
			out.append('\n');
			formatLinkNode(child, depth + 1, out, index);
		}
		if (depth == 0)
		{
			out.append('\n');
			indent(out, depth);
		}
		out.append(')');
	}

	private static void indent(final StringBuilder out, final int depth)
	{
		for (int i = 0; i < depth; i++)
		{
			out.append(INDENT);
		}
	}

	private static void appendTokens(final StringBuilder out,
									 final List<PToken> tokens,
									 final ReferencePathToNameIndex index,
									 final Group<?> nodeGroup)
	{
		PToken previous = null;
		boolean inQuote = false;
		String currentFeatureName = null;

		for (int i = 0; i < tokens.size(); i++)
		{
			final var token = tokens.get(i);
			if (token.type() == ELMTokenType.WHITE_SPACE)
			{
				continue;
			}

			if (token.type() == ELMTokenType.ASSIGN)
			{
				currentFeatureName = findAssignedFeatureName(tokens, i);
			}

			if (shouldInsertSpace(previous, token))
			{
				out.append(' ');
			}

			if (token.type() == ELMTokenType.QUOTE)
			{
				out.append('"');
				inQuote = !inQuote;
			}
			else if (token.type() == ELMTokenType.VALUE && inQuote)
			{
				out.append(escapeQuotedValue(token.value()));
			}
			else
			{
				final var value = token.value();
				if (!inQuote
					&& index != null
					&& token.type() == ELMTokenType.VALUE
					&& value != null
					&& value.startsWith("/"))
				{
					final var concept = resolveConceptGroup(nodeGroup, currentFeatureName);
					final var replacement = index.replacementForAbsolutePath(value, concept);
					out.append(replacement == null ? value : replacement);
				}
				else
				{
					out.append(value);
				}
			}

			previous = token;
		}
	}

	private static String findAssignedFeatureName(final List<PToken> tokens, final int assignIndex)
	{
		for (int i = assignIndex - 1; i >= 0; i--)
		{
			final var token = tokens.get(i);
			if (token.type() == ELMTokenType.WHITE_SPACE)
			{
				continue;
			}
			return token.type() == ELMTokenType.VALUE_NAME ? token.value() : null;
		}
		return null;
	}

	private static Group<?> resolveConceptGroup(final Group<?> nodeGroup, final String featureName)
	{
		if (nodeGroup == null || featureName == null || featureName.isBlank())
		{
			return null;
		}

		return ModelUtil.streamAllFeatures(nodeGroup)
						.filter(f -> featureName.equals(f.name()))
						.filter(Relation.class::isInstance)
						.map(Relation.class::cast)
						.map(Relation::concept)
						.filter(Group.class::isInstance)
						.map(Group.class::cast)
						.findFirst()
						.orElse(null);
	}

	private static boolean shouldInsertSpace(final PToken previous, final PToken current)
	{
		if (previous == null)
		{
			return false;
		}

		final ELMTokenType prevType = previous.type();
		final ELMTokenType curType = current.type();

		if (prevType == ELMTokenType.ASSIGN || curType == ELMTokenType.ASSIGN)
		{
			return false;
		}
		if (prevType == ELMTokenType.LIST_SEPARATOR || curType == ELMTokenType.LIST_SEPARATOR)
		{
			return false;
		}
		if (prevType == ELMTokenType.QUOTE && (curType == ELMTokenType.VALUE || curType == ELMTokenType.QUOTE))
		{
			return false;
		}
		if (curType == ELMTokenType.QUOTE && prevType == ELMTokenType.VALUE)
		{
			return false;
		}
		return true;
	}

	private static String escapeQuotedValue(final String value)
	{
		if (value == null || value.isEmpty())
		{
			return "";
		}

		final var builder = new StringBuilder(value.length());
		for (int i = 0; i < value.length(); i++)
		{
			final char c = value.charAt(i);
			if (c == '\\' || c == '"')
			{
				builder.append('\\');
			}
			builder.append(c);
		}
		return builder.toString();
	}
}
