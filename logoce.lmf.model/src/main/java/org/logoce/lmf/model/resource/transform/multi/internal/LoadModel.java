package org.logoce.lmf.model.resource.transform.multi.internal;

import org.logoce.lmf.model.lexer.ELMTokenType;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.util.tree.Tree;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public record LoadModel(Tree<PNode> tree, String qualifiedName, List<String> imports)
{
	public static LoadModel from(Tree<PNode> tree)
	{
		final var node = tree.data();
		final var imports = resolveImports(node);
		final var domain = resolveDomain(node);
		final var name = resolveName(node);
		final var qualifiedName = domain.map(s -> s + "." + name).orElse(name);

		return new LoadModel(tree, qualifiedName, imports);
	}

	public static List<String> resolveImports(final PNode node)
	{
		final var it = node.tokens().iterator();
		while (it.hasNext())
		{
			final var token = it.next();
			if (token.type() == ELMTokenType.VALUE_NAME && token.value().equals("imports"))
			{
				if (it.hasNext()) it.next(); // skip ASSIGN

				final var values = new java.util.ArrayList<String>();
				while (it.hasNext())
				{
					final var next = it.next();
					if (next.type() == ELMTokenType.VALUE)
					{
						Arrays.stream(next.value().split(","))
							  .map(String::trim)
							  .filter(s -> !s.isEmpty())
							  .forEach(values::add);
					}
					else if (next.type() != ELMTokenType.LIST_SEPARATOR && next.type() != ELMTokenType.WHITE_SPACE)
					{
						break;
					}
				}
				return values;
			}
		}
		return List.of();
	}

	public static Optional<String> resolveDomain(final PNode node)
	{
		return extractValue(node, "domain");
	}

	public static String resolveName(final PNode node)
	{
		return extractValue(node, "name").orElseThrow(() -> new IllegalStateException("Model Name should be set"));
	}

	private static Optional<String> extractValue(final PNode node, final String property)
	{
		final var it = node.tokens().iterator();
		while (it.hasNext())
		{
			final var token = it.next();
			if (token.type() == ELMTokenType.VALUE_NAME && token.value().equals(property))
			{
				it.next();
				return Optional.of(it.next().value());
			}
		}
		return Optional.empty();
	}
}
